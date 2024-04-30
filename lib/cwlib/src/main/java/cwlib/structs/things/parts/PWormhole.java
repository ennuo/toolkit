package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PWormhole implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public int type;

    @GsonRevision(min = 0x111, lbp3 = true)
    public byte activeTypeForTwoWayHole;

    public int playerMode;
    public boolean audioEnabled, trigger, finished, activated;
    public int exitCount, exitDelay;

    @Override
    public void serialize(Serializer serializer)
    {
        type = serializer.s32(type);
        if (serializer.getRevision().getSubVersion() >= 0x111)
            activeTypeForTwoWayHole = serializer.i8(activeTypeForTwoWayHole);

        playerMode = serializer.s32(playerMode);
        audioEnabled = serializer.bool(audioEnabled);

        trigger = serializer.bool(trigger);
        finished = serializer.bool(finished);
        activated = serializer.bool(activated);

        exitCount = serializer.i32(exitCount);
        exitDelay = serializer.i32(exitDelay);
    }

    @Override
    public int getAllocatedSize()
    {
        return PWormhole.BASE_ALLOCATION_SIZE;
    }
}
