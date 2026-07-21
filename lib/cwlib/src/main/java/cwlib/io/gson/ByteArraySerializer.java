package cwlib.io.gson;

import com.google.gson.*;

import cwlib.ConfigShared;

import java.lang.reflect.Type;
import java.util.Base64;

public class ByteArraySerializer implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> 
{
    @Override
    public byte[] deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        if (je.isJsonPrimitive())
        {
            var prim = je.getAsJsonPrimitive();
            return Base64.getDecoder().decode(prim.getAsString());
        }

        var arr = je.getAsJsonArray();
        byte[] bytes = new byte[arr.size()];
        for (int i = 0; i < arr.size(); ++i)
            bytes[i] = arr.get(i).getAsByte();
        
        return bytes;
    }

    @Override
    public JsonElement serialize(byte[] bytes, Type type, JsonSerializationContext jsc)
    {
        if (ConfigShared.export().encodeBytes)
            return new JsonPrimitive(Base64.getEncoder().encodeToString(bytes));

        var arr = new JsonArray(bytes.length);
        for (byte b : bytes)
            arr.add(b);

        return arr;
    }
}
