package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class PSpriteLight implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0xD0;

    public Vector4f color, colorOff;
    public float multiplier = 1.8f, multiplierOff = 0.0f;
    public float glowRadius = 20.0f;
    public float farDist = 2.3f;
    public float sourceSize = 0.28f;
    ResourceDescriptor falloffTexture;
    public Thing lookAt;
    public boolean spotlight;
    @Deprecated public boolean enableFogShadows = true;
    @Deprecated public boolean enableFog = true;
    public float fogAmount = 0.6f;
    @Deprecated public float onDest = 1.0f;
    public float onSpeed = 1.0f;
    public float offSpeed = 1.0f;
    public float flickerProb;
    public float flickerAmount;
    public int behavior;
    public boolean highBeam, tracker;
    public byte trackerType;
    public float causticStrength, causticWidth;
    public float trackingLimit, trackingAccel, trackingSpeed;
    public float movementInput, lightingInput;
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
            spriteLight.movementInput = serializer.f32(spriteLight.movementInput);
            spriteLight.lightingInput = serializer.f32(spriteLight.movementInput);

            spriteLight.beamDir = serializer.v3(spriteLight.beamDir);
            spriteLight.azimuth = serializer.v3(spriteLight.azimuth);
        }

        return spriteLight;
    }
    
    @Override public int getAllocatedSize() { return PSpriteLight.BASE_ALLOCATION_SIZE; }
}
