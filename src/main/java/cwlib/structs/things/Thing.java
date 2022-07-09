package cwlib.structs.things;

import java.util.Arrays;

import cwlib.enums.Branch;
import cwlib.enums.Part;
import cwlib.enums.PartHistory;
import cwlib.enums.Revisions;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.parts.PJoint;
import cwlib.types.data.GUID;
import cwlib.types.data.Revision;

/**
 * Represents an object in the game world.
 */
public class Thing implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public int UID = 1;
    public Thing world;
    public Thing parent;
    public Thing groupHead;
    public Thing oldEmitter;

    public short createdBy = -1, changedBy = -1;
    public boolean isStamping;
    public GUID planGUID;
    public boolean hidden;
    public byte flags, extraFlags;

    private Serializable[] parts = new Serializable[0x3f];

    public Thing(){};
    public Thing(int UID) { this.UID = UID; }
    
    @SuppressWarnings("unchecked")
    @Override public Thing serialize(Serializer serializer, Serializable structure) {
        Thing thing = (structure == null) ? new Thing() : (Thing) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        // Test serialization marker.
        if (version >= Revisions.THING_TEST_MARKER || revision.has(Branch.LEERDAMMER, Revisions.LD_TEST_MARKER)) {
            serializer.log("TEST_SERIALISATION_MARKER");
            serializer.u8(0xAA);
        }

        if (version < 0x1fd)
            thing.world = serializer.reference(thing.world, Thing.class);
        if (version < 0x27f) {
            thing.parent = serializer.reference(thing.parent, Thing.class);
            thing.UID = serializer.i32(thing.UID);
        } else {
            thing.UID = serializer.i32(thing.UID);
            thing.parent = serializer.reference(thing.parent, Thing.class);
        }

        thing.groupHead = serializer.reference(thing.groupHead, Thing.class);

        if (version >= 0x1c7)
            thing.oldEmitter = serializer.reference(thing.oldEmitter, Thing.class);

        if (version >= 0x1a6 && version < 0x1bc)
            serializer.array(null, PJoint.class, true);
        
        if (version >= 0x214) {
            thing.createdBy = serializer.i16(thing.createdBy);
            thing.changedBy = serializer.i16(thing.changedBy);
        }

        if (version < 0x341) {
            if (version > 0x21a) 
                thing.isStamping = serializer.bool(thing.isStamping);
            if (version >= 0x254)
                thing.planGUID = serializer.guid(thing.planGUID);
            if (version >= 0x2f2)
                thing.hidden = serializer.bool(thing.hidden);
        } else {
            if (version >= 0x254)
                thing.planGUID = serializer.guid(thing.planGUID);
            if (version >= 0x341)
                thing.flags = serializer.i8(thing.flags);
            if (subVersion >= 0x110)
                thing.extraFlags = serializer.i8(thing.extraFlags);
        }

        boolean isCompressed = (version >= 0x297 || revision.has(Branch.LEERDAMMER, Revisions.LD_RESOURCES));
        
        int partsRevision = PartHistory.STREAMING_HINT;
        long flags = -1;

        if (serializer.isWriting()) {
            serializer.log("GENERATING FLAGS");
            Part lastPart = null;
            if (isCompressed) flags = 0;
            for (Part part : Part.values()) {
                int index = part.getIndex();
                if (version >= 0x13c && (index >= 0x36 && index <= 0x3c)) continue;
                if (version >= 0x18c && index == 0x3d) continue;
                if (subVersion >= 0x107 && index == 0x3e) continue;
                else if (index == 0x3e) {
                    if (this.parts[index] != null) {
                        flags |= (1l << 0x29);
                        lastPart = part;
                    }
                    continue;
                }

                if (this.parts[index] != null) {
                    // Offset due to PCreatorAnim
                    if (subVersion < 0x107 && index > 0x3e) index++; 

                    flags |= (1l << index);

                    lastPart = part;
                }
            }
            partsRevision = (lastPart == null) ? 0 : lastPart.getVersion();
        }

        partsRevision = serializer.s32(partsRevision);
        if (isCompressed)
            flags = serializer.i64(flags);

        // I have no idea why they did this
        if (version == 0x13c) partsRevision += 7;
        
        Part[] parts = Part.fromFlags(revision.getHead(), flags, partsRevision);
        System.out.println(Arrays.toString(parts));

        for (Part part : parts) {
            serializer.log(part.name() + " [START]");
            if (!part.serialize(this.parts, partsRevision, flags, serializer)) {
                serializer.log(part.name() + " FAILED");
                System.exit(0);
                throw new SerializationException(part.name() + " failed to serialize!");
            }
            serializer.log(part.name() + " [END]");
        }
        
        return thing;
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> T getPart(Part part) { return (T) this.parts[part.getIndex()]; }
    public <T extends Serializable> void setPart(Part part, T value) { this.parts[part.getIndex()] = value; }

    @Override public int getAllocatedSize() {  return BASE_ALLOCATION_SIZE;  }
}
