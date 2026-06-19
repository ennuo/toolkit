package cwlib.io.gson;

import com.google.gson.*;

import cwlib.structs.things.parts.PScriptName;

import java.lang.reflect.Type;

public class ScriptNameSerializer implements JsonSerializer<PScriptName>, JsonDeserializer<PScriptName>
{
    @Override
    public PScriptName deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        PScriptName part = new PScriptName();
        JsonObject object = je.getAsJsonObject();
        if (object.has("name"))
            part.setName(jdc.deserialize(object.get("name"), String.class));
        return part;
    }

    @Override
    public JsonElement serialize(PScriptName name, Type type, JsonSerializationContext jsc)
    {
        JsonObject object = new JsonObject();
        object.add("name", jsc.serialize(name.getName()));
        return object;
    }
}
