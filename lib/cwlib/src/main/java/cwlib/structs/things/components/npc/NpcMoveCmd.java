package cwlib.structs.things.components.npc;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class NpcMoveCmd implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int buttons;
    public byte stickX, stickY;

    @Override
    public void serialize(Serializer serializer)
    {
        buttons = serializer.i32(buttons);
        stickX = serializer.i8(stickX);
        stickY = serializer.i8(stickY);
        if (serializer.getRevision().getVersion() < 0x280)
            serializer.u8(0);
    }

    @Override
    public int getAllocatedSize()
    {
        return NpcMoveCmd.BASE_ALLOCATION_SIZE;
    }
}
