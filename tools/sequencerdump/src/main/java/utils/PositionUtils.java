package utils;

import cwlib.enums.Part;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PPos;
import org.joml.*;

import java.util.Objects;

public final class PositionUtils {

    private PositionUtils() {
    }

    public static Vector3f getRelativePosition(Thing container, Thing thing) {
        PPos containerPos = Objects.requireNonNull(container.getPart(Part.POS));
        PPos thingPos = Objects.requireNonNull(thing.getPart(Part.POS));
        return getRelativePosition(containerPos.worldPosition, thingPos.worldPosition);
    }

    public static Vector3f getRelativePosition(Matrix4f container, Matrix4f thing) {
        return thing
                .getTranslation(new Vector3f())
                .sub(container.getTranslation(new Vector3f()))
                .rotate(thing.getNormalizedRotation(new Quaternionf()).invert());
    }
}
