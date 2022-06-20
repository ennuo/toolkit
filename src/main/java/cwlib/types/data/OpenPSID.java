package cwlib.types.data;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class OpenPSID implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    private long high, low;

    public OpenPSID() {};
    public OpenPSID(long high, long low) {
        this.high = high;
        this.low = low;
    }

    @SuppressWarnings("unchecked")
    @Override public OpenPSID serialize(Serializer serializer, Serializable structure) {
        OpenPSID psid = (structure == null) ? new OpenPSID() : (OpenPSID) structure;

        psid.high = serializer.i64(psid.high);
        psid.low = serializer.i64(psid.low);

        return psid;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }

    public long getHigh() { return this.high; }
    public long getLow() { return this.low; }
}
