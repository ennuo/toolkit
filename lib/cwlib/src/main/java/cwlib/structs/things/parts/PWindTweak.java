package cwlib.structs.things.parts;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PWindTweak implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public float windStrength = 50.0f;
    public Vector3f direction = new Vector3f(0.0f, 1.0f, 0.0f);
    public float angle, decay;
    public float currentInput = 1.0f;
    public float minRadius = 0.0f, maxRadius = 500.0f, angleRange = 180.0f;

    @GsonRevision(lbp3 = true, min = 0x2c)
    public int behavior;

    @GsonRevision(lbp3 = true, min = 0x85)
    public float maxSpeed = 4.0f;

    @GsonRevision(lbp3 = true, min = 0x87)
    public boolean affectsCharacters;

    @GsonRevision(lbp3 = true, min = 0xab)
    public boolean blowAtTag;

    @GsonRevision(lbp3 = true, min = 0xb4)
    public boolean blasterEffect;

    @GsonRevision(lbp3 = true, min = 0x172)
    public boolean occluders = true;

    @GsonRevision(lbp3 = true, min = 0x172)
    public byte effectType = 1;

    @GsonRevision(lbp3 = true, min = 0x212)
    public byte horizontalSpeed, verticalSpeed;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        windStrength = serializer.f32(windStrength);
        direction = serializer.v3(direction);
        angle = serializer.f32(angle);
        decay = serializer.f32(decay);
        if (subVersion <= 0x2a) serializer.u8(0);
        currentInput = serializer.f32(currentInput);

        if (subVersion < 0xab)
            serializer.i32(0);
        if (subVersion <= 0x26)
        {
            serializer.i32(0);
            serializer.wstr(null);
        }

        minRadius = serializer.f32(minRadius);
        maxRadius = serializer.f32(maxRadius);
        angleRange = serializer.f32(angleRange);

        if (subVersion > 0x2b)
            behavior = serializer.i32(behavior);

        if (subVersion > 0x84)
            maxSpeed = serializer.f32(maxSpeed);

        if (subVersion > 0x86)
            affectsCharacters = serializer.bool(affectsCharacters);
        if (subVersion > 0xaa)
            blowAtTag = serializer.bool(blowAtTag);
        if (subVersion > 0xb3)
            blasterEffect = serializer.bool(blasterEffect);

        if (subVersion > 0x171)
        {
            occluders = serializer.bool(occluders);
            effectType = serializer.i8(effectType);
        }

        if (subVersion > 0x211)
        {
            horizontalSpeed = serializer.i8(horizontalSpeed);
            verticalSpeed = serializer.i8(verticalSpeed);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return PWindTweak.BASE_ALLOCATION_SIZE;
    }
}
