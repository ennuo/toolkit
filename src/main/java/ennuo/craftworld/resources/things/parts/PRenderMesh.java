package ennuo.craftworld.resources.things.parts;

import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.resources.things.ThingPtr;
import org.joml.Vector4f;

public class PRenderMesh implements Part {
    public ResourcePtr mesh = new ResourcePtr(null, RType.MESH);
    public ThingPtr[] boneThings;
    public ResourcePtr anim = new ResourcePtr(null, RType.ANIM);
    public float animPos = 0.0f, animSpeed = 0.0f;
    public boolean animLoop = false;
    public float loopStart = 0.0f, loopEnd = 1.0f;
    
    public Vector4f editorColourLegacy = new Vector4f(1, 1, 1, 1);
    public long editorColour = 0xFFFFFFFFL;
    
    public boolean dontCastShadows = false;
    
    public int castShadows = 0;
    
    public boolean RTTEnable = false, visible = true;
    
    public int visibilityFlags = 3;
    
    public float poppetRenderScale = 1.0f, parentDistanceFront = 0.0f, parentDistanceSide = 0.0f;

    @Override
    public void Serialize(Serializer serializer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Deserialize(Serializer serializer) {
        mesh = serializer.input.resource(RType.MESH);
        boneThings = new ThingPtr[serializer.input.i32()];
        for (int i = 0; i < boneThings.length; ++i)
            boneThings[i] = serializer.deserializeThing();
        anim = serializer.input.resource(RType.ANIM);
        animPos = serializer.input.f32();
        animSpeed = serializer.input.f32();
        animLoop = serializer.input.bool();
        loopStart = serializer.input.f32();
        loopEnd = serializer.input.f32();
        
        if (serializer.partsRevision < 0x5e) {
            editorColourLegacy = serializer.input.v4();
            dontCastShadows = serializer.input.bool();
        }
        else {
            editorColour = serializer.input.u32();
            castShadows = serializer.input.i32();
        }
        
        RTTEnable = serializer.input.bool();
                    
        if (serializer.partsRevision < 0x5e)
            visible = serializer.input.bool();
        else visibilityFlags = serializer.input.i32();
        
        poppetRenderScale = serializer.input.f32();
        
        if (serializer.partsRevision < 0x5e) {
            parentDistanceFront = serializer.input.f32();
            parentDistanceSide = serializer.input.f32();
        }
    }
    
    
}
