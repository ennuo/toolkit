package cwlib.io.gson;

import com.google.gson.*;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.script.FieldLayoutDetails;
import cwlib.structs.things.components.script.InstanceLayout;
import cwlib.structs.things.components.script.ScriptInstance;
import cwlib.structs.things.components.script.ScriptObject;
import cwlib.structs.things.components.script.ScriptObjectUID;
import cwlib.types.data.ResourceDescriptor;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.lang.reflect.Type;

public class ScriptInstanceSerializer implements JsonSerializer<ScriptInstance>,
    JsonDeserializer<ScriptInstance>
{
    @Override
    public ScriptInstance deserialize(JsonElement je, Type type,
                                          JsonDeserializationContext jdc)
    throws JsonParseException
    {
        ScriptInstance instance = new ScriptInstance();
        JsonObject object = je.getAsJsonObject();

        if (object.has("script"))
            instance.script = jdc.deserialize(object.get("script"), ResourceDescriptor.class);
        if (object.has("layout"))
            instance.layout = jdc.deserialize(object.get("layout"), InstanceLayout.class);
        
        if (instance.layout == null || !object.has("members")) return instance;
        JsonObject members = object.getAsJsonObject("members");
        for (FieldLayoutDetails field : instance.layout.fields)
        {
            if (!members.has(field.name)) continue;
            Object value = null;
            switch (field.machineType)
            {
                case BOOL:
                    value = members.get(field.name).getAsBoolean();
                    break;
                case CHAR:
                    value = members.get(field.name).getAsByte();
                    break;
                case S32:
                    value = members.get(field.name).getAsInt();
                    break;
                case F32:
                    value = members.get(field.name).getAsFloat();
                    break;
                case V4:
                    value = jdc.deserialize(members.get(field.name), Vector4f.class);
                    break;
                case M44:
                    value = jdc.deserialize(members.get(field.name), Matrix4f.class);
                    break;
                case OBJECT_REF:
                    value = jdc.deserialize(members.get(field.name), ScriptObjectUID.class);
                    break;
                case SAFE_PTR:
                    value = jdc.deserialize(members.get(field.name), Thing.class);
                    break;
                default:
                    break;
            }

            instance.memberData.put(field.name, value);
        }

        return instance;
    }

    @Override
    public JsonElement serialize(ScriptInstance instance, Type type,
                                 JsonSerializationContext jsc)
    {
        JsonObject object = new JsonObject();
        object.add("script", jsc.serialize(instance.script));
        object.add("layout", jsc.serialize(instance.layout));

        JsonObject members = new JsonObject();
        if (instance.layout == null) return object;

        for (FieldLayoutDetails field : instance.layout.fields)
        {
            Object value = instance.memberData.get(field.name);
            if (value == null) continue;
            switch (field.machineType)
            {
                case BOOL:
                    members.add(field.name, new JsonPrimitive((boolean) value));
                    break;
                case CHAR:
                    members.add(field.name, new JsonPrimitive((byte) value));
                    break;
                case S32:
                    members.add(field.name, new JsonPrimitive((int) value));
                    break;
                case F32:
                    members.add(field.name, new JsonPrimitive((float) value));
                    break;
                case V4:
                    members.add(field.name, jsc.serialize(value, Vector4f.class));
                    break;
                case M44:
                    members.add(field.name, jsc.serialize(value, Matrix4f.class));
                    break;
                case OBJECT_REF:
                    members.add(field.name, jsc.serialize(value, ScriptObjectUID.class));
                    break;
                case SAFE_PTR:
                    members.add(field.name, jsc.serialize(value, Thing.class));
                default:
                    break;
            }
        }

        object.add("members", members);
        return object;
    }
}
