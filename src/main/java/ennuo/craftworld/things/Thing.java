package ennuo.craftworld.things;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.things.parts.*;

public class Thing implements Serializable {
    public PBody body;
    public PJoint joint;
    public PWorld world;
    public PRenderMesh renderMesh;
    public PPos pos;
    public PTrigger trigger;
    public PYellowHead yellowHead;
    public PAudioWorld audioWorld;
    public PAnimation animation;
    public PGeneratedMesh generatedMesh;
    public PLevelSettings levelSettings;
    public PSpriteLight spriteLight;
    public PScriptName scriptName;
    public PCreature creature;
    public PCheckpoint checkpoint;
    public PStickers stickers;
    public PDecorations decorations;
    public PScript script;
    public PShape shape;
    public PEffector effector;
    public PEmitter emitter;
    public PRef ref;
    public PMetadata metadata;
    public PCostume costume;
    public PCameraTweak cameraTweak;
    public PSwitch switchBase;
    public PSwitchKey switchKey;
    public PGameplayData gameplayData;
    public PEnemy enemy;
    public PGroup group;
    public PPhysicsTweak physicsTweak;
    
    public Thing firstChild;
    public Thing nextSibling;
    public Thing parent;
    public Thing root;
    public Thing groupHead;
    
    public Thing microchip;
    public Thing[] inputs;
    public Thing bodyRoot;
    public Thing[] jointList;
    
    public long planGUID;
    public int UID;
    public short createdBy = -1;
    public short changedBy = -1;
    public byte flags = 0;
    public boolean stamping;
    public boolean hidden;
    
    public static int getCompressedPartsRevision(int revision) {
        if (revision <= 0x272) return PartHistory.PHYSICS_TWEAK;
        if (revision <= 0x2c3) return PartHistory.MATERIAL_TWEAK;
        return 0;
    }
    
    public static long getCompressedPartsFlags(Thing thing, int revision) {
        int parts = Thing.getCompressedPartsRevision(revision);
        long flags = 0;
        if (parts == 0) return flags;
        return 0xFFFFFFFFFFFFFFFFL;
    }
    
    public Thing serialize(Serializer serializer, Serializable structure) {
        Thing thing = (structure == null) ? new Thing() : (Thing) structure;
        
        if (serializer.revision.head > 0x2a0 || serializer.revision.isAfterLeerdammerRevision(5))
            serializer.i8((byte) 0xAA); // test_serialize_marker
        
        if (serializer.revision.head < 0x27f) {
            thing.parent = serializer.reference(thing.parent, Thing.class);
            thing.UID = serializer.i32(thing.UID);
        } else {
            thing.UID = serializer.i32(thing.UID);
            thing.parent = serializer.reference(thing.parent, Thing.class);
        }
        
        thing.groupHead = serializer.reference(thing.groupHead, Thing.class);
        if (serializer.revision.head > 0x1c6)
            serializer.reference(null, Thing.class); // oldEmitter
        
        if (serializer.revision.head > 0x213) {
            thing.createdBy = serializer.i16(thing.createdBy);
            thing.changedBy = serializer.i16(thing.changedBy);
        }
        
        if (serializer.revision.head > 0x21a)
            thing.stamping = serializer.bool(thing.stamping);
        
        if (serializer.revision.head > 0x253)
            thing.planGUID = serializer.u32(thing.planGUID);
        
        if (serializer.revision.head > 0x2f1)
            thing.hidden = serializer.bool(thing.hidden);
        
        if (serializer.revision.head > 0x340)
            thing.flags = serializer.i8(thing.flags);
        
        long flags = Thing.getCompressedPartsFlags(thing, serializer.revision.head);
        int parts = Thing.getCompressedPartsRevision(serializer.revision.head);
        parts = (int) serializer.i64d((long) parts);
        
        if (serializer.revision.head > 0x297 || serializer.revision.isAfterLeerdammerRevision(2))
            flags = serializer.i64(flags);
        
        if (((flags & (1 << 0)) != 0) && parts >= PartHistory.BODY)
                thing.body = serializer.reference(thing.body, PBody.class);
        if (((flags & (1 << 1)) != 0) && parts >= PartHistory.JOINT)
                thing.joint = serializer.reference(thing.joint, PJoint.class);
        if (((flags & (1 << 2)) != 0) && parts >= PartHistory.WORLD)
                thing.world = serializer.reference(thing.world, PWorld.class);
        if (((flags & (1 << 3)) != 0) && parts >= PartHistory.RENDER_MESH)
                thing.renderMesh = serializer.reference(thing.renderMesh, PRenderMesh.class);
        if (((flags & (1 << 4)) != 0) && parts >= PartHistory.POS)
                thing.pos = serializer.reference(thing.pos, PPos.class);
        if (((flags & (1 << 5)) != 0) && parts >= PartHistory.TRIGGER)
                thing.trigger = serializer.reference(thing.trigger, PTrigger.class);
        if (((flags & (1 << 6)) != 0) && parts >= PartHistory.YELLOWHEAD)
                thing.yellowHead = serializer.reference(thing.yellowHead, PYellowHead.class);
        if (((flags & (1 << 7)) != 0) && parts >= PartHistory.AUDIO_WORLD)
                thing.audioWorld = serializer.reference(thing.audioWorld, PAudioWorld.class);
        if (((flags & (1 << 8)) != 0) && parts >= PartHistory.ANIMATION)
                thing.animation = serializer.reference(thing.animation, PAnimation.class);
        if (((flags & (1 << 9)) != 0) && parts >= PartHistory.GENERATED_MESH)
                thing.generatedMesh = serializer.reference(thing.generatedMesh, PGeneratedMesh.class);
        if (((flags & (1 << 10)) != 0) && parts >= PartHistory.LEVEL_SETTINGS)
                thing.levelSettings = serializer.reference(thing.levelSettings, PLevelSettings.class);
        if (((flags & (1 << 11)) != 0) && parts >= PartHistory.SPRITE_LIGHT)
                thing.spriteLight = serializer.reference(thing.spriteLight, PSpriteLight.class);
        if (((flags & (1 << 12)) != 0) && parts >= PartHistory.SCRIPT_NAME)
                thing.scriptName = serializer.reference(thing.scriptName, PScriptName.class);
        if (((flags & (1 << 13)) != 0) && parts >= PartHistory.CREATURE)
                thing.creature = serializer.reference(thing.creature, PCreature.class);
        if (((flags & (1 << 14)) != 0) && parts >= PartHistory.CHECKPOINT)
                thing.checkpoint = serializer.reference(thing.checkpoint, PCheckpoint.class);
        if (((flags & (1 << 15)) != 0) && parts >= PartHistory.STICKERS)
                thing.stickers = serializer.reference(thing.stickers, PStickers.class);
        if (((flags & (1 << 16)) != 0) && parts >= PartHistory.DECORATIONS)
                thing.decorations = serializer.reference(thing.decorations, PDecorations.class);
        if (((flags & (1 << 17)) != 0) && parts >= PartHistory.SCRIPT)
                thing.script = serializer.reference(thing.script, PScript.class);
        if (((flags & (1 << 18)) != 0) && parts >= PartHistory.SHAPE)
                thing.shape = serializer.reference(thing.shape, PShape.class);
        if (((flags & (1 << 19)) != 0) && parts >= PartHistory.EFFECTOR)
                thing.effector = serializer.reference(thing.effector, PEffector.class);
        if (((flags & (1 << 20)) != 0) && parts >= PartHistory.EMITTER)
                thing.emitter = serializer.reference(thing.emitter, PEmitter.class);
        if (((flags & (1 << 21)) != 0) && parts >= PartHistory.REF)
                thing.ref = serializer.reference(thing.ref, PRef.class);
        if (((flags & (1 << 22)) != 0) && parts >= PartHistory.METADATA)
                thing.metadata = serializer.reference(thing.metadata, PMetadata.class);
        if (((flags & (1 << 23)) != 0) && parts >= PartHistory.COSTUME)
                thing.costume = serializer.reference(thing.costume, PCostume.class);
        if (((flags & (1 << 24)) != 0) && parts >= PartHistory.CAMERA_TWEAK)
                thing.cameraTweak = serializer.reference(thing.cameraTweak, PCameraTweak.class);
        if (((flags & (1 << 25)) != 0) && parts >= PartHistory.SWITCH)
                thing.switchBase = serializer.reference(thing.switchBase, PSwitch.class);
        if (((flags & (1 << 26)) != 0) && parts >= PartHistory.SWITCH_KEY)
                thing.switchKey = serializer.reference(thing.switchKey, PSwitchKey.class);
        if (((flags & (1 << 27)) != 0) && parts >= PartHistory.GAMEPLAY_DATA)
                thing.gameplayData = serializer.reference(thing.gameplayData, PGameplayData.class);
        if (((flags & (1 << 28)) != 0) && parts >= PartHistory.ENEMY)
                thing.enemy = serializer.reference(thing.enemy, PEnemy.class);
        if (((flags & (1 << 29)) != 0) && parts >= PartHistory.GROUP)
                thing.group = serializer.reference(thing.group, PGroup.class);
        if (((flags & (1 << 30)) != 0) && parts >= PartHistory.PHYSICS_TWEAK)
                thing.physicsTweak = serializer.reference(thing.physicsTweak, PPhysicsTweak.class);

        return thing;
    }    
}
