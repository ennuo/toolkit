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
    public Vector3f posVel;
    public float angVel;
    public int frequency, phase, lifetime;
    public boolean recycleEmittedObjects;

    @Deprecated public GlobalThingDescriptor thing;
    public ResourceDescriptor plan;

    public int maxEmitted, maxEmittedAtOnce = 1000;
    public float modStartFrames, modDeltaFrames;
    public SwitchSignal modScale, speedScale;
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
    public Thing emittedOjbectSource;
    public boolean editorEmitter;
    public boolean isLimboFlippedForGunEmitter;
    public float theFuckingZOffset;
    public boolean justUseTheFuckingZOffset;

    @SuppressWarnings("unchecked")
    @Override public PEmitter serialize(Serializer serializer, Serializable structure) {
        PEmitter emitter = (structure == null) ? new PEmitter() : (PEmitter) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        emitter.posVel = serializer.v3(emitter.posVel);
        emitter.angVel = serializer.f32(emitter.angVel);

        if (version < 0x137) {
            serializer.f32(0);
            serializer.f32(0);
        }

        emitter.frequency = serializer.i32(emitter.frequency);
        emitter.phase = serializer.i32(emitter.phase);
        emitter.lifetime = serializer.i32(emitter.lifetime);

        if (version < 0x160)
            emitter.thing = serializer.struct(emitter.thing, GlobalThingDescriptor.class);
        else
            emitter.plan = serializer.resource(emitter.plan, ResourceType.PLAN, true);

        emitter.maxEmitted = serializer.i32(emitter.maxEmitted);
        if (version >= 0x1c8)
            emitter.maxEmittedAtOnce = serializer.i32(emitter.maxEmittedAtOnce);

        if (version < 0x137)
            serializer.bool(false);

        emitter.modStartFrames = serializer.f32(emitter.modStartFrames);
        emitter.modDeltaFrames = serializer.f32(emitter.modDeltaFrames);

        emitter.modScale = serializer.struct(emitter.modScale, SwitchSignal.class);
        // emitter.speedScale = serializer.struct(emitter.speedScale, SwitchSignal.class);

        emitter.lastUpdateFrame = serializer.f32(emitter.lastUpdateFrame);

        if (version >= 0x137) {
            emitter.worldOffset = serializer.v4(emitter.worldOffset);
            emitter.worldRotation = serializer.f32(emitter.worldRotation);
            emitter.emitScale = serializer.f32(emitter.emitScale);
            emitter.linearVel = serializer.f32(emitter.linearVel);
            emitter.parentThing = serializer.reference(emitter.parentThing, Thing.class);
        }

        if (version >= 0x13f)
            emitter.currentEmitted = serializer.i32(emitter.currentEmitted);

        if (version >= 0x144)
            emitter.emitFlip = serializer.bool(emitter.emitFlip);

        if (version >= 0x1ce) {
            emitter.parentRelativeOffset = serializer.v4(emitter.parentRelativeOffset);
            emitter.parentRelativeRotation = serializer.f32(emitter.parentRelativeRotation);
            emitter.worldZ = serializer.f32(emitter.worldZ);
            emitter.zOffset = serializer.f32(emitter.zOffset);
            emitter.emitFrontZ = serializer.f32(emitter.emitFrontZ);
            emitter.emitBackZ = serializer.f32(emitter.emitBackZ);
        }

        if (version >= 0x226)
            emitter.hideInPlayMode = serializer.bool(emitter.hideInPlayMode);
        if (version >= 0x230)
            emitter.modScaleActive = serializer.bool(emitter.modScaleActive);

        // serializer.log("UNFINISHED EMITTER");
        // System.exit(1);

        return emitter;
    }
    
    // TODO: Actually implement
    @Override public int getAllocatedSize() { return 0; }
}
