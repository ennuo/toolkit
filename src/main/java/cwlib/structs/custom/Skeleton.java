package cwlib.structs.custom;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.enums.SkeletonType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.CullBone;

public class Skeleton implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x14;

    public Bone[] bones;
    public short[] mirror;
    public byte[] mirrorType;
    public CullBone[] cullBones;
    
    @GsonRevision(max=1,branch=0x4d5a)
    @Deprecated private SkeletonType type;

    @SuppressWarnings("unchecked")
    @Override public Skeleton serialize(Serializer serializer, Serializable structure) {
        Skeleton skeleton = (structure == null) ? new Skeleton() : (Skeleton) structure;

        skeleton.bones = serializer.array(skeleton.bones, Bone.class);
        skeleton.mirror = serializer.shortarray(skeleton.mirror);
        skeleton.mirrorType = serializer.bytearray(skeleton.mirrorType);
        skeleton.cullBones = serializer.array(skeleton.cullBones, CullBone.class);
        if (serializer.getRevision().before(Branch.MIZUKI, Revisions.MZ_BST_REMOVE_SK))
            skeleton.type = serializer.enum8(skeleton.type);

        return skeleton;
    }

    public int getAllocatedSize() {
        int size = Skeleton.BASE_ALLOCATION_SIZE;
        if (this.bones != null) for (Bone bone : bones) size += bone.getAllocatedSize();
        if (this.mirror != null) size += (this.mirror.length * 0x2);
        if (this.mirrorType != null) size += (this.mirrorType.length * 0x2);
        if (this.cullBones != null) size += (this.cullBones.length * CullBone.BASE_ALLOCATION_SIZE);
        return size;
    }
}
