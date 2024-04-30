package cwlib.structs.profile;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.NetworkOnlineID;
import cwlib.types.data.Revision;

public class PlayerDataLabels implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public DataLabelValue[] values;
    public NetworkOnlineID[] protectedIds;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int head = revision.getVersion();

        values = serializer.array(values, DataLabelValue.class);
        if (revision.has(Branch.DOUBLE11, Revisions.D1_PROTECTED_LABELS) || head >= Revisions.DATALABELS)
            protectedIds = serializer.array(protectedIds, NetworkOnlineID.class);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PlayerDataLabels.BASE_ALLOCATION_SIZE;
        if (this.values != null)
            for (DataLabelValue value : this.values)
                size += value.getAllocatedSize();
        if (this.protectedIds != null)
            size += (this.protectedIds.length * NetworkOnlineID.BASE_ALLOCATION_SIZE);
        return size;
    }
}
