package cwlib.resources;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import cwlib.enums.Branch;
import cwlib.enums.InventoryObjectType;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.types.Resource;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.inventory.UserCreatedDetails;
import cwlib.structs.level.AdventureData;
import cwlib.structs.level.CachedInventoryData;
import cwlib.structs.level.PlayerRecord;
import cwlib.structs.profile.InventoryItem;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PBody;
import cwlib.structs.things.parts.PEffector;
import cwlib.structs.things.parts.PGameplayData;
import cwlib.structs.things.parts.PMetadata;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PScript;
import cwlib.structs.things.parts.PWorld;

public class RLevel implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    @GsonRevision(min=0x3e6) public SHA1[] crossPlayVitaDependencyHashes;

    public Thing world;
    
    @GsonRevision(min=0x214) public PlayerRecord playerRecord = new PlayerRecord();

    @GsonRevision(min=0x348) public ArrayList<InventoryItem> tutorialInventory = new ArrayList<>();
    @GsonRevision(min=0x35d) public ArrayList<CachedInventoryData> tutorialInventoryData = new ArrayList<>();
    @GsonRevision(min=0x39c) public boolean tutorialInventoryActive;
    @GsonRevision(min=0x3b1) public boolean copiedFromSomeoneElse;

    // Vita
    @GsonRevision(branch=0x4431, min=0x70)
    public GUID musicGUID, musicSettingsGUID;
    @GsonRevision(branch=0x4431, min=0x70)
    public float[] musicStemVolumes;

    @GsonRevision(lbp3=true, min=0xfa)
    public byte[] dceUuid;

    @GsonRevision(lbp3=true, min=0x169)
    public AdventureData adventureData;

    public RLevel() {
        Thing thing = new Thing(-1);

        PWorld world = new PWorld();

        thing.setPart(Part.BODY, new PBody());
        thing.setPart(Part.WORLD, world);
        thing.setPart(Part.POS, new PPos());
        thing.setPart(Part.EFFECTOR, new PEffector());

        PScript script = new PScript();
        script.instance.script = new ResourceDescriptor(19744, ResourceType.SCRIPT);

        thing.setPart(Part.SCRIPT, script);
        thing.setPart(Part.GAMEPLAY_DATA, new PGameplayData());

        world.things.add(thing);

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
        
        if (subVersion > 0x34 && subVersion < 0x91)
            serializer.bool(false);
        if (subVersion > 0x34 && subVersion < 0xb3) 
            serializer.bool(false);
        if (subVersion > 0x94 && subVersion < 0x12a)
            serializer.bool(false); // savedThroughPusher
        
        if (subVersion >= 0xf1 && subVersion <= 0xf9)
            throw new SerializationException("Legacy adventure data not supported in serialization!");

        if (subVersion >= 0xfa)
            level.dceUuid = serializer.bytearray(level.dceUuid);

        if (subVersion >= 0x161 && subVersion < 0x169)
            serializer.resource(null, ResourceType.ADVENTURE_SHARED_DATA);

        if (subVersion >= 0x169)
            level.adventureData = serializer.reference(level.adventureData, AdventureData.class);
        
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
    
    public int getNextUID() {
        return ++((PWorld)this.world.getPart(Part.WORLD)).thingUIDCounter;
    }

    public void addPlan(RPlan plan) {
        Thing[] things = plan.getThings();
        PWorld world = this.world.getPart(Part.WORLD);
        for (Thing thing : things) {
            if (thing != null) {
                thing.UID = this.getNextUID();
                synchronized(world.things) {
                    world.things.add(thing);
                }
            }
        }
    }

    public byte[] toPlan() {
        RPlan plan = new RPlan();
        plan.revision = new Revision(0x272, 0x4c44, 0x0017);
        plan.compressionFlags = 0x7;
        ArrayList<Thing> things = new ArrayList<>();
        PWorld world = ((PWorld)this.world.getPart(Part.WORLD));
        for (Thing thing : world.things) {
            if (thing == this.world) continue;
            if (thing == world.backdrop) continue;

            things.add(thing);
        }

        plan.setThings(things.toArray(Thing[]::new));

        plan.inventoryData = new InventoryItemDetails();
        plan.inventoryData.type = EnumSet.of(InventoryObjectType.READYMADE);
        plan.inventoryData.icon = new ResourceDescriptor(2551, ResourceType.TEXTURE);
        plan.inventoryData.userCreatedDetails = new UserCreatedDetails("World Export", "Exported world");


        return Resource.compress(plan.build());
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
