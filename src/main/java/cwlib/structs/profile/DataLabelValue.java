package cwlib.structs.profile;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.NetworkOnlineID;
import cwlib.types.data.Revision;

public class DataLabelValue implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public NetworkOnlineID creatorID;
    public String labelName;
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

        if (revision.isVita()) {
            
            if (revision.has(Branch.DOUBLE11, Revisions.D1_LABEL_ANALOGUE_ARRAY))
                value.analogue = serializer.floatarray(value.analogue);
            else if (revision.has(Branch.DOUBLE11, Revisions.D1_DATALABELS)) {
                if (value.analogue != null && value.analogue.length != 0)
                    serializer.getOutput().f32(value.analogue[0]);
                else
                    value.analogue = new float[] { serializer.getInput().f32() };
            }

            if (revision.has(Branch.DOUBLE11, Revisions.D1_LABEL_TERNARY))
                value.ternary = serializer.bytearray(value.ternary);
        } else if (head >= Revisions.DATALABELS) {
            value.analogue = serializer.floatarray(value.analogue);
            value.ternary = serializer.bytearray(value.ternary);
        }
        
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
