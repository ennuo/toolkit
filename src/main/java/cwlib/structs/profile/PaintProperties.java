package cwlib.structs.profile;

import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class PaintProperties implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    private int triggerOverride, stickerSaveSize;
    private boolean angleOverride, uiHidden;
    private ResourceDescriptor lastAutoSave;

    @GsonRevision(min=Revisions.PTG_USE_DEFAULT_BACKGROUND)
    private boolean useDefaultBackground;

    @SuppressWarnings("unchecked")
    @Override public PaintProperties serialize(Serializer serializer, Serializable structure) {
        PaintProperties properties = (structure == null) ? new PaintProperties() : (PaintProperties) structure;

        properties.triggerOverride = serializer.i32(properties.triggerOverride);
        properties.stickerSaveSize = serializer.i32(properties.stickerSaveSize);
        properties.angleOverride = serializer.bool(properties.angleOverride);
        properties.uiHidden = serializer.bool(properties.uiHidden);
        properties.lastAutoSave = serializer.resource(properties.lastAutoSave, ResourceType.PAINTING, true);
        if (serializer.getRevision().getVersion() >= Revisions.PTG_USE_DEFAULT_BACKGROUND)
            properties.useDefaultBackground = serializer.bool(properties.useDefaultBackground);
        
        return properties;
    }

    @Override public int getAllocatedSize() { return PaintProperties.BASE_ALLOCATION_SIZE; }
}
