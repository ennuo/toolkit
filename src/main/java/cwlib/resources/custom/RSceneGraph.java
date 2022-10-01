package cwlib.resources.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;

import cwlib.enums.Part;
import cwlib.enums.ResourceType;
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
import toolkit.gl.Camera;

public class RSceneGraph implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    private String name;
    private Camera camera = new Camera();
    private PWorld world = new PWorld();
    private transient PLevelSettings lighting = new PLevelSettings();
    private List<Thing> things = Collections.synchronizedList(new ArrayList<>());
    private int nextUID = 1;
    private transient Thing[] backdrop;
    private ResourceDescriptor background;

    public RSceneGraph() {}
    public RSceneGraph(RLevel level) {
        this.world = level.world.getPart(Part.WORLD);
        for (Thing thing : this.world.things) {
            if (thing == null) continue;
            if (thing.hasPart(Part.LEVEL_SETTINGS)) {
                this.lighting = thing.getPart(Part.LEVEL_SETTINGS);
                continue;
            }
            if (thing.hasPart(Part.WORLD)) continue;
            if (thing == this.world.backdrop && thing.hasPart(Part.REF)) {
                this.background = ((PRef)thing.getPart(Part.REF)).plan;
                continue;
            }
            thing.UID = this.nextUID++;
            this.things.add(thing);
        }
        
        if (this.backdrop == null) 
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

        return graph;
    }

    public Thing addThing() { return new Thing(this.nextUID++); }
    public Thing addMesh(ResourceDescriptor mesh) {
        Thing thing = new Thing(this.nextUID++);
        thing.setPart(Part.POS, new PPos(thing, 0, new Matrix4f().identity()));
        thing.setPart(Part.BODY, new PBody());
        thing.setPart(Part.GROUP, new PGroup());
        thing.setPart(Part.RENDER_MESH, new PRenderMesh(mesh));
        synchronized(this.things) {
            this.things.add(thing);
        }
        return thing;
    }

    private void loadBackdrop() {
        if (this.backdrop != null || this.background == null) return;
        byte[] planData = ResourceSystem.extract(this.background);
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
                thing.render(this.lighting);
        }
        synchronized(this.things) {
            Iterator<Thing> i = this.things.iterator();
            while (i.hasNext())
                (i.next()).render(this.lighting);
        }
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
