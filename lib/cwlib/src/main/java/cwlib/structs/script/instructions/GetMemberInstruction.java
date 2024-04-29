package cwlib.structs.script.instructions;

import cwlib.enums.InstructionType;
import cwlib.enums.MachineType;
import cwlib.structs.script.Instruction;

public class GetMemberInstruction extends Instruction
{
    /**
     * Address of index or source in the stack.
     */
    public short field;

    /**
     * Address of object in stack.
     */
    public short base;

    /**
     * Address in stack to store result
     */
    public short dest;

    /**
     * Internal type of element.
     */
    public MachineType type = MachineType.VOID;

    public GetMemberInstruction(InstructionType type)
    {
        super(type);
    }

    public GetMemberInstruction(long bits)
    {
        super(InstructionType.fromValue((int) (bits & 0xff)));

        this.field = (short) ((bits >>> 48) & 0xffff);
        this.base = (short) ((bits >>> 32) & 0xffff);
        this.dest = (short) ((bits >>> 16) & 0xffff);
        this.type = MachineType.fromValue((int) ((bits >>> 8) & 0xff));
    }

    @Override
    public long getBits()
    {
        return (((long) this.field & 0xffff) << 48) |
               (((long) this.base & 0xffff) << 32) |
               (((long) (this.dest & 0xffff)) << 16) |
               (((long) (this.type.getValue())) << 8) |
               ((long) (this.getInstructionType().getValue()));
    }
}
