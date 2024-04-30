package cwlib.structs.things.components.poppet;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PoppetShapeOverride implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public Vector3f[] polygon;
    public int[] loops;

    @GsonRevision(min = 0x1b6)
    public int back, front;
    @GsonRevision(min = 0x1b6)
    public float scale, angle;

    @GsonRevision(min = 0x318)
    public Matrix4f worldMatrix;

    @Override
    public void serialize(Serializer serializer)
    {
        if (!serializer.isWriting()) polygon = new Vector3f[serializer.getInput().i32()];
        else
        {
            if (polygon == null)
                polygon = new Vector3f[0];
            serializer.getOutput().i32(polygon.length);
        }
        for (int i = 0; i < polygon.length; ++i)
            polygon[i] = serializer.v3(polygon[i]);
        loops = serializer.intvector(loops);
        back = serializer.s32(back);
        front = serializer.s32(front);
        scale = serializer.f32(scale);
        angle = serializer.f32(angle);
        if (serializer.getRevision().getVersion() > 0x317)
            worldMatrix = serializer.m44(worldMatrix);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PoppetShapeOverride.BASE_ALLOCATION_SIZE;
        if (this.polygon != null)
            size += (this.polygon.length * 0xC);
        if (this.loops != null)
            size += (this.loops.length * 0x4);
        return size;
    }
}
