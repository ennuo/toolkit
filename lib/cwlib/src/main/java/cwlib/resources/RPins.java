package cwlib.resources;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.profile.Pin;
import cwlib.types.data.Revision;

public class RPins implements Resource
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public ArrayList<Pin> pins = new ArrayList<>();

    @Override
    public void serialize(Serializer serializer)
    {
        pins = serializer.arraylist(pins, Pin.class);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE + pins.size() * Pin.BASE_ALLOCATION_SIZE;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision,
            compressionFlags);
        serializer.struct(this, RPins.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.PINS,
            SerializationType.BINARY,
            serializer.getDependencies());
    }
}
