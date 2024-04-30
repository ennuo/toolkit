package cwlib.structs.things.components.poppet;

import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class RaycastResults implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public Vector4f hitpoint, normal;
    public float baryU, baryV;
    public int triIndex;
    public Thing hitThing, refThing;
    public int onCostumePiece, decorationIdx;
    public boolean switchConnector;

    @Override
    public void serialize(Serializer serializer)
    {
        hitpoint = serializer.v4(hitpoint);
        normal = serializer.v4(normal);

        baryU = serializer.f32(baryU);
        baryV = serializer.f32(baryV);
        triIndex = serializer.i32(triIndex);

        hitThing = serializer.thing(hitThing);
        refThing = serializer.thing(refThing);

        onCostumePiece = serializer.s32(onCostumePiece);
        decorationIdx = serializer.i32(decorationIdx);
        switchConnector = serializer.bool(switchConnector);
    }

    @Override
    public int getAllocatedSize()
    {
        return RaycastResults.BASE_ALLOCATION_SIZE;
    }
}
