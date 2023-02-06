package cwlib.structs.things;

import java.util.ArrayList;
import java.util.Arrays;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.Branch;
import cwlib.enums.Part;
import cwlib.enums.PartHistory;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.ThingSerializer;
import cwlib.io.serializer.Serializer;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.things.components.CostumePiece;
import cwlib.structs.things.parts.PCostume;
import cwlib.structs.things.parts.PGeneratedMesh;
import cwlib.structs.things.parts.PInstrument;
import cwlib.structs.things.parts.PJoint;
import cwlib.structs.things.parts.PLevelSettings;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.structs.things.parts.PShape;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.Bytes;
import cwlib.util.Colors;
import editor.gl.MeshInstance;
import editor.gl.objects.Mesh;

/**
 * Represents an object in the game world.
 */
@JsonAdapter(ThingSerializer.class)
public class Thing implements Serializable {
    public static boolean SERIALIZE_WORLD_THING = true;

    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public String name;
    
    public int UID = 1;
    public Thing world;
    public Thing parent;
    public Thing groupHead;
    public Thing oldEmitter;

    public short createdBy = -1, changedBy = -1;
    public boolean isStamping;
    public GUID planGUID;
    public boolean hidden;
    public short flags;
    public byte extraFlags;

    private Serializable[] parts = new Serializable[0x3f];

    public Thing(){};
    public Thing(int UID) { 
        this.UID = UID; 
    }

    @SuppressWarnings("unchecked")
    @Override public Thing serialize(Serializer serializer, Serializable structure) {
        Thing thing = (structure == null) ? new Thing() : (Thing) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        int maxPartsRevision = PartHistory.STREAMING_HINT;
        if (version <= 0x3e2)
            maxPartsRevision = PartHistory.CONTROLINATOR;
        if (version <= 0x33a)
            maxPartsRevision = PartHistory.MATERIAL_OVERRIDE;
        if (version <= 0x2c3)
            maxPartsRevision = PartHistory.MATERIAL_TWEAK;
        if (version <= 0x272)
            maxPartsRevision = PartHistory.GROUP;

        // Test serialization marker.
        if (revision.has(Branch.MIZUKI, Revisions.MZ_SCENE_GRAPH)) thing.name = serializer.wstr(thing.name);
        else if (version >= Revisions.THING_TEST_MARKER || revision.has(Branch.LEERDAMMER, Revisions.LD_TEST_MARKER)) {
            serializer.log("TEST_SERIALISATION_MARKER");
            if (serializer.u8(0xAA) != 0xaa)
                throw new SerializationException("Test serialization marker is invalid, something has gone terribly wrong!");
        }

        if (version < 0x1fd) {
            if (serializer.isWriting())
                serializer.reference(SERIALIZE_WORLD_THING ? thing.world : null, Thing.class);
            else
                thing.world = serializer.reference(thing.world, Thing.class);
        }
        if (version < 0x27f) {
            thing.parent = serializer.reference(thing.parent, Thing.class);
            thing.UID = serializer.i32(thing.UID);
        } else {
            thing.UID = serializer.i32(thing.UID);
            thing.parent = serializer.reference(thing.parent, Thing.class);
        }

        thing.groupHead = serializer.reference(thing.groupHead, Thing.class);

        if (version >= 0x1c7)
            thing.oldEmitter = serializer.reference(thing.oldEmitter, Thing.class);

        if (version >= 0x1a6 && version < 0x1bc)
            serializer.array(null, PJoint.class, true);
        
        if ((version >= 0x214 && !revision.isToolkit()) || revision.before(Branch.MIZUKI, Revisions.MZ_SCENE_GRAPH)) {
            thing.createdBy = serializer.i16(thing.createdBy);
            thing.changedBy = serializer.i16(thing.changedBy);
        }

        if (version < 0x341) {
            if (version > 0x21a) 
                thing.isStamping = serializer.bool(thing.isStamping);
            if (version >= 0x254)
                thing.planGUID = serializer.guid(thing.planGUID);
            if (version >= 0x2f2)
                thing.hidden = serializer.bool(thing.hidden);
        } else {
            if (version >= 0x254)
                thing.planGUID = serializer.guid(thing.planGUID);

            if (version >= 0x341) {
                if (revision.has(Branch.DOUBLE11, 0x62))
                    thing.flags = serializer.i16(thing.flags);
                else
                    thing.flags = serializer.i8((byte) thing.flags);
            }
            if (subVersion >= 0x110)
                thing.extraFlags = serializer.i8(thing.extraFlags);
        }

        boolean isCompressed = (version >= 0x297 || revision.has(Branch.LEERDAMMER, Revisions.LD_RESOURCES));
        
        int partsRevision = PartHistory.STREAMING_HINT;
        long flags = -1;

        if (serializer.isWriting()) {
            serializer.log("GENERATING FLAGS");
            Part lastPart = null;
            if (isCompressed) flags = 0;
            for (Part part : Part.values()) {
                int index = part.getIndex();
                if (version >= 0x13c && (index >= 0x36 && index <= 0x3c)) continue;
                if (version >= 0x18c && index == 0x3d) continue;
                if (subVersion >= 0x107 && index == 0x3e) continue;
                else if (index == 0x3e) {
                    if (thing.parts[index] != null) {
                        flags |= (1l << 0x29);
                        lastPart = part;
                    }
                    continue;
                }

                if (thing.parts[index] != null) {
                    // Offset due to PCreatorAnim
                    if (subVersion < 0x107 && index > 0x28) index++; 

                    flags |= (1l << index);

                    lastPart = part;
                }
            }
            partsRevision = (lastPart == null) ? 0 : lastPart.getVersion();
        }

        if (serializer.isWriting()) {
            if (partsRevision > maxPartsRevision)
                partsRevision = maxPartsRevision;
        }
        
        partsRevision = serializer.s32(partsRevision);
        if (isCompressed) {
            // serializer.log("FLAGS");
            flags = serializer.i64(flags);
        }

        // I have no idea why they did this
        if (version == 0x13c) partsRevision += 7;
        
        Part[] parts = Part.fromFlags(revision.getHead(), flags, partsRevision);
        if (!ResourceSystem.DISABLE_LOGS)
            serializer.log(Arrays.toString(parts));

        for (Part part : parts) {
            serializer.log(part.name() + " [START]");
            if (!part.serialize(thing.parts, partsRevision, flags, serializer)) {
                serializer.log(part.name() + " FAILED");
                throw new SerializationException(part.name() + " failed to serialize!");
            }
            serializer.log(part.name() + " [END]");
        }

        // if (subVersion >= 0x83 && subVersion < 0x8b)
            // serializer.u8(0);
        
        serializer.log("THING " + Bytes.toHex(thing.UID) + " [END]");
        
        return thing;
    }

    public void render() {
        PPos pos = this.getPart(Part.POS);
        if (pos == null) return;

        PRenderMesh mesh = this.getPart(Part.RENDER_MESH);
        PCostume costume = this.getPart(Part.COSTUME);

        int[] regionIDsToHide = null;
        if (costume != null) regionIDsToHide = costume.meshPartsHidden;
        if (mesh != null) mesh.update(this, pos.getWorldPosition(), regionIDsToHide);

        PShape shape = this.getPart(Part.SHAPE);
        PGeneratedMesh generatedMesh = this.getPart(Part.GENERATED_MESH);
        if (shape != null && generatedMesh != null) {
            if (generatedMesh.instance == null)
                generatedMesh.instance = new MeshInstance(Mesh.getProceduralMesh(generatedMesh, shape));
            generatedMesh.instance.draw(new Matrix4f[] { pos.getWorldPosition() }, Colors.RGBA32.fromARGB(shape.color));
        }

        PLevelSettings settings = this.getPart(Part.LEVEL_SETTINGS);
        if (settings != null && settings.backdropMesh != null) {
            if (settings.backdropInstance == null) {
                Mesh glMesh = Mesh.getStaticMesh(settings.backdropMesh);
                if (glMesh != null)
                    settings.backdropInstance = new MeshInstance(glMesh);
            }
            else settings.backdropInstance.draw(new Matrix4f[] { new Matrix4f().identity().invert() }, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
        }

        PInstrument instrument = this.getPart(Part.INSTRUMENT);
        if (instrument != null && mesh != null && mesh.instance != null)
            mesh.instance.texture = instrument.icon;


        ResourceDescriptor base = new ResourceDescriptor(9698, ResourceType.GFX_MATERIAL);
        if (costume != null && mesh != null && mesh.instance != null) {
            mesh.instance.override(base, costume.material);
            for (CostumePiece piece : costume.costumePieces) {
                if (piece == null || piece.mesh == null) continue;
                if (piece.mesh.isGUID()) {
                    long guid = piece.mesh.getGUID().getValue();
                    if (guid == 9876 || guid == 9877) continue;
                }
                if (piece.instance == null) {
                    Mesh glMesh = Mesh.getSkinnedMesh(piece.mesh);
                    if (glMesh != null)
                        piece.instance = new MeshInstance(glMesh);
                } else {
                    piece.instance.override(base, costume.material);
                    piece.instance.draw(mesh.boneModels, Colors.RGBA32.fromARGB(mesh.editorColor));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> T getPart(Part part) { return (T) this.parts[part.getIndex()]; }
    public <T extends Serializable> void setPart(Part part, T value) { this.parts[part.getIndex()] = value; }
    public boolean hasPart(Part part) { return this.parts[part.getIndex()] != null; }

    @Override public int getAllocatedSize() {  return BASE_ALLOCATION_SIZE;  }

    @Override public String toString() {
        if (this.name != null) return this.name;
        return String.format("New Thing (%d)", this.UID);
    }
}
