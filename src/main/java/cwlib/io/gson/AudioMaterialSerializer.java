package cwlib.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cwlib.enums.AudioMaterial;

public class AudioMaterialSerializer implements JsonSerializer<Integer>, JsonDeserializer<Integer> {
    @Override public Integer deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonPrimitive prim = je.getAsJsonPrimitive();
        if (prim.isString()) {
            try { return AudioMaterial.valueOf(prim.getAsString()).getValue(); } 
            catch (Exception ex) { return 0; }
        }

        if (prim.isNumber())
            return prim.getAsInt();
        
        return 0;
    }

    @Override public JsonElement serialize(Integer value, Type type, JsonSerializationContext jsc) {
        AudioMaterial aum = AudioMaterial.fromValue((int) value);
        if (aum != null)
            return new JsonPrimitive(aum.name());
        return new JsonPrimitive(value);
    }
}
