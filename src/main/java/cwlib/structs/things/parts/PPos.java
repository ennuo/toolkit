package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.gson.TranslationSerializer;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

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

    public Thing thingOfWhichIAmABone;
    public int animHash;
    
    @GsonRevision(max=0x340) 
    @JsonAdapter(TranslationSerializer.class)
    public Matrix4f localPosition = new Matrix4f();

    @JsonAdapter(TranslationSerializer.class) 
    public Matrix4f worldPosition = new Matrix4f();

    private transient Vector3f translation = new Vector3f();
    private transient Vector3f rotation = new Vector3f();
    private transient Vector3f scale = new Vector3f();

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

    public Vector3f getTranslation() { return this.translation; }
    public Vector3f getRotation() { return this.rotation; }
    public Vector3f getScale() { return this.scale; }

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
