package cwlib.structs.things.parts;

import cwlib.enums.Part;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.gson.TranslationSerializer;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import editor.gl.RenderSystem;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.google.gson.annotations.JsonAdapter;

/**
 * This part represents a Thing's
 * position in the world, as well
 * as information about a bone.
 */
public class PPos implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public transient Thing myThing;
    public Thing thingOfWhichIAmABone;
    public int animHash;
    
    @GsonRevision(max=0x340) 
    @JsonAdapter(TranslationSerializer.class)
    public Matrix4f localPosition = new Matrix4f();

    @JsonAdapter(TranslationSerializer.class) 
    public Matrix4f worldPosition = new Matrix4f();

    public PPos() {};
    public PPos(Matrix4f wpos) { this(wpos, wpos); }
    
    public PPos(Thing root, int animHash) { 
        this.thingOfWhichIAmABone = root;
        this.animHash = animHash;
    }
    public PPos(Matrix4f wpos, Matrix4f pos) {
        this.worldPosition = wpos;
        this.localPosition = pos;
    }
    public PPos(Thing root, int animHash, Matrix4f wpos) {
        this.thingOfWhichIAmABone = root;
        this.animHash = animHash;
        this.worldPosition = wpos;
        this.localPosition = wpos;
    }

    public PPos(Thing root, int animHash, Matrix4f wpos, Matrix4f pos) {
        this.thingOfWhichIAmABone = root;
        this.animHash = animHash;
        this.worldPosition = wpos;
        this.localPosition = pos;
    }

    public Matrix4f getWorldPosition() { return this.worldPosition; }
    public Matrix4f getLocalPosition() { return this.localPosition; }

    public void translate(Vector3f delta)
    {
        this.setWorldPos(this.worldPosition.translate(delta));
    }

    public void rotateX(float degrees)
    {
        this.setWorldPos(this.worldPosition.rotate(
            (float) Math.toRadians(degrees),
            new Vector3f(1.0f, 0.0f, 0.0f)
        ));
    }

    public void rotateY(float degrees)
    {
        this.setWorldPos(this.worldPosition.rotate(
            (float) Math.toRadians(degrees),
            new Vector3f(0.0f, 1.0f, 0.0f)
        ));
    }

    public void rotateZ(float degrees)
    {
        this.setWorldPos(this.worldPosition.rotate(
            (float) Math.toRadians(degrees),
            new Vector3f(0.0f, 0.0f, 1.0f)
        ));
    }

    public void setWorldPos(Matrix4f wpos) {
        this.worldPosition = wpos;
        this.recomputeLocalPos();
        this.worldPosChanged();
    }

    public void worldPosChanged() {
        if (this.thingOfWhichIAmABone != null) {
            PRenderMesh mesh = this.thingOfWhichIAmABone.getPart(Part.RENDER_MESH);
            if (mesh != null)
                mesh.isDirty = true;
        }
        Thing[] things = RenderSystem.getSceneGraph().getThings();
        for (Thing thing : things) {
            if (thing.parent == this.myThing) {
                PPos pos = thing.getPart(Part.POS);
                if (pos != null)
                    pos.recomputeWorldPos();
            }
        }
    }

    public void recomputeWorldPos() {
        if (this.myThing.parent == null)
            this.worldPosition = new Matrix4f(this.localPosition);
        else {
            Matrix4f parent = this.myThing.parent.<PPos>getPart(Part.POS).worldPosition;
            this.worldPosition = parent.mul(this.localPosition, new Matrix4f());
        }
        this.worldPosChanged();
    }

    public void recomputeLocalPos()
    {
        if (this.myThing.parent == null)
            this.localPosition = new Matrix4f(this.worldPosition);
        else {
            Matrix4f parent = this.myThing.parent.<PPos>getPart(Part.POS).worldPosition;
            this.localPosition = parent.invert(new Matrix4f()).mul(this.worldPosition);
        }
    }


    @SuppressWarnings("unchecked")
    @Override public PPos serialize(Serializer serializer, Serializable structure) {
        PPos pos = (structure == null) ? new PPos() : (PPos) structure;
        
        int version = serializer.getRevision().getVersion();
        
        pos.thingOfWhichIAmABone = serializer.reference(pos.thingOfWhichIAmABone, Thing.class);
        pos.animHash = serializer.i32(pos.animHash);
        
        if (version < 0x341)
            pos.localPosition = serializer.m44(pos.localPosition);
        pos.worldPosition = serializer.m44(pos.worldPosition);

        if (pos.localPosition == null)
            pos.localPosition = pos.worldPosition;

        // Unknown value, depreciated very early
        if (version < 0x155)
            serializer.i32(0);
        
        return pos;
    }

    @Override public int getAllocatedSize() { return PPos.BASE_ALLOCATION_SIZE; }
}
