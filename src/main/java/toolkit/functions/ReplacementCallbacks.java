package toolkit.functions;

import cwlib.util.FileIO;
import cwlib.util.Images;
import toolkit.utilities.FileChooser;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.types.databases.FileEntry;

import java.awt.image.BufferedImage;
import java.io.File;

public class ReplacementCallbacks {
    public static void replaceImage() {
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        File file = FileChooser.openFile("image.png", "png,jpeg,jpg,dds", false);
        if (file == null) return;

        BufferedImage image;
        if (file.getAbsolutePath().toLowerCase().endsWith(".dds"))
            image = Images.fromDDS(FileIO.read(file.getAbsolutePath()));
        else image = FileIO.readBufferedImage(file.getAbsolutePath());

        if (image == null) {
            System.err.println("Image was null, cancelling replacement operation.");
            return;
        }
        
        byte[] newImage = Images.toTEX(image);
        if (newImage != null) {
            ResourceSystem.replace(entry, newImage);
            return;
        }

        System.out.println("Could not replace texture.");
    }

    public static void replaceDecompressed() {
        FileEntry entry = ResourceSystem.getSelected().getEntry();

        File file = FileChooser.openFile(ResourceSystem.getSelected().getName(), null, false);
        if (file == null) return;
        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data == null) {
            System.err.println("Failed to read data!");
            return;
        }

        byte[] original = ResourceSystem.extract(entry);
        if (original == null) {
            System.out.println("Couldn't find entry, can't replace.");
            return;
        }

        byte[] output = new Resource(original).compress(data);
        if (output == null) {
            System.err.println("Error occurred when compressing data.");
            return;
        }

        ResourceSystem.replace(entry, output);
        System.out.println("Data compressed and added!");
    }

    public static void replaceCompressed() {
        File file = FileChooser.openFile(ResourceSystem.getSelected().getName(), null, false);
        if (file == null) return;
        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data != null) 
            ResourceSystem.replace(ResourceSystem.getSelected().getEntry(), data);
    }
}