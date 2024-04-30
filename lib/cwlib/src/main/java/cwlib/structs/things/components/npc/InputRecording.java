package cwlib.structs.things.components.npc;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import cwlib.enums.Branch;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class InputRecording implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public byte[] inputBuffer;
    public int[] offsetBuffer;
    public int[] absoluteExpressionBuffer;

    @GsonRevision(min = 0x2de)
    public Matrix4f startWorldTransform;

    @GsonRevision(min = 0x2de)
    public Matrix4f[] startLocalSceneGraph;

    @GsonRevision(min = 0x2e0)
    public Vector3f startFootPos;

    @GsonRevision(min = 0x2e1)
    public Vector3f[] startVelocities;

    @GsonRevision(min = 0x3c5)
    public boolean recordingContainsMoveData;

    @GsonRevision(branch = 0x4c44, min = 0x3a)
    public boolean recordingContainsVitaData; // Vita, perhaps obviously

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        if (version > 0x28f)
            inputBuffer = serializer.bytearray(inputBuffer);
        if (version > 0x28f)
            offsetBuffer = serializer.intvector(offsetBuffer);
        if (version > 0x2a5)
            absoluteExpressionBuffer =
                serializer.intvector(absoluteExpressionBuffer);

        if (version > 0x2dd)
        {
            startWorldTransform = serializer.m44(startWorldTransform);
            if (!serializer.isWriting())
                startLocalSceneGraph = new Matrix4f[serializer.getInput().i32()];
            else
            {
                if (startLocalSceneGraph == null)
                    startLocalSceneGraph = new Matrix4f[0];
                serializer.getOutput().i32(startLocalSceneGraph.length);
            }
            for (int i = 0; i < startLocalSceneGraph.length; ++i)
                startLocalSceneGraph[i] =
                    serializer.m44(startLocalSceneGraph[i]);
        }

        if (version > 0x2df)
            startFootPos = serializer.v3(startFootPos);

        if (version > 0x2e0)
        {
            if (!serializer.isWriting())
                startVelocities = new Vector3f[serializer.getInput().i32()];
            else
            {
                if (startVelocities == null)
                    startVelocities = new Vector3f[0];
                serializer.getOutput().i32(startVelocities.length);
            }
            for (int i = 0; i < startVelocities.length; ++i)
                startVelocities[i] = serializer.v3(startVelocities[i]);
        }

        if (version > 0x3c4)
            recordingContainsMoveData =
                serializer.bool(recordingContainsMoveData);

        if (serializer.getRevision().has(Branch.DOUBLE11, 0x3a))
            recordingContainsVitaData =
                serializer.bool(recordingContainsVitaData);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = InputRecording.BASE_ALLOCATION_SIZE;
        if (this.inputBuffer != null) size += (this.inputBuffer.length);
        if (this.offsetBuffer != null) size += (this.offsetBuffer.length * 0x4);
        if (this.absoluteExpressionBuffer != null)
            size += (this.absoluteExpressionBuffer.length * 0x4);
        if (this.startLocalSceneGraph != null)
            size += (this.startLocalSceneGraph.length * 0x40);
        if (this.startVelocities != null) size += (this.startVelocities.length * 0x10);
        return size;
    }
}
