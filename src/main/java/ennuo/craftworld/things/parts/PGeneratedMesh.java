package ennuo.craftworld.things.parts;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.data.ResourceDescriptor;
import org.joml.Vector4f;

public class PGeneratedMesh implements Serializable {
    public ResourceDescriptor gfxMaterial;
    public ResourceDescriptor bevel;
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
