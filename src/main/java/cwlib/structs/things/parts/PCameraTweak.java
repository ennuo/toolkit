package cwlib.structs.things.parts;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PCameraTweak implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;
    public static boolean ENABLE_IMPROPER_LOADING = false;

    @Deprecated public Vector3f pitchAngle;
    @Deprecated public Vector4f targetBox;

    public Vector4f triggerBox;

    public byte triggerLayerOffset, triggerLayerDepth;
    public boolean isCameraZRelative;

    @Deprecated public float zoomDistance;

    public float positionFactor;
    public int photoBoothTimerLength, cameraType;
    public float activationLimit;
    public boolean disableZoomMode, requireAll = true, motionControllerZone;
    public int behavior;
    public byte cutSceneTransitionType;
    public int cutSceneHoldTime;
    public boolean cutSceneSkippable;
    public byte cutSceneUseHoldTime;

    @SuppressWarnings("unchecked")
    @Override public PCameraTweak serialize(Serializer serializer, Serializable structure) {
        PCameraTweak tweak = (structure == null) ? new PCameraTweak() : (PCameraTweak) structure;

        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        if (version < 0x37e) {
            tweak.pitchAngle = serializer.v3(tweak.pitchAngle);
            tweak.targetBox = serializer.v4(tweak.targetBox);
        }

        tweak.triggerBox = serializer.v4(tweak.triggerBox);

        if (subVersion >= 0x1b) {
            tweak.triggerLayerOffset = serializer.i8(tweak.triggerLayerOffset);
            tweak.triggerLayerDepth = serializer.i8(tweak.triggerLayerDepth);
            if (subVersion >= 0x3d)
                tweak.isCameraZRelative = serializer.bool(tweak.isCameraZRelative);
        }

        if (version < 0x37e)
            tweak.zoomDistance = serializer.f32(tweak.zoomDistance);

        tweak.positionFactor = serializer.f32(tweak.positionFactor);

        if (version >= 0x1f5)
            tweak.photoBoothTimerLength = serializer.i32(tweak.photoBoothTimerLength);

        // some 0x13d and 0x176 levels somehow don't serialize this,
        // if a level at that revision crashes,
        // it might be this
        if (ENABLE_IMPROPER_LOADING && (version == 0x13d || version == 0x176)) {
            if (!serializer.isWriting())
                serializer.log("ADD CAMERA TYPE HERE", 1);
        } else {
            if (version < 0x1d7) 
                tweak.cameraType = serializer.u8(tweak.cameraType);
            else
                tweak.cameraType = serializer.s32(tweak.cameraType);
        }
        

        if (version > 0x196 && version < 0x2c4)
            tweak.activationLimit = serializer.f32(tweak.activationLimit);

        if (version > 0x1b6 && version < 0x1d2) {
            serializer.f32(0);
            serializer.f32(0);
        }

        if (version >= 0x1ff)
            tweak.disableZoomMode = serializer.bool(tweak.disableZoomMode);

        if (version >= 0x26a)
            tweak.requireAll = serializer.bool(tweak.requireAll);

        if (version >= 0x2ba)
            tweak.motionControllerZone = serializer.bool(tweak.motionControllerZone);

        if (version >= 0x2c4)
            tweak.behavior = serializer.i32(tweak.behavior);

        if (version >= 0x2f8 && version < 0x37e)
            serializer.u8(0);
        
        if (version >= 0x2eb) {
            tweak.cutSceneTransitionType = serializer.i8(tweak.cutSceneTransitionType);
            tweak.cutSceneHoldTime = serializer.i32(tweak.cutSceneHoldTime);
        }

        if (version >= 0x2eb)
            tweak.cutSceneSkippable = serializer.bool(tweak.cutSceneSkippable);

        if (serializer.getRevision().getSubVersion() >= 0x9f)
            tweak.cutSceneUseHoldTime = serializer.i8(tweak.cutSceneUseHoldTime);
            
        return tweak;
    }

    @Override public int getAllocatedSize() { return PCameraTweak.BASE_ALLOCATION_SIZE; }
}
