package cwlib.structs.things.components.world;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class CutsceneCameraManager implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x14;

    public int state;
    public Thing currentCutSceneCamera;
    public int timeInCurrentCamera, endCountdown;
    public float transitionStage;

    @GsonRevision(min = 0x3a0)
    public boolean currentCameraTweaking;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        state = serializer.i32(state);
        currentCutSceneCamera = serializer.reference(currentCutSceneCamera,
            Thing.class);
        timeInCurrentCamera = serializer.s32(timeInCurrentCamera);
        endCountdown = serializer.s32(endCountdown);
        transitionStage = serializer.f32(transitionStage);

        if (0x2ef < version && version < 0x36e)
        {
            serializer.bool(false);
            serializer.v3(null);
            serializer.f32(0);
            serializer.v3(null);
            serializer.bool(false);
            serializer.v3(null);
            serializer.f32(0);
            serializer.v3(null);
        }

        if (version >= 0x3a0)
            currentCameraTweaking = serializer.bool(currentCameraTweaking);
    }

    @Override
    public int getAllocatedSize()
    {
        return CutsceneCameraManager.BASE_ALLOCATION_SIZE;
    }
}
