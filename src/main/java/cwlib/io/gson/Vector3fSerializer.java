package cwlib.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joml.Vector3f;

public class Vector3fSerializer implements JsonSerializer<Vector3f>, JsonDeserializer<Vector3f> {
    @Override public Vector3f deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonArray array = je.getAsJsonArray();
        if (array.size() != 3)
            throw new JsonParseException("Vector3f serializable object must have 3 elements!");
        return new Vector3f(
            array.get(0).getAsFloat(), 
            array.get(1).getAsFloat(),
            array.get(2).getAsFloat()
        );
    }

    @Override public JsonElement serialize(Vector3f vector, Type type, JsonSerializationContext jsc) {
        JsonArray array = new JsonArray(3);
        array.add(vector.x);
        array.add(vector.y);
        array.add(vector.z);
        return array;
    }
}
