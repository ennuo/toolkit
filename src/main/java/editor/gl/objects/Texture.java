package editor.gl.objects;

import java.util.HashMap;

import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import cwlib.resources.RTexture;
import cwlib.types.data.ResourceDescriptor;
import editor.gl.RenderSystem;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Texture {
    public static HashMap<ResourceDescriptor, Texture> TEXTURES = new HashMap<>();

    public int textureID;
    public final ResourceDescriptor descriptor;

    public Texture(ResourceDescriptor descriptor) {
        this.descriptor = descriptor;
        
        System.out.println("Loading texture: " + descriptor);
        
        if (TEXTURES.containsKey(descriptor))
            throw new RuntimeException("Texture is already linked!");
        
        byte[] data = RenderSystem.getSceneGraph().getResourceData(descriptor);
        if (data == null)
            throw new RuntimeException("Unable to retrieve data for texture!");
        RTexture texture = new RTexture(data);

        BufferedImage image = texture.getImage();
        int width = image.getWidth(), height = image.getHeight();

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int pixel = pixels[x + y * width];

                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();

        this.textureID = glGenTextures();
        
        glBindTexture(GL_TEXTURE_2D, textureID);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(
            GL_TEXTURE_2D, 
            0, 
            (texture.noSRGB ? GL_RGBA : GL_SRGB_ALPHA), 
            width, 
            height, 
            0, 
            GL_RGBA, 
            GL_UNSIGNED_BYTE, 
            buffer
        );
        glGenerateMipmap(GL_TEXTURE_2D);

        

        TEXTURES.put(descriptor, this);
    }

    public void delete() {
        if (this.textureID != 0) {
            glDeleteTextures(this.textureID);
            this.textureID = 0;
        }
        TEXTURES.remove(this.descriptor);
    }

    public static Texture get(ResourceDescriptor descriptor) {
        if (descriptor == null) return null;
        if (TEXTURES.containsKey(descriptor))
            return TEXTURES.get(descriptor);
        if (RenderSystem.getSceneGraph().getResourceData(descriptor) == null)
            return null;
        return new Texture(descriptor);
    }
}
