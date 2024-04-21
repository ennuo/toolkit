package cwlib.structs.things;

import java.util.Arrays;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.Branch;
import cwlib.enums.Part;
import cwlib.enums.PartHistory;
import cwlib.enums.Revisions;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.ThingSerializer;
import cwlib.io.serializer.Serializer;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.things.parts.PJoint;
import cwlib.types.data.GUID;
import cwlib.types.data.Revision;
import cwlib.util.Bytes;

/**
 * Represents an object in the game world.
 */
@JsonAdapter(ThingSerializer.class)
public class Thing implements Serializable
{
      public static boolean SERIALIZE_WORLD_THING = true;

      public static final int BASE_ALLOCATION_SIZE = 0x100;

      public String name;

      public int UID = 1;
      public Thing world;
      public Thing parent;
      public Thing groupHead;
      public Thing oldEmitter;

      public short createdBy = -1, changedBy = -1;
      public boolean isStamping;
      public GUID planGUID;
      public boolean hidden;
      public short flags;
      public byte extraFlags;

      private Serializable[] parts = new Serializable[0x3f];

      public Thing() { }

      ;

      public Thing(int UID)
      {
            this.UID = UID;
      }

      @Override
      public void serialize(Serializer serializer)
      {
            Revision revision = serializer.getRevision();
            int version = revision.getVersion();
            int subVersion = revision.getSubVersion();

            int maxPartsRevision = PartHistory.STREAMING_HINT;
            if (version <= 0x3e2)
                  maxPartsRevision = PartHistory.CONTROLINATOR;
            if (version <= 0x33a)
                  maxPartsRevision = PartHistory.MATERIAL_OVERRIDE;
            if (version <= 0x2c3)
                  maxPartsRevision = PartHistory.MATERIAL_TWEAK;
            if (version <= 0x272)
                  maxPartsRevision = PartHistory.GROUP;

            // Test serialization marker.
            if (revision.has(Branch.MIZUKI, Revisions.MZ_SCENE_GRAPH))
                  name = serializer.wstr(name);
            else if (version >= Revisions.THING_TEST_MARKER || revision.has(Branch.LEERDAMMER,
                    Revisions.LD_TEST_MARKER))
            {
                  serializer.log("TEST_SERIALISATION_MARKER");
                  if (serializer.u8(0xAA) != 0xaa)
                        throw new SerializationException("Test serialization marker is invalid, " +
                                                         "something" +
                                                         " has gone terribly wrong!");
            }

            if (version < 0x1fd)
            {
                  if (serializer.isWriting())
                        serializer.reference(SERIALIZE_WORLD_THING ? world : null, Thing.class);
                  else
                        world = serializer.reference(world, Thing.class);
            }
            if (version < 0x27f)
            {
                  parent = serializer.reference(parent, Thing.class);
                  UID = serializer.i32(UID);
            }
            else
            {
                  UID = serializer.i32(UID);
                  parent = serializer.reference(parent, Thing.class);
            }

            groupHead = serializer.reference(groupHead, Thing.class);

            if (version >= 0x1c7)
                  oldEmitter = serializer.reference(oldEmitter, Thing.class);

            if (version >= 0x1a6 && version < 0x1bc)
                  serializer.array(null, PJoint.class, true);

            if ((version >= 0x214 && !revision.isToolkit()) || revision.before(Branch.MIZUKI,
                    Revisions.MZ_SCENE_GRAPH))
            {
                  createdBy = serializer.i16(createdBy);
                  changedBy = serializer.i16(changedBy);
            }

            if (version < 0x341)
            {
                  if (version > 0x21a)
                        isStamping = serializer.bool(isStamping);
                  if (version >= 0x254)
                        planGUID = serializer.guid(planGUID);
                  if (version >= 0x2f2)
                        hidden = serializer.bool(hidden);
            }
            else
            {
                  if (version >= 0x254)
                        planGUID = serializer.guid(planGUID);

                  if (version >= 0x341)
                  {
                        if (revision.has(Branch.DOUBLE11, 0x62))
                              flags = serializer.i16(flags);
                        else
                              flags = serializer.i8((byte) flags);
                  }
                  if (subVersion >= 0x110)
                        extraFlags = serializer.i8(extraFlags);
            }

            boolean isCompressed = (version >= 0x297 || revision.has(Branch.LEERDAMMER,
                    Revisions.LD_RESOURCES));

            int partsRevision = PartHistory.STREAMING_HINT;
            long flags = -1;

            if (serializer.isWriting())
            {
                  serializer.log("GENERATING FLAGS");
                  Part lastPart = null;
                  if (isCompressed) flags = 0;
                  for (Part part : Part.values())
                  {
                        int index = part.getIndex();
                        if (version >= 0x13c && (index >= 0x36 && index <= 0x3c)) continue;
                        if (version >= 0x18c && index == 0x3d) continue;
                        if (subVersion >= 0x107 && index == 0x3e) continue;
                        else if (index == 0x3e)
                        {
                              if (parts[index] != null)
                              {
                                    flags |= (1l << 0x29);
                                    lastPart = part;
                              }
                              continue;
                        }

                        if (parts[index] != null)
                        {
                              // Offset due to PCreatorAnim
                              if (subVersion < 0x107 && index > 0x28) index++;

                              flags |= (1l << index);

                              lastPart = part;
                        }
                  }
                  partsRevision = (lastPart == null) ? 0 : lastPart.getVersion();
            }

            if (serializer.isWriting())
            {
                  if (partsRevision > maxPartsRevision)
                        partsRevision = maxPartsRevision;
            }

            partsRevision = serializer.s32(partsRevision);
            if (isCompressed)
            {
                  // serializer.log("FLAGS");
                  flags = serializer.i64(flags);
            }

            // I have no idea why they did this
            if (version == 0x13c) partsRevision += 7;

            Part[] partsToSerialize = Part.fromFlags(revision.getHead(), flags, partsRevision);
            if (!ResourceSystem.DISABLE_LOGS)
                  serializer.log(Arrays.toString(partsToSerialize));

            for (Part part : partsToSerialize)
            {
                  serializer.log(part.name() + " [START]");
                  if (!part.serialize(this.parts, partsRevision, flags, serializer))
                  {
                        serializer.log(part.name() + " FAILED");
                        throw new SerializationException(part.name() + " failed to serialize!");
                  }
                  serializer.log(part.name() + " [END]");
            }

            // if (subVersion >= 0x83 && subVersion < 0x8b)
            // serializer.u8(0);

            serializer.log("THING " + Bytes.toHex(UID) + " [END]");
      }

      @SuppressWarnings("unchecked")
      public <T extends Serializable> T getPart(Part part)
      {
            return (T) this.parts[part.getIndex()];
      }

      public <T extends Serializable> void setPart(Part part, T value)
      {
            this.parts[part.getIndex()] = value;
      }

      public boolean hasPart(Part part)
      {
            return this.parts[part.getIndex()] != null;
      }

      @Override
      public int getAllocatedSize()
      {
            return BASE_ALLOCATION_SIZE;
      }

      @Override
      public String toString()
      {
            if (this.name != null) return this.name;
            return String.format("New Thing (%d)", this.UID);
      }
}
