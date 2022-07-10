package cwlib.structs.things.parts;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.script.FieldLayoutDetails;
import cwlib.structs.things.components.script.InstanceLayout;
import cwlib.structs.things.components.script.ScriptObject;
import cwlib.types.data.ResourceDescriptor;

public class PScript implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;
    
    public ResourceDescriptor script;
    public InstanceLayout instanceLayout;

    @SuppressWarnings("unchecked")
    @Override public PScript serialize(Serializer serializer, Serializable structure) {
        PScript script = (structure == null) ? new PScript() : (PScript) structure;

        int version = serializer.getRevision().getVersion();

        script.script = serializer.resource(script.script, ResourceType.SCRIPT);

        boolean serialize = true;
        if (version > 0x1a0) {
            if (serializer.isWriting()) serialize = script.instanceLayout != null;
            serialize = serializer.bool(serialize);
        }

        if (serialize) {
            script.instanceLayout = serializer.reference(script.instanceLayout, InstanceLayout.class);
            if (script.instanceLayout == null) return script;

            boolean reflectDivergent = false;
            if (version > 0x19c)
                reflectDivergent = serializer.bool(reflectDivergent);
            FieldLayoutDetails[] fields = script.instanceLayout.getFieldsForReflection(reflectDivergent);
            boolean writing = serializer.isWriting();


            serializer.log(Arrays.toString(fields));
            for (FieldLayoutDetails field : fields) {
                if (0x198 < version && version < 0x19d) serializer.u8(0);
                serializer.log(field.name + " " + field.machineType);
                switch (field.machineType) {
                    case BOOL: field.value = serializer.bool(writing ? (boolean) field.value : false); break;
                    case CHAR: field.value = serializer.i8(writing ? (byte) field.value : 0); break;
                    case S32: field.value = serializer.i32(writing ? (int) field.value : 0); break;
                    case F32: field.value = serializer.f32(writing ? (float) field.value : 0); break;
                    case V4: field.value = serializer.v4((Vector4f) field.value); break;
                    case M44: field.value = serializer.m44((Matrix4f) field.value); break;
                    case OBJECT_REF: field.value = serializer.struct((ScriptObject) field.value, ScriptObject.class); break;
                    case SAFE_PTR: field.value = serializer.reference((Thing) field.value, Thing.class); break;
                    default: throw new SerializationException("Unhandled machine type in field member reflection!");
                }
            }


            // serializer.log("SCRIPT STRUCTURE NOT FINISHED!");
            // System.exit(0);
        }
        
        return script;
    }

    @Override public int getAllocatedSize() { return PScript.BASE_ALLOCATION_SIZE; }
}
