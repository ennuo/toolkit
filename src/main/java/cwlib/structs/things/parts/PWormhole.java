package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PWormhole implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public int type;

    @GsonRevision(min=0x111, lbp3=true)
    public byte activeTypeForTwoWayHole;
    
    public int playerMode;
    public boolean audioEnabled, trigger, finished, activated;
    public int exitCount, exitDelay;

    @SuppressWarnings("unchecked")
    @Override public PWormhole serialize(Serializer serializer, Serializable structure) {
        PWormhole wormhole = (structure == null) ? new PWormhole() : (PWormhole) structure;
        
        wormhole.type = serializer.s32(wormhole.type);
        if (serializer.getRevision().getSubVersion() >= 0x111)
            wormhole.activeTypeForTwoWayHole = serializer.i8(wormhole.activeTypeForTwoWayHole);
        
        wormhole.playerMode = serializer.s32(wormhole.playerMode);
        wormhole.audioEnabled = serializer.bool(wormhole.audioEnabled);

        wormhole.trigger = serializer.bool(wormhole.trigger);
        wormhole.finished = serializer.bool(wormhole.finished);
        wormhole.activated = serializer.bool(wormhole.activated);

        wormhole.exitCount = serializer.i32(wormhole.exitCount);
        wormhole.exitDelay = serializer.i32(wormhole.exitDelay);

        return wormhole;
    }

    @Override public int getAllocatedSize() { return PWormhole.BASE_ALLOCATION_SIZE; }
}
