package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class NewObjectInstruction extends Instruction
{
    /**
     * Index of type reference.
     */
    public short type;

    /**
     * Address in stack to store reference to created object.
     */
    public short dest;

    public NewObjectInstruction(InstructionType type)
    {
        super(type);
    }

    public NewObjectInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.type = (short) ((bits >>> 48) & 0xffff);
        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) this.type & 0xffff) << 48) |
               (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
