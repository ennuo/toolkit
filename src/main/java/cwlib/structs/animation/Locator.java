package cwlib.structs.animation;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import org.joml.Vector3f;

public class Locator implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x12;

    public Vector3f position;
    public String name;
    public byte looping, type;

    @SuppressWarnings("unchecked")
    @Override public Locator serialize(Serializer serializer, Serializable structure) {
        Locator locator = (structure == null) ? new Locator() : (Locator) structure;

        locator.position = serializer.v3(locator.position);
        locator.name = serializer.str(locator.name);
        locator.looping = serializer.i8(locator.looping);
        locator.type = serializer.i8(locator.type);

        return locator;
    }

    @Override public int getAllocatedSize() {
        int size = BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += (this.name.length());
        return size;
    }
}
