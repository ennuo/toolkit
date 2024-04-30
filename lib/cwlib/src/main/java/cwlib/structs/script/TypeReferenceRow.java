package cwlib.structs.script;

import cwlib.enums.BuiltinType;
import cwlib.enums.MachineType;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class TypeReferenceRow implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    public BuiltinType fishType = BuiltinType.VOID;
    public MachineType machineType = MachineType.VOID;
    public byte dimensionCount;
    public MachineType arrayBaseMachineType = MachineType.VOID;
    public ResourceDescriptor script;
    public int typeNameStringIdx;

    @Override
    public void serialize(Serializer serializer)
    {
        if (serializer.isWriting())
        {
            serializer.getOutput().u8(machineType == null ? 0 : machineType.getValue());
        }
        else machineType = MachineType.fromValue(serializer.getInput().u8());

        if (serializer.isWriting())
        {
            serializer.getOutput().u8(fishType == null ? 0 : fishType.getValue());
        }
        else fishType = BuiltinType.fromValue(serializer.getInput().u8());


        dimensionCount = serializer.i8(dimensionCount);

        if (serializer.isWriting())
        {
            serializer.getOutput().u8(arrayBaseMachineType == null ? 0 :
                arrayBaseMachineType.getValue());
        }
        else arrayBaseMachineType = MachineType.fromValue(serializer.getInput().u8());

        script = serializer.resource(script, ResourceType.SCRIPT);

        typeNameStringIdx = serializer.i32(typeNameStringIdx);
    }

    public boolean isArray()
    {
        return this.dimensionCount > 0;
    }

    public boolean isObjectType()
    {
        return this.script != null;
    }

    @Override
    public int getAllocatedSize()
    {
        return TypeReferenceRow.BASE_ALLOCATION_SIZE;
    }
}
