package executables;

import cwlib.resources.RTexture;
import cwlib.types.Resource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class TexToPng {
    public static void main(String[] args) throws IOException {
        byte[] data = System.in.readAllBytes();
        RTexture texture = new RTexture(new Resource(data));
        ImageIO.write(texture.getImage(), "png", new File(args[0]));
    }
}
