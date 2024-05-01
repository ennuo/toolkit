package cwlib.structs.things.parts;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.Part;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.gson.TranslationSerializer;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;

import org.joml.Matrix4f;

/**
 * This part represents a Thing's
 * position in the world, as well
 * as information about a bone.
 */
public class PPos implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public Thing thingOfWhichIAmABone;
    public int animHash;

    @GsonRevision(max = 0x340)
    @JsonAdapter(TranslationSerializer.class)
    public Matrix4f localPosition = new Matrix4f().identity();

    @JsonAdapter(TranslationSerializer.class)
    public Matrix4f worldPosition = new Matrix4f().identity();

    public PPos() { }

    public PPos(Matrix4f wpos)
    {
        this(wpos, wpos);
    }

    public PPos(Thing root, int animHash)
    {
        this.thingOfWhichIAmABone = root;
        this.animHash = animHash;
    }

    public PPos(Matrix4f wpos, Matrix4f pos)
    {
        this.worldPosition = wpos;
        this.localPosition = pos;
    }

    public PPos(Thing root, int animHash, Matrix4f wpos)
    {
        this.thingOfWhichIAmABone = root;
        this.animHash = animHash;
        this.worldPosition = wpos;
        this.localPosition = wpos;
    }

    public PPos(Thing root, int animHash, Matrix4f wpos, Matrix4f pos)
    {
        this.thingOfWhichIAmABone = root;
        this.animHash = animHash;
        this.worldPosition = wpos;
        this.localPosition = pos;
    }

    public void recomputeLocalPos(Thing thing)
    {
        if (thing.parent == null)
        {
            localPosition = new Matrix4f(worldPosition);
            return;
        }

        PPos parent = thing.parent.getPart(Part.POS);

        // This generally shouldn't happen, but make sure to check it anyway
        if (parent == null) return;

        Matrix4f inv = parent.worldPosition.invert(new Matrix4f());
        localPosition = inv.mul(worldPosition);
    }
    
    public void fixup(Thing thing, Revision revision)
    {
        // Local positions were removed, so we should re-calculate them
        if (revision.getVersion() >= 0x341 || localPosition == null || (localPosition.properties() & Matrix4f.PROPERTY_IDENTITY) != 0)
            recomputeLocalPos(thing);
    }

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        thingOfWhichIAmABone = serializer.reference(thingOfWhichIAmABone, Thing.class);
        animHash = serializer.i32(animHash);

        if (version < 0x341)
            localPosition = serializer.m44(localPosition);
        worldPosition = serializer.m44(worldPosition);

        // Unknown value, depreciated very early
        if (version < 0x155)
            serializer.i32(0);
    }

    @Override
    public int getAllocatedSize()
    {
        return PPos.BASE_ALLOCATION_SIZE;
    }
}
