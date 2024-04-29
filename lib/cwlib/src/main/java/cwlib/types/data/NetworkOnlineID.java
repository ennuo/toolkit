package cwlib.types.data;

import com.google.gson.annotations.JsonAdapter;
import cwlib.io.Serializable;
import cwlib.io.gson.NetworkOnlineIDSerializer;
import cwlib.io.serializer.Serializer;

import java.nio.charset.StandardCharsets;

@JsonAdapter(NetworkOnlineIDSerializer.class)
public class NetworkOnlineID implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    private byte[] data = new byte[16];
    private byte term = '\0';
    private byte[] dummy = new byte[3];

    public NetworkOnlineID() { }

    public NetworkOnlineID(String psid)
    {
        if (psid == null) return;
        if (psid.length() > 16)
            throw new IllegalArgumentException("PSID can only be between 0 and 16 " +
                                               "characters!");
        System.arraycopy(psid.getBytes(StandardCharsets.US_ASCII), 0, this.data, 0,
            psid.length());
    }

    @Override
    public void serialize(Serializer serializer)
    {
        boolean lengthPrefixed = serializer.getRevision().getVersion() < 0x234;

        if (lengthPrefixed) serializer.i32(16);
        data = serializer.bytes(data, 16);

        term = serializer.i8(term);

        if (lengthPrefixed) serializer.i32(3);
        dummy = serializer.bytes(dummy, 3);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }

    public void setData(String data)
    {
        if (data == null)
            throw new NullPointerException("Data cannot be null!");
        if (data.length() > 16)
            throw new IllegalArgumentException("PSID can only be between 0 and 16 " +
                                               "characters!");
        this.data = new byte[16];
        System.arraycopy(data.getBytes(StandardCharsets.US_ASCII), 0, this.data, 0,
            data.length());
    }

    @Override
    public String toString()
    {
        return new String(this.data).replace("\0", "");
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this) return true;
        if (!(other instanceof NetworkOnlineID otherID)) return false;
        return otherID.toString().equals(this.toString());
    }

    @Override
    public int hashCode()
    {
        return this.toString().hashCode();
    }
}
