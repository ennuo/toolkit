package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class LoadConstInstruction extends Instruction
{
    /**
     * Index of constant.
     */
    public int index;

    /**
     * Address in stack to store constant.
     */
    public short dest;

    public LoadConstInstruction(InstructionType type)
    {
        super(type);
    }

    public LoadConstInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.index = (int) (bits >>> 32);
        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) this.index) << 32) |
               (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
