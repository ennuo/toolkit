package ennuo.toolkit.utilities.services;

import ennuo.craftworld.resources.Mesh;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.FileEntry;
import java.nio.file.Paths;
import javax.swing.JTree;

public class MeshService implements ResourceService  {
    public static final int[] HEADERS = { 0x4d534862 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        Mesh mesh = entry.getResource("mesh");
        if (mesh == null) {
            String fileName = Paths.get(entry.path).getFileName().toString();
            String name = fileName.replaceFirst("[.][^.]+$", "");
            try { mesh = new Mesh(name, new Resource(data)); entry.setResource("mesh", mesh); }
            catch (Exception e) { System.err.println("There was an error attempting to process RMesh file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
