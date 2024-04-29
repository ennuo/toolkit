package cwlib.structs.things.components.npc;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class NpcJumpSolver implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    @GsonRevision(min = 0x2ce)
    public boolean isCurrentJumpFlipped;

    @GsonRevision(min = 0x2ce)
    public Vector3f curSource, curTarget0, curTarget1;

    @GsonRevision(min = 0x2ce)
    public int currentJump, currentJumpPos;

    @GsonRevision(max = 0x2c6)
    @Deprecated
    public NpcJumpData[] jumpData;

    @GsonRevision(max = 0x2c6)
    @Deprecated
    public float maxEffectiveJumpHeight;

    @GsonRevision(max = 0x2c6)
    @Deprecated
    public boolean trained;

    @GsonRevision(max = 0x2c6)
    @Deprecated
    public NpcJumpData standingJump;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        if (version < 0x2c7)
        {
            jumpData = serializer.array(jumpData, NpcJumpData.class);
            maxEffectiveJumpHeight = serializer.f32(maxEffectiveJumpHeight);
            trained = serializer.bool(trained);
            standingJump = serializer.struct(standingJump, NpcJumpData.class);
        }

        if (version > 0x2cd)
        {
            isCurrentJumpFlipped = serializer.bool(isCurrentJumpFlipped);
            curSource = serializer.v3(curSource);
            curTarget0 = serializer.v3(curTarget0);
            curTarget1 = serializer.v3(curTarget1);
            currentJump = serializer.s32(currentJump);
            currentJumpPos = serializer.i32(currentJumpPos);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = NpcJumpSolver.BASE_ALLOCATION_SIZE;
        if (this.jumpData != null)
            size += (this.jumpData.length * NpcJumpData.BASE_ALLOCATION_SIZE);
        return size;
    }
}
