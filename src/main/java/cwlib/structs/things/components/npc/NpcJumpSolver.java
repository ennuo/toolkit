package cwlib.structs.things.components.npc;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class NpcJumpSolver implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public boolean isCurrentJumpFlipped;
    public Vector3f curSource, curTarget0, curTarget1;
    public int currentJump, currentJumpPos;

    @Deprecated public NpcJumpData[] jumpData;
    @Deprecated public float maxEffectiveJumpHeight;
    @Deprecated public boolean trained;
    @Deprecated public NpcJumpData standingJump;

    @SuppressWarnings("unchecked")
    @Override public NpcJumpSolver serialize(Serializer serializer, Serializable structure) {
        NpcJumpSolver solver = (structure == null) ? new NpcJumpSolver() : (NpcJumpSolver) structure;

        int version = serializer.getRevision().getVersion();

        if (version < 0x2c7) {
            solver.jumpData = serializer.array(solver.jumpData, NpcJumpData.class);
            solver.maxEffectiveJumpHeight = serializer.f32(solver.maxEffectiveJumpHeight);
            solver.trained = serializer.bool(solver.trained);
            solver.standingJump = serializer.struct(solver.standingJump, NpcJumpData.class);
        }

        if (version > 0x2cd) {
            solver.isCurrentJumpFlipped = serializer.bool(solver.isCurrentJumpFlipped);
            solver.curSource = serializer.v3(solver.curSource);
            solver.curTarget0 = serializer.v3(solver.curTarget0);
            solver.curTarget1 = serializer.v3(solver.curTarget1);
            solver.currentJump = serializer.s32(solver.currentJump);
            solver.currentJumpPos = serializer.i32(solver.currentJumpPos);
        }

        return solver;
    }

    @Override public int getAllocatedSize() {
        int size = NpcJumpSolver.BASE_ALLOCATION_SIZE;
        if (this.jumpData != null)
            size += (this.jumpData.length * NpcJumpData.BASE_ALLOCATION_SIZE);
        return size;
    }
}
