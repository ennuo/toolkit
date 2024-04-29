package cwlib.gl.objects;

import cwlib.resources.RTexture;
import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33.*;

public class GlTexture
{
    public final int width;
    public final int height;
    public final boolean linear;
    private final ByteBuffer cpuData;
    private int id;

    public GlTexture(RTexture texture)
    {
        this(texture.getImage(), texture.noSRGB);
    }

    public GlTexture(BufferedImage image, boolean linear)
    {
        width = image.getWidth();
        height = image.getHeight();
        this.linear = linear;
        cpuData = BufferUtils.createByteBuffer(width * height * 4);
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        for (int y = 0; y < height; ++y)
        {
            for (int x = 0; x < width; ++x)
            {
                int pixel = pixels[x + y * width];

                cpuData.put((byte) ((pixel >> 16) & 0xFF));
                cpuData.put((byte) ((pixel >> 8) & 0xFF));
                cpuData.put((byte) (pixel & 0xFF));
                cpuData.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        cpuData.flip();
    }

    public void Link()
    {
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            (linear ? GL_RGBA : GL_SRGB_ALPHA),
            width,
            height,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            cpuData
        );
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public int GetID()
    {
        return id;
    }

    public void Destroy()
    {
        if (id != 0)
        {
            glDeleteTextures(id);
            id = 0;
        }
    }
}
