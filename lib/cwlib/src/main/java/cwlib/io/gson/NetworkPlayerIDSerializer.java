package cwlib.io.gson;

import com.google.gson.*;
import cwlib.types.data.NetworkPlayerID;

import java.lang.reflect.Type;

public class NetworkPlayerIDSerializer implements JsonSerializer<NetworkPlayerID>,
    JsonDeserializer<NetworkPlayerID>
{
    @Override
    public NetworkPlayerID deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        return new NetworkPlayerID(je.getAsString());
    }

    @Override
    public JsonElement serialize(NetworkPlayerID id, Type type, JsonSerializationContext jsc)
    {
        return new JsonPrimitive(id.toString());
    }
}
