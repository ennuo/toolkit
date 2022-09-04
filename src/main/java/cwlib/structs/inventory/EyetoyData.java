package cwlib.structs.inventory;

import org.joml.Matrix4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class EyetoyData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100 + ColorCorrection.BASE_ALLOCATION_SIZE;

    public ResourceDescriptor frame, alphaMask;
    public Matrix4f colorCorrection = new Matrix4f().identity();
    public ColorCorrection colorCorrectionSrc = new ColorCorrection();

    @GsonRevision(min=0x3a0)
    public ResourceDescriptor outline;

    @SuppressWarnings("unchecked")
    @Override public EyetoyData serialize(Serializer serializer, Serializable structure) {
        EyetoyData eyetoy = (structure == null) ? new EyetoyData() : (EyetoyData) structure;
        
        if (serializer.getRevision().getVersion() < 0x15e)
            return eyetoy;

        eyetoy.frame = serializer.resource(eyetoy.frame, ResourceType.TEXTURE);
        eyetoy.alphaMask = serializer.resource(eyetoy.alphaMask, ResourceType.TEXTURE);
        eyetoy.colorCorrection = serializer.m44(eyetoy.colorCorrection);
        eyetoy.colorCorrectionSrc = serializer.struct(eyetoy.colorCorrectionSrc, ColorCorrection.class);
        if (serializer.getRevision().getVersion() > 0x39f)
            eyetoy.outline = serializer.resource(eyetoy.outline, ResourceType.TEXTURE);

        return eyetoy;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
