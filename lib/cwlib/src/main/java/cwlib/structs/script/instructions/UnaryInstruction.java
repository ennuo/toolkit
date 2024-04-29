package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class UnaryInstruction extends Instruction
{
    /**
     * Address of value in stack.
     */
    public short source;

    /**
     * Address in stack to store result
     */
    public short dest;

    public UnaryInstruction(InstructionType type)
    {
        super(type);
    }

    public UnaryInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.source = (short) ((bits >>> 32) & 0xffff);
        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) this.source & 0xffff) << 32) |
               (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
