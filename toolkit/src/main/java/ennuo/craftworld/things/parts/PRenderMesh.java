package ennuo.craftworld.things.parts;

import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.memory.Vector4f;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.things.Part;
import ennuo.craftworld.things.Serializer;
import ennuo.craftworld.things.ThingPtr;

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
        boneThings = new ThingPtr[serializer.input.int32()];
        for (int i = 0; i < boneThings.length; ++i)
            boneThings[i] = serializer.deserializeThing();
        anim = serializer.input.resource(RType.ANIM);
        animPos = serializer.input.float32();
        animSpeed = serializer.input.float32();
        animLoop = serializer.input.bool();
        loopStart = serializer.input.float32();
        loopEnd = serializer.input.float32();
        
        if (serializer.partsRevision < 0x5e) {
            editorColourLegacy = serializer.input.v4();
            dontCastShadows = serializer.input.bool();
        }
        else {
            editorColour = serializer.input.uint32();
            castShadows = serializer.input.int32();
        }
        
        RTTEnable = serializer.input.bool();
                    
        if (serializer.partsRevision < 0x5e)
            visible = serializer.input.bool();
        else visibilityFlags = serializer.input.int32();
        
        poppetRenderScale = serializer.input.float32();
        
        if (serializer.partsRevision < 0x5e) {
            parentDistanceFront = serializer.input.float32();
            parentDistanceSide = serializer.input.float32();
        }
    }
    
    
}
