package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

public class PMaterialTweak implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    @GsonRevision(max = 0x2c3)
    @Deprecated
    public float activation;

    public boolean hideInPlayMode;

    @GsonRevision(min = 0x2b7)
    public float restitution;

    @GsonRevision(min = 0x30d)
    public float frictionScale;

    @GsonRevision(min = 0x343)
    public byte grabbability;

    @GsonRevision(min = 0x3bd)
    public byte grabFilter;

    @GsonRevision(min = 0x343)
    public byte stickiness;

    @GsonRevision(min = 0x2b9)
    public boolean noAutoDestruct;

    @GsonRevision(lbp3 = true, min = 0x3)
    public boolean isProjectile;

    @GsonRevision(min = 0x357)
    public boolean disablePhysicsAudio;

    @GsonRevision(lbp3 = true, min = 0x4)
    public boolean isUsableByPoppetAudio;

    @GsonRevision(lbp3 = true, min = 6)
    @GsonRevision(min = 0x3ed)
    public boolean hasShadow;

    @GsonRevision(lbp3 = true, min = 0x34)
    public boolean noBevel;

    @GsonRevision(lbp3 = true, min = 0xdc)
    public boolean zSlice;

    @GsonRevision(lbp3 = true, min = 0x49)
    public boolean climbability;

    @GsonRevision(lbp3 = true, min = 0x8d)
    public boolean ppGrab;

    @GsonRevision(lbp3 = true, min = 0x8d)
    public byte ppTweakability;

    @GsonRevision(lbp3 = true, min = 0x8d)
    public boolean ppRigidConnection;

    @GsonRevision(lbp3 = true, min = 0xa8)
    public boolean ppMaterialMergeable;

    /* Vita  */

    @GsonRevision(branch = 0x4431, min = 0x5)
    public byte touchability;

    @GsonRevision(branch = 0x4431, min = 0x7)
    public int tweakType;

    @GsonRevision(branch = 0x4431, min = 0x7)
    public boolean highlight, includeRigidConnectors;

    @GsonRevision(branch = 0x4431, min = 0x7)
    public boolean droppable, requiresTag, requiresNewTouch;

    @GsonRevision(branch = 0x4431, min = 0x7)
    public float maxSpeed;

    @GsonRevision(max = 0x2cc)
    @GsonRevision(branch = 0x4431, min = 0x7)
    public int colorIndex;

    @GsonRevision(branch = 0x4431, min = 0x7)
    public String name;

    @GsonRevision(branch = 0x4431, min = 0x49)
    public float touchRotationalStrength;

    @GsonRevision(branch = 0x4431, min = 0x6c)
    public boolean touchabilityInvisible;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version < 0x2c4)
            activation = serializer.f32(activation);

        hideInPlayMode = serializer.bool(hideInPlayMode);

        if (version < 0x2cd)
            colorIndex = serializer.i32(colorIndex);

        if (version > 0x2b6)
            restitution = serializer.f32(restitution);
        if (version > 0x30c)
            frictionScale = serializer.f32(frictionScale);

        if (version >= 0x2b7 && version < 0x343)
        {
            serializer.u8(0);
            serializer.u8(0);
        }

        if (version > 0x342)
            grabbability = serializer.i8(grabbability);
        if (version > 0x3bc)
            grabFilter = serializer.i8(grabFilter);
        if (version > 0x342)
            stickiness = serializer.i8(stickiness);

        if (version > 0x2b8)
            noAutoDestruct = serializer.bool(noAutoDestruct);
        if (subVersion > 0x2)
            isProjectile = serializer.bool(isProjectile);

        if (version >= 0x2df && version < 0x327)
            serializer.thing(null);
        if (version > 0x356)
            disablePhysicsAudio = serializer.bool(disablePhysicsAudio);

        if (revision.isVita())
        { // 0x3c0
            int vita = revision.getBranchRevision();
            if (vita >= 0x5)
                touchability = serializer.i8(touchability);
            if (vita >= 0x7)
            {
                tweakType = serializer.i32(tweakType);
                highlight = serializer.bool(highlight);
                includeRigidConnectors = serializer.bool(includeRigidConnectors);
                droppable = serializer.bool(droppable);
                requiresTag = serializer.bool(requiresTag);
                requiresNewTouch = serializer.bool(requiresNewTouch);
                maxSpeed = serializer.f32(maxSpeed);
                colorIndex = serializer.s32(colorIndex);
            }
            if (vita >= 0x49) // 0x3e1
                touchRotationalStrength = serializer.f32(touchRotationalStrength);
            if (vita >= 0x6c) // 0x3c3
                touchabilityInvisible = serializer.bool(touchabilityInvisible);
            if (vita >= 0x7) // 0x3c0
                name = serializer.wstr(name);
        }

        if (subVersion > 0x3)
            isUsableByPoppetAudio = serializer.bool(isUsableByPoppetAudio);
        if (version > 0x3ec || subVersion > 5)
            hasShadow = serializer.bool(hasShadow);
        if (subVersion > 0x33)
            noBevel = serializer.bool(noBevel);
        if (subVersion > 0xdb)
            zSlice = serializer.bool(zSlice);
        if (subVersion > 0x48)
            climbability = serializer.bool(climbability);
        if (subVersion > 0x8c)
        {
            ppGrab = serializer.bool(ppGrab);
            ppTweakability = serializer.i8(ppTweakability);
            ppRigidConnection = serializer.bool(ppRigidConnection);
        }

        if (subVersion >= 0x8d)
        {
            if (subVersion < 0x13d) serializer.f32(0);
            if (subVersion < 0x92) serializer.u8(0);
            if (subVersion >= 0x92 && subVersion < 0x13d)
                serializer.i32(0);
        }

        if (subVersion > 0xa7)
            ppMaterialMergeable = serializer.bool(ppMaterialMergeable);
    }

    @Override
    public int getAllocatedSize()
    {
        return PMaterialTweak.BASE_ALLOCATION_SIZE;
    }
}
