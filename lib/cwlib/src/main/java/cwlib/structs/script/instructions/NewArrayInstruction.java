package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class NewArrayInstruction extends Instruction
{
    /**
     * Index of type reference
     */
    public short type;

    /**
     * Address of size value in stack.
     */
    public short size;

    /**
     * Address in stack to store reference to created array.
     */
    public short dest;

    public NewArrayInstruction(InstructionType type)
    {
        super(type);
    }

    public NewArrayInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.type = (short) ((bits >>> 48) & 0xffff);
        this.size = (short) ((bits >>> 32) & 0xffff);
        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) this.type & 0xffff) << 48) |
               (((long) this.size & 0xffff) << 32) |
               (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
