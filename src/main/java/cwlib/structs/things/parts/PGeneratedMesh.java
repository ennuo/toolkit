package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceReference;
import org.joml.Vector4f;

public class PGeneratedMesh implements Serializable {
    public ResourceReference gfxMaterial;
    public ResourceReference bevel;
    public Vector4f uvOffset = new Vector4f(0, 0, 0, 0);
    public long planGUID;
    public boolean visible;
    
    public PGeneratedMesh serialize(Serializer serializer, Serializable structure) {
        PGeneratedMesh generatedMesh = (structure == null) ? new PGeneratedMesh() : (PGeneratedMesh) structure;
        
        generatedMesh.gfxMaterial = serializer.resource(generatedMesh.gfxMaterial, ResourceType.GFX_MATERIAL);
        generatedMesh.bevel = serializer.resource(generatedMesh.bevel, ResourceType.BEVEL);
        
        generatedMesh.uvOffset = serializer.v4(generatedMesh.uvOffset);
        
        if (serializer.revision.head > 0x257)
            generatedMesh.planGUID = serializer.u32(generatedMesh.planGUID);
        
        if (serializer.revision.head < 0x27c) return generatedMesh;
        
        generatedMesh.visible = serializer.bool(generatedMesh.visible);
        
        return generatedMesh;
    }
    
}
