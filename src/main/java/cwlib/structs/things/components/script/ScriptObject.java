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
public class ScriptObject implements Serializable {
    public ScriptObjectType type = ScriptObjectType.NULL;
    public Object value;

    @SuppressWarnings("unchecked")
    @Override public ScriptObject serialize(Serializer serializer, Serializable structure) {
        ScriptObject object = (structure == null) ? new ScriptObject() : (ScriptObject) structure;

        object.type = serializer.enum32(object.type);
        if (object.type == ScriptObjectType.NULL) return object;

        if (object.type == ScriptObjectType.INSTANCE) {
            object.value = serializer.reference((ScriptInstance) object.value, ScriptInstance.class);
            return object;
        }

        int reference = 0;
        if (serializer.isWriting()) {
            if (object.value != null)
                reference = serializer.getNextReference();
            serializer.getOutput().i32(reference);
        }
        else reference = serializer.getInput().i32();
        if (reference == 0) return object;
        
        if (!serializer.isWriting()) {
            Object value = serializer.getPointer(reference);
            if (value != null) {
                object.value = value;
                return object;
            }
        }

        serializer.log("" + object.type);
        switch (object.type) {
            case NULL: object.value = null; break;
            case ARRAY_BOOL: object.value = serializer.boolarray((boolean[]) object.value); break;
            case ARRAY_S32: object.value = serializer.intvector((int[]) object.value); break;
            case ARRAY_F32: object.value = serializer.floatarray((float[]) object.value); break;
            case ARRAY_VECTOR4: object.value = serializer.vectorarray((Vector4f[]) object.value); break;
            case STRINGW: object.value = serializer.wstr((String) object.value); break;
            case STRINGA: object.value = serializer.str((String) object.value); break;
            case RESOURCE: {
                ResourceDescriptor descriptor = (ResourceDescriptor) object.value;

                ResourceType type = ResourceType.INVALID;
                if (serializer.isWriting()) type = descriptor.getType();
                type = serializer.enum32(type);

                if (type != ResourceType.INVALID) {
                    if (serializer.isWriting())
                        serializer.resource((ResourceDescriptor) object.value, type);
                    else
                        object.value = serializer.resource(null, type);
                }

                break;
            }
            // case INSTANCE: object.value = serializer.reference((PScript) object.value, PScript.class); break;
            case AUDIOHANDLE: object.value = null; break;
            case ARRAY_SAFE_PTR: object.value = serializer.array((Thing[]) object.value, Thing.class, true); break;
            case ARRAY_OBJECT_REF: {
                ScriptObject[] array = (ScriptObject[]) object.value;
                if (!serializer.isWriting()) {
                    array = new ScriptObject[serializer.getInput().i32()];
                    serializer.setPointer(reference, array);
                }
                else serializer.getOutput().i32(array.length);
                for (int i = 0; i < array.length; ++i)
                    array[i] = serializer.struct(array[i], ScriptObject.class);
                return object;
            }
            default: throw new SerializationException("Unhandled script object type in field member reflection!");
        }

        if (!serializer.isWriting())
            serializer.setPointer(reference, object.value);
        
        return object;
    }
    
    @Override public int getAllocatedSize() { return 0; }

}
