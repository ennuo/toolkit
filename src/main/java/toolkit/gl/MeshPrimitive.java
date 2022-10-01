package toolkit.gl;

import cwlib.enums.ResourceType;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.staticmesh.StaticPrimitive;
import cwlib.types.data.ResourceDescriptor;

public class MeshPrimitive {
    public int firstIndex, numIndices;
    public int type;
    public ResourceDescriptor shader;
    public int alphaLayer;

    public MeshPrimitive(ResourceDescriptor shader, int count) {
        this.firstIndex = 0;
        this.numIndices = count;
        this.shader = shader;
        this.alphaLayer = Shader.get(this.shader).alphaLayer;
    }

    public MeshPrimitive(StaticPrimitive primitive) {
        this.firstIndex = primitive.indexStart;
        this.numIndices = primitive.numIndices;
        this.shader = primitive.gmat;
        this.type = (primitive.type.getValue() & 0xff) - 1;
        this.alphaLayer = Shader.get(this.shader).alphaLayer;
    }

    public MeshPrimitive(Primitive primitive) {
        this.firstIndex = primitive.getFirstIndex();
        this.numIndices = primitive.getNumIndices();
        this.shader = primitive.getMaterial();
        if (this.shader == null)
            this.shader = new ResourceDescriptor(10803, ResourceType.GFX_MATERIAL);
        this.alphaLayer = Shader.get(this.shader).alphaLayer;
    }
}