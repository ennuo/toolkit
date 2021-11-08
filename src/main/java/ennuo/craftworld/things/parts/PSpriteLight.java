package ennuo.craftworld.things.parts;

import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.things.Thing;
import ennuo.craftworld.types.data.ResourceDescriptor;
import org.joml.Vector4f;

public class PSpriteLight implements Serializable {
    public Vector4f color = new Vector4f(1.11f, 1.04f, 0.589f, 1.0f);
    public float multiplier = 1.8f;
    public float glowRadius = 20.0f;
    public float farDist = 2.3f;
    public float sourceSize = 0.28f;
    ResourceDescriptor falloffTexture;
    public Thing lookAt;
    public boolean spotlight;
    public boolean enableFogShadows = true;
    public boolean enableFog = true;
    public float fogAmount = 0.6f;
    public float onDest = 1.0f;
    public float onSpeed = 1.0f;
    public float offSpeed = 1.0f;
    public float flickerProb;
    public float flickerAmount;
    
    public PSpriteLight serialize(Serializer serializer, Serializable structure) {
        PSpriteLight spriteLight = (structure == null) ? new PSpriteLight() : (PSpriteLight) structure;
        
        spriteLight.color = serializer.v4(spriteLight.color);
        spriteLight.multiplier = serializer.f32(spriteLight.multiplier);
        spriteLight.glowRadius = serializer.f32(spriteLight.glowRadius);
        spriteLight.farDist = serializer.f32(spriteLight.farDist);
        spriteLight.sourceSize = serializer.f32(spriteLight.sourceSize);
        spriteLight.falloffTexture = serializer.resource(spriteLight.falloffTexture, RType.TEXTURE);
        spriteLight.lookAt = serializer.reference(spriteLight.lookAt, Thing.class);
        spriteLight.spotlight = serializer.bool(spriteLight.spotlight);
        spriteLight.enableFogShadows = serializer.bool(spriteLight.enableFogShadows);
        spriteLight.enableFog = serializer.bool(spriteLight.enableFog);
        spriteLight.fogAmount = serializer.f32(spriteLight.fogAmount);
        spriteLight.onDest = serializer.f32(spriteLight.onDest);
        spriteLight.onSpeed = serializer.f32(spriteLight.onSpeed);
        spriteLight.offSpeed = serializer.f32(spriteLight.offSpeed);
        spriteLight.flickerProb = serializer.f32(spriteLight.flickerProb);
        spriteLight.flickerAmount = serializer.f32(spriteLight.flickerAmount);
        
        return spriteLight;
    }
    
}
