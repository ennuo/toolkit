package cwlib.structs.things.parts;

import cwlib.enums.Branch;
import cwlib.enums.TriggerType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;

public class PTrigger implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public TriggerType triggerType = TriggerType.RADIUS;
    public Thing[] inThings;
    public float radiusMultiplier = 600.0f;

    @GsonRevision(min = 0x2a, lbp3 = true)
    public byte zRangeHundreds;

    public boolean allZLayers;

    @GsonRevision(min = 0x19b)
    public float hysteresisMultiplier = 1.0f;
    @GsonRevision(min = 0x19b)
    public boolean enabled = true;

    @GsonRevision(min = 0x322)
    public float zOffset;

    @GsonRevision(min = 0x30, branch = 0x4431)
    @GsonRevision(min = 0x90, lbp3 = true)
    public int scoreValue;

    public PTrigger() { }

    public PTrigger(TriggerType type, float radius)
    {
        this.triggerType = type;
        this.radiusMultiplier = radius;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();


        triggerType = serializer.enum8(triggerType);

        if (revision.has(Branch.DOUBLE11, 0x17))
        {
            if (serializer.isWriting())
            {
                MemoryOutputStream stream = serializer.getOutput();
                stream.i32(inThings != null ? inThings.length : 0);
                if (inThings != null)
                {
                    for (Thing thing : inThings)
                    {
                        serializer.thing(thing);
                        stream.s32(0);
                    }
                }
            }
            else
            {
                MemoryInputStream stream = serializer.getInput();
                inThings = new Thing[stream.i32()];
                for (int i = 0; i < inThings.length; ++i)
                {
                    inThings[i] = serializer.thing(null);
                    stream.s32(); // mThingAction
                }
            }
        }
        else
        {
            inThings = serializer.thingarray(inThings);
        }

        radiusMultiplier = serializer.f32(radiusMultiplier);

        if (version < 0x1d5)
            serializer.s32(0); // zLayers?

        if (subVersion >= 0x2a)
            zRangeHundreds = serializer.i8(zRangeHundreds);

        allZLayers = serializer.bool(allZLayers);

        if (version >= 0x19b)
        {
            hysteresisMultiplier = serializer.f32(hysteresisMultiplier);
            enabled = serializer.bool(enabled);
        }

        if (version >= 0x322)
            zOffset = serializer.f32(zOffset);

        if (subVersion >= 0x90 || revision.has(Branch.DOUBLE11, 0x30))
            scoreValue = serializer.s32(scoreValue);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        // We'll actually calculate the size of these Thing's
        // in the Thing class to avoid circular dependencies.
        if (this.inThings != null)
            size += (this.inThings.length) * Thing.BASE_ALLOCATION_SIZE;
        return size;
    }
}
