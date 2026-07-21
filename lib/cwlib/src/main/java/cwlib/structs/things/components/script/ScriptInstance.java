package cwlib.structs.things.components.script;

import java.util.Arrays;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.BuiltinType;
import cwlib.enums.MachineType;
import cwlib.enums.ModifierType;
import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.ScriptInstanceSerializer;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;

@JsonAdapter(ScriptInstanceSerializer.class)
public class ScriptInstance implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ResourceDescriptor script;
    public InstanceLayout layout = new InstanceLayout();
    public HashMap<String, Object> memberData = new HashMap<>();

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        script = serializer.resource(script, ResourceType.SCRIPT);

        boolean serialize = true;
        if (version > 0x1a0)
        {
            if (serializer.isWriting()) serialize = layout != null;
            serialize = serializer.bool(serialize);
        }

        if (!serialize)
        {
            layout = null;
            return;
        }
        
        layout = serializer.reference(layout, InstanceLayout.class);

        boolean reflectDivergent = false;
        if (version > 0x19c)
            reflectDivergent = serializer.bool(reflectDivergent);
        FieldLayoutDetails[] fields =
            layout.getFieldsForReflection(reflectDivergent);
        boolean writing = serializer.isWriting();

        serializer.log(Arrays.toString(fields));
        for (FieldLayoutDetails field : fields)
        {
            if (0x198 < version && version < 0x19d) serializer.u8(0);
            serializer.log(field.name + " " + field.machineType);
            
            Object value = memberData.get(field.name);
            switch (field.machineType)
            {
                case BOOL:
                    value = serializer.bool(writing && (boolean) value);
                    break;
                case CHAR:
                    value = serializer.i16(writing ? (short) value : 0);
                    break;
                case S32:
                    value = serializer.i32(writing ? (int) value : 0);
                    if (serializer.isWriting() && field.fishType == BuiltinType.GUID && ((int) value) != 0)
                    {
                        if (field.name != null && field.name.equals("FSB"))
                            serializer.addDependency(new ResourceDescriptor(new GUID(((int) value) & 0xffffffffL), ResourceType.FILENAME));
                        else if (field.name != null && field.name.equals(
                            "SettingsFile"))
                            serializer.addDependency(new ResourceDescriptor(new GUID(((int) value) & 0xffffffffL), ResourceType.MUSIC_SETTINGS));
                        else
                            serializer.addDependency(new ResourceDescriptor(new GUID(((int) value) & 0xffffffffL), ResourceType.FILE_OF_BYTES));
                    }
                    break;
                case F32:
                    value = serializer.f32(writing ? (float) value : 0);
                    break;
                case V4:
                    value = serializer.v4((Vector4f) value);
                    break;
                case M44:
                    value = serializer.m44((Matrix4f) value);
                    break;
                case OBJECT_REF:
                    value = serializer.struct((ScriptObjectUID) value, ScriptObjectUID.class);
                    break;
                case SAFE_PTR:
                    value = serializer.reference((Thing) value, Thing.class);
                    break;
                default:
                    throw new SerializationException("Unhandled machine type in " +
                                                        "field member " +
                                                        "reflection!");
            }

            if (!serializer.isWriting()) memberData.put(field.name, value);
        }
    }

    private FieldLayoutDetails addFieldInternal(String name, BuiltinType fishType, MachineType machineType, Object value)
    {
        memberData.put(name, value);

        // Return the existing field if it already exists
        for (FieldLayoutDetails field : layout.fields)
        {
            if (field.name.equals(name))
                return field;
        }

        // Otherwise create the field
        int instanceOffset = layout.instanceSize;
        int memberSize;
        switch (machineType)
        {
            case VOID: memberSize = 0; break;
            case BOOL: memberSize = 1; break;
            case CHAR: memberSize = 2; break;
            case V4: memberSize = 16; break;
            case M44: memberSize = 64; break;
            case S64: memberSize = 8; break;
            case F64: memberSize = 8; break;
            default: memberSize = 4; break;
        }

        if ((instanceOffset % memberSize) != 0)
            instanceOffset += (instanceOffset - (instanceOffset % memberSize));

    
        FieldLayoutDetails field = new FieldLayoutDetails();

        field.modifiers.add(ModifierType.PUBLIC); // Modifiers don't really matter, just make it public by default, VM will correct it
        field.name = name;
        field.instanceOffset = instanceOffset;
        field.fishType = fishType;
        field.machineType = machineType;

        layout.instanceSize = instanceOffset + memberSize;
        layout.fields.add(field);

        return field;
    }

    public void addField(String name, boolean value)
    {
        addFieldInternal(name, BuiltinType.BOOL, MachineType.BOOL, value);
    }

    public void addField(String name, GUID value)
    {
        addFieldInternal(name, BuiltinType.GUID, MachineType.S32, (int) value.getValue());
    }

    public void addField(String name, Thing value)
    {
        addFieldInternal(name, BuiltinType.VOID, MachineType.SAFE_PTR, value);
    }

    public void addField(String name, int value)
    {
        addFieldInternal(name, BuiltinType.S32, MachineType.S32, value);
    }

    public void addField(String name, float value)
    {
        addFieldInternal(name, BuiltinType.F32, MachineType.F32, value);
    }

    public void unsetField(String name)
    {
        for (FieldLayoutDetails field : layout.fields)
        {
            if (field.name.equals(name))
            {
                Object unset = null;
                switch (field.machineType)
                {
                    case BOOL:
                        unset = false;
                        break;
                    case CHAR:
                        unset = (short) 0;
                    case S32:
                        unset = 0;
                        break;
                    case F32:
                        unset = 0.0f;
                        break;
                    case V4:
                        unset = new Vector4f().zero();
                        break;
                    case M44:
                        unset = new Matrix4f().identity();
                        break;
                    case OBJECT_REF:
                        unset = new ScriptObjectUID();
                        break;
                    default:
                        unset = null;
                        break;
                }

                memberData.put(name, unset);
                break;
            }
        }
    }

    public FieldLayoutDetails getField(String name)
    {
        if (this.layout == null) return null;
        for (FieldLayoutDetails details : layout.fields)
        {
            if (details.name.equals(name))
                return details;
        }
        return null;
    }

    public void setField(String name, Object value)
    {
        if (getField(name) == null)
            throw new IllegalArgumentException(name + " does not exist on the script instance!");
        memberData.put(name, value);
    }

    @Override
    public int getAllocatedSize()
    {
        return ScriptInstance.BASE_ALLOCATION_SIZE;
    }
}
