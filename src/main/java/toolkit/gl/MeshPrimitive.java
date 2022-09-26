package toolkit.gl;

import cwlib.structs.mesh.Primitive;
import cwlib.structs.staticmesh.StaticPrimitive;
import cwlib.types.data.ResourceDescriptor;

public class MeshPrimitive {
    public int firstIndex, numIndices;
    public int type;
    public ResourceDescriptor shader;

    public MeshPrimitive(ResourceDescriptor shader, int count) {
        this.firstIndex = 0;
        this.numIndices = count;
        this.shader = shader;
    }

    public MeshPrimitive(StaticPrimitive primitive) {
        this.firstIndex = primitive.indexStart;
        this.numIndices = primitive.numIndices;
        this.shader = primitive.gmat;
        this.type = (primitive.type.getValue() & 0xff) - 1;
    }

    public MeshPrimitive(Primitive primitive) {
        this.firstIndex = primitive.getFirstIndex();
        this.numIndices = primitive.getNumIndices();
        this.shader = primitive.getMaterial();
        Shader.get(this.shader); // Preload
    }
}