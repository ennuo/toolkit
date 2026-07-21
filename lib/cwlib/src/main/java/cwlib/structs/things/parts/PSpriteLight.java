package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class PSpriteLight implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0xD0;

    public Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    @GsonRevision(min = 0x2fd)
    public Vector4f colorOff;

    public float multiplier = 1.8f;

    @GsonRevision(min = 0x30a)
    public float multiplierOff = 0.0f;

    public float glowRadius = 20.0f;
    public float farDist = 2.3f;
    public float sourceSize = 0.28f;
    ResourceDescriptor falloffTexture;
    public Thing lookAt;
    public boolean spotlight;

    @GsonRevision(max = 0x336)
    public boolean enableFogShadows = false, enableFog = false;

    @GsonRevision(min = 0x139)
    public float fogAmount = 0.6f;

    @GsonRevision(min = 0x13a, max = 0x2c3)
    public float onDest = 1.0f;

    @GsonRevision(min = 0x13a)
    public float onSpeed = 1.0f, offSpeed = 1.0f, flickerProb, flickerAmount;

    @GsonRevision(min = 0x2c4)
    public int behavior;

    @GsonRevision(lbp3 = true, min = 0x113)
    public boolean highBeam;

    @GsonRevision(lbp3 = true, min = 0x146)
    public boolean tracker;

    @GsonRevision(lbp3 = true, min = 0x14a)
    public byte trackerType;

    @GsonRevision(lbp3 = true, min = 0x151)
    public float causticStrength, causticWidth;

    @GsonRevision(lbp3 = true, min = 0x16f)
    public float trackingLimit, trackingAccel, trackingSpeed;

    @GsonRevision(lbp3 = true, min = 0x16f)
    public int movementInput, lightingInput;

    @GsonRevision(lbp3 = true, min = 0x16f)
    public Vector3f beamDir, azimuth;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        color = serializer.v4(color);
        if (version >= 0x2fd)
            colorOff = serializer.v4(colorOff);
        multiplier = serializer.f32(multiplier);
        if (version >= 0x30a)
            multiplierOff = serializer.f32(multiplierOff);

        glowRadius = serializer.f32(glowRadius);
        farDist = serializer.f32(farDist);
        sourceSize = serializer.f32(sourceSize);
        falloffTexture = serializer.resource(falloffTexture,
            ResourceType.TEXTURE);

        lookAt = serializer.thing(lookAt);
        spotlight = serializer.bool(spotlight);

        if (version < 0x337)
        {
            enableFogShadows = serializer.bool(enableFogShadows);
            enableFog = serializer.bool(enableFog);
        }

        if (version >= 0x139)
            fogAmount = serializer.f32(fogAmount);

        if (version >= 0x13a)
        {
            if (version < 0x2c4)
                onDest = serializer.f32(onDest);
            onSpeed = serializer.f32(onSpeed);
            offSpeed = serializer.f32(offSpeed);
            flickerProb = serializer.f32(flickerProb);
            flickerAmount = serializer.f32(flickerAmount);
        }

        if (version >= 0x2c4)
            behavior = serializer.i32(behavior);

        if (subVersion >= 0x113)
            highBeam = serializer.bool(highBeam);

        if (subVersion >= 0x146)
            tracker = serializer.bool(tracker);

        if (subVersion >= 0x14a)
            trackerType = serializer.i8(trackerType);

        if (subVersion >= 0x151)
        {
            causticStrength = serializer.f32(causticStrength);
            causticWidth = serializer.f32(causticWidth);
        }

        if (subVersion >= 0x16f)
        {
            trackingLimit = serializer.f32(trackingLimit);
            trackingAccel = serializer.f32(trackingAccel);
            trackingSpeed = serializer.f32(trackingSpeed);
            movementInput = serializer.s32(movementInput);
            lightingInput = serializer.s32(movementInput);

            beamDir = serializer.v3(beamDir);
            azimuth = serializer.v3(azimuth);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return PSpriteLight.BASE_ALLOCATION_SIZE;
    }
}
