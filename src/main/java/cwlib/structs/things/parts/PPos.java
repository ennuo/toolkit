package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.gson.TranslationSerializer;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

import org.joml.Matrix4f;

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
    public Matrix4f localPosition;

    @JsonAdapter(TranslationSerializer.class) 
    public Matrix4f worldPosition;

    // Matrix4
    // Vector4 mCol0
    // Vector4 mCol1
    // Vector4 mCol2
    // Vector4 mCol3

    
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
