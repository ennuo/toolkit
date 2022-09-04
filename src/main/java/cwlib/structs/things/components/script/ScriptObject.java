package cwlib.structs.things.components.script;

import cwlib.enums.ResourceType;
import cwlib.enums.ScriptObjectType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PScript;
import cwlib.types.data.ResourceDescriptor;

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
            case AUDIOHANDLE: object.value = null; break;
            case ARRAY_S32: object.value = serializer.intvector((int[]) object.value); break;
            case ARRAY_F32: object.value = serializer.floatarray((float[]) object.value); break;
            // case INSTANCE: object.value = serializer.reference((PScript) object.value, PScript.class); break;
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
            case ARRAY_SAFE_PTR: object.value = serializer.array((Thing[]) object.value, Thing.class, true); break;
            case ARRAY_OBJECT_REF: {
                Object[] array = (Object[]) object.value;
                if (!serializer.isWriting()) array = new Object[serializer.getInput().i32()];
                else serializer.getOutput().i32(array.length);
                for (int i = 0; i < array.length; ++i)
                    array[i] = serializer.struct((ScriptObject) array[i], ScriptObject.class);
                break;
            }
            default: throw new SerializationException("Unhandled script object type in field member reflection!");
        }

        serializer.setPointer(reference, object.value);
        
        return object;
    }
    
    @Override public int getAllocatedSize() { return 0; }

}
