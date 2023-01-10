package toolkit.functions;

import cwlib.util.FileIO;
import cwlib.util.Images;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;
import toolkit.windows.bundlers.TextureImporter;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.types.databases.FileEntry;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JOptionPane;

public class ReplacementCallbacks {
    public static void replaceImage() {
        FileEntry entry = ResourceSystem.getSelected().getEntry();

        byte[] texture = null;
        try { texture = TextureImporter.getTexture(); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Texture failed to convert!", "Texture Importer", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (texture == null) return;

        ResourceSystem.replace(entry, texture);
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