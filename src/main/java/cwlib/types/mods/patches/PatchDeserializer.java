package cwlib.types.mods.patches;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class PatchDeserializer<T> implements JsonDeserializer<T> {
    @Override
    public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        String patchType = je.getAsJsonObject().get("type").getAsString().toLowerCase();
        switch (patchType) {
            case "translation":
                return jdc.deserialize(je, TranslationPatch.class);
            default: return null;
        }
    }
    
}
