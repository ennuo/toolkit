package cwlib.structs.things.components.script;

import java.util.Arrays;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.enums.BuiltinType;
import cwlib.enums.MachineType;
import cwlib.enums.ModifierType;
import cwlib.enums.ResourceType;
import cwlib.enums.ScriptObjectType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;

public class ScriptInstance implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ResourceDescriptor script;
    public InstanceLayout instanceLayout = new InstanceLayout();

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        script = serializer.resource(script, ResourceType.SCRIPT);

        boolean serialize = true;
        if (version > 0x1a0)
        {
            if (serializer.isWriting()) serialize = instanceLayout != null;
            serialize = serializer.bool(serialize);
        }

        if (serialize)
        {
            int reference = 0;
            if (serializer.isWriting())
            {
                if (instanceLayout != null)
                    reference = serializer.getNextReference();
                serializer.getOutput().i32(reference);
            }
            else reference = serializer.getInput().i32();
            if (reference == 0) return;

            if (!serializer.isWriting())
            {
                InstanceLayout layout = serializer.getPointer(reference);
                if (layout == null)
                {
                    layout = new InstanceLayout();
                    instanceLayout = layout;
                    serializer.setPointer(reference, layout);
                    layout.serialize(serializer);
                }
                else
                    instanceLayout = new InstanceLayout(layout);
            }
            else instanceLayout.serialize(serializer);

            boolean reflectDivergent = false;
            if (version > 0x19c)
                reflectDivergent = serializer.bool(reflectDivergent);
            FieldLayoutDetails[] fields =
                instanceLayout.getFieldsForReflection(reflectDivergent);
            boolean writing = serializer.isWriting();


            serializer.log(Arrays.toString(fields));
            for (FieldLayoutDetails field : fields)
            {
                if (0x198 < version && version < 0x19d) serializer.u8(0);
                serializer.log(field.name + " " + field.machineType);
                switch (field.machineType)
                {
                    case BOOL:
                        field.value = serializer.bool(writing && (boolean) field.value);
                        break;
                    case CHAR:
                        field.value = serializer.i16(writing ? (short) field.value : 0);
                        break;
                    case S32:
                        field.value = serializer.i32(writing ? (int) field.value : 0);
                        if (serializer.isWriting() && field.fishType == BuiltinType.GUID && ((int) field.value) != 0)
                        {
                            if (field.name != null && field.name.equals("FSB"))
                                serializer.addDependency(new ResourceDescriptor(new GUID(((int) field.value) & 0xffffffffL), ResourceType.FILENAME));
                            else if (field.name != null && field.name.equals(
                                "SettingsFile"))
                                serializer.addDependency(new ResourceDescriptor(new GUID(((int) field.value) & 0xffffffffL), ResourceType.MUSIC_SETTINGS));
                            else
                                serializer.addDependency(new ResourceDescriptor(new GUID(((int) field.value) & 0xffffffffL), ResourceType.FILE_OF_BYTES));
                        }
                        break;
                    case F32:
                        field.value = serializer.f32(writing ? (float) field.value : 0);
                        break;
                    case V4:
                        field.value = serializer.v4((Vector4f) field.value);
                        break;
                    case M44:
                        field.value = serializer.m44((Matrix4f) field.value);
                        break;
                    case OBJECT_REF:
                        field.value = serializer.struct((ScriptObject) field.value,
                            ScriptObject.class);
                        break;
                    case SAFE_PTR:
                        field.value = serializer.reference((Thing) field.value,
                            Thing.class);
                        break;
                    default:
                        throw new SerializationException("Unhandled machine type in " +
                                                         "field member " +
                                                         "reflection!");
                }
            }
        }
    }

    public void addField(String name, GUID value)
    {
        FieldLayoutDetails field = new FieldLayoutDetails();
        field.modifiers.add(ModifierType.PUBLIC);
        field.name = name;
        field.instanceOffset = this.instanceLayout.instanceSize;
        field.fishType = BuiltinType.GUID;
        field.machineType = MachineType.S32;
        field.value = (int) value.getValue();
        this.instanceLayout.instanceSize += 4;
        this.instanceLayout.fields.add(field);
    }

    public void addField(String name, Thing value)
    {
        FieldLayoutDetails field = new FieldLayoutDetails();
        field.modifiers.add(ModifierType.PUBLIC);
        field.name = name;
        field.instanceOffset = this.instanceLayout.instanceSize;
        field.fishType = BuiltinType.VOID;
        field.machineType = MachineType.SAFE_PTR;
        field.value = value;
        this.instanceLayout.instanceSize += 4;
        this.instanceLayout.fields.add(field);
    }

    public void unsetField(String name)
    {
        for (FieldLayoutDetails field : instanceLayout.fields)
        {
            if (field.name.equals(name))
            {
                switch (field.machineType)
                {
                    case BOOL:
                        field.value = false;
                        break;
                    case CHAR:
                        field.value = (short) 0;
                    case S32:
                        field.value = 0;
                        break;
                    case F32:
                        field.value = 0.0f;
                        break;
                    case V4:
                        field.value = new Vector4f();
                        break;
                    case M44:
                        field.value = new Matrix4f().identity();
                        break;
                    case OBJECT_REF:
                        field.value = new ScriptObject(ScriptObjectType.NULL, null);
                        break;
                    default:
                        field.value = null;
                        break;
                }
            }
        }
    }

    public FieldLayoutDetails getField(String name)
    {
        if (this.instanceLayout == null) return null;
        for (FieldLayoutDetails details : instanceLayout.fields)
        {
            if (details.name.equals(name))
                return details;
        }
        return null;
    }

    public void setField(String name, Object value)
    {
        FieldLayoutDetails field = this.getField(name);
        if (field != null)
            field.value = value;
    }

    @Override
    public int getAllocatedSize()
    {
        return ScriptInstance.BASE_ALLOCATION_SIZE;
    }
}
