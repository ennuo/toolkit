package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class BinaryInstruction extends Instruction
{
    /**
     * Address of B in stack
     */
    public short b = -1;

    /**
     * Address of A in stack
     */
    public short a = -1;

    /**
     * Address in stack to store result
     */
    public short dest = -1;

    public BinaryInstruction(InstructionType type)
    {
        super(type);
    }

    public BinaryInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        int source = (int) (bits >>> 32);
        this.a = (short) (source & 0xffff);
        this.b = (short) ((source >>> 16) & 0xffff);

        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((((long) this.b & 0xffff) << 16) | ((long) (this.a & 0xffff))) << 32) |
               (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
