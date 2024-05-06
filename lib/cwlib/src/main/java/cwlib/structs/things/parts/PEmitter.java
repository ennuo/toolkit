package cwlib.structs.things.parts;

import cwlib.enums.Part;
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

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PEmitter implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x120;

    @GsonRevision(max = 0x367)
    @Deprecated
    public Vector3f posVel;

    public float angVel;
    public int frequency, phase, lifetime;

    @GsonRevision(min = 0x2fe)
    public boolean recycleEmittedObjects;

    @GsonRevision(max = 0x15f)
    @Deprecated
    public GlobalThingDescriptor thing;

    @GsonResourceType(ResourceType.PLAN)
    @GsonRevision(min = 0x160)
    public ResourceDescriptor plan;

    public int maxEmitted;

    @GsonRevision(min = 0x1c8)
    public int maxEmittedAtOnce = 1000;

    public float speedScaleStartFrame, speedScaleDeltaFrames;
    public SwitchSignal speedScale;

    @GsonRevision(max = 0x2c3)
    public float lastUpdateFrame;

    @GsonRevision(min = 0x137, max = 0x313)
    public Vector4f worldOffset = new Vector4f().zero();
    @GsonRevision(min = 0x137, max = 0x313)
    public float worldRotation;

    @GsonRevision(min = 0x38e)
    public float worldRotationForEditorEmitters;

    @GsonRevision(min = 0x137)
    public float emitScale, linearVel;

    @GsonRevision(min = 0x137, max = 0x313)
    public Thing parentThing;

    @GsonRevision(min = 0x13f)
    public int currentEmitted;

    @GsonRevision(min = 0x144)
    public boolean emitFlip;

    @GsonRevision(min = 0x1ce)
    public Vector4f parentRelativeOffset;
    @GsonRevision(min = 0x1ce)
    public float parentRelativeRotation;
    @GsonRevision(min = 0x1ce)
    public float worldZ, zOffset, emitFrontZ, emitBackZ;

    @GsonRevision(min = 0x226)
    public boolean hideInPlayMode;


    @GsonRevision(min = 0x230, max = 0x2c3)
    public boolean modScaleActive;

    @GsonRevision(min = 0x2c4)
    public int behavior;

    @GsonRevision(min = 0x308)
    public byte effectCreate, effectDestroy;

    @GsonRevision(min = 0x32e)
    public boolean ignoreParentsVelocity;

    @GsonRevision(min = 0x340)
    public EmittedObjectSource emittedObjectSource;

    @GsonRevision(min = 0x361)
    public boolean editorEmitter;

    @GsonRevision(min = 0x38d)
    public boolean isLimboFlippedForGunEmitter;

    @GsonRevision(min = 0x3ae)
    public float theFuckingZOffset;

    @GsonRevision(lbp3 = true, min = 0x18e)
    public boolean justUseTheFuckingZOffset;

    @GsonRevision(lbp3 = true, min = 0x31)
    public boolean soundEnabled;

    @GsonRevision(lbp3 = true, min = 0x64)
    public boolean emitByReferenceInPlayMode;

    @GsonRevision(lbp3 = true, min = 0x76)
    public boolean emitToNearestRearLayer;

    /* Vita */
    @GsonRevision(branch = 0x4431, min = 0xf)
    public byte emitNode;
    @GsonRevision(branch = 0x4431, min = 0x2f)
    public byte cleanUpOnDestroyed;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version < 0x368)
            posVel = serializer.v3(posVel);
        angVel = serializer.f32(angVel);

        if (version < 0x137)
        {
            serializer.f32(0);
            serializer.f32(0);
        }

        frequency = serializer.i32(frequency);
        phase = serializer.i32(phase);
        lifetime = serializer.i32(lifetime);

        if (version >= 0x2fe && subVersion < 0x65)
            recycleEmittedObjects = serializer.bool(recycleEmittedObjects);
        if (subVersion > 0x64)
            recycleEmittedObjects = serializer.bool(recycleEmittedObjects); // ???

        if (version < 0x160)
            thing = serializer.struct(thing, GlobalThingDescriptor.class);
        else
            plan = serializer.resource(plan, ResourceType.PLAN);

        maxEmitted = serializer.i32(maxEmitted);
        if (version >= 0x1c8)
            maxEmittedAtOnce = serializer.i32(maxEmittedAtOnce);

        if (version < 0x137)
            serializer.bool(false);

        speedScaleStartFrame = serializer.f32(speedScaleStartFrame);
        speedScaleDeltaFrames = serializer.f32(speedScaleDeltaFrames);
        speedScale = serializer.struct(speedScale, SwitchSignal.class);

        if (version < 0x2c4)
            lastUpdateFrame = serializer.f32(lastUpdateFrame);

        if (version >= 0x137)
        {
            if (version < 0x314)
            {
                worldOffset = serializer.v4(worldOffset);
                worldRotation = serializer.f32(worldRotation);
            }
            if (version >= 0x38e)
                worldRotationForEditorEmitters =
                    serializer.f32(worldRotationForEditorEmitters);
            emitScale = serializer.f32(emitScale);
            linearVel = serializer.f32(linearVel);
            if (version < 0x314)
                parentThing = serializer.reference(parentThing, Thing.class);
        }

        if (version >= 0x13f)
            currentEmitted = serializer.i32(currentEmitted);

        if (version >= 0x144 && subVersion < 0x64)
            emitFlip = serializer.bool(emitFlip);
        if (subVersion > 0x64)
            emitFlip = serializer.bool(emitFlip); // ???

        if (version >= 0x1ce)
        {
            parentRelativeOffset = serializer.v4(parentRelativeOffset);
            parentRelativeRotation = serializer.f32(parentRelativeRotation);
            worldZ = serializer.f32(worldZ);
            zOffset = serializer.f32(zOffset);
            emitFrontZ = serializer.f32(emitFrontZ);
            emitBackZ = serializer.f32(emitBackZ);
        }

        if (version >= 0x226 && subVersion < 0x64)
            hideInPlayMode = serializer.bool(hideInPlayMode);
        if (subVersion > 0x64)
            hideInPlayMode = serializer.bool(hideInPlayMode); // Literally what
        // is the point

        if (version >= 0x230 && version < 0x2c4)
        {
            modScaleActive = serializer.bool(modScaleActive);
            if (!serializer.isWriting() && modScaleActive)
                lastUpdateFrame = speedScaleStartFrame + speedScaleDeltaFrames;
        }

        if (version >= 0x2c4)
            behavior = serializer.i32(behavior);

        if (version >= 0x308)
        {
            if (version >= 0x38d)
            {
                effectCreate = serializer.i8(effectCreate);
                effectDestroy = serializer.i8(effectDestroy);
            }
            else
            {
                effectCreate = (byte) serializer.i32(effectCreate);
                effectDestroy = (byte) serializer.i32(effectDestroy);
            }
        }

        if (version >= 0x32e && subVersion < 0x64)
            ignoreParentsVelocity = serializer.bool(ignoreParentsVelocity);
        if (subVersion > 0x64)
            ignoreParentsVelocity = serializer.bool(ignoreParentsVelocity); //
        // what the hell is happening here

        if (version >= 0x340)
            emittedObjectSource = serializer.reference(emittedObjectSource,
                EmittedObjectSource.class);

        if (version >= 0x361 && subVersion < 0x64)
            editorEmitter = serializer.bool(editorEmitter);
        if (subVersion > 0x64)
            editorEmitter = serializer.bool(editorEmitter); // did every boolean
        // just die or something wtf

        if (version >= 0x38d && subVersion < 0x64)
            isLimboFlippedForGunEmitter =
                serializer.bool(isLimboFlippedForGunEmitter);
        if (subVersion > 0x64)
            isLimboFlippedForGunEmitter =
                serializer.bool(isLimboFlippedForGunEmitter); // my god

        // they renamed this field to backupZOffset in Vita, not fans of swears? :fearful:
        if (version >= 0x3ae)
            theFuckingZOffset = serializer.f32(theFuckingZOffset);

        if (revision.isVita())
        {
            int vita = revision.getBranchRevision();
            if (vita >= 0xf)
                emitNode = serializer.i8(emitNode);
            if (vita >= 0x2f)
                cleanUpOnDestroyed = serializer.i8(cleanUpOnDestroyed);
        }

        if (subVersion >= 0x18e)
            justUseTheFuckingZOffset = serializer.bool(justUseTheFuckingZOffset);

        if (subVersion >= 0x31 && subVersion < 0x65)
            soundEnabled = serializer.bool(soundEnabled);
        if (subVersion >= 0x65)
            soundEnabled = serializer.bool(soundEnabled); // seriously, for what
        // reason?

        if (subVersion >= 0x41 && subVersion < 0x1a7)
            serializer.bool(false);

        if (subVersion > 0x64)
            emitByReferenceInPlayMode = serializer.bool(emitByReferenceInPlayMode);
        if (subVersion > 0x75)
            emitToNearestRearLayer = serializer.bool(emitToNearestRearLayer);
    }

    public void fixup(Thing thing, Revision revision)
    {
        int version = revision.getVersion();

        if (version >= 0x314)
        {
            parentThing = thing.parent;
            
            // World offset was removed in later versions, so calculate it if necessary
            if (plan != null && parentThing != null)
            {
                PPos parentPartPos = parentThing.getPart(Part.POS);
                PPos partPos = thing.getPart(Part.POS);
                if (partPos != null && parentPartPos != null)
                {
                    Matrix4f parentMatrix = parentPartPos.worldPosition;
                    
                    worldOffset = parentRelativeOffset.mul(parentMatrix.transpose(new Matrix4f()), new Vector4f());
                    worldOffset.w = 0.0f;
                    
                    // Not the best at Matrix math, so this'll do
                    Matrix4f worldRotationMatrix = parentMatrix.rotateZ(parentRelativeRotation, new Matrix4f());
                    Vector3f worldEulerAngles = worldRotationMatrix.getNormalizedRotation(new Quaternionf()).getEulerAnglesXYZ(new Vector3f());
                    worldRotation = (float)Math.toDegrees(worldEulerAngles.z);

                    // Convert parent rotation from radians to degrees
                    parentRelativeRotation = (float)Math.toDegrees(parentRelativeRotation);
                }
            }
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return PEmitter.BASE_ALLOCATION_SIZE;
    }
}
