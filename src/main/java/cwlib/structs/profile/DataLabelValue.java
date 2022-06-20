package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.NetworkOnlineID;
import cwlib.types.data.Revision;

public class DataLabelValue implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public NetworkOnlineID creatorID;
    public int labelIndex;
    public float[] analogue;
    public byte[] ternary;

    @SuppressWarnings("unchecked")
    @Override public DataLabelValue serialize(Serializer serializer, Serializable structure) {
        DataLabelValue value = (structure == null) ? new DataLabelValue() : (DataLabelValue) structure;

        Revision revision = serializer.getRevision();
        int head = revision.getVersion();

        value.creatorID = serializer.struct(value.creatorID, NetworkOnlineID.class);
        value.labelIndex = serializer.i32(value.labelIndex);

        if (revision.isAfterVitaRevision(0x2d) && !revision.isAfterVitaRevision(0x32)) {
            if (serializer.isWriting()) {
                if (value.analogue != null && value.analogue.length != 0)
                    serializer.getOutput().f32(value.analogue[0]);
                else
                    value.analogue = new float[] { serializer.getInput().f32() };
            }
        } else if (revision.isAfterVitaRevision(0x32) || head > 0x3ee)
            value.analogue = serializer.floatarray(value.analogue);

        if (revision.isAfterVitaRevision(0x3b) || head > 0x3ee)
            value.ternary = serializer.bytearray(value.ternary);
        
        return value;
    }

    @Override public int getAllocatedSize() {
        int size = DataLabelValue.BASE_ALLOCATION_SIZE;
        if (this.analogue != null)
            size += (this.analogue.length * 4);
        if (this.ternary != null)
            size += (this.ternary.length);
        return size;
    }
}
