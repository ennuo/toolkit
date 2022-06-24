package cwlib.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cwlib.types.data.SHA1;

public class SHA1Serializer implements JsonSerializer<SHA1>, JsonDeserializer<SHA1> {
    @Override public SHA1 deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        return new SHA1(je.getAsString());
    }

    @Override public JsonElement serialize(SHA1 hash, Type type, JsonSerializationContext jsc) {
        return new JsonPrimitive(hash.toString());
    }
}
