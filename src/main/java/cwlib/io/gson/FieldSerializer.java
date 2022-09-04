package cwlib.io.gson;

import java.lang.reflect.Type;
import java.util.EnumSet;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import cwlib.enums.BuiltinType;
import cwlib.enums.MachineType;
import cwlib.enums.ModifierType;
import cwlib.enums.ScriptObjectType;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.script.FieldLayoutDetails;
import cwlib.structs.things.components.script.ScriptObject;
import cwlib.util.GsonUtils;

public class FieldSerializer implements JsonSerializer<FieldLayoutDetails>, JsonDeserializer<FieldLayoutDetails> {
    @Override public FieldLayoutDetails deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        FieldLayoutDetails layout = new FieldLayoutDetails();
        JsonObject object = je.getAsJsonObject();
        
        if (object.has("name"))
            layout.name = object.get("name").getAsString();
        if (object.has("modifiers"))
            layout.modifiers = jdc.deserialize(object.get("modifiers"), TypeToken.getParameterized(EnumSet.class, ModifierType.class).getType());
        if (object.has("machineType"))
            layout.machineType = jdc.deserialize(object.get("machineType"), MachineType.class);
        if (object.has("fishType"))
            layout.fishType = jdc.deserialize(object.get("fishType"), BuiltinType.class);
        if (object.has("arrayBaseMachineType"))
            layout.arrayBaseMachineType = jdc.deserialize(object.get("arrayBaseMachineType"), MachineType.class);
        if (object.has("instanceOffset"))
            layout.instanceOffset = object.get("instanceOffset").getAsInt();
        if (object.has("type"))
            layout.type = jdc.deserialize(object.get("type"), ScriptObjectType.class);
        if (object.has("value")) {
            switch (layout.machineType) {
                case BOOL: layout.value = object.get("value").getAsBoolean(); break;
                case CHAR: layout.value = object.get("value").getAsByte(); break;
                case S32: layout.value = object.get("value").getAsInt(); break;
                case F32:layout.value = object.get("value").getAsFloat(); break;
                case V4: layout.value = jdc.deserialize(object.get("value"), Vector4f.class); break;
                case M44: layout.value = jdc.deserialize(object.get("value"), Matrix4f.class); break;
                case OBJECT_REF: layout.value = jdc.deserialize(object.get("value"), ScriptObject.class); break;
                case SAFE_PTR: layout.value = jdc.deserialize(object.get("value"), Thing.class); break;
                default: break;
            }
        }
        return layout;
    }

    @Override public JsonElement serialize(FieldLayoutDetails layout, Type type, JsonSerializationContext jsc) {
        JsonObject object = new JsonObject();
        object.add("name", jsc.serialize(layout.name));
        object.add("modifiers", jsc.serialize(layout.modifiers));
        object.add("machineType", jsc.serialize(layout.machineType));
        if (GsonUtils.REVISION.getVersion() >= 0x145)
            object.add("fishType", jsc.serialize(layout.fishType));
        object.add("arrayBaseMachineType", jsc.serialize(layout.arrayBaseMachineType));
        object.add("instanceOffset", new JsonPrimitive(layout.instanceOffset));
        if (layout.value != null) {
            switch (layout.machineType) {
                case BOOL: object.add("value", new JsonPrimitive((boolean) layout.value)); break;
                case CHAR: object.add("value", new JsonPrimitive((byte) layout.value)); break;
                case S32: object.add("value", new JsonPrimitive((int) layout.value)); break;
                case F32: object.add("value", new JsonPrimitive((float) layout.value)); break;
                case V4: object.add("value", jsc.serialize(layout.value, Vector4f.class)); break;
                case M44: object.add("value", jsc.serialize(layout.value, Matrix4f.class)); break;
                case OBJECT_REF: 
                    object.add("type", jsc.serialize(layout.type));
                    object.add("value", jsc.serialize(layout.value, ScriptObject.class)); 
                    break;
                case SAFE_PTR:
                    object.add("value", jsc.serialize(layout.value, Thing.class));
                default: break;
            }
        }
        return object;
    }
}
