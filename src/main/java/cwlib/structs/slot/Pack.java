package cwlib.structs.slot;

import java.util.Date;

import cwlib.enums.ContentsType;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

/**
 * Represents additional content that can be
 * purchased from the store.
 */
public class Pack implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x40 + Slot.BASE_ALLOCATION_SIZE;

    /**
     * The type of DLC content contained by this pack.
     */
    public ContentsType contentsType = ContentsType.LEVEL;

    /**
     * The badge mesh used with this pack.
     */
    public ResourceDescriptor mesh = ContentsType.LEVEL.getBadgeMesh();

    /**
     * The underlying slot for this pack, contains the information about the DLC,
     * including icon/name, as well as the playable level if it is one.
     */
    public Slot slot = new Slot();

    /**
     * The PSN contentID associated with this DLC pack, this contentID must be
     * owned by the player for this pack to be available for use.
     */
    public String contentID;

    /**
     * The timestamp at which this pack was added.
     */
    public long timestamp = new Date().getTime() / 1000;

    /**
     * Whether or not this pack is compatible with Vita/LBP2 cross-buy.
     */
    @GsonRevision(branch=0x4431)
    public boolean crossBuyCompatible = false;

    @SuppressWarnings("unchecked")
    @Override public Pack serialize(Serializer serializer, Serializable structure) {
        Pack pack = (structure == null) ? new Pack() : (Pack) structure;

        pack.contentsType = serializer.enum32(pack.contentsType);
        pack.mesh = serializer.resource(pack.mesh, ResourceType.MESH, true);
        pack.slot = serializer.struct(pack.slot, Slot.class);
        pack.contentID = serializer.str(pack.contentID);
        pack.timestamp = serializer.i64(pack.timestamp);
        if (serializer.getRevision().isVita())
            pack.crossBuyCompatible = serializer.bool(pack.crossBuyCompatible);

        return pack;
    }

    @Override public int getAllocatedSize() {
        int size = BASE_ALLOCATION_SIZE;
        if (this.slot != null)
            size += this.slot.getAllocatedSize();
        if (this.contentID != null)
            size += this.contentID.length();
        return size; 
    }
}
