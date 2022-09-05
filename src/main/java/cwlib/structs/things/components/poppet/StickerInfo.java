package cwlib.structs.things.components.poppet;

import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.inventory.EyetoyData;
import cwlib.types.data.ResourceDescriptor;

public class StickerInfo implements Serializable {
    public static int BASE_ALLOCATION_SIZE = 0x150;
    
    public Vector4f up, across;
    public ResourceDescriptor texture;
    public float height, width;
    public float angle, scale;
    public boolean reversed;
    public Vector4f offset;
    public boolean stamping;

    @GsonRevision(min=0x160)
    public ResourceDescriptor plan;

    @GsonRevision(min=0x24c)
    public EyetoyData eyetoyData = new EyetoyData();

    @SuppressWarnings("unchecked")
    @Override public StickerInfo serialize(Serializer serializer, Serializable structure) {
        StickerInfo info = (structure == null) ? new StickerInfo() : (StickerInfo) structure;

        int version = serializer.getRevision().getVersion();

        info.up = serializer.v4(info.up);
        info.across = serializer.v4(info.across);
        info.texture = serializer.resource(info.texture, ResourceType.TEXTURE);
        info.height = serializer.f32(info.height);
        info.width = serializer.f32(info.width);
        info.angle = serializer.f32(info.angle);
        info.scale = serializer.f32(info.scale);
        info.reversed = serializer.bool(info.reversed);
        info.offset = serializer.v4(info.offset);
        info.stamping = serializer.bool(info.stamping);
        if (version > 0x15f)
            info.plan = serializer.resource(info.plan, ResourceType.PLAN, true);
        if (version > 0x24b)
            info.eyetoyData = serializer.struct(info.eyetoyData, EyetoyData.class);
        
        return info;
    }


    @Override public int getAllocatedSize() { return StickerInfo.BASE_ALLOCATION_SIZE; }
    

}