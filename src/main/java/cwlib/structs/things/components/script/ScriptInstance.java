package cwlib.structs.things.components.script;

import java.util.Arrays;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.enums.BuiltinType;
import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;

public class ScriptInstance implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;
    
    public ResourceDescriptor script;
    public InstanceLayout instanceLayout;

    @SuppressWarnings("unchecked")
    @Override public ScriptInstance serialize(Serializer serializer, Serializable structure) {
        ScriptInstance script = (structure == null) ? new ScriptInstance() : (ScriptInstance) structure;

        int version = serializer.getRevision().getVersion();
        
        script.script = serializer.resource(script.script, ResourceType.SCRIPT);

        boolean serialize = true;
        if (version > 0x1a0) {
            if (serializer.isWriting()) serialize = script.instanceLayout != null;
            serialize = serializer.bool(serialize);
        }

        if (serialize) {
            int reference = 0;
            if (serializer.isWriting()) {
                if (script.instanceLayout != null)
                    reference = serializer.getNextReference();
                serializer.getOutput().i32(reference);
            }
            else reference = serializer.getInput().i32();
            if (reference == 0) return script;
            
            if (!serializer.isWriting()) {
                InstanceLayout layout = serializer.getPointer(reference);
                if (layout == null) {
                    layout = new InstanceLayout();
                    script.instanceLayout = layout;
                    serializer.setPointer(reference, layout);
                    layout.serialize(serializer, layout);
                } else 
                    script.instanceLayout = new InstanceLayout(layout);
            } else script.instanceLayout.serialize(serializer, script.instanceLayout);
            
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
                    case S32: 
                        field.value = serializer.i32(writing ? (int) field.value : 0); 
                        if (serializer.isWriting() && field.fishType == BuiltinType.GUID && ((int)field.value) != 0) {
                            if (field.name != null && field.name.equals("FSB"))
                                serializer.addDependency(new ResourceDescriptor(new GUID((int)field.value), ResourceType.FILENAME));
                            else if (field.name != null && field.name.equals("SettingsFile"))
                                serializer.addDependency(new ResourceDescriptor(new GUID((int)field.value), ResourceType.MUSIC_SETTINGS));
                            else
                                serializer.addDependency(new ResourceDescriptor(new GUID((int)field.value), ResourceType.FILE_OF_BYTES));
                        }
                        break;
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

    @Override public int getAllocatedSize() { return ScriptInstance.BASE_ALLOCATION_SIZE; }
}
