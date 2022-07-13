package cwlib.structs.things.parts;

import org.joml.Vector3f;

import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
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
    public NpcBehavior recordingBehavior;
    public int flags;
    public Thing behaviorThing, rootBehaviorThing;
    public Vector3f moveTarget;
    public int waitTime, playerNumber;
    public String actorName;
    public int lastTimeThrown, lastTimeHitTheGround, lastThrower;
    public byte costumeToCopy;

    @SuppressWarnings("unchecked")
    @Override public PNpc serialize(Serializer serializer, Serializable structure) {
        PNpc npc = (structure == null) ? new PNpc() : (PNpc) structure;
        
        int version = serializer.getRevision().getVersion();
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

        npc.soundRecording = serializer.bytearray(npc.soundRecording);
        npc.soundRecordingDataNbytes = serializer.intvector(npc.soundRecordingDataNbytes);
        npc.soundRecordingPacket = serializer.i32(npc.soundRecordingPacket);
        npc.soundRecordingPacketOffset = serializer.i32(npc.soundRecordingPacketOffset);
        npc.sackbotRecordingTimes = serializer.intvector(npc.sackbotRecordingTimes);

        if (version > 0x286 && version < 0x293) {
            throw new SerializationException("Serialization of CBehaviourFollow and CBehaviourAct are not supported!");
            // CBehaviourFollow
            // CBehaviourAct
        }

        if (version > 0x2da || (version < 0x29b && version > 0x294))
            npc.recordingBehavior = serializer.reference(npc.recordingBehavior, NpcBehavior.class);

        if (version > 0x2ac) {
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
        
        return npc;
    }

    @Override public int getAllocatedSize() {
        int size = PNpc.BASE_ALLOCATION_SIZE;
        return size;
    }
}
