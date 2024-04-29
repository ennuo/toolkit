package cwlib.io.gson;

import com.google.gson.*;
import org.joml.Vector2f;

import java.lang.reflect.Type;

public class Vector2fSerializer implements JsonSerializer<Vector2f>, JsonDeserializer<Vector2f>
{
    @Override
    public Vector2f deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        JsonArray array = je.getAsJsonArray();
        if (array.size() != 2)
            throw new JsonParseException("Vector2f serializable object must have 2 " +
                                         "elements!");
        return new Vector2f(
            array.get(0).getAsFloat(),
            array.get(1).getAsFloat()
        );
    }

    @Override
    public JsonElement serialize(Vector2f vector, Type type, JsonSerializationContext jsc)
    {
        JsonArray array = new JsonArray(2);
        array.add(vector.x);
        array.add(vector.y);
        return array;
    }
}
