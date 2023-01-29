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
            return GsonUtils.THINGS.get(je.getAsInt());
        JsonObject object = je.getAsJsonObject();
        int UID = object.get("UID").getAsInt();
        Thing thing = new Thing(UID);
        GsonUtils.THINGS.put(UID, thing);

        if (object.has("world"))
            thing.world = jdc.deserialize(object.get("world"), Thing.class);
        if (object.has("planGUID"))
            thing.planGUID = jdc.deserialize(object.get("planGUID"), GUID.class);
        if (object.has("parent"))
            thing.parent = jdc.deserialize(object.get("parent"), Thing.class);
        if (object.has("group"))
            thing.groupHead = jdc.deserialize(object.get("group"), Thing.class);

        for (Part part : Part.values()) {
            JsonElement element = object.get(part.getNameForReflection());
            if (element != null)
                thing.setPart(part, jdc.deserialize(element, part.getSerializable()));
        }
        
        return thing;
    }

    @Override public JsonElement serialize(Thing thing, Type type, JsonSerializationContext jsc) {
        if (GsonUtils.UNIQUE_THINGS.contains(thing))
            return new JsonPrimitive(thing.UID);
        GsonUtils.UNIQUE_THINGS.add(thing);
        
        JsonObject object = new JsonObject();

        int version = GsonUtils.REVISION.getVersion();

        object.addProperty("UID", thing.UID);

        if (version < 0x1fd)
            object.add("world", jsc.serialize(thing.world));
        
        if (version >= 0x254)
            object.add("planGUID", jsc.serialize(thing.planGUID));

        object.add("parent", jsc.serialize(thing.parent));
        object.add("group", jsc.serialize(thing.groupHead));

        for (Part part : Part.values()) {
            Object component = thing.getPart(part);
            if (component != null)
                object.add(part.getNameForReflection(), jsc.serialize(component));
        }

        // JsonObject parts = new JsonObject();
        // for (Part part : Part.values()) {
        //     // System.out.println(part);
        //     Object component = thing.getPart(part);
        //     if (component != null)
        //         parts.add(part.getNameForReflection(), jsc.serialize(component));
        // }

        // object.add("parts", parts);


        return object;
    }
}
