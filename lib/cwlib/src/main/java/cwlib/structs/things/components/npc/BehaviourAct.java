package cwlib.structs.things.components.npc;

import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class BehaviourAct extends BehaviourBase
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public Thing recordingPlayer;
    public InputRecording recording = new InputRecording();
    public int currentFrame;

    @Override
    public void serialize(Serializer serializer)
    {
        super.serialize(serializer);
        if (serializer.getRevision().getVersion() <= 0x28f) return;

        recordingPlayer = serializer.thing(recordingPlayer);
        recording = serializer.struct(recording, InputRecording.class);
        currentFrame = serializer.i32(currentFrame);
    }

    @Override
    public int getAllocatedSize()
    {
        return BehaviourAct.BASE_ALLOCATION_SIZE;
    }
}
