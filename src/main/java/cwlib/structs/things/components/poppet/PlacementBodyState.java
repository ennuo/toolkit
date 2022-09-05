package cwlib.structs.things.components.poppet;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class PlacementBodyState implements Serializable {
    public static int BASE_ALLOCATION_SIZE = 0x10;

    public Thing thing;
    public Thing oldParent;
    public int frozen;

    @SuppressWarnings("unchecked")
    @Override public PlacementBodyState serialize(Serializer serializer, Serializable structure) {
        PlacementBodyState state = (structure == null) ? new PlacementBodyState() : (PlacementBodyState) structure;

        state.thing = serializer.thing(state.thing);
        state.oldParent = serializer.thing(state.oldParent);
        state.frozen = serializer.i32(state.frozen);

        return state;
    }


    @Override public int getAllocatedSize() { return PlacementBodyState.BASE_ALLOCATION_SIZE; }
}