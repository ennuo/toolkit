package cwlib.types.data;

import com.google.gson.annotations.JsonAdapter;

import cwlib.io.gson.SHA1Serializer;
import cwlib.util.Bytes;
import cwlib.util.Crypto;

/**
 * Encapsulates a fixed size byte array to 
 * represent a SHA1 hash.
 */
@JsonAdapter(SHA1Serializer.class)
public final class SHA1 {
    public static final SHA1 EMPTY = new SHA1();
    
    private final byte[] hashBytes;
    private final String hashString;
    
    /**
     * Creates an empty SHA1.
     */
    public SHA1() {
        this.hashBytes = new byte[0x14];
        this.hashString = Bytes.toHex(this.hashBytes).toLowerCase();
    }
    
    /**
     * Constructs a SHA1 hash from a 40 character string.
     * @param hash SHA1 hash string
     */
    public SHA1(String hash) {
        if (hash == null)
            throw new NullPointerException("SHA1 hash string cannot be null!");
        if (hash.length() != 40) 
            throw new IllegalArgumentException("SHA1 hash string must be 40 characters in length!");
        this.hashString = hash.toLowerCase();
        this.hashBytes = Bytes.fromHex(hash);
    }
    
    /**
     * Creates a SHA1 instance from a 20 byte buffer.
     * @param hash SHA1 source buffer
     */
    public SHA1(byte[] hash) {
        if (hash == null)
            throw new NullPointerException("SHA1 hash cannot be null!");
        if (hash.length != 0x14)
            throw new IllegalArgumentException("SHA1 hash must be 20 bytes in length!");
        this.hashBytes = hash;
        this.hashString = Bytes.toHex(this.hashBytes).toLowerCase();
    }
    
    /**
     * Computes a SHA1 hash from a buffer.
     * @param buffer Source buffer to be hashed
     * @return A SHA1 hash instance
     */
    public static SHA1 fromBuffer(byte[] buffer) {
        if (buffer == null)
            throw new NullPointerException("Data buffer provided to SHA1 hasher cannot be null!");
        return Crypto.SHA1(buffer);
    }
    
    public byte[] getHash() { return this.hashBytes; }
    
    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof SHA1)) return false;
        final SHA1 otherSHA1 = (SHA1) other;
        return otherSHA1.toString().equals(this.toString());
    }

    @Override public int hashCode() { return this.hashString.hashCode(); }
    @Override public String toString() { return this.hashString; }
}
