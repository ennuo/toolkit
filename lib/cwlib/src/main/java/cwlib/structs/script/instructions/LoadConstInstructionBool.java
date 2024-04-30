package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class LoadConstInstructionBool extends Instruction
{
    /**
     * Constant value.
     */
    public boolean value;

    /**
     * Address in stack to store constant.
     */
    public short dest;

    public LoadConstInstructionBool(InstructionType type)
    {
        super(type);
    }

    public LoadConstInstructionBool(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.value = (bits >>> 63) != 0;
        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return ((this.value ? 1L : 0L) << 63) |
               (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
