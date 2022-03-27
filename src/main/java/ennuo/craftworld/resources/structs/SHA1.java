package ennuo.craftworld.resources.structs;

import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.utilities.StringUtils;

public class SHA1 {
    private byte[] hashBytes;
    private String hashString;
    
    public SHA1() {
        this.hashBytes = new byte[0x14];
        this.hashString = Bytes.toHex(this.hashBytes).toLowerCase();
    }
    
    public SHA1(String hash) {
        if (hash == null || hash.isEmpty())
            throw new NullPointerException("No hash passed to SHA1 constructor.");
        
        if (hash.length() == 41 && hash.startsWith("h"))
            hash = hash.substring(1);
        
        if (hash.length() > 40)
            throw new IllegalArgumentException("SHA1 hash must be 40 characters long!");
        
        // Maybe I should replace this with an error,
        // but I have to actually validate other parts of the
        // UI first before I can do this without causing errors.
        // This is bad design!
        if (hash.length() != 40)
            hash = StringUtils.leftPad(hash, 40);
        
        this.hashString = hash.toLowerCase();
        this.hashBytes = Bytes.toBytes(this.hashString);
    }
    
    public SHA1(byte[] hash) {
        if (hash.length != 0x14) {
            this.hashBytes = new byte[0x14];
            System.arraycopy(hash, 0, this.hashBytes, 0, hash.length);
        } else this.hashBytes = hash;
        this.hashString = Bytes.toHex(this.hashBytes).toLowerCase();
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
