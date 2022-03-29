package ennuo.toolkit.utilities.services;

import ennuo.craftworld.resources.Pack;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.FileEntry;
import javax.swing.JTree;

public class PackService implements ResourceService  {
    public static final int[] HEADERS = { 0x50434b62 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        Pack pack = entry.getResource("pack");
        if (pack == null) {
            try { 
                pack = new Serializer(new Resource(data).handle).struct(null, Pack.class);
                entry.setResource("pack", pack); 
            }
            catch (Exception e) { System.err.println("There was an error processing RPack file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
