package cwlib.resources;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.Branch;
import cwlib.enums.EnemyPart;
import cwlib.enums.InventoryObjectType;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.enums.SwitchType;
import cwlib.enums.TriggerType;
import cwlib.enums.VisibilityFlags;
import cwlib.ex.SerializationException;
import cwlib.types.SerializedResource;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.io.Resource;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.inventory.UserCreatedDetails;
import cwlib.structs.level.AdventureData;
import cwlib.structs.level.CachedInventoryData;
import cwlib.structs.level.PlayerRecord;
import cwlib.structs.profile.InventoryItem;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.switches.SwitchOutput;
import cwlib.structs.things.components.switches.SwitchTarget;
import cwlib.structs.things.parts.PAudioWorld;
import cwlib.structs.things.parts.PBody;
import cwlib.structs.things.parts.PCreature;
import cwlib.structs.things.parts.PEffector;
import cwlib.structs.things.parts.PEmitter;
import cwlib.structs.things.parts.PEnemy;
import cwlib.structs.things.parts.PGameplayData;
import cwlib.structs.things.parts.PGeneratedMesh;
import cwlib.structs.things.parts.PGroup;
import cwlib.structs.things.parts.PMetadata;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PRef;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.structs.things.parts.PScript;
import cwlib.structs.things.parts.PSpriteLight;
import cwlib.structs.things.parts.PSwitch;
import cwlib.structs.things.parts.PSwitchInput;
import cwlib.structs.things.parts.PTrigger;
import cwlib.structs.things.parts.PWorld;

public class RLevel implements Resource
{
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    @GsonRevision(min = 0x3e6)
    public SHA1[] crossPlayVitaDependencyHashes = new SHA1[0];

    public Thing worldThing;

    @GsonRevision(min = 0x214)
    public PlayerRecord playerRecord = new PlayerRecord();

    @GsonRevision(min = 0x348)
    public ArrayList<InventoryItem> tutorialInventory = new ArrayList<>();
    @GsonRevision(min = 0x35d)
    public ArrayList<CachedInventoryData> tutorialInventoryData = new ArrayList<>();
    @GsonRevision(min = 0x39c)
    public boolean tutorialInventoryActive;
    @GsonRevision(min = 0x3b1)
    public boolean copiedFromSomeoneElse;

    // Vita
    @GsonRevision(branch = 0x4431, min = 0x70)
    public GUID musicGUID, musicSettingsGUID;
    @GsonRevision(branch = 0x4431, min = 0x70)
    public float[] musicStemVolumes;

    @GsonRevision(lbp3 = true, min = 0xfa)
    public byte[] dceUuid;

    @GsonRevision(lbp3 = true, min = 0x169)
    public AdventureData adventureData;

    public RLevel()
    {
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

        this.worldThing = thing;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        // Should move this to a common place at some point.
        if (serializer.isWriting()) onStartSave(revision);


        if (version > 0x3e6)
        {
            if (!serializer.isWriting())
                crossPlayVitaDependencyHashes = new SHA1[serializer.getInput().i32()];
            else
            {
                if (crossPlayVitaDependencyHashes == null)
                    crossPlayVitaDependencyHashes = new SHA1[0];
                serializer.getOutput().i32(crossPlayVitaDependencyHashes.length);
            }
            for (int i = 0; i < crossPlayVitaDependencyHashes.length; ++i)
                crossPlayVitaDependencyHashes[i] =
                    serializer.sha1(crossPlayVitaDependencyHashes[i]);
        }

        worldThing = serializer.reference(worldThing, Thing.class);
        if (version > 0x213)
            playerRecord = serializer.struct(playerRecord, PlayerRecord.class);

        if (version > 0x347)
            tutorialInventory = serializer.arraylist(tutorialInventory,
                InventoryItem.class);
        if (version > 0x35c)
            tutorialInventoryData = serializer.arraylist(tutorialInventoryData,
                CachedInventoryData.class);
        if (version > 0x39b)
            tutorialInventoryActive = serializer.bool(tutorialInventoryActive);
        if (version > 0x3b0)
            copiedFromSomeoneElse = serializer.bool(copiedFromSomeoneElse);

        if (revision.has(Branch.DOUBLE11, 0x70))
        {
            musicGUID = serializer.guid(musicGUID);
            musicSettingsGUID = serializer.guid(musicSettingsGUID);
            musicStemVolumes = serializer.floatarray(musicStemVolumes);
        }

        if (subVersion > 0x34 && subVersion < 0x91)
            serializer.bool(false);
        if (subVersion > 0x34 && subVersion < 0xb3)
            serializer.bool(false);
        if (subVersion > 0x94 && subVersion < 0x12a)
            serializer.bool(false); // savedThroughPusher

        if (subVersion >= 0xf1 && subVersion <= 0xf9)
            throw new SerializationException("Legacy adventure data not supported in " +
                                             "serialization!");

        if (subVersion >= 0xfa)
            dceUuid = serializer.bytearray(dceUuid);

        if (subVersion >= 0x161 && subVersion < 0x169)
            serializer.resource(null, ResourceType.ADVENTURE_SHARED_DATA);

        if (subVersion >= 0x169)
            adventureData = serializer.reference(adventureData, AdventureData.class);

        // Should move this to a common place at some point.
        if (!serializer.isWriting()) onLoadFinished(revision);
    }

    public void fixup(Revision revision)
    {
        if (worldThing != null)
            worldThing.fixup(revision);
    }

    private boolean isValidLevel()
    {
        // Who's serializing a level without a world thing?
        if (worldThing == null) return false;

        PWorld world = worldThing.getPart(Part.WORLD);

        // Just in case somebody's serializing a world without a world for whatever reason.
        return world != null;
    }

    @Override
    public void onLoadFinished(Revision revision)
    {
        fixup(revision);
    }

    @Override
    public void onStartSave(Revision revision)
    {
        if (!isValidLevel()) return;
        int version = revision.getVersion();
        PWorld world = worldThing.getPart(Part.WORLD);

        // Don't know the exact revision the scripts were removed, but deploy and below is a good
          // guess,
        // since that's when they overhauled the switch system, LBP2 already removes them so.
        if (version <= 0x2c3)
        {
            // Attach missing scripts to components if we're serializing to LBP1 from a later
              // version.
            ResourceDescriptor switchBaseScript = new ResourceDescriptor(42511,
                ResourceType.SCRIPT);
            ResourceDescriptor tweakJointScript = new ResourceDescriptor(19749,
                ResourceType.SCRIPT);
            ResourceDescriptor checkpointScript = new ResourceDescriptor(11757,
                ResourceType.SCRIPT);
            ResourceDescriptor triggerCollectScript = new ResourceDescriptor(11538,
                ResourceType.SCRIPT);
            ResourceDescriptor tweakEggScript = new ResourceDescriptor(27432, ResourceType.SCRIPT);
            ResourceDescriptor enemyScript = new ResourceDescriptor(31617, ResourceType.SCRIPT);
            ResourceDescriptor triggerParentScript = new ResourceDescriptor(17789,
                ResourceType.SCRIPT);
            ResourceDescriptor tweakParentScript = new ResourceDescriptor(39027,
                ResourceType.SCRIPT);
            ResourceDescriptor cameraZoneScript = new ResourceDescriptor(26580,
                ResourceType.SCRIPT);
            ResourceDescriptor tweakKeyScript = new ResourceDescriptor(52759, ResourceType.SCRIPT);
            ResourceDescriptor triggerCollectKeyScript = new ResourceDescriptor(17022,
                ResourceType.SCRIPT);
            ResourceDescriptor enemyWardScript = new ResourceDescriptor(43463, ResourceType.SCRIPT);
            ResourceDescriptor soundObjectScript = new ResourceDescriptor(31319, ResourceType.SCRIPT);
            ResourceDescriptor emitterScript = new ResourceDescriptor(27150, ResourceType.SCRIPT);
            ResourceDescriptor spriteLightScript = new ResourceDescriptor(46946, ResourceType.SCRIPT);

            GUID scoreboardScriptKey = new GUID(11599);
            GUID gunScriptKey = new GUID(66090);
            GUID speechBubbleScriptKey = new GUID(18420);
            GUID triggerMusicScriptKey = new GUID(18256);

            GUID emitterMeshKey = new GUID(18299);
            GUID paintSwitchMeshKey = new GUID(66172);

            for (Thing thing : world.things)
            {
                if (thing == null) continue;

                // Remap any plans and gmats back to their original GUIDs in LBP1
                {
                    if (thing.planGUID != null)
                        thing.planGUID =
                            RGuidSubst.LBP2_TO_LBP1_PLANS.getOrDefault(thing.planGUID,
                                thing.planGUID);

                    if (thing.hasPart(Part.EMITTER))
                    {
                        PEmitter emitter = thing.getPart(Part.EMITTER);
                        if (emitter.plan != null && emitter.plan.isGUID())
                        {
                            GUID guid = emitter.plan.getGUID();
                            guid = RGuidSubst.LBP2_TO_LBP1_PLANS.getOrDefault(guid, guid);
                            emitter.plan = new ResourceDescriptor(guid, ResourceType.PLAN);
                        }
                    }

                    if (thing.hasPart(Part.GROUP))
                    {
                        PGroup group = thing.getPart(Part.GROUP);
                        if (group.planDescriptor != null && group.planDescriptor.isGUID())
                        {
                            GUID guid = group.planDescriptor.getGUID();
                            guid = RGuidSubst.LBP2_TO_LBP1_PLANS.getOrDefault(guid, guid);
                            group.planDescriptor = new ResourceDescriptor(guid, ResourceType.PLAN);
                        }
                    }

                    // A bunch of gmats got remapped in LBP2
                    if (thing.hasPart(Part.GENERATED_MESH))
                    {
                        PGeneratedMesh mesh = thing.getPart(Part.GENERATED_MESH);
                        if (mesh.gfxMaterial != null && mesh.gfxMaterial.isGUID())
                        {
                            GUID guid = mesh.gfxMaterial.getGUID();
                            guid = RGuidSubst.LBP2_TO_LBP1_GMATS.getOrDefault(guid, guid);
                            mesh.gfxMaterial = new ResourceDescriptor(guid, ResourceType.GFX_MATERIAL);
                        }
                    }
                }

                if (thing.hasPart(Part.SCRIPT))
                {
                    PScript script = thing.getPart(Part.SCRIPT);

                    // Remove the DialogueListGibberishFile from any script (magic mouths)
                    // if they exist, since this will always trigger a dependencies error in LBP1
                    if (script.is(speechBubbleScriptKey))
                    {
                        script.instance.unsetField("DialogueListGibberishFile");
                        script.instance.unsetField("DialogueListFile");
                    }

                    // Deploy has custom gun, but LBP1 doesn't, will have to remove
                    // these fields conditionally, don't know the exact revision, so we'll go
                    // with 0x272, LBP1 retails final revision
                    if (version <= 0x272 && script.is(gunScriptKey))
                    {
                        script.instance.unsetField("Plan");
                        script.instance.unsetField("PlanIcon");
                    }

                    // LBP3 has a non-divergent BasicIcons resource attached to the script
                    // for some reason.
                    if (script.is(switchBaseScript))
                        script.instance.unsetField("BasicIcons");

                    // LBP music boxes should have all z layers enabled in their trigger range
                    if (script.is(triggerMusicScriptKey) && thing.hasPart(Part.TRIGGER))
                        thing.<PTrigger>getPart(Part.TRIGGER).allZLayers = true;
                }

                // Some switches/logic/etc get their meshes removed if they aren't visible in play mode.
                if (!thing.hasPart(Part.RENDER_MESH))
                {
                    GUID meshKey = null;

                    if (thing.hasPart(Part.EMITTER)) meshKey = emitterMeshKey;
                    if (thing.hasPart(Part.SWITCH))
                    {
                        PSwitch switchBase = thing.getPart(Part.SWITCH);
                        if (switchBase.type == SwitchType.PAINT)
                            meshKey = paintSwitchMeshKey;
                    }

                    // Normally we'd have to account for bones from the mesh file itself,
                    // but it should mostly be fine since they only have a single root bone.
                    if (meshKey != null)
                    {
                        PRenderMesh mesh = new PRenderMesh(new ResourceDescriptor(meshKey, ResourceType.MESH), new Thing[] { thing });
                        mesh.visibilityFlags &= ~VisibilityFlags.PLAY_MODE;
                        thing.setPart(Part.RENDER_MESH, mesh);
                    }
                }

                // Fixup creature brains
                if (thing.hasPart(Part.ENEMY))
                {
                    PEnemy enemy = thing.getPart(Part.ENEMY);
                    if (enemy.partType == EnemyPart.BRAIN)
                    {
                        if (!thing.hasPart(Part.SCRIPT))
                            thing.setPart(Part.SCRIPT, new PScript(enemyScript));

                        // Attach the prize bubble script to the life sources attached to this enemy
                        PCreature creature = thing.getPart(Part.CREATURE);
                        if (creature != null && creature.lifeSourceList != null)
                        {
                            for (Thing lifeSource : creature.lifeSourceList)
                            {
                                if (lifeSource == null) continue;

                                if (!lifeSource.hasPart(Part.SCRIPT))
                                {
                                    PScript script = new PScript(triggerCollectScript);
                                    script.instance.addField("CreatureThing", thing);
                                    lifeSource.setPart(Part.SCRIPT, script);
                                }
                            }
                        }
                    }
                }

                // Only set the script instances if they don't already exist
                if (!thing.hasPart(Part.SCRIPT))
                {
                    // Old keys have a child object with a trigger, handle that later
                    if (thing.isNewKey())
                        thing.setPart(Part.SCRIPT, new PScript(triggerCollectKeyScript));
                    
                    // Generally sprite lights are children of a tweakable mesh object
                    if (thing.hasPart(Part.SPRITE_LIGHT))
                    {
                        PSpriteLight light = thing.getPart(Part.SPRITE_LIGHT);

                        // It gets attached to both the child and parent?
                        thing.setPart(Part.SCRIPT, new PScript(spriteLightScript));

                        // Attach the parent script to the root of the group for more complex setups
                        // This probably won't mess anything up, right?
                        Thing root = thing;
                        if (thing.parent != null)
                        {
                            root = thing.parent;
                            Thing group = thing.groupHead;
                            if (group != null)
                            {
                                while (true)
                                {
                                    if (root.parent == null || root.parent.groupHead != group) break;
                                    root = root.parent;
                                }
                            }

                            if (!root.hasPart(Part.SCRIPT))
                                root.setPart(Part.SCRIPT, new PScript(spriteLightScript));
                        }

                        // Fixup the light activation
                        SwitchOutput output = world.getSwitchInput(root);
                        if (output != null)
                            light.onDest = output.activation.activation;
                    }
                    
                    else if (thing.hasPart(Part.EMITTER))
                        thing.setPart(Part.SCRIPT, new PScript(emitterScript));
                    
                    // Sound names got moved to a native field in later versions
                    else if (thing.hasPart(Part.AUDIO_WORLD))
                    {
                        // Sounds in LBP1 trigger at all layers
                        if (thing.hasPart(Part.TRIGGER))
                            thing.<PTrigger>getPart(Part.TRIGGER).allZLayers = true;

                        PAudioWorld sfx = thing.getPart(Part.AUDIO_WORLD);
                        sfx.triggerBySwitch = world.hasSwitchInput(thing);
                        PScript script = new PScript(soundObjectScript);
                        if (sfx.soundNames != null)
                            script.instance.addField("SoundNames", sfx.soundNames);
                        thing.setPart(Part.SCRIPT, script);
                    }

                    else if (thing.isEnemyWard())
                        thing.setPart(Part.SCRIPT, new PScript(enemyWardScript));

                    // Prize bubbles and score bubbles used a trigger collect script in LBP1
                    else if (thing.hasPart(Part.GAMEPLAY_DATA))
                    {
                        if (thing.isPrizeBubble())
                            thing.setPart(Part.SCRIPT, new PScript(tweakEggScript));
                        else if (thing.isScoreBubble())
                            thing.setPart(Part.SCRIPT, new PScript(triggerCollectScript));
                    }

                    // Checkpoint tweakable scripts
                    else if (thing.hasPart(Part.CHECKPOINT))
                        thing.setPart(Part.SCRIPT, new PScript(checkpointScript));

                        // Joint tweakable scripts
                    else if (thing.hasPart(Part.JOINT))
                        thing.setPart(Part.SCRIPT, new PScript(tweakJointScript));

                    else if (thing.hasPart(Part.SWITCH_KEY))
                        thing.setPart(Part.SCRIPT, new PScript(tweakKeyScript));

                        // Switch base script get removed in later versions, I assume because it
                        // just gets automatically added internally?
                    else if (thing.hasPart(Part.SWITCH))
                        thing.setPart(Part.SCRIPT, new PScript(switchBaseScript));

                        // Both camera types
                    else if (thing.hasPart(Part.CAMERA_TWEAK))
                        thing.setPart(Part.SCRIPT, new PScript(cameraZoneScript));

                        // Fixup scoreboard, no join posts, and anything with triggers
                    else if (thing.parent != null)
                    {
                        // Handle old level keys
                        if (thing.parent.isOldKey() && thing.hasPart(Part.TRIGGER))
                        {
                            thing.setPart(Part.SCRIPT, new PScript(triggerCollectKeyScript));
                            continue;
                        }

                        PScript script = thing.parent.getPart(Part.SCRIPT);
                        if (script != null)
                        {
                            if (script.is(scoreboardScriptKey))
                            {
                                if (thing.hasPart(Part.TRIGGER))
                                    thing.setPart(Part.SCRIPT, new PScript(triggerParentScript));
                                else if (thing.hasPart(Part.CHECKPOINT))
                                    thing.setPart(Part.SCRIPT, new PScript(checkpointScript));
                                else
                                    thing.setPart(Part.SCRIPT, new PScript(tweakParentScript));

                            }
                            else if (thing.hasPart(Part.TRIGGER))
                                thing.setPart(Part.SCRIPT, new PScript(triggerParentScript));
                        }
                    }
                }
            }
        }

        // Attach tweak joint scripts to components if we're serialized to LBP1

        // If we're writing a file that was originally from after LBP1, we need to
        // put the backdropPlan as a PRef component
        if (version < 0x321 && world.backdropPlan != null && world.backdrop != null)
        {
            PRef ref;
            if (!world.backdrop.hasPart(Part.REF))
            {
                ref = new PRef();

                world.backdrop.setPart(Part.REF, ref);
            }
            else ref = world.backdrop.getPart(Part.REF);

            ref.childrenSelectable = false;
            ref.stripChildren = true;
            ref.plan = world.backdropPlan;
        }
        else if (version >= 0x321 && world.backdropPlan == null && world.backdrop != null)
        {
            PRef ref = world.backdrop.getPart(Part.REF);
            if (ref != null)
            {
                world.backdropPlan = ref.plan;
                world.backdrop.setPart(Part.REF, null);
            }
        }
    }

    private ArrayList<Thing> getAllReferences(ArrayList<Thing> things, Thing thing)
    {
        PWorld world = (this.worldThing.getPart(Part.WORLD));
        if (!things.contains(thing)) things.add(thing);
        for (Thing worldThing : world.things)
        {
            if (worldThing == null || worldThing == thing) continue;
            if (worldThing.getPart(Part.WORLD) != null || things.contains(worldThing))
                continue;
            if (worldThing.parent == thing || (thing.groupHead != null && (worldThing.groupHead == thing.groupHead)))
                this.getAllReferences(things, worldThing);
        }
        return things;
    }

    public HashMap<String, RPlan> getPalettes(String name, Revision revision,
                                              byte compressionFlags, boolean includeChildren)
    {
        HashMap<String, RPlan> plans = new HashMap<>();
        PWorld world = this.worldThing.getPart(Part.WORLD);
        Thing.SERIALIZE_WORLD_THING = false;
        for (Thing thing : world.things)
        {
            if (thing == null || thing.getPart(Part.WORLD) != null) continue;
            PMetadata metadata = thing.getPart(Part.METADATA);
            if (metadata == null) continue;
            if (includeChildren)
            {
                Thing[] things =
                    this.getAllReferences(new ArrayList<>(), thing).toArray(Thing[]::new);
                plans.put(name + "_" + thing.UID + ".plan", new RPlan(revision,
                    compressionFlags,
                    things, metadata));
            }
            else
                plans.put(name + "_" + thing.UID + ".plan", new RPlan(revision,
                    compressionFlags,
                    thing, metadata));
        }
        Thing.SERIALIZE_WORLD_THING = true;
        return plans;
    }

    public int getNextUID()
    {
        return ++((PWorld) this.worldThing.getPart(Part.WORLD)).thingUIDCounter;
    }

    public void addPlan(RPlan plan)
    {
        Thing[] things = plan.getThings();
        PWorld world = this.worldThing.getPart(Part.WORLD);
        for (Thing thing : things)
        {
            if (thing != null)
            {
                thing.UID = this.getNextUID();
                synchronized (world.things)
                {
                    world.things.add(thing);
                }
            }
        }
    }

    public byte[] toPlan()
    {
        RPlan plan = new RPlan();
        plan.revision = new Revision(0x272, 0x4c44, 0x0017);
        plan.compressionFlags = 0x7;
        ArrayList<Thing> things = new ArrayList<>();
        PWorld world = this.worldThing.getPart(Part.WORLD);
        for (Thing thing : world.things)
        {
            if (thing == this.worldThing) continue;
            if (thing == world.backdrop) continue;

            things.add(thing);
        }

        plan.setThings(things.toArray(Thing[]::new));

        plan.inventoryData = new InventoryItemDetails();
        plan.inventoryData.type = EnumSet.of(InventoryObjectType.READYMADE);
        plan.inventoryData.icon = new ResourceDescriptor(2551, ResourceType.TEXTURE);
        plan.inventoryData.userCreatedDetails = new UserCreatedDetails("World Export",
            "Exported " +
            "world");


        return SerializedResource.compress(plan.build());
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        return size;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
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
