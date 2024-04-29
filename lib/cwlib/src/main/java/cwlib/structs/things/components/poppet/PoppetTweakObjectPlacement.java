package cwlib.structs.things.components.poppet;

import org.joml.Matrix4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.GlobalThingDescriptor;
import cwlib.types.data.ResourceDescriptor;

public class PoppetTweakObjectPlacement implements Serializable
{
    public static int BASE_ALLOCATION_SIZE = 0x100;

    public Thing[] objectList;
    public Matrix4f startMatrix;

    @GsonRevision(max = 0x15f)
    @Deprecated
    public GlobalThingDescriptor thing;
    @GsonRevision(min = 0x160)
    public ResourceDescriptor plan;

    public Thing proxyObject;

    public int backZ;
    public int thickness;

    public float backToZPos;
    public float rotate;
    public float scale;

    @GsonRevision(min = 0x26c)
    public int lastGridMoveFrame, lastGridRotateFrame, lastGridScaleFrame;


    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        objectList = serializer.thingarray(objectList);
        startMatrix = serializer.m44(startMatrix);

        if (version < 0x160)
            thing = serializer.struct(thing, GlobalThingDescriptor.class);
        else
            plan = serializer.resource(plan, ResourceType.PLAN, true, false, false);

        proxyObject = serializer.thing(proxyObject);
        backZ = serializer.s32(backZ);
        thickness = serializer.s32(thickness);
        backToZPos = serializer.f32(backToZPos);
        rotate = serializer.f32(rotate);
        scale = serializer.f32(scale);

        if (version > 0x26b)
        {
            lastGridMoveFrame = serializer.i32(lastGridMoveFrame);
            lastGridRotateFrame = serializer.i32(lastGridRotateFrame);
            lastGridScaleFrame = serializer.i32(lastGridScaleFrame);
        }
    }


    @Override
    public int getAllocatedSize()
    {
        return PoppetTweakObjectPlacement.BASE_ALLOCATION_SIZE;
    }


}