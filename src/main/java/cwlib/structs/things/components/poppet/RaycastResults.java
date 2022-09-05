package cwlib.structs.things.components.poppet;

import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class RaycastResults implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public Vector4f hitpoint, normal;
    public float baryU, baryV;
    public int triIndex;
    public Thing hitThing, refThing;
    public int onCostumePiece, decorationIdx;
    public boolean switchConnector;

    @SuppressWarnings("unchecked")
    @Override public RaycastResults serialize(Serializer serializer, Serializable structure) {
        RaycastResults results = 
            (structure == null) ? new RaycastResults() : (RaycastResults) structure;

        results.hitpoint = serializer.v4(results.hitpoint);
        results.normal = serializer.v4(results.normal);

        results.baryU = serializer.f32(results.baryU);
        results.baryV = serializer.f32(results.baryV);
        results.triIndex = serializer.i32(results.triIndex);

        results.hitThing = serializer.thing(results.hitThing);
        results.refThing = serializer.thing(results.refThing);

        results.onCostumePiece = serializer.s32(results.onCostumePiece);
        results.decorationIdx = serializer.i32(results.decorationIdx);
        results.switchConnector = serializer.bool(results.switchConnector);

        return results;
    }

    @Override public int getAllocatedSize() { return RaycastResults.BASE_ALLOCATION_SIZE; }
}
