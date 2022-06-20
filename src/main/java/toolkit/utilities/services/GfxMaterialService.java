package toolkit.utilities.services;

import cwlib.resources.RGfxMaterial;
import cwlib.types.Resource;
import cwlib.types.databases.FileEntry;
import javax.swing.JTree;

public class GfxMaterialService implements ResourceService  {
    public static final int[] HEADERS = { 0x474d5462 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        RGfxMaterial gfxMaterial = entry.getResource("gfxMaterial");
        if (gfxMaterial == null) {
            try { gfxMaterial = new RGfxMaterial(new Resource(data)); entry.setResource("gfxMaterial", gfxMaterial); }
            catch (Exception e) { System.err.println("There was an error processing RGfxMaterial file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
