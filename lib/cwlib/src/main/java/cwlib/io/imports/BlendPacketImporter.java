package cwlib.io.imports;

import static com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.imageio.ImageIO;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import cwlib.enums.BoxType;
import cwlib.enums.CompressionFlags;
import cwlib.enums.GameShader;
import cwlib.enums.InventoryObjectType;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.exports.MeshExporter;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.resources.RGfxMaterial;
import cwlib.resources.RMesh;
import cwlib.resources.RPlan;
import cwlib.structs.gmat.MaterialBox;
import cwlib.structs.gmat.MaterialWire;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.inventory.UserCreatedDetails;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.mesh.SoftbodySpring;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PBody;
import cwlib.structs.things.parts.PGeneratedMesh;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PShape;
import cwlib.types.SerializedResource;
import cwlib.types.archives.FileArchive;
import cwlib.types.archives.SaveArchive;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.util.Bytes;
import cwlib.util.Crypto;
import cwlib.util.FileIO;
import cwlib.util.Images;
import cwlib.util.gfx.CgAssembler;
import cwlib.util.gfx.GfxAssembler;
import gr.zdimensions.jsquish.Squish.CompressionType;

public class BlendPacketImporter 
{
    public static class BlendBone
    {
        public String Name;
        public int Parent;
        public int FirstChild;
        public int NextSibling;
        public Matrix4f SkinPoseMatrix;
    }

    public static class BlendPrimitive
    {
        public String Name;
        public int MaterialIndex;
        public int[] Triangles;
    }

    public static class BlendMaterial
    {
        public static int GLASSY = 1;

        public String Path = "";
        public String Name = "";
        public int Flags;
        public byte[] BinaryData = {};
        public ResourceDescriptor Descriptor;
    }

    public static class BlendTexture
    {
        public static final int SRGB = 1;

        public int Key;
        public String Name = "";
        public int Width;
        public int Height;
        public int Flags;
        public int[] PixelData = {};
        public ResourceDescriptor Descriptor;
    }

    public static class BlendPacket
    {
        public int Version;
        public BlendTexture[] Textures = {};
        public BlendMaterial[] Materials = {};
        public BlendBone[] Bones = {};
        public BlendMesh[] Meshes = {};
        public String[] Clusters = {};

        public static BlendPacket FromFile(File file)
        {
            var packet = new BlendPacket();

            var stream = new MemoryInputStream(file.getAbsolutePath());
            if (stream.i32() != 0x424d5348 /* BMSH */)
                throw new SerializationException("Blend packet has invalid header!");
            
            stream.setLittleEndian(true);

            packet.Version = stream.i32();

            packet.Textures = new BlendTexture[stream.i32()];
            packet.Materials = new BlendMaterial[stream.i32()];
            packet.Meshes = new BlendMesh[stream.i32()];
            packet.Bones = new BlendBone[stream.i32()];
            packet.Clusters = new String[stream.i32()];

            for (int i = 0; i < packet.Textures.length; ++i)
            {
                var texture = new BlendTexture();
                texture.Key = stream.i32();
                texture.Name = stream.str(stream.i32());
                if (!stream.bool())
                {
                    texture.Width = stream.i32();
                    texture.Height = stream.i32();
                    texture.Flags = stream.i32();

                    texture.PixelData = new int[stream.i32() / 4];
                    for (int j = 0; j < texture.PixelData.length; ++j)
                    {
                        // ABGR -> ARGB
                        int pixel = stream.i32();
                        int r = (pixel >> 16) & 0xff;
                        int b = pixel & 0xff;
                        texture.PixelData[j] = (pixel & 0xff00ff00) | (b << 16) | r;
                    }
                }

                packet.Textures[i] = texture;
            }

            for (int i = 0; i < packet.Materials.length; ++i)
            {
                var material = new BlendMaterial();
                material.Path = stream.str(stream.i32());
                material.Name = stream.str(stream.i32());
                material.Flags = stream.i32();
                material.BinaryData = stream.bytes(stream.i32());

                packet.Materials[i] = material;
            }

            for (int i = 0; i < packet.Bones.length; ++i)
            {
                var bone = new BlendBone();
                bone.Name = stream.str(stream.i32());
                bone.Parent = stream.i32();
                bone.FirstChild = stream.i32();
                bone.NextSibling = stream.i32();
                bone.SkinPoseMatrix = stream.m44();

                packet.Bones[i] = bone;
            }

            for (int i = 0; i < packet.Clusters.length; ++i)
                packet.Clusters[i] = stream.str(stream.i32());

            for (int i = 0; i < packet.Meshes.length; ++i)
                packet.Meshes[i] = BlendMesh.FromFile(stream.bytes(stream.i32()), packet.Version);

            return packet;
        }
    }

    public static class BlendMesh
    {
        public static final int HAS_ARMATURE = (1 << 0);
        public static final int HAS_SHAPE_KEYS = (1 << 1);
        public static final int HAS_VERTEX_MASS = (1 << 2);
        public static final int HAS_SOFTBODY_CLUSTERS = (1 << 3);
        public static final int HAS_SOFTBODY_SPRINGS = (1 << 4);
        public static final int HAS_SOFTPHYSICS = HAS_SOFTBODY_CLUSTERS | HAS_SOFTBODY_SPRINGS;

        public String Path;
        public String Name;

        public int Flags;

        public int NumVertices;
        public int NumLoops;
        public int NumTriangles;
        public int NumUvLayers;
        public int NumEdges;
        public int NumPrimitives;
        public int NumShapeKeys;
        public int NumSoftbodySprings;

        public Vector3f[] Positions;
        public Vector2f[][] TextureCoordinates;
        public Vector3f[] Normals;
        public Vector3f[] Tangents;
        public Vector4f[] Weights;
        public Vector4i[] Joints;
        public int[] Loops;
        public BlendPrimitive[] Primitives;
        public int[] Edges;
        public int[] ClusterIndices;
    
        public float[] Mass;
        public ArrayList<SoftbodySpring> Springs = new ArrayList<>();
        public String SoftPhysSettings;

        public static BlendMesh FromFile(byte[] binaryData, int version)
        {
            var packet = new BlendMesh();

            var stream = new MemoryInputStream(binaryData);
            stream.setLittleEndian(true);

            packet.Path = stream.str(stream.i32());
            packet.Name = stream.str(stream.i32());

            packet.Flags = stream.i32();

            packet.NumVertices = stream.i32();
            packet.NumLoops = stream.i32();
            packet.NumTriangles = stream.i32();
            packet.NumUvLayers = stream.i32();
            packet.NumEdges = stream.i32();
            packet.NumPrimitives = stream.i32();
            if ((packet.Flags & HAS_SHAPE_KEYS) != 0)
                packet.NumShapeKeys = stream.i32();

            packet.Positions = new Vector3f[packet.NumVertices];
            packet.TextureCoordinates = new Vector2f[packet.NumUvLayers][];
            packet.Normals = new Vector3f[packet.NumLoops];
            packet.Tangents = new Vector3f[packet.NumLoops];
            packet.Loops = new int[packet.NumLoops];
            packet.Primitives = new BlendPrimitive[packet.NumPrimitives];
            packet.Edges = new int[packet.NumEdges * 2];
            for (int i = 0; i < packet.NumUvLayers; ++i)
                packet.TextureCoordinates[i] = new Vector2f[packet.NumLoops];

            for (int i = 0; i < packet.NumVertices; ++i) packet.Positions[i] = stream.v3();
            
            for (int i = 0; i < packet.NumUvLayers; ++i)
            for (int j = 0; j < packet.NumLoops; ++j)
                packet.TextureCoordinates[i][j] = stream.v2();

            for (int i = 0; i < packet.NumLoops; ++i) packet.Normals[i] = stream.v3();
            for (int i = 0; i < packet.NumLoops; ++i) packet.Tangents[i] = stream.v3();

            for (int i = 0; i < packet.NumLoops; ++i) packet.Loops[i] = stream.i32();

            for (int i = 0; i < packet.NumPrimitives; ++i)
            {
                var primitive = new BlendPrimitive();
                primitive.MaterialIndex = stream.i32();
                primitive.Triangles = new int[stream.i32()];
                for (int j = 0; j < primitive.Triangles.length; ++j)
                    primitive.Triangles[j] = stream.i32();
                packet.Primitives[i] = primitive;
            }

            if ((packet.Flags & HAS_ARMATURE) != 0)
            {
                packet.Joints = new Vector4i[packet.NumVertices];
                packet.Weights = new Vector4f[packet.NumVertices];

                for (int i = 0; i < packet.NumVertices; ++i)
                    packet.Joints[i] = new Vector4i(stream.i32(), stream.i32(), stream.i32(), stream.i32());
                for (int i = 0; i < packet.NumVertices; ++i)
                    packet.Weights[i] = stream.v4();
            }

            if ((packet.Flags & HAS_VERTEX_MASS) != 0)
            {
                packet.Mass = new float[packet.NumVertices];
                for (int i = 0; i < packet.NumVertices; ++i)
                {
                    packet.Mass[i] = stream.f32();
                    // System.out.println(packet.Mass[i]);
                    // if (packet.Mass[i] < 0.39215686274f)
                    //     packet.Mass[i] = 0.39215686274f;
                }
            }
            

            if ((packet.Flags & HAS_SOFTBODY_SPRINGS) != 0)
            {
                int numSprings = stream.i32();
                packet.Springs.ensureCapacity(numSprings);
                for (int i = 0; i < numSprings; ++i)
                {
                    int a = stream.i32();
                    int b = stream.i32();

                    var va = packet.Positions[a];
                    var vb = packet.Positions[b];
                    float restlengthsq = 
                        (float)(Math.pow(vb.x - va.x, 2.0) +
                        Math.pow(vb.y - va.y, 2.0) + 
                        Math.pow(vb.z - va.z, 2.0));
                    
                    var spring = new SoftbodySpring(a, b, restlengthsq);
                    packet.Springs.add(spring);
                }
            }

            if ((packet.Flags & HAS_SOFTBODY_CLUSTERS) != 0)
            {
                packet.ClusterIndices = new int[packet.NumVertices];
                for (int i = 0; i < packet.NumVertices; ++i)
                    packet.ClusterIndices[i] = stream.i32();
            }

            if ((packet.Flags & HAS_SOFTPHYSICS) != 0)
            {
                packet.SoftPhysSettings = stream.str(stream.i32());

                if ((packet.Flags & HAS_VERTEX_MASS) == 0)
                {
                    packet.Mass = new float[packet.NumVertices];
                    for (int i = 0; i < packet.NumVertices; ++i)
                        packet.Mass[i] = 1.0f;
                }
            }

            return packet;
        }



    }

    public static class PackedVertex
    {
        public static final Vector2f ZERO = new Vector2f().zero();

        public Vector3f Position;
        public Vector3f Normal;
        public Vector3f Tangent;
        public Vector2f UV0 = ZERO, UV1 = ZERO, UV2 = ZERO;
        public float Mass = 1.0f;
        public ArrayList<SoftbodySpring> Springs = new ArrayList<>();

        public float Weight0, Weight1, Weight2, Weight3;
        public int Joint0, Joint1, Joint2, Joint3;
        public int ClusterIndex;

        @Override public boolean equals(Object other)
        {
            if (!(other instanceof PackedVertex v)) return false;
            if (v == this) return true;
            return
                v.Position.equals(Position) &&
                v.Normal.equals(Normal) &&
                v.Tangent.equals(Tangent) &&
                v.UV0.equals(UV0) &&
                v.UV1.equals(UV1) &&
                v.UV2.equals(UV2);
        }

        @Override public int hashCode()
        {
            int result = 1;
            result = 31 * result + Position.hashCode();
            result = 31 * result + Normal.hashCode();
            result = 31 * result + Tangent.hashCode();
            result = 31 * result + UV0.hashCode();
            result = 31 * result + UV1.hashCode();
            result = 31 * result + UV2.hashCode();
            return result;
        }

    }

    public static void Work2(String[] args)
    {
        // if (args.length != 1)
        // {
        //     System.out.println("java -jar asya.jar <blendpacket>");
        //     return;
        // }

        args = new String[] { "C:/Users/Aidan/Desktop/asya.blendpacket" };



        final String BASE_TEXTURE_DIRECTORY = "gamedata_randy/texture_library/";
        final String BASE_MESH_DIRECTORY = "gamedata_randy/mesh_library/";
        final String BASE_MATERIAL_DIRECTORY = "gamedata_randy/gmat/";
        final String PALETTE_DIRECTORY = "gamedata_randy/palettes/auto_generated/";

        System.out.println("Processing blend packet");
        var packet = BlendPacket.FromFile(new File(args[0]));

        // var database = new FileDB("E:/emu/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/output/asya.map");
        // var cache = new FileArchive("E:/emu/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/patch0.farc");

        var database = new FileDB("E:\\emu\\rpcs3\\dev_hdd0\\game\\NPEA00324\\USRDIR\\output\\brg_patch.map");
        var cache = new FileArchive("E:\\\\emu\\\\rpcs3\\\\dev_hdd0\\\\game\\\\NPEA00324\\\\USRDIR\\\\data.farc");

        // var database = new FileDB(0x100);
        // var cache = new SaveArchive(new Revision(0x3f8), 4);


        for (var texture : packet.Textures)
        {
            String path = (BASE_TEXTURE_DIRECTORY + texture.Name + ".tex").toLowerCase();
            GUID guid = Crypto.makePathGUID(path);

            texture.Descriptor = new ResourceDescriptor(guid, ResourceType.TEXTURE);

            if (texture.PixelData.length == 0) continue;

            var image = new BufferedImage(texture.Width, texture.Height, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, texture.Width, texture.Height, texture.PixelData, 0, texture.Width);

            byte[] resourceData = Images.toTEX(image, CompressionType.DXT5, (texture.Flags & 1) == 0, true);

            var row = database.get(guid);
            if (row == null) row = database.newFileDBRow(path, guid);
            row.setDetails(resourceData);
            cache.add(resourceData);
        }

        for (var material : packet.Materials)
        {
            String path;
            if (material.Path != null && !material.Path.isEmpty())
                path = material.Path;
            else 
                path = (BASE_MATERIAL_DIRECTORY + material.Name + ".gmat").toLowerCase();

            GUID guid = Crypto.makePathGUID(path);
            material.Descriptor = new ResourceDescriptor(guid, ResourceType.GFX_MATERIAL);

            if (material.BinaryData.length == 0) continue;

            var resource = new SerializedResource(material.BinaryData).loadResource(RGfxMaterial.class);
            resource.flags &= ~0x10000;
            resource.shaders = new byte[10][];
            resource.forceGenerateAsGlassy = (material.Flags & BlendMaterial.GLASSY) != 0;

            var dict = new HashMap<Integer, Integer>();
            int rolling = 0;
            for (var box : resource.boxes)
            {
                if (box.type != BoxType.BL_TEXTURE_SAMPLE) continue;
                int[] params = box.getParameters();
                
                int index = params[0];
                if (dict.containsKey(index)) params[0] = dict.get(index);
                else
                {
                    resource.textures[rolling] = packet.Textures[index].Descriptor;
                    dict.put(index, rolling);
                    params[0] = rolling;
                    index = rolling++;
                    
                }
            }

            String source = GfxAssembler.generateShaderSource(resource, -1, false);

            try { CgAssembler.compile(source, resource, GameShader.LBP2); } 
            catch (Exception ex) 
            {
                ex.printStackTrace();
                continue;
            }

            byte[] resourceData = SerializedResource.compress(resource.build(new Revision(0x3f8), CompressionFlags.USE_ALL_COMPRESSION));


            var row = database.get(guid);
            if (row == null) row = database.newFileDBRow(path, guid);
            row.setDetails(resourceData);
            cache.add(resourceData);
        }

        for (var material : packet.Materials)
        {
            String path = (PALETTE_DIRECTORY + material.Name + ".plan").toLowerCase();
            GUID guid = Crypto.makePathGUID(path);

            var thing = new Thing(1);
            thing.setPart(Part.SHAPE, new PShape());
            thing.setPart(Part.GENERATED_MESH, new PGeneratedMesh(material.Descriptor, null));
            thing.setPart(Part.POS, new PPos());
            thing.setPart(Part.BODY, new PBody());

            var details = new InventoryItemDetails();
            details.userCreatedDetails = new UserCreatedDetails(material.Name, "");
            details.type = EnumSet.of(InventoryObjectType.PRIMITIVE_MATERIAL);


            var plan = new RPlan(new Revision(0x272, 0x4c44, 0x0017), CompressionFlags.USE_ALL_COMPRESSION, thing, details);


            byte[] resourceData = SerializedResource.compress(plan.build());

            var row = database.get(guid);
            if (row == null) row = database.newFileDBRow(path, guid);
            row.setDetails(resourceData);
            cache.add(resourceData);
        }

        var skeleton = new Bone[packet.Bones.length];
        for (int i = 0; i < skeleton.length; ++i)
        {
            var bbone = packet.Bones[i];
            var bone = new Bone(bbone.Name);

            bone.firstChild = bbone.FirstChild;
            bone.nextSibling = bbone.NextSibling;
            bone.parent = bbone.Parent;
            bone.skinPoseMatrix = bbone.SkinPoseMatrix;
            bone.invSkinPoseMatrix = bbone.SkinPoseMatrix.invert(new Matrix4f());
            
            skeleton[i] = bone;
        }

        if (packet.Bones.length == 0)
        {
            skeleton = new Bone[] { new Bone("Bone") };
        }

        for (var mesh : packet.Meshes)
        {
            String path;
            if (mesh.Path != null && !mesh.Path.isEmpty())
                path = mesh.Path;
            else
                path = (BASE_MESH_DIRECTORY + mesh.Name + ".mol").toLowerCase();

            // GUID guid = Crypto.makePathGUID(path);
            GUID guid = new GUID(251193);

            var loopToVertex = new int[mesh.NumLoops];
            var vertexSet = new HashSet<PackedVertex>(mesh.NumLoops);
            var vertexList = new ArrayList<PackedVertex>(mesh.NumLoops);
            var vertexRemap = new int[mesh.NumVertices];

            // want duplicate vertices to be sequential
            for (int i = 0; i < mesh.NumVertices; ++i)
            {
                vertexRemap[i] = vertexList.size();

                for (int j = 0; j < mesh.NumLoops; ++j)
                {
                    if (mesh.Loops[j] != i) continue;

                    var v = new PackedVertex();
                    v.Position = mesh.Positions[i];
                    v.Normal = mesh.Normals[j];
                    v.Tangent = mesh.Tangents[j];
                    if ((mesh.Flags & BlendMesh.HAS_SOFTPHYSICS) != 0)
                        v.Mass = mesh.Mass[i];

                    if (mesh.NumUvLayers > 0) v.UV0 = mesh.TextureCoordinates[0][j];
                    if (mesh.NumUvLayers > 1) v.UV1 = mesh.TextureCoordinates[1][j];
                    if (mesh.NumUvLayers > 2) v.UV2 = mesh.TextureCoordinates[2][j];

                    if ((mesh.Flags & BlendMesh.HAS_ARMATURE) != 0)
                    {
                        v.Joint0 = mesh.Joints[i].x;
                        v.Joint1 = mesh.Joints[i].y;
                        v.Joint2 = mesh.Joints[i].z;
                        v.Joint3 = mesh.Joints[i].w;

                        v.Weight0 = mesh.Weights[i].x;
                        v.Weight1 = mesh.Weights[i].y;
                        v.Weight2 = mesh.Weights[i].z;
                        v.Weight3 = mesh.Weights[i].w;
                    }

                    if ((mesh.Flags & BlendMesh.HAS_SOFTBODY_CLUSTERS) != 0)
                        v.ClusterIndex = mesh.ClusterIndices[i] * 2;

                    if (vertexSet.add(v))
                    {
                        loopToVertex[j] = vertexList.size();
                        vertexList.add(v);
                    }
                    else
                    {
                        loopToVertex[j] = vertexList.indexOf(v);
                    }
                }
            }

            int vertexOffset = 0;
            if (vertexList.size() % 8 != 0)
                vertexOffset = 8 - (vertexList.size() % 8);
            
            for (var spring : mesh.Springs)
            {
                vertexList.get(vertexRemap[spring.A]).Springs.add(spring);
                vertexList.get(vertexRemap[spring.B]).Springs.add(spring);
            }
            
            if (mesh.Springs.size() > 0)
            {
                PackedVertex bp, bpo;
                SoftbodySpring bs, bs2, bs3;
                bs3 = null;
                int a, b, c, i, v0;
                int notthis = 0;

                for (a = mesh.NumVertices, i = 0; a > 0; --a, ++i)
                {
                    bp = vertexList.get(vertexRemap[i]);
                    bpo = null;
                    
                    v0 = (mesh.NumVertices - a);
                    for (b = bp.Springs.size(); b > 0; --b)
                    {
                        bs = bp.Springs.get(b - 1);
                        if (v0 == bs.A)
                        {
                            bpo = vertexList.get(vertexRemap[bs.B]);
                            notthis = bs.B;
                        }
                        else
                        {
                            if (v0 == bs.B)
                            {
                                bpo = vertexList.get(vertexRemap[bs.A]);
                                notthis = bs.A;
                            }
                            else
                            {
                                throw new RuntimeException("fuck you");
                            }
                        }

                        if (bpo != null)
                        {
                            for (c = bpo.Springs.size(); c > 0; c--)
                            {
                                bs2 = bpo.Springs.get(c - 1);
                                if ((bs2.A != notthis) && (bs2.A > v0))
                                {
                                    var spring = new SoftbodySpring(v0, bs2.A, 0.0f);
                                    var va = vertexList.get(vertexRemap[spring.A]).Position;
                                    var vb = vertexList.get(vertexRemap[spring.B]).Position;
                                    spring.restLengthSq = 
                                        (float)(Math.pow(vb.x - va.x, 2.0) +
                                        Math.pow(vb.y - va.y, 2.0) + 
                                        Math.pow(vb.z - va.z, 2.0));



                                    mesh.Springs.add(spring);

                                }
                                if (bs2.B != notthis && bs2.B > v0)
                                {
                                    var spring = new SoftbodySpring(v0, bs2.B, 0.0f);
                                    var va = vertexList.get(vertexRemap[spring.A]).Position;
                                    var vb = vertexList.get(vertexRemap[spring.B]).Position;
                                    spring.restLengthSq = 
                                        (float)(Math.pow(vb.x - va.x, 2.0) +
                                        Math.pow(vb.y - va.y, 2.0) + 
                                        Math.pow(vb.z - va.z, 2.0));


                                    mesh.Springs.add(spring);

                                }
                            }
                        }


                    }


                }




            }

            for (var spring : mesh.Springs)
            {
                spring.A = (short)(vertexRemap[spring.A] + vertexOffset);
                spring.B = (short)(vertexRemap[spring.B] + vertexOffset);
            }

            int numVertices = vertexList.size() + vertexOffset;





            var vs = new MemoryOutputStream(numVertices * 0x10);
            var ss = new MemoryOutputStream(numVertices * 0x10);
            var as = new MemoryOutputStream(numVertices * mesh.NumUvLayers * 0x8);


            var dummyVertexListHack = new ArrayList<>(vertexList);
            for (int i = 0; i < vertexOffset; ++i)
                dummyVertexListHack.add(0, vertexList.get(0));

            for (var vertex : dummyVertexListHack)
            {
                vs.v3(vertex.Position);

                // up to 3 clusters! i dont care though
                vs.u8(vertex.ClusterIndex); 
                vs.u8(0); vs.u8(0);

                vs.u8(Math.round(vertex.Mass * 255.0f));


                ss.u8(Math.round(vertex.Weight2 * 255.0f)); 
                ss.u8(Math.round(vertex.Weight1 * 255.0f));
                ss.u8(Math.round(vertex.Weight0 * 255.0f));

                ss.u8(vertex.Joint0); // first bone
                ss.u24(Bytes.packNormal24(vertex.Normal));
                ss.u8(vertex.Joint1); // second bone
                ss.u24(Bytes.packNormal24(vertex.Normal)); // smooth normal, calculate later
                ss.u8(vertex.Joint2); // third bone
                ss.u24(Bytes.packNormal24(vertex.Tangent));
                ss.u8(vertex.Joint3); // fourth bone


                if (mesh.NumUvLayers > 0) as.v2(vertex.UV0);
                if (mesh.NumUvLayers > 1) as.v2(vertex.UV1);
                if (mesh.NumUvLayers > 2) as.v2(vertex.UV2);
            }

            var is = new MemoryOutputStream(mesh.NumTriangles * 0x2 * 0x3);
            int firstIndex = 0;
            var primitives = new ArrayList<Primitive>();
            for (var prim : mesh.Primitives)
            {
                int max = Integer.MIN_VALUE;
                int min = Integer.MAX_VALUE;

                for (int index : prim.Triangles)
                {
                    int v = loopToVertex[index] + vertexOffset;
                    if (v > max) max = v;
                    if (v < min) min = v;

                    is.u16(v);
                }

                primitives.add(new Primitive(packet.Materials[prim.MaterialIndex].Descriptor, min, max, firstIndex, prim.Triangles.length));
                firstIndex += prim.Triangles.length;
            }

            var rmesh = new RMesh(
                new byte[][] { vs.getBuffer(), ss.getBuffer() },
                as.getBuffer(),
                is.getBuffer(),
                skeleton
            );

            if ((mesh.Flags & BlendMesh.HAS_SOFTPHYSICS) != 0)
                rmesh.generateEquivs();

            rmesh.getPrimitives().addAll(primitives);
            if ((mesh.Flags & BlendMesh.HAS_SOFTBODY_SPRINGS) != 0)
                rmesh.generateSprings(mesh.Springs);
            rmesh.calculateSmoothNormals();
            rmesh.calculateBoundBoxes(true);
            if ((mesh.Flags & BlendMesh.HAS_SOFTBODY_CLUSTERS) != 0)
                rmesh.generateClusters(packet.Clusters);
            rmesh.strip();

            if ((mesh.Flags & BlendMesh.HAS_SOFTPHYSICS) != 0 && mesh.SoftPhysSettings != null && !mesh.SoftPhysSettings.isEmpty())
            {
                rmesh.setSoftPhysSettings(new ResourceDescriptor(Crypto.makePathGUID(mesh.SoftPhysSettings), ResourceType.SETTINGS_SOFT_PHYS));
            }


            byte[] resourceData = SerializedResource.compress(rmesh.build(new Revision(0x132), CompressionFlags.USE_NO_COMPRESSION));

            var row = database.get(guid);
            if (row == null) row = database.newFileDBRow(path, guid);
            row.setDetails(resourceData);
            cache.add(resourceData);


            // MeshExporter.OBJ.export("C:/Users/Aidan/Desktop/mesh.obj", rmesh);
            // MeshExporter.OBJ.exportsprings("C:/Users/Aidan/Desktop/springs.obj", rmesh);
        }


        // for (FileDBRow row : database)
        // {
        //     if (!row.getPath().endsWith(".mol")) continue;
        //     byte[] fileData = cache.extract(row.getSHA1());
        //     if (fileData == null) continue;
        //     FileIO.write(fileData, row.getName());
        // }

        cache.save();
        database.save();
    }

    public static void main(String[] args) throws Exception
    {
        Work2(args);
        if (true) return;

        var ws = FileSystems.getDefault().newWatchService();
        Paths.get("C:/Users/Aidan/Desktop/blend")
            .register(ws, new WatchEvent.Kind[] { ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE});

        while (true)
        {
            var k = ws.take();
            for (WatchEvent<?> e : k.pollEvents())
            {
                
            }

            try
            {
            Thread.sleep(100);
            }
            catch (Exception ex) {}


            Work2(args);

            k.reset();
        }














    }
}
