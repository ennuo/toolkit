package cwlib.structs.things.parts;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.CameraNode;
import de.javagl.jgltf.impl.v1.Camera;

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
    public boolean cutSceneSkippable, cutSceneUseHoldTime;
    public byte cutSceneTimeSinceUsed;
    public int cutSceneTransitionTime;
    public int cutSceneColour;
    public boolean cutSceneMovieMode;
    public float cutSceneDepthOfField, cutSceneFog, cutSceneFOV, cutSceneShake;
    public boolean fadeAudio, oldStyleCameraZone;
    public boolean cutSceneTrackPlayer, cutSceneSendsSignalOnCancelled, cutSceneWasActiveLastFrame;
    public CameraNode[] nodes;
    public float frontDOF, sackTrackDOF;
    public boolean allowSmoothZTransition;

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
            tweak.cutSceneUseHoldTime = serializer.bool(tweak.cutSceneUseHoldTime);

        if (version > 0x2ea && version < 0x2f1) {
            serializer.wstr(null);
            serializer.s32(0);
        }

        if (version > 0x2ed) {
            tweak.cutSceneTimeSinceUsed = serializer.i8(tweak.cutSceneTimeSinceUsed);
            tweak.cutSceneTransitionTime = serializer.i32(tweak.cutSceneTransitionTime);
            if (version < 0x35a)
                serializer.bool(false);
        }

        if (version > 0x359)
            tweak.cutSceneColour = serializer.i32(tweak.cutSceneColour);
        
        if (version > 0x2ed)
            tweak.cutSceneMovieMode = serializer.bool(tweak.cutSceneMovieMode);
        
        if (version > 0x2f7) {
            tweak.cutSceneDepthOfField = serializer.f32(tweak.cutSceneDepthOfField);
            tweak.cutSceneFog = serializer.f32(tweak.cutSceneFog);
        }

        if (version > 0x2f8)
            tweak.cutSceneFOV = serializer.f32(tweak.cutSceneFOV);

        if (version > 0x315)
            tweak.cutSceneShake = serializer.f32(tweak.cutSceneShake);
        
        if (version > 0x318)
            tweak.fadeAudio = serializer.bool(tweak.fadeAudio);
        if (version > 0x33e)
            tweak.oldStyleCameraZone = serializer.bool(tweak.oldStyleCameraZone);

        if (version > 0x369)
            tweak.cutSceneTrackPlayer = serializer.bool(tweak.cutSceneTrackPlayer);
        if (version > 0x395)
            tweak.cutSceneSendsSignalOnCancelled = serializer.bool(tweak.cutSceneSendsSignalOnCancelled);
        if (version > 0x396)
            tweak.cutSceneWasActiveLastFrame = serializer.bool(tweak.cutSceneWasActiveLastFrame);
        
        if (version > 0x37d) 
            tweak.nodes = serializer.array(tweak.nodes, CameraNode.class);
        
        if (subVersion > 0x7d)
            tweak.frontDOF = serializer.f32(tweak.frontDOF);
        if (subVersion > 0x79)
            tweak.sackTrackDOF = serializer.f32(tweak.sackTrackDOF);
        if (subVersion > 0x7f)
            tweak.allowSmoothZTransition = serializer.bool(tweak.allowSmoothZTransition);
            
        return tweak;
    }

    @Override public int getAllocatedSize() { return PCameraTweak.BASE_ALLOCATION_SIZE; }
}
