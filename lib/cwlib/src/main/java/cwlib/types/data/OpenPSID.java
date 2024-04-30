package cwlib.types.data;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class OpenPSID implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    private long high, low;

    public OpenPSID() { }

    public OpenPSID(long high, long low)
    {
        this.high = high;
        this.low = low;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        high = serializer.u64(high);
        low = serializer.u64(low);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }

    public long getHigh()
    {
        return this.high;
    }

    public long getLow()
    {
        return this.low;
    }
}
