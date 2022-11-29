package cwlib.structs.things.components.npc;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class BehaviourAct extends BehaviourBase {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public Thing recordingPlayer;
    public InputRecording recording = new InputRecording();
    public int currentFrame;

    @SuppressWarnings("unchecked")
    @Override public BehaviourAct serialize(Serializer serializer, Serializable structure) {
        BehaviourAct act = (structure == null) ? new BehaviourAct() : (BehaviourAct) structure;

        super.serialize(serializer, act);
        if (serializer.getRevision().getVersion() <= 0x28f) return act;

        act.recordingPlayer = serializer.thing(act.recordingPlayer);
        act.recording = serializer.struct(act.recording, InputRecording.class);
        act.currentFrame = serializer.i32(act.currentFrame);

        return act;
    }
    
    @Override public int getAllocatedSize() { return BehaviourAct.BASE_ALLOCATION_SIZE; }
}
