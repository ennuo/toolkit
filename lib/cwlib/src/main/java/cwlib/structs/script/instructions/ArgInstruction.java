package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.enums.MachineType;
import cwlib.structs.script.Instruction;

public class ArgInstruction extends Instruction
{
    /**
     * Address to store value in argument list.
     */
    public short argument = -1;

    /**
     * Address of value in stack.
     */
    public short source = -1;

    /**
     * Type of value being stored.
     */
    public MachineType type = MachineType.VOID;

    public ArgInstruction(InstructionType type)
    {
        super(type);
    }

    public ArgInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.argument = (short) ((bits >>> 32) & 0xffff);
        this.source = (short) ((bits >>> 16) & 0xffff);
        this.type = MachineType.fromValue((int) ((bits >> 8) & 0xff));
    }

    @Override
    public long getBits()
    {
        long arg = this.argument & 0xffff;
        long src = this.source & 0xffff;
        long type = this.type.getValue();
        long op = this.getInstructionType().getValue();

        return (arg << 32) | (src << 16) | (type << 8) | op;
    }
}
