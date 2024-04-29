package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.structs.script.Instruction;

public class CastInstruction extends Instruction
{
    /**
     * Index of type reference
     */
    public short type;

    /**
     * Address of value in stack.
     */
    public short source;

    /**
     * Address in stack to store result
     */
    public short dest;

    public CastInstruction(InstructionType type)
    {
        super(type);
    }

    public CastInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.type = (short) ((bits >>> 48) & 0xffff);
        this.source = (short) ((bits >>> 32) & 0xffff);
        this.dest = (short) ((bits >>> 16) & 0xffff);
    }

    @Override
    public long getBits()
    {
        return (((long) this.type & 0xffff) << 48) |
               (((long) this.source & 0xffff) << 32) |
               (((long) (this.dest & 0xffff)) << 16) |
               ((long) (this.getInstructionType().getValue()));
    }
}
