package cwlib.io.imports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.Part;
import cwlib.io.serializer.SerializationData;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.resources.RMesh;
import cwlib.resources.RPlan;
import cwlib.resources.RStaticMesh;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.staticmesh.StaticMeshInfo;
import cwlib.structs.staticmesh.StaticPrimitive;
import cwlib.structs.staticmesh.StaticMeshInfo.StaticMeshTreeNode;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.types.SerializedResource;
import cwlib.types.archives.FileArchive;
import cwlib.types.data.Revision;
import cwlib.types.databases.FileDB;
import cwlib.util.Bytes;
import cwlib.util.Crypto;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;

public class StaticMeshBuilder 
{
    private class StaticMeshVertex
    {
        public Vector3f Position;
        public Vector3f Normal;
        public Vector3f UV0;
        public Vector3f Tangent;
        public Vector3f UV1;
        public Vector3f SmoothNormal;
    };

    private RStaticMesh build = new RStaticMesh();
    private Vector4f globalMaxCoord =  new Vector4f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    private Vector4f globalMinCoord = new Vector4f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

    private int nextUID = 1;
    private int vertexOffset;
    private int indexOffset;

    private HashMap<Integer, Integer> indexCache = new HashMap<>();

    public void add(Thing thing)
    {
        if (thing == null) return;

        PRenderMesh part = thing.getPart(Part.RENDER_MESH);
        PPos pos = thing.getPart(Part.POS);

        // No point if we don't actually have a position in the world.
        if (pos == null) return;

        // We're only building static meshes from things that
        // have render mesh components.
        if (part == null || part.mesh == null) return;

        // If the render mesh has an animation, we can't bake it
        // into the static mesh.
        if (part.anim != null) return;

        Matrix4f wpos = pos.worldPosition;

        RMesh mesh = ResourceSystem.load(part.mesh, RMesh.class);
        if (mesh == null) throw new RuntimeException("Failed to extract RMesh (" + part.mesh.toString() + ")");

        StaticMeshInfo info = build.getMeshInfo();

        // Wrapping this in an inner-scope so the arrays are hopefully destroyed,
        // although I doubt the GC even hits until after the function completes,
        // no idea, I don't care about how Java works internally honestly.

        Vector4f[] wVerts = new Vector4f[mesh.getNumVerts()];
        {
            Vector3f[] vertices = mesh.getVertices();
            Vector3f[] normals = mesh.getNormals();
            Vector3f[] smoothNormals = mesh.getSmoothNormals();
            Vector4f[] tangents = mesh.getTangents();
            Vector2f[] uv0 = mesh.getUVs(0);
            Vector2f[] uv1 = mesh.getUVs(1);
            byte[][] joints = mesh.getJoints();
            Vector4f[] weights = mesh.getWeights();

            Bone[] bones = mesh.getBones();

            // Compute all the bone matrices so we can bake the skinned vertex positions.
            Matrix4f[] matrices = new Matrix4f[bones.length];
            matrices[0] = wpos.mul(bones[0].invSkinPoseMatrix, new Matrix4f());

            // In case any bone things are null, use the bind matrix of the root bone.
            for (int i = 0; i < bones.length; ++i) matrices[i] = matrices[0];

            for (Thing boneThing : part.boneThings)
            {
                if (boneThing == null || boneThing == thing || !boneThing.hasPart(Part.POS)) continue;
                
                PPos bonePos = boneThing.getPart(Part.POS);
                int index = Bone.indexOf(bones, bonePos.animHash);
                if (index == -1) continue;

                matrices[index] = bonePos.worldPosition.mul(bones[index].invSkinPoseMatrix, new Matrix4f());
            }


            MemoryOutputStream vertexStream = new MemoryOutputStream(mesh.getNumVerts() * RStaticMesh.VERTEX_STRIDE);
            Vector4f wMax = new Vector4f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
            Vector4f wMin = new Vector4f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
            for (int i = 0; i < mesh.getNumVerts(); ++i)
            {
                Matrix4f skin = matrices[joints[i][0]].scale(weights[i].x, new Matrix4f())
                        .add(matrices[joints[i][1]].scale(weights[i].y, new Matrix4f()))
                        .add(matrices[joints[i][2]].scale(weights[i].z, new Matrix4f()))
                        .add(matrices[joints[i][3]].scale(weights[i].w, new Matrix4f()));

                Matrix3f skin3 = skin.get3x3(new Matrix3f());
                Vector4f vpos = new Vector4f(vertices[i], 1.0f).mul(skin);

                wVerts[i] = vpos;

                Vector3f normal = normals[i].mul(skin3);
                Vector3f smoothNormal = smoothNormals[i].mul(skin3);
                Vector3f tangent = new Vector3f(tangents[i].x, tangents[i].y, tangents[i].z).mul(skin3);

                vertexStream.v3(vpos);
                vertexStream.u32(Bytes.packNormal32(normal));
                vertexStream.f16(uv0[i].x);
                vertexStream.f16(uv0[i].y);
                vertexStream.u32(Bytes.packNormal32(tangent));
                vertexStream.f16(uv1[i].x);
                vertexStream.f16(uv1[i].y);
                vertexStream.u32(Bytes.packNormal32(smoothNormal));

                wMax.max(vpos);
                wMin.min(vpos);
            }

            globalMaxCoord.max(wMax);
            globalMinCoord.min(wMin);

            build.vertexData = Bytes.combine(build.vertexData, vertexStream.getBuffer());
        }

        byte[] indexData = mesh.getIndexStream();

        // Every primitive in a standard mesh uses a vertex offset of 0,
        // while in a static mesh, we can actually offset by the primitive's min vert
        for (Primitive primitive : mesh.getPrimitives())
        {
            Vector4f max = new Vector4f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
            Vector4f min = new Vector4f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

            byte[] localIndexData = new byte[primitive.numIndices * 2];
            for (int i = 0, offset = (2 * primitive.firstIndex); i < primitive.numIndices; ++i, offset += 2)
            {
                int index = ((indexData[offset] & 0xff) << 8) | (indexData[offset + 1] & 0xff);

                if (index != 65535)
                {
                    max.max(wVerts[index]);
                    min.min(wVerts[index]);

                    index -= primitive.minVert;
                }

                localIndexData[(i * 2) + 0] = (byte)(index >> 8);
                localIndexData[(i * 2) + 1] = (byte)(index & 0xff);
            }

            int localIndexHash = Crypto.getHash(localIndexData);
            
            int indexDataStart;
            if (indexCache.containsKey(localIndexHash))
            {
                indexDataStart = indexCache.get(localIndexHash);
            }
            else
            {
                indexDataStart = indexOffset;
                build.indexData = Bytes.combine(build.indexData, localIndexData);
                indexOffset += primitive.numIndices;
            }

            // Primitives should be sorted into the bounding boxes too honestly
            info.primitives.add(new StaticPrimitive(min, max, primitive.getMaterial(), vertexOffset + primitive.minVert, indexDataStart, primitive.numIndices, mesh.getPrimitiveType()));
            info.nodes.get(0).numPrimitives++;
        }

        vertexOffset += mesh.getNumVerts();
    }

    public void export(String path)
    {
        StaticMeshInfo info = build.getMeshInfo();
        info.indexBufferSize = build.indexData.length;
        info.vertexStreamSize = build.vertexData.length;

        StaticMeshTreeNode node = info.nodes.get(0);
        node.max = new Vector3f(globalMaxCoord.x, globalMaxCoord.y, globalMaxCoord.z);
        node.min = new Vector3f(globalMinCoord.x, globalMinCoord.y, globalMinCoord.z);



        byte[] resourceData = SerializedResource.compress(new SerializationData(
            Bytes.combine(build.vertexData, build.indexData),
            new Revision(0x3f8),
            info
        ));

        FileIO.write(GsonUtils.toJSON(info).getBytes(), path + ".json");

        FileIO.write(resourceData, path);

    }



    public static void main(String[] args) 
    {
        ResourceSystem.GUI_MODE = false;
        ResourceSystem.getDatabases().add(new FileDB("E:\\emu\\rpcs3\\dev_hdd0\\game\\NPUA80662\\USRDIR\\output\\brg_patch.map"));
        ResourceSystem.getDatabases().add(new FileDB("E:\\emu\\rpcs3\\dev_hdd0\\game\\NPUA80662\\USRDIR\\output\\blurayguids.map"));

        ResourceSystem.getArchives().add(new FileArchive("E:\\emu\\rpcs3\\dev_hdd0\\game\\NPUA80662\\USRDIR\\data.farc"));
        ResourceSystem.getArchives().add(new FileArchive("E:\\emu\\rpcs3\\dev_hdd0\\game\\NPUA80662\\USRDIR\\patches\\cumulative_0133.farc"));

        RPlan plan = new SerializedResource("C:/Users/Aidan/Desktop/ball.plan").loadResource(RPlan.class);
        Thing[] things = plan.getThings();

        StaticMeshBuilder builder = new StaticMeshBuilder();
        builder.add(things[0]);
        builder.export("C:/Users/Aidan/Desktop/test.smh");

    }
}
