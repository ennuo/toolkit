package cwlib.types.data;

import com.google.gson.annotations.JsonAdapter;

import cwlib.io.gson.GUIDSerializer;

/**
 * Used to represent unique identifiers for resources in-game,
 * due to Java being a signed language, it's not optimal to use
 * integers to represent them, and if longs were used, it brings the
 * possibility of overflowing the data.
 */
@JsonAdapter(GUIDSerializer.class)
public final class GUID {
    /**
     * The serialized representation of a GUID is a uint32_t, so the
     * max value should be reflected as such.
     */
    private final static long MAX_VALUE = 4_294_967_295L;
    private final long value;

    public GUID(long value) {
        if (value > GUID.MAX_VALUE || value <= 0)
            throw new IllegalArgumentException("GUID can only be between 0 and 4294967295");
        this.value = value;
    }

    public long getValue() { return this.value; }

    @Override public String toString() {
        return "g" + this.value;
    }

    @Override public int hashCode() { 
        return Long.valueOf(this.value).hashCode(); 
    }

    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof Long)
            return ((long)other) == this.value;
        if (!(other instanceof GUID)) return false;
        final GUID otherGUID = (GUID) other;
        return otherGUID.value == this.value;
    }
}
