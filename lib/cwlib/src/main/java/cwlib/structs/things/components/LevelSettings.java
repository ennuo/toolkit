package cwlib.structs.things.components;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

public class LevelSettings implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0xb0;

    public Vector3f sunPosition = new Vector3f(0.86f, 0.987f, 0.568f);
    public float sunPositionScale = 300000f;
    public Vector4f sunColor = new Vector4f(1.0f, 0.9f, 0.8f, 1.0f);
    public Vector4f ambientColor = new Vector4f(0.2f, 0.3f, 0.4f, 1.0f);
    public float sunMultiplier = 1.0f, exposure = 1.0f;
    public Vector4f fogColor = new Vector4f(0.3f, 0.5f, 0.7f, 1.0f);
    public float fogNear = 1000f, fogFar = 4000f;
    public Vector4f rimColor = new Vector4f(0.3f, 0.2f, 0.1f, 1.0f);
    @GsonRevision(min = 0x138)
    public Vector4f rimColor2 = new Vector4f(0.3f, 0.4f, 0.5f, 1.0f);

    @GsonRevision(min = 0x325)
    public float bakedShadowAmount, bakedShadowBlur = 0.1f;
    @GsonRevision(min = 0x325)
    public float bakedAOBias, bakedAOScale = 1.0f;
    @GsonRevision(min = 0x325)
    public float dynamicAOAmount = 0.4f;
    @GsonRevision(min = 0x325)
    public float dofNear = 4000f;
    @GsonRevision(min = 0x326)
    public float dofFar = 50000f;

    @GsonRevision(min = 0x331)
    public float zEffectAmount = 0f, zEffectBright = 0f, zEffectContrast = 0.333f;

    @GsonRevision(lbp3 = true, min = 0x7a)
    public float dofNear2 = -7500f, dofFar2 = -3500f;
    @GsonRevision(lbp3 = true, min = 0xce)
    public float dofFar3 = -100000f;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        sunPosition = serializer.v3(sunPosition);
        sunPositionScale = serializer.f32(sunPositionScale);
        sunColor = serializer.v4(sunColor);
        ambientColor = serializer.v4(ambientColor);
        sunMultiplier = serializer.f32(sunMultiplier);
        exposure = serializer.f32(exposure);
        fogColor = serializer.v4(fogColor);
        fogNear = serializer.f32(fogNear);
        fogFar = serializer.f32(fogFar);
        rimColor = serializer.v4(rimColor);

        if (version >= 0x138)
            rimColor2 = serializer.v4(rimColor2);

        if (version >= 0x325)
        {
            bakedShadowAmount = serializer.f32(bakedShadowAmount); // 0x1f
            bakedShadowBlur = serializer.f32(bakedShadowBlur); // 0x20
            bakedAOBias = serializer.f32(bakedAOBias); // 0x21
            bakedAOScale = serializer.f32(bakedAOScale); // 0x22
            dynamicAOAmount = serializer.f32(dynamicAOAmount); // 0x23
            dofNear = serializer.f32(dofNear); // 0x24
        }

        if (version > 0x324 && version < 0x331)
            serializer.f32(0);

        if (version >= 0x326)
            dofFar = serializer.f32(dofFar); // 0x25

        if (version > 0x324 && version < 0x331)
            serializer.f32(0);

        if (version >= 0x331)
        {
            zEffectAmount = serializer.f32(zEffectAmount);
            zEffectBright = serializer.f32(zEffectBright);
            zEffectContrast = serializer.f32(zEffectContrast);
        }

        if (subVersion >= 0x7a)
        {
            dofNear2 = serializer.f32(dofNear2);
            dofFar2 = serializer.f32(dofFar2);
            if (subVersion >= 0xce)
                dofFar3 = serializer.f32(dofFar3);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return LevelSettings.BASE_ALLOCATION_SIZE;
    }
}
