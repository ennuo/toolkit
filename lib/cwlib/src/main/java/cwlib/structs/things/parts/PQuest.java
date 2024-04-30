package cwlib.structs.things.parts;

import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PQuest implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public int type;

    // This is a guess really
    @GsonRevision(lbp3 = true, min = 0x9d)
    public String questID, objectiveID;

    @GsonRevision(lbp3 = true, min = 0x141)
    public int questKey, objectiveKey;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        type = serializer.i32(type);

        if (type != 5)
            throw new SerializationException("Unsupported quest type!");

        if (subVersion > 0x9c)
        {
            questID = serializer.wstr(questID);
            objectiveID = serializer.wstr(objectiveID);
        }

        if (subVersion > 0x140)
        {
            questKey = serializer.i32(questKey);
            objectiveKey = serializer.i32(objectiveKey);
        }

        if (subVersion < 0x52)
            serializer.u8(0);

        // These fields are supposed to be serialized based on quest type
        // and some field not being null, but this field is never serialized so?
        // It's probably just a legacy thing, might be a problem?

        if (subVersion >= 0x47)
        {
            switch (type)
            {
                case 1:
                {
                    serializer.wstr(null);
                    serializer.wstr(null);
                    serializer.u8(0);
                    if (subVersion >= 0x5e && subVersion < 0x127)
                        serializer.u8(0);
                    break;
                }

                case 2:
                {
                    serializer.wstr(null);
                    break;
                }
                case 3:
                case 4:
                {
                    serializer.i32(0);
                    serializer.wstr(null);
                    serializer.wstr(null);
                    serializer.wstr(null);
                    break;
                }
                default:
                    break;
            }
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PQuest.BASE_ALLOCATION_SIZE;
        if (this.questID != null)
            size += (this.questID.length() * 0x2);
        if (this.objectiveID != null)
            size += (this.objectiveID.length() * 0x2);
        return size;
    }
}
