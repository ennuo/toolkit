package cwlib.structs.profile;

import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class PaintProperties implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    public int triggerOverride, stickerSaveSize;
    public boolean angleOverride, uiHidden;
    public ResourceDescriptor lastAutoSave;

    @GsonRevision(min = Revisions.PTG_USE_DEFAULT_BACKGROUND)
    public boolean useDefaultBackground;

    @Override
    public void serialize(Serializer serializer)
    {
        triggerOverride = serializer.i32(triggerOverride);
        stickerSaveSize = serializer.i32(stickerSaveSize);
        angleOverride = serializer.bool(angleOverride);
        uiHidden = serializer.bool(uiHidden);
        lastAutoSave = serializer.resource(lastAutoSave,
            ResourceType.PAINTING, true);
        if (serializer.getRevision().getVersion() >= Revisions.PTG_USE_DEFAULT_BACKGROUND)
            useDefaultBackground = serializer.bool(useDefaultBackground);
    }

    @Override
    public int getAllocatedSize()
    {
        return PaintProperties.BASE_ALLOCATION_SIZE;
    }
}
