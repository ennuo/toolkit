package cwlib.structs.adventure;

import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class PlanetArea implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public int areaID;
    public ResourceDescriptor mainAreaDescriptor;
    public ResourceDescriptor shadowAreaDescriptor;
    public int ambienceTrack;
    public float ambienceVolume;
    public Thing[] spritelights;
    public Vector3f[] spritelightPositions;
    public Thing area;

    @SuppressWarnings("unchecked")
    @Override public PlanetArea serialize(Serializer serializer, Serializable structure) {
        PlanetArea area = (structure == null) ? new PlanetArea() : (PlanetArea) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x109) 
            area.areaID = serializer.s32(area.areaID);
        
        if (subVersion < 0x187) {
            if (subVersion > 0x109)
                area.area = serializer.thing(area.area);
        } else {
            if (subVersion > 0x186)
                area.mainAreaDescriptor = serializer.resource(area.mainAreaDescriptor, ResourceType.PLAN, true);
            if (subVersion > 0x18b)
                area.shadowAreaDescriptor = serializer.resource(area.shadowAreaDescriptor, ResourceType.PLAN, true);
        }

        if (subVersion > 0x178) {
            area.ambienceTrack = serializer.s32(area.ambienceTrack);
            area.ambienceVolume = serializer.f32(area.ambienceVolume);
        }

        if (subVersion > 0x179) {
            area.spritelights = serializer.thingarray(area.spritelights);
        
            int numPositions = serializer.i32(area.spritelightPositions != null ? area.spritelightPositions.length : 0);
            if (!serializer.isWriting()) 
                area.spritelightPositions = new Vector3f[numPositions];
            for (int i = 0; i < numPositions; ++i)
                area.spritelightPositions[i] = serializer.v3(area.spritelightPositions[i]);

            area.area = serializer.thing(area.area);
        }

        return area;
    }

    @Override public int getAllocatedSize() {
        int size = PlanetArea.BASE_ALLOCATION_SIZE;
        if (this.spritelights != null)
            size += (this.spritelights.length * 0x4);
        if (this.spritelightPositions != null)
            size += (this.spritelightPositions.length * 0x10);
        return size;
    }
}
