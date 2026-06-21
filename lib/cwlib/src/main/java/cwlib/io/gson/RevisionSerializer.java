package cwlib.io.gson;

import com.google.gson.*;

import cwlib.types.data.Revision;
import cwlib.util.Bytes;

import java.lang.reflect.Type;

public class RevisionSerializer 
implements JsonSerializer<Revision>, JsonDeserializer<Revision>
{
    @Override public Revision deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        if (je.isJsonPrimitive())
            return new Revision(je.getAsInt());

        var obj = je.getAsJsonObject();

        int head = obj.get("revision").getAsInt();

        short branchID = 0, branchRevision = 0;
        if (obj.has("branch") && !obj.get("branch").isJsonNull())
        {
            JsonObject branch = obj.get("branch").getAsJsonObject();
            if (branch.has("id") && !branch.get("id").isJsonNull())
            {
                String text = branch.get("id").getAsString();
                branchID =
                    (short) (((short) text.charAt(1)) | (((short) text.charAt(0)) << 8));
            }
            if (branch.has("revision"))
                branchRevision = branch.get("revision").getAsShort();
        }

        var revision = new Revision(head, branchID, branchRevision);

        if (obj.has("alear") && !obj.get("alear").isJsonNull())
        {
            JsonObject branch = obj.get("alear").getAsJsonObject();

            int customVersion = 1;
            int customBranch = 0;

            if (branch.has("id") && !branch.get("id").isJsonNull())
            {
                String text = branch.get("id").getAsString();
                customBranch = Bytes.toMagic(text);
            }

            if (branch.has("revision")) customVersion = branch.get("revision").getAsInt();

            revision.setCustomBranchDescription(customBranch, customVersion);
        }

        return revision;
    }

    @Override public JsonElement serialize(Revision revision, Type type, JsonSerializationContext jsc)
    {
        boolean alear = revision.hasExtraData();
        boolean branched = revision.getBranchID() != 0;

        if (alear || branched)
        {
            var obj = new JsonObject();
            obj.add("revision", new JsonPrimitive(revision.getHead()));
            
            if (branched)
            {
                short id = revision.getBranchID();
                var branch = new JsonObject();
                branch.add("id", new JsonPrimitive(new String(new byte[] { (byte) (id >> 8), (byte) (id & 0xff) })));
                branch.add("revision", new JsonPrimitive(revision.getBranchRevision()));
                obj.add("branch", branch);
            }

            if (alear)
            {
                var branch = new JsonObject();
                branch.add("id", new JsonPrimitive(Bytes.toMagic(revision.getCustomBranchID())));
                branch.add("revision", new JsonPrimitive(revision.getCustomVersion()));
                obj.add("alear", branch);
            }

            return obj;
        }

        return new JsonPrimitive(revision.getHead());
    }
}
