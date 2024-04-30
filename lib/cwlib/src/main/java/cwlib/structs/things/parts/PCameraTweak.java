package cwlib.structs.things.parts;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.CameraNode;

public class PCameraTweak implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;
    public static boolean ENABLE_IMPROPER_LOADING = false;

    @GsonRevision(max = 0x37d)
    @Deprecated
    public Vector3f pitchAngle;
    @GsonRevision(max = 0x37d)
    @Deprecated
    public Vector4f targetBox;

    public Vector4f triggerBox;

    @GsonRevision(lbp3 = true, min = 0x1b)
    public byte triggerLayerOffset, triggerLayerDepth;
    @GsonRevision(lbp3 = true, min = 0x3d)
    public boolean isCameraZRelative;

    @GsonRevision(max = 0x37d)
    @Deprecated
    public float zoomDistance = 1000.0f;

    public float positionFactor;

    @GsonRevision(min = 0x1f5)
    public int photoBoothTimerLength;
    public int cameraType;

    @GsonRevision(min = 0x197, max = 0x2c3)
    @Deprecated
    public float activationLimit = 1.0f;

    @GsonRevision(min = 0x1ff)
    public boolean disableZoomMode;

    @GsonRevision(min = 0x26a)
    public boolean requireAll = true;

    @GsonRevision(min = 0x2ba)
    public boolean motionControllerZone;

    @GsonRevision(min = 0x2c4)
    public int behavior;

    @GsonRevision(min = 0x2eb)
    public byte cutSceneTransitionType;
    @GsonRevision(min = 0x2eb)
    public int cutSceneHoldTime;

    @GsonRevision(min = 0x2eb)
    public boolean cutSceneSkippable;

    @GsonRevision(lbp3 = true, min = 0x9f)
    public boolean cutSceneUseHoldTime;

    @GsonRevision(min = 0x2ee)
    public byte cutSceneTimeSinceUsed;
    @GsonRevision(min = 0x2ee)
    public int cutSceneTransitionTime;

    @GsonRevision(min = 0x35a)
    public int cutSceneColour;

    @GsonRevision(min = 0x2ee)
    public boolean cutSceneMovieMode;


    @GsonRevision(min = 0x2f8)
    public float cutSceneDepthOfField, cutSceneFog;

    @GsonRevision(min = 0x2f9)
    public float cutSceneFOV;
    @GsonRevision(min = 0x316)
    public float cutSceneShake;

    @GsonRevision(min = 0x319)
    public boolean fadeAudio;
    @GsonRevision(min = 0x33f)
    public boolean oldStyleCameraZone;

    @GsonRevision(min = 0x36a)
    public boolean cutSceneTrackPlayer;

    @GsonRevision(min = 0x396)
    public boolean cutSceneSendsSignalOnCancelled;

    @GsonRevision(min = 0x397)
    public boolean cutSceneWasActiveLastFrame;

    @GsonRevision(min = 0x37e)
    public CameraNode[] nodes;

    @GsonRevision(lbp3 = true, min = 0x7e)
    public float frontDOF;

    @GsonRevision(lbp3 = true, min = 0x7a)
    public float sackTrackDOF;

    @GsonRevision(lbp3 = true, min = 0x80)
    public boolean allowSmoothZTransition;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        if (version < 0x37e)
        {
            pitchAngle = serializer.v3(pitchAngle);
            targetBox = serializer.v4(targetBox);
        }

        triggerBox = serializer.v4(triggerBox);

        if (subVersion >= 0x1b)
        {
            triggerLayerOffset = serializer.i8(triggerLayerOffset);
            triggerLayerDepth = serializer.i8(triggerLayerDepth);
            if (subVersion >= 0x3d)
                isCameraZRelative = serializer.bool(isCameraZRelative);
        }

        if (version < 0x37e)
            zoomDistance = serializer.f32(zoomDistance);

        positionFactor = serializer.f32(positionFactor);

        if (version >= 0x1f5)
            photoBoothTimerLength = serializer.i32(photoBoothTimerLength);

        // some 0x13d and 0x176 levels somehow don't serialize this,
        // if a level at that revision crashes,
        // it might be this
        if (ENABLE_IMPROPER_LOADING && (version == 0x13d || version == 0x176))
        {
            if (!serializer.isWriting())
                serializer.log("ADD CAMERA TYPE HERE", 1);
        }
        else
        {
            if (version < 0x1d7)
                cameraType = serializer.u8(cameraType);
            else
                cameraType = serializer.s32(cameraType);
        }


        if (version > 0x196 && version < 0x2c4)
            activationLimit = serializer.f32(activationLimit);

        if (version > 0x1b6 && version < 0x1d2)
        {
            serializer.f32(0);
            serializer.f32(0);
        }

        if (version >= 0x1ff)
            disableZoomMode = serializer.bool(disableZoomMode);

        if (version >= 0x26a)
            requireAll = serializer.bool(requireAll);

        if (version >= 0x2ba)
            motionControllerZone = serializer.bool(motionControllerZone);

        if (version >= 0x2c4)
            behavior = serializer.i32(behavior);

        if (version >= 0x2f8 && version < 0x37e)
            serializer.u8(0);

        if (version >= 0x2eb)
        {
            cutSceneTransitionType = serializer.i8(cutSceneTransitionType);
            cutSceneHoldTime = serializer.i32(cutSceneHoldTime);
        }

        if (version >= 0x2eb)
            cutSceneSkippable = serializer.bool(cutSceneSkippable);

        if (serializer.getRevision().getSubVersion() >= 0x9f)
            cutSceneUseHoldTime = serializer.bool(cutSceneUseHoldTime);

        if (version > 0x2ea && version < 0x2f1)
        {
            serializer.wstr(null);
            serializer.s32(0);
        }

        if (version > 0x2ed)
        {
            cutSceneTimeSinceUsed = serializer.i8(cutSceneTimeSinceUsed);
            cutSceneTransitionTime = serializer.i32(cutSceneTransitionTime);
            if (version < 0x35a)
                serializer.bool(false);
        }

        if (version > 0x359)
            cutSceneColour = serializer.i32(cutSceneColour);

        if (version > 0x2ed)
            cutSceneMovieMode = serializer.bool(cutSceneMovieMode);

        if (version > 0x2f7)
        {
            cutSceneDepthOfField = serializer.f32(cutSceneDepthOfField);
            cutSceneFog = serializer.f32(cutSceneFog);
        }

        if (version > 0x2f8)
            cutSceneFOV = serializer.f32(cutSceneFOV);

        if (version > 0x315)
            cutSceneShake = serializer.f32(cutSceneShake);

        if (version > 0x318)
            fadeAudio = serializer.bool(fadeAudio);
        if (version > 0x33e)
            oldStyleCameraZone = serializer.bool(oldStyleCameraZone);

        if (version > 0x369)
            cutSceneTrackPlayer = serializer.bool(cutSceneTrackPlayer);
        if (version > 0x395)
            cutSceneSendsSignalOnCancelled =
                serializer.bool(cutSceneSendsSignalOnCancelled);
        if (version > 0x396)
            cutSceneWasActiveLastFrame = serializer.bool(cutSceneWasActiveLastFrame);

        if (version > 0x37d)
        {
            nodes = serializer.array(nodes, CameraNode.class);

            // Fill in the LBP1 data with the first node if it exists
            if (!serializer.isWriting() && nodes != null && nodes.length > 0)
            {
                CameraNode node = nodes[0];
                pitchAngle = node.pitchAngle;
                targetBox = node.targetBox;
                zoomDistance = node.zoomDistance;
            }
        }
        // Cache an LBP2 node based on this data
        else if (!serializer.isWriting())
        {
            nodes = new CameraNode[1];
            CameraNode node = new CameraNode();
            node.pitchAngle = pitchAngle;
            node.targetBox = targetBox;
            node.zoomDistance = zoomDistance;
            nodes[0] = node;
        }

        if (subVersion > 0x7d)
            frontDOF = serializer.f32(frontDOF);
        if (subVersion > 0x79)
            sackTrackDOF = serializer.f32(sackTrackDOF);
        if (subVersion > 0x7f)
            allowSmoothZTransition = serializer.bool(allowSmoothZTransition);
    }

    @Override
    public int getAllocatedSize()
    {
        return PCameraTweak.BASE_ALLOCATION_SIZE;
    }
}
