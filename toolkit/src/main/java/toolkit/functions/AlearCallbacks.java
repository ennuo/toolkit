package toolkit.functions;

import cwlib.enums.ResourceType;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.singleton.ResourceSystem;
import cwlib.types.data.*;
import cwlib.types.databases.FileEntry;
import cwlib.types.swing.FileNode;
import cwlib.util.Resources;

import java.awt.event.ActionEvent;
import java.net.Socket;

public class AlearCallbacks
{
    public static class PacketType
    {
        /* Client -> Server */
        public static final int NOTIFICATION = 0;
        public static final int RESOURCE = 1;
        public static final int CHANGE_LEVEL = 2;
        public static final int ADD_PLAN = 3;
        public static final int ADD_LEVEL_DESCRIPTOR = 4;
        public static final int CLEAR_LEVEL_DESCRIPTORS = 5;
        public static final int SET_THING_TRANSFORM = 7;
        public static final int ADD_MESH = 8;
        public static final int SET_BACKGROUND = 9;
        public static final int RELOAD_RESOURCE = 10;

        /* Server -> Client */
        public static final int LOAD_LEVEL_DESCRIPTOR = 6;
    }

    public static SHA1 sendResourceDataMessage(Socket client, byte[] data, String name)
    throws Exception
    {
        if (data == null || name == null || name.isEmpty()) return null;

        MemoryOutputStream packet =
            new MemoryOutputStream(32 + data.length + name.length() + 1);
        packet.u16(PacketType.RESOURCE);
        packet.u16(packet.getLength());

        packet.u32(data.length);

        SHA1 sha1 = SHA1.fromBuffer(data);
        packet.sha1(sha1);

        packet.u32(name.length() + 1);
        packet.str(name, name.length() + 1);

        packet.bytes(data);

        client.getOutputStream().write(packet.getBuffer());

        return sha1;
    }

    public static void sendAddPlanMessage(Socket client, GUID guid, SHA1 sha1)
    throws Exception
    {
        MemoryOutputStream packet = new MemoryOutputStream(4 + 4 + 0x14);
        packet.u16(PacketType.ADD_PLAN);
        packet.u16(packet.getLength());

        packet.guid(guid);
        packet.sha1(sha1);

        client.getOutputStream().write(packet.getBuffer());
    }

    public static void upload(ActionEvent event)
    {
        Socket client = null;

        FileNode node = ResourceSystem.getSelected();
        ResourceInfo info = node.getEntry().getInfo();
        if (info == null || info.getType() != ResourceType.PLAN || info.getResource() == null)
            return;

        FileEntry entry = node.getEntry();

        ResourceDescriptor descriptor;
        if (entry.getSource().getType().hasGUIDs())
            descriptor = new ResourceDescriptor((GUID) entry.getKey(), ResourceType.PLAN);
        else
            descriptor = new ResourceDescriptor(entry.getSHA1(), ResourceType.PLAN);

        byte[] data = ResourceSystem.extract(entry);
        if (data == null) return;

        try
        {
            client = new Socket("127.0.0.1", 1337);

            GatherData[] entries = Resources.hashinate(data, descriptor);
            GatherData root = entries[entries.length - 1];

            for (GatherData gatherable : entries)
                sendResourceDataMessage(client, gatherable.getData(), gatherable.getPath());

            sendAddPlanMessage(client, null, root.getSHA1());

            client.close();
        }
        catch (Exception ex)
        {
            System.out.println("Alear resource transfer failed!");
        }
    }
}
