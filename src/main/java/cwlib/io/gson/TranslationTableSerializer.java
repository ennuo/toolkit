package cwlib.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cwlib.resources.RTranslationTable;
import java.util.Map.Entry;

public class TranslationTableSerializer implements JsonDeserializer<RTranslationTable> {
    @Override public RTranslationTable deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        RTranslationTable table = new RTranslationTable();
        JsonObject object = je.getAsJsonObject();
        for (Entry<String, JsonElement> entry : object.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            try { table.add(Long.parseLong(key), value.getAsString());
            } catch (NumberFormatException ex) { table.add(key, value.getAsString()); }
        }
        return table;
    }
}
