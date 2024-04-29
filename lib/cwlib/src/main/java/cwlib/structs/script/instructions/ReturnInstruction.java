package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.enums.MachineType;
import cwlib.structs.script.Instruction;

public class ReturnInstruction extends Instruction
{
    /**
     * Address of value in stack.
     */
    public short source;

    /**
     * Internal type of source value
     */
    public MachineType type = MachineType.VOID;

    public ReturnInstruction(InstructionType type)
    {
        super(type);
    }

    public ReturnInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.source = (short) ((bits >>> 32) & 0xffff);
        this.type = MachineType.fromValue((int) ((bits >>> 8) & 0xff));
    }

    @Override
    public long getBits()
    {
        return (((long) this.source & 0xffff) << 32) |
               (((long) (this.type.getValue())) << 8) |
               ((long) (this.getInstructionType().getValue()));
    }
}
