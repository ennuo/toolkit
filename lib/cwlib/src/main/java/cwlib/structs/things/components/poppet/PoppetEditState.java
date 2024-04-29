package cwlib.structs.things.components.poppet;

import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class PoppetEditState implements Serializable
{
    public static int BASE_ALLOCATION_SIZE = 0x20;

    public ObjectState[] editObjectList;

    @GsonRevision(max = 0x2bf)
    public int backZ, frontZ;

    public Thing[] frozenList;
    public float lerpFactor;
    public StickerInfo sticker = new StickerInfo();
    public DecorationInfo decoration = new DecorationInfo();

    @GsonRevision(min = 0x1b6)
    public Thing cursorDummy;
    @GsonRevision(min = 0x148)
    public Thing placementDummy;
    @GsonRevision(min = 0x200)
    public PlacementBodyState[] placementBodyState;

    @GsonRevision(min = 0x148)
    public Thing[] pauseList;

    @GsonRevision(min = 0x1dd)
    public Vector3f vertexCursor;

    @GsonRevision(min = 0x148)
    public float decorativeThingAngle;

    @GsonRevision(min = 0x148)
    public Thing switchConnectorRef, switchConnector;

    @GsonRevision(min = 0x186)
    public float decorativeThingScale;

    @GsonRevision(min = 0x148)
    public PoppetShapeOverride overrideShape = new PoppetShapeOverride();
    @GsonRevision(min = 0x1b6)
    public PoppetMaterialOverride overrideMaterial = new PoppetMaterialOverride();

    @GsonRevision(min = 0x26c)
    public int lastGridMoveFrame;
    @GsonRevision(min = 0x26c, max = 0x2c1)
    public int lastGridRotateFrame, lastGridScaleFrame;

    @GsonRevision(min = 0x27e)
    public int switchConnectorUID;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        editObjectList = serializer.array(editObjectList, ObjectState.class);
        if (version < 0x1a0) serializer.thingarray(null);
        frozenList = serializer.thingarray(frozenList);
        if (version < 0x2bf)
        {
            backZ = serializer.s32(backZ);
            frontZ = serializer.s32(frontZ);
        }
        lerpFactor = serializer.f32(lerpFactor);
        sticker = serializer.struct(sticker, StickerInfo.class);
        decoration = serializer.struct(decoration, DecorationInfo.class);
        if (version < 0x18b)
            serializer.v3(null);
        if (version > 0x1b5)
            cursorDummy = serializer.thing(cursorDummy);
        if (version > 0x147)
            placementDummy = serializer.thing(placementDummy);
        if (version > 0x147 && version < 0x1a0)
        {
            serializer.thingarray(null);
            serializer.thingarray(null);
        }

        if (version > 0x19f)
            placementBodyState = serializer.array(placementBodyState,
                PlacementBodyState.class);
        if (version > 0x147)
            pauseList = serializer.thingarray(pauseList);
        if (version > 0x1dc)
            vertexCursor = serializer.v3(vertexCursor);
        if (version > 0x147)
        {
            decorativeThingAngle = serializer.f32(decorativeThingAngle);
            switchConnectorRef = serializer.thing(switchConnectorRef);
            switchConnector = serializer.thing(switchConnector);
        }

        if (version > 0x147 && version < 0x190)
            serializer.resource(null, ResourceType.SCRIPT);

        if (version > 0x147)
        {
            if (version < 0x190)
                serializer.bool(false);
            if (overrideShape == null)
                overrideShape = new PoppetShapeOverride();
            if (!serializer.isWriting())
                overrideShape.polygon = new Vector3f[serializer.getInput().i32()];
            else
            {
                if (overrideShape.polygon == null)
                    overrideShape.polygon = new Vector3f[0];
                serializer.getOutput().i32(overrideShape.polygon.length);
            }
            for (int i = 0; i < overrideShape.polygon.length; ++i)
                overrideShape.polygon[i] = serializer.v3(overrideShape.polygon[i]);
            overrideShape.loops = serializer.intvector(overrideShape.loops);
        }

        if (version > 0x185)
            decorativeThingScale = serializer.f32(decorativeThingScale);

        if (version > 0x1b5)
        {
            overrideShape.back = serializer.s32(overrideShape.back);
            overrideShape.front = serializer.s32(overrideShape.front);
            overrideShape.scale = serializer.f32(overrideShape.scale);
            overrideShape.angle = serializer.f32(overrideShape.angle);

            overrideMaterial = serializer.struct(overrideMaterial,
                PoppetMaterialOverride.class);
        }

        if (version > 0x26b)
        {
            lastGridMoveFrame = serializer.i32(lastGridMoveFrame);
            if (version < 0x2c2)
            {
                lastGridRotateFrame = serializer.i32(lastGridRotateFrame);
                lastGridScaleFrame = serializer.i32(lastGridScaleFrame);
            }
        }

        if (version > 0x27d)
            switchConnectorUID = serializer.i32(switchConnectorUID);
    }


    @Override
    public int getAllocatedSize()
    {
        return PoppetEditState.BASE_ALLOCATION_SIZE;
    }
}