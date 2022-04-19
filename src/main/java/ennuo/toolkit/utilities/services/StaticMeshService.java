package ennuo.toolkit.utilities.services;

import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.StaticMesh;
import ennuo.craftworld.types.FileEntry;
import javax.swing.JTree;

public class StaticMeshService implements ResourceService  {
    public static final int[] HEADERS = { 0x534d4862 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        StaticMesh mesh = entry.getResource("staticMesh");
        if (mesh == null) {
            try { mesh = new StaticMesh(new Resource(data)); entry.setResource("staticMesh", mesh); }
            catch (Exception e) { System.err.println("There was an error attempting to process RStaticMesh file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
