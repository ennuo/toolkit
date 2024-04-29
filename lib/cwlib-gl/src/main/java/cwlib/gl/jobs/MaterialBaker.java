package cwlib.gl.jobs;

import cwlib.enums.BrdfPort;
import cwlib.enums.TextureWrap;
import cwlib.gl.RenderJobManager;
import cwlib.gl.objects.GlProgram;
import cwlib.gl.objects.GlTexture;
import cwlib.resources.RGfxMaterial;
import cwlib.resources.RMesh;
import cwlib.resources.RTexture;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.mesh.Primitive;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.FileIO;
import cwlib.util.gfx.GfxAssembler;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.lwjgl.opengl.GL30.*;

public class MaterialBaker
{
    private static final String VERTEX_SHADER_SOURCE =
        FileIO.getResourceFileAsString("/shaders/bake.vs");

    private final String fragmentShaderSource;

    private int primitiveType;
    private FloatBuffer attributeData;
    private IntBuffer indexData;

    private final int[] wrapS = new int[8];
    private final int[] wrapT = new int[8];
    private final RTexture[] textures = new RTexture[8];
    private final ArrayList<Primitive> primitives = new ArrayList<>();

    private int textureWidth = 1024;
    private int textureHeight = 1024;
    private BufferedImage result;

    private final boolean isNormalMap;

    private static int UpperPower(int v)
    {
        v--;
        v |= v >>> 1;
        v |= v >>> 2;
        v |= v >>> 4;
        v |= v >>> 8;
        v |= v >>> 16;
        return ++v;
    }

    public MaterialBaker(RMesh mesh, RGfxMaterial material, ResourceDescriptor materialDescriptor
        , Vector4f uvTransform, int port, int targetUvMap)
    {
        InitializeMeshBufferData(mesh, uvTransform, targetUvMap);

        fragmentShaderSource = GfxAssembler.generateBakedShaderSource(material, port, false);
        isNormalMap = port == BrdfPort.BUMP;

        int w = 128;
        int h = 128;

        for (int i = 0; i < 8; ++i)
        {
            wrapS[i] = MapTextureWrapParameter(material.wrapS[i]);
            wrapT[i] = MapTextureWrapParameter(material.wrapT[i]);

            ResourceDescriptor descriptor = material.textures[i];
            if (descriptor == null) continue;
            byte[] textureData = ResourceSystem.extract(descriptor);
            if (textureData == null) continue;
            textures[i] = new RTexture(textureData);

            w = Math.max(textures[i].getImage().getWidth(), w);
            h = Math.max(textures[i].getImage().getHeight(), h);
        }

        // for (MaterialBox box : material.boxes)
        // {
        //     if (!box.isTexture()) continue;
        //     int index = box.getParameters()[5];
        //     RTexture texture = Textures[index];
        //     if (texture == null) continue;

        //     Vector4f transform = box.getTextureTransform();

        //     w = Math.max((int)(texture.getImage().getWidth() * transform.x), w);
        //     h = Math.max((int)(texture.getImage().getHeight() * transform.y), h);
        // }

        textureWidth = UpperPower(Math.min(w, 2048));
        textureHeight = UpperPower(Math.min(h, 2048));

        // Filter primitives for rendering
        for (Primitive primitive : mesh.getPrimitives())
        {
            if (primitive.getMaterial().equals(materialDescriptor))
                primitives.add(primitive);
        }
    }

    public BufferedImage Bake()
    {
        CountDownLatch latch =
            RenderJobManager.GetInstance().EnqueueJob(MaterialBaker::WorkFunctionStatic,
                this);

        try { latch.await(); }
        catch (Exception ex) { return null; }

        return result;
    }

    public byte[] BakeToPNG()
    {
        Bake();
        byte[] png;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            ImageIO.write(result, "png", stream);
            png = stream.toByteArray();
        }
        catch (Exception ex) { return null; }
        return png;
    }

    private static int MapTextureWrapParameter(TextureWrap wrap)
    {
        int wrapMode = GL_REPEAT;
        if (wrap == TextureWrap.MIRROR)
            wrapMode = GL_MIRRORED_REPEAT;
        if (wrap == TextureWrap.CLAMP_TO_EDGE)
            wrapMode = GL_CLAMP_TO_EDGE;
        if (wrap == TextureWrap.BORDER)
            wrapMode = GL_CLAMP_TO_BORDER;
        return wrapMode;
    }

    private void InitializeMeshBufferData(RMesh mesh, Vector4f transform, int attributeIndex)
    {
        // Convert the mesh primitive type to one for an OpenGL context
        primitiveType = (mesh.getPrimitiveType().getValue() & 0xff) - 1;

        // Convert index data from big endian short to little endian integer
        indexData = MemoryUtil.memAllocInt(mesh.getNumIndices());
        byte[] indexStream = mesh.getIndexStream();
        for (int i = 0; i < mesh.getNumIndices(); ++i)
        {
            int offset = (i * 0x2);
            int index =
                ((indexStream[offset] & 0xff) << 8) | (indexStream[offset + 1] & 0xff);
            indexData.put(index);
        }
        indexData.flip();

        // We specifically only need the UV attributes for the vertex buffers
        attributeData = MemoryUtil.memAllocFloat(8 * mesh.getNumVerts());

        Vector2f[] UV0 = mesh.getUVs(0);
        Vector2f[] UV1 = UV0;
        Vector2f[] decalUV = mesh.getUVs(mesh.getAttributeCount() - 1);
        if (mesh.getAttributeCount() > 1)
            UV1 = mesh.getUVs(1);
        Vector2f[] targetUV = mesh.getUVs(attributeIndex);


        for (int i = 0; i < mesh.getNumVerts(); ++i)
        {
            attributeData.put(UV0[i].x * transform.x);
            attributeData.put(UV0[i].y * transform.y);
            attributeData.put(UV1[i].x * transform.x);
            attributeData.put(UV1[i].y * transform.y);
            attributeData.put(decalUV[i].x);
            attributeData.put(decalUV[i].y);
            attributeData.put(targetUV[i].x);
            attributeData.put(targetUV[i].y);
        }
        attributeData.flip();
    }

    private void WorkFunction(RenderJobManager target)
    {
        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        int ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, attributeData, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 0x20, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0x20, 0x10);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0x20, 0x18);

        GlProgram program = new GlProgram(VERTEX_SHADER_SOURCE, fragmentShaderSource);
        program.bind();

        // First loop to initialize the textures
        GlTexture[] localGlTextures = new GlTexture[8];
        for (int i = 0; i < 8; ++i)
        {
            if (textures[i] == null) continue;

            GlTexture texture = new GlTexture(textures[i]);
            localGlTextures[i] = texture;
            texture.Link();
        }

        // Second loop to bind the textures
        for (int i = 0; i < 8; ++i)
        {
            GlTexture texture = localGlTextures[i];
            if (texture == null) continue;

            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, texture.GetID());
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS[i]);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT[i]);
        }

        glViewport(0, 0, textureWidth, textureHeight);
        if (isNormalMap)
        {
            target.SetSRGB(false);
            target.SetNormalClearColor();
        }
        else
        {
            target.SetSRGB(true);
            target.SetTransparentClearColor();
        }
        glClear(GL_COLOR_BUFFER_BIT);

        // Render the material
        for (Primitive primitive : primitives)
            glDrawElements(primitiveType, primitive.getNumIndices(), GL_UNSIGNED_INT,
                primitive.getFirstIndex() * 4L);
        result = target.GetResult(0, 0, textureWidth, textureHeight);

        // Make sure to clean up any resources we used
        glUseProgram(0);
        glBindVertexArray(0);

        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);

        program.Destroy();
        for (int i = 0; i < 8; ++i)
        {
            GlTexture texture = localGlTextures[i];
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, 0);
            if (texture != null) texture.Destroy();
        }

        glActiveTexture(GL_TEXTURE0);
    }

    private static void WorkFunctionStatic(RenderJobManager target, Object userData)
    {
        MaterialBaker job = (MaterialBaker) userData;
        job.WorkFunction(target);
    }
}
