package cwlib.structs.script;

import cwlib.enums.ModifierType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

import java.util.EnumSet;

public class FunctionDefinitionRow implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    public EnumSet<ModifierType> modifiers = EnumSet.noneOf(ModifierType.class);
    public int typeReferenceIdx;
    public int nameStringIdx;
    public int argumentsBegin, argumentsEnd;
    public int bytecodeBegin, bytecodeEnd;
    public int lineNosBegin, lineNosEnd;
    public int localVariablesBegin, localVariablesEnd;
    public int stackSize;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        if (version >= 0x3d9 && serializer.isWriting())
        {
            argumentsEnd -= argumentsBegin;
            bytecodeEnd -= bytecodeBegin;
            lineNosEnd -= lineNosBegin;
            localVariablesEnd -= localVariablesBegin;
        }

        if (serializer.isWriting())
        {
            short flags = ModifierType.getFlags(modifiers);
            if (version >= 0x3d9) serializer.getOutput().i16(flags);
            else serializer.getOutput().i32(flags);
        }
        else
        {
            int flags = (version >= 0x3d9) ? serializer.getInput().i16() :
                serializer.getInput().i32();
            modifiers = ModifierType.fromValue(flags);
        }

        typeReferenceIdx = serializer.i32(typeReferenceIdx);
        nameStringIdx = serializer.i32(nameStringIdx);

        if (version >= 0x3d9)
        {
            argumentsBegin = serializer.u16(argumentsBegin);
            argumentsEnd = serializer.u16(argumentsEnd);
            bytecodeBegin = serializer.u16(bytecodeBegin);
            bytecodeEnd = serializer.u16(bytecodeEnd);
            lineNosBegin = serializer.u16(lineNosBegin);
            lineNosEnd = serializer.u16(lineNosEnd);
            localVariablesBegin = serializer.u16(localVariablesBegin);
            localVariablesEnd = serializer.u16(localVariablesEnd);
        }
        else
        {
            argumentsBegin = serializer.i32(argumentsBegin);
            argumentsEnd = serializer.i32(argumentsEnd);
            bytecodeBegin = serializer.i32(bytecodeBegin);
            bytecodeEnd = serializer.i32(bytecodeEnd);
            lineNosBegin = serializer.i32(lineNosBegin);
            lineNosEnd = serializer.i32(lineNosEnd);
            localVariablesBegin = serializer.i32(localVariablesBegin);
            localVariablesEnd = serializer.i32(localVariablesEnd);
        }

        stackSize = serializer.i32(stackSize);

        if (version >= 0x3d9)
        {
            argumentsEnd += argumentsBegin;
            bytecodeEnd += bytecodeBegin;
            lineNosEnd += lineNosBegin;
            localVariablesEnd += localVariablesBegin;
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return FunctionDefinitionRow.BASE_ALLOCATION_SIZE;
    }
}
