package cwlib.io.gson;

import com.google.gson.*;
import cwlib.types.data.SHA1;

import java.lang.reflect.Type;

public class SHA1Serializer implements JsonSerializer<SHA1>, JsonDeserializer<SHA1>
{
    @Override
    public SHA1 deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        return new SHA1(je.getAsString());
    }

    @Override
    public JsonElement serialize(SHA1 hash, Type type, JsonSerializationContext jsc)
    {
        return new JsonPrimitive(hash.toString());
    }
}
