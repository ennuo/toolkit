package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.Decoration;

public class PDecorations implements Serializable
{
    public Decoration[] decorations;

    public PDecorations() { }

    public PDecorations(Decoration decor)
    {
        this.decorations = new Decoration[] { decor };
    }

    @Override
    public void serialize(Serializer serializer)
    {
        decorations = serializer.array(decorations, Decoration.class);
    }

    // TODO: Actually implement
    @Override
    public int getAllocatedSize()
    {
        return 0;
    }
}
