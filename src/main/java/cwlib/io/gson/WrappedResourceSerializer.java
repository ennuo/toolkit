package cwlib.io.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.resources.RPlan;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;
import cwlib.types.data.WrappedResource;
import cwlib.util.GsonUtils;
import java.lang.reflect.Type;

public class WrappedResourceSerializer implements JsonSerializer<WrappedResource>, JsonDeserializer<WrappedResource> {
    public static class PlanWrapper {
        @GsonRevision(lbp3=true,min=204)
        public boolean isUsedForStreaming;
        public Thing[] things;
        @GsonRevision(min=407)
        public InventoryItemDetails inventoryData;
    }

    @Override public WrappedResource deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonObject object = je.getAsJsonObject();

        WrappedResource resource = new WrappedResource();

        int head = object.get("revision").getAsInt();
        short branchID = 0;
        short branchRevision = 0;
        if (object.has("branch") && !object.get("branch").isJsonNull()) {
            JsonObject branch = object.get("branch").getAsJsonObject();
            if (branch.has("id") && !branch.get("id").isJsonNull()) {
                String text = branch.get("id").getAsString();
                branchID = (short) (((short) text.charAt(1)) | (((short) text.charAt(0)) << 8));
            }
            if (branch.has("revision"))
                branchRevision = branch.get("revision").getAsShort();
        }

        Revision revision = new Revision(head, branchID, branchRevision);
        byte compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
        if (head >= 0x297 || (head == 0x272 && (branchID == 0x4c44) && ((branchRevision & 0xffff) > 1)))
            compressionFlags = CompressionFlags.USE_ALL_COMPRESSION;

        resource.revision = revision;
        GsonUtils.REVISION = revision;

        ResourceType resourceType = jdc.deserialize(object.get("type"), ResourceType.class);
        resource.type = resourceType;

        if (resource.type.equals(ResourceType.PLAN)) {
            PlanWrapper wrapper = jdc.deserialize(object.get("resource"), PlanWrapper.class);
            RPlan plan = new RPlan();

            plan.revision = resource.revision;
            plan.compressionFlags = compressionFlags;
            plan.inventoryData = wrapper.inventoryData;
            plan.isUsedForStreaming = wrapper.isUsedForStreaming;
            plan.setThings(wrapper.things);

            resource.resource = plan;

            return resource;
        }

        resource.resource = jdc.deserialize(object.get("resource"), resourceType.getCompressable());

        return resource;
    }

    @Override public JsonElement serialize(WrappedResource resource, Type type, JsonSerializationContext jsc) {
        JsonObject object = new JsonObject();

        object.add("revision", new JsonPrimitive(resource.revision.getHead()));
        short id = resource.revision.getBranchID();
        if (id != 0) {
            JsonObject branch = new JsonObject();
            branch.add("id", new JsonPrimitive(new String(new byte[] { (byte) (id >> 8), (byte) (id & 0xff) })));
            branch.add("revision", new JsonPrimitive(resource.revision.getBranchRevision()));
            object.add("branch", branch);
        }

        object.add("type", jsc.serialize(resource.type));

        if (resource.type.equals(ResourceType.PLAN)) {
            PlanWrapper wrapper = new PlanWrapper();
            RPlan plan = (RPlan) resource.resource;
            wrapper.isUsedForStreaming = plan.isUsedForStreaming;
            wrapper.things = plan.getThings();
            wrapper.inventoryData = plan.inventoryData;
            object.add("resource", jsc.serialize(wrapper));
        } else object.add("resource", jsc.serialize(resource.resource));

        return object;
    }
}
