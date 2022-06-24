package cwlib.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cwlib.types.data.GUID;

public class GUIDSerializer implements JsonSerializer<GUID>, JsonDeserializer<GUID> {
    @Override public GUID deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        return new GUID(je.getAsInt());
    }

    @Override public JsonElement serialize(GUID guid, Type type, JsonSerializationContext jsc) {
        return new JsonPrimitive(guid.getValue());
    }
}
