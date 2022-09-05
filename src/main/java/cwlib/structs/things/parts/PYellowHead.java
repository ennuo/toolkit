package cwlib.structs.things.parts;

import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.poppet.Poppet;

public class PYellowHead implements Serializable {
    public Thing head;
    public Thing legacyToolTetherJoint;
    public float legacyToolTetherWidth;
    public int playerNumber;
    public Poppet poppet;
    public Vector4f[] sensorHistory;
    public int newestSense;
    public boolean requestedSuicide;
    public Thing legacyJetpack;
    public float onScreenCounter;
    public byte onScreenStatus;
    public boolean editJetpack;
    public boolean recording;
    public Thing recordee;

    public int lastTimeSlappedAPlayer;
    public int animSetKey;
    public boolean monstrousHeadScale;
    public int creatureToSpawnAs;
    public boolean spawnAsAlternateForm;


    @SuppressWarnings("unchecked")
    @Override public PYellowHead serialize(Serializer serializer, Serializable structure) {
        PYellowHead player = (structure == null) ? new PYellowHead() : (PYellowHead) structure;
        
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        player.head = serializer.thing(player.head);
        if (version > 0x1fc)
            player.legacyToolTetherJoint = serializer.thing(player.legacyToolTetherJoint);
        else
            serializer.i32(0);

        if (version < 0x13c) serializer.v4(null);
        if (version < 0x8c) serializer.i32(0);
        if (version < 0x1d6) serializer.resource(null, ResourceType.GFX_MATERIAL);

        player.legacyToolTetherWidth = serializer.f32(player.legacyToolTetherWidth);
        player.playerNumber = serializer.s32(player.playerNumber);

        if (version < 0x155) serializer.i32(0);
        if (version < 0x20c) serializer.bool(false);
        if (version < 0x161) serializer.bool(false);
        if (version == 0x170) serializer.bool(false);


        if (version < 0x17f || (version > 0x184 && version < 0x192) || version > 0x1b5) {
            player.poppet = serializer.reference(player.poppet, Poppet.class);
            serializer.log("POPPET_INSTANCE");
            //System.exit(1);
        }

        if (subVersion > 0xc && subVersion <= 0x65) {
            // figure this out later
        }

        if (version < 0x3da) 
            player.sensorHistory = serializer.vectorarray(player.sensorHistory);

        if (version < 0x203) serializer.f32(0.0f);

        player.newestSense = serializer.i32(player.newestSense);

        if (version >= 0x146 && version < 0x16b)
            serializer.i32(0);
        
        if (version >= 0x16b)
            player.requestedSuicide = serializer.bool(player.requestedSuicide);
        
        player.legacyJetpack = serializer.thing(player.legacyJetpack);

        if (version > 0x134 && version < 0x164) {
            serializer.i32(0); // Primary
            serializer.i32(0); // Secondary
            serializer.i32(0); // Tertiary
        }

        if (version > 0x193)
            player.onScreenCounter = serializer.f32(player.onScreenCounter);
        
        if (version > 0x1ba && version < 0x1fd)
            serializer.i32(0);

        if (version > 0x1d0)
            player.onScreenStatus = serializer.i8(player.onScreenStatus);

        if (version > 0x1d3)
            player.editJetpack = serializer.bool(player.editJetpack);

        if (version > 0x272) {
            player.recording = serializer.bool(player.recording);
            player.recordee = serializer.thing(player.recordee);
        }

        if (version > 0x359)
            player.lastTimeSlappedAPlayer = serializer.i32(player.lastTimeSlappedAPlayer);

        if (subVersion > 0x83 && subVersion <= 0x8a) {
            // figure this out later
        }

        if (subVersion > 0xa5)
            player.animSetKey = serializer.i32(player.animSetKey);

        if (subVersion > 0xd2)
            player.monstrousHeadScale = serializer.bool(player.monstrousHeadScale);
        
        if (subVersion > 0x12a) {
            player.creatureToSpawnAs = serializer.i32(player.creatureToSpawnAs); 
            player.spawnAsAlternateForm = serializer.bool(player.spawnAsAlternateForm);
        }
        
        return player;
    }

    @Override public int getAllocatedSize() { return 0; }
}
