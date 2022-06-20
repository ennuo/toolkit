package toolkit.utilities.services;

import cwlib.resources.RPacks;
import cwlib.types.Resource;
import cwlib.io.serializer.Serializer;
import cwlib.types.databases.FileEntry;
import javax.swing.JTree;

public class PackService implements ResourceService  {
    public static final int[] HEADERS = { 0x50434b62 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        RPacks pack = entry.getResource("pack");
        if (pack == null) {
            try { 
                pack = new Serializer(new Resource(data).handle).struct(null, RPacks.class);
                entry.setResource("pack", pack); 
            }
            catch (Exception e) { System.err.println("There was an error processing RPack file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
