package cwlib.io.gson;

import com.google.gson.*;
import cwlib.enums.ScriptObjectType;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.script.ScriptInstance;
import cwlib.structs.things.components.script.ScriptObject;
import cwlib.structs.things.components.script.ScriptObjectUID;
import cwlib.types.data.ResourceDescriptor;
import org.joml.Vector4f;

import java.lang.reflect.Type;

public class ScriptObjectUIDSerializer implements JsonSerializer<ScriptObjectUID>,
    JsonDeserializer<ScriptObjectUID>
{
    @Override
    public ScriptObjectUID deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
    throws JsonParseException
    {
        ScriptObjectUID layout = new ScriptObjectUID();
        JsonObject object = je.getAsJsonObject();

        if (object.has("type"))
        {
            layout.type = jdc.deserialize(object.get("type"), ScriptObjectType.class);
            if (layout.type != ScriptObjectType.NULL && object.has("value"))
            {
                layout.object = new ScriptObject();
                switch (layout.type)
                {
                    case ARRAY_BOOL:
                        layout.object.value = jdc.deserialize(object.get("value"),
                            boolean[].class);
                        break;
                    case ARRAY_CHAR:
                        layout.object.value = jdc.deserialize(object.get("value"),
                            byte[].class);
                        break;
                    case ARRAY_S32:
                        layout.object.value = jdc.deserialize(object.get("value"),
                            int[].class);
                        break;
                    case ARRAY_F32:
                        layout.object.value = jdc.deserialize(object.get("value"),
                            float[].class);
                        break;
                    case ARRAY_VECTOR4:
                        layout.object.value = jdc.deserialize(object.get("value"),
                            Vector4f[].class);
                        break;
                    case INSTANCE:
                        layout.object.value = jdc.deserialize(object.get("value"),
                            ScriptInstance.class);
                        break;
                    case STRINGW:
                    case STRINGA:
                        layout.object.value = jdc.deserialize(object.get("value"),
                            String.class);
                        break;
                    case RESOURCE:
                        layout.object.value = jdc.deserialize(object.get("value"),
                            ResourceDescriptor.class);
                        break;
                    case ARRAY_SAFE_PTR:
                        layout.object.value = jdc.deserialize(object.get("value"),
                            Thing[].class);
                        break;
                    case ARRAY_OBJECT_REF:
                        layout.object.value = jdc.deserialize(object.get("value"), ScriptObjectUID[].class);
                        break;
                    default:
                        break;
                }
            }
        }

        return layout;
    }

    @Override
    public JsonElement serialize(ScriptObjectUID layout, Type type, JsonSerializationContext jsc)
    {
        JsonObject object = new JsonObject();
        object.add("type", jsc.serialize(layout.type));
        if (layout.object != null)
        {
            switch (layout.type)
            {
                case ARRAY_BOOL:
                    object.add("value", jsc.serialize(layout.object.value, boolean[].class));
                    break;
                case ARRAY_CHAR:
                    object.add("value", jsc.serialize(layout.object.value, byte[].class));
                    break;
                case ARRAY_S32:
                    object.add("value", jsc.serialize(layout.object.value, int[].class));
                    break;
                case ARRAY_F32:
                    object.add("value", jsc.serialize(layout.object.value, float[].class));
                    break;
                case ARRAY_VECTOR4:
                    object.add("value", jsc.serialize(layout.object.value, Vector4f[].class));
                    break;
                case INSTANCE:
                    object.add("value", jsc.serialize(layout.object.value,
                        ScriptInstance.class));
                    break;
                case STRINGW:
                case STRINGA:
                    object.add("value", jsc.serialize(layout.object.value, String.class));
                    break;
                case RESOURCE:
                    object.add("value", jsc.serialize(layout.object.value,
                        ResourceDescriptor.class));
                    break;
                case ARRAY_SAFE_PTR:
                    object.add("value", jsc.serialize(layout.object.value, Thing[].class));
                    break;
                case ARRAY_OBJECT_REF:
                    object.add("value", jsc.serialize(layout.object.value, ScriptObjectUID[].class));
                    break;
                default:
                    break;
            }
        }
        return object;
    }
}
