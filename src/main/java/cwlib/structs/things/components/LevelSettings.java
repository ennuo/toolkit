package cwlib.structs.things.components;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

public class LevelSettings implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0xb0;

    public Vector3f sunPosition = new Vector3f(1.0f, 0.5f, 1.0f);
    public float sunPositionScale = 310309f;
    public Vector4f sunColor = new Vector4f(0.49f, 0.441f, 0.392f, 0.49f);
    public Vector4f ambientColor = 	new Vector4f(0.4f, 0.6f, 0.8f, 1.0f);
    public float sunMultiplier = 1.23093f, exposure = 1.0f;
    public Vector4f fogColor = new Vector4f(-0.3f, -0.5f, -0.7f, 1.0f);
    public float fogNear = 200f, fogFar = 15000f;
    public Vector4f rimColor = new Vector4f(0.45f, 0.3f, 0.15f, 1.5f);
    public Vector4f rimColor2 = new Vector4f(0.3f, 0.4f, 0.5f, 1.0f);

    public float bakedShadowAmount, bakedShadowBlur = 0.1f;
    public float bakedAOBias, bakedAOScale = 1.0f;
    public float dynamicAOAmount = 0.4f;
    public float dofNear = 4000f, dofFar = 50000f;

    public float zEffectAmount = 0f, zEffectBright = 0f, zEffectContrast = 0.333f;

    public float dofNear2 = -7500f, dofFar2 = -3500f, dofFar3 = -100000f;

    @SuppressWarnings("unchecked")
    @Override public LevelSettings serialize(Serializer serializer, Serializable structure) {
        LevelSettings settings = (structure == null) ? new LevelSettings() : (LevelSettings) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();


        settings.sunPosition = serializer.v3(settings.sunPosition);
        settings.sunPositionScale = serializer.f32(settings.sunPositionScale);
        settings.sunColor = serializer.v4(settings.sunColor);
        settings.ambientColor = serializer.v4(settings.ambientColor);
        settings.sunMultiplier = serializer.f32(settings.sunMultiplier);
        settings.exposure = serializer.f32(settings.exposure);
        settings.fogColor = serializer.v4(settings.fogColor);
        settings.fogNear = serializer.f32(settings.fogNear);
        settings.fogFar = serializer.f32(settings.fogFar);
        settings.rimColor = serializer.v4(settings.rimColor);

        if (version >= 0x138)
            settings.rimColor2 = serializer.v4(settings.rimColor2);

        if (version >= 0x325) {
            settings.bakedShadowAmount = serializer.f32(settings.bakedShadowAmount); // 0x1f
            settings.bakedShadowBlur = serializer.f32(settings.bakedShadowBlur); // 0x20
            settings.bakedAOBias = serializer.f32(settings.bakedAOBias); // 0x21
            settings.bakedAOScale = serializer.f32(settings.bakedAOScale); // 0x22
            settings.dynamicAOAmount = serializer.f32(settings.dynamicAOAmount); // 0x23
            settings.dofNear = serializer.f32(settings.dofNear); // 0x24
        }

        // 0x325 -> 0x330, f32

        if (version >= 0x326)
            settings.dofFar = serializer.f32(settings.dofFar); // 0x25

        // 0x325 -> 0x330, f32

        if (version >= 0x331) {
            settings.zEffectAmount = serializer.f32(settings.zEffectAmount);
            settings.zEffectBright = serializer.f32(settings.zEffectBright);
            settings.zEffectContrast = serializer.f32(settings.zEffectContrast);
        }

        if (subVersion >= 0x7a) {
            settings.dofNear2 = serializer.f32(settings.dofNear2);
            settings.dofFar2 = serializer.f32(settings.dofFar2);
            settings.dofFar3 = serializer.f32(settings.dofFar3);
        }

        return settings;
    }

    @Override public int getAllocatedSize() { return LevelSettings.BASE_ALLOCATION_SIZE; }
}
