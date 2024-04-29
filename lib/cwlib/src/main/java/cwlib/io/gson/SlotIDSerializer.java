package cwlib.io.gson;

import com.google.gson.*;
import cwlib.enums.SlotType;
import cwlib.structs.slot.SlotID;

import java.lang.reflect.Type;

public class SlotIDSerializer implements JsonSerializer<SlotID>, JsonDeserializer<SlotID>
{
    @Override
    public SlotID deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        String id = je.getAsString().toUpperCase();
        if (id.equals("NONE"))
            return new SlotID();
        String[] fragments = je.getAsString().split(":");
        return new SlotID(SlotType.valueOf(fragments[0]), Long.valueOf(fragments[1]));
    }

    @Override
    public JsonElement serialize(SlotID id, Type type, JsonSerializationContext jsc)
    {
        if (id.slotNumber == 0 && id.slotType == SlotType.DEVELOPER)
            return new JsonPrimitive("NONE");
        return new JsonPrimitive(id.slotType.toString() + ":" + id.slotNumber);
    }
}
