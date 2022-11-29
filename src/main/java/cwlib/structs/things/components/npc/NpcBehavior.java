package cwlib.structs.things.components.npc;

import org.joml.Vector3f;

import cwlib.enums.Branch;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;

public class NpcBehavior implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public Thing npc, targetThing;
    public int type, attributes;
    public float maxMoveSpeed;
    public int maxWaitTime;

    @GsonRevision(min=0x2e6)
    public String waypointKeyName, poiKeyName;

    @GsonRevision(min=0x2d5)
    public int waypointKeyColorIndex;

    @GsonRevision(min=0x2e6)
    public int poiKeyColorIndex;

    @GsonRevision(min=0x296)
    public ActingData actingData;

    @GsonRevision(min=0x2ad)
    public float awarenessRadius;

    @GsonRevision(min=0x2ad)
    public int sharedStateTimer;

    @GsonRevision(min=0x2ad)
    public Vector3f idleLookAtPos;
    
    @GsonRevision(min=0x2d8)
    public Vector3f lastGoodPosition;

    @GsonRevision(min=0x2d9)
    public boolean lastPositionValid;

    @GsonRevision(min=0x2cf)
    public Vector3f patrolDirection;

    @GsonRevision(min=0x2cf)
    public int lastPatrolGridX, lastPatrolGridZ;

    @GsonRevision(min=0x2cf)
    public int targetPatrolGridX, targetPatrolGridZ;

    @GsonRevision(min=0x2cf)
    public int gridDirectionX, gridDirectionZ;

    @GsonRevision(min=0x2cf)
    public int patrolStationaryCounter, patrolUnblockedCounter;

    @GsonRevision(min=0x2e7)
    public int animSet;

    @GsonRevision(min=0x372)
    public byte expressionType, expressionLevel;

    @GsonRevision(min=0x376)
    public boolean willRecordAudio;

    @GsonRevision(branch=0x4c44, min=0x17)
    public byte multiJumpLevel; // Vita 

    @GsonRevision(lbp3=true, min=0xc7)
    public int awarenessRange;

    @GsonRevision(lbp3=true, min=0x10f)
    public float lookAtSpeed;

    @GsonRevision(lbp3=true, min=0x176)
    public boolean showAdvancedOptions;



    @SuppressWarnings("unchecked")
    @Override public NpcBehavior serialize(Serializer serializer, Serializable structure) {
        NpcBehavior behavior = (structure == null) ? new NpcBehavior() : (NpcBehavior) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version <= 0x293) return behavior;

        behavior.npc = serializer.thing(behavior.npc);
        behavior.targetThing = serializer.thing(behavior.targetThing);
        // ENpcBehaviourType
        // 0 = PATROL
        // 1 = FOLLOW
        // 2 = FLEE
        // 3 = IDLE
        // 4 = ACT
        // 5 = WAYPOINT
        behavior.type = serializer.s32(behavior.type);

        behavior.attributes = serializer.i32(behavior.attributes);
        // ATTRIBUTES
        // 0x0 = NONE
        // 0x1 = CAN_CHANGE_HEIGHT
        // 0x2 = CAN_JUMP
        // 0x4 = CAN_CHANGE_LAYER
        // 0x8 = UNUSED
        // 0x10 = HOSTILE
        
        behavior.maxMoveSpeed = serializer.f32(behavior.maxMoveSpeed);
        behavior.maxWaitTime = serializer.s32(behavior.maxWaitTime);

        if (version < 0x2cf) serializer.s32(0);

        if (version > 0x2e5)
            behavior.waypointKeyName = serializer.wstr(behavior.waypointKeyName);
        
        if (version > 0x2d4)
            behavior.waypointKeyColorIndex = serializer.s32(behavior.waypointKeyColorIndex);
        
        if (version > 0x2e5) {
            behavior.poiKeyName = serializer.wstr(behavior.poiKeyName);
            behavior.poiKeyColorIndex = serializer.s32(behavior.poiKeyColorIndex);
        }

        if (version > 0x295)
            behavior.actingData = serializer.reference(behavior.actingData, ActingData.class);

        if (version > 0x2ac) {
            behavior.awarenessRadius = serializer.f32(behavior.awarenessRadius);
            behavior.sharedStateTimer = serializer.i32(behavior.sharedStateTimer);
            behavior.idleLookAtPos = serializer.v3(behavior.idleLookAtPos);
        }

        if (version > 0x2d7)
            behavior.lastGoodPosition = serializer.v3(behavior.lastGoodPosition);
        if (version > 0x2d8)
            behavior.lastPositionValid = serializer.bool(behavior.lastPositionValid);
        if (version > 0x2ce) {
            behavior.patrolDirection = serializer.v3(behavior.patrolDirection);
            behavior.lastPatrolGridX = serializer.s32(behavior.lastPatrolGridX);
            behavior.lastPatrolGridZ = serializer.s32(behavior.lastPatrolGridZ);
            behavior.targetPatrolGridX = serializer.s32(behavior.targetPatrolGridX);
            behavior.targetPatrolGridZ = serializer.s32(behavior.targetPatrolGridZ);
            behavior.gridDirectionX = serializer.s32(behavior.gridDirectionX);
            behavior.gridDirectionZ = serializer.s32(behavior.gridDirectionZ);
            behavior.patrolStationaryCounter = serializer.s32(behavior.patrolStationaryCounter);
            behavior.patrolUnblockedCounter = serializer.s32(behavior.patrolUnblockedCounter);
        }

        if (version > 0x2e6)
            behavior.animSet = serializer.s32(behavior.animSet);
        if (version > 0x371) {
            behavior.expressionType = serializer.i8(behavior.expressionType);
            behavior.expressionLevel = serializer.i8(behavior.expressionLevel);
        }

        if (version > 0x375)
            behavior.willRecordAudio = serializer.bool(behavior.willRecordAudio);
        
        if (revision.has(Branch.DOUBLE11, 0x16)) // 0x3d4
            behavior.multiJumpLevel = serializer.i8(behavior.multiJumpLevel);

        if (subVersion > 0xc6)
            behavior.awarenessRange = serializer.i32(behavior.awarenessRange);
        if (subVersion > 0x10e)
            behavior.lookAtSpeed = serializer.f32(behavior.lookAtSpeed);
        if (subVersion > 0x175)
            behavior.showAdvancedOptions = serializer.bool(behavior.showAdvancedOptions);
        
        return behavior;
    }

    @Override public int getAllocatedSize() {
        int size = NpcBehavior.BASE_ALLOCATION_SIZE;
        if (this.waypointKeyName != null) size += (this.waypointKeyName.length() * 0x2);
        if (this.poiKeyName != null) size += (this.poiKeyName.length() * 0x2);
        if (this.actingData != null) size += this.actingData.getAllocatedSize();
        return size;
    }
}
