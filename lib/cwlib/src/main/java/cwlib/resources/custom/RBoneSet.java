package cwlib.resources.custom;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.custom.Skeleton;
import cwlib.types.data.Revision;

public class RBoneSet implements Resource
{
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    private ArrayList<Skeleton> skeletons = new ArrayList<>();

    @Override
    public void serialize(Serializer serializer)
    {
        skeletons = serializer.arraylist(skeletons, Skeleton.class);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = RBoneSet.BASE_ALLOCATION_SIZE;
        for (Skeleton skeleton : this.skeletons)
            size += skeleton.getAllocatedSize();
        return size;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision,
            compressionFlags);
        serializer.struct(this, RBoneSet.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.BONE_SET,
            SerializationType.BINARY,
            serializer.getDependencies()
        );
    }

    public ArrayList<Skeleton> getSkeletons()
    {
        return this.skeletons;
    }
}
