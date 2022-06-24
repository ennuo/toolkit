package cwlib.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joml.Vector4f;

public class Vector4fSerializer implements JsonSerializer<Vector4f>, JsonDeserializer<Vector4f> {
    @Override public Vector4f deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonArray array = je.getAsJsonArray();
        if (array.size() != 4)
            throw new JsonParseException("Vector4f serializable object must have 4 elements!");
        return new Vector4f(
            array.get(0).getAsFloat(), 
            array.get(1).getAsFloat(),
            array.get(2).getAsFloat(),
            array.get(3).getAsFloat()
        );
    }

    @Override public JsonElement serialize(Vector4f vector, Type type, JsonSerializationContext jsc) {
        JsonArray array = new JsonArray(4);
        array.add(vector.x);
        array.add(vector.y);
        array.add(vector.z);
        array.add(vector.w);
        return array;
    }
}
