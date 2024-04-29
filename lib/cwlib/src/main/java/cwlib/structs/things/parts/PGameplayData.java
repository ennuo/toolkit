package cwlib.structs.things.parts;

import cwlib.types.data.Revision;
import cwlib.structs.slot.SlotID;
import cwlib.structs.things.components.EggLink;
import cwlib.enums.GameplayPartType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PGameplayData implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    @GsonRevision(min = 0x2d1)
    public GameplayPartType gameplayType = GameplayPartType.UNDEFINED;
    public int fluffCost = 100000;
    public EggLink eggLink;
    public SlotID keyLink;
    @GsonRevision(lbp3 = true, min = 0xf3)
    public int treasureType;
    @GsonRevision(lbp3 = true, min = 0xf3)
    public short treasureCount = 1;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (subVersion >= 0xef && version > 0x2d0)
            gameplayType = serializer.enum32(gameplayType);

        fluffCost = serializer.s32(fluffCost);
        eggLink = serializer.reference(eggLink, EggLink.class);
        keyLink = serializer.reference(keyLink, SlotID.class);

        if (version >= 0x2d1 && subVersion < 0xef)
            gameplayType = serializer.enum32(gameplayType);

        if (subVersion >= 0xf3)
        {
            treasureType = serializer.i32(treasureType);
            treasureCount = serializer.i16(treasureCount);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PGameplayData.BASE_ALLOCATION_SIZE;
        if (this.eggLink != null) size += this.eggLink.getAllocatedSize();
        if (this.keyLink != null) size += this.keyLink.getAllocatedSize();
        return size;
    }
}
