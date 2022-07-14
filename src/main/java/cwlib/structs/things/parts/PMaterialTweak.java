package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

public class PMaterialTweak implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    @Deprecated public float activation;

    public boolean hideInPlayMode;
    public float restitution, frictionScale;
    public byte grabbability, grabFilter, stickiness;
    public boolean noAutoDestruct, isProjectile;
    public boolean disablePhysicsAudio;

    public boolean isUsableByPoppetAudio;
    public boolean hasShadow, noBevel, zSlice, climbability;
    public boolean ppGrab;
    public byte ppTweakability;
    public boolean ppRigidConnection;
    public boolean ppMaterialMergeable;

    /* Vita  */

    public byte touchability;
    public int tweakType;
    public boolean highlight, includeRigidConnectors;
    public boolean droppable, requiresTag, requiresNewTouch;
    public float maxSpeed;
    public int colorIndex;
    public String name;
    public float touchRotationalStrength;
    public boolean touchabilityInvisible;

    @SuppressWarnings("unchecked")
    @Override public PMaterialTweak serialize(Serializer serializer, Serializable structure) {
        PMaterialTweak tweak = (structure == null) ? new PMaterialTweak() : (PMaterialTweak) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version < 0x2c4)
            tweak.activation = serializer.f32(tweak.activation);
        
        tweak.hideInPlayMode = serializer.bool(tweak.hideInPlayMode);

        if (version < 0x2cd)
            tweak.colorIndex = serializer.i32(tweak.colorIndex);

        if (version > 0x2b6)
            tweak.restitution = serializer.f32(tweak.restitution);
        if (version > 0x30c)
            tweak.frictionScale = serializer.f32(tweak.frictionScale);

        if (version >= 0x2b7 && version < 0x343) {
            serializer.u8(0);
            serializer.u8(0);
        }

        if (version > 0x342)
            tweak.grabbability = serializer.i8(tweak.grabbability);
        if (version > 0x3bc)
            tweak.grabFilter = serializer.i8(tweak.grabFilter);
        if (version > 0x342)
            tweak.stickiness = serializer.i8(tweak.stickiness);

        if (version > 0x2b8)
            tweak.noAutoDestruct = serializer.bool(tweak.noAutoDestruct);
        if (subVersion > 0x2)
            tweak.isProjectile = serializer.bool(tweak.isProjectile);

        if (version >= 0x2df && version < 0x327)
            serializer.thing(null);
        if (version > 0x356)
            tweak.disablePhysicsAudio = serializer.bool(tweak.disablePhysicsAudio);

        if (revision.isVita()) { // 0x3c0
            int vita = revision.getBranchRevision();
            if (vita >= 0x5)
                tweak.touchability = serializer.i8(tweak.touchability);
            if (vita >= 0x7) {
                tweak.tweakType = serializer.i32(tweak.tweakType);
                tweak.highlight = serializer.bool(tweak.highlight);
                tweak.includeRigidConnectors = serializer.bool(tweak.includeRigidConnectors);
                tweak.droppable = serializer.bool(tweak.droppable);
                tweak.requiresTag = serializer.bool(tweak.requiresTag);
                tweak.requiresNewTouch = serializer.bool(tweak.requiresNewTouch);
                tweak.maxSpeed = serializer.f32(tweak.maxSpeed);
                tweak.colorIndex = serializer.s32(tweak.colorIndex);
            }
            if (vita >= 0x49) // 0x3e1
                tweak.touchRotationalStrength = serializer.f32(tweak.touchRotationalStrength);
            if (vita >= 0x6c) // 0x3c3
                tweak.touchabilityInvisible = serializer.bool(tweak.touchabilityInvisible);
            if (vita >= 0x7) // 0x3c0
                tweak.name = serializer.wstr(tweak.name);
        }
        
        if (subVersion > 0x3)
            tweak.isUsableByPoppetAudio = serializer.bool(tweak.isUsableByPoppetAudio);
        if (version > 0x3ec || subVersion > 5)
            tweak.hasShadow = serializer.bool(tweak.hasShadow);
        if (subVersion > 0x33)
            tweak.noBevel = serializer.bool(tweak.noBevel);
        if (subVersion > 0xdb)
            tweak.zSlice = serializer.bool(tweak.zSlice);
        if (subVersion > 0x48)
            tweak.climbability = serializer.bool(tweak.climbability);
        if (subVersion > 0x8c) {
            tweak.ppGrab = serializer.bool(tweak.ppGrab);
            tweak.ppTweakability = serializer.i8(tweak.ppTweakability);
            tweak.ppRigidConnection = serializer.bool(tweak.ppRigidConnection);
        }

        if (subVersion >= 0x8d) {
            if (subVersion < 0x13d) serializer.f32(0);
            if (subVersion < 0x92) serializer.u8(0);
            if (subVersion >= 0x92 && subVersion < 0x13d)
                serializer.i32(0);
        }

        if (subVersion > 0xa7)
            tweak.ppMaterialMergeable = serializer.bool(tweak.ppMaterialMergeable);
        
        return tweak;
    }

    @Override public int getAllocatedSize() { return PMaterialTweak.BASE_ALLOCATION_SIZE; }
}
