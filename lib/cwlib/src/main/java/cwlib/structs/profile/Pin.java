package cwlib.structs.profile;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class Pin implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public long id, progressType, category;
    public long titleLamsKey, descriptionLamsKey;
    public ResourceDescriptor icon;
    public long initialProgressValue, targetValue;
    public byte trophyToUnlock;
    public short behaviourFlags;
    public byte trophyToUnlockLBP1;

    @Override
    public void serialize(Serializer serializer)
    {
        id = serializer.u32(id);
        progressType = serializer.u32(progressType);
        category = serializer.u32(category);
        titleLamsKey = serializer.u32(titleLamsKey);
        descriptionLamsKey = serializer.u32(descriptionLamsKey);
        icon = serializer.resource(icon, ResourceType.TEXTURE, true);
        initialProgressValue = serializer.u32(initialProgressValue);
        targetValue = serializer.u32(targetValue);
        trophyToUnlock = serializer.i8(trophyToUnlock);
        behaviourFlags = serializer.i16(behaviourFlags);
        if (serializer.getRevision().getVersion() >= 0x3f7)
            trophyToUnlockLBP1 = serializer.i8(trophyToUnlockLBP1);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}