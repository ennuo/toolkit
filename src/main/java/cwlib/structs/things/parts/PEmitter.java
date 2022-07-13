package cwlib.structs.things.parts;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.GlobalThingDescriptor;
import cwlib.structs.things.components.SwitchSignal;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class PEmitter implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x120;

    public Vector3f posVel;
    public float angVel;
    public int frequency, phase, lifetime;
    public boolean recycleEmittedObjects;

    @Deprecated public GlobalThingDescriptor thing;
    public ResourceDescriptor plan;

    public int maxEmitted, maxEmittedAtOnce = 1000;
    public float speedScaleStartFrame, speedScaleDeltaFrames;
    public SwitchSignal speedScale;
    public float lastUpdateFrame;
    public Vector4f worldOffset;
    public float worldRotation, worldRotationForEditorEmitters;
    public float emitScale, linearVel;
    public Thing parentThing;
    public int currentEmitted;
    public boolean emitFlip;
    public Vector4f parentRelativeOffset;
    public float parentRelativeRotation;
    public float worldZ, zOffset, emitFrontZ, emitBackZ;
    public boolean hideInPlayMode, modScaleActive;
    public int behavior;
    public byte effectCreate, effectDestroy;
    public boolean ignoreParentsVelocity;
    public Thing emittedObjectSource;
    public boolean editorEmitter;
    public boolean isLimboFlippedForGunEmitter;
    public float theFuckingZOffset;
    public boolean justUseTheFuckingZOffset;
    public boolean soundEnabled;
    public boolean emitByReferenceInPlayMode, emitToNearestRearLayer;

    @SuppressWarnings("unchecked")
    @Override public PEmitter serialize(Serializer serializer, Serializable structure) {
        PEmitter emitter = (structure == null) ? new PEmitter() : (PEmitter) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version < 0x368)
            emitter.posVel = serializer.v3(emitter.posVel);
        emitter.angVel = serializer.f32(emitter.angVel);

        if (version < 0x137) {
            serializer.f32(0);
            serializer.f32(0);
        }

        emitter.frequency = serializer.i32(emitter.frequency);
        emitter.phase = serializer.i32(emitter.phase);
        emitter.lifetime = serializer.i32(emitter.lifetime);

        if (version >= 0x2fe && subVersion < 0x65)
            emitter.recycleEmittedObjects = serializer.bool(emitter.recycleEmittedObjects);
        if (subVersion > 0x64)
            emitter.recycleEmittedObjects = serializer.bool(emitter.recycleEmittedObjects); // ???

        if (version < 0x160)
            emitter.thing = serializer.struct(emitter.thing, GlobalThingDescriptor.class);
        else
            emitter.plan = serializer.resource(emitter.plan, ResourceType.PLAN);

        emitter.maxEmitted = serializer.i32(emitter.maxEmitted);
        if (version >= 0x1c8)
            emitter.maxEmittedAtOnce = serializer.i32(emitter.maxEmittedAtOnce);

        if (version < 0x137)
            serializer.bool(false);

        emitter.speedScaleStartFrame = serializer.f32(emitter.speedScaleStartFrame);
        emitter.speedScaleDeltaFrames = serializer.f32(emitter.speedScaleDeltaFrames);
        emitter.speedScale = serializer.struct(emitter.speedScale, SwitchSignal.class);

        if (version < 0x2c4)
            emitter.lastUpdateFrame = serializer.f32(emitter.lastUpdateFrame);

        if (version >= 0x137) {
            if (version < 0x314) {
                emitter.worldOffset = serializer.v4(emitter.worldOffset);
                emitter.worldRotation = serializer.f32(emitter.worldRotation);
            }
            if (version >= 0x38e)
                emitter.worldRotationForEditorEmitters = serializer.f32(emitter.worldRotationForEditorEmitters);
            emitter.emitScale = serializer.f32(emitter.emitScale);
            emitter.linearVel = serializer.f32(emitter.linearVel);
            if (version < 0x314)
                emitter.parentThing = serializer.reference(emitter.parentThing, Thing.class);
        }

        if (version >= 0x13f)
            emitter.currentEmitted = serializer.i32(emitter.currentEmitted);

        if (version >= 0x144 && subVersion < 0x64)
            emitter.emitFlip = serializer.bool(emitter.emitFlip);
        if (subVersion > 0x64)
            emitter.emitFlip = serializer.bool(emitter.emitFlip); // ???

        if (version >= 0x1ce) {
            emitter.parentRelativeOffset = serializer.v4(emitter.parentRelativeOffset);
            emitter.parentRelativeRotation = serializer.f32(emitter.parentRelativeRotation);
            emitter.worldZ = serializer.f32(emitter.worldZ);
            emitter.zOffset = serializer.f32(emitter.zOffset);
            emitter.emitFrontZ = serializer.f32(emitter.emitFrontZ);
            emitter.emitBackZ = serializer.f32(emitter.emitBackZ);
        }

        if (version >= 0x226 && subVersion < 0x64)
            emitter.hideInPlayMode = serializer.bool(emitter.hideInPlayMode);
        if (subVersion > 0x64)
            emitter.hideInPlayMode = serializer.bool(emitter.hideInPlayMode); // Literally what is the point
        
        if (version >= 0x230 && version < 0x2c4)
            emitter.modScaleActive = serializer.bool(emitter.modScaleActive);

        if (version >= 0x2c4)
            emitter.behavior = serializer.i32(emitter.behavior);

        if (version >= 0x308) {
            if (version >= 0x38d) {
                emitter.effectCreate = serializer.i8(emitter.effectCreate);
                emitter.effectDestroy = serializer.i8(emitter.effectDestroy);
            } else {
                emitter.effectCreate = (byte) serializer.i32(emitter.effectCreate);
                emitter.effectDestroy = (byte) serializer.i32(emitter.effectDestroy);
            }
        }

        if (version >= 0x32e && subVersion < 0x64)
            emitter.ignoreParentsVelocity = serializer.bool(emitter.ignoreParentsVelocity);
        if (subVersion > 0x64)
            emitter.ignoreParentsVelocity = serializer.bool(emitter.ignoreParentsVelocity); // what the hell is happening here

        if (version >= 0x340)
            emitter.emittedObjectSource = serializer.thing(emitter.emittedObjectSource);

        if (version >= 0x361 && subVersion < 0x64)
            emitter.editorEmitter = serializer.bool(emitter.editorEmitter);
        if (subVersion > 0x64)
            emitter.editorEmitter = serializer.bool(emitter.editorEmitter); // did every boolean just die or something wtf

        if (version >= 0x38d && subVersion < 0x64)
            emitter.isLimboFlippedForGunEmitter = serializer.bool(emitter.isLimboFlippedForGunEmitter);
        if (subVersion > 0x64)
            emitter.isLimboFlippedForGunEmitter = serializer.bool(emitter.isLimboFlippedForGunEmitter); // my god

        if (version >= 0x3ae)
            emitter.theFuckingZOffset = serializer.f32(emitter.theFuckingZOffset);
        if (subVersion >= 0x18e)
            emitter.justUseTheFuckingZOffset = serializer.bool(emitter.justUseTheFuckingZOffset);
        
        if (subVersion >= 0x31 && subVersion < 0x65)
            emitter.soundEnabled = serializer.bool(emitter.soundEnabled);
        if (subVersion >= 0x65)
            emitter.soundEnabled = serializer.bool(emitter.soundEnabled); // seriously, for what reason?
            
        if (subVersion >= 0x41 && subVersion < 0x1a7)
            serializer.bool(false);
        
        if (subVersion > 0x64)
            emitter.emitByReferenceInPlayMode = serializer.bool(emitter.emitByReferenceInPlayMode);
        if (subVersion > 0x75)
            emitter.emitToNearestRearLayer = serializer.bool(emitter.emitToNearestRearLayer);
        
        return emitter;
    }
    
    @Override public int getAllocatedSize() { return PEmitter.BASE_ALLOCATION_SIZE; }
}
