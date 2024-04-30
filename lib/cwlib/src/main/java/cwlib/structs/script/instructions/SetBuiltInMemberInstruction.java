package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class SetBuiltInMemberInstruction extends Instruction
{
    /**
     * Address of object in stack.
     */
    public short base;

    /**
     * Address of value in stack.
     */
    public short source;

    public SetBuiltInMemberInstruction(InstructionType type)
    {
        super(type);
    }

    public SetBuiltInMemberInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.base = (short) ((bits >>> 32) & 0xffff);
        this.source = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) this.base & 0xffff) << 32) |
               (((long) (this.source & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
