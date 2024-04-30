package cwlib.structs.adventure;

import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class PlanetArea implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public int areaID;
    public ResourceDescriptor mainAreaDescriptor;
    public ResourceDescriptor shadowAreaDescriptor;
    public int ambienceTrack;
    public float ambienceVolume;
    public Thing[] spritelights;
    public Vector3f[] spritelightPositions;
    public Thing area;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x109)
            areaID = serializer.s32(areaID);

        if (subVersion < 0x187)
        {
            if (subVersion > 0x109)
                area = serializer.thing(area);
        }
        else
        {
            if (subVersion > 0x186)
                mainAreaDescriptor = serializer.resource(mainAreaDescriptor,
                    ResourceType.PLAN, true);
            if (subVersion > 0x18b)
                shadowAreaDescriptor = serializer.resource(shadowAreaDescriptor,
                    ResourceType.PLAN, true);
        }

        if (subVersion > 0x178)
        {
            ambienceTrack = serializer.s32(ambienceTrack);
            ambienceVolume = serializer.f32(ambienceVolume);
        }

        if (subVersion > 0x179)
        {
            spritelights = serializer.thingarray(spritelights);

            int numPositions = serializer.i32(spritelightPositions != null ?
                spritelightPositions.length : 0);
            if (!serializer.isWriting())
                spritelightPositions = new Vector3f[numPositions];
            for (int i = 0; i < numPositions; ++i)
                spritelightPositions[i] = serializer.v3(spritelightPositions[i]);

            area = serializer.thing(area);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PlanetArea.BASE_ALLOCATION_SIZE;
        if (this.spritelights != null)
            size += (this.spritelights.length * 0x4);
        if (this.spritelightPositions != null)
            size += (this.spritelightPositions.length * 0x10);
        return size;
    }
}
