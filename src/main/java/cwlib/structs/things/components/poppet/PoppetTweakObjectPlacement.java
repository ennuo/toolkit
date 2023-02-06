package cwlib.structs.things.components.poppet;

import org.joml.Matrix4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.GlobalThingDescriptor;
import cwlib.types.data.ResourceDescriptor;

public class PoppetTweakObjectPlacement implements Serializable {
    public static int BASE_ALLOCATION_SIZE = 0x100;

    public Thing[] objectList;
    public Matrix4f startMatrix;

    @GsonRevision(max=0x15f)
    @Deprecated public GlobalThingDescriptor thing;
    @GsonRevision(min=0x160)
    public ResourceDescriptor plan;

    public Thing proxyObject;

    public int backZ;
    public int thickness;

    public float backToZPos;
    public float rotate;
    public float scale;

    @GsonRevision(min=0x26c)
    public int lastGridMoveFrame, lastGridRotateFrame, lastGridScaleFrame;


    @SuppressWarnings("unchecked")
    @Override public PoppetTweakObjectPlacement serialize(Serializer serializer, Serializable structure) {
        PoppetTweakObjectPlacement object = 
            (structure == null) ? new PoppetTweakObjectPlacement() : (PoppetTweakObjectPlacement) structure;

        int version = serializer.getRevision().getVersion();

        object.objectList = serializer.thingarray(object.objectList);
        object.startMatrix = serializer.m44(object.startMatrix);
        
        if (version < 0x160) 
            object.thing = serializer.struct(object.thing, GlobalThingDescriptor.class);
        else
            object.plan = serializer.resource(object.plan, ResourceType.PLAN, true, false, false);
        
        object.proxyObject = serializer.thing(object.proxyObject);
        object.backZ = serializer.s32(object.backZ);
        object.thickness = serializer.s32(object.thickness);
        object.backToZPos = serializer.f32(object.backToZPos);
        object.rotate = serializer.f32(object.rotate);
        object.scale = serializer.f32(object.scale);

        if (version  > 0x26b) {
            object.lastGridMoveFrame = serializer.i32(object.lastGridMoveFrame);
            object.lastGridRotateFrame = serializer.i32(object.lastGridRotateFrame);
            object.lastGridScaleFrame = serializer.i32(object.lastGridScaleFrame);
        }

        return object;
    }


    @Override public int getAllocatedSize() { return PoppetTweakObjectPlacement.BASE_ALLOCATION_SIZE; }
    

}