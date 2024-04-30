package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class LoadConstInstructionInt extends Instruction
{
    /**
     * Constant value.
     */
    public int value;

    /**
     * Address in stack to store constant.
     */
    public short dest;

    public LoadConstInstructionInt(InstructionType type)
    {
        super(type);
    }

    public LoadConstInstructionInt(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.value = (int) (bits >>> 32);
        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) this.value) << 32) |
               (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
