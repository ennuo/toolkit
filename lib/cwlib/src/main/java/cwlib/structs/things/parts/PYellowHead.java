package cwlib.structs.things.parts;

import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.poppet.Poppet;

public class PYellowHead implements Serializable
{
    public Thing head;
    public Thing legacyToolTetherJoint;
    public float legacyToolTetherWidth;
    public int playerNumber;
    public Poppet poppet;

    @GsonRevision(max = 0x3d9)
    public Vector4f[] sensorHistory;
    @GsonRevision(max = 0x3d9)
    public int newestSense;

    @GsonRevision(min = 0x16b)
    public boolean requestedSuicide;

    public Thing legacyJetpack;

    @GsonRevision(min = 0x194)
    public float onScreenCounter;

    @GsonRevision(min = 0x1d1)
    public byte onScreenStatus;

    @GsonRevision(min = 0x1d4)
    public boolean editJetpack;

    @GsonRevision(min = 0x273)
    public boolean recording;

    @GsonRevision(min = 0x273)
    public Thing recordee;

    @GsonRevision(min = 0x35a)
    public int lastTimeSlappedAPlayer;

    @GsonRevision(min = 0xa6, lbp3 = true)
    public int animSetKey;

    @GsonRevision(min = 0xd3, lbp3 = true)
    public boolean monstrousHeadScale;

    @GsonRevision(min = 0x12b, lbp3 = true)
    public int creatureToSpawnAs;

    @GsonRevision(min = 0x12b, lbp3 = true)
    public boolean spawnAsAlternateForm;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        head = serializer.thing(head);
        if (version > 0x1fc)
            legacyToolTetherJoint = serializer.thing(legacyToolTetherJoint);
        else
            serializer.i32(0);

        if (version < 0x13c) serializer.v4(null);
        if (version < 0x8c) serializer.i32(0);
        if (version < 0x1d6) serializer.resource(null, ResourceType.GFX_MATERIAL);

        legacyToolTetherWidth = serializer.f32(legacyToolTetherWidth);
        playerNumber = serializer.s32(playerNumber);

        if (version < 0x155) serializer.i32(0);
        if (version < 0x20c) serializer.bool(false);
        if (version < 0x161) serializer.bool(false);
        if (version == 0x170) serializer.bool(false);

        if (version < 0x17f || (version > 0x184 && version < 0x192) || version > 0x1b5)
            poppet = serializer.reference(poppet, Poppet.class);

        if (subVersion >= 0xc && subVersion < 0x66)
            throw new SerializationException("Unknown serialization object in PYellowHead!");

        if (version < 0x3da)
            sensorHistory = serializer.vectorarray(sensorHistory);

        if (version < 0x203) serializer.f32(0.0f);

        if (version < 0x3da)
            newestSense = serializer.i32(newestSense);

        if (version >= 0x146 && version < 0x16b)
            serializer.i32(0);

        if (version >= 0x16b)
            requestedSuicide = serializer.bool(requestedSuicide);

        legacyJetpack = serializer.thing(legacyJetpack);

        if (version > 0x134 && version < 0x164)
        {
            serializer.i32(0); // Primary
            serializer.i32(0); // Secondary
            serializer.i32(0); // Tertiary
        }

        if (version > 0x193)
            onScreenCounter = serializer.f32(onScreenCounter);

        if (version > 0x1ba && version < 0x1fd)
            serializer.i32(0);

        if (version > 0x1d0)
            onScreenStatus = serializer.i8(onScreenStatus);

        if (version > 0x1d3)
            editJetpack = serializer.bool(editJetpack);

        if (version > 0x272)
        {
            if (version < 0x2df)
                recording = serializer.bool(recording);
            recordee = serializer.thing(recordee);
        }

        if (version > 0x359)
            lastTimeSlappedAPlayer = serializer.i32(lastTimeSlappedAPlayer);

        if (subVersion >= 0x83 && subVersion < 0x8b) serializer.u8(0);
        if (subVersion >= 0x88 && subVersion < 0xa3)
            throw new SerializationException("Unknown serialization object in PYellowHead!");

        if (subVersion > 0xa5)
            animSetKey = serializer.i32(animSetKey);

        if (subVersion > 0xd2)
            monstrousHeadScale = serializer.bool(monstrousHeadScale);

        if (subVersion > 0x12a)
        {
            creatureToSpawnAs = serializer.i32(creatureToSpawnAs);
            spawnAsAlternateForm = serializer.bool(spawnAsAlternateForm);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return 0;
    }
}
