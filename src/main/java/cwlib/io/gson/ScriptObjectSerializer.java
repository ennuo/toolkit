package cwlib.io.gson;

import java.lang.reflect.Type;

import org.joml.Vector4f;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cwlib.enums.ScriptObjectType;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.script.ScriptInstance;
import cwlib.structs.things.components.script.ScriptObject;
import cwlib.types.data.ResourceDescriptor;

public class ScriptObjectSerializer implements JsonSerializer<ScriptObject>, JsonDeserializer<ScriptObject> {
    @Override public ScriptObject deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        ScriptObject layout = new ScriptObject();
        JsonObject object = je.getAsJsonObject();

        if (object.has("type")) {
            layout.type = jdc.deserialize(object.get("type"), ScriptObjectType.class);
            if (object.has("value")) {
                switch (layout.type) {
                    case ARRAY_BOOL: layout.value = jdc.deserialize(object.get("value"), boolean[].class); break;
                    case ARRAY_CHAR: layout.value = jdc.deserialize(object.get("value"), byte[].class); break;
                    case ARRAY_S32: layout.value = jdc.deserialize(object.get("value"), int[].class); break;
                    case ARRAY_F32: layout.value = jdc.deserialize(object.get("value"), float[].class); break;
                    case ARRAY_VECTOR4: layout.value = jdc.deserialize(object.get("value"), Vector4f[].class); break;
                    case INSTANCE: layout.value = jdc.deserialize(object.get("value"), ScriptInstance.class); break;
                    case STRINGW:
                    case STRINGA:
                        layout.value = jdc.deserialize(object.get("value"), String.class); break;
                    case RESOURCE:
                        layout.value = jdc.deserialize(object.get("value"), ResourceDescriptor.class); break;
                    case ARRAY_SAFE_PTR:
                        layout.value = jdc.deserialize(object.get("value"), Thing[].class); break;
                    case ARRAY_OBJECT_REF:
                        layout.value = jdc.deserialize(object.get("value"), ScriptObject[].class); break;
                    default: break;
                }
            }
        }

        return layout;
    }

    @Override public JsonElement serialize(ScriptObject layout, Type type, JsonSerializationContext jsc) {
        JsonObject object = new JsonObject();
        object.add("type", jsc.serialize(layout.type));
        if (layout.value != null) {
            switch (layout.type) {
                case ARRAY_BOOL: object.add("value", jsc.serialize(layout.value, boolean[].class)); break;
                case ARRAY_CHAR: object.add("value", jsc.serialize(layout.value, byte[].class)); break;
                case ARRAY_S32: object.add("value", jsc.serialize(layout.value, int[].class)); break;
                case ARRAY_F32: object.add("value", jsc.serialize(layout.value, float[].class)); break;
                case ARRAY_VECTOR4: object.add("value", jsc.serialize(layout.value, Vector4f[].class)); break;
                case INSTANCE: object.add("value", jsc.serialize(layout.value, ScriptInstance.class)); break;
                case STRINGW:
                case STRINGA:
                    object.add("value", jsc.serialize(layout.value, String.class)); break;
                case RESOURCE: object.add("value", jsc.serialize(layout.value, ResourceDescriptor.class)); break;
                case ARRAY_SAFE_PTR: object.add("value", jsc.serialize(layout.value, Thing[].class)); break;
                case ARRAY_OBJECT_REF: object.add("value", jsc.serialize(layout.value, ScriptObject[].class)); break;
                default: break;
            }
        }
        return object;
    }
}
