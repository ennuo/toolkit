package cwlib.structs.things.parts;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.npc.BehaviourAct;
import cwlib.structs.things.components.npc.BehaviourFollow;
import cwlib.structs.things.components.npc.Input;
import cwlib.structs.things.components.npc.NpcBehavior;
import cwlib.structs.things.components.npc.NpcJumpSolver;

public class PNpc implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    public NpcJumpSolver jumpSolver = new NpcJumpSolver();
    public byte[] soundRecording;
    public int[] soundRecordingDataNbytes;
    public int soundRecordingPacket;
    public int soundRecordingPacketOffset;
    public int[] sackbotRecordingTimes;

    @GsonRevision(min=0x287, max=0x292)
    @Deprecated public BehaviourFollow follow;

    @GsonRevision(min=0x287, max=0x292)
    @Deprecated public BehaviourAct act;


    @GsonRevision(min=0x2db)
    @GsonRevision(min=0x295, max=0x29b)
    public NpcBehavior recordingBehavior;

    @GsonRevision(min=0x2ad)
    public int flags;

    @GsonRevision(min=0x29b)
    public Thing behaviorThing;
    
    @GsonRevision(min=0x2d6)
    public Thing rootBehaviorThing;

    @GsonRevision(min=0x2ad)
    public Vector3f moveTarget;

    @GsonRevision(min=0x2ad)
    public int waitTime;
    
    @GsonRevision(min=0x2af)
    public int playerNumber;

    @GsonRevision(min=0x339)
    public String actorName;

    @GsonRevision(min=0x354)
    public int lastTimeThrown, lastTimeHitTheGround, lastThrower;

    @GsonRevision(min=0x392)
    public byte costumeToCopy;

    @GsonRevision(lbp3=true, min=0x1a6)
    public boolean copyFormAsWell;

    @SuppressWarnings("unchecked")
    @Override public PNpc serialize(Serializer serializer, Serializable structure) {
        PNpc npc = (structure == null) ? new PNpc() : (PNpc) structure;
        
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        if (version < 0x273) return npc;

        npc.jumpSolver = serializer.struct(npc.jumpSolver, NpcJumpSolver.class);

        if (version < 0x2ac) serializer.bool(false);
        if (version < 0x293) serializer.bool(false);
        if (version < 0x290) {
            serializer.array(null, Input.class);
            serializer.i32(0);
            serializer.bool(false);
            serializer.thing(null);
        }

        if (serializer.getRevision().getSubVersion() < 0x118)
            npc.soundRecording = serializer.bytearray(npc.soundRecording);
        npc.soundRecordingDataNbytes = serializer.intvector(npc.soundRecordingDataNbytes);
        npc.soundRecordingPacket = serializer.i32(npc.soundRecordingPacket);
        npc.soundRecordingPacketOffset = serializer.i32(npc.soundRecordingPacketOffset);
        npc.sackbotRecordingTimes = serializer.intvector(npc.sackbotRecordingTimes);

        if (version > 0x286 && version < 0x293) {
            npc.follow = serializer.reference(npc.follow, BehaviourFollow.class);
            npc.act = serializer.reference(npc.act, BehaviourAct.class);
        }

        if (version > 0x2da || (version < 0x29b && version > 0x294))
            npc.recordingBehavior = serializer.reference(npc.recordingBehavior, NpcBehavior.class);

        if (version > 0x2ac) {
            // ENpcFlags
            // IS_JUMPING 1
            // IS_HOSTILE 2
            // IS_PAUSED 4
            npc.flags = serializer.i32(npc.flags);
            if (version < 0x2ce)
                serializer.s32(0); // jumpBackoff
        }

        if (version > 0x29a)
            npc.behaviorThing = serializer.thing(npc.behaviorThing);
        if (version > 0x2d5)
            npc.rootBehaviorThing = serializer.thing(npc.rootBehaviorThing);

        if (version > 0x2cd && version < 0x36e) {
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
        }

        if (version > 0x2ac) {
            npc.moveTarget = serializer.v3(npc.moveTarget);

            if (version > 0x2ac && version < 0x36e) serializer.v3(null); // lookAt
            if (version > 0x2d6 && version < 0x2e6) serializer.v3(null); 
            if (version < 0x2ce)
                serializer.s32(0); // stuckTime

            npc.waitTime = serializer.s32(npc.waitTime);

            if (version < 0x2ce)
                serializer.f32(0); // expectedVelLen
        }

        if (version > 0x2ae)
            npc.playerNumber = serializer.i32(npc.playerNumber);
        if (version > 0x2aa && version < 0x2d5)
            serializer.i32(0); // color

        if (version > 0x338)
            npc.actorName = serializer.wstr(npc.actorName);
        
        if (version > 0x353) {
            if (version == 0x354 || version == 0x355) {
                serializer.f32(0); // lastTimeThrown
                serializer.f32(0); // lastTimeHitTheGround
            } else {
                npc.lastTimeThrown = serializer.i32(npc.lastTimeThrown);
                npc.lastTimeHitTheGround = serializer.i32(npc.lastTimeHitTheGround);
            }
            npc.lastThrower = serializer.i32(npc.lastThrower);
        }

        if (version > 0x391)
            npc.costumeToCopy = serializer.i8(npc.costumeToCopy);
        if (subVersion > 0x1a5)
            npc.copyFormAsWell = serializer.bool(npc.copyFormAsWell);
        
        return npc;
    }

    @Override public int getAllocatedSize() {
        int size = PNpc.BASE_ALLOCATION_SIZE;
        return size;
    }
}
