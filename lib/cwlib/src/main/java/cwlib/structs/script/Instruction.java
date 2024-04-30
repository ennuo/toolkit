package cwlib.structs.script;

import cwlib.enums.InstructionClass;
import cwlib.enums.InstructionType;

public abstract class Instruction
{
    private final InstructionType instruction;
    public transient boolean visited = false;

    protected Instruction(InstructionType type)
    {
        this.instruction = type;
    }

    public InstructionType getInstructionType()
    {
        return this.instruction;
    }

    public InstructionClass getInstructionClass()
    {
        return this.instruction.getInstructionClass();
    }

    public abstract long getBits();

    @Override
    public String toString()
    {
        return this.instruction.name();
    }

    public static void main(String[] args)
    {
        System.out.println(InstructionType.ARRAY_ERASE.getInstructionClass());
    }
}
