package cwlib.types.data;

import com.google.gson.annotations.JsonAdapter;

import cwlib.io.Serializable;
import cwlib.io.gson.NetworkPlayerIDSerializer;
import cwlib.io.serializer.Serializer;

@JsonAdapter(NetworkPlayerIDSerializer.class)
public class NetworkPlayerID implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = NetworkOnlineID.BASE_ALLOCATION_SIZE + 0x10;

    private NetworkOnlineID handle = new NetworkOnlineID();
    private byte[] opt = new byte[8];
    private byte[] reserved = new byte[8];
    
    public NetworkPlayerID() {};
    public NetworkPlayerID(String psid) {
        this.handle = new NetworkOnlineID(psid);
    }

    @SuppressWarnings("unchecked")
    @Override public NetworkPlayerID serialize(Serializer serializer, Serializable structure) {
        NetworkPlayerID id = (structure == null) ? new NetworkPlayerID() : (NetworkPlayerID) structure;

        id.handle = serializer.struct(id.handle, NetworkOnlineID.class);

        boolean lengthPrefixed = serializer.getRevision().getVersion() < 0x234;

        if (lengthPrefixed) serializer.i32(8);
        id.opt = serializer.bytes(id.opt, 8);

        if (lengthPrefixed) serializer.i32(8);
        id.reserved = serializer.bytes(id.reserved, 8);

        return id;
    }

    public NetworkOnlineID getHandle() { return this.handle; }

    
    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
    
    @Override public NetworkPlayerID clone() {
        NetworkPlayerID id = new NetworkPlayerID();
        id.handle = new NetworkOnlineID(id.handle.toString());
        id.opt = this.opt.clone();
        id.reserved = this.reserved.clone();
        return id;
    }
    
    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof NetworkPlayerID)) return false;
        NetworkPlayerID otherID = (NetworkPlayerID) other;
        return otherID.handle.equals(this.handle);
    }
    
    @Override public String toString() {
        return this.handle.toString();
    }
    
    @Override public int hashCode() {
        return this.handle.hashCode();
    }
}
