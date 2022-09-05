package cwlib.structs.things.components.poppet;

import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class PoppetEditState implements Serializable {
    public static int BASE_ALLOCATION_SIZE = 0x20;

    public ObjectState[] editObjectList;

    @GsonRevision(max=0x2bf) 
    public int backZ, frontZ;

    public Thing[] frozenList;
    public float lerpFactor;
    public StickerInfo sticker = new StickerInfo();
    public DecorationInfo decoration = new DecorationInfo();

    @GsonRevision(min=0x1b6)
    public Thing cursorDummy;
    @GsonRevision(min=0x148)
    public Thing placementDummy;
    @GsonRevision(min=0x200)
    public PlacementBodyState[] placementBodyState;

    @GsonRevision(min=0x148)
    public Thing[] pauseList;

    @GsonRevision(min=0x1dd)
    public Vector3f vertexCursor;

    @GsonRevision(min=0x148)
    public float decorativeThingAngle;

    @GsonRevision(min=0x148)
    public Thing switchConnectorRef, switchConnector;

    @GsonRevision(min=0x186)
    public float decorativeThingScale;

    @GsonRevision(min=0x148)
    public PoppetShapeOverride overrideShape = new PoppetShapeOverride();
    @GsonRevision(min=0x1b6)
    public PoppetMaterialOverride overrideMaterial = new PoppetMaterialOverride();

    @GsonRevision(min=0x26c)
    public int lastGridMoveFrame;
    @GsonRevision(min=0x26c, max=0x2c1)
    public int lastGridRotateFrame, lastGridScaleFrame;

    @GsonRevision(min=0x27e)
    public int switchConnectorUID;

    @SuppressWarnings("unchecked")
    @Override public PoppetEditState serialize(Serializer serializer, Serializable structure) {
        PoppetEditState state = (structure == null) ? new PoppetEditState() : (PoppetEditState) structure;

        int version = serializer.getRevision().getVersion();

        state.editObjectList = serializer.array(state.editObjectList, ObjectState.class);
        if (version < 0x1a0) serializer.thingarray(null);
        state.frozenList = serializer.thingarray(state.frozenList);
        if (version < 0x2bf) {
            state.backZ = serializer.s32(state.backZ);
            state.frontZ = serializer.s32(state.frontZ);
        }
        state.lerpFactor = serializer.f32(state.lerpFactor);
        state.sticker = serializer.struct(state.sticker, StickerInfo.class);
        state.decoration = serializer.struct(state.decoration, DecorationInfo.class);
        if (version < 0x18b)
            serializer.v3(null);
        if (version > 0x1b5)
            state.cursorDummy = serializer.thing(state.cursorDummy);
        if (version > 0x147)
            state.placementDummy = serializer.thing(state.placementDummy);
        if (version > 0x147 && version < 0x1a0) {
            serializer.thingarray(null);
            serializer.thingarray(null);
        }

        if (version > 0x19f)
            state.placementBodyState = serializer.array(state.placementBodyState, PlacementBodyState.class);
        if (version > 0x147)
            state.pauseList = serializer.thingarray(state.pauseList);
        if (version > 0x1dc)
            state.vertexCursor = serializer.v3(state.vertexCursor);
        if (version > 0x147) {
            state.decorativeThingAngle = serializer.f32(state.decorativeThingAngle);
            state.switchConnectorRef = serializer.thing(state.switchConnectorRef);
            state.switchConnector = serializer.thing(state.switchConnector);
        }

        if (version > 0x147 && version < 0x190)
            serializer.resource(null, ResourceType.SCRIPT);

        if (version > 0x147) {
            if (version < 0x190)
                serializer.bool(false);
            if (state.overrideShape == null)
                state.overrideShape = new PoppetShapeOverride();
            if (!serializer.isWriting()) state.overrideShape.polygon = new Vector3f[serializer.getInput().i32()];
            else {
                if (state.overrideShape.polygon == null)
                    state.overrideShape.polygon = new Vector3f[0];
                serializer.getOutput().i32(state.overrideShape.polygon.length);
            }
            for (int i = 0; i < state.overrideShape.polygon.length; ++i)
                state.overrideShape.polygon[i] = serializer.v3(state.overrideShape.polygon[i]);
            state.overrideShape.loops = serializer.intvector(state.overrideShape.loops);
        }
        
        if (version > 0x185)
            state.decorativeThingScale = serializer.f32(state.decorativeThingScale);

        if (version > 0x1b5) {
            state.overrideShape.back = serializer.s32(state.overrideShape.back);
            state.overrideShape.front = serializer.s32(state.overrideShape.front);
            state.overrideShape.scale = serializer.f32(state.overrideShape.scale);
            state.overrideShape.angle = serializer.f32(state.overrideShape.angle);

            state.overrideMaterial = serializer.struct(state.overrideMaterial, PoppetMaterialOverride.class);
        }

        if (version > 0x26b) {
            state.lastGridMoveFrame = serializer.i32(state.lastGridMoveFrame);
            if (version < 0x2c2) {
                state.lastGridRotateFrame = serializer.i32(state.lastGridRotateFrame);
                state.lastGridScaleFrame = serializer.i32(state.lastGridScaleFrame);
            }
        }

        if (version > 0x27d)
            state.switchConnectorUID = serializer.i32(state.switchConnectorUID);

        return state;
    }


    @Override public int getAllocatedSize() { return PoppetEditState.BASE_ALLOCATION_SIZE; }
}