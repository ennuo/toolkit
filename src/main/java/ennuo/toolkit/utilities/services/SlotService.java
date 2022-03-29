package ennuo.toolkit.utilities.services;

import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.SlotList;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.FileEntry;
import java.util.ArrayList;
import javax.swing.JTree;

public class SlotService implements ResourceService  {
    public static final int[] HEADERS = { 0x534c5462 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        SlotList slots = entry.getResource("slots");
        if (slots == null) {
            try { 
                slots = new Serializer(new Resource(data).handle).struct(null, SlotList.class);
                entry.setResource("slots", slots); 
            }
            catch (Exception e) { System.err.println("There was an error processing RSlotList file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
