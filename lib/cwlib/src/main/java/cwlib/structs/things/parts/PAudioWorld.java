package cwlib.structs.things.parts;

import cwlib.enums.PlayMode;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;

public class PAudioWorld implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public String soundName;
    public float initialVolume, initialPitch, initialParam1;
    public float maxFalloff, impactTolerance;

    @GsonRevision(max = 0x2c3)
    public boolean triggerBySwitch;

    public PlayMode playMode = PlayMode.TRIGGER_BY_FALLOFF;

    public boolean paramAffectVol;
    public boolean paramAffectPitch;
    public boolean paramAffectParam;

    @GsonRevision(min = 0x165)
    public boolean isLocal;
    @GsonRevision(min = 0x198)
    public boolean hideInPlayMode;

    @GsonRevision(min = 0x2c4)
    public int behavior;

    @GsonRevision(min = 0x380)
    public GUID soundNames;
    @GsonRevision(min = 0x380)
    public int meshColor;

    @GsonRevision(lbp3 = true, min = 0x178)
    public int categoryGUID;
    @GsonRevision(lbp3 = true, min = 0x191)
    public boolean activatedLastFrame;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        soundName = serializer.str(soundName);

        initialVolume = serializer.f32(initialVolume);
        initialPitch = serializer.f32(initialPitch);
        initialParam1 = serializer.f32(initialParam1);

        maxFalloff = serializer.f32(maxFalloff);
        impactTolerance = serializer.f32(impactTolerance);

        if (version < 0x2c4)
        {
            boolean triggerByFalloff = playMode == PlayMode.TRIGGER_BY_FALLOFF;
            boolean triggerByImpact = playMode == PlayMode.TRIGGER_BY_IMPACT;
            boolean triggerByDestroy = playMode == PlayMode.TRIGGER_BY_DESTROY;

            triggerByFalloff = serializer.bool(triggerByFalloff);
            triggerByImpact = serializer.bool(triggerByImpact);
            if (version < 0x165)
                serializer.bool(false); // unk
            triggerBySwitch = serializer.bool(triggerBySwitch);
            if (version < 0x165)
                serializer.bool(false);
            if (version >= 0x1ad)
                triggerByDestroy = serializer.bool(triggerByDestroy);

            if (!serializer.isWriting())
            {
                if (triggerByFalloff) playMode = PlayMode.TRIGGER_BY_FALLOFF;
                if (triggerByImpact) playMode = PlayMode.TRIGGER_BY_IMPACT;
                if (triggerByDestroy) playMode = PlayMode.TRIGGER_BY_DESTROY;
            }
        }
        else playMode = serializer.enum32(playMode);

        paramAffectVol = serializer.bool(paramAffectVol);
        paramAffectPitch = serializer.bool(paramAffectPitch);
        paramAffectParam = serializer.bool(paramAffectParam);

        if (version >= 0x165)
            isLocal = serializer.bool(isLocal);
        if (version >= 0x198)
            hideInPlayMode = serializer.bool(hideInPlayMode);

        if (version >= 0x2c4)
            behavior = serializer.i32(behavior);

        if (version >= 0x380)
            soundNames = serializer.guid(soundNames);

        if (version >= 0x380)
            meshColor = serializer.i32(meshColor);

        if (subVersion >= 0x178)
            categoryGUID = serializer.i32(categoryGUID);
        if (subVersion >= 0x191)
            activatedLastFrame = serializer.bool(activatedLastFrame);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PAudioWorld.BASE_ALLOCATION_SIZE;
        if (this.soundName != null)
            size += (this.soundName.length());
        return size;
    }
}
