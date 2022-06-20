package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.NetworkOnlineID;
import cwlib.types.data.Revision;

public class PlayerDataLabels implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public DataLabelValue[] values;
    public NetworkOnlineID[] protectedIds;

    @SuppressWarnings("unchecked")
    @Override public PlayerDataLabels serialize(Serializer serializer, Serializable structure) {
        PlayerDataLabels labels = (structure == null) ? new PlayerDataLabels() : (PlayerDataLabels) structure;

        Revision revision = serializer.getRevision();
        int head = revision.getVersion();

        labels.values = serializer.array(labels.values, DataLabelValue.class);
        if (revision.isAfterVitaRevision(0x30) || head > 0x3ee)
            labels.protectedIds = serializer.array(labels.protectedIds, NetworkOnlineID.class);

        return labels;
    }

    @Override public int getAllocatedSize() {
        int size = PlayerDataLabels.BASE_ALLOCATION_SIZE;
        if (this.values != null)
            for (DataLabelValue value : this.values)
                size += value.getAllocatedSize();
        if (this.protectedIds != null)
            size += (this.protectedIds.length * NetworkOnlineID.BASE_ALLOCATION_SIZE);
        return size;
    }
}
