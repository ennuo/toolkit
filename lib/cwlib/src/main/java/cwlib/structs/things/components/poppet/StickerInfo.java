package cwlib.structs.things.components.poppet;

import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.inventory.EyetoyData;
import cwlib.types.data.ResourceDescriptor;

public class StickerInfo implements Serializable
{
    public static int BASE_ALLOCATION_SIZE = 0x150;

    public Vector4f up, across;
    public ResourceDescriptor texture;
    public float height, width;
    public float angle, scale;
    public boolean reversed;
    public Vector4f offset;
    public boolean stamping;

    @GsonRevision(min = 0x160)
    public ResourceDescriptor plan;

    @GsonRevision(min = 0x24c)
    public EyetoyData eyetoyData = new EyetoyData();

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        up = serializer.v4(up);
        across = serializer.v4(across);
        texture = serializer.resource(texture, ResourceType.TEXTURE);
        height = serializer.f32(height);
        width = serializer.f32(width);
        angle = serializer.f32(angle);
        scale = serializer.f32(scale);
        reversed = serializer.bool(reversed);
        offset = serializer.v4(offset);
        stamping = serializer.bool(stamping);
        if (version > 0x15f)
            plan = serializer.resource(plan, ResourceType.PLAN, true);
        if (version > 0x24b)
            eyetoyData = serializer.struct(eyetoyData, EyetoyData.class);
    }


    @Override
    public int getAllocatedSize()
    {
        return StickerInfo.BASE_ALLOCATION_SIZE;
    }


}