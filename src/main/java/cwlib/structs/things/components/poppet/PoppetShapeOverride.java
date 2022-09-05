package cwlib.structs.things.components.poppet;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PoppetShapeOverride implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public Vector3f[] polygon;
    public int[] loops;

    @GsonRevision(min=0x1b6)
    public int back, front;
    @GsonRevision(min=0x1b6)
    public float scale, angle;

    @GsonRevision(min=0x318)
    public Matrix4f worldMatrix;

    @SuppressWarnings("unchecked")
    @Override public PoppetShapeOverride serialize(Serializer serializer, Serializable structure) {
        PoppetShapeOverride override = 
            (structure == null) ? new PoppetShapeOverride() : (PoppetShapeOverride) structure;

        if (!serializer.isWriting()) override.polygon = new Vector3f[serializer.getInput().i32()];
        else {
            if (override.polygon == null)
                override.polygon = new Vector3f[0];
            serializer.getOutput().i32(override.polygon.length);
        }
        for (int i = 0; i < override.polygon.length; ++i)
            override.polygon[i] = serializer.v3(override.polygon[i]);
        override.loops = serializer.intvector(override.loops);
        override.back = serializer.s32(override.back);
        override.front = serializer.s32(override.front);
        override.scale = serializer.f32(override.scale);
        override.angle = serializer.f32(override.angle);
        if (serializer.getRevision().getVersion() > 0x317)
            override.worldMatrix = serializer.m44(override.worldMatrix);

        return override;
    }

    @Override public int getAllocatedSize() {
        int size = PoppetShapeOverride.BASE_ALLOCATION_SIZE;
        if (this.polygon != null)
            size += (this.polygon.length * 0xC);
        if (this.loops != null)
            size += (this.loops.length * 0x4);
        return size;
    }
}
