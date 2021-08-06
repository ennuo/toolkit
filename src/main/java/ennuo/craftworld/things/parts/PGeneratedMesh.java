package ennuo.craftworld.things.parts;

import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.things.Part;
import ennuo.craftworld.things.Serializer;
import org.joml.Vector4f;

public class PGeneratedMesh implements Part {
    public ResourcePtr gfxMaterial = new ResourcePtr(null, RType.GFXMATERIAL);
    public ResourcePtr bevel = new ResourcePtr(null, RType.BEVEL);
    
    public Vector4f uvOffset = new Vector4f(0, 0, 0, 1);
    public int planGUID;
    
    public byte visibilityFlags = 3;
    
    public float textureAnimationSpeed = 1;
    public float textureAnimationSpeedOff = 1;
    
    public boolean noBevel = false;
    public boolean sharded = false;
    
    public boolean includeSides = true;
    
    public byte slideImpactDamping = 50;
    public byte slideSteer = 100;
    public byte slideSpeed = 50;
    
    
    @Override
    public void Serialize(Serializer serializer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Deserialize(Serializer serializer) {
        gfxMaterial = serializer.input.resource(RType.GFXMATERIAL);
        bevel = serializer.input.resource(RType.BEVEL);
        
        uvOffset = serializer.input.v4();
        
        if (serializer.partsRevision >= 0x4e)
            planGUID = serializer.input.int32();
        
        if (serializer.partsRevision >= 0x5e) {
            visibilityFlags = serializer.input.int8();
            textureAnimationSpeed = serializer.input.float32();
            textureAnimationSpeedOff = serializer.input.float32();
        }
        
        if (serializer.partsRevision >= 0x76) {
            noBevel = serializer.input.bool();
            sharded = serializer.input.bool();
        }
        
        if (serializer.partsRevision >= 0x7e) {
            includeSides = serializer.input.bool();
            if (serializer.gameRevision >= 0x014703f0)
                slideImpactDamping = serializer.input.int8();
            slideSteer = serializer.input.int8();
            slideSpeed = serializer.input.int8();
        }
        
    }
    
}
