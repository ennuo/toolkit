package cwlib.structs.things.components.script;

import org.joml.Vector4f;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.ResourceType;
import cwlib.enums.ScriptObjectType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.ScriptObjectSerializer;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

@JsonAdapter(ScriptObjectSerializer.class)
public class ScriptObject implements Serializable
{
    public ScriptObjectType type = ScriptObjectType.NULL;
    public Object value;

    public ScriptObject() { }

    public ScriptObject(ScriptObjectType type, Object value)
    {
        this.type = type;
        this.value = value;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        type = serializer.enum32(type);
        if (type == ScriptObjectType.NULL) return;

        if (type == ScriptObjectType.INSTANCE)
        {
            value = serializer.reference((ScriptInstance) value,
                ScriptInstance.class);
            return;
        }

        int reference = 0;
        if (serializer.isWriting())
        {
            if (value != null)
                reference = serializer.getNextReference();
            serializer.getOutput().i32(reference);
        }
        else reference = serializer.getInput().i32();
        if (reference == 0) return;

        if (!serializer.isWriting())
        {
            Object value = serializer.getPointer(reference);
            if (value != null)
            {
                this.value = value;
                return;
            }
        }

        serializer.log("" + type);
        switch (type)
        {
            case NULL:
                value = null;
                break;
            case ARRAY_BOOL:
                value = serializer.boolarray((boolean[]) value);
                break;
            case ARRAY_S32:
                value = serializer.intvector((int[]) value, true);
                break;
            case ARRAY_F32:
                value = serializer.floatarray((float[]) value);
                break;
            case ARRAY_VECTOR4:
                value = serializer.vectorarray((Vector4f[]) value);
                break;
            case STRINGW:
                value = serializer.wstr((String) value);
                break;
            case STRINGA:
                value = serializer.str((String) value);
                break;
            case RESOURCE:
            {
                ResourceDescriptor descriptor = (ResourceDescriptor) value;

                ResourceType type = ResourceType.INVALID;
                if (serializer.isWriting()) type = descriptor.getType();
                type = serializer.enum32(type);

                if (type != ResourceType.INVALID)
                {
                    if (serializer.isWriting())
                        serializer.resource((ResourceDescriptor) value, type);
                    else
                        value = serializer.resource(null, type);
                }

                break;
            }
            // case INSTANCE: object.value = serializer.reference((PScript) object.value,
            // PScript
            // .class); break;
            case AUDIOHANDLE:
                value = null;
                break;
            case ARRAY_SAFE_PTR:
                value = serializer.array((Thing[]) value, Thing.class, true);
                break;
            case ARRAY_OBJECT_REF:
            {
                ScriptObject[] array = (ScriptObject[]) value;
                if (!serializer.isWriting())
                {
                    array = new ScriptObject[serializer.getInput().i32()];
                    serializer.setPointer(reference, array);
                }
                else serializer.getOutput().i32(array.length);
                for (int i = 0; i < array.length; ++i)
                    array[i] = serializer.struct(array[i], ScriptObject.class);
                return;
            }
            default:
                throw new SerializationException("Unhandled script object type in field " +
                                                 "member " +
                                                 "reflection!");
        }

        if (!serializer.isWriting())
            serializer.setPointer(reference, value);

    }

    @Override
    public int getAllocatedSize()
    {
        return 0;
    }

}
