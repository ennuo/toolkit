package cwlib.structs.things.components.switches;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class SwitchOutput implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public SwitchSignal activation = new SwitchSignal();
    public SwitchTarget[] targetList;

    @GsonRevision(min=0x34d)
    public String userDefinedName;

    @SuppressWarnings("unchecked")
    @Override public SwitchOutput serialize(Serializer serializer, Serializable structure) {
        SwitchOutput output = (structure == null) ? new SwitchOutput() : (SwitchOutput) structure;

        int version = serializer.getRevision().getVersion();

        output.activation = serializer.struct(output.activation, SwitchSignal.class);
        output.targetList = serializer.array(output.targetList, SwitchTarget.class);
        if (version >= 0x34d)
            output.userDefinedName = serializer.wstr(output.userDefinedName);

        return output;
    }

    @Override public int getAllocatedSize() { 
        int size = SwitchOutput.BASE_ALLOCATION_SIZE;
        if (this.userDefinedName != null) size += (this.userDefinedName.length() * 2);
        return size;
    }
}
