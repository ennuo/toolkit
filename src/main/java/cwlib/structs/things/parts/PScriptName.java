package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * Part that just contains the name of the
 * script attached, unknown if it serves any current
 * purpose.
 */
public class PScriptName implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    public String name;
    
    @SuppressWarnings("unchecked")
    @Override public PScriptName serialize(Serializer serializer, Serializable structure) {
        PScriptName scriptName = (structure == null) ? new PScriptName() : (PScriptName) structure;
        
        scriptName.name = serializer.str(scriptName.name);
        
        return scriptName;
    }

    @Override public int getAllocatedSize() {
        int size = PScriptName.BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += this.name.length();
        return size;
    }
}
