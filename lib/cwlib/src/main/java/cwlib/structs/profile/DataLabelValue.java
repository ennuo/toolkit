package cwlib.structs.profile;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.NetworkOnlineID;
import cwlib.types.data.Revision;

public class DataLabelValue implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public NetworkOnlineID creatorID;
    public String labelName;
    public int labelIndex;
    public float[] analogue;
    public byte[] ternary;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int head = revision.getVersion();

        creatorID = serializer.struct(creatorID, NetworkOnlineID.class);
        labelIndex = serializer.i32(labelIndex);

        if (revision.isVita())
        {

            if (revision.has(Branch.DOUBLE11, Revisions.D1_LABEL_ANALOGUE_ARRAY))
                analogue = serializer.floatarray(analogue);
            else if (revision.has(Branch.DOUBLE11, Revisions.D1_DATALABELS))
            {
                if (serializer.isWriting())
                {
                    float value = analogue != null && analogue.length != 0 ? analogue[0] : 0.0f;
                    serializer.getOutput().f32(value);
                }
                else
                    analogue = new float[] { serializer.getInput().f32() };
            }

            if (revision.has(Branch.DOUBLE11, Revisions.D1_LABEL_TERNARY))
                ternary = serializer.bytearray(ternary);
        }
        else if (head >= Revisions.DATALABELS)
        {
            analogue = serializer.floatarray(analogue);
            ternary = serializer.bytearray(ternary);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = DataLabelValue.BASE_ALLOCATION_SIZE;
        if (this.analogue != null)
            size += (this.analogue.length * 4);
        if (this.ternary != null)
            size += (this.ternary.length);
        return size;
    }
}
