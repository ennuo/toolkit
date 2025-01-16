package cwlib.structs.things.components.script;

import org.joml.Vector4f;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.ResourceType;
import cwlib.enums.ScriptObjectType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.ScriptObjectUIDSerializer;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

@JsonAdapter(ScriptObjectUIDSerializer.class)
public class ScriptObjectUID implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public ScriptObjectType type = ScriptObjectType.NULL;
    public ScriptObject object;

    public ScriptObjectUID() {}
    public ScriptObjectUID(ScriptObjectType type, ScriptObject value)
    {
        this.type = type;
        this.object = value;
    }
    public ScriptObjectUID(ScriptObjectType type, Object value)
    {
        this.type = type;
        this.object = new ScriptObject(value);
    }
    
    @Override
    public void serialize(Serializer serializer) 
    {
        if (serializer.isWriting() && object == null)
            type = ScriptObjectType.NULL;

        type = serializer.enum32(type);
        if (type == ScriptObjectType.NULL) return;

        if (serializer.isWriting())
        {
            int reference = serializer.getNextReference();
            serializer.getOutput().i32(reference);
            if (serializer.getVisited(reference) != null) return;
            serializer.setVisited(reference, object);
        }
        else
        {
            int reference = serializer.getInput().i32();
            if (reference == 0)
            {
                type = ScriptObjectType.NULL;
                return;
            }

            object = serializer.getVisited(reference);
            if (object == null)
            {
                object = new ScriptObject();
                serializer.setVisited(reference, object);
            }
            else return;
        }

        switch (type)
        {
            case ARRAY_BOOL:
                object.value = serializer.boolarray((boolean[]) object.value);
                break;
            case ARRAY_S32:
                object.value = serializer.intvector((int[]) object.value, true);
                break;
            case ARRAY_F32:
                object.value = serializer.floatarray((float[]) object.value);
                break;
            case ARRAY_VECTOR4:
                object.value = serializer.vectorarray((Vector4f[]) object.value);
                break;
            case INSTANCE:
                object.value = serializer.struct((ScriptInstance) object.value, ScriptInstance.class);
                break;
            case STRINGW:
                object.value = serializer.wstr((String) object.value);
                break;
            case STRINGA:
                object.value = serializer.str((String) object.value);
                break;
            case RESOURCE:
            {
                ResourceDescriptor descriptor = (ResourceDescriptor) object.value;

                ResourceType type = ResourceType.INVALID;
                if (serializer.isWriting() && descriptor != null) type = descriptor.getType();
                type = serializer.enum32(type);

                if (type != ResourceType.INVALID)
                    object.value = serializer.resource((ResourceDescriptor) object.value, type);

                break;
            }
            case AUDIOHANDLE:
                break;
            case ARRAY_SAFE_PTR:
                object.value = serializer.array((Thing[]) object.value, Thing.class, true);
                break;
            case ARRAY_OBJECT_REF:
                object.value = serializer.array((ScriptObjectUID[]) object.value, ScriptObjectUID.class);
                break;
            default:
                throw new SerializationException("Unhandled script object type in field " +
                                                 "member " +
                                                 "reflection!");
        }
    }

    @Override
    public int getAllocatedSize() 
    {
        return BASE_ALLOCATION_SIZE;
    }
}
