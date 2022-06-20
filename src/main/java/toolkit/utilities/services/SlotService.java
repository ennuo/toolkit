package toolkit.utilities.services;

import cwlib.types.Resource;
import cwlib.resources.RSlotList;
import cwlib.structs.slot.Slot;
import cwlib.io.serializer.Serializer;
import cwlib.types.databases.FileEntry;
import java.util.ArrayList;
import javax.swing.JTree;

public class SlotService implements ResourceService  {
    public static final int[] HEADERS = { 0x534c5462 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        RSlotList slots = entry.getResource("slots");
        if (slots == null) {
            try { 
                slots = new Serializer(new Resource(data).handle).struct(null, RSlotList.class);
                entry.setResource("slots", slots); 
            }
            catch (Exception e) { System.err.println("There was an error processing RSlotList file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
