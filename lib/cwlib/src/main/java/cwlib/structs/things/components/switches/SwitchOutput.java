package cwlib.structs.things.components.switches;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class SwitchOutput implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public SwitchSignal activation = new SwitchSignal();
    public SwitchTarget[] targetList;

    @GsonRevision(min = 0x34d)
    public String userDefinedName;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        activation = serializer.struct(activation, SwitchSignal.class);
        targetList = serializer.array(targetList, SwitchTarget.class);
        if (version >= 0x34d)
            userDefinedName = serializer.wstr(userDefinedName);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = SwitchOutput.BASE_ALLOCATION_SIZE;
        if (this.userDefinedName != null) size += (this.userDefinedName.length() * 2);
        return size;
    }
}
