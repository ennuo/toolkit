package toolkit.functions;

import cwlib.util.FileIO;
import cwlib.util.Images;
import toolkit.utilities.FileChooser;
import toolkit.utilities.ResourceSystem;
import toolkit.windows.Toolkit;
import cwlib.types.Resource;
import cwlib.io.streams.MemoryInputStream;
import cwlib.types.databases.FileEntry;

import java.awt.image.BufferedImage;
import java.io.File;

public class ReplacementCallbacks {
    public static void replaceImage() {
        FileEntry entry = ResourceSystem.lastSelected.entry;
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
            ResourceSystem.replaceEntry(entry, newImage);
            return;
        }

        System.out.println("Could not replace texture.");
    }

    public static void replaceDecompressed() {
        File file = FileChooser.openFile(ResourceSystem.lastSelected.header, null, false);
        if (file == null) return;
        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data != null) {
            byte[] original = ResourceSystem.lastSelected.entry.data;
            if (original == null) ResourceSystem.extractFile(ResourceSystem.lastSelected.entry.GUID);
            if (original == null) original = ResourceSystem.extractFile(ResourceSystem.lastSelected.entry.hash);
            if (original == null) {
                System.out.println("Couldn't find entry, can't replace.");
                return;
            }
            Resource resource = new Resource(original);
            resource.handle.setData(data);
            byte[] out = resource.compressToResource();
            if (out == null) {
                System.err.println("Error occurred when compressing data.");
                return;
            }
            ResourceSystem.replaceEntry(ResourceSystem.lastSelected.entry, out);
            System.out.println("Data compressed and added!");
        }
    }

    public static void replaceCompressed() {
        File file = FileChooser.openFile(ResourceSystem.lastSelected.header, null, false);
        if (file == null) return;
        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data != null) ResourceSystem.replaceEntry(ResourceSystem.lastSelected.entry, data);
    }

}