package cwlib.structs.things.components.world;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class CutsceneCameraManager implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x14;

    public int state;
    public Thing currentCutSceneCamera;
    public int timeInCurrentCamera, endCountdown;
    public float transitionStage;
    
    @GsonRevision(min=0x3a0)
    public boolean currentCameraTweaking;

    @SuppressWarnings("unchecked")
    @Override public CutsceneCameraManager serialize(Serializer serializer, Serializable structure) {
        CutsceneCameraManager manager = (structure == null) ? new CutsceneCameraManager() : (CutsceneCameraManager) structure;

        int version = serializer.getRevision().getVersion();

        manager.state = serializer.i32(manager.state);
        manager.currentCutSceneCamera = serializer.reference(manager.currentCutSceneCamera, Thing.class);
        manager.timeInCurrentCamera = serializer.s32(manager.timeInCurrentCamera);
        manager.endCountdown = serializer.s32(manager.endCountdown);
        manager.transitionStage = serializer.f32(manager.transitionStage);

        if (0x2ef < version && version < 0x36e) {
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
            manager.currentCameraTweaking = serializer.bool(manager.currentCameraTweaking);

        return manager;
    }

    @Override public int getAllocatedSize() { return CutsceneCameraManager.BASE_ALLOCATION_SIZE; }
}
