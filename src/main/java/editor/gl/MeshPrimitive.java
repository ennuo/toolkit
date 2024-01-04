package editor.gl;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.enums.PrimitiveType;
import cwlib.enums.ResourceType;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.staticmesh.StaticPrimitive;
import cwlib.types.data.ResourceDescriptor;
import editor.gl.RenderSystem.MorphInstance;
import editor.gl.objects.Shader;
import editor.gl.objects.Texture;

import static org.lwjgl.opengl.GL30.*;

public class MeshPrimitive {
    /**
     * Index of first face to render in mesh
     */
    private final int firstIndex;

    /**
     * Number of indices to render from starting point.
     */
    private final int numIndices;

    /**
     * OpenGL primitive type of mesh.
     */
    private final int type;

    /**
     * Material associated with this primitive.
     */
    private final ResourceDescriptor shader;

    private final int region;

    /**
     * Overrides current shader, used for costume pieces.
     */
    private ResourceDescriptor overrideShader;

    
    /**
     * Alpha layer associated with override shader.
     */
    private int overrideAlphaLayer;

    /**
     * Alpha layer associated with this primitive.
     * Used for internal sorting.
     */
    private final int alphaLayer;

    /**
     * Creates a copy of a MeshPrimitive
     * @param primitive Primitive to copy
     */
    public MeshPrimitive(MeshPrimitive primitive) {
        this.firstIndex = primitive.firstIndex;
        this.numIndices = primitive.numIndices;
        this.type = primitive.type;
        this.shader = primitive.shader;
        this.alphaLayer = primitive.alphaLayer;
        this.region = primitive.region;
    }

    /**
     * Constructs a MeshPrimitive given a material, type, and index count.
     * Used for GeneratedMesh primitives.
     * @param shader Material associated with primitive
     * @param type Primitive type
     * @param count Number of indices
     */
    public MeshPrimitive(ResourceDescriptor shader, PrimitiveType type, int count) {
        this.firstIndex = 0;
        this.numIndices = count;
        this.type = (type.getValue() & 0xff) - 1;

        this.shader = shader;

        this.alphaLayer = Shader.get(this.shader).alphaLayer;

        this.region = 0;
    }

    /**
     * Constructs a MeshPrimitive from a RStaticMesh primitive.
     * @param primitive StaticMesh primitive
     */
    public MeshPrimitive(StaticPrimitive primitive) {
        this.firstIndex = primitive.indexStart;
        this.numIndices = primitive.numIndices;
        this.type = (primitive.type.getValue() & 0xff) - 1;

        this.shader = primitive.gmat;

        this.alphaLayer = Shader.get(this.shader).alphaLayer;

        this.region = 0;
    }

    /**
     * Constructs a MeshPrimitive from a skinned mesh primitive.
     * @param primitive Skinned primitive
     * @param type Primitive type
     */
    public MeshPrimitive(Primitive primitive, PrimitiveType type) {
        this.firstIndex = primitive.getFirstIndex();
        this.numIndices = primitive.getNumIndices();
        this.type = (type.getValue() & 0xff) - 1;

        this.shader = primitive.getMaterial();

        this.alphaLayer = Shader.get(this.shader).alphaLayer;

        this.region = primitive.getRegion();
    }

    /**
     * Draws this primitive to current buffer.
     */
    public void draw(Texture instanceTexture, Matrix4f[] model, Vector4f color, MorphInstance morph) {
        Shader shader = RenderSystem.OVERRIDE_SHADER;
        if (shader == null)
            shader = Shader.get(this.getMaterial());
        shader.bind(instanceTexture, model, color, morph);

        // Offset polygons slightly for decals
        if (this.alphaLayer != 0) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(-1.0f, -1.0f);
            glDrawElements(this.type, this.numIndices, GL_UNSIGNED_INT, this.firstIndex * 4);
            glDisable(GL_POLYGON_OFFSET_FILL);
            return;
        }

        glDrawElements(this.type, this.numIndices, GL_UNSIGNED_INT, this.firstIndex * 4);
    }

    public void draw(Texture instanceTexture, Matrix4f[] model, Vector4f color) {
        this.draw(instanceTexture, model, color, null);
    }

    public int getRegion() { return this.region; }

    public int getAlphaLayer() {
        if (this.overrideShader != null) return this.overrideAlphaLayer;
        return this.alphaLayer; 
    }

    public int getFirstIndex() { return this.firstIndex; }
    public int getNumIndices() { return this.numIndices; }
    public int getPrimitiveType() { return this.type; }

    public ResourceDescriptor getBaseMaterial() { return this.shader; }
    public ResourceDescriptor getMaterial() {
        if (this.overrideShader != null) return this.overrideShader;
        return this.shader; 
    }

    public void override(ResourceDescriptor shader) {
        this.overrideShader = shader;
        this.overrideAlphaLayer = Shader.get(this.overrideShader).alphaLayer;
    }
}