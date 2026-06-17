package cwlib.structs.things.components;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class RecordingPlayer implements Serializable
{
    public ResourceDescriptor recording;
    public float playHead;
    public byte type;
    public byte dir;
    public Vector3f prevDesiredPosition = new Vector3f();
    public boolean prevDesiredPosSet;
    public Matrix4f startOrientation = new Matrix4f();
    public float speed;
    public boolean pathIsAbsolute;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();

        recording = serializer.resource(recording, ResourceType.THING_RECORDING);
        playHead = serializer.f32(playHead);
        if (version < 0x3c4) serializer.u8(0);
        type = serializer.i8(type);
        dir = serializer.i8(dir);
        prevDesiredPosition = serializer.v3(prevDesiredPosition);
        prevDesiredPosSet = serializer.bool(prevDesiredPosSet);
        startOrientation = serializer.m44(startOrientation);
        speed = serializer.f32(speed);
        if (version > 0x3c4)
            pathIsAbsolute = serializer.bool(pathIsAbsolute);
    }


    // TODO: Actually implement
    @Override
    public int getAllocatedSize()
    {
        return 0;
    }
}
