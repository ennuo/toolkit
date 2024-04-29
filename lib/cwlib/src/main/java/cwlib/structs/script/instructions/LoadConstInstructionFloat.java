package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class LoadConstInstructionFloat extends Instruction
{
    /**
     * Constant value.
     */
    public float value;

    /**
     * Address in stack to store constant.
     */
    public short dest;

    public LoadConstInstructionFloat(InstructionType type)
    {
        super(type);
    }

    public LoadConstInstructionFloat(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.value = Float.intBitsToFloat((int) (bits >>> 32));
        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) Float.floatToIntBits(this.value)) << 32) |
               (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
