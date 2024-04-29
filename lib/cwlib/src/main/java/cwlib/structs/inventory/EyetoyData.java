package cwlib.structs.inventory;

import org.joml.Matrix4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class EyetoyData implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100 + ColorCorrection.BASE_ALLOCATION_SIZE;

    public ResourceDescriptor frame, alphaMask;
    public Matrix4f colorCorrection = new Matrix4f().identity();
    public ColorCorrection colorCorrectionSrc = new ColorCorrection();

    @GsonRevision(min = 0x3a0)
    public ResourceDescriptor outline;

    @Override
    public void serialize(Serializer serializer)
    {
        if (serializer.getRevision().getVersion() < 0x15e)
            return;

        frame = serializer.resource(frame, ResourceType.TEXTURE);
        alphaMask = serializer.resource(alphaMask, ResourceType.TEXTURE);
        colorCorrection = serializer.m44(colorCorrection);
        colorCorrectionSrc = serializer.struct(colorCorrectionSrc,
            ColorCorrection.class);
        if (serializer.getRevision().getVersion() > 0x39f)
            outline = serializer.resource(outline, ResourceType.TEXTURE);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}
