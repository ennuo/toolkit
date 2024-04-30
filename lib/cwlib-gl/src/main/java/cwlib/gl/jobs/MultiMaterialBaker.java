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
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.lwjgl.opengl.GL30.*;

public class MultiMaterialBaker
{
    private static final String VertexShaderSource =
        FileIO.getResourceFileAsString("/shaders/bake.vs");

    private static class MaterialBakeParams
    {
        private String FragmentShaderSource;
        private final int[] WrapS = new int[8];
        private final int[] WrapT = new int[8];
        private final RTexture[] Textures = new RTexture[8];
        private final ArrayList<Primitive> Primitives = new ArrayList<>();
    }

    private final boolean IsNormalMap;
    private final boolean IsSpecularMap;
    private final ArrayList<MaterialBakeParams> Materials = new ArrayList<>();

    private int PrimitiveType;
    private FloatBuffer AttributeData;
    private IntBuffer IndexData;

    private final int Width = 2048;
    private final int Height = 2048;
    private BufferedImage Result;

    public MultiMaterialBaker(RMesh mesh, ResourceDescriptor[] descriptors, int port)
    {
        InitializeMeshBufferData(mesh);
        IsNormalMap = port == BrdfPort.BUMP;
        IsSpecularMap = port == BrdfPort.SPECULAR;

        for (ResourceDescriptor descriptor : descriptors)
        {
            RGfxMaterial material = ResourceSystem.load(descriptor, RGfxMaterial.class);
            MaterialBakeParams params = new MaterialBakeParams();
            params.FragmentShaderSource = GfxAssembler.generateBakedShaderSource(material,
                port,
                true);

            for (int i = 0; i < 8; ++i)
            {
                params.WrapS[i] = MapTextureWrapParameter(material.wrapS[i]);
                params.WrapT[i] = MapTextureWrapParameter(material.wrapT[i]);

                ResourceDescriptor texDescriptor = material.textures[i];
                if (texDescriptor == null) continue;
                byte[] textureData = ResourceSystem.extract(texDescriptor);
                if (textureData == null) continue;
                params.Textures[i] = new RTexture(textureData);
            }

            // Filter primitives for rendering
            for (Primitive primitive : mesh.getPrimitives())
            {
                if (primitive.getMaterial().equals(descriptor))
                    params.Primitives.add(primitive);
            }

            Materials.add(params);
        }
    }

    public BufferedImage Bake()
    {
        CountDownLatch latch =
            RenderJobManager.GetInstance().EnqueueJob(MultiMaterialBaker::WorkFunctionStatic,
                this);

        try { latch.await(); }
        catch (Exception ex) { return null; }

        return Result;
    }

    public byte[] BakeToPNG()
    {
        Bake();
        byte[] png;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            ImageIO.write(Result, "png", stream);
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
        if (wrap == TextureWrap.CLAMP)
            wrapMode = GL_CLAMP_TO_EDGE;
        if (wrap == TextureWrap.CLAMP_TO_EDGE)
            wrapMode = GL_CLAMP_TO_EDGE;
        if (wrap == TextureWrap.BORDER)
            wrapMode = GL_CLAMP_TO_BORDER;
        return wrapMode;
    }

    private void InitializeMeshBufferData(RMesh mesh)
    {
        // Convert the mesh primitive type to one for an OpenGL context
        PrimitiveType = (mesh.getPrimitiveType().getValue() & 0xff) - 1;

        // Convert index data from big endian short to little endian integer
        IndexData = MemoryUtil.memAllocInt(mesh.getNumIndices());
        byte[] indexStream = mesh.getIndexStream();
        for (int i = 0; i < mesh.getNumIndices(); ++i)
        {
            int offset = (i * 0x2);
            int index =
                ((indexStream[offset] & 0xff) << 8) | (indexStream[offset + 1] & 0xff);
            IndexData.put(index);
        }
        IndexData.flip();

        // We specifically only need the UV attributes for the vertex buffers
        AttributeData = MemoryUtil.memAllocFloat(8 * mesh.getNumVerts());

        Vector2f[] UV0 = mesh.getUVs(0);
        Vector2f[] UV1 = UV0;
        Vector2f[] decalUV = mesh.getUVs(mesh.getAttributeCount() - 1);
        if (mesh.getAttributeCount() > 1)
            UV1 = mesh.getUVs(1);
        Vector2f[] targetUV = UV0;


        for (int i = 0; i < mesh.getNumVerts(); ++i)
        {
            AttributeData.put(UV0[i].x);
            AttributeData.put(UV0[i].y);
            AttributeData.put(UV1[i].x);
            AttributeData.put(UV1[i].y);
            AttributeData.put(decalUV[i].x);
            AttributeData.put(decalUV[i].y);
            AttributeData.put(targetUV[i].x);
            AttributeData.put(targetUV[i].y);
        }
        AttributeData.flip();
    }

    private void WorkFunction(RenderJobManager target)
    {
        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        int ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, AttributeData, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, IndexData, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 0x20, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0x20, 0x10);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0x20, 0x18);

        glViewport(0, 0, Width, Height);
        if (IsNormalMap)
        {
            target.SetSRGB(false);
            target.SetNormalClearColor();
        }
        else
        {
            target.SetSRGB(true);
            target.SetTransparentClearColor();
        }

        if (IsSpecularMap)
        {
            glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        glClear(GL_COLOR_BUFFER_BIT);

        for (MaterialBakeParams params : Materials)
        {
            GlProgram program = new GlProgram(VertexShaderSource,
                params.FragmentShaderSource);
            program.bind();

            // First loop to initialize the textures
            GlTexture[] localGlTextures = new GlTexture[8];
            for (int i = 0; i < 8; ++i)
            {
                if (params.Textures[i] == null) continue;

                GlTexture texture = new GlTexture(params.Textures[i]);
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
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR_MIPMAP_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, params.WrapS[i]);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, params.WrapT[i]);

            }


            // Render the material
            for (Primitive primitive : params.Primitives)
            {
                glDrawElements(PrimitiveType, primitive.getNumIndices(), GL_UNSIGNED_INT,
                    primitive.getFirstIndex() * 4L);
            }

            glUseProgram(0);
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

        Result = target.GetResult(0, 0, Width, Height);

        // Make sure to clean up any resources we used
        glBindVertexArray(0);
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
    }

    private static void WorkFunctionStatic(RenderJobManager target, Object userData)
    {
        MultiMaterialBaker job = (MultiMaterialBaker) userData;
        job.WorkFunction(target);
    }
}
