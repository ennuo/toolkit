package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.script.ScriptInstance;

public class PScript implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ScriptInstance instance = new ScriptInstance();

    @SuppressWarnings("unchecked")
    @Override public PScript serialize(Serializer serializer, Serializable structure) {
        PScript script = (structure == null) ? new PScript() : (PScript) structure;

        int version = serializer.getRevision().getVersion();

        if (0x179 < version && version < 0x1a1)
            serializer.bool(false); // unknown
        script.instance = serializer.struct(script.instance, ScriptInstance.class);

        return script;
    }

    @Override public int getAllocatedSize() { return PScript.BASE_ALLOCATION_SIZE; }
}
