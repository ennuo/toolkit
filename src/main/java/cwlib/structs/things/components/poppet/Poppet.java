package cwlib.structs.things.components.poppet;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class Poppet implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x0;

    public boolean isInUse;
    public PoppetMode[] modeStack;
    
    @GsonRevision(max=0x2ec)
    @Deprecated PoppetEditState edit = new PoppetEditState();

    @GsonRevision(min=0x147, max=0x2ec)
    @Deprecated public Thing tweakObject;

    @GsonRevision(min=0x147, max=0x2ec)
    @Deprecated public Vector4f backupCameraZoneTargetBox;

    @GsonRevision(min=0x147, max=0x2ec)
    @Deprecated public Vector3f backupCameraZonePitchAngle;

    @GsonRevision(min=0x147, max=0x2ec)
    @Deprecated public float backupCameraZoneZoomDistance;

    @GsonRevision(min=0x147, max=0x2ec)
    @Deprecated public float cameraZoneZoomSpeed;

    @GsonRevision(min=0x147, max=0x2ec)
    @Deprecated public PoppetTweakObjectPlacement tweakObjectPlacement = new PoppetTweakObjectPlacement();
    
    @GsonRevision(min=0x1dd, max=0x2ec)
    @Deprecated public Vector3f marqueeSelectOrigin;

    @GsonRevision(min=0x1dd, max=0x2ec)
    @Deprecated public Thing[] marqueeSelectList;

    @GsonRevision(min=0x232)
    public RaycastResults raycast = new RaycastResults();

    @GsonRevision(min=0x232, max=0x2ec)
    @Deprecated public int dangerMode;

    @GsonRevision(min=0x2ed)
    public Thing[] frozenList;

    @GsonRevision(min=0x2f2)
    public Thing[] hiddenList;

    @GsonRevision(min=0x218)
    public PoppetMaterialOverride overrideMaterial = new PoppetMaterialOverride();

    @GsonRevision(min=0x218)
    public PoppetShapeOverride overrideShape = new PoppetShapeOverride();

    @GsonRevision(min=0x3a0)
    public Thing[] tweakObjects;

    @SuppressWarnings("unchecked")
    @Override public Poppet serialize(Serializer serializer, Serializable structure) {
        Poppet poppet = (structure == null) ? new Poppet() : (Poppet) structure;

        int version = serializer.getRevision().getVersion();

        poppet.isInUse = serializer.bool(poppet.isInUse);
        poppet.modeStack = serializer.array(poppet.modeStack, PoppetMode.class);

        if (version < 0x2ed) {
            if (version < 0x232)
                serializer.v3(null);
            if (version < 0x135)
                serializer.i32(0); // c32
            
            // revision - 0x185 < 0x35
            if (version >= 0x185 && version < 0x1ba)
                serializer.i32(0); // scriptobjectuid

            poppet.edit = serializer.struct(poppet.edit, PoppetEditState.class);
            
            if (version < 0x18f)
                serializer.intarray(null);
            if (version >= 0x148 && version < 0x185)
                serializer.thing(null);

            if (version >= 0x147) {
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

            if (version >= 0x218)
                poppet.overrideMaterial = serializer.struct(poppet.overrideMaterial, PoppetMaterialOverride.class);
            
            if (version >= 0x1b8 && version < 0x1e2)
                serializer.i32(0);
            if (version >= 0x1ba && version < 0x1e2)
                serializer.i32(0);
            
            if (version >= 0x232)
                poppet.raycast = serializer.struct(poppet.raycast, RaycastResults.class);
            if (version >= 0x232)
                poppet.dangerMode = serializer.i32(poppet.dangerMode);
            if (version >= 0x236) {
                serializer.i32(0);
                serializer.v3(null);
                if (version >= 0x23a)
                    serializer.v3(null);
            }

            return poppet;
        }

        poppet.raycast = serializer.struct(poppet.raycast, RaycastResults.class);
        if (version > 0x2ec)
            poppet.frozenList = serializer.thingarray(poppet.frozenList);
        if (version > 0x2f1)
            poppet.hiddenList = serializer.thingarray(poppet.hiddenList);
        if (version >= 0x311) {
            poppet.overrideMaterial = serializer.struct(poppet.overrideMaterial, PoppetMaterialOverride.class);
            poppet.overrideShape = serializer.struct(poppet.overrideShape, PoppetShapeOverride.class);
        }

        if (version >= 0x3a0)
            poppet.tweakObjects = serializer.thingarray(poppet.tweakObjects);

        return poppet;
    }

    @Override public int getAllocatedSize() {
        return Poppet.BASE_ALLOCATION_SIZE;
    }
}
