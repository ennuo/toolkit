package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.script.ScriptInstance;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;

public class PScript implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ScriptInstance instance = new ScriptInstance();

    public PScript() { }

    public PScript(ResourceDescriptor script)
    {
        this.instance.script = script;
    }

    public boolean is(ResourceDescriptor descriptor)
    {
        if (instance == null || instance.script == null) return false;
        return instance.script.equals(descriptor);
    }

    public boolean is(GUID guid)
    {
        if (instance == null || instance.script == null) return false;
        return instance.script.isGUID() && guid.equals(instance.script.getGUID());
    }

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        if (0x179 < version && version < 0x1a1)
            serializer.bool(false); // unknown
        instance = serializer.struct(instance, ScriptInstance.class);
    }

    @Override
    public int getAllocatedSize()
    {
        return PScript.BASE_ALLOCATION_SIZE;
    }
}
