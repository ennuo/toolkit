package ennuo.craftworld.things;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.things.parts.*;

public class Thing implements Serializable {
    PBody body;
    PJoint joint;
    PWorld world;
    PRenderMesh renderMesh;
    PPos pos;
    PTrigger trigger;
    PYellowHead yellowHead;
    PAudioWorld audioWorld;
    PAnimation animation;
    PGeneratedMesh generatedMesh;
    PLevelSettings levelSettings;
    PSpriteLight spriteLight;
    PScriptName scriptName;
    PCreature creature;
    PCheckpoint checkpoint;
    PStickers stickers;
    PDecorations decorations;
    PScript script;
    PShape shape;
    PEffector effector;
    PEmitter emitter;
    PRef ref;
    PMetadata metadata;
    PCostume costume;
    PCameraTweak cameraTweak;
    PSwitch switchBase;
    PSwitchKey switchKey;
    PGameplayData gameplayData;
    PEnemy enemy;
    PGroup group;
    PPhysicsTweak physicsTweak;
    
    Thing firstChild;
    Thing nextSibling;
    Thing parent;
    Thing root;
    Thing groupHead;
    
    Thing microchip;
    Thing[] inputs;
    Thing bodyRoot;
    Thing[] jointList;
    
    long planGUID;
    int UID;
    short createdBy = -1;
    short changedBy = -1;
    short flags = 0;
    
    public Thing serialize(Serializer serializer, Serializable structure) {
        Thing thing = (structure == null) ? new Thing() : (Thing) structure;
        
        return thing;
    }
    
}
