package cwlib.structs.things.components.poppet;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class Poppet implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x0;

    public boolean isInUse;
    public PoppetMode[] modeStack;
    PoppetEditState edit = new PoppetEditState();
    public Thing tweakObject;
    public Vector4f backupCameraZoneTargetBox;
    public Vector3f backupCameraZonePitchAngle;
    public float backupCameraZoneZoomDistance;
    public float cameraZoneZoomSpeed;
    public PoppetTweakObjectPlacement tweakObjectPlacement = new PoppetTweakObjectPlacement();
    public Vector3f marqueeSelectOrigin;
    public Thing[] marqueeSelectList;

    public ResourceDescriptor floodFillGfxMaterial;
    public ResourceDescriptor floodFillBevel;
    public ResourceDescriptor floodFillPhysicsMaterial;
    public int floodFillSoundEnumOverride;
    public float floodFillBevelSize;
    public ResourceDescriptor floodFillMaterialPlan;

    @SuppressWarnings("unchecked")
    @Override public Poppet serialize(Serializer serializer, Serializable structure) {
        Poppet poppet = (structure == null) ? new Poppet() : (Poppet) structure;

        int version = serializer.getRevision().getVersion();

        poppet.isInUse = serializer.bool(poppet.isInUse);
        poppet.modeStack = serializer.array(poppet.modeStack, PoppetMode.class);

        if (version < 0x232)
            serializer.v3(null);
        if (version < 0x135)
            serializer.i32(0); // c32
        if (version > 0x184 && version < 0x1ba)
            serializer.i32(0); // scriptobjectuid

        poppet.edit = serializer.struct(poppet.edit, PoppetEditState.class);
        
        if (version < 0x18f)
            serializer.intarray(null);
        if (version > 0x147 && version < 0x185)
            serializer.thing(null);

        if (version > 0x147) {
            poppet.tweakObject = serializer.thing(poppet.tweakObject);
            poppet.backupCameraZoneTargetBox = serializer.v4(poppet.backupCameraZoneTargetBox);
            poppet.backupCameraZonePitchAngle = serializer.v3(poppet.backupCameraZonePitchAngle);
            poppet.backupCameraZoneZoomDistance = serializer.f32(poppet.backupCameraZoneZoomDistance);
            poppet.cameraZoneZoomSpeed = serializer.f32(poppet.cameraZoneZoomSpeed);
            poppet.tweakObjectPlacement = serializer.struct(poppet.tweakObjectPlacement, PoppetTweakObjectPlacement.class);
            if (version < 0x211)
                serializer.bool(false);
        }

        if (version > 0x184 && version < 0x1dd)
            serializer.v3(null);
        
        if (version > 0x1dc) {
            if (version < 0x232) serializer.bool(false);
            poppet.marqueeSelectOrigin = serializer.v3(poppet.marqueeSelectOrigin);
            poppet.marqueeSelectList = serializer.thingarray(poppet.marqueeSelectList);
        } 

        if (version > 0x217) {
            poppet.floodFillGfxMaterial = serializer.resource(poppet.floodFillGfxMaterial, ResourceType.GFX_MATERIAL);
            poppet.floodFillBevel = serializer.resource(poppet.floodFillBevel, ResourceType.BEVEL);
            poppet.floodFillPhysicsMaterial = serializer.resource(poppet.floodFillPhysicsMaterial, ResourceType.MATERIAL);
            poppet.floodFillSoundEnumOverride = serializer.i32(poppet.floodFillSoundEnumOverride);
            poppet.floodFillBevelSize = serializer.f32(poppet.floodFillBevelSize);
            poppet.floodFillMaterialPlan = serializer.resource(poppet.floodFillMaterialPlan, ResourceType.PLAN, true);
        }

        if (version < 0x1b8 || version > 0x1e1) {
            if (version < 0x1ba || version > 0x1e1) {
                // raycast
                if (version >= 0x232) {
                    serializer.v4(null); // hitpoint
                    serializer.v4(null); // normal
                    serializer.f32(0); // bary u
                    serializer.f32(0); // bary v
                    serializer.i32(0); // tri index
                    serializer.thing(null); // hitthing
                    serializer.thing(null); // refthing
                    serializer.s32(-1); // oncostumepiece
                    serializer.i32(0); // decorationidx
                    serializer.bool(false); // switchconnector
                }

                if (version >= 0x232)
                    serializer.i32(0); // danger mode

                // inventory
                if (version >= 0x236) {
                    serializer.i32(0);
                    serializer.v3(null);
                    if (version >= 0x23a)
                        serializer.v3(null);
                }

                return poppet;
            }
            serializer.i32(0);
        } else serializer.i32(0);


        return poppet;
    }

    @Override public int getAllocatedSize() {
        return Poppet.BASE_ALLOCATION_SIZE;
    }
}
