package cwlib.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cwlib.types.data.NetworkOnlineID;

public class NetworkOnlineIDSerializer implements JsonSerializer<NetworkOnlineID>, JsonDeserializer<NetworkOnlineID> {
    @Override public NetworkOnlineID deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        return new NetworkOnlineID(je.getAsString());
    }

    @Override public JsonElement serialize(NetworkOnlineID id, Type type, JsonSerializationContext jsc) {
        return new JsonPrimitive(id.toString());
    }
}
