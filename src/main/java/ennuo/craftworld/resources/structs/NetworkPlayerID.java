package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class NetworkPlayerID implements Serializable {
    public NetworkOnlineID handle = new NetworkOnlineID();
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

        boolean lengthPrefixed = serializer.revision.head < 0x234;

        if (lengthPrefixed) serializer.i32(8);
        id.opt = serializer.bytes(id.opt, 8);

        if (lengthPrefixed) serializer.i32(8);
        id.reserved = serializer.bytes(id.reserved, 8);

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
