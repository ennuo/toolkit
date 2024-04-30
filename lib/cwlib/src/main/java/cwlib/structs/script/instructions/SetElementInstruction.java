package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.enums.MachineType;
import cwlib.structs.script.Instruction;

public class SetElementInstruction extends Instruction
{
    /**
     * Address of element index in stack.
     */
    public short index;

    /**
     * Address of object in stack.
     */
    public short base;

    /**
     * Address of value in stack.
     */
    public short source;

    /**
     * Internal type of element.
     */
    public MachineType type = MachineType.VOID;

    public SetElementInstruction(InstructionType type)
    {
        super(type);
    }

    public SetElementInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.index = (short) ((bits >>> 48) & 0xffff);
        this.base = (short) ((bits >>> 32) & 0xffff);
        this.source = (short) ((bits >>> 16) & 0xffff);
        this.type = MachineType.fromValue((int) ((bits >>> 8) & 0xff));
    }

    @Override
    public long getBits()
    {
        return (((long) this.index & 0xffff) << 48) |
               (((long) this.base & 0xffff) << 32) |
               (((long) (this.source & 0xffff)) << 16) |
               (((long) (this.type.getValue())) << 8) |
               ((long) (this.getInstructionType().getValue()));
    }
}
