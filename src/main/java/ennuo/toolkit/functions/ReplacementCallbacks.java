package ennuo.toolkit.functions;

import ennuo.craftworld.memory.Compressor;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.memory.Images;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.resources.enums.Metadata;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.windows.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;

public class ReplacementCallbacks {
    public static void replaceImage() {
        FileEntry entry = Globals.lastSelected.entry;

        File file = Toolkit.instance.fileChooser.openFile("image.png", "png", "Portable Network Graphics (PNG)", false);
        if (file == null) return;

        BufferedImage image;
        if (file.getAbsolutePath().toLowerCase().endsWith(".dds"))
            image = Images.fromDDS(FileIO.read(file.getAbsolutePath()));
        else image = FileIO.readBufferedImage(file.getAbsolutePath());

        if (image == null) {
            System.err.println("Image was null, cancelling replacement operation.");
            return;
        }

        Resource oldImage = new Resource(Globals.extractFile(entry.SHA1));

        Resource newImage = null;
        if (oldImage.type == Metadata.CompressionType.GTF_TEXTURE)
            newImage = Images.toGTF(image);
        else if ((oldImage.type == null && oldImage.data == null) || oldImage.type == Metadata.CompressionType.LEGACY_TEXTURE)
            newImage = Images.toTEX(image);

        if (newImage != null) {
            Globals.replaceEntry(entry, newImage.data);
            return;
        }

        System.out.println("Could not replace texture.");
    }

    public static void replaceDecompressed() {
        File file = Toolkit.instance.fileChooser.openFile(Globals.lastSelected.header, "", "Resource", false);
        if (file == null) return;
        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data != null) {
            byte[] original = Globals.extractFile(Globals.lastSelected.entry.GUID);
            if (original == null) original = Globals.extractFile(Globals.lastSelected.entry.SHA1);
            if (original == null) {
                System.out.println("Couldn't find entry, can't replace.");
                return;
            }
            Resource resource = new Resource(original);
            resource.getDependencies(Globals.lastSelected.entry);
            byte[] out = Compressor.Compress(data, resource.magic, resource.revision, resource.resources);
            if (out == null) {
                System.err.println("Error occurred when compressing data.");
                return;
            }
            Globals.replaceEntry(Globals.lastSelected.entry, out);
            System.out.println("Data compressed and added!");
        }
    }

    public static void replaceCompressed() {
        File file = Toolkit.instance.fileChooser.openFile(Globals.lastSelected.header, "", "Resource", false);
        if (file == null) return;
        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data != null) Globals.replaceEntry(Globals.lastSelected.entry, data);
    }

}