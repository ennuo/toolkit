package cwlib.structs.things.parts;

import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.enums.ShadowType;
import cwlib.enums.VisibilityFlags;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.Colors;

public class PRenderMesh implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public ResourceDescriptor mesh;
    public Thing[] boneThings = new Thing[0];
    public ResourceDescriptor anim;
    public float animPos, animSpeed = 1.0f;
    public boolean animLoop = true;
    public float loopStart, loopEnd = 1.0f;
    public int editorColor = -1;
    public ShadowType castShadows = ShadowType.ALWAYS;
    public boolean RTTEnable;
    public byte visibilityFlags = VisibilityFlags.PLAY_MODE | VisibilityFlags.EDIT_MODE;
    public float poppetRenderScale = 1.0f;
    @GsonRevision(min=0x1f6,max=0x34c)
    public float parentDistanceFront, parentDistanceSide;

    public PRenderMesh() {}
    public PRenderMesh(ResourceDescriptor mesh) {
        this.mesh = mesh;
    }
    
    @SuppressWarnings("unchecked")
    @Override public PRenderMesh serialize(Serializer serializer, Serializable structure) {
        PRenderMesh mesh = (structure == null) ? new PRenderMesh() : (PRenderMesh) structure;
        
        int version = serializer.getRevision().getVersion();

        mesh.mesh = serializer.resource(mesh.mesh, ResourceType.MESH);

        mesh.boneThings = serializer.thingarray(mesh.boneThings);
        
        mesh.anim = serializer.resource(mesh.anim, ResourceType.ANIMATION);
        mesh.animPos = serializer.f32(mesh.animPos);
        mesh.animSpeed = serializer.f32(mesh.animSpeed);
        mesh.animLoop = serializer.bool(mesh.animLoop);
        mesh.loopStart = serializer.f32(mesh.loopStart);
        mesh.loopEnd = serializer.f32(mesh.loopEnd);
        
        if (version > 0x31a) mesh.editorColor = serializer.i32(mesh.editorColor);
        else {
            if (serializer.isWriting())
                serializer.getOutput().v4(Colors.RGBA32.fromARGB(mesh.editorColor));
            else {
                Vector4f color = serializer.getInput().v4();
                mesh.editorColor = Colors.RGBA32.getARGB(color);
            }
        }
        
        mesh.castShadows = serializer.enum8(mesh.castShadows);
        mesh.RTTEnable = serializer.bool(mesh.RTTEnable);
        
        if (version > 0x2e2)
            mesh.visibilityFlags = serializer.i8(mesh.visibilityFlags);
        else {
            if (serializer.isWriting())
                serializer.getOutput().bool((mesh.visibilityFlags & VisibilityFlags.PLAY_MODE) != 0);
            else {
                mesh.visibilityFlags = VisibilityFlags.EDIT_MODE;
                if (serializer.getInput().bool())
                    mesh.visibilityFlags |=  VisibilityFlags.PLAY_MODE;
            }
        }
        
        mesh.poppetRenderScale = serializer.f32(mesh.poppetRenderScale);
        
        if (version > 0x1f5 && version < 0x34d) {
            mesh.parentDistanceFront = serializer.f32(mesh.parentDistanceFront);
            mesh.parentDistanceSide = serializer.f32(mesh.parentDistanceFront);
        }

        return mesh;
    }

    @Override public int getAllocatedSize() {
        int size = PRenderMesh.BASE_ALLOCATION_SIZE;
        if (this.boneThings != null) size += (this.boneThings.length * 0x4);
        return size;
    }
}
