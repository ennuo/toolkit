package cwlib.io.gson;

import com.google.gson.*;
import cwlib.enums.AudioMaterial;

import java.lang.reflect.Type;

public class AudioMaterialSerializer implements JsonSerializer<Integer>, JsonDeserializer<Integer>
{
    @Override
    public Integer deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        JsonPrimitive prim = je.getAsJsonPrimitive();
        if (prim.isString())
        {
            try { return AudioMaterial.valueOf(prim.getAsString()).getValue(); }
            catch (Exception ex) { return 0; }
        }

        if (prim.isNumber())
            return prim.getAsInt();

        return 0;
    }

    @Override
    public JsonElement serialize(Integer value, Type type, JsonSerializationContext jsc)
    {
        AudioMaterial aum = AudioMaterial.fromValue(value);
        if (aum != null)
            return new JsonPrimitive(aum.name());
        return new JsonPrimitive(value);
    }
}
