package cwlib.structs.profile;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceReference;

public class PaintProperties implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    private int triggerOverride, stickerSaveSize;
    private boolean angleOverride, uiHidden;
    private ResourceReference lastAutoSave;
    private boolean useDefaultBackground;

    @SuppressWarnings("unchecked")
    @Override public PaintProperties serialize(Serializer serializer, Serializable structure) {
        PaintProperties properties = (structure == null) ? new PaintProperties() : (PaintProperties) structure;

        properties.triggerOverride = serializer.i32(properties.triggerOverride);
        properties.stickerSaveSize = serializer.i32(properties.stickerSaveSize);
        properties.angleOverride = serializer.bool(properties.angleOverride);
        properties.uiHidden = serializer.bool(properties.uiHidden);
        properties.lastAutoSave = serializer.resource(properties.lastAutoSave, ResourceType.PAINTING, true);
        if (serializer.getRevision().getVersion() > 0x3df)
            properties.useDefaultBackground = serializer.bool(properties.useDefaultBackground);
        
        return properties;
    }

    @Override public int getAllocatedSize() { return PaintProperties.BASE_ALLOCATION_SIZE; }
}