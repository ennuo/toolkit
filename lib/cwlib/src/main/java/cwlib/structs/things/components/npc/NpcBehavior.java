package cwlib.structs.things.components.npc;

import org.joml.Vector3f;

import cwlib.enums.Branch;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;

public class NpcBehavior implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public Thing npc, targetThing;
    public int type, attributes;
    public float maxMoveSpeed;
    public int maxWaitTime;

    @GsonRevision(min = 0x2e6)
    public String waypointKeyName, poiKeyName;

    @GsonRevision(min = 0x2d5)
    public int waypointKeyColorIndex;

    @GsonRevision(min = 0x2e6)
    public int poiKeyColorIndex;

    @GsonRevision(min = 0x296)
    public ActingData actingData;

    @GsonRevision(min = 0x2ad)
    public float awarenessRadius;

    @GsonRevision(min = 0x2ad)
    public int sharedStateTimer;

    @GsonRevision(min = 0x2ad)
    public Vector3f idleLookAtPos;

    @GsonRevision(min = 0x2d8)
    public Vector3f lastGoodPosition;

    @GsonRevision(min = 0x2d9)
    public boolean lastPositionValid;

    @GsonRevision(min = 0x2cf)
    public Vector3f patrolDirection;

    @GsonRevision(min = 0x2cf)
    public int lastPatrolGridX, lastPatrolGridZ;

    @GsonRevision(min = 0x2cf)
    public int targetPatrolGridX, targetPatrolGridZ;

    @GsonRevision(min = 0x2cf)
    public int gridDirectionX, gridDirectionZ;

    @GsonRevision(min = 0x2cf)
    public int patrolStationaryCounter, patrolUnblockedCounter;

    @GsonRevision(min = 0x2e7)
    public int animSet;

    @GsonRevision(min = 0x372)
    public byte expressionType, expressionLevel;

    @GsonRevision(min = 0x376)
    public boolean willRecordAudio;

    @GsonRevision(branch = 0x4c44, min = 0x17)
    public byte multiJumpLevel; // Vita

    @GsonRevision(lbp3 = true, min = 0xc7)
    public int awarenessRange;

    @GsonRevision(lbp3 = true, min = 0x10f)
    public float lookAtSpeed;

    @GsonRevision(lbp3 = true, min = 0x176)
    public boolean showAdvancedOptions;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version <= 0x293) return;

        npc = serializer.thing(npc);
        targetThing = serializer.thing(targetThing);
        // ENpcBehaviourType
        // 0 = PATROL
        // 1 = FOLLOW
        // 2 = FLEE
        // 3 = IDLE
        // 4 = ACT
        // 5 = WAYPOINT
        type = serializer.s32(type);

        attributes = serializer.i32(attributes);
        // ATTRIBUTES
        // 0x0 = NONE
        // 0x1 = CAN_CHANGE_HEIGHT
        // 0x2 = CAN_JUMP
        // 0x4 = CAN_CHANGE_LAYER
        // 0x8 = UNUSED
        // 0x10 = HOSTILE

        maxMoveSpeed = serializer.f32(maxMoveSpeed);
        maxWaitTime = serializer.s32(maxWaitTime);

        if (version < 0x2cf) serializer.s32(0);

        if (version > 0x2e5)
            waypointKeyName = serializer.wstr(waypointKeyName);

        if (version > 0x2d4)
            waypointKeyColorIndex = serializer.s32(waypointKeyColorIndex);

        if (version > 0x2e5)
        {
            poiKeyName = serializer.wstr(poiKeyName);
            poiKeyColorIndex = serializer.s32(poiKeyColorIndex);
        }

        if (version > 0x295)
            actingData = serializer.reference(actingData, ActingData.class);

        if (version > 0x2ac)
        {
            awarenessRadius = serializer.f32(awarenessRadius);
            sharedStateTimer = serializer.i32(sharedStateTimer);
            idleLookAtPos = serializer.v3(idleLookAtPos);
        }

        if (version > 0x2d7)
            lastGoodPosition = serializer.v3(lastGoodPosition);
        if (version > 0x2d8)
            lastPositionValid = serializer.bool(lastPositionValid);
        if (version > 0x2ce)
        {
            patrolDirection = serializer.v3(patrolDirection);
            lastPatrolGridX = serializer.s32(lastPatrolGridX);
            lastPatrolGridZ = serializer.s32(lastPatrolGridZ);
            targetPatrolGridX = serializer.s32(targetPatrolGridX);
            targetPatrolGridZ = serializer.s32(targetPatrolGridZ);
            gridDirectionX = serializer.s32(gridDirectionX);
            gridDirectionZ = serializer.s32(gridDirectionZ);
            patrolStationaryCounter = serializer.s32(patrolStationaryCounter);
            patrolUnblockedCounter = serializer.s32(patrolUnblockedCounter);
        }

        if (version > 0x2e6)
            animSet = serializer.s32(animSet);
        if (version > 0x371)
        {
            expressionType = serializer.i8(expressionType);
            expressionLevel = serializer.i8(expressionLevel);
        }

        if (version > 0x375)
            willRecordAudio = serializer.bool(willRecordAudio);

        if (revision.has(Branch.DOUBLE11, 0x16)) // 0x3d4
            multiJumpLevel = serializer.i8(multiJumpLevel);

        if (subVersion > 0xc6)
            awarenessRange = serializer.i32(awarenessRange);
        if (subVersion > 0x10e)
            lookAtSpeed = serializer.f32(lookAtSpeed);
        if (subVersion > 0x175)
            showAdvancedOptions = serializer.bool(showAdvancedOptions);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = NpcBehavior.BASE_ALLOCATION_SIZE;
        if (this.waypointKeyName != null) size += (this.waypointKeyName.length() * 0x2);
        if (this.poiKeyName != null) size += (this.poiKeyName.length() * 0x2);
        if (this.actingData != null) size += this.actingData.getAllocatedSize();
        return size;
    }
}
