package cwlib.structs.staticmesh;

import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

import java.util.ArrayList;

import org.joml.Vector3f;

public class StaticMeshInfo implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public static class StaticMeshTreeNode implements Serializable
    {
        public static final int BASE_ALLOCATION_SIZE = 0x20;

        /**
         * The 3D coordinate of the minimal point of the bound box containing this primitive group.
         */
        public Vector3f min;

        /**
         * The 3D coordinate of the maximal point of the bound box containing this primitive group.
         */
        public Vector3f max;

        /**
         * The index of the first child of this tree node, if any.
         */
        public short firstChild = -1;

        /**
         * The index of the next sibling of this tree node, if any.
         */
        public short nextSibling = -1;
        

        /**
         * The index of the first mesh primitive referenced by this group, if any.
         */
        public short firstPrimitive;

        /**
         * The number of primitive contained in this group.
         */
        public short numPrimitives;

        @Override
        public void serialize(Serializer serializer)
        {
            min = serializer.v3(min);
            firstChild = serializer.i16(firstChild);
            nextSibling = serializer.i16(nextSibling);
            max = serializer.v3(max);
            firstPrimitive = serializer.i16(firstPrimitive);
            numPrimitives = serializer.i16(numPrimitives);
        }

        @Override
        public int getAllocatedSize()
        {
            return StaticMeshTreeNode.BASE_ALLOCATION_SIZE;
        }
    }

    public ResourceDescriptor lightmap, risemap, fallmap;

    public int indexBufferSize, vertexStreamSize;

    /**
     * All render primitives contained in this background.
     */
    public ArrayList<StaticPrimitive> primitives = new ArrayList<>();
    
    /**
     * Bound box volumes containing primitives in a tree hierachy.
     * Used for streaming in/out the parts of the background.
     * 
     * If there's only a single primitive, root node should just be the bound box
     * of the entire model and point to the primitive
     * 
     * If there are multiple primitives, the root node should be a group node encompassing
     * every model in the background and point to the children groups.
     */
    public ArrayList<StaticMeshTreeNode> nodes = new ArrayList<>();

    public StaticMeshInfo()
    {
        nodes.add(new StaticMeshTreeNode());
    }

    @Override
    public void serialize(Serializer serializer)
    {
        lightmap = serializer.resource(lightmap, ResourceType.TEXTURE);
        risemap = serializer.resource(risemap, ResourceType.TEXTURE);
        fallmap = serializer.resource(fallmap, ResourceType.TEXTURE);


        int primitiveCount = serializer.i32(primitives.size());
        int nodeCount = serializer.i32(nodes.size());

        indexBufferSize = serializer.i32(indexBufferSize);
        vertexStreamSize = serializer.i32(vertexStreamSize);

        // 0x4c
        // 0x0f
        // @ 0x2c

        primitives = serializer.arraylist(primitives, StaticPrimitive.class);
        nodes = serializer.arraylist(nodes, StaticMeshTreeNode.class);

        if (primitiveCount != primitives.size()) throw new SerializationException("Primitive count mismatch!");
        if (nodeCount != nodes.size()) throw new SerializationException("Node count mismatch!");

        serializer.i32(0x48454c50); // "HELP", no idea, used as a marker?
    }

    @Override
    public int getAllocatedSize()
    {
        int size = StaticMeshInfo.BASE_ALLOCATION_SIZE;
        if (this.primitives != null)
            size += (this.primitives.size() * StaticPrimitive.BASE_ALLOCATION_SIZE);
        if (this.nodes != null)
            size += (this.nodes.size() * StaticMeshTreeNode.BASE_ALLOCATION_SIZE);
        return size;
    }
}
