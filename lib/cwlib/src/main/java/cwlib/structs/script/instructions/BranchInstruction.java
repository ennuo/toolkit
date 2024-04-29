package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class BranchInstruction extends Instruction
{
    /**
     * Relative offset to branch to
     */
    public int offset;

    /**
     * Address of conditional result in stack.
     */
    public short source;

    public BranchInstruction(InstructionType type)
    {
        super(type);
    }

    public BranchInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.offset = (int) (bits >>> 32);
        this.source = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) this.offset) << 32) |
               (((long) (this.source & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
