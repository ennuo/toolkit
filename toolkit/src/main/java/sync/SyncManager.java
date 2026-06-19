package sync;

import java.awt.List;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import configurations.Config;
import cwlib.enums.CompressionFlags;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.singleton.ResourceSystem;
import cwlib.types.archives.FileArchive;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileDB;
import cwlib.util.BinaryPrimitives;
import cwlib.util.FileIO;
import toolkit.utilities.SlowOp;
import toolkit.windows.Toolkit;
import toolkit.windows.utilities.SlowOpGUI;

public class SyncManager 
{
    public static final SyncManager instance = new SyncManager();
    private static final int MESSAGE_HEADER_SIZE = 16;

    public static enum SyncEvent
    {
        ConnectionEstablished,
        ConnectionLost,
        DepotDownloaded,
        DepotCreated
    };

    public static enum ConnectionState
    {
        Disconnected,
        Establishing,
        Established,
        WaitingForServerInfo,
        Authenticating,
        FetchingDepotList,
        Connected
    };

    private ConnectionState _state = ConnectionState.Disconnected;
    private Socket _socket;
    private Thread _thread;
    private boolean _wantQuit = false;
    private Object _sendLock = new Object();
    private Object _depotLock = new Object();
    private byte[] _send = new byte[MESSAGE_HEADER_SIZE];
    private byte[] _recv = new byte[Short.MAX_VALUE];
    private FileArchive _cache;

    private byte[] _session;
    public long _account;
    private int _version;
    private ArrayList<Depot> _depots = new ArrayList<>();
    private SHA1[] _filteredHashes;
    private boolean _inCommitTask = false;
    private boolean _waitingForCommitResponse;
    private int _lastCommitStatus;
    private String _requestedDepotCreationId;

    private ArrayList<BiConsumer<SyncEvent, Object>> _callbacks = new ArrayList<>();

    public void reset()
    {
        disconnect();
        _callbacks.clear();
        
        if (_cache != null)
        {
            _cache.getFile().delete();
            _cache = null;
        }

        try
        {
            var file = File.createTempFile("toolkit-synccache-", ".farc");
            file.deleteOnExit();
            FileIO.write(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x46, 0x41, 0x52, 0x43 }, file.getAbsolutePath());
            _cache = new FileArchive(file);
            _cache.setDisableQueue();
        }
        catch (Exception ex)
        {
            System.out.println("Failed to create temporary cache for sync depots, this may cause issues!");
        }
    }

    public FileArchive getCache()
    {
        return _cache;
    }

    public void create(String shortName, String displayName, String branch)
    {
        var msg = new MemoryOutputStream();
        msg.str(shortName);
        msg.str(displayName);
        msg.str(branch);

        _requestedDepotCreationId = shortName;

        send(msg.flush(), MessageChannel.SYNC, MessageType.CREATE_DEPOT);
    }

    public boolean canDownload()
    {
        return _state == ConnectionState.Connected && !_inCommitTask;
    }

    public ArrayList<Depot> getDepots()
    {
        synchronized (_depotLock)
        {
            return _depots;
        }
    }
    
    public ConnectionState getState()
    {
        return _state;
    }

    public void registerCallback(BiConsumer<SyncEvent, Object> cb)
    {
        _callbacks.add(cb);
    }

    public SHA1[] filter(SHA1[] hashes)
    {
        // assume only one filter request at a time
        _filteredHashes = null;

        var stream = new MemoryOutputStream(4 + (hashes.length * 0x14));
        stream.i32(hashes.length);
        for (var hash : hashes)
            stream.sha1(hash);

        if (!send(stream.getBuffer(), MessageChannel.RESOURCE, MessageType.FILTER_RESOURCES))
            return null;

        while (_filteredHashes == null)
        {
            if (_state == ConnectionState.Disconnected)
                return null;

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ex)
            {
                return null;
            }
        }

        return _filteredHashes;
    }

    public byte[] downloadSync(SHA1 hash)
    {
        if (!canDownload()) return null;
        
        var config = Config.sync();

        var msg = new byte[MESSAGE_HEADER_SIZE + 32 + 0x1 + 0x14];
        System.arraycopy(_session, 0, msg, MESSAGE_HEADER_SIZE, 32);
        msg[MESSAGE_HEADER_SIZE + 32] = 1;
        System.arraycopy(hash.getHash(), 0, msg, MESSAGE_HEADER_SIZE + 32 + 1, 0x14);

        BinaryPrimitives.writeInt32BigEndian(msg, 0, msg.length);
        BinaryPrimitives.writeInt32BigEndian(msg, 4, MessageType.DOWNLOAD);
        BinaryPrimitives.writeInt32BigEndian(msg, 8, MessageChannel.RESOURCE);

        try (Socket client = new Socket(config.address, config.port))
        {
            client.getOutputStream().write(msg);

            DataInputStream stream = new DataInputStream(new BufferedInputStream(client.getInputStream()));
            msg = stream.readNBytes(MESSAGE_HEADER_SIZE);

            if (BinaryPrimitives.readInt32BigEndian(msg, 12) != MessageResponse.OK)
                return null;

            int length = BinaryPrimitives.readInt32BigEndian(msg, 0) - MESSAGE_HEADER_SIZE;
            msg = stream.readNBytes(length);

            length = BinaryPrimitives.readInt32BigEndian(msg, 0x14);

            return stream.readNBytes(length);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private boolean tryCommitSync(CommitInfo info)
    {
        var stream = new MemoryOutputStream();
        stream.u64(info.id);
        stream.i32(info.files.size());
        for (var file : info.files)
        {
            stream.guid(file.fileGuid);
            stream.i32(file.fileSize);
            if (file.IsDeletedFile()) continue;
            stream.sha1(file.fileHash);
            stream.str(file.path);
        }

        _waitingForCommitResponse = true;
        if (!send(stream.flush(), MessageChannel.SYNC, MessageType.COMMIT))
        {
            System.out.println("failed to send commit message");
            return false;
        }

        while (_waitingForCommitResponse)
        {
            if (_state == ConnectionState.Disconnected)
                return false;

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ex)
            {
                return false;
            }
        }

        return _lastCommitStatus == MessageResponse.OK;
    }


    public boolean uploadSync(SHA1 hash)
    {
        var config = Config.sync();
        if (_state != ConnectionState.Connected) return false;

        byte[] fileData = ResourceSystem.extract(hash);
        if (fileData == null) return false;

        var msg = new byte[MESSAGE_HEADER_SIZE + 32 + 0x14 + 0x4];
        System.arraycopy(_session, 0, msg, MESSAGE_HEADER_SIZE, 32);
        System.arraycopy(hash.getHash(), 0, msg, MESSAGE_HEADER_SIZE + 32, 0x14);
        BinaryPrimitives.writeInt32BigEndian(msg, MESSAGE_HEADER_SIZE + 32 + 0x14, fileData.length);

        BinaryPrimitives.writeInt32BigEndian(msg, 0, msg.length);
        BinaryPrimitives.writeInt32BigEndian(msg, 4, MessageType.UPLOAD_RESOURCE);
        BinaryPrimitives.writeInt32BigEndian(msg, 8, MessageChannel.RESOURCE);

        try (Socket client = new Socket(config.address, config.port))
        {
            client.getOutputStream().write(msg);
            client.getOutputStream().write(fileData);

            DataInputStream stream = new DataInputStream(new BufferedInputStream(client.getInputStream()));
            msg = stream.readNBytes(MESSAGE_HEADER_SIZE);

            return BinaryPrimitives.readInt32BigEndian(msg, 12) == MessageResponse.OK;
        }
        catch (Exception ex)
        {
            return false;
        }
    }
    
    public boolean commit(CommitInfo info)
    {
        _inCommitTask = true;
        boolean ok = SlowOpGUI.performSlowOperation(Toolkit.INSTANCE, "IKAROS//SYNC", "Comitting...", -1, new SlowOp() 
        {
            @Override public int run(SlowOpGUI state) 
            {
                var files = info.files;

                var depot = get(info.id);
                if (depot == null)
                {
                    state.setErrorMessage("Depot did not exist on server!");
                    return -1;
                }

                var hashSet = new HashSet<SHA1>(files.size());
                state.setMessage("Gathering hashes for upload...");
                for (var file : files)
                {
                    if (file.IsDeletedFile()) continue;
                    hashSet.add(file.fileHash);
                }

                state.setMessage("Filtering hashes on server...");
                var hashes = filter(hashSet.toArray(SHA1[]::new));
                _filteredHashes = null;
                if (hashes == null)
                {
                    state.setErrorMessage("Failed to filter resources!");
                    return -1;
                }

                if (hashes.length > 0)
                {
                    state.setMessage("Uploading files...\n");
                    state.setMaxProgress(hashes.length);
                    for (int i = 0; i < hashes.length; ++i)
                    {
                        var hash = hashes[i];

                        state.setProgress(i);
                        state.setMessage("Uploading h" + hashes[i]);

                        if (!uploadSync(hash))
                        {
                            var file = info.find(hash);
                            if (file != null)
                            {
                                state.setErrorMessage(String.format("Failed to upload h%s (%s)", hashes[i], file.path));
                            }
                            else
                            {
                                state.setErrorMessage("Failed to upload h" + hashes[i]);
                            }
                            return -1;
                        }
                    }

                    state.setMaxProgress(-1);
                }

                state.setMessage("Comitting...");

                if (tryCommitSync(info))
                {
                    depot.Database = new NetworkFileDB(depot, depot.WorkingDatabase.build());
                    return 0;
                }
                else
                {
                    state.setErrorMessage("Failed to commit to server!");
                    return -1;
                }
            }
            
        });
        
        _inCommitTask = false;
        return ok;
    }

    public void clear(long id)
    {
        var depot = get(id);
        if (depot == null) return;

        synchronized(_depotLock)
        {
            depot.Database = null;
            depot.WorkingDatabase = null;
        }
    }

    public void request(long id)
    {
        var depot = get(id);
        if (depot == null) return;

        synchronized (_depotLock)
        {
            if (depot.Database != null)
            {
                for (var cb : _callbacks)
                    cb.accept(SyncEvent.DepotDownloaded, depot);

                return;
            }
        }

        byte[] span = new byte[Long.BYTES];
        BinaryPrimitives.writeInt64BigEndian(span, 0, id);
        if (!send(span, MessageChannel.SYNC, MessageType.DEPOT))
            disconnect();
    }

    public Depot get(long id)
    {
        synchronized (_depotLock)
        {
            for (var depot : _depots)
            {
                if (depot.Id == id)
                    return depot;
            }

            return null;
        }
    }

    public boolean send(byte[] data, int channel, int type)
    {
        if (_state == ConnectionState.Disconnected) return false;

        int datalen = data != null ? data.length : 0;

        int size = MESSAGE_HEADER_SIZE + datalen;
        synchronized(_sendLock)
        {
            BinaryPrimitives.writeInt32BigEndian(_send, 0, size);
            BinaryPrimitives.writeInt32BigEndian(_send, 4, type);
            BinaryPrimitives.writeInt32BigEndian(_send, 8, channel);
            BinaryPrimitives.writeInt32BigEndian(_send, 12, (short)0 /* eResponse_OK */);
            
            try
            {
                _socket.getOutputStream().write(_send);
                if (datalen > 0)
                    _socket.getOutputStream().write(data);

                return true;
            }
            catch (IOException ioex)
            {
                disconnect();
                return false;
            }
        }
    }

    private void threadfunc()
    {
        var config = Config.sync();
        while (!_wantQuit)
        {
            if (_state == ConnectionState.Disconnected)
            {
                _state = ConnectionState.Establishing;
                try
                {
                    _socket = new Socket(config.address, config.port);
                    _state = ConnectionState.Established;
                }
                catch (Exception ex)
                {
                    System.out.println("Could not establish connection to sync server!");
                    disconnect();
                    return;
                }

                _state = ConnectionState.Established;
            }
            else if (_state == ConnectionState.Established)
            {
                // Request server information and protocol version so we can
                // see if our client is compatible with the server.
                if (send(null, MessageChannel.GATE, MessageType.SERVER_INFO))
                    _state = ConnectionState.WaitingForServerInfo;
            }
            else
            {
                try
                {
                    _socket.getInputStream().readNBytes(_recv, 0, MESSAGE_HEADER_SIZE);
                    int length = BinaryPrimitives.readInt32BigEndian(_recv, 0);
                    int message = BinaryPrimitives.readInt32BigEndian(_recv, 4);
                    int channel = BinaryPrimitives.readInt32BigEndian(_recv, 8);
                    int status = BinaryPrimitives.readInt32BigEndian(_recv, 12);

                    length -= MESSAGE_HEADER_SIZE;

                    byte[] data = _recv;
                    if (length > 0)
                    {
                        if (length > Short.MAX_VALUE)
                            data = new byte[length];
                        _socket.getInputStream().readNBytes(data, 0, length);
                    }

                    var resp = new MemoryInputStream(data, CompressionFlags.USE_NO_COMPRESSION);

                    switch (_state)
                    {
                        case WaitingForServerInfo:
                        {
                            if (message != MessageType.SERVER_INFO)
                            {
                                System.out.println("Expected server info response from server, closing sync connection!");
                                disconnect();
                                return;
                            }

                            if (status != MessageResponse.OK)
                            {
                                System.out.println("Server info response contained an error, closing sync connection!");
                                disconnect();
                                return;
                            }

                            try
                            {
                                _version = resp.i32();
                                String name = resp.str();
                                String version = resp.str();
                                String build = resp.str();

                                System.out.printf("Connecting to %s (version=%s, build_date=%s)\n", name, version, build);
                            }
                            catch (Exception ex)
                            {
                                System.out.println("An error occurred while parsing server info response, closing sync connection!");
                                disconnect();
                                return;
                            }


                            final int AUTHENTICATION_TYPE_CREDENTIALS = 1;
                            final int MAX_MESSAGE_BUFFER = 128;

                            var stream = new MemoryOutputStream(MAX_MESSAGE_BUFFER, CompressionFlags.USE_NO_COMPRESSION);
                            stream.s32(AUTHENTICATION_TYPE_CREDENTIALS);
                            stream.str(config.username);
                            stream.str(config.password);

                            if (send(stream.shrink().getBuffer(), MessageChannel.GATE, MessageType.LOGIN))
                                _state = ConnectionState.Authenticating;

                            break;
                        }
                        case Authenticating:
                        {
                            if (message != MessageType.LOGIN)
                            {
                                System.out.println("Expected login response from server, closing sync connection!");
                                disconnect();
                                return;
                            }

                            if (status != MessageResponse.OK)
                            {
                                System.out.println("Failed to authenticate against server, closing sync connection!");
                                disconnect();
                                return;
                            }

                            _account = resp.u64();
                            _session = resp.bytes(32);

                            System.out.println("Connection established to sync server!");

                            System.out.println("Fetching depot list from sync server...");
                            if (send(null, MessageChannel.SYNC, MessageType.DEPOT_LIST))
                                _state = ConnectionState.FetchingDepotList;

                            break;
                        }
                        case FetchingDepotList:
                        {
                            if (message != MessageType.DEPOT_LIST)
                            {
                                System.out.println("Expected depot list from server, closing sync connection!");
                                disconnect();
                                return;
                            }

                            if (status != MessageResponse.OK)
                            {
                                System.out.println("Failed to fetch depot list!");
                                disconnect();
                                return;
                            }

                            synchronized (_depotLock)
                            {
                                int numDepots = resp.i32();
                                _depots = new ArrayList<>(numDepots);
                                System.out.printf("Got %d depots from server\n", numDepots);
                                for (int i = 0; i < numDepots; ++i)
                                {
                                    var depot = new Depot();
                                    depot.Id = resp.u64();
                                    depot.CommitId = resp.u64();
                                    depot.UniqueIdentifier = resp.str();
                                    depot.DisplayName = resp.str();
                                    depot.Branch = resp.str();
                                    depot.Priority = resp.i32();
                                    depot.Flags = resp.i32();

                                    System.out.printf("\t%s\n", depot.UniqueIdentifier);

                                    _depots.add(depot);
                                }
                            }

                            System.out.println("Sync server successfully connected!");
                            _state = ConnectionState.Connected;
                            for (var callback : _callbacks)
                                callback.accept(SyncEvent.ConnectionEstablished, null);
                        
                            break;
                        }
                        case Connected:
                        {
                            switch (message)
                            {
                                case MessageType.ON_DEPOT_COMMIT: break;
                                case MessageType.ON_DEPOT_CREATED:
                                {
                                    var depot = new Depot();
                                    depot.Id = resp.u64();
                                    depot.CommitId = resp.u64();
                                    depot.UniqueIdentifier = resp.str();
                                    depot.DisplayName = resp.str();
                                    depot.Branch = resp.str();
                                    depot.Priority = resp.i32();
                                    depot.Flags = resp.i32();

                                    synchronized (_depotLock)
                                    {
                                        _depots.add(depot);
                                    }

                                    for (var cb : _callbacks)
                                        cb.accept(SyncEvent.DepotCreated, depot);

                                    if (depot.UniqueIdentifier.equals(_requestedDepotCreationId))
                                    {
                                        _requestedDepotCreationId = null;
                                        request(depot.Id);
                                    }

                                    break;
                                }
                                case MessageType.FILTER_RESOURCES:
                                {
                                    int numHashes = resp.i32();
                                    var hashes = new SHA1[numHashes];
                                    for (int i = 0; i < numHashes; ++i)
                                        hashes[i] = resp.sha1();
                                    _filteredHashes = hashes;
                                    break;
                                }
                                case MessageType.COMMIT:
                                {
                                    _lastCommitStatus = status;
                                    _waitingForCommitResponse = false;
                                    break;
                                }
                                case MessageType.DEPOT:
                                {
                                    if (status != MessageResponse.OK) break;

                                    long id = resp.u64();
                                    long commit = resp.u64();
                                    byte[] fileData = resp.bytearray();


                                    var depot = get(id);
                                    if (depot == null) break;

                                    synchronized (_depotLock)
                                    {
                                        depot.CommitId = commit;

                                        // lazy hack, create readonly copy with gui mode disabled
                                        boolean backup = ResourceSystem.GUI_MODE;
                                        ResourceSystem.GUI_MODE = false;
                                        depot.Database = new NetworkFileDB(depot, fileData);
                                        ResourceSystem.GUI_MODE = backup;

                                        depot.WorkingDatabase = new NetworkFileDB(depot, fileData);
                                    }

                                    for (var cb : _callbacks)
                                        cb.accept(SyncEvent.DepotDownloaded, depot);


                                    break;
                                }
                            }

                            break;
                        }
                        default:
                        {
                            System.out.println("Received message in invalid state!");
                            break;
                        }
                    }
                }
                catch (Exception ex)
                {
                    disconnect();
                    return;
                }

            }

            try { Thread.sleep(100); }
            catch (Exception ex) { /* Who cares? */ }
        }
    }


    public boolean connect()
    {
        // Don't attempt to reconnect if we're either already connected
        // or in the process of connecting,
        if (_state != ConnectionState.Disconnected)
            return true;
        
        
        var config = Config.sync();

        // Requires authentication to be able to access depot list.
        if (!config.hasCredentials())
        {
            System.out.println("No credentials, cannot connect to sync server!");
            return false;
        }

        System.out.println("Establishing connection to sync server...");

        // Perform the rest of the connection process and message handling
        // on a separate thread.
        _thread = new Thread(() -> {
            SyncManager.instance.threadfunc();
        });

        _thread.start();

        return true;
    }

    public void disconnect()
    {
        if (_state == ConnectionState.Disconnected) return;

        for (var callback : _callbacks)
            callback.accept(SyncEvent.ConnectionLost, null);

        _wantQuit = true;

        try 
        { 
            if (_socket != null)
                _socket.close(); 
        } 
        catch (IOException e) 
        {
            // Don't really give a shit if this causes an exception,
            // socket will ultimately be closed.
        }
        
        _socket = null;
        _state = ConnectionState.Disconnected;

        if (_thread != null && Thread.currentThread() != _thread)
        {
            try
            {
                _thread.join();
            }
            catch (Exception ex)
            {
                // Womp
            }
        }

        _thread = null;
        _wantQuit = false;
        _depots.clear();
    }
}
