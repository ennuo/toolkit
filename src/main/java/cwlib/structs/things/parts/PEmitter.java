package cwlib.structs.things.parts;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonResourceType;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.EmittedObjectSource;
import cwlib.structs.things.components.GlobalThingDescriptor;
import cwlib.structs.things.components.switches.SwitchSignal;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class PEmitter implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x120;

    @GsonRevision(max=0x367)
    @Deprecated public Vector3f posVel;

    public float angVel;
    public int frequency, phase, lifetime;

    @GsonRevision(min=0x2fe)
    public boolean recycleEmittedObjects;

    @GsonRevision(max=0x15f)
    @Deprecated public GlobalThingDescriptor thing;

    @GsonResourceType(ResourceType.PLAN)
    @GsonRevision(min=0x160)
    public ResourceDescriptor plan;

    public int maxEmitted;

    @GsonRevision(min=0x1c8)
    public int maxEmittedAtOnce = 1000;

    public float speedScaleStartFrame, speedScaleDeltaFrames;
    public SwitchSignal speedScale;

    @GsonRevision(max=0x2c3)
    public float lastUpdateFrame;

    @GsonRevision(min=0x137,max=0x313)
    public Vector4f worldOffset;
    @GsonRevision(min=0x137,max=0x313)
    public float worldRotation;

    @GsonRevision(min=0x38e)
    public float worldRotationForEditorEmitters;

    @GsonRevision(min=0x137)
    public float emitScale, linearVel;

    @GsonRevision(min=0x137,max=0x313)
    public Thing parentThing;

    @GsonRevision(min=0x13f)
    public int currentEmitted;

    @GsonRevision(min=0x144)
    public boolean emitFlip;

    @GsonRevision(min=0x1ce)
    public Vector4f parentRelativeOffset;
    @GsonRevision(min=0x1ce)
    public float parentRelativeRotation;
    @GsonRevision(min=0x1ce)
    public float worldZ, zOffset, emitFrontZ, emitBackZ;

    @GsonRevision(min=0x226)
    public boolean hideInPlayMode;


    @GsonRevision(min=0x230, max=0x2c3)
    @Deprecated public boolean modScaleActive;

    @GsonRevision(min=0x2c4)
    public int behavior;

    @GsonRevision(min=0x308)
    public byte effectCreate, effectDestroy;

    @GsonRevision(min=0x32e)
    public boolean ignoreParentsVelocity;

    @GsonRevision(min=0x340)
    public EmittedObjectSource emittedObjectSource;

    @GsonRevision(min=0x361)
    public boolean editorEmitter;

    @GsonRevision(min=0x38d)
    public boolean isLimboFlippedForGunEmitter;

    @GsonRevision(min=0x3ae)
    public float theFuckingZOffset;

    @GsonRevision(lbp3=true, min=0x18e)
    public boolean justUseTheFuckingZOffset;

    @GsonRevision(lbp3=true, min=0x31)
    public boolean soundEnabled;

    @GsonRevision(lbp3=true, min=0x64)
    public boolean emitByReferenceInPlayMode;
    
    @GsonRevision(lbp3=true, min=0x76)
    public boolean emitToNearestRearLayer;

    /* Vita */
    @GsonRevision(branch=0x4431, min=0xf)
    public byte emitNode;
    @GsonRevision(branch=0x4431, min=0x2f)
    public byte cleanUpOnDestroyed;

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
            emitter.emittedObjectSource = serializer.reference(emitter.emittedObjectSource, EmittedObjectSource.class);

        if (version >= 0x361 && subVersion < 0x64)
            emitter.editorEmitter = serializer.bool(emitter.editorEmitter);
        if (subVersion > 0x64)
            emitter.editorEmitter = serializer.bool(emitter.editorEmitter); // did every boolean just die or something wtf

        if (version >= 0x38d && subVersion < 0x64)
            emitter.isLimboFlippedForGunEmitter = serializer.bool(emitter.isLimboFlippedForGunEmitter);
        if (subVersion > 0x64)
            emitter.isLimboFlippedForGunEmitter = serializer.bool(emitter.isLimboFlippedForGunEmitter); // my god

        // they renamed this field to backupZOffset in Vita, not fans of swears? :fearful:
        if (version >= 0x3ae)
            emitter.theFuckingZOffset = serializer.f32(emitter.theFuckingZOffset);

        if (revision.isVita()) {
            int vita = revision.getBranchRevision();
            if (vita >= 0xf) 
                emitter.emitNode = serializer.i8(emitter.emitNode);
            if (vita >= 0x2f)
                emitter.cleanUpOnDestroyed = serializer.i8(emitter.cleanUpOnDestroyed);
        }
        
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
