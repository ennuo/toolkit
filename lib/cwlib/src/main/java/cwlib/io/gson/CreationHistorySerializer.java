package cwlib.io.gson;

import com.google.gson.*;
import cwlib.structs.inventory.CreationHistory;

import java.lang.reflect.Type;

public class CreationHistorySerializer implements JsonSerializer<CreationHistory>,
    JsonDeserializer<CreationHistory>
{
    @Override
    public CreationHistory deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        JsonArray array = je.getAsJsonArray();
        CreationHistory history = new CreationHistory();
        if (array.size() == 0) return null;
        history.creators = new String[array.size()];
        for (int i = 0; i < array.size(); ++i)
            history.creators[i] = array.get(i).getAsString();
        return history;
    }

    @Override
    public JsonElement serialize(CreationHistory history, Type type, JsonSerializationContext jsc)
    {
        if (history.creators == null || history.creators.length == 0) return null;
        JsonArray array = new JsonArray(history.creators.length);
        for (int i = 0; i < history.creators.length; ++i)
            array.add(history.creators[i]);
        return array;
    }
}
