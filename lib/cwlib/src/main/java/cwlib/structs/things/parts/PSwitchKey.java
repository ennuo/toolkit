package cwlib.structs.things.parts;

import cwlib.enums.SwitchKeyType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.switches.SwitchSignal;

public class PSwitchKey implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public int colorIndex;
    @GsonRevision(min = 0x2dc)
    public String name;

    @GsonRevision(min = 0x1bc)
    public boolean hideInPlayMode;
    @GsonRevision(lbp3 = true, min = 0x132)
    public boolean isDummy;

    @GsonRevision(min = 0x2a0, max = 0x2c3)
    @Deprecated
    public SwitchSignal isActive;
    @GsonRevision(min = 0x27d, max = 0x2db)
    @Deprecated
    public SwitchKeyType type = SwitchKeyType.MAGNETIC;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        colorIndex = serializer.s32(colorIndex);
        if (version >= 0x2dc)
            name = serializer.wstr(name);

        if (subVersion < 0x132 && version > 0x1bc)
        {
            if (version < 0x3ed) hideInPlayMode = serializer.bool(hideInPlayMode);
            else if (serializer.isWriting())
            {
                int flags = this.hideInPlayMode ? 0x80 : 0x0;
                if (this.isDummy) flags |= 0x40;
                serializer.getOutput().u8(flags);
            }
            else
            {
                int flags = serializer.getInput().u8();
                hideInPlayMode = (flags & 0x80) != 0;
                isDummy = (flags & 0x40) != 0;
            }
        }
        else
        {
            if (version > 0x1bc)
                hideInPlayMode = serializer.bool(hideInPlayMode);
            if (subVersion >= 0x132)
                isDummy = serializer.bool(isDummy);
        }

        // isActiveBoolOldForSerialisation
        if (version > 0x272 && version < 0x2c4)
        {
            if (serializer.isWriting())
            {
                boolean isActive = this.isActive != null;
                if (isActive) isActive = this.isActive.activation != 0.0f;
                serializer.getOutput().bool(isActive);
            }
            else serializer.getInput().bool();
        }

        if (version > 0x29f && version < 0x2c4)
            serializer.struct(isActive, SwitchSignal.class);

        if (version > 0x27c && version < 0x2dc)
            serializer.enum32(type);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PSwitchKey.BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += (this.name.length() * 2);
        return size;
    }
}
