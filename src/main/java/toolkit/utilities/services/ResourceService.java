package toolkit.utilities.services;

import cwlib.types.databases.FileEntry;
import javax.swing.JTree;

public interface ResourceService {
    public void process(JTree tree, FileEntry entry, byte[] data);
    public int[] getSupportedHeaders();
}
