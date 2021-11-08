package ennuo.toolkit.utilities.services;

import ennuo.craftworld.types.FileEntry;
import javax.swing.JTree;

public interface ResourceService {
    public void process(JTree tree, FileEntry entry, byte[] data);
    public int[] getSupportedHeaders();
}
