package cwlib.structs.things.parts;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PWindTweak implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public float windStrength = 50.0f;
    public Vector3f direction = new Vector3f(0.0f, 1.0f, 0.0f);
    public float angle, decay;
    public float currentInput = 1.0f;
    public float minRadius = 0.0f, maxRadius = 500.0f, angleRange = 180.0f;

    @GsonRevision(lbp3=true, min=0x2c)
    public int behavior;

    @GsonRevision(lbp3=true, min=0x85)
    public float maxSpeed = 4.0f;

    @GsonRevision(lbp3=true, min=0x87)
    public boolean affectsCharacters;

    @GsonRevision(lbp3=true, min=0xab)
    public boolean blowAtTag;

    @GsonRevision(lbp3=true, min=0xb4)
    public boolean blasterEffect;

    @GsonRevision(lbp3=true, min=0x172)
    public boolean occluders = true;

    @GsonRevision(lbp3=true, min=0x172)
    public byte effectType = 1;

    @GsonRevision(lbp3=true, min=0x212)
    public byte horizontalSpeed, verticalSpeed;

    @SuppressWarnings("unchecked")
    @Override public PWindTweak serialize(Serializer serializer, Serializable structure) {
        PWindTweak wind = (structure == null) ? new PWindTweak() : (PWindTweak) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        wind.windStrength = serializer.f32(wind.windStrength);
        wind.direction = serializer.v3(wind.direction);
        wind.angle = serializer.f32(wind.angle);
        wind.decay = serializer.f32(wind.decay);
        if (subVersion <= 0x2a) serializer.u8(0);
        wind.currentInput = serializer.f32(wind.currentInput);

        if (subVersion < 0xab)
            serializer.i32(0);
        if (subVersion <= 0x26) {
            serializer.i32(0);
            serializer.wstr(null);
        }

        wind.minRadius = serializer.f32(wind.minRadius);
        wind.maxRadius = serializer.f32(wind.maxRadius);
        wind.angleRange = serializer.f32(wind.angleRange);

        if (subVersion > 0x2b)
            wind.behavior = serializer.i32(wind.behavior);

        if (subVersion > 0x84)
            wind.maxSpeed = serializer.f32(wind.maxSpeed);

        if (subVersion > 0x86)
            wind.affectsCharacters = serializer.bool(wind.affectsCharacters);
        if (subVersion > 0xaa)
            wind.blowAtTag = serializer.bool(wind.blowAtTag);
        if (subVersion > 0xb3)
            wind.blasterEffect = serializer.bool(wind.blasterEffect);
        
        if (subVersion > 0x171) {
            wind.occluders = serializer.bool(wind.occluders);
            wind.effectType = serializer.i8(wind.effectType);
        }

        if (subVersion > 0x211) {
            wind.horizontalSpeed = serializer.i8(wind.horizontalSpeed);
            wind.verticalSpeed = serializer.i8(wind.verticalSpeed);
        }
        
        return wind;
    }

    @Override public int getAllocatedSize() {
        return PWindTweak.BASE_ALLOCATION_SIZE;
    }
}
