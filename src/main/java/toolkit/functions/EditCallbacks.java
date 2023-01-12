package toolkit.functions;

import cwlib.enums.DatabaseType;
import cwlib.enums.ResourceType;
import cwlib.resources.RAdventureCreateProfile;
import cwlib.resources.RPacks;
import cwlib.resources.RPlan;
import cwlib.resources.RSlotList;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.slot.Slot;
import cwlib.types.data.ResourceInfo;
import cwlib.types.databases.FileEntry;
import cwlib.types.save.BigSave;
import cwlib.types.save.SaveEntry;
import java.awt.event.ActionEvent;
import toolkit.windows.managers.ItemManager;
import toolkit.windows.managers.SlotManager;

public class EditCallbacks {
    public static void editSlot(ActionEvent event) {
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        ResourceInfo info = entry.getInfo();
        if (info == null  || info.getType() == ResourceType.INVALID || info.getResource() == null) return;


        if (info.getType() == ResourceType.ADVENTURE_CREATE_PROFILE) {
            new SlotManager(entry, (RAdventureCreateProfile) info.getResource()).setVisible(true);
            return;
        }
        
        if (ResourceSystem.getDatabaseType() == DatabaseType.BIGFART) {
            Slot slot = ((SaveEntry)entry).getSlot();
            if (slot == null) return;
            new SlotManager((BigSave)entry.getSource(), slot).setVisible(true);
            return;
        }

        if (info.getType() == ResourceType.SLOT_LIST) 
            new SlotManager(entry, (RSlotList) info.getResource()).setVisible(true);
        else if (info.getType() == ResourceType.PACKS)
            new SlotManager(entry, (RPacks) info.getResource()).setVisible(true);
    }
    
    public static void editItem(ActionEvent event) {
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        RPlan plan = entry.getInfo().getResource();
        if (plan == null) return;
        ItemManager manager = new ItemManager(entry, plan);
        manager.setVisible(true);
    }
}
