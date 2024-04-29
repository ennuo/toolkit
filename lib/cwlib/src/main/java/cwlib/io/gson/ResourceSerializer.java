package cwlib.io.gson;

import com.google.gson.*;
import cwlib.enums.ResourceType;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.SHA1;

import java.lang.reflect.Type;

public class ResourceSerializer implements JsonSerializer<ResourceDescriptor>,
    JsonDeserializer<ResourceDescriptor>
{
    @Override
    public ResourceDescriptor deserialize(JsonElement je, Type type,
                                          JsonDeserializationContext jdc)
    throws JsonParseException
    {
        JsonObject object = je.getAsJsonObject();
        ResourceType resType = ResourceType.INVALID;
        if (object.has("type"))
            resType = jdc.deserialize(object.get("type"), ResourceType.class);
        JsonElement element = object.get("value");
        if (element == null || !element.isJsonPrimitive()) return null;
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isString())
            return new ResourceDescriptor(new SHA1(primitive.getAsString()), resType);
        else if (primitive.isNumber())
            return new ResourceDescriptor(new GUID(primitive.getAsLong()), resType);
        return null;
    }

    @Override
    public JsonElement serialize(ResourceDescriptor resource, Type type,
                                 JsonSerializationContext jsc)
    {
        JsonElement value = null;
        if (resource == null) return null;
        if (resource.isGUID())
            value = jsc.serialize(resource.getGUID());
        else if (resource.isHash())
            value = jsc.serialize(resource.getSHA1());
        else return null;

        JsonObject object = new JsonObject();
        object.add("value", value);
        object.add("type", jsc.serialize(resource.getType()));
        return object;
    }
}
