package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import java.nio.charset.StandardCharsets;

public class NetworkOnlineID implements Serializable {  
    private byte[] data = new byte[16]; 
    private byte term = '\0';
    private byte[] dummy = new byte[3];
    
    public NetworkOnlineID() {};
    public NetworkOnlineID(String psid) {
        if (psid == null) return;
        if (psid.length() > 16)
            throw new IllegalArgumentException("PSID can only be between 0 and 16 characters!");
        
        System.arraycopy(psid.getBytes(StandardCharsets.US_ASCII), 0, this.data, 0, psid.length());
    }

    @SuppressWarnings("unchecked")
    @Override public NetworkOnlineID serialize(Serializer serializer, Serializable structure) {
        NetworkOnlineID id = (structure == null) ? new NetworkOnlineID() : (NetworkOnlineID) structure;
       
        boolean lengthPrefixed = serializer.revision.head < 0x234;
        
        if (lengthPrefixed) serializer.i32(16);
        id.data = serializer.bytes(id.data, 16);
        
        id.term = serializer.i8(id.term);
        
        if (lengthPrefixed) serializer.i32(3);
        id.dummy = serializer.bytes(id.dummy, 3);
        
        return id;
    }
    
    public void setData(String data) {
        if (data == null)
            throw new NullPointerException("Data cannot be null!");
        if (data.length() != 16)
            throw new IllegalArgumentException("Data must be 16 bytes in length!");
        this.data = new byte[16];
        System.arraycopy(data.getBytes(StandardCharsets.US_ASCII), 0, this.data, 0, data.length());
    }
    
    @Override public String toString() {
        return new String(this.data).replace("\0", "");
    }
    
    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof NetworkOnlineID)) return false;
        NetworkOnlineID otherID = (NetworkOnlineID) other;
        return otherID.toString().equals(this.toString());
    }
    
    @Override public int hashCode() { return this.toString().hashCode(); }
}
