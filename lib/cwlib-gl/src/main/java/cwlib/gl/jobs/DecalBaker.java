package cwlib.gl.jobs;

import cwlib.gl.RenderJobManager;
import cwlib.gl.objects.GlProgram;
import cwlib.gl.objects.GlTexture;
import cwlib.resources.RTexture;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.things.components.decals.Decal;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.FileIO;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static org.lwjgl.opengl.GL30.*;

public class DecalBaker
{
    private static final String VERTEX_SHADER_SOURCE =
        FileIO.getResourceFileAsString("/shaders/decal.vs");
    private static final String FRAGMENT_SHADER_SOURCE =
        FileIO.getResourceFileAsString("/shaders/decal.fs");

    private final HashMap<ResourceDescriptor, RTexture> textures = new HashMap<>();
    private final Decal[] decals;
    private BufferedImage result;
    private final int textureWidth = 1024;
    private final int textureHeight = 1024;

    public DecalBaker(Decal[] decals)
    {
        this.decals = decals;

        // Register all texture data
        for (Decal decal : decals)
        {
            byte[] textureData = ResourceSystem.extract(decal.texture);
            if (textureData == null) continue;
            textures.put(decal.texture, new RTexture(textureData));
        }
    }

    public BufferedImage Bake()
    {
        CountDownLatch latch =
            RenderJobManager.GetInstance().EnqueueJob(DecalBaker::WorkFunctionStatic, this);

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

    private void WorkFunction(RenderJobManager target)
    {
        glViewport(0, 0, textureWidth, textureHeight);
        target.SetSRGB(true);
        target.SetTransparentClearColor();
        glClear(GL_COLOR_BUFFER_BIT);

        HashMap<ResourceDescriptor, GlTexture> localGlTextures = new HashMap<>();
        for (ResourceDescriptor descriptor : textures.keySet())
        {
            GlTexture texture = new GlTexture(textures.get(descriptor));
            texture.Link();
            localGlTextures.put(descriptor, texture);
        }

        FloatBuffer vbuf = MemoryUtil.memAllocFloat(0x10 * decals.length);
        IntBuffer ibuf = MemoryUtil.memAllocInt(decals.length * 0x6);

        int base = 0;

        for (Decal decal : decals)
        {
            float u = decal.u;
            float v = decal.v;
            float xvecu = decal.xvecu;
            float xvecv = decal.xvecv;
            float yvecu = decal.yvecu;
            float yvecv = decal.yvecv;
            v -= 0.00440481584519148f;

            vbuf.put((u - xvecu) - yvecu);
            vbuf.put((v - xvecv) - yvecv);
            vbuf.put(0.0f);
            vbuf.put(0.0f);

            vbuf.put((u + xvecu) - yvecu);
            vbuf.put((v + xvecv) - yvecv);
            vbuf.put(1.0f);
            vbuf.put(0.0f);

            vbuf.put((u + xvecu) + yvecu);
            vbuf.put((v + xvecv) + yvecv);
            vbuf.put(1.0f);
            vbuf.put(1.0f);

            vbuf.put((u - xvecu) + yvecu);
            vbuf.put((v - xvecv) + yvecv);
            vbuf.put(0.0f);
            vbuf.put(1.0f);

            ibuf.put(base);
            ibuf.put(base + 1);
            ibuf.put(base + 2);
            ibuf.put(base);
            ibuf.put(base + 2);
            ibuf.put(base + 3);

            base += 4;
        }

        vbuf.flip();
        ibuf.flip();

        GlProgram shader = new GlProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
        shader.bind();

        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        int ebo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vbuf, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 0x0, 0x0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ibuf, GL_STATIC_DRAW);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        int index = 0;
        for (Decal decal : decals)
        {

            GlTexture texture = localGlTextures.get(decal.texture);
            if (texture == null)
            {
                index++;
                continue;
            }

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture.GetID());
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_READ_COLOR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_READ_COLOR);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 6L * index * 4);

            index++;
        }

        glDisable(GL_BLEND);

        result = target.GetResult(0, 0, textureWidth, textureHeight);

        // Cleanup
        shader.Destroy();
        for (GlTexture texture : localGlTextures.values())
            texture.Destroy();
        glDeleteBuffers(ebo);
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        glBindVertexArray(0);
        glUseProgram(0);
        glActiveTexture(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);
    }


    private static void WorkFunctionStatic(RenderJobManager target, Object userData)
    {
        DecalBaker job = (DecalBaker) userData;
        job.WorkFunction(target);
    }
}
