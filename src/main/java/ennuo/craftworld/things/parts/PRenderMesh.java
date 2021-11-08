package ennuo.craftworld.things.parts;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.Color;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.things.Thing;
import ennuo.craftworld.types.data.ResourceDescriptor;

public class PRenderMesh implements Serializable {
    public ResourceDescriptor mesh;
    public Thing[] boneThings;
    public ResourceDescriptor anim;
    public float animPos, animSpeed = 1.0f;
    public boolean animLoop = true;
    public float loopStart, loopEnd = 1.0f;
    public Color editorColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    public boolean dontCastShadows;
    public boolean RTTEnable;
    public boolean visible = true;
    public float poppetRenderScale = 1.0f;
    public float parentDistanceFront, parentDistanceSide;
    
    public PRenderMesh serialize(Serializer serializer, Serializable structure) {
        PRenderMesh renderMesh = (structure == null) ? new PRenderMesh() : (PRenderMesh) structure;
        
        renderMesh.mesh = serializer.resource(renderMesh.mesh, ResourceType.MESH);
        renderMesh.boneThings = serializer.array(renderMesh.boneThings, Thing.class, true);
        
        renderMesh.anim = serializer.resource(renderMesh.anim, ResourceType.ANIMATION);
        renderMesh.animPos = serializer.f32(renderMesh.animPos);
        renderMesh.animSpeed = serializer.f32(renderMesh.animSpeed);
        renderMesh.animLoop = serializer.bool(renderMesh.animLoop);
        renderMesh.loopStart = serializer.f32(renderMesh.loopStart);
        renderMesh.loopEnd = serializer.f32(renderMesh.loopEnd);
        
        renderMesh.editorColor = serializer.struct(renderMesh.editorColor, Color.class);
        
        renderMesh.dontCastShadows = serializer.bool(renderMesh.dontCastShadows);
        renderMesh.RTTEnable = serializer.bool(renderMesh.RTTEnable);
        renderMesh.visible = serializer.bool(renderMesh.visible);
        
        renderMesh.poppetRenderScale = serializer.f32(renderMesh.poppetRenderScale);
        
        if (serializer.revision >= 0x25b) {
            renderMesh.parentDistanceFront = serializer.f32(renderMesh.parentDistanceFront);
            renderMesh.parentDistanceSide = serializer.f32(renderMesh.parentDistanceFront);
        }
        
        return renderMesh;
    }
    
}
