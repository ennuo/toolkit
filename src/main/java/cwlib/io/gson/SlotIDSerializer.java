package cwlib.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cwlib.enums.SlotType;
import cwlib.structs.slot.SlotID;

public class SlotIDSerializer implements JsonSerializer<SlotID>, JsonDeserializer<SlotID> {
    @Override public SlotID deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        String id = je.getAsString().toUpperCase();
        if (id.equals("NONE"))
            return new SlotID();
        String[] fragments = je.getAsString().split(":");
        return new SlotID(SlotType.valueOf(fragments[0]), Long.valueOf(fragments[1]));
    }

    @Override public JsonElement serialize(SlotID id, Type type, JsonSerializationContext jsc) {
        if (id.slotNumber == 0 && id.slotType == SlotType.DEVELOPER)
            return new JsonPrimitive("NONE");
        return new JsonPrimitive(id.slotType.toString() + ":" + id.slotNumber);
    }
}
