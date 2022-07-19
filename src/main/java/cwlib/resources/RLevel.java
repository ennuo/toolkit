package cwlib.resources;

import java.util.ArrayList;
import java.util.HashMap;

import cwlib.enums.Branch;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.level.CachedInventoryData;
import cwlib.structs.level.PlayerRecord;
import cwlib.structs.profile.InventoryItem;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PBody;
import cwlib.structs.things.parts.PEffector;
import cwlib.structs.things.parts.PGameplayData;
import cwlib.structs.things.parts.PLevelSettings;
import cwlib.structs.things.parts.PMetadata;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PScript;
import cwlib.structs.things.parts.PWorld;

public class RLevel implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public SHA1[] crossPlayVitaDependencyHashes;

    public Thing world;
    public PlayerRecord playerRecord = new PlayerRecord();

    public ArrayList<InventoryItem> tutorialInventory = new ArrayList<>();
    public ArrayList<CachedInventoryData> tutorialInventoryData = new ArrayList<>();
    public boolean tutorialInventoryActive;
    public boolean copiedFromSomeoneElse;

    // Vita
    public GUID musicGUID;
    public GUID musicSettingsGUID;
    public float[] musicStemVolumes;

    public RLevel() {
        Thing thing = new Thing(-1);

        PWorld world = new PWorld();

        thing.setPart(Part.BODY, new PBody());
        thing.setPart(Part.WORLD, world);
        thing.setPart(Part.POS, new PPos());
        thing.setPart(Part.LEVEL_SETTINGS, new PLevelSettings());
        thing.setPart(Part.EFFECTOR, new PEffector());

        PScript script = new PScript();
        script.instance.script = new ResourceDescriptor(19744, ResourceType.SCRIPT);

        thing.setPart(Part.SCRIPT, script);
        thing.setPart(Part.GAMEPLAY_DATA, new PGameplayData());

        world.things.add(thing);
        // world.things.add(new Thing(1));

        this.world = thing;
    }

    @SuppressWarnings("unchecked")
    @Override public RLevel serialize(Serializer serializer, Serializable structure) {
        RLevel level = (structure == null) ? new RLevel() : (RLevel) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version >= 0x3e6) {
            if (!serializer.isWriting()) level.crossPlayVitaDependencyHashes = new SHA1[serializer.getInput().i32()];
            else {
                if (level.crossPlayVitaDependencyHashes == null)
                    level.crossPlayVitaDependencyHashes = new SHA1[0];
                serializer.getOutput().i32(level.crossPlayVitaDependencyHashes.length);
            }
            for (int i = 0; i < level.crossPlayVitaDependencyHashes.length; ++i)
                level.crossPlayVitaDependencyHashes[i] = serializer.sha1(level.crossPlayVitaDependencyHashes[i]);
        }

        level.world = serializer.reference(level.world, Thing.class);
        if (version > 0x213)
            level.playerRecord = serializer.struct(level.playerRecord, PlayerRecord.class);
        
        if (version > 0x347) level.tutorialInventory = serializer.arraylist(level.tutorialInventory, InventoryItem.class);
        if (version > 0x35c) level.tutorialInventoryData = serializer.arraylist(level.tutorialInventoryData, CachedInventoryData.class);
        if (version > 0x39b) level.tutorialInventoryActive = serializer.bool(level.tutorialInventoryActive);
        if (version > 0x3b0) level.copiedFromSomeoneElse = serializer.bool(level.copiedFromSomeoneElse);

        if (revision.has(Branch.DOUBLE11, 0x70)) { 
            level.musicGUID = serializer.guid(level.musicGUID);
            level.musicSettingsGUID = serializer.guid(level.musicSettingsGUID);
            level.musicStemVolumes = serializer.floatarray(level.musicStemVolumes);
        }

        if (subVersion > 0x34 && subVersion < 0x91) { /* some bool */}
        if (subVersion > 0x34 && subVersion < 0xb3) { /* some bool */}
        if (subVersion > 0x94 && subVersion < 0x12a) { /* some bool */}


        if (subVersion >= 0x169) { /* adventure data */}
        
        // dceUuid
        // adventureData

        serializer.log("END PARSE");
        
        return level;
    }

    private ArrayList<Thing> getAllReferences(ArrayList<Thing> things, Thing thing) {
        PWorld world = (this.world.getPart(Part.WORLD));
        if (!things.contains(thing)) things.add(thing);
        for (Thing worldThing : world.things) {
            if (worldThing == null || worldThing == thing) continue;
            if (worldThing.getPart(Part.WORLD) != null || things.contains(worldThing)) continue;
            if (worldThing.parent == thing || (thing.groupHead != null && (worldThing.groupHead == thing.groupHead)))
                this.getAllReferences(things, worldThing);
        }
        return things;
    }

    public HashMap<String, RPlan> getPalettes(String name, Revision revision, byte compressionFlags, boolean includeChildren) {
        HashMap<String, RPlan> plans = new HashMap<>();
        PWorld world = this.world.getPart(Part.WORLD);
        Thing.SERIALIZE_WORLD_THING = false;
        for (Thing thing : world.things) {
            if (thing == null || thing.getPart(Part.WORLD) != null) continue;
            PMetadata metadata = thing.getPart(Part.METADATA);
            if (metadata == null) continue;
            if (includeChildren) {
                Thing[] things = this.getAllReferences(new ArrayList<>(), thing).toArray(Thing[]::new);
                plans.put(name + "_" + thing.UID + ".plan", new RPlan(revision, compressionFlags, things, metadata));
            } else 
                plans.put(name + "_" + thing.UID + ".plan", new RPlan(revision, compressionFlags, thing, metadata));
        }
        Thing.SERIALIZE_WORLD_THING = true;
        return plans;
    }
    
    @Override public int getAllocatedSize() { 
        int size = BASE_ALLOCATION_SIZE;
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        // 16MB buffer for generation of levels, since the allocated size will get
        // stuck in a recursive loop until I fix it.
        Serializer serializer = new Serializer(0x1000000, revision, compressionFlags);
        serializer.struct(this, RLevel.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags,
            ResourceType.LEVEL,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }
}
