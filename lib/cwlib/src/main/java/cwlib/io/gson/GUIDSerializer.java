package cwlib.io.gson;

import com.google.gson.*;
import cwlib.types.data.GUID;

import java.lang.reflect.Type;

public class GUIDSerializer implements JsonSerializer<GUID>, JsonDeserializer<GUID>
{
    @Override
    public GUID deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        return new GUID(je.getAsInt());
    }

    @Override
    public JsonElement serialize(GUID guid, Type type, JsonSerializationContext jsc)
    {
        return new JsonPrimitive(guid.getValue());
    }
}
