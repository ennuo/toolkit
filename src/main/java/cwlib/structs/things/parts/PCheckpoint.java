package cwlib.structs.things.parts;

import cwlib.enums.Branch;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class PCheckpoint implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public byte activeFlags;
    public int activationFrame;

    @GsonRevision(min=0x1a7)
    public int spawnsLeft;
    @GsonRevision(min=0x1c6)
    public int maxSpawnsLeft;
    @GsonRevision(min=0x1eb)
    public boolean instanceInfiniteSpawns;

    @GsonRevision(min=0x1f3)
    public Thing[] spawningList;
    @GsonRevision(min=0x1f3)
    public int spawningDelay;
    @GsonRevision(min=0x1fa)
    public int lifeMultiplier;

    @GsonRevision(min=0x2ae)
    public int teamFilter;

    @GsonRevision(lbp3=true,min=0x88)
    public int creatureToSpawnAs;
    @GsonRevision(lbp3=true,min=0xcb)
    public boolean isStartPoint;
    @GsonRevision(lbp3=true,min=0xd5)
    public boolean linkVisibleOnPlanet;
    @GsonRevision(lbp3=true,min=0xcb)
    public String name;

    @GsonRevision(lbp3=true,min=0xe4)
    public int unk1;
    @GsonRevision(lbp3=true,min=0xd1)
    public String unk2;
    
    @GsonRevision(lbp3=true,min=0x101)
    public int checkpointType, teamFlags;

    @GsonRevision(branch=0x4431,min=80)
    @GsonRevision(lbp3=true,min=0x15a)
    public boolean enableAudio;

    @GsonRevision(lbp3=true,min=0x191)
    public boolean continueMusic;

    @GsonRevision(lbp3=true,min=0x199)
    public int creatureToChangeBackTo;
    @GsonRevision(lbp3=true,min=0x199)
    public boolean changeBackGate;
    
    @GsonRevision(lbp3=true,min=0x19b)
    public boolean persistLives;

    @SuppressWarnings("unchecked")
    @Override public PCheckpoint serialize(Serializer serializer, Serializable structure) {
        PCheckpoint checkpoint = (structure == null) ? new PCheckpoint() : (PCheckpoint) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();
        
        if (subVersion > 0x101) checkpoint.activeFlags = serializer.i8(checkpoint.activeFlags);
        else {
            if (serializer.isWriting()) 
                serializer.getOutput().bool((checkpoint.activeFlags & 0xf) != 0);
            else 
                checkpoint.activeFlags = serializer.getInput().bool() ? (0xf) : ((byte) 0);
        }

        checkpoint.activationFrame = serializer.i32(checkpoint.activationFrame);

        if (version < 0x1f3) {
            serializer.reference(null, Thing.class);
            serializer.i32(0);
        }

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

        if (subVersion >= 0x1 && subVersion < 0x127)
            serializer.u8(0); // persistPoint

        if (subVersion >= 0x88) {

            if (subVersion <= 0x12a) {
                ResourceDescriptor descriptor = null;
                if (serializer.isWriting()) {
                    if (checkpoint.creatureToSpawnAs != 0)
                        descriptor = new ResourceDescriptor(checkpoint.creatureToChangeBackTo, ResourceType.PLAN);
                }
                descriptor = serializer.resource(descriptor, ResourceType.PLAN);
                if (!serializer.isWriting()) {
                    if (descriptor != null && descriptor.isGUID())
                        checkpoint.creatureToChangeBackTo = (int) descriptor.getGUID().getValue();
                }
            }

            if (subVersion >= 0xc5)
                checkpoint.creatureToSpawnAs = serializer.s32(checkpoint.creatureToSpawnAs);
        }
        if (subVersion >= 0xcb)
            checkpoint.isStartPoint = serializer.bool(checkpoint.isStartPoint);
        if (subVersion >= 0xd5)
            checkpoint.linkVisibleOnPlanet = serializer.bool(checkpoint.linkVisibleOnPlanet);

        if (subVersion >= 0x108 && subVersion < 0x129) 
            serializer.u8(0); 

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
