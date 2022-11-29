package cwlib.structs.things.components.npc;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import cwlib.enums.Branch;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class InputRecording implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public byte[] inputBuffer;
    public int[] offsetBuffer;
    public int[] absoluteExpressionBuffer;

    @GsonRevision(min=0x2de)
    public Matrix4f startWorldTransform;

    @GsonRevision(min=0x2de)
    public Matrix4f[] startLocalSceneGraph;

    @GsonRevision(min=0x2e0)
    public Vector3f startFootPos;

    @GsonRevision(min=0x2e1)
    public Vector3f[] startVelocities;

    @GsonRevision(min=0x3c5)
    public boolean recordingContainsMoveData;

    @GsonRevision(branch=0x4c44, min=0x3a)
    public boolean recordingContainsVitaData; // Vita, perhaps obviously

    @SuppressWarnings("unchecked")
    @Override public InputRecording serialize(Serializer serializer, Serializable structure) {
        InputRecording recording = (structure == null) ? new InputRecording() : (InputRecording) structure;
        
        int version = serializer.getRevision().getVersion();

        if (version > 0x28f)
            recording.inputBuffer = serializer.bytearray(recording.inputBuffer);
        if (version > 0x28f)
            recording.offsetBuffer = serializer.intvector(recording.offsetBuffer);
        if (version > 0x2a5)
            recording.absoluteExpressionBuffer = serializer.intvector(recording.absoluteExpressionBuffer);

        if (version > 0x2dd) {
            recording.startWorldTransform = serializer.m44(recording.startWorldTransform);
            if (!serializer.isWriting()) recording.startLocalSceneGraph = new Matrix4f[serializer.getInput().i32()];
            else {
                if (recording.startLocalSceneGraph == null)
                    recording.startLocalSceneGraph = new Matrix4f[0];
                serializer.getOutput().i32(recording.startLocalSceneGraph.length);
            }
            for (int i = 0; i < recording.startLocalSceneGraph.length; ++i)
                recording.startLocalSceneGraph[i] = serializer.m44(recording.startLocalSceneGraph[i]);
        }

        if (version > 0x2df)
            recording.startFootPos = serializer.v3(recording.startFootPos);
        
        if (version > 0x2e0) {
            if (!serializer.isWriting()) recording.startVelocities = new Vector3f[serializer.getInput().i32()];
            else {
                if (recording.startVelocities == null)
                    recording.startVelocities = new Vector3f[0];
                serializer.getOutput().i32(recording.startVelocities.length);
            }
            for (int i = 0; i < recording.startVelocities.length; ++i)
                recording.startVelocities[i] = serializer.v3(recording.startVelocities[i]);
        }

        if (version > 0x3c4)
            recording.recordingContainsMoveData = serializer.bool(recording.recordingContainsMoveData);

        if (serializer.getRevision().has(Branch.DOUBLE11, 0x3a))
            recording.recordingContainsVitaData = serializer.bool(recording.recordingContainsVitaData);
        
        return recording;
    }

    @Override public int getAllocatedSize() {
        int size = InputRecording.BASE_ALLOCATION_SIZE;
        if (this.inputBuffer != null) size += (this.inputBuffer.length);
        if (this.offsetBuffer != null) size += (this.offsetBuffer.length * 0x4);
        if (this.absoluteExpressionBuffer != null) size += (this.absoluteExpressionBuffer.length * 0x4);
        if (this.startLocalSceneGraph != null) size += (this.startLocalSceneGraph.length * 0x40);
        if (this.startVelocities != null) size += (this.startVelocities.length * 0x10);
        return size;
    }
}
