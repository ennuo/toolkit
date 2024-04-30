package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.enums.MachineType;
import cwlib.structs.script.Instruction;

public class SetMemberInstruction extends Instruction
{
    /**
     * Index of field reference.
     */
    public short field;

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

    public SetMemberInstruction(InstructionType type)
    {
        super(type);
    }

    public SetMemberInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.field = (short) ((bits >>> 48) & 0xffff);
        this.base = (short) ((bits >>> 32) & 0xffff);
        this.source = (short) ((bits >>> 16) & 0xffff);
        this.type = MachineType.fromValue((int) ((bits >>> 8) & 0xff));
    }

    @Override
    public long getBits()
    {
        return (((long) this.field & 0xffff) << 48) |
               (((long) this.base & 0xffff) << 32) |
               (((long) (this.source & 0xffff)) << 16) |
               (((long) (this.type.getValue())) << 8) |
               ((long) (this.getInstructionType().getValue()));
    }
}
