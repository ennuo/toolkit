package toolkit.windows.managers;

import cwlib.resources.RAdventureCreateProfile;
import cwlib.resources.RPacks;
import cwlib.types.Resource;
import cwlib.resources.RSlotList;
import cwlib.resources.RTranslationTable;
import cwlib.singleton.ResourceSystem;
import cwlib.enums.ContentsType;
import cwlib.enums.Crater;
import cwlib.enums.GameMode;
import cwlib.enums.LevelType;
import cwlib.enums.ResourceType;
import cwlib.enums.SlotType;
import cwlib.structs.slot.Pack;
import cwlib.structs.slot.Slot;
import cwlib.structs.slot.SlotID;
import cwlib.io.Compressable;
import cwlib.resources.RTexture;
import cwlib.types.databases.FileEntry;
import cwlib.types.save.BigSave;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.ResourceInfo;
import cwlib.types.data.Revision;
import cwlib.util.Images;
import cwlib.util.Resources;
import cwlib.util.Strings;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import toolkit.windows.Toolkit;
import toolkit.windows.utilities.ResourcePicker;

public class SlotManager extends javax.swing.JFrame {
    private static Slot EMPTY_SLOT;
    static {
        EMPTY_SLOT = new Slot();
        EMPTY_SLOT.id = new SlotID(SlotType.DEVELOPER, 0);
        EMPTY_SLOT.name = "None";
    }
    
    private enum EditorType {
        SAVE,
        PACK,
        SLOT_LIST,
        ADVENTURE
    }
    
    
    /**
     * Used for getting random slot location.
     */
    private static final Random RNG = new Random();
    
    /**
     * Flag to stop action events from triggering when first
     * loading each slot's information.
     */
    private boolean canUpdate = false;
    
    /**
     * The version of the game the files come from.
     * LBP1/LBP3 mainly, for the moon crater locations.
     */
    private int game = 1;
    
    /**
     * The type of file we're editing.
     */
    private EditorType type;
    
    private class SlotEntry {
        private SlotID id;
        private Slot slot;
        
        public SlotEntry(Slot slot) {
            this.id = slot.id;
            this.slot = slot;
        }
        
        @Override public String toString() { return this.slot.toString(); }
        @Override public int hashCode() { return this.id.hashCode(); }
        @Override public boolean equals(Object other) {
            if (other == this) return true;
            if (other instanceof SlotID)
                return ((SlotID)other).equals(this.id);
            if (!(other instanceof SlotEntry)) return false;
            SlotEntry o = (SlotEntry) other;
            return o.id.equals(this.id);
        }
    }
    
    private FileEntry entry;
    
    public ArrayList<Slot> slots;
    public ArrayList<Pack> packs;
    private RAdventureCreateProfile adventure;
    
    private Pack selectedItem;
    private Slot selectedSlot;
    
    private final DefaultListModel model = new DefaultListModel();
    private final DefaultComboBoxModel<SlotEntry> groups = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<SlotEntry> links = new DefaultComboBoxModel<>(); 
    
    public SlotManager(BigSave profile, Slot selectedSlot) {
        this.type = EditorType.SAVE;

        this.entry = null;
        this.game = profile.getArchive().getGameRevision().getSubVersion() > 0x105 ? 3 : 1;

        this.slots = new ArrayList<>(profile.getProfile().myMoonSlots.values());
        
        this.setup();
        
        this.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { 
                onCloseProfile(profile);
            }
        });
        
        this.closeButton.addActionListener(l -> {
            onCloseProfile(profile);
        });
        
        if (selectedSlot != null)
            this.slotList.setSelectedValue(selectedSlot, true);
    }
    
    public SlotManager(FileEntry entry, RSlotList slotList) {
        this.type = EditorType.SLOT_LIST;
        this.slots = new ArrayList<>(slotList.getSlots());
        this.entry = entry;
        
        this.setup();
        
        this.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onClose(); }
        });
        this.closeButton.addActionListener(l -> this.onClose());
    }
    
    public SlotManager(FileEntry entry, RAdventureCreateProfile profile) {
        this.type = EditorType.ADVENTURE;
        this.slots = new ArrayList<>(profile.getAdventureSlots().values());
        this.entry = entry;
        this.adventure = profile;
            
        this.setup();
        
        this.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onClose(); }
        });
        this.closeButton.addActionListener(l -> this.onClose());
    }
    
    public SlotManager(FileEntry entry, RPacks pack) {
        this.type = EditorType.PACK;
        
        this.slots = new ArrayList<>(pack.getPacks().size());
        this.packs = new ArrayList<>(pack.getPacks().size());
        for (Pack item : pack) {
            this.packs.add(item);
            this.slots.add(item.slot);
        }
        
        this.entry = entry;
        
        this.setup();
        
        this.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onClose(); }
        });
        this.closeButton.addActionListener(l -> this.onClose());
    }
    
    private void onCloseProfile(BigSave profile) {
        // We need to rebuild the HashMap since slots may have been added/removed
        HashMap<SlotID, Slot> slotMap = new HashMap<>(this.slots.size());
        for (Slot slot : this.slots)
            slotMap.put(slot.id, slot);
        profile.getProfile().myMoonSlots = slotMap;
        
        profile.setHasChanges();
        Toolkit.INSTANCE.updateWorkspace();
        this.dispose();
    }
    
    private void onClose() {
        int result = JOptionPane.showConfirmDialog(null, "Do you want to save your changes?", "Pending changes", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            ResourceInfo info = this.entry.getInfo();
            Compressable compressable = null;
            if (this.type == EditorType.PACK) compressable = new RPacks(this.packs);
            else if (this.type == EditorType.ADVENTURE) {
                HashMap<SlotID, Slot> slotMap = adventure.getAdventureSlots();
                slotMap.clear();
                for (Slot slot : this.slots)
                    slotMap.put(slot.id, slot);
                compressable = adventure;
            } else if (this.type == EditorType.SLOT_LIST) compressable = new RSlotList(this.slots);
            ResourceSystem.replace(this.entry, Resource.compress(compressable, info.getRevision(), info.getCompressionFlags()));
        }
        this.dispose();
    }
    
    private void setup() {
        this.initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        this.setResizable(false);
        
        if (this.entry != null)
            this.game = this.entry.getInfo().getRevision().getSubVersion() > 0x105 ? 3 : 1;
        
        this.slotList.setModel(this.model);
        this.groupCombo.setModel(this.groups);
        this.linkCombo.setModel(this.links);
        
        if (!(this.type == EditorType.PACK))
            this.slotSettings.remove(3);
        
        for (Slot slot : this.slots)
            this.model.addElement(slot);
        
        if (this.model.size() == 0) {
            this.removeSlotButton.setEnabled(false);
            this.slotSettings.setSelectedIndex(-1);
            this.slotSettings.setEnabled(false);
        }
        
        this.setupGroups();
        this.setupLinks();
        
        this.setupDetailsListeners();
        this.setupDataListeners();
        this.setupSettingsListeners();
        if (this.type == EditorType.PACK)
            this.setupDLCListeners();
        
        this.slotList.addListSelectionListener(listener -> {
            int index = this.slotList.getSelectedIndex();
            if (index == -1) return;
            if (this.type == EditorType.PACK) 
                this.selectedItem = this.packs.get(index);
            this.selectedSlot = (Slot) this.model.getElementAt(index);
            this.setSlotData();
        });
        
        this.addSlotButton.addActionListener(e -> {
            if (this.slots.size() == 0) {
                this.removeSlotButton.setEnabled(true);
                this.slotSettings.setEnabled(true);
                this.slotSettings.setSelectedIndex(0);
            }
            
            Slot slot = new Slot();
            
            if (this.type == EditorType.PACK) {
                slot.id.slotType = SlotType.DLC_PACK;
                
                Pack item = new Pack();
                item.contentsType = ContentsType.PLANS;
                item.mesh = ContentsType.PLANS.getBadgeMesh();
                
                item.slot = slot;
                item.slot.root = null;
                
                this.packs.add(item);
            }
            else if (this.type == EditorType.SAVE)
                slot.id.slotType = SlotType.USER_CREATED_STORED_LOCAL;
            else if (this.type == EditorType.ADVENTURE)
                slot.id.slotType = SlotType.ADVENTURE_AREA_LEVEL;
            else
                slot.id.slotType = SlotType.DEVELOPER;
            
            slot.id.slotNumber = this.getNextAvailableSlot(slot.id.slotType);
            
            if (this.type == EditorType.SAVE)
                slot.location = Crater.valueOf("SLOT_" + (slot.id.slotNumber % 82) + "_LBP" + this.game).getValue();
            else
                slot.location = Crater.valueOf("SLOT_" + SlotManager.RNG.nextInt(82) + "_LBP" + this.game).getValue();

            this.slots.add(slot);
            this.model.addElement(slot);
            this.slotList.setSelectedValue(slot, true);
            
            this.setupLinks();
            this.setupGroups();
        });
        
        this.removeSlotButton.addActionListener(e -> {
            int index = this.slotList.getSelectedIndex();
            if (this.model.size() - 1 != 0) {
                if (index == 0)
                    this.slotList.setSelectedIndex(index + 1);
                else
                    this.slotList.setSelectedIndex(index - 1);   
            } else {
                this.removeSlotButton.setEnabled(false);
                this.slotSettings.setSelectedIndex(-1);
                this.slotSettings.setEnabled(false);
            }
            
            this.model.remove(index);
            this.slots.remove(index);
            if (this.type == EditorType.PACK)
                this.packs.remove(index);
            
            this.setupLinks();
            this.setupGroups();
        });
        
        this.slotList.setSelectedIndex(0);
    }
    
    private long getNextAvailableSlot(SlotType type) {
        ArrayList<Long> IDs = new ArrayList<>(this.slots.size());
        for (Slot slot : this.slots)
            IDs.add(slot.id.slotNumber);
        long ID = 0;
        if (!type.equals(SlotType.USER_CREATED_STORED_LOCAL)) {
            IDs.sort((Long a, Long b) -> (int)(b - a));
            ID = IDs.get(IDs.size() - 1);
        }
        while (IDs.contains(ID)) ID++;
        return ID;
    }
    
    private SlotEntry getGroup(SlotID id) {
        for (int i = 0; i < this.groups.getSize(); ++i) {
            SlotEntry entry = this.groups.getElementAt(i);
            if (entry.equals(id)) return entry;
        }
        return new SlotEntry(EMPTY_SLOT);
    }
    
    private SlotEntry getLink(SlotID id) {
        for (int i = 0; i < this.links.getSize(); ++i) {
            SlotEntry entry = this.links.getElementAt(i);
            if (entry.equals(id)) return entry;
        }
        return new SlotEntry(EMPTY_SLOT);
    }
    
    private void setupGroups() {
        this.groups.removeAllElements();
        this.groups.addElement(new SlotEntry(EMPTY_SLOT));
        for (Slot slot : this.slots) {
            SlotType type = slot.id.slotType;
            if (type.isGroup())
                this.groups.addElement(new SlotEntry(slot));
        }
    }
    
    private void setupLinks() {
        this.links.removeAllElements();
        this.links.addElement(new SlotEntry(EMPTY_SLOT));
        for (Slot slot : this.slots) {
            SlotType type = slot.id.slotType;
            if (type.isLink())
                this.links.addElement(new SlotEntry(slot));
        }
    }
    
    private void setupSettingsListeners() {
        this.levelTypeCombo.addActionListener(e -> this.selectedSlot.developerLevelType = (LevelType) this.levelTypeCombo.getSelectedItem());
        this.gameModeCombo.addActionListener(e -> this.selectedSlot.gameMode = ((GameMode) this.gameModeCombo.getSelectedItem()).getValue());
        
        this.badgeSizeSpinner.addChangeListener(e -> this.selectedSlot.customBadgeSize = (byte) this.badgeSizeSpinner.getValue());
        
        this.minPlayerSpinner.addChangeListener(e -> this.selectedSlot.minPlayers = (byte) this.minPlayerSpinner.getValue());
        this.maxPlayerSpinner.addChangeListener(e -> this.selectedSlot.maxPlayers = (byte) this.maxPlayerSpinner.getValue());
        this.enforcePlayerCheckbox.addChangeListener(e -> this.selectedSlot.enforceMinMaxPlayers = this.enforcePlayerCheckbox.isSelected());
        
        this.planetDecorationTextEntry.addActionListener(e -> {
            String descriptor = this.planetDecorationTextEntry.getText();
            if (descriptor.isEmpty()) {
                this.selectedSlot.planetDecorations = null;
                return;
            }
            if (Strings.isGUID(descriptor) || Strings.isSHA1(descriptor))
                this.selectedSlot.planetDecorations = new ResourceDescriptor(descriptor, ResourceType.LEVEL);
        });
        this.backgroundGUIDTextEntry.addChangeListener(e -> {
            long value = (long) this.backgroundGUIDTextEntry.getValue();
            if (value == 0) this.selectedSlot.backgroundGUID = null;
            else this.selectedSlot.backgroundGUID = new GUID(value);
        });
        
        this.locationXSpinner.addChangeListener(e -> this.selectedSlot.location.x = (float) this.locationXSpinner.getValue());
        this.locationYSpinner.addChangeListener(e -> this.selectedSlot.location.y = (float) this.locationYSpinner.getValue());
        this.locationZSpinner.addChangeListener(e -> this.selectedSlot.location.z = (float) this.locationZSpinner.getValue());
        this.locationWSpinner.addChangeListener(e -> this.selectedSlot.location.w = (float) this.locationWSpinner.getValue());
        
        this.lockedCheckbox.addChangeListener(e -> this.selectedSlot.initiallyLocked = this.lockedCheckbox.isSelected());
        this.subLevelCheckbox.addChangeListener(e -> this.selectedSlot.isSubLevel = this.subLevelCheckbox.isSelected());
        this.moveCheckbox.addChangeListener(e -> this.selectedSlot.moveRecommended = this.moveCheckbox.isSelected());
        this.shareableCheckbox.addChangeListener(e -> this.selectedSlot.shareable = this.shareableCheckbox.isSelected());
        this.gameKitCheckbox.addChangeListener(e -> this.selectedSlot.isGameKit = this.gameKitCheckbox.isSelected());
        this.visibleCheckbox.addChangeListener(e -> this.selectedSlot.showOnPlanet = this.visibleCheckbox.isSelected());
        this.crossControllerCheckbox.addChangeListener(e -> this.selectedSlot.crossCompatible = this.crossControllerCheckbox.isSelected());
    }
    
    private void setupDetailsListeners() {
        this.titleTextEntry.addActionListener(e -> {
            this.selectedSlot.name = this.titleTextEntry.getText();
            this.slotList.repaint();
        });
        
        this.descriptionTextEntry.addKeyListener(new KeyListener() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    selectedSlot.description = descriptionTextEntry.getText();
            }
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyReleased(KeyEvent e) { return; }
        });
        
        
        this.iconTextEntry.addActionListener(e -> {
            String text = this.iconTextEntry.getText();
            if (text.isEmpty()) {
                this.selectedSlot.icon = null;
                this.updateIcon();
                return;
            }
            if (Strings.isGUID(text) || Strings.isSHA1(text)) {
                ResourceDescriptor descriptor = new ResourceDescriptor(text, ResourceType.TEXTURE);
                if (descriptor.equals(this.selectedSlot.icon)) return;
                this.selectedSlot.icon = descriptor;
                this.updateIcon();
            }
        });
        
        this.creatorTextEntry.addActionListener(e -> this.selectedSlot.authorName = this.creatorTextEntry.getText());
        this.translationKeyTextEntry.addActionListener(e -> {
            this.selectedSlot.translationTag = this.translationKeyTextEntry.getText();
            this.updateTranslations();
            this.slotList.repaint();
        });
                
    }
    
    private void setupDataListeners() {
        this.rootLevelTextEntry.addActionListener(e -> {
            String descriptor = this.rootLevelTextEntry.getText();
            if (descriptor == null || descriptor.isEmpty()) {
                this.selectedSlot.root = null;
                return;
            }
            if (Strings.isGUID(descriptor) || Strings.isSHA1(descriptor))
                this.selectedSlot.root = new ResourceDescriptor(descriptor, ResourceType.LEVEL);
        });
        
        this.adventureTextEntry.addActionListener(e -> {
            String descriptor = this.adventureTextEntry.getText();
            if (descriptor == null || descriptor.isEmpty()) {
                this.selectedSlot.adventure = null;
                return;
            }
            if (Strings.isGUID(descriptor) || Strings.isSHA1(descriptor))
                this.selectedSlot.adventure = new ResourceDescriptor(descriptor, ResourceType.ADVENTURE_CREATE_PROFILE);
        });
        
        this.slotTypeCombo.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED || !this.canUpdate) return;
            
            SlotType oldType = this.selectedSlot.id.slotType;
            this.selectedSlot.id.slotType = (SlotType) this.slotTypeCombo.getSelectedItem();
            SlotType newType = this.selectedSlot.id.slotType;
            
            this.canUpdate = false;
            
            // Reset the link/group collections if we modify it.
            if ((!oldType.isGroup() && newType.isGroup()) || (oldType.isGroup() && !newType.isGroup()))
                this.setupGroups();
            if ((!oldType.isLink() && newType.isLink()) || (oldType.isLink() && !newType.isLink()))
                this.setupLinks();
            
            this.groupCombo.setSelectedItem(this.getGroup(this.selectedSlot.group));
            this.linkCombo.setSelectedItem(this.getLink(this.selectedSlot.primaryLinkLevel));
            
            this.canUpdate = true;
            
        });
        
        this.slotNumberSpinner.addChangeListener(e -> {
            SlotID oldSlotID = new SlotID(this.selectedSlot.id.slotType, this.selectedSlot.id.slotNumber);
            this.selectedSlot.id.slotNumber = (long) this.slotNumberSpinner.getValue();
            SlotID newSlotID = this.selectedSlot.id;
            for (Slot slot : this.slots) {
                if (slot.group.equals(oldSlotID))
                    slot.group.slotNumber = newSlotID.slotNumber;
                else if (slot.primaryLinkLevel.equals(oldSlotID))
                    slot.primaryLinkLevel.slotNumber = newSlotID.slotNumber;
            }
        });
        
        
        this.groupCombo.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED || !this.canUpdate) return;
            SlotEntry entry = (SlotEntry) this.groupCombo.getSelectedItem();
            this.selectedSlot.group.slotNumber = entry.id.slotNumber;
            this.selectedSlot.group.slotType = entry.id.slotType;
        });
        
        this.linkCombo.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED || !this.canUpdate) return;
            SlotEntry entry = (SlotEntry) this.linkCombo.getSelectedItem();
            this.selectedSlot.primaryLinkLevel.slotNumber = entry.id.slotNumber;
            this.selectedSlot.primaryLinkLevel.slotType = entry.id.slotType;
        });
    }
    
    private void setupDLCListeners() {
        this.contentsTypeCombo.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED || !this.canUpdate) return;
            
            ContentsType type = (ContentsType) this.contentsTypeCombo.getSelectedItem();
            this.selectedItem.contentsType = type;
            
            // Use the default badge mesh for specified type.
            
            ResourceDescriptor badgeMesh = type.getBadgeMesh();
            this.selectedItem.mesh = badgeMesh;
            this.badgeMeshTextEntry.setText(badgeMesh.toString());
        });
        
        this.badgeMeshTextEntry.addActionListener(e -> {
            String descriptor = this.badgeMeshTextEntry.getText();
            if (descriptor == null || descriptor.isEmpty()) {
                this.selectedItem.mesh = null;
                return;
            }
            if (Strings.isGUID(descriptor) || Strings.isSHA1(descriptor))
                this.selectedItem.mesh = new ResourceDescriptor(descriptor, ResourceType.MESH);
        });
        
        this.contentIDTextEntry.addActionListener(e -> this.selectedItem.contentID = this.contentIDTextEntry.getText());
        
        this.timestampSpinner.addChangeListener(e -> {
            long timestamp = ((Date)this.timestampSpinner.getValue()).getTime() / 1000l;
            this.selectedItem.timestamp = timestamp;
        });
    }
    
    private void updateTranslations() {
        String tag = this.selectedSlot.translationTag;
        if (tag == null || tag.isEmpty()) {
            this.titleTextEntry.setEnabled(true);
            this.descriptionTextEntry.setEnabled(true);
            if (selectedSlot.name == null || selectedSlot.name.isEmpty())
                this.titleTextEntry.setText("Unnamed Level");
            else 
                this.titleTextEntry.setText(selectedSlot.name);
            this.descriptionTextEntry.setText(selectedSlot.description);
        } else {
            this.titleTextEntry.setEnabled(false);
            this.descriptionTextEntry.setEnabled(false);
            this.titleTextEntry.setText(tag);
            this.descriptionTextEntry.setText("A valid translation table needs to be loaded for the title and description to appear. Alternatively, remove the translation key, and set your own title/description.");

            RTranslationTable LAMS = ResourceSystem.getLAMS();
            if (LAMS != null) {
                this.titleTextEntry.setText(LAMS.translate(tag + "_NAME"));
                this.descriptionTextEntry.setText(LAMS.translate(tag + "_DESC"));   
            }
        }
    }
    
    private void resetIcon() {
         this.slotIcon.setIcon(null);
         this.slotIcon.setText("No icon available.");
    }
    
    private void updateIcon() {
        Slot slot = this.selectedSlot;
        
        byte[] data = ResourceSystem.extract(slot.icon);
        if (data == null) { this.resetIcon(); return; }
        
        ResourceType type = Resources.getResourceType(data);
        if (type != ResourceType.TEXTURE && type != ResourceType.GTF_TEXTURE) return;
        
        RTexture texture = null;
        try { texture = new RTexture(data); }
        catch (Exception ex) { this.resetIcon(); return; }
        
        BufferedImage image = texture.getImage();
        if (image == null) { this.resetIcon(); return; }
        
        ImageIcon icon = null;
        if (slot.id.slotType.equals(SlotType.DEVELOPER_GROUP) || slot.id.slotType.equals(SlotType.DLC_PACK))
            icon = Images.getGroupIcon(image);
        else if (slot.adventure != null) icon = Images.getAdventureIcon(image);
        else {
            Revision levelRevision = Resources.getRevision(ResourceSystem.extract(slot.root));
            if (levelRevision == null) {
                if (this.entry != null)
                    levelRevision = this.entry.getInfo().getRevision();
                else
                    levelRevision = new Revision(0x272);
            }
            
            icon = Images.getSlotIcon(image, levelRevision.getHead());
        }
        
        if (icon == null) { this.resetIcon(); return; }
        
        this.slotIcon.setText("");
        this.slotIcon.setIcon(icon);
    }
    
    private void setSlotData() {
        this.canUpdate = false;
        Slot slot = this.selectedSlot;
        
        this.titleTextEntry.setText(slot.name);
        this.descriptionTextEntry.setText(slot.description);

        
        if (slot.icon != null) 
            this.iconTextEntry.setText(slot.icon.toString());
        else this.iconTextEntry.setText("");
        this.updateIcon();
        
        this.creatorTextEntry.setText(slot.authorName);
        this.translationKeyTextEntry.setText(slot.translationTag);
        this.updateTranslations();

        /* Data page */
        
        if (slot.root != null) this.rootLevelTextEntry.setText(slot.root.toString());
        else this.rootLevelTextEntry.setText("");

        if (slot.adventure != null) this.adventureTextEntry.setText(slot.adventure.toString());
        else this.adventureTextEntry.setText("");

        this.slotTypeCombo.setSelectedItem(slot.id.slotType);
        this.slotNumberSpinner.setValue(slot.id.slotNumber);
        
        this.groupCombo.setSelectedItem(this.getGroup(slot.group));
        this.linkCombo.setSelectedItem(this.getLink(slot.primaryLinkLevel));

        this.levelTypeCombo.setSelectedItem(slot.developerLevelType);
        this.gameModeCombo.setSelectedItem(GameMode.fromValue(slot.gameMode));
        this.badgeSizeSpinner.setValue(slot.customBadgeSize);
        this.minPlayerSpinner.setValue(slot.minPlayers);
        this.maxPlayerSpinner.setValue(slot.maxPlayers);
        this.enforcePlayerCheckbox.setSelected(slot.enforceMinMaxPlayers);

        if (slot.planetDecorations != null)
            this.planetDecorationTextEntry.setText(slot.planetDecorations.toString());
        else
            this.planetDecorationTextEntry.setText("");

        if (slot.backgroundGUID == null) this.backgroundGUIDTextEntry.setValue(0l);
        else this.backgroundGUIDTextEntry.setValue(slot.backgroundGUID.getValue());

        this.locationXSpinner.setValue(slot.location.x);
        this.locationYSpinner.setValue(slot.location.y);
        this.locationZSpinner.setValue(slot.location.z);
        this.locationWSpinner.setValue(slot.location.w);

        this.lockedCheckbox.setSelected(slot.initiallyLocked);
        this.subLevelCheckbox.setSelected(slot.isSubLevel);
        this.moveCheckbox.setSelected(slot.moveRecommended);
        this.shareableCheckbox.setSelected(slot.shareable);
        this.gameKitCheckbox.setSelected(slot.isGameKit);
        this.visibleCheckbox.setSelected(slot.showOnPlanet);
        this.crossControllerCheckbox.setSelected(slot.crossCompatible);
        
        /* DLC page */
        
        if (this.type == EditorType.PACK) {
            Pack item = this.selectedItem;
            this.contentsTypeCombo.setSelectedItem(item.contentsType);
            if (item.mesh != null)
                this.badgeMeshTextEntry.setText(item.mesh.toString());
            else
                this.badgeMeshTextEntry.setText("");
            this.contentIDTextEntry.setText(item.contentID);
            this.timestampSpinner.setValue(new Date(item.timestamp * 1000));
        }
        
        this.canUpdate = true;
        
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        slotContainer = new javax.swing.JScrollPane();
        slotList = new javax.swing.JList<>();
        addSlotButton = new javax.swing.JButton();
        removeSlotButton = new javax.swing.JButton();
        slotSettings = new javax.swing.JTabbedPane();
        detailsPane = new javax.swing.JPanel();
        slotIcon = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        titleTextEntry = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionPane = new javax.swing.JScrollPane();
        descriptionTextEntry = new javax.swing.JTextArea();
        iconLabel = new javax.swing.JLabel();
        iconTextEntry = new javax.swing.JTextField();
        creatorLabel = new javax.swing.JLabel();
        creatorTextEntry = new javax.swing.JTextField();
        translationKeyLabel = new javax.swing.JLabel();
        translationKeyTextEntry = new javax.swing.JTextField();
        selectIconButon = new javax.swing.JButton();
        dataPane = new javax.swing.JPanel();
        dataContainer = new javax.swing.JPanel();
        rootLevelLabel = new javax.swing.JLabel();
        adventureLabel = new javax.swing.JLabel();
        rootLevelTextEntry = new javax.swing.JTextField();
        adventureTextEntry = new javax.swing.JTextField();
        slotIDPane = new javax.swing.JPanel();
        slotIDLabel = new javax.swing.JLabel();
        slotTypeLabel = new javax.swing.JLabel();
        slotTypeCombo = new javax.swing.JComboBox(SlotType.values());
        slotNumberLabel = new javax.swing.JLabel();
        slotNumberSpinner = new javax.swing.JSpinner();
        groupLabel = new javax.swing.JLabel();
        linkLabel = new javax.swing.JLabel();
        groupCombo = new javax.swing.JComboBox<>();
        linkCombo = new javax.swing.JComboBox<>();
        selectRootLevelButton = new javax.swing.JButton();
        selectAdventureButton = new javax.swing.JButton();
        settingsPane = new javax.swing.JPanel();
        levelTypeLabel = new javax.swing.JLabel();
        gameModeLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        badgeSizeLabel = new javax.swing.JLabel();
        flagsLabel = new javax.swing.JLabel();
        flagsPane = new javax.swing.JPanel();
        lockedCheckbox = new javax.swing.JCheckBox();
        subLevelCheckbox = new javax.swing.JCheckBox();
        moveCheckbox = new javax.swing.JCheckBox();
        crossControllerCheckbox = new javax.swing.JCheckBox();
        shareableCheckbox = new javax.swing.JCheckBox();
        gameKitCheckbox = new javax.swing.JCheckBox();
        visibleCheckbox = new javax.swing.JCheckBox();
        levelTypeCombo = new javax.swing.JComboBox(LevelType.values());
        gameModeCombo = new javax.swing.JComboBox(GameMode.values());
        minPlayerSpinner = new javax.swing.JSpinner();
        maxPlayerSpinner = new javax.swing.JSpinner();
        enforcePlayerCheckbox = new javax.swing.JCheckBox();
        badgeSizeSpinner = new javax.swing.JSpinner();
        planetDecorationLabel = new javax.swing.JLabel();
        planetDecorationTextEntry = new javax.swing.JTextField();
        backgroundGUIDLabel = new javax.swing.JLabel();
        backgroundGUIDTextEntry = new javax.swing.JSpinner();
        locationLabel = new javax.swing.JLabel();
        locationXSpinner = new javax.swing.JSpinner();
        locationYSpinner = new javax.swing.JSpinner();
        locationZSpinner = new javax.swing.JSpinner();
        locationWSpinner = new javax.swing.JSpinner();
        dlcPane = new javax.swing.JPanel();
        contentsTypeLabel = new javax.swing.JLabel();
        badgeMeshLabel = new javax.swing.JLabel();
        contentIDLabel = new javax.swing.JLabel();
        timestampLabel = new javax.swing.JLabel();
        contentsTypeCombo = new javax.swing.JComboBox(ContentsType.values());
        timestampSpinner = new javax.swing.JSpinner();
        badgeMeshTextEntry = new javax.swing.JTextField();
        contentIDTextEntry = new javax.swing.JTextField();
        closeButton = new javax.swing.JButton();
        slotsLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Slot Manager");

        slotList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        slotContainer.setViewportView(slotList);

        addSlotButton.setText("Add Slot");

        removeSlotButton.setText("Remove");

        slotIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        slotIcon.setText("No icon available.");

        titleLabel.setText("Title:");

        descriptionLabel.setText("Description:");

        descriptionTextEntry.setColumns(20);
        descriptionTextEntry.setLineWrap(true);
        descriptionTextEntry.setRows(5);
        descriptionTextEntry.setWrapStyleWord(true);
        descriptionPane.setViewportView(descriptionTextEntry);

        iconLabel.setText("Icon:");

        creatorLabel.setText("Creator:");

        translationKeyLabel.setText("Translation Key:");

        selectIconButon.setText("Select");
        selectIconButon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectIconButonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout detailsPaneLayout = new javax.swing.GroupLayout(detailsPane);
        detailsPane.setLayout(detailsPaneLayout);
        detailsPaneLayout.setHorizontalGroup(
            detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(detailsPaneLayout.createSequentialGroup()
                        .addComponent(slotIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(detailsPaneLayout.createSequentialGroup()
                                .addComponent(titleLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(titleTextEntry))
                            .addGroup(detailsPaneLayout.createSequentialGroup()
                                .addComponent(descriptionLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(descriptionPane, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)))
                    .addGroup(detailsPaneLayout.createSequentialGroup()
                        .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(translationKeyLabel)
                            .addComponent(creatorLabel)
                            .addComponent(iconLabel))
                        .addGap(12, 12, 12)
                        .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(creatorTextEntry, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(translationKeyTextEntry)
                            .addGroup(detailsPaneLayout.createSequentialGroup()
                                .addComponent(iconTextEntry)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selectIconButon, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        detailsPaneLayout.setVerticalGroup(
            detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(detailsPaneLayout.createSequentialGroup()
                        .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(titleLabel)
                            .addComponent(titleTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(descriptionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(descriptionPane, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(slotIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iconLabel)
                    .addComponent(iconTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectIconButon))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(creatorLabel)
                    .addComponent(creatorTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(translationKeyLabel)
                    .addComponent(translationKeyTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(54, Short.MAX_VALUE))
        );

        slotSettings.addTab("Details", detailsPane);

        rootLevelLabel.setText("Root Level:");

        adventureLabel.setText("Adventure:");

        slotIDLabel.setText("Slot ID:");

        slotTypeLabel.setText("Type:");

        slotNumberLabel.setText("Number:");

        slotNumberSpinner.setModel(new javax.swing.SpinnerNumberModel(0L, 0L, null, 1L));

        javax.swing.GroupLayout slotIDPaneLayout = new javax.swing.GroupLayout(slotIDPane);
        slotIDPane.setLayout(slotIDPaneLayout);
        slotIDPaneLayout.setHorizontalGroup(
            slotIDPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(slotIDPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(slotIDPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(slotIDLabel)
                    .addGroup(slotIDPaneLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(slotIDPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(slotNumberLabel)
                            .addComponent(slotTypeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(slotIDPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(slotTypeCombo, 0, 388, Short.MAX_VALUE)
                            .addComponent(slotNumberSpinner))))
                .addContainerGap())
        );
        slotIDPaneLayout.setVerticalGroup(
            slotIDPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(slotIDPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(slotIDLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(slotIDPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(slotTypeLabel)
                    .addComponent(slotTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(slotIDPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(slotNumberLabel)
                    .addComponent(slotNumberSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        groupLabel.setText("Group:");

        linkLabel.setText("Link:");

        selectRootLevelButton.setText("Select");
        selectRootLevelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRootLevelButtonActionPerformed(evt);
            }
        });

        selectAdventureButton.setText("Select");
        selectAdventureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAdventureButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dataContainerLayout = new javax.swing.GroupLayout(dataContainer);
        dataContainer.setLayout(dataContainerLayout);
        dataContainerLayout.setHorizontalGroup(
            dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(slotIDPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(dataContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dataContainerLayout.createSequentialGroup()
                        .addGroup(dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(rootLevelLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(adventureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(adventureTextEntry)
                            .addComponent(rootLevelTextEntry))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selectRootLevelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(selectAdventureButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(dataContainerLayout.createSequentialGroup()
                        .addGroup(dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(linkLabel)
                            .addComponent(groupLabel))
                        .addGap(18, 18, 18)
                        .addGroup(dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(linkCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(groupCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        dataContainerLayout.setVerticalGroup(
            dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rootLevelTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rootLevelLabel)
                    .addComponent(selectRootLevelButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(adventureLabel)
                    .addComponent(adventureTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectAdventureButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(slotIDPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(groupLabel)
                    .addComponent(groupCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(linkLabel)
                    .addComponent(linkCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(70, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout dataPaneLayout = new javax.swing.GroupLayout(dataPane);
        dataPane.setLayout(dataPaneLayout);
        dataPaneLayout.setHorizontalGroup(
            dataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dataContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        dataPaneLayout.setVerticalGroup(
            dataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dataContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        slotSettings.addTab("Data", dataPane);

        levelTypeLabel.setText("Level Type:");

        gameModeLabel.setText("GameMode:");

        jLabel3.setText("Player Limit:");

        badgeSizeLabel.setText("Badge Size:");

        flagsLabel.setText("Flags:");

        lockedCheckbox.setText("Locked");

        subLevelCheckbox.setText("Sublevel");

        moveCheckbox.setText("Move");

        crossControllerCheckbox.setText("Cross-Controller");

        shareableCheckbox.setText("Shareable");

        gameKitCheckbox.setText("Is Gamekit");

        visibleCheckbox.setText("Show Slot on Planet");

        javax.swing.GroupLayout flagsPaneLayout = new javax.swing.GroupLayout(flagsPane);
        flagsPane.setLayout(flagsPaneLayout);
        flagsPaneLayout.setHorizontalGroup(
            flagsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(flagsPaneLayout.createSequentialGroup()
                .addGroup(flagsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(shareableCheckbox)
                    .addComponent(lockedCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flagsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(subLevelCheckbox)
                    .addComponent(gameKitCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flagsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(moveCheckbox)
                    .addGroup(flagsPaneLayout.createSequentialGroup()
                        .addComponent(visibleCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(crossControllerCheckbox)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        flagsPaneLayout.setVerticalGroup(
            flagsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(flagsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(flagsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lockedCheckbox)
                    .addComponent(subLevelCheckbox)
                    .addComponent(moveCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flagsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shareableCheckbox)
                    .addComponent(gameKitCheckbox)
                    .addComponent(visibleCheckbox)
                    .addComponent(crossControllerCheckbox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        minPlayerSpinner.setModel(new javax.swing.SpinnerNumberModel(Byte.valueOf((byte)1), Byte.valueOf((byte)1), Byte.valueOf((byte)4), Byte.valueOf((byte)1)));

        maxPlayerSpinner.setModel(new javax.swing.SpinnerNumberModel(Byte.valueOf((byte)4), Byte.valueOf((byte)1), Byte.valueOf((byte)4), Byte.valueOf((byte)1)));

        enforcePlayerCheckbox.setText("Enforce");

        badgeSizeSpinner.setModel(new javax.swing.SpinnerNumberModel((byte)1, (byte)0, null, (byte)1));

        planetDecorationLabel.setText("Planet Decoration:");

        backgroundGUIDLabel.setText("Background GUID:");

        backgroundGUIDTextEntry.setModel(new javax.swing.SpinnerNumberModel(0L, 0L, null, 1L));

        locationLabel.setText("Location (XYZW):");

        locationXSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        locationYSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        locationZSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        locationWSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        javax.swing.GroupLayout settingsPaneLayout = new javax.swing.GroupLayout(settingsPane);
        settingsPane.setLayout(settingsPaneLayout);
        settingsPaneLayout.setHorizontalGroup(
            settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(flagsPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(settingsPaneLayout.createSequentialGroup()
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(levelTypeLabel)
                            .addComponent(gameModeLabel)
                            .addComponent(badgeSizeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(levelTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(gameModeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsPaneLayout.createSequentialGroup()
                                .addComponent(badgeSizeSpinner)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minPlayerSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maxPlayerSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(enforcePlayerCheckbox))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsPaneLayout.createSequentialGroup()
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(backgroundGUIDLabel)
                            .addComponent(locationLabel))
                        .addGap(17, 17, 17)
                        .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(settingsPaneLayout.createSequentialGroup()
                                .addComponent(locationXSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(locationYSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(locationZSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(locationWSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))
                            .addComponent(backgroundGUIDTextEntry)))
                    .addGroup(settingsPaneLayout.createSequentialGroup()
                        .addComponent(flagsLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(settingsPaneLayout.createSequentialGroup()
                        .addComponent(planetDecorationLabel)
                        .addGap(18, 18, 18)
                        .addComponent(planetDecorationTextEntry)))
                .addContainerGap())
        );
        settingsPaneLayout.setVerticalGroup(
            settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(levelTypeLabel)
                    .addComponent(levelTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gameModeLabel)
                    .addComponent(gameModeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(minPlayerSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPlayerSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(enforcePlayerCheckbox)
                    .addComponent(badgeSizeLabel)
                    .addComponent(badgeSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(planetDecorationLabel)
                    .addComponent(planetDecorationTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(backgroundGUIDLabel)
                    .addComponent(backgroundGUIDTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationXSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationYSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationZSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationWSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flagsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flagsPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        slotSettings.addTab("Settings", settingsPane);

        contentsTypeLabel.setText("Contents Type:");

        badgeMeshLabel.setText("Badge Mesh:");

        contentIDLabel.setText("Content ID:");

        timestampLabel.setText("Timestamp:");

        timestampSpinner.setModel(new javax.swing.SpinnerDateModel());

        javax.swing.GroupLayout dlcPaneLayout = new javax.swing.GroupLayout(dlcPane);
        dlcPane.setLayout(dlcPaneLayout);
        dlcPaneLayout.setHorizontalGroup(
            dlcPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlcPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dlcPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentsTypeLabel)
                    .addComponent(badgeMeshLabel)
                    .addComponent(contentIDLabel)
                    .addComponent(timestampLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dlcPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(badgeMeshTextEntry, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(contentIDTextEntry, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(timestampSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                    .addComponent(contentsTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        dlcPaneLayout.setVerticalGroup(
            dlcPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlcPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dlcPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contentsTypeLabel)
                    .addComponent(contentsTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dlcPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(badgeMeshLabel)
                    .addComponent(badgeMeshTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(dlcPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contentIDLabel)
                    .addComponent(contentIDTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(dlcPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timestampSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timestampLabel))
                .addContainerGap(157, Short.MAX_VALUE))
        );

        slotSettings.addTab("DLC", dlcPane);

        closeButton.setText("Close");

        slotsLabel.setText("Slots:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(slotContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(slotsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(slotSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addSlotButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeSlotButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(slotSettings)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(slotsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(slotContainer)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addSlotButton)
                    .addComponent(removeSlotButton)
                    .addComponent(closeButton))
                .addContainerGap())
        );

        slotSettings.getAccessibleContext().setAccessibleName("DLC");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectIconButonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectIconButonActionPerformed
        this.iconTextEntry.setText(ResourcePicker.getResourceString(
                this, 
                this.iconTextEntry.getText(), 
                ResourceType.TEXTURE
        ));
        this.iconTextEntry.getActionListeners()[0].actionPerformed(new ActionEvent(this.iconTextEntry, ActionEvent.ACTION_PERFORMED, null));
    }//GEN-LAST:event_selectIconButonActionPerformed

    private void selectRootLevelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectRootLevelButtonActionPerformed
        this.rootLevelTextEntry.setText(ResourcePicker.getResourceString(
                this, 
                this.rootLevelTextEntry.getText(), 
                ResourceType.LEVEL
        ));
        this.rootLevelTextEntry.getActionListeners()[0].actionPerformed(new ActionEvent(this.rootLevelTextEntry, ActionEvent.ACTION_PERFORMED, null));
    }//GEN-LAST:event_selectRootLevelButtonActionPerformed

    private void selectAdventureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAdventureButtonActionPerformed
        this.adventureTextEntry.setText(ResourcePicker.getResourceString(
                this, 
                this.adventureTextEntry.getText(), 
                ResourceType.ADVENTURE_CREATE_PROFILE
        ));
        this.adventureTextEntry.getActionListeners()[0].actionPerformed(new ActionEvent(this.adventureTextEntry, ActionEvent.ACTION_PERFORMED, null));
    }//GEN-LAST:event_selectAdventureButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSlotButton;
    private javax.swing.JLabel adventureLabel;
    private javax.swing.JTextField adventureTextEntry;
    private javax.swing.JLabel backgroundGUIDLabel;
    private javax.swing.JSpinner backgroundGUIDTextEntry;
    private javax.swing.JLabel badgeMeshLabel;
    private javax.swing.JTextField badgeMeshTextEntry;
    private javax.swing.JLabel badgeSizeLabel;
    private javax.swing.JSpinner badgeSizeSpinner;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel contentIDLabel;
    private javax.swing.JTextField contentIDTextEntry;
    private javax.swing.JComboBox<String> contentsTypeCombo;
    private javax.swing.JLabel contentsTypeLabel;
    private javax.swing.JLabel creatorLabel;
    private javax.swing.JTextField creatorTextEntry;
    private javax.swing.JCheckBox crossControllerCheckbox;
    private javax.swing.JPanel dataContainer;
    private javax.swing.JPanel dataPane;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionPane;
    private javax.swing.JTextArea descriptionTextEntry;
    private javax.swing.JPanel detailsPane;
    private javax.swing.JPanel dlcPane;
    private javax.swing.JCheckBox enforcePlayerCheckbox;
    private javax.swing.JLabel flagsLabel;
    private javax.swing.JPanel flagsPane;
    private javax.swing.JCheckBox gameKitCheckbox;
    private javax.swing.JComboBox<String> gameModeCombo;
    private javax.swing.JLabel gameModeLabel;
    private javax.swing.JComboBox<SlotEntry> groupCombo;
    private javax.swing.JLabel groupLabel;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JTextField iconTextEntry;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox<String> levelTypeCombo;
    private javax.swing.JLabel levelTypeLabel;
    private javax.swing.JComboBox<SlotEntry> linkCombo;
    private javax.swing.JLabel linkLabel;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JSpinner locationWSpinner;
    private javax.swing.JSpinner locationXSpinner;
    private javax.swing.JSpinner locationYSpinner;
    private javax.swing.JSpinner locationZSpinner;
    private javax.swing.JCheckBox lockedCheckbox;
    private javax.swing.JSpinner maxPlayerSpinner;
    private javax.swing.JSpinner minPlayerSpinner;
    private javax.swing.JCheckBox moveCheckbox;
    private javax.swing.JLabel planetDecorationLabel;
    private javax.swing.JTextField planetDecorationTextEntry;
    private javax.swing.JButton removeSlotButton;
    private javax.swing.JLabel rootLevelLabel;
    private javax.swing.JTextField rootLevelTextEntry;
    private javax.swing.JButton selectAdventureButton;
    private javax.swing.JButton selectIconButon;
    private javax.swing.JButton selectRootLevelButton;
    private javax.swing.JPanel settingsPane;
    private javax.swing.JCheckBox shareableCheckbox;
    private javax.swing.JScrollPane slotContainer;
    private javax.swing.JLabel slotIDLabel;
    private javax.swing.JPanel slotIDPane;
    private javax.swing.JLabel slotIcon;
    private javax.swing.JList<String> slotList;
    private javax.swing.JLabel slotNumberLabel;
    private javax.swing.JSpinner slotNumberSpinner;
    private javax.swing.JTabbedPane slotSettings;
    private javax.swing.JComboBox<String> slotTypeCombo;
    private javax.swing.JLabel slotTypeLabel;
    private javax.swing.JLabel slotsLabel;
    private javax.swing.JCheckBox subLevelCheckbox;
    private javax.swing.JLabel timestampLabel;
    private javax.swing.JSpinner timestampSpinner;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextEntry;
    private javax.swing.JLabel translationKeyLabel;
    private javax.swing.JTextField translationKeyTextEntry;
    private javax.swing.JCheckBox visibleCheckbox;
    // End of variables declaration//GEN-END:variables
}
