package cwlib.structs.things.components.poppet;

import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class PoppetEditState implements Serializable {
    public static int BASE_ALLOCATION_SIZE = 0x20;

    public ObjectState[] editObjectList;
    public int backZ;
    public int frontZ;
    public Thing[] frozenList;
    public float lerpFactor;
    public StickerInfo sticker = new StickerInfo();
    public DecorationInfo decoration = new DecorationInfo();
    public Thing cursorDummy;
    public Thing placementDummy;
    public PlacementBodyState[] placementBodyState;
    public Thing[] pauseList;
    public Vector3f vertexCursor;
    public float decorativeThingAngle;
    public Thing switchConnectorRef;
    public Thing switchConnector;
    public Vector3f[] overridePolygon;
    public int[] overrideLoops;
    public float decorativeThingScale;
    public int overrideBack, overrideFront;
    public float overrideScale;
    public float overrideAngle;
    public ResourceDescriptor overrideGfxMaterial;
    public ResourceDescriptor overrideBevel;
    public ResourceDescriptor overridePhysicsMaterial;
    public int overrideSoundEnumOverride;
    public float overrideBevelSize;
    public ResourceDescriptor overrideMaterialPlan;
    public int lastGridMoveFrame;
    public int lastGridRotateFrame;
    public int lastGridScaleFrame;
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
            if (!serializer.isWriting()) state.overridePolygon = new Vector3f[serializer.getInput().i32()];
            else {
                if (state.overridePolygon == null)
                    state.overridePolygon = new Vector3f[0];
                serializer.getOutput().i32(state.overridePolygon.length);
            }
            for (int i = 0; i < state.overridePolygon.length; ++i)
                state.overridePolygon[i] = serializer.v3(state.overridePolygon[i]);
            state.overrideLoops = serializer.intvector(state.overrideLoops);
        }
        
        if (version > 0x185)
            state.decorativeThingScale = serializer.f32(state.decorativeThingScale);

        if (version > 0x1b5) {
            state.overrideBack = serializer.s32(state.overrideBack);
            state.overrideFront = serializer.s32(state.overrideFront);
            state.overrideScale = serializer.f32(state.overrideScale);
            state.overrideAngle = serializer.f32(state.overrideAngle);
            state.overrideGfxMaterial = serializer.resource(state.overrideGfxMaterial, ResourceType.GFX_MATERIAL);
            state.overrideBevel = serializer.resource(state.overrideBevel, ResourceType.BEVEL);
            state.overridePhysicsMaterial = serializer.resource(state.overridePhysicsMaterial, ResourceType.MATERIAL);
            state.overrideSoundEnumOverride = serializer.i32(state.overrideSoundEnumOverride);
            state.overrideBevelSize = serializer.f32(state.overrideBevelSize);
            state.overrideMaterialPlan = serializer.resource(state.overrideMaterialPlan, ResourceType.PLAN, true);
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