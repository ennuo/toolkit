package cwlib.structs.things.components.npc;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class NpcMoveCmd implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int buttons;
    public byte stickX, stickY;

    @SuppressWarnings("unchecked")
    @Override public NpcMoveCmd serialize(Serializer serializer, Serializable structure) {
        NpcMoveCmd cmd = (structure == null) ? new NpcMoveCmd() : (NpcMoveCmd) structure;

        cmd.buttons = serializer.i32(cmd.buttons);
        cmd.stickX = serializer.i8(cmd.stickX);
        cmd.stickY = serializer.i8(cmd.stickY);
        if (serializer.getRevision().getVersion() < 0x280)
            serializer.u8(0);
        
        return cmd;
    }
    
    @Override public int getAllocatedSize() { return NpcMoveCmd.BASE_ALLOCATION_SIZE; }
}
