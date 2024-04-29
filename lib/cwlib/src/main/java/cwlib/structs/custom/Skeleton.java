package cwlib.structs.custom;

import cwlib.enums.Branch;
import cwlib.enums.FlipType;
import cwlib.enums.Revisions;
import cwlib.enums.SkeletonType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.CullBone;

public class Skeleton implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x14;

    public Bone[] bones;
    public short[] mirror;
    public FlipType[] mirrorType;
    public CullBone[] cullBones;

    @GsonRevision(max = 1, branch = 0x4d5a)
    @Deprecated
    private SkeletonType type;

    @Override
    public void serialize(Serializer serializer)
    {
        bones = serializer.array(bones, Bone.class);
        mirror = serializer.shortarray(mirror);
        mirrorType = serializer.enumarray(mirrorType, FlipType.class);
        cullBones = serializer.array(cullBones, CullBone.class);
        if (serializer.getRevision().before(Branch.MIZUKI, Revisions.MZ_BST_REMOVE_SK))
            type = serializer.enum8(type);
    }

    public int getAllocatedSize()
    {
        int size = Skeleton.BASE_ALLOCATION_SIZE;
        if (this.bones != null) for (Bone bone : bones) size += bone.getAllocatedSize();
        if (this.mirror != null) size += (this.mirror.length * 0x2);
        if (this.mirrorType != null) size += (this.mirrorType.length * 0x2);
        if (this.cullBones != null)
            size += (this.cullBones.length * CullBone.BASE_ALLOCATION_SIZE);
        return size;
    }
}
