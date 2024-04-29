package cwlib.structs.things.components.npc;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class ActingData implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x120;

    @GsonRevision(min = 0x2da)
    public int state;
    @GsonRevision(min = 0x2da)
    public Thing recordingNpc;
    @GsonRevision(min = 0x296)
    public InputRecording recording = new InputRecording();
    @GsonRevision(min = 0x296)
    public Thing recordingPlayer;
    @GsonRevision(min = 0x296)
    public int currentFrame;
    @GsonRevision(min = 0x2a6)
    public int recordingCountdown;
    @GsonRevision(min = 0x33e)
    public ResourceDescriptor VoIPRecording;
    @GsonRevision(lbp3 = true, min = 0xb6)
    public boolean transformOnRestart;
    @GsonRevision(lbp3 = true, min = 0xbd)
    public byte previousState;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        if (version > 0x2d9)
        {
            state = serializer.i32(state);
            recordingNpc = serializer.thing(recordingNpc);
        }

        if (version > 0x295)
        {
            recording = serializer.struct(recording, InputRecording.class);
            recordingPlayer = serializer.thing(recordingPlayer);
            currentFrame = serializer.i32(currentFrame);
        }

        if (version > 0x2a5)
            recordingCountdown = serializer.s32(recordingCountdown);
        if (version >= 0x33e)
            VoIPRecording = serializer.resource(VoIPRecording,
                ResourceType.VOIP_RECORDING);

        if (subVersion >= 0xb6)
            transformOnRestart = serializer.bool(transformOnRestart);
        if (subVersion >= 0xbd)
            previousState = serializer.i8(previousState);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = ActingData.BASE_ALLOCATION_SIZE;
        if (this.recording != null)
            size += this.recording.getAllocatedSize();
        return size;
    }
}
