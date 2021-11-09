package ennuo.toolkit.functions;

import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.utilities.Images;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.serializer.Data;
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
        
        byte[] data = entry.data;
        if (data == null)
            data = Globals.extractFile(entry.SHA1);
        
        byte[] newImage = null;
        if (data != null) {
            Data oldImage = new Data(Globals.extractFile(entry.SHA1));
            String magic = oldImage.str(3);
            if (magic == "GTF")
                newImage = Images.toGTF(image);
            else if (magic == "TEX")
                newImage = Images.toTEX(image);
        } else newImage = Images.toTEX(image);

        if (newImage != null) {
            Globals.replaceEntry(entry, newImage);
            return;
        }

        System.out.println("Could not replace texture.");
    }

    public static void replaceDecompressed() {
        File file = Toolkit.instance.fileChooser.openFile(Globals.lastSelected.header, "", "Resource", false);
        if (file == null) return;
        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data != null) {
            byte[] original = Globals.lastSelected.entry.data;
            if (original == null) Globals.extractFile(Globals.lastSelected.entry.GUID);
            if (original == null) original = Globals.extractFile(Globals.lastSelected.entry.SHA1);
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