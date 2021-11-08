package ennuo.toolkit.utilities.services;

import ennuo.craftworld.resources.GfxMaterial;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.FileEntry;
import javax.swing.JTree;

public class GfxMaterialService implements ResourceService  {
    public static final int[] HEADERS = { 0x474d5462 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        GfxMaterial gfxMaterial = entry.getResource("gfxMaterial");
        if (gfxMaterial == null) {
            Resource resource = new Resource(data);
            resource.decompress(true);
            try { gfxMaterial = new GfxMaterial(resource); entry.setResource("gfxMaterial", gfxMaterial); }
            catch (Exception e) { System.err.println("There was an error processing RGfxMaterial file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
