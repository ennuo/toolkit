package cwlib.structs.things.components.npc;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class NpcJumpData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public float a, b, c;
    public Vector3f min, max;

    @GsonRevision(min=0x273)
    public boolean flipped;

    @GsonRevision(min=0x273)
    public NpcMoveCmd[] commandList;

    @GsonRevision(min=0x273)
    public Vector3f apex;

    @SuppressWarnings("unchecked")
    @Override public NpcJumpData serialize(Serializer serializer, Serializable structure) {
        NpcJumpData data = (structure == null) ? new NpcJumpData() : (NpcJumpData) structure;

        int version = serializer.getRevision().getVersion();

        data.a = serializer.f32(data.a);
        data.b = serializer.f32(data.b);
        data.c = serializer.f32(data.c);

        data.min = serializer.v3(data.min);
        data.max = serializer.v3(data.max);

        if (version > 0x272) {
            data.flipped = serializer.bool(data.flipped);
            data.commandList = serializer.array(data.commandList, NpcMoveCmd.class);
            data.apex = serializer.v3(data.apex);
        }

        return data;
    }

    @Override public int getAllocatedSize() {
        int size = NpcJumpData.BASE_ALLOCATION_SIZE;
        if (this.commandList != null)
            size += (this.commandList.length * NpcMoveCmd.BASE_ALLOCATION_SIZE);
        return size;
    }
}
