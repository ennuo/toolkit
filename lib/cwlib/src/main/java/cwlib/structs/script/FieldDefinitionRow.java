package cwlib.structs.script;

import cwlib.enums.ModifierType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

import java.util.EnumSet;

public class FieldDefinitionRow implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public EnumSet<ModifierType> modifiers = EnumSet.noneOf(ModifierType.class);
    public int typeReferenceIdx;
    public int nameStringIdx;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        if (serializer.isWriting())
        {
            short flags = ModifierType.getFlags(modifiers);
            if (version >= 0x3d9) serializer.getOutput().i16(flags);
            else serializer.getOutput().i32(flags);
        }
        else
        {
            int flags = (version >= 0x3d9) ? serializer.getInput().i16() :
                serializer.getInput().i32();
            modifiers = ModifierType.fromValue(flags);
        }

        typeReferenceIdx = serializer.i32(typeReferenceIdx);
        nameStringIdx = serializer.i32(nameStringIdx);
    }

    @Override
    public int getAllocatedSize()
    {
        return FieldDefinitionRow.BASE_ALLOCATION_SIZE;
    }
}
