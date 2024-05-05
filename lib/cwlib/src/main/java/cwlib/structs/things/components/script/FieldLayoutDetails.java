package cwlib.structs.things.components.script;

import java.util.EnumSet;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.BuiltinType;
import cwlib.enums.MachineType;
import cwlib.enums.ModifierType;
import cwlib.io.Serializable;
import cwlib.io.gson.FieldSerializer;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;

@JsonAdapter(FieldSerializer.class)
public class FieldLayoutDetails implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public String name;
    public EnumSet<ModifierType> modifiers = EnumSet.noneOf(ModifierType.class);
    public MachineType machineType = MachineType.VOID;

    @GsonRevision(min = 0x145)
    public BuiltinType fishType = BuiltinType.VOID;

    public byte dimensionCount;
    public MachineType arrayBaseMachineType = MachineType.VOID;
    public int instanceOffset;

    public Object value;

    public FieldLayoutDetails() { }

    public FieldLayoutDetails(FieldLayoutDetails details)
    {
        this.name = details.name;
        this.modifiers = details.modifiers.clone();
        this.machineType = details.machineType;
        this.fishType = details.fishType;
        this.dimensionCount = details.dimensionCount;
        this.arrayBaseMachineType = details.arrayBaseMachineType;
        this.instanceOffset = details.instanceOffset;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        name = serializer.str(name);

        if (version >= 0x3d9)
        {
            if (serializer.isWriting())
            {
                MemoryOutputStream stream = serializer.getOutput();
                stream.i16(ModifierType.getFlags(modifiers));
                stream.u8(machineType.getValue());
                stream.u8(fishType.getValue());
                stream.i8(dimensionCount);
                stream.u8(arrayBaseMachineType.getValue());
            }
            else
            {
                MemoryInputStream stream = serializer.getInput();
                modifiers = ModifierType.fromValue(stream.i16());
                machineType = MachineType.fromValue(stream.u8());
                fishType = BuiltinType.fromValue(stream.u8());
                dimensionCount = stream.i8();
                arrayBaseMachineType = MachineType.fromValue(stream.u8());
            }

            instanceOffset = serializer.i32(instanceOffset);
            return;
        }

        if (serializer.isWriting()) serializer.getOutput().i32(ModifierType.getFlags(modifiers));
        else modifiers = ModifierType.fromValue(serializer.getInput().i32());
        
        machineType = serializer.enum32(machineType);
        if (version >= 0x145)
            fishType = serializer.enum32(fishType);

        dimensionCount = serializer.i8(dimensionCount);
        arrayBaseMachineType = serializer.enum32(arrayBaseMachineType);

        instanceOffset = serializer.i32(instanceOffset);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = FieldLayoutDetails.BASE_ALLOCATION_SIZE;
        if (this.name != null) size += (this.name.length());
        return size;
    }
}
