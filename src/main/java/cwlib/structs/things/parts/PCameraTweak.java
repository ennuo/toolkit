package cwlib.structs.things.parts;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PCameraTweak implements Serializable {
    public Vector3f pitchAngle;
    public Vector4f targetBox, triggerBox;
    public float zoomDistance, positionFactor;
    public int photoBoothTimerLength, cameraType;
    public float activationLimit;
    public boolean disableZoomMode, requireAll = true, motionControllerZone;
    public int behavior;
    public int cutSceneTransitionType, cutSceneHoldTime;
    public boolean cutSceneSkippable;

    @SuppressWarnings("unchecked")
    @Override public PCameraTweak serialize(Serializer serializer, Serializable structure) {
        PCameraTweak tweak = (structure == null) ? new PCameraTweak() : (PCameraTweak) structure;

        int version = serializer.getRevision().getVersion();

        tweak.pitchAngle = serializer.v3(tweak.pitchAngle);
        tweak.targetBox = serializer.v4(tweak.targetBox);
        tweak.triggerBox = serializer.v4(tweak.triggerBox);
        tweak.zoomDistance = serializer.f32(tweak.zoomDistance);
        tweak.positionFactor = serializer.f32(tweak.positionFactor);

        if (version > 0x1f4)
            tweak.photoBoothTimerLength = serializer.i32(tweak.photoBoothTimerLength);

        // some 0x13d levels somehow don't serialize this,
        // if a level at that revision crashes,
        // it might be this
        if (version == 0x13d && !serializer.isWriting()) 
            serializer.log("ADD CAMERA TYPE HERE", 1);
        System.exit(1);
        
        // 0x179
        if (version < 0x1d7) 
            tweak.cameraType = serializer.u8(tweak.cameraType);
        else
            tweak.cameraType = serializer.s32(tweak.cameraType);

        if (version > 0x196 && version < 0x2c4)
            tweak.activationLimit = serializer.f32(tweak.activationLimit);

        if (version > 0x1b6 && version < 0x1d2) {
            serializer.f32(0);
            serializer.f32(0);
        }

        if (version > 0x269)
            tweak.requireAll = serializer.bool(tweak.requireAll);

        if (version > 0x2b9)
            tweak.motionControllerZone = serializer.bool(tweak.motionControllerZone);
            
        return tweak;
    }
    
    // TODO: Actually implement
    @Override public int getAllocatedSize() { return 0; }

}
