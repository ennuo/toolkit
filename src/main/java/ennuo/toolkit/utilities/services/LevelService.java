package ennuo.toolkit.utilities.services;

import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.windows.Toolkit;
import javax.swing.JTree;

public class LevelService implements ResourceService  {
    public static final int[] HEADERS = { 0x4c564c62 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        Slot slot = entry.getResource("slot");
        if (slot != null) {
            if (slot.renderedIcon == null)
                slot.renderIcon(entry);
            Toolkit.instance.setImage(slot.renderedIcon);
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
