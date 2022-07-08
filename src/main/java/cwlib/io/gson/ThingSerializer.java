package cwlib.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cwlib.enums.Part;
import cwlib.structs.things.Thing;
import cwlib.types.data.GUID;
import cwlib.util.GsonUtils;

public class ThingSerializer implements JsonSerializer<Thing>, JsonDeserializer<Thing> {


    @Override public Thing deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        if (je.isJsonPrimitive())
            return GsonUtils.THINGS.get(je.getAsNumber());
        
        return jdc.deserialize(je, Thing.class);
    }

    @Override public JsonElement serialize(Thing thing, Type type, JsonSerializationContext jsc) {
        if (GsonUtils.UIDs.contains(thing.UID))
            return new JsonPrimitive(thing.UID);
        GsonUtils.UIDs.add(thing.UID);
        

        JsonObject object = new JsonObject();

        object.addProperty("UID", thing.UID);
        object.add("planGUID", jsc.serialize(thing.planGUID));

        object.add("parent", jsc.serialize(thing.parent));
        object.add("group", jsc.serialize(thing.groupHead));

        JsonObject parts = new JsonObject();
        for (Part part : Part.values()) {
            System.out.println(part);
            Object component = thing.getPart(part);
            if (component != null)
                parts.add(part.getNameForReflection(), jsc.serialize(component));
        }

        object.add("parts", parts);


        return object;
    }
}
