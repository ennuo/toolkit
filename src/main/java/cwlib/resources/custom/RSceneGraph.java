package cwlib.resources.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.joml.Matrix4f;

import cwlib.enums.Branch;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RLevel;
import cwlib.resources.RPlan;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.*;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import editor.gl.Camera;
import editor.gl.MeshInstance;
import editor.gl.MeshPrimitive;
import editor.gl.objects.Mesh;
import editor.gl.objects.Shader;
import editor.gl.objects.Texture;

public class RSceneGraph implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    private String name;
    private Camera camera = new Camera();
    private PWorld world = new PWorld();
    private PLevelSettings lighting = new PLevelSettings();
    private List<Thing> things = Collections.synchronizedList(new ArrayList<>());
    private int nextUID = 1;
    private transient Thing[] backdrop;
    private ResourceDescriptor background;
    private HashMap<ResourceDescriptor, byte[]> packedData = new HashMap<>();

    private transient List<Thing> queue = Collections.synchronizedList(new ArrayList<>());

    public RSceneGraph() {}
    public RSceneGraph(RLevel level) {
        this.world = level.world.getPart(Part.WORLD);
        while (this.world.things.remove(null));
        this.world.things.sort((a, z) -> a.UID - z.UID);
        
        for (Thing thing : this.world.things) {
            if (thing == null) continue;

            if (thing.hasPart(Part.LEVEL_SETTINGS)) {
                this.lighting = thing.getPart(Part.LEVEL_SETTINGS);
                if (thing == this.world.backdrop && thing.hasPart(Part.REF))
                    this.background = ((PRef)thing.getPart(Part.REF)).plan;
                continue;
            }

            if (thing.hasPart(Part.WORLD)) continue;
            thing.UID = this.nextUID++;
            this.things.add(thing);
        }
        
        if (this.background == null) 
            this.background = world.backdropPlan;
    }

    @SuppressWarnings("unchecked")
    @Override public RSceneGraph serialize(Serializer serializer, Serializable structure) {
        RSceneGraph graph = (structure == null) ? new RSceneGraph() : (RSceneGraph) structure;

        graph.name = serializer.wstr(graph.name);
        graph.camera = serializer.struct(graph.camera, Camera.class);
        graph.world = serializer.struct(graph.world, PWorld.class);

        if (serializer.isWriting()) {
            synchronized(graph.things) {
                serializer.i32(graph.things.size());
                for (Thing thing : things)
                    serializer.thing(thing);
            }
        } else
            graph.things = Collections.synchronizedList(serializer.arraylist(null, Thing.class, true));

        graph.nextUID = serializer.i32(graph.nextUID);
        graph.background = serializer.resource(graph.background, ResourceType.PLAN);

        if (serializer.getRevision().has(Branch.MIZUKI, Revisions.MZ_SCE_DEFAULT_LIGHTING))
            graph.lighting = serializer.struct(graph.lighting, PLevelSettings.class);

        if (serializer.getRevision().has(Branch.MIZUKI, Revisions.MZ_SCE_PACKED_DATA)) {
            if (serializer.isWriting()) {
                Set<ResourceDescriptor> keys = graph.packedData.keySet();
                serializer.getOutput().i32(keys.size());
                for (ResourceDescriptor key : keys) {
                    serializer.resource(key, ResourceType.FILE_OF_BYTES);
                    serializer.bytearray(graph.packedData.get(key));
                }
            } else {
                int count = serializer.getInput().i32();
                graph.packedData = new HashMap<>(count);
                for (int i = 0; i < count; ++i) {
                    graph.packedData.put(
                        serializer.resource(null, ResourceType.FILE_OF_BYTES), 
                        serializer.bytearray(null)
                    );
                }
            }
        }
        
        return graph;
    }

    public Thing addThing() { 
        Thing thing = new Thing(this.nextUID++);
        synchronized(this.queue) {
            this.queue.add(thing);
        }
        return thing;
    }
    
    public Thing addMesh(ResourceDescriptor mesh) {
        Thing thing = new Thing(this.nextUID++);
        thing.setPart(Part.POS, new PPos(thing, 0, new Matrix4f().identity()));
        thing.setPart(Part.BODY, new PBody());
        thing.setPart(Part.GROUP, new PGroup());
        thing.setPart(Part.RENDER_MESH, new PRenderMesh(mesh));
        synchronized(this.queue) {
            this.queue.add(thing);
        }
        return thing;
    }

    @SuppressWarnings("unchecked")
    public void packAssets() {
        Set<ResourceDescriptor>[] resources = new Set[] {
            Shader.PROGRAMS.keySet(),
            Texture.TEXTURES.keySet(),
            Mesh.MESHES.keySet(),
            PRenderMesh.ANIMATIONS.keySet()
        };

        for (Set<ResourceDescriptor> collection : resources) {
            for (ResourceDescriptor descriptor : collection) {
                if (this.packedData.containsKey(descriptor)) continue;
                byte[] fileData = ResourceSystem.extract(descriptor);
                if (fileData != null)
                    this.packedData.put(descriptor, fileData);
            }
        }

        // Make sure to include background plan
        if (this.background != null && !this.packedData.containsKey(this.background)) {
            byte[] fileData = ResourceSystem.extract(this.background);
            if (fileData != null)
                this.packedData.put(this.background, fileData);
        }
    }

    public byte[] getResourceData(ResourceDescriptor descriptor) {
        if (descriptor == null) return null;
        if (this.packedData.containsKey(descriptor))
            return this.packedData.get(descriptor);
        return ResourceSystem.extract(descriptor);
    }

    private void loadBackdrop() {
        if (this.backdrop != null || this.background == null) return;
        byte[] planData = this.getResourceData(this.background);
        if (planData == null) return;
        this.backdrop = new Resource(planData).loadResource(RPlan.class).getThings();
        for (Thing thing : this.backdrop) {
            if (thing == null) continue;
            if (thing.hasPart(Part.LEVEL_SETTINGS)) {
                this.lighting = thing.getPart(Part.LEVEL_SETTINGS);
                break;
            }
        }
    }

    public void update() {
        this.loadBackdrop();
        if (this.backdrop != null) {
            for (Thing thing : this.backdrop)
                thing.render();
        }

        synchronized(this.things) {
            int index = 0;
            Iterator<Thing> i = this.things.iterator();
            while (i.hasNext()) {
                Thing next = i.next();
                next.render();
                index++;
            }
        }

        synchronized(this.queue) {
            Thing[] queue = this.queue.toArray(Thing[]::new);
            this.queue.clear();
            for (Thing thing : queue)
                this.things.add(thing);
        }
    }

    public HashSet<ResourceDescriptor> getResourcesInUse() {
        HashSet<ResourceDescriptor> resources = new HashSet<>();
        synchronized(this.things) {
            Iterator<Thing> i = this.things.iterator();
            while (i.hasNext()) {
                Thing thing = i.next();
                PRenderMesh mesh = thing.getPart(Part.RENDER_MESH);
                PGeneratedMesh proc = thing.getPart(Part.GENERATED_MESH);
                PLevelSettings settings = thing.getPart(Part.LEVEL_SETTINGS);

                MeshInstance instance = null;

                if (mesh != null) {
                    resources.add(mesh.mesh);
                    resources.add(mesh.anim);
                    instance = mesh.instance;
                }

                if (proc != null) {
                    resources.add(proc.gfxMaterial);
                    resources.add(proc.bevel);
                    instance = proc.instance;
                }

                if (settings != null) {
                    resources.add(settings.backdropMesh);
                    instance = settings.backdropInstance;
                }

                if (instance != null) {
                    resources.add(instance.texture);
                    for (MeshPrimitive primitive : instance.mesh.getPrimitives()) {
                        resources.add(primitive.getMaterial());
                        Shader shader = Shader.get(primitive.getMaterial());
                        for (ResourceDescriptor descriptor : shader.textures) {
                            resources.add(descriptor);
                        }
                    }
                }
            }
        }
        resources.remove(null);
        return resources;
    }

    public String getName() { return this.name; }
    public Camera getCamera() { return this.camera; }
    public PLevelSettings getLighting() { return this.lighting; }

    public void setName(String name) { this.name = name; }

    public byte[] toLevelData(Revision revision, byte compressionFlags) {
        RLevel level = new RLevel();
        level.world.setPart(Part.WORLD, this.world);
        this.world.thingUIDCounter = this.nextUID;
        this.world.backdropPlan = this.background;

        Thing backdrop = new Thing(this.world.thingUIDCounter++);
        backdrop.setPart(Part.LEVEL_SETTINGS, this.lighting);
        backdrop.setPart(Part.REF, new PRef(this.background));
        this.world.backdrop = backdrop;
        

        ArrayList<Thing> things = new ArrayList<>(this.things);
        things.add(0, backdrop);
        things.add(0, level.world);

        world.things = things;

        return Resource.compress(level.build(revision, compressionFlags));
    }

    public RPlan getPlan() {
        RPlan plan = new RPlan();
        plan.setThings(this.things.toArray(Thing[]::new));
        return plan;
    }

    @Override public int getAllocatedSize() {
        int size = RSceneGraph.BASE_ALLOCATION_SIZE;
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        // 16MB buffer for generation of levels, since the allocated size will get
        // stuck in a recursive loop until I fix it.
        Serializer serializer = new Serializer(0x1000000, revision, compressionFlags);
        serializer.struct(this, RSceneGraph.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags,
            ResourceType.SCENE_GRAPH,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }

    @Override public String toString() {
        if (this.name != null) return this.name;
        return "Unnamed SceneGraph";
    }
}
