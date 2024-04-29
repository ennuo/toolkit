package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class GetBuiltInMemberInstruction extends Instruction
{
    /**
     * Address of value in stack.
     */
    public short base;

    /**
     * Address in stack to store result
     */
    public short dest;

    public GetBuiltInMemberInstruction(InstructionType type)
    {
        super(type);
    }

    public GetBuiltInMemberInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.base = (short) ((bits >>> 32) & 0xffff);
        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) this.base & 0xffff) << 32) |
               (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
