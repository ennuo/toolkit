package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class LoadConstInstructionNullSafePtr extends Instruction
{
    /**
     * Address in stack to store constant.
     */
    public short dest;

    public LoadConstInstructionNullSafePtr(InstructionType type)
    {
        super(type);
    }

    public LoadConstInstructionNullSafePtr(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));
        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
