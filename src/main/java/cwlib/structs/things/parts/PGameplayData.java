package cwlib.structs.things.parts;

import cwlib.types.data.Revision;
import cwlib.structs.slot.SlotID;
import cwlib.structs.things.components.EggLink;
import cwlib.enums.GameplayPartType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PGameplayData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    @GsonRevision(min=0x2d1) public GameplayPartType gameplayType = GameplayPartType.UNDEFINED;
    public int fluffCost = 100000;
    public EggLink eggLink;
    public SlotID keyLink;
    @GsonRevision(lbp3=true,min=0xf3) public int treasureType;
    @GsonRevision(lbp3=true,min=0xf3) public short treasureCount = 1;

    @SuppressWarnings("unchecked")
    @Override public PGameplayData serialize(Serializer serializer, Serializable structure) {
        PGameplayData data = (structure == null) ? new PGameplayData() : (PGameplayData) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (subVersion >= 0xef && version > 0x2d0)
            data.gameplayType = serializer.enum32(data.gameplayType);
        
        data.fluffCost = serializer.s32(data.fluffCost);
        data.eggLink = serializer.reference(data.eggLink, EggLink.class);
        data.keyLink = serializer.reference(data.keyLink, SlotID.class);

        if (version >= 0x2d1 && subVersion < 0xef)
            data.gameplayType = serializer.enum32(data.gameplayType);

        if (subVersion >= 0xf3) {
            data.treasureType = serializer.i32(data.treasureType);
            data.treasureCount = serializer.i16(data.treasureCount);
        }

        return data;
    }

    @Override public int getAllocatedSize() {
        int size = PGameplayData.BASE_ALLOCATION_SIZE;
        if (this.eggLink != null) size += this.eggLink.getAllocatedSize();
        if (this.keyLink != null) size += this.keyLink.getAllocatedSize();
        return size;
    }
}
