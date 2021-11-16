package ennuo.craftworld.resources.structs;

import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.utilities.StringUtils;

public class SHA1 {
    private byte[] hashBytes;
    private String hashString;
    
    public SHA1() {
        this.hashBytes = new byte[0x14];
        this.hashString = Bytes.toHex(this.hashBytes);
    }
    
    public SHA1(String hash) {
        if (hash.length() != 40) 
            hash = StringUtils.leftPad(hash, 40);
        this.hashString = hashString;
        this.hashBytes = Bytes.toBytes(this.hashString);
    }
    
    public SHA1(byte[] hash) {
        if (hash.length != 0x14) {
            this.hashBytes = new byte[0x14];
            System.arraycopy(hash, 0, this.hashBytes, 0, hash.length);
        } else this.hashBytes = hash;
        this.hashString = Bytes.toHex(this.hashBytes);
    }
    
    public static SHA1 fromBuffer(byte[] buffer) {
        SHA1 hash = new SHA1(Bytes.SHA1(buffer));
        return hash;
    }
    
    public byte[] getHash() { return this.hashBytes; }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SHA1)) return false;
        SHA1 d = (SHA1)o;
        return d.toString().equals(this.toString());
    }
    
    @Override
    public int hashCode() { return this.hashString.hashCode(); }
    
    @Override
    public String toString() { return this.hashString; }
}
