package cwlib.io.gson;

import com.google.gson.*;
import cwlib.types.data.NetworkOnlineID;

import java.lang.reflect.Type;

public class NetworkOnlineIDSerializer implements JsonSerializer<NetworkOnlineID>,
    JsonDeserializer<NetworkOnlineID>
{
    @Override
    public NetworkOnlineID deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        return new NetworkOnlineID(je.getAsString());
    }

    @Override
    public JsonElement serialize(NetworkOnlineID id, Type type, JsonSerializationContext jsc)
    {
        return new JsonPrimitive(id.toString());
    }
}
