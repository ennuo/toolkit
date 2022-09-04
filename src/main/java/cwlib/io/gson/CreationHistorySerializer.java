package cwlib.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cwlib.structs.inventory.CreationHistory;

public class CreationHistorySerializer implements JsonSerializer<CreationHistory>, JsonDeserializer<CreationHistory> {
    @Override public CreationHistory deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonArray array = je.getAsJsonArray();
        CreationHistory history = new CreationHistory();
        if (array.size() == 0) return null;
        history.creators = new String[array.size()];
        for (int i = 0; i < array.size(); ++i)
            history.creators[i] = array.get(i).getAsString();
        return history;
    }

    @Override public JsonElement serialize(CreationHistory history, Type type, JsonSerializationContext jsc) {
        if (history.creators == null || history.creators.length == 0) return null;
        JsonArray array = new JsonArray(history.creators.length);
        for (int i = 0; i < history.creators.length; ++i)
            array.add(history.creators[i]);
        return array;
    }
}
