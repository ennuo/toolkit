package toolkit.utilities.services;

import cwlib.types.databases.FileEntry;

import javax.swing.*;

public interface ResourceService
{
    void process(JTree tree, FileEntry entry, byte[] data);

    int[] getSupportedHeaders();
}
