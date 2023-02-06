package cwlib.structs.things.components;

import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class HUDDecal implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    public ResourceDescriptor texture;
    public int frameNo, gpuColor;
    public Vector4f[] corners = new Vector4f[4];

    @SuppressWarnings("unchecked")
    @Override public HUDDecal serialize(Serializer serializer, Serializable structure) {
        HUDDecal decal = (structure == null) ? new HUDDecal() : (HUDDecal) structure;

        if (serializer.getRevision().getSubVersion() < 0x24)
            return decal;
        
        decal.texture = serializer.resource(decal.texture, ResourceType.TEXTURE);
        decal.frameNo = serializer.i32(decal.frameNo);
        decal.gpuColor = serializer.i32(decal.gpuColor);
        for (int i = 0; i < 4; ++i)
            decal.corners[i] = serializer.v4(decal.corners[i]);
        
        return decal;
    }

    @Override public int getAllocatedSize() { return HUDDecal.BASE_ALLOCATION_SIZE; }
}
