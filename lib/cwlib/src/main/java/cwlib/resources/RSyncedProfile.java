package cwlib.resources;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.NetworkPlayerID;
import cwlib.types.data.Revision;

public class RSyncedProfile implements Resource
{
      public static final int BASE_ALLOCATION_SIZE = 0x20;

      public int timePlayed;
      public Thing platonicAvatar;
      public long uniqueNumber;

      public int primary, secondary, tertiary;

      public NetworkPlayerID playerID;

      @Override
      public void serialize(Serializer serializer)
      {
            int version = serializer.getRevision().getVersion();

            timePlayed = serializer.i32(timePlayed);
            platonicAvatar = serializer.thing(platonicAvatar);

            if (version < 0x13e)
                  serializer.resource(null, ResourceType.TEXTURE);

            timePlayed = serializer.i32(timePlayed);
            uniqueNumber = serializer.i64(uniqueNumber);

            if (version > 0x163)
            {
                  primary = serializer.i32(primary);
                  secondary = serializer.i32(secondary);
                  tertiary = serializer.i32(tertiary);
            }

            if (version > 0x1a7)
                  playerID = serializer.struct(playerID, NetworkPlayerID.class);

            if (version > 0x1c4 && version < 0x213)
                  serializer.i32(0);
      }

      @Override
      public int getAllocatedSize()
      {
            return BASE_ALLOCATION_SIZE;
      }

      @Override
      public SerializationData build(Revision revision, byte compressionFlags)
      {
            // 16MB buffer for generation of levels, since the allocated size will get
            // stuck in a recursive loop until I fix it.
            Serializer serializer = new Serializer(0x1000000, revision, compressionFlags);
            serializer.struct(this, RSyncedProfile.class);
            return new SerializationData(
                    serializer.getBuffer(),
                    revision,
                    compressionFlags,
                    ResourceType.SYNCED_PROFILE,
                    SerializationType.BINARY,
                    serializer.getDependencies()
            );
      }
}
