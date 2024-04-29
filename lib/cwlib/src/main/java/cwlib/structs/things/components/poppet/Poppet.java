package cwlib.structs.things.components.poppet;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class Poppet implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x0;

    public boolean isInUse;
    public PoppetMode[] modeStack;

    @GsonRevision(max = 0x2ec)
    @Deprecated
    PoppetEditState edit = new PoppetEditState();

    @GsonRevision(min = 0x147, max = 0x2ec)
    @Deprecated
    public Thing tweakObject;

    @GsonRevision(min = 0x147, max = 0x2ec)
    @Deprecated
    public Vector4f backupCameraZoneTargetBox;

    @GsonRevision(min = 0x147, max = 0x2ec)
    @Deprecated
    public Vector3f backupCameraZonePitchAngle;

    @GsonRevision(min = 0x147, max = 0x2ec)
    @Deprecated
    public float backupCameraZoneZoomDistance;

    @GsonRevision(min = 0x147, max = 0x2ec)
    @Deprecated
    public float cameraZoneZoomSpeed;

    @GsonRevision(min = 0x147, max = 0x2ec)
    @Deprecated
    public PoppetTweakObjectPlacement tweakObjectPlacement = new PoppetTweakObjectPlacement();

    @GsonRevision(min = 0x1dd, max = 0x2ec)
    @Deprecated
    public Vector3f marqueeSelectOrigin;

    @GsonRevision(min = 0x1dd, max = 0x2ec)
    @Deprecated
    public Thing[] marqueeSelectList;

    @GsonRevision(min = 0x232)
    public RaycastResults raycast = new RaycastResults();

    @GsonRevision(min = 0x232, max = 0x2ec)
    @Deprecated
    public int dangerMode;

    @GsonRevision(min = 0x2ed)
    public Thing[] frozenList;

    @GsonRevision(min = 0x2f2)
    public Thing[] hiddenList;

    @GsonRevision(min = 0x218)
    public PoppetMaterialOverride overrideMaterial = new PoppetMaterialOverride();

    @GsonRevision(min = 0x218)
    public PoppetShapeOverride overrideShape = new PoppetShapeOverride();

    @GsonRevision(min = 0x3a0)
    public Thing[] tweakObjects;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        isInUse = serializer.bool(isInUse);
        modeStack = serializer.array(modeStack, PoppetMode.class);

        if (version < 0x2ed)
        {
            if (version < 0x232)
                serializer.v3(null);
            if (version < 0x135)
                serializer.i32(0); // c32

            // revision - 0x185 < 0x35
            if (version >= 0x185 && version < 0x1ba)
                serializer.i32(0); // scriptobjectuid

            edit = serializer.struct(edit, PoppetEditState.class);

            if (version < 0x18f)
                serializer.intarray(null);
            if (version >= 0x148 && version < 0x185)
                serializer.thing(null);

            if (version >= 0x147)
            {
                tweakObject = serializer.thing(tweakObject);
                backupCameraZoneTargetBox = serializer.v4(backupCameraZoneTargetBox);
                backupCameraZonePitchAngle =
                    serializer.v3(backupCameraZonePitchAngle);
                backupCameraZoneZoomDistance =
                    serializer.f32(backupCameraZoneZoomDistance);
                cameraZoneZoomSpeed = serializer.f32(cameraZoneZoomSpeed);
                tweakObjectPlacement = serializer.struct(tweakObjectPlacement,
                    PoppetTweakObjectPlacement.class);
                if (version < 0x211)
                    serializer.bool(false);
            }

            if (version > 0x184 && version < 0x1dd)
                serializer.v3(null);

            if (version > 0x1dc)
            {
                if (version < 0x232) serializer.bool(false);
                marqueeSelectOrigin = serializer.v3(marqueeSelectOrigin);
                marqueeSelectList = serializer.thingarray(marqueeSelectList);
            }

            if (version >= 0x218)
                overrideMaterial = serializer.struct(overrideMaterial,
                    PoppetMaterialOverride.class);

            if (version >= 0x1b8 && version < 0x1e2)
                serializer.i32(0);
            if (version >= 0x1ba && version < 0x1e2)
                serializer.i32(0);

            if (version >= 0x232)
                raycast = serializer.struct(raycast, RaycastResults.class);
            if (version >= 0x232)
                dangerMode = serializer.i32(dangerMode);
            if (version >= 0x236)
            {
                serializer.i32(0);
                serializer.v3(null);
                if (version >= 0x23a)
                    serializer.v3(null);
            }

            return;
        }

        raycast = serializer.struct(raycast, RaycastResults.class);
        if (version > 0x2ec)
            frozenList = serializer.thingarray(frozenList);
        if (version > 0x2f1)
            hiddenList = serializer.thingarray(hiddenList);
        if (version >= 0x311)
        {
            overrideMaterial = serializer.struct(overrideMaterial,
                PoppetMaterialOverride.class);
            overrideShape = serializer.struct(overrideShape,
                PoppetShapeOverride.class);
        }

        if (version >= 0x3a0)
            tweakObjects = serializer.thingarray(tweakObjects);
    }

    @Override
    public int getAllocatedSize()
    {
        return Poppet.BASE_ALLOCATION_SIZE;
    }
}
