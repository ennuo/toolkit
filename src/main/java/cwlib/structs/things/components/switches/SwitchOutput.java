package cwlib.structs.things.components.switches;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class SwitchOutput implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public SwitchSignal activation;
    public SwitchTarget[] targetList;
    public String userDefinedName;

    @SuppressWarnings("unchecked")
    @Override public SwitchOutput serialize(Serializer serializer, Serializable structure) {
        SwitchOutput output = (structure == null) ? new SwitchOutput() : (SwitchOutput) structure;

        int version = serializer.getRevision().getVersion();

        output.activation = serializer.struct(output.activation, SwitchSignal.class);

        if (version > 0x326)
            output.targetList = serializer.array(output.targetList, SwitchTarget.class);
        else {
            if (serializer.isWriting()) {
                if (output.targetList == null || output.targetList.length == 0) serializer.i32(0);
                else {
                    serializer.i32(output.targetList.length);
                    for (SwitchTarget target : output.targetList)
                        serializer.thing(target.thing);
                }
            } else {
                output.targetList = new SwitchTarget[serializer.getInput().i32()];
                for (int i = 0; i < output.targetList.length; ++i)
                    output.targetList[i] = new SwitchTarget(serializer.thing(null));
            }
        }
        
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
