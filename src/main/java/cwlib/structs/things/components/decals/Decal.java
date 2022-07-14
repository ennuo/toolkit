package cwlib.structs.things.components.decals;

import org.joml.Vector4f;

import cwlib.enums.Branch;
import cwlib.enums.DecalType;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;

/**
 * Represents a sticker on an object.
 */
public class Decal implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    /**
     * The texture used for this decal.
     */
    public ResourceDescriptor texture;

    /**
     * Coordinates of decal on UV map.
     */
    public float u, v;

    /**
     * Rect of decal on UV map.
     */
    public float xvecu, xvecv, yvecu, yvecv;

    /**
     * Color tint of this decal (RGB565).
     */
    public short color;

    /**
     * Type of decal
     */
    public DecalType type = DecalType.STICKER;

    /* No idea what this is actually used for, probably a reference to the thing containing the PMetaData for this sticker? */
    public short metadataIndex, numMetadata;

    /**
     * Player that placed this decal.
     */
    public short placedBy;

    /**
     * Number of frames that have passed in play mode.
     */
    public int playModeFrame;

    /**
     * If this decal has a scorch mark on it.
     */
    public boolean scorchMark;

    /**
     * The plan that this decal came from.
     */
    public ResourceDescriptor plan;

    public Vector4f localHitPoint; // Vita

    @SuppressWarnings("unchecked")
    @Override public Decal serialize(Serializer serializer, Serializable structure) {
        Decal decal = (structure == null) ? new Decal() : (Decal) structure;

        int version = serializer.getRevision().getVersion();

        decal.texture = serializer.resource(decal.texture, ResourceType.TEXTURE);
        decal.u = serializer.f32(decal.u);
        decal.v = serializer.f32(decal.v);
        decal.xvecu = serializer.f32(decal.xvecu);
        decal.xvecv = serializer.f32(decal.xvecv);
        decal.yvecu = serializer.f32(decal.yvecu);
        decal.yvecv = serializer.f32(decal.yvecv);

        if (version >= 0x14e && version < 0x25c) 
            serializer.i32(0); // u32 color

        if (version >= 0x260)
            decal.color = serializer.i16(decal.color);
        
        if (version >= 0x158) {
            decal.type = serializer.enum8(decal.type);
            decal.metadataIndex = serializer.i16(decal.metadataIndex);
            if (version <= 0x3ba)
                decal.numMetadata = serializer.i16(decal.numMetadata);
        }

        if (version >= 0x214)
            decal.placedBy = serializer.i16(decal.placedBy);
        if (version >= 0x215)
            decal.playModeFrame = serializer.i32(decal.playModeFrame);
        
        if (version >= 0x219)
            decal.scorchMark = serializer.bool(decal.scorchMark);

        if (version >= 0x34c)
            decal.plan = serializer.resource(decal.plan, ResourceType.PLAN, true);
        else if (version >= 0x25b) {
            if (serializer.isWriting()) {
                if (decal.plan == null || decal.plan.isHash()) 
                    serializer.getOutput().i32(0);
                else if (decal.plan.isGUID())
                    serializer.getOutput().guid(decal.plan.getGUID());
            } else {
                GUID guid = serializer.getInput().guid();
                if (guid == null) decal.plan = null;
                else decal.plan = new ResourceDescriptor(guid, ResourceType.PLAN);
            }
        }

        if (serializer.getRevision().has(Branch.DOUBLE11, 0x3f))
            decal.localHitPoint = serializer.v4(decal.localHitPoint);
        
        return decal;
    }

    @Override public int getAllocatedSize() { return Decal.BASE_ALLOCATION_SIZE; }
}