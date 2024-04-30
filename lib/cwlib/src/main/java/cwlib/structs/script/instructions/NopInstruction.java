package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class NopInstruction extends Instruction
{
    public NopInstruction(InstructionType type)
    {
        super(type);
    }

    public NopInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));
    }

    @Override
    public long getBits()
    {
        return this.getInstructionType().getValue();
    }
}
