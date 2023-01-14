package cwlib.structs.animation;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * Stores information about bone relations for animation.
 */
public class AnimBone implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    /**
     * Unique identifier for the bone, generated from the name.
     */
    public int animHash;

    /**
     * Index of the parent of this bone.
     */
    public int parent = -1;

    /**
     * Index of the first child of this bone.
     */
    public int firstChild = -1;

    /**
     * Index of the next sibling of this bone.
     */
    public int nextSibling = -1;

    public AnimBone() {};
    public AnimBone(int animHash, int parent, int firstChild, int nextSibling) {
        this.animHash = animHash;
        this.parent = parent;
        this.firstChild = firstChild;
        this.nextSibling = nextSibling;
    }


    @SuppressWarnings("unchecked")
    @Override public AnimBone serialize(Serializer serializer, Serializable structure) {
        AnimBone bone = (structure == null) ? new AnimBone() : (AnimBone) structure;
        
        bone.animHash = serializer.i32(bone.animHash);
        bone.parent = serializer.s32(bone.parent);
        bone.firstChild = serializer.s32(bone.firstChild);
        bone.nextSibling = serializer.s32(bone.nextSibling);

        return bone;
    }

    @Override public int getAllocatedSize() { return AnimBone.BASE_ALLOCATION_SIZE; }
}
