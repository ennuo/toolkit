package cwlib.structs.things.parts;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;

public class PCheckpoint implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public byte activeFlags;
    public int activationFrame;
    public int spawnsLeft, maxSpawnsLeft;
    public boolean instanceInfiniteSpawns;
    public Thing[] spawningList;
    public int spawningDelay;
    public int lifeMultiplier;

    public int teamFilter;

    public int creatureToSpawnAs;
    public boolean isStartPoint;
    public boolean linkVisibleOnPlanet;
    public String name;

    public int unk1;
    public String unk2;
    
    public int checkpointType, teamFlags;
    public boolean enableAudio, continueMusic;
    public int creatureToChangeBackTo;
    public boolean changeBackGate, persistLives;

    @SuppressWarnings("unchecked")
    @Override public PCheckpoint serialize(Serializer serializer, Serializable structure) {
        PCheckpoint checkpoint = (structure == null) ? new PCheckpoint() : (PCheckpoint) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version < 0x1f3)
            throw new SerializationException("PCheckpoint revisions below r499 are not supported!");
        
        if (subVersion < 0x102) checkpoint.activeFlags = serializer.i8(checkpoint.activeFlags);
        else {
            if (serializer.isWriting()) 
                serializer.getOutput().bool((checkpoint.activeFlags & 0xf) != 0);
            else 
                checkpoint.activeFlags = serializer.getInput().bool() ? (0xf) : ((byte) 0);
        }

        checkpoint.activationFrame = serializer.i32(checkpoint.activationFrame);

        if (version >= 0x1a7)
            checkpoint.spawnsLeft = serializer.s32(checkpoint.spawnsLeft);
        if (version >= 0x1c6)
            checkpoint.maxSpawnsLeft = serializer.s32(checkpoint.maxSpawnsLeft);

        if (version >= 0x1eb)
            checkpoint.instanceInfiniteSpawns = serializer.bool(checkpoint.instanceInfiniteSpawns);
        
        if (version >= 0x1f3) {
            checkpoint.spawningList = serializer.array(checkpoint.spawningList, Thing.class, true);
            checkpoint.spawningDelay = serializer.i32(checkpoint.spawningDelay);
        }
        
        if (version >= 0x1fa)
            checkpoint.lifeMultiplier = serializer.i32(checkpoint.lifeMultiplier);

        if (version >= 0x2ae && subVersion < 0x100)
            checkpoint.teamFilter = serializer.i32(checkpoint.teamFilter);

        // some removed boolean/byte (head - 0x20000 < 0x1250000)

        if (subVersion >= 0x88)
            checkpoint.creatureToSpawnAs = serializer.i32(checkpoint.creatureToSpawnAs);
        if (subVersion >= 0xcb)
            checkpoint.isStartPoint = serializer.bool(checkpoint.isStartPoint);
        if (subVersion >= 0xd5)
            checkpoint.linkVisibleOnPlanet = serializer.bool(checkpoint.linkVisibleOnPlanet);

        // some removed boolean/byte (head + 0xfef80000 < 0x210000) ??? ghidra actin funny

        if (subVersion >= 0xcb)
            checkpoint.name = serializer.wstr(checkpoint.name);
        if (subVersion >= 0xe4) 
            checkpoint.unk1 = serializer.i32(checkpoint.unk1);
        if (subVersion >= 0xd1)
            checkpoint.unk2 = serializer.wstr(checkpoint.unk2);

        if (subVersion >= 0x101) {
            checkpoint.checkpointType = serializer.i32(checkpoint.checkpointType);
            checkpoint.teamFlags = serializer.i32(checkpoint.teamFlags);
        }

        if (revision.has(Branch.DOUBLE11, Revisions.D1_CHECKPOINT_PLAY_AUDIO) || subVersion >= 0x15a)
            checkpoint.enableAudio = serializer.bool(checkpoint.enableAudio);

        if (subVersion >= 0x191)
            checkpoint.continueMusic = serializer.bool(checkpoint.continueMusic);

        if (subVersion >= 0x199) {
            checkpoint.creatureToChangeBackTo = serializer.i32(checkpoint.creatureToChangeBackTo);
            checkpoint.changeBackGate = serializer.bool(checkpoint.changeBackGate);
        }
        
        if (subVersion >= 0x19b)
            checkpoint.persistLives = serializer.bool(checkpoint.persistLives);
        
        return checkpoint;
    }

    @Override public int getAllocatedSize() {
        int size = PCheckpoint.BASE_ALLOCATION_SIZE;
        if (this.spawningList != null) size += (this.spawningList.length * 4);
        if (this.name != null) size += (this.name.length() * 2);
        if (this.unk2 != null) size += (this.unk2.length() * 2);
        return size;
    }
}
