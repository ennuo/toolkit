package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class PSpriteLight implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0xD0;

    public Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    @GsonRevision(min=0x2fd)
    public Vector4f colorOff;

    public float multiplier = 1.8f;

    @GsonRevision(min=0x30a)
    public float multiplierOff = 0.0f;

    public float glowRadius = 20.0f;
    public float farDist = 2.3f;
    public float sourceSize = 0.28f;
    ResourceDescriptor falloffTexture;
    public Thing lookAt;
    public boolean spotlight;
    
    @GsonRevision(max=0x336)
    @Deprecated public boolean enableFogShadows = true, enableFog = true;

    @GsonRevision(min=0x139)
    public float fogAmount = 0.6f;

    @GsonRevision(min=0x13a, max=0x2c3)
    @Deprecated public float onDest = 1.0f;

    @GsonRevision(min=0x13a)
    public float onSpeed = 1.0f, offSpeed = 1.0f, flickerProb, flickerAmount;

    @GsonRevision(min=0x2c4)
    public int behavior;

    @GsonRevision(lbp3=true,min=0x113)
    public boolean highBeam;

    @GsonRevision(lbp3=true,min=0x146)
    public boolean tracker;

    @GsonRevision(lbp3=true,min=0x14a)
    public byte trackerType;

    @GsonRevision(lbp3=true,min=0x151)
    public float causticStrength, causticWidth;

    @GsonRevision(lbp3=true,min=0x16f)
    public float trackingLimit, trackingAccel, trackingSpeed;
    
    @GsonRevision(lbp3=true,min=0x16f)
    public int movementInput, lightingInput;

    @GsonRevision(lbp3=true,min=0x16f)
    public Vector3f beamDir, azimuth;
    
    @SuppressWarnings("unchecked")
    @Override public PSpriteLight serialize(Serializer serializer, Serializable structure) {
        PSpriteLight spriteLight = (structure == null) ? new PSpriteLight() : (PSpriteLight) structure;
        
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        spriteLight.color = serializer.v4(spriteLight.color);
        if (version >= 0x2fd)
            spriteLight.colorOff = serializer.v4(spriteLight.colorOff);
        spriteLight.multiplier = serializer.f32(spriteLight.multiplier);
        if (version >= 0x30a)
            spriteLight.multiplierOff = serializer.f32(spriteLight.multiplierOff);
        
        spriteLight.glowRadius = serializer.f32(spriteLight.glowRadius);
        spriteLight.farDist = serializer.f32(spriteLight.farDist);
        spriteLight.sourceSize = serializer.f32(spriteLight.sourceSize);
        spriteLight.falloffTexture = serializer.resource(spriteLight.falloffTexture, ResourceType.TEXTURE);
        
        spriteLight.lookAt = serializer.thing(spriteLight.lookAt);
        spriteLight.spotlight = serializer.bool(spriteLight.spotlight);

        if (version < 0x337) {
            spriteLight.enableFogShadows = serializer.bool(spriteLight.enableFogShadows);
            spriteLight.enableFog = serializer.bool(spriteLight.enableFog);
        }

        if (version >= 0x139)
            spriteLight.fogAmount = serializer.f32(spriteLight.fogAmount);

        if (version >= 0x13a) {
            if (version < 0x2c4)
                spriteLight.onDest = serializer.f32(spriteLight.onDest);
            spriteLight.onSpeed = serializer.f32(spriteLight.onSpeed);
            spriteLight.offSpeed = serializer.f32(spriteLight.offSpeed);
            spriteLight.flickerProb = serializer.f32(spriteLight.flickerProb);
            spriteLight.flickerAmount = serializer.f32(spriteLight.flickerAmount);
        }

        if (version >= 0x2c4) 
            spriteLight.behavior = serializer.i32(spriteLight.behavior);

        if (subVersion >= 0x113)
            spriteLight.highBeam = serializer.bool(spriteLight.highBeam);

        if (subVersion >= 0x146)
            spriteLight.tracker = serializer.bool(spriteLight.tracker);

        if (subVersion >= 0x14a)
            spriteLight.trackerType = serializer.i8(spriteLight.trackerType);

        if (subVersion >= 0x151) {
            spriteLight.causticStrength = serializer.f32(spriteLight.causticStrength);
            spriteLight.causticWidth = serializer.f32(spriteLight.causticWidth);
        }

        if (subVersion >= 0x16f) {
            spriteLight.trackingLimit = serializer.f32(spriteLight.trackingLimit);
            spriteLight.trackingAccel = serializer.f32(spriteLight.trackingAccel);
            spriteLight.trackingSpeed = serializer.f32(spriteLight.trackingSpeed);
            spriteLight.movementInput = serializer.s32(spriteLight.movementInput);
            spriteLight.lightingInput = serializer.s32(spriteLight.movementInput);

            spriteLight.beamDir = serializer.v3(spriteLight.beamDir);
            spriteLight.azimuth = serializer.v3(spriteLight.azimuth);
        }

        return spriteLight;
    }
    
    @Override public int getAllocatedSize() { return PSpriteLight.BASE_ALLOCATION_SIZE; }
}
