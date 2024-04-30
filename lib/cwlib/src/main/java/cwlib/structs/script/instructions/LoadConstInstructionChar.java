package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class LoadConstInstructionChar extends Instruction
{
    /**
     * Constant value.
     */
    public char value;

    /**
     * Address in stack to store constant.
     */
    public short dest;

    public LoadConstInstructionChar(InstructionType type)
    {
        super(type);
    }

    public LoadConstInstructionChar(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.value = (char) ((bits >>> 48) & 0xffff);
        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) this.value & 0xffff) << 48) |
               (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
