package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.enums.MachineType;
import cwlib.structs.script.Instruction;

public class CallInstruction extends Instruction
{
    /**
     * Index of function reference
     */
    public short call;

    /**
     * Address in stack to store result
     */
    public short dest;

    /**
     * Return type of function to call.
     */
    public MachineType type;

    public CallInstruction(InstructionType type)
    {
        super(type);
    }

    public CallInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.call = (short) ((bits >>> 32) & 0xffff);
        this.dest = (short) ((bits >>> 16) & 0xffff);
        this.type = MachineType.fromValue((int) ((bits >>> 8) & 0xff));
    }

    @Override
    public long getBits()
    {
        return (((long) this.call) << 32) |
               (((long) (this.dest & 0xffff)) << 16) |
               (((long) (this.type.getValue() & 0xff)) << 8) |
               ((long) (this.getInstructionType().getValue() & 0xff));
    }
}
