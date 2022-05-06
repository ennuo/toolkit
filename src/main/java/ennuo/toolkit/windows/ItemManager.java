package ennuo.toolkit.windows;

import ennuo.craftworld.resources.Plan;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.enums.GameVersion;
import ennuo.craftworld.resources.enums.InventoryItemFlags;
import ennuo.craftworld.resources.enums.InventoryObjectSubType;
import ennuo.craftworld.resources.enums.InventoryObjectType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.enums.ToolType;
import ennuo.craftworld.resources.structs.InventoryItem;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.plan.EyetoyData;
import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.resources.structs.plan.PhotoData;
import ennuo.craftworld.resources.structs.plan.PhotoMetadata;
import ennuo.craftworld.resources.structs.plan.UserCreatedDetails;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.BigStreamingFart;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.utilities.Bytes;
import ennuo.toolkit.utilities.Globals;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class ItemManager extends javax.swing.JFrame {
    private class ItemWrapper {
        private InventoryDetails details;
        
        private ItemWrapper(InventoryDetails details) {
            this.details = details;
        }
        
        @Override public String toString() {
            if (this.details.titleKey != 0 && Globals.LAMS != null) {
                String translated = Globals.LAMS.translate(this.details.titleKey);
                if (translated != null) return translated;
            }
            
            UserCreatedDetails uad = this.details.userCreatedDetails;
            if (uad != null) {
                if (uad.title != null && !uad.title.isEmpty())
                    return uad.title;
            }
            
            return "Some kind of object";
        }
    }
    
    
    private Revision revision;
    private FileEntry entry;
    private Plan plan;
    
    private BigStreamingFart profile;
    private ArrayList<InventoryItem> inventory;
    private ArrayList<InventoryDetails> items;
    private InventoryDetails selectedDetails;
    private InventoryItem selectedItem;
    private final DefaultListModel model = new DefaultListModel();
    
    private boolean listenForChanges = true;
    private boolean hasMadeChanges = false;
    
    private JCheckBox[] typeCheckboxes;
    
    public ItemManager(FileEntry entry, Plan plan) {
        this.entry = entry;
        this.items = new ArrayList<>(1);
        this.plan = plan;
        this.items.add(plan.details);
        this.revision = new Revision(plan.revision);
        this.setup();
        
        /* Remove the list related elemenets, since we're only dealing with a single item. */
        
        this.itemsLabel.getParent().remove(this.itemsLabel);
        this.itemList.getParent().remove(this.itemList);
        this.addItemButton.getParent().remove(this.addItemButton);
        this.removeItemButton.getParent().remove(this.removeItemButton);
        
        this.setSize(this.itemSettings.getSize().width, this.getSize().height);
        this.setContentPane(this.itemSettings);
    }
    
    public ItemManager(BigStreamingFart profile) {
        this.profile = profile;
        this.inventory = profile.bigProfile.inventory;
        this.items = new ArrayList<InventoryDetails>(this.inventory.size());
        for (InventoryItem item : this.inventory)
            this.items.add(item.details);
        this.revision = new Resource(profile.rootProfileEntry.data).revision;
        this.setup();
    }
    
    /**
     * Debug
     */
    public ItemManager() {
        this.items = new ArrayList<>();
       
            
        Arrays.asList(1046484, 412572, 1113767, 1022588, 1022587).stream().forEach(GUID -> {
            byte[] data = Globals.extractFile(GUID);
            Plan plan = new Serializer(new Resource(data).handle).struct(null, Plan.class);
            this.revision = new Resource(data).revision;
            this.items.add(plan.details);
        });
       

        this.setup();
    }
    
    private void setup() {
        this.initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        this.setResizable(false);
        this.typeScrollPane.getVerticalScrollBar().setUnitIncrement(15);
        this.detailsScrollPane.getVerticalScrollBar().setUnitIncrement(15);
        this.photoAndEyetoyDataScrollPane.getVerticalScrollBar().setUnitIncrement(15);
        
        // Disable inventory tab, since we're editing an item.
        if (this.inventory == null)
            this.itemSettings.remove(4);
        
        this.typeCheckboxes = new JCheckBox[] {
            this.materialsCheckbox, this.objectsCheckbox, this.decorationsCheckbox, this.stickersCheckbox,
            this.costumesCheckbox, this.costumeMaterialsCheckbox, this.jointsCheckbox, this.userObjectsCheckbox,
            this.backgroundsCheckbox, this.gameplayKitsCheckbox, this.userStickersCheckbox, this.shapesCheckbox,
            this.sequencerCheckbox, this.dangerCheckbox, this.eyetoyCheckbox, this.gadgetsCheckbox, this.toolsCheckbox,
            this.sackbotMeshCheckbox, this.creatureCharactersCheckbox, this.playerColorsCheckbox, this.userCostumesCheckbox,
            this.musicCheckbox, this.soundsCheckbox, this.photoboothCheckbox, this.userPlanetCheckbox, this.levelKeyCheckbox,
            this.emittedItemCheckbox, this.gunItemCheckbox, this.npcCostumeCheckbox, this.instrumentCheckbox, this.podsCheckbox,
            this.costumeTweakerCheckbox, this.paintCheckbox, this.floodFillCheckbox, this.stickerToolCheckbox, this.costumeToolCheckbox,
            this.planToolCheckbox, this.photoToolCheckbox, this.pictureToolsCheckbox, this.communityPhotosCheckbox, this.communityObjectsCheckbox,
            this.podsCheckbox, this.podToolLbp1Checkbox, this.editModeToolCheckbox, this.podToolLbp2Checkbox, this.earthToolCheckbox
        };
        
        /* Disable types that aren't applicable for game
           these items are built for.
        */
        int version = GameVersion.getFlag(this.revision);
        if ((version & GameVersion.LBP1) == 0) {
            this.lbp1TypesLabel.setVisible(false);
            this.setChildrenEnabled(this.lbp1TypeContainer, false);   
        }
        else {
            this.setChildrenEnabled(this.lbp2TypeContainer, false);  
            this.lbp2TypesLabel.setVisible(false);
        }
        if ((version & GameVersion.LBP3) == 0) {
            this.lbp3TypesLabel.setVisible(false);
            this.setChildrenEnabled(this.lbp3TypesContainer, false);   
        }
        
        // Hide painting field if it wasn't added yet.
        if (this.revision.head <= 0x3c7) {
            this.paintingLabel.setVisible(false);
            this.paintingResourceTextEntry.setVisible(false);
        }
        
        // Disable user category index if it wasn't added yet.
        if (this.revision.head <= 0x349) {
            this.categoryIndexSpinner.setVisible(false);
            this.categoryIndexLabel.setVisible(false);
        }
        
        // Disable eyetoy outline if it wasn't added yet.
        if (this.revision.head <= 0x39f) {
            this.outlineLabel.setVisible(false);
            this.outlineTextEntry.setVisible(false);
        }
        
        // Disable GUID if it wasn't added yet.
        if (!this.revision.isAfterLBP3Revision(0x105)) {
            this.guidLabel.setVisible(false);
            this.guidSpinner.setVisible(false);
        }
        
        this.itemList.setModel(this.model);
        for (InventoryDetails item : this.items)
            this.model.addElement(new ItemWrapper(item));
        
        if (this.model.size() == 0) {
            this.removeItemButton.setEnabled(false);
            this.itemSettings.setSelectedIndex(-1);
            this.itemSettings.setEnabled(false);
        }
        
        this.itemList.addListSelectionListener(listener -> {
            int index = this.itemList.getSelectedIndex();
            if (index == -1) return;
            this.selectedDetails = this.items.get(index);
            if (this.inventory != null)
                this.selectedItem = this.inventory.get(index);
            this.setItemData();
        });
        
        this.addItemButton.addActionListener(e -> {
            if (this.items.size() == 0) {
                this.removeItemButton.setEnabled(true);
                this.itemSettings.setEnabled(true);
                this.itemSettings.setSelectedIndex(0);
            }
            
            InventoryDetails details = new InventoryDetails();

            this.items.add(details);
            if (this.inventory != null) {
                InventoryItem item = new InventoryItem();
                item.details = details;
                this.inventory.add(item);
            }
            
            this.model.addElement(details);
            this.itemList.setSelectedValue(details, true);
        });
        
        this.removeItemButton.addActionListener(e -> {
            int index = this.itemList.getSelectedIndex();
            if (this.model.size() - 1 != 0) {
                if (index == 0)
                    this.itemList.setSelectedIndex(index + 1);
                else
                    this.itemList.setSelectedIndex(index - 1);   
            } else {
                this.removeItemButton.setEnabled(false);
                this.itemSettings.setSelectedIndex(-1);
                this.itemSettings.setEnabled(false);
            }
            
            this.model.remove(index);
        });
        
        this.photoDataCheckbox.addActionListener(e -> {
            if (!this.listenForChanges) return;
            this.hasMadeChanges = true;
            boolean isEnabled = this.photoDataCheckbox.isSelected();
            if (isEnabled) {
                this.selectedDetails.photoData = new PhotoData();
                this.photoDataPane.setVisible(true);
                this.setChildrenEnabled(this.photoDataPane, true);
            } else {
                this.selectedDetails.photoData = null;
                this.photoDataPane.setVisible(false);
                this.setChildrenEnabled(this.photoDataPane, false);
            }
        });
        
        this.eyetoyDataCheckbox.addActionListener(e -> {
            if (!this.listenForChanges) return;
            this.hasMadeChanges = true;
            boolean isEnabled = this.eyetoyDataCheckbox.isSelected();
            if (isEnabled) {
                this.selectedDetails.eyetoyData = new EyetoyData();
                this.eyetoyDataPane.setVisible(true);
                this.setChildrenEnabled(this.eyetoyDataPane, true);
            } else {
                this.selectedDetails.eyetoyData = null;
                this.eyetoyDataPane.setVisible(false);
                this.setChildrenEnabled(this.eyetoyDataPane, false);
            }
        });
        
        this.itemList.setSelectedIndex(0);
    }
    
    private void updateTranslations() {
        InventoryDetails details = this.selectedDetails;
        if (this.ucdRadio.isSelected()) {
            this.titleTextEntry.setEnabled(true);
            this.descriptionTextEntry.setEnabled(true);
            UserCreatedDetails ucd = details.userCreatedDetails;
            if (ucd == null) {
                this.titleTextEntry.setText("Some kind of object");
                this.descriptionTextEntry.setText("No description was provided.");
            } else {
                if (ucd.title == null || ucd.title.isEmpty())
                    this.titleTextEntry.setText("Some kind of object");
                else this.titleTextEntry.setText(ucd.title);
                if (ucd.description == null || ucd.description.isEmpty())
                    this.descriptionTextEntry.setText("No description was provided.");
                else this.descriptionTextEntry.setText(ucd.description);
            }
        } else {
            this.titleTextEntry.setEnabled(false);
            this.descriptionTextEntry.setEnabled(false);
            this.titleTextEntry.setText("Some kind of object");
            this.descriptionTextEntry.setText("A valid translation table needs to be loaded for the title and description to appear. Alternatively, remove the translation keys, and set your own title/description.");

            if (Globals.LAMS != null) {
                this.titleTextEntry.setText(Globals.LAMS.translate(details.titleKey));
                this.descriptionTextEntry.setText(Globals.LAMS.translate(details.descriptionKey));   
            }
        }
    }
    
    private void setChildrenEnabled(JPanel panel, boolean state) {
        panel.setVisible(state);
        for (Component child : panel.getComponents()) {
            if (child instanceof JPanel)
                this.setChildrenEnabled((JPanel) child, state);
            child.setEnabled(state);   
        }
    }
    
    private void setTranslationKeyFieldVisibility(boolean visible) {
        this.titleKeyLabel.setVisible(visible);
        this.titleKeySpinner.setVisible(visible);
        this.descKeyLabel.setVisible(visible);
        this.descKeySpinner.setVisible(visible);
        if (this.inventory != null) visible = false;
        this.categoryKeyLabel.setVisible(visible);
        this.categoryKeySpinner.setVisible(visible);
        this.locationKeyLabel.setVisible(visible);
        this.locationKeySpinner.setVisible(visible);
    }
    
    private void setTranslationTagFieldVisibility(boolean visible) {
        this.translationKeyLabel.setVisible(visible);
        this.translationKeyTextEntry.setVisible(visible);
        if (this.inventory == null) {
            this.categoryLabel.setVisible(visible);
            this.categoryTextEntry.setVisible(visible);
            this.locationLabel.setVisible(visible);
            this.locationTextEntry.setVisible(visible);
        }
    }
    
    private void changeTranslationType(boolean isUCD) {
        boolean isUsingKeys = (this.revision.isAfterLeerdammerRevision(7) || this.revision.head > 0x2ba);
        if (isUCD) {
            this.setTranslationKeyFieldVisibility(false);
            this.setTranslationTagFieldVisibility(false);
        } else {
            this.setTranslationKeyFieldVisibility(isUsingKeys);
            this.setTranslationTagFieldVisibility(!isUsingKeys);
        }
        this.updateTranslations();
    }
    
    private void resetIcon() {
        this.itemIcon.setIcon(null);
        this.itemIcon.setText("No icon available.");
    }
    
    private void setItemData() {
        InventoryDetails details = this.selectedDetails;
        
        System.out.println("SUBTYPE: " + details.subType);
        
        // Details tab
        
        this.resetIcon();
        if (details.icon != null) {
            this.iconTextEntry.setText(details.icon.toString());
            byte[] data = Globals.extractFile(details.icon);
            if (data != null) {
                Texture texture = new Texture(data);
                if (texture != null) {
                    this.itemIcon.setText(null);
                    this.itemIcon.setIcon(texture.getImageIcon(128, 128));
                }
            }
        }
        else this.iconTextEntry.setText("");
        
        if (details.creator != null)
            this.creatorTextEntry.setText(details.creator.handle);
        else
            this.creatorTextEntry.setText("");
        
        this.translationKeyTextEntry.setText(details.translationTag);
        this.titleKeySpinner.setValue(details.titleKey);
        this.descKeySpinner.setValue(details.descriptionKey);
        
        if (this.selectedItem == null) {
            this.categoryKeySpinner.setValue(details.category);
            this.locationKeySpinner.setValue(details.location);
            this.categoryTextEntry.setText(details.categoryTag);
            this.locationTextEntry.setText(details.locationTag);
        } else {
            this.categoryTextEntry.setText(this.profile.bigProfile.stringTable.get(details.categoryIndex));
            this.locationTextEntry.setText(this.profile.bigProfile.stringTable.get(details.locationIndex));
        }
        
        boolean useUCD = (details.userCreatedDetails != null) || (details.titleKey == 0 && details.descriptionKey == 0);
        this.translationKeysRadio.setSelected(!useUCD);
        this.ucdRadio.setSelected(useUCD);
        this.changeTranslationType(useUCD);
        
        // Types tab
        
        InventoryObjectType[] objectTypes = InventoryObjectType.values();
        for (int i = 1; i < objectTypes.length; ++i)
            this.typeCheckboxes[i - 1].setSelected(details.type.contains(objectTypes[i]));
        
        boolean isCostume = details.type.contains(InventoryObjectType.COSTUME);
        boolean isColour = details.type.contains(InventoryObjectType.PLAYER_COLOUR);
        boolean isPlanet = details.type.contains(InventoryObjectType.USER_PLANET);
        boolean isFullCostume = (details.subType & 0x80000000) != 0;
        boolean isOutfit = details.type.contains(InventoryObjectType.USER_COSTUME);
        boolean showCharacterMask = isCostume && this.revision.isLBP3();
        
        this.costumeCategoriesLabel.setVisible(isCostume && !isFullCostume);
        this.costumeCategoriesPane.setVisible(isCostume && !isFullCostume);
        this.characterMaskCombo.setVisible(showCharacterMask);
        this.characterMaskLabel.setVisible(showCharacterMask);
        
        this.planetTypeCombo.setVisible(isPlanet);
        this.planetTypeLabel.setVisible(isPlanet);
        
        this.colorIndexLabel.setVisible(isColour);
        this.colorIndexSpinner.setVisible(isColour);
        
        this.outfitFlagsLabel.setVisible(isOutfit || isCostume);
        this.outfitFlagsPane.setVisible(isOutfit || isCostume);
        
        
        JCheckBox[] categories = {
            this.beardCheckbox,
            this.feetCheckbox,
            this.eyesCheckbox,
            this.glassesCheckbox,
            this.mouthCheckbox,
            this.moustacheCheckbox,
            this.noseCheckbox,
            this.hairCheckbox,
            this.headCheckbox,
            this.neckCheckbox,
            this.torsoCheckbox,
            this.legsCheckbox,
            this.handsCheckbox,
            this.waistCheckbox
        };
        
        if (isCostume) {
            for (int i = 0; i < categories.length; ++i)
                categories[i].setSelected((details.subType & (1 << i)) != 0);
        }
        
        if (showCharacterMask) {
            boolean isDwarf = (details.subType & InventoryObjectSubType.CREATURE_MASK_DWARF) != 0;
            boolean isGiant = (details.subType & InventoryObjectSubType.CREATURE_MASK_GIANT) != 0;
            if (isGiant && isDwarf) this.characterMaskCombo.setSelectedIndex(3);
            else if (isGiant) this.characterMaskCombo.setSelectedIndex(1);
            else if (isDwarf) this.characterMaskCombo.setSelectedIndex(2);
            else if ((details.subType & InventoryObjectSubType.CREATURE_MASK_QUAD) != 0)
                this.characterMaskCombo.setSelectedIndex(4);
            else
                this.characterMaskCombo.setSelectedIndex(0);
        }
        
        if (isPlanet) {
            if (details.subType == 0) this.planetTypeCombo.setSelectedIndex(0);
            else if (details.subType == 1) this.planetTypeCombo.setSelectedIndex(1);
            else if (details.subType == 2) this.planetTypeCombo.setSelectedIndex(2);
            else if (details.subType == 4) this.planetTypeCombo.setSelectedIndex(3);
        }
        
        if (isColour) this.colorIndexSpinner.setValue(details.subType);
        
        if (isOutfit || isCostume) {
            this.madeByMeCheckbox.setSelected((details.subType & 0x20000000) != 0);
            this.madeByOthersCheckbox.setSelected((details.subType & 0x40000000) != 0);
            this.fullCostumeCheckbox.setSelected((details.subType & 0x80000000) != 0);
            this.specialCostumeCheckbox.setSelected((details.subType & 0x04000000) != 0);
        }
        
        // Other tab
        
        this.unlockSlotTypeCombo.setSelectedItem(details.levelUnlockSlotID.type);
        this.unlockSlotNumberSpinner.setValue(details.levelUnlockSlotID.ID);
        this.highlightSoundSpinner.setValue(details.highlightSound);
        this.dateAddedSpinner.setValue(new Date(details.dateAdded / 2 * 1000));
        
        byte[] color = Bytes.toBytes(details.colour);
        this.colorRedSpinner.setValue(color[1] & 0xFF);
        this.colorGreenSpinner.setValue(color[2] & 0xFF);
        this.colorBlueSpinner.setValue(color[3] & 0xFF);
        
        this.toolTypeCombo.setSelectedItem(details.toolType);
        
        this.allowEmitCheckbox.setSelected((details.flags & InventoryItemFlags.ALLOW_EMIT) != 0);
        this.copyrightCheckbox.setSelected((details.flags & InventoryItemFlags.COPYRIGHT) != 0);
        this.unusedCheckbox.setSelected((details.flags & InventoryItemFlags.USED) == 0);
        this.hiddenCheckbox.setSelected((details.flags & InventoryItemFlags.HIDDEN_ITEM) != 0);
        this.restrictedCheckbox.setSelected(
                (details.flags & InventoryItemFlags.RESTRICTED_LEVEL) != 0 || 
                (details.flags & InventoryItemFlags.RESTRICTED_POD) != 0);
        this.loopPreviewCheckbox.setSelected((details.flags & InventoryItemFlags.DISABLE_LOOP_PREVIEW) == 0);
        
        // Photo data tab
        if (details.photoData != null) {
            PhotoData data = details.photoData;
            
            this.photoDataCheckbox.setSelected(true);
            this.setChildrenEnabled(this.photoDataPane, true);
            this.photoDataPane.setVisible(true);
            
            this.photoIconResourceTextEntry.setText(data.icon != null ? data.icon.toString() : "");
            this.stickerResourceTextEntry.setText(data.sticker != null ? data.sticker.toString() : "");
            this.paintingResourceTextEntry.setText(data.painting != null ? data.painting.toString() : "");
            
            PhotoMetadata metadata = data.photoMetadata;
            
            this.photoResourceTextEntry.setText(metadata.photo != null ? metadata.photo.toString() : "");
            this.photoLevelTypeCombo.setSelectedItem(metadata.level.type);
            this.photoLevelNumberSpinner.setValue(metadata.level.ID);
            this.photoLevelHashTextEntry.setText(metadata.levelHash.toString());
        } else {
            this.photoDataCheckbox.setSelected(false);
            this.setChildrenEnabled(this.photoDataPane, false);
            this.photoDataPane.setVisible(false);
        }
        
        if (details.eyetoyData != null) {
            this.eyetoyDataCheckbox.setSelected(true);
            this.setChildrenEnabled(this.eyetoyDataPane, true);
            this.frameTextEntry.setText(details.eyetoyData.frame != null ? details.eyetoyData.frame.toString() : "");
            this.alphaMaskTextEntry.setText(details.eyetoyData.alphaMask != null ? details.eyetoyData.alphaMask.toString() : "");
            this.outlineTextEntry.setText(details.eyetoyData.outline != null ? details.eyetoyData.outline.toString() : "");
        } else {
            this.frameTextEntry.setText("");
            this.alphaMaskTextEntry.setText("");
            this.outlineTextEntry.setText("");
            
            this.eyetoyDataCheckbox.setSelected(false);
            this.setChildrenEnabled(this.eyetoyDataPane, false);
            this.eyetoyDataPane.setVisible(false);
        }
        
        
        
        // Inventory tab
        if (this.selectedItem != null) {
            InventoryItem item = this.selectedItem;
            if (item.plan != null) this.planTextField.setText(item.plan.toString());
            else this.planTextField.setText("");
            this.guidSpinner.setValue(item.GUID);
            this.uidSpinner.setValue(item.UID);
            this.categoryIndexSpinner.setValue(item.userCategoryIndex);
            
            this.heartedCheckbox.setSelected((item.flags & InventoryItemFlags.HEARTED) != 0);
            this.uploadedCheckbox.setSelected((item.flags & InventoryItemFlags.UPLOADED) != 0);
            this.cheatCheckbox.setSelected((item.flags & InventoryItemFlags.CHEAT) != 0);
            this.unsavedCheckbox.setSelected((item.flags & InventoryItemFlags.UNSAVED) != 0);
            this.erroredCheckbox.setSelected((item.flags & InventoryItemFlags.ERRORED) != 0);
            this.inventoryHiddenCheckbox.setSelected((item.flags & InventoryItemFlags.HIDDEN_ITEM) != 0);
            this.autosavedCheckbox.setSelected((item.flags & InventoryItemFlags.AUTOSAVED) != 0);
        }
        
    }
  
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        translationTypeButtonGroup = new javax.swing.ButtonGroup();
        slotContainer = new javax.swing.JScrollPane();
        itemList = new javax.swing.JList<>();
        addItemButton = new javax.swing.JButton();
        removeItemButton = new javax.swing.JButton();
        itemSettings = new javax.swing.JTabbedPane();
        detailsScrollPane = new javax.swing.JScrollPane();
        detailsPane = new javax.swing.JPanel();
        itemIcon = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        titleTextEntry = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionPane = new javax.swing.JScrollPane();
        descriptionTextEntry = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        iconTextEntry = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        creatorTextEntry = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        translationKeysRadio = new javax.swing.JRadioButton();
        ucdRadio = new javax.swing.JRadioButton();
        translationKeyLabel = new javax.swing.JLabel();
        titleKeyLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        titleKeySpinner = new javax.swing.JSpinner();
        descKeyLabel = new javax.swing.JLabel();
        descKeySpinner = new javax.swing.JSpinner();
        categoryKeyLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        categoryKeySpinner = new javax.swing.JSpinner();
        locationKeyLabel = new javax.swing.JLabel();
        locationKeySpinner = new javax.swing.JSpinner();
        categoryLabel = new javax.swing.JLabel();
        categoryTextEntry = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationTextEntry = new javax.swing.JTextField();
        translationKeyTextEntry = new javax.swing.JTextField();
        typeScrollPane = new javax.swing.JScrollPane();
        typesPane = new javax.swing.JPanel();
        generalTypesLabel = new javax.swing.JLabel();
        generalTypesContainer = new javax.swing.JPanel();
        materialsCheckbox = new javax.swing.JCheckBox();
        objectsCheckbox = new javax.swing.JCheckBox();
        decorationsCheckbox = new javax.swing.JCheckBox();
        stickersCheckbox = new javax.swing.JCheckBox();
        costumeMaterialsCheckbox = new javax.swing.JCheckBox();
        jointsCheckbox = new javax.swing.JCheckBox();
        userObjectsCheckbox = new javax.swing.JCheckBox();
        backgroundsCheckbox = new javax.swing.JCheckBox();
        gameplayKitsCheckbox = new javax.swing.JCheckBox();
        costumesCheckbox = new javax.swing.JCheckBox();
        userStickersCheckbox = new javax.swing.JCheckBox();
        shapesCheckbox = new javax.swing.JCheckBox();
        toolsCheckbox = new javax.swing.JCheckBox();
        dangerCheckbox = new javax.swing.JCheckBox();
        eyetoyCheckbox = new javax.swing.JCheckBox();
        gadgetsCheckbox = new javax.swing.JCheckBox();
        playerColorsCheckbox = new javax.swing.JCheckBox();
        userCostumesCheckbox = new javax.swing.JCheckBox();
        musicCheckbox = new javax.swing.JCheckBox();
        soundsCheckbox = new javax.swing.JCheckBox();
        photoboothCheckbox = new javax.swing.JCheckBox();
        podsCheckbox = new javax.swing.JCheckBox();
        lbp1TypesLabel = new javax.swing.JLabel();
        lbp1TypeContainer = new javax.swing.JPanel();
        paintCheckbox = new javax.swing.JCheckBox();
        planToolCheckbox = new javax.swing.JCheckBox();
        communityPhotosCheckbox = new javax.swing.JCheckBox();
        floodFillCheckbox = new javax.swing.JCheckBox();
        photoToolCheckbox = new javax.swing.JCheckBox();
        stickerToolCheckbox = new javax.swing.JCheckBox();
        pictureToolsCheckbox = new javax.swing.JCheckBox();
        costumeToolCheckbox = new javax.swing.JCheckBox();
        podToolLbp1Checkbox = new javax.swing.JCheckBox();
        communityObjectsCheckbox = new javax.swing.JCheckBox();
        lbp2TypesLabel = new javax.swing.JLabel();
        lbp2TypeContainer = new javax.swing.JPanel();
        sequencerCheckbox = new javax.swing.JCheckBox();
        gunItemCheckbox = new javax.swing.JCheckBox();
        editModeToolCheckbox = new javax.swing.JCheckBox();
        userPlanetCheckbox = new javax.swing.JCheckBox();
        npcCostumeCheckbox = new javax.swing.JCheckBox();
        levelKeyCheckbox = new javax.swing.JCheckBox();
        instrumentCheckbox = new javax.swing.JCheckBox();
        emittedItemCheckbox = new javax.swing.JCheckBox();
        earthToolCheckbox = new javax.swing.JCheckBox();
        podToolLbp2Checkbox = new javax.swing.JCheckBox();
        lbp3TypesLabel = new javax.swing.JLabel();
        lbp3TypesContainer = new javax.swing.JPanel();
        sackbotMeshCheckbox = new javax.swing.JCheckBox();
        creatureCharactersCheckbox = new javax.swing.JCheckBox();
        costumeTweakerCheckbox = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        costumeCategoriesPane = new javax.swing.JPanel();
        beardCheckbox = new javax.swing.JCheckBox();
        feetCheckbox = new javax.swing.JCheckBox();
        eyesCheckbox = new javax.swing.JCheckBox();
        glassesCheckbox = new javax.swing.JCheckBox();
        hairCheckbox = new javax.swing.JCheckBox();
        noseCheckbox = new javax.swing.JCheckBox();
        moustacheCheckbox = new javax.swing.JCheckBox();
        mouthCheckbox = new javax.swing.JCheckBox();
        neckCheckbox = new javax.swing.JCheckBox();
        headCheckbox = new javax.swing.JCheckBox();
        torsoCheckbox = new javax.swing.JCheckBox();
        legsCheckbox = new javax.swing.JCheckBox();
        handsCheckbox = new javax.swing.JCheckBox();
        waistCheckbox = new javax.swing.JCheckBox();
        costumeCategoriesLabel = new javax.swing.JLabel();
        characterMaskLabel = new javax.swing.JLabel();
        characterMaskCombo = new javax.swing.JComboBox<>();
        planetTypeLabel = new javax.swing.JLabel();
        planetTypeCombo = new javax.swing.JComboBox<>();
        colorIndexLabel = new javax.swing.JLabel();
        colorIndexSpinner = new javax.swing.JSpinner();
        outfitFlagsLabel = new javax.swing.JLabel();
        outfitFlagsPane = new javax.swing.JPanel();
        madeByMeCheckbox = new javax.swing.JCheckBox();
        madeByOthersCheckbox = new javax.swing.JCheckBox();
        fullCostumeCheckbox = new javax.swing.JCheckBox();
        specialCostumeCheckbox = new javax.swing.JCheckBox();
        othersPane = new javax.swing.JPanel();
        unlockSlotIDLabel = new javax.swing.JLabel();
        slotTypeLabel = new javax.swing.JLabel();
        unlockSlotTypeCombo = new javax.swing.JComboBox(SlotType.values());
        slotNumberLabel = new javax.swing.JLabel();
        unlockSlotNumberSpinner = new javax.swing.JSpinner();
        highlightSoundLabel = new javax.swing.JLabel();
        dateAddedLabel = new javax.swing.JLabel();
        dateAddedSpinner = new javax.swing.JSpinner();
        colorLabel = new javax.swing.JLabel();
        colorRedSpinner = new javax.swing.JSpinner();
        colorGreenSpinner = new javax.swing.JSpinner();
        colorBlueSpinner = new javax.swing.JSpinner();
        highlightSoundSpinner = new javax.swing.JSpinner();
        planFlagsLabel = new javax.swing.JLabel();
        allowEmitCheckbox = new javax.swing.JCheckBox();
        restrictedCheckbox = new javax.swing.JCheckBox();
        unusedCheckbox = new javax.swing.JCheckBox();
        copyrightCheckbox = new javax.swing.JCheckBox();
        loopPreviewCheckbox = new javax.swing.JCheckBox();
        toolTypeLabel = new javax.swing.JLabel();
        toolTypeCombo = new javax.swing.JComboBox(ToolType.values());
        hiddenCheckbox = new javax.swing.JCheckBox();
        photoAndEyetoyDataScrollPane = new javax.swing.JScrollPane();
        photoAndEyetoyDataPane = new javax.swing.JPanel();
        photoDataCheckbox = new javax.swing.JCheckBox();
        photoDataPane = new javax.swing.JPanel();
        photoIconLabel = new javax.swing.JLabel();
        photoIconResourceTextEntry = new javax.swing.JTextField();
        stickerLabel = new javax.swing.JLabel();
        stickerResourceTextEntry = new javax.swing.JTextField();
        paintingLabel = new javax.swing.JLabel();
        paintingResourceTextEntry = new javax.swing.JTextField();
        photoLabel = new javax.swing.JLabel();
        photoResourceTextEntry = new javax.swing.JTextField();
        photoLevelLabel = new javax.swing.JLabel();
        photoLevelTypeLabel = new javax.swing.JLabel();
        photoLevelTypeCombo = new javax.swing.JComboBox(SlotType.values());
        photoLevelNumberLabel = new javax.swing.JLabel();
        photoLevelNumberSpinner = new javax.swing.JSpinner();
        levelHashLabel = new javax.swing.JLabel();
        photoLevelHashTextEntry = new javax.swing.JTextField();
        photoUsersLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        photoUserListScrollPane = new javax.swing.JScrollPane();
        photoUserList = new javax.swing.JList<>();
        photoUserPsidLabel = new javax.swing.JLabel();
        photoPsidTextField = new javax.swing.JTextField();
        photoUserLabel = new javax.swing.JLabel();
        photoUserTextEntry = new javax.swing.JTextField();
        photoUserBoundsLabel = new javax.swing.JLabel();
        jSpinner2 = new javax.swing.JSpinner();
        jSpinner3 = new javax.swing.JSpinner();
        jSpinner4 = new javax.swing.JSpinner();
        jSpinner5 = new javax.swing.JSpinner();
        removePhotoUserButton = new javax.swing.JButton();
        addPhotoUserButton = new javax.swing.JButton();
        eyetoyDataCheckbox = new javax.swing.JCheckBox();
        eyetoyDataPane = new javax.swing.JPanel();
        frameLabel = new javax.swing.JLabel();
        frameTextEntry = new javax.swing.JTextField();
        alphaMaskLabel = new javax.swing.JLabel();
        alphaMaskTextEntry = new javax.swing.JTextField();
        outlineLabel = new javax.swing.JLabel();
        outlineTextEntry = new javax.swing.JTextField();
        inventoryPane = new javax.swing.JPanel();
        planLabel = new javax.swing.JLabel();
        planTextField = new javax.swing.JTextField();
        guidLabel = new javax.swing.JLabel();
        uidLabel = new javax.swing.JLabel();
        guidSpinner = new javax.swing.JSpinner();
        uidSpinner = new javax.swing.JSpinner();
        categoryIndexLabel = new javax.swing.JLabel();
        categoryIndexSpinner = new javax.swing.JSpinner();
        inventoryFlagsLabel = new javax.swing.JLabel();
        inventoryFlagsContainer = new javax.swing.JPanel();
        heartedCheckbox = new javax.swing.JCheckBox();
        cheatCheckbox = new javax.swing.JCheckBox();
        unsavedCheckbox = new javax.swing.JCheckBox();
        erroredCheckbox = new javax.swing.JCheckBox();
        uploadedCheckbox = new javax.swing.JCheckBox();
        inventoryHiddenCheckbox = new javax.swing.JCheckBox();
        autosavedCheckbox = new javax.swing.JCheckBox();
        closeButton = new javax.swing.JButton();
        itemsLabel = new javax.swing.JLabel();

        this.translationTypeButtonGroup.add(this.translationKeysRadio);
        this.translationTypeButtonGroup.add(this.ucdRadio);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Item Manager");

        itemList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        slotContainer.setViewportView(itemList);

        addItemButton.setText("Add Item");
        addItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemButtonActionPerformed(evt);
            }
        });

        removeItemButton.setText("Remove");
        removeItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeItemButtonActionPerformed(evt);
            }
        });

        detailsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        itemIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        itemIcon.setText("No icon available.");

        titleLabel.setText("Title:");

        descriptionLabel.setText("Description:");

        descriptionPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        descriptionTextEntry.setColumns(20);
        descriptionTextEntry.setLineWrap(true);
        descriptionTextEntry.setRows(5);
        descriptionTextEntry.setWrapStyleWord(true);
        descriptionPane.setViewportView(descriptionTextEntry);

        jLabel1.setText("Icon:");

        jLabel2.setText("Creator:");

        jLabel3.setText("Translation Type:");

        translationKeysRadio.setText("Translation Keys");
        translationKeysRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                translationKeysRadioActionPerformed(evt);
            }
        });

        ucdRadio.setText("User Created Details");
        ucdRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ucdRadioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(translationKeysRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ucdRadio)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(translationKeysRadio)
                .addComponent(ucdRadio))
        );

        translationKeyLabel.setText("Translation Key:");

        titleKeyLabel.setText("Title Key:");

        descKeyLabel.setText("Description Key:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(titleKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(descKeyLabel)
                .addGap(18, 18, 18)
                .addComponent(descKeySpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(titleKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(descKeyLabel)
                .addComponent(descKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        categoryKeyLabel.setText("Category Key:");

        locationKeyLabel.setText("Location Key:");
        locationKeyLabel.setMaximumSize(new java.awt.Dimension(85, 16));
        locationKeyLabel.setMinimumSize(new java.awt.Dimension(85, 16));
        locationKeyLabel.setPreferredSize(new java.awt.Dimension(85, 16));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(categoryKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(locationKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(locationKeySpinner))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(categoryKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(locationKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(locationKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        categoryLabel.setText("Category:");

        locationLabel.setText("Location:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(translationKeyLabel)
                    .addComponent(titleKeyLabel)
                    .addComponent(categoryKeyLabel)
                    .addComponent(categoryLabel)
                    .addComponent(locationLabel)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iconTextEntry)
                    .addComponent(creatorTextEntry)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(locationTextEntry, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(categoryTextEntry, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(translationKeyTextEntry))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(iconTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(creatorTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(translationKeyLabel)
                    .addComponent(translationKeyTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(titleKeyLabel)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(categoryKeyLabel)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(categoryLabel)
                    .addComponent(categoryTextEntry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout detailsPaneLayout = new javax.swing.GroupLayout(detailsPane);
        detailsPane.setLayout(detailsPaneLayout);
        detailsPaneLayout.setHorizontalGroup(
            detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(detailsPaneLayout.createSequentialGroup()
                        .addComponent(itemIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(detailsPaneLayout.createSequentialGroup()
                                .addComponent(titleLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(titleTextEntry))
                            .addGroup(detailsPaneLayout.createSequentialGroup()
                                .addComponent(descriptionLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(descriptionPane, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE))))
                .addContainerGap())
        );
        detailsPaneLayout.setVerticalGroup(
            detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(detailsPaneLayout.createSequentialGroup()
                        .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(titleLabel)
                            .addComponent(titleTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(descriptionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(descriptionPane, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(itemIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        detailsScrollPane.setViewportView(detailsPane);

        itemSettings.addTab("Details", detailsScrollPane);

        typeScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        generalTypesLabel.setText("General Types:");

        materialsCheckbox.setText("Materials");

        objectsCheckbox.setText("Objects");

        decorationsCheckbox.setText("Decorations");

        stickersCheckbox.setText("Stickers");

        costumeMaterialsCheckbox.setText("Costume Materials");

        jointsCheckbox.setText("Joints");

        userObjectsCheckbox.setText("User Objects");

        backgroundsCheckbox.setText("Backgrounds");

        gameplayKitsCheckbox.setText("Gameplay Kits");

        costumesCheckbox.setText("Costumes");

        userStickersCheckbox.setText("User Stickers");

        shapesCheckbox.setText("Shapes");

        toolsCheckbox.setText("Tools");

        dangerCheckbox.setText("Danger");

        eyetoyCheckbox.setText("Eyetoy");

        gadgetsCheckbox.setText("Gadgets");

        playerColorsCheckbox.setText("Player Colors");

        userCostumesCheckbox.setText("User Costumes");

        musicCheckbox.setText("Music");

        soundsCheckbox.setText("Sound");

        photoboothCheckbox.setText("Photobooth");

        podsCheckbox.setText("Pods");

        javax.swing.GroupLayout generalTypesContainerLayout = new javax.swing.GroupLayout(generalTypesContainer);
        generalTypesContainer.setLayout(generalTypesContainerLayout);
        generalTypesContainerLayout.setHorizontalGroup(
            generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalTypesContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalTypesContainerLayout.createSequentialGroup()
                        .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(decorationsCheckbox)
                            .addComponent(materialsCheckbox)
                            .addComponent(objectsCheckbox)
                            .addComponent(stickersCheckbox)
                            .addComponent(costumesCheckbox))
                        .addGap(43, 43, 43)
                        .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(generalTypesContainerLayout.createSequentialGroup()
                                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(gameplayKitsCheckbox)
                                    .addComponent(jointsCheckbox)
                                    .addComponent(userObjectsCheckbox)
                                    .addComponent(backgroundsCheckbox))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(gadgetsCheckbox)
                                    .addComponent(eyetoyCheckbox)
                                    .addComponent(toolsCheckbox)
                                    .addComponent(dangerCheckbox)))
                            .addGroup(generalTypesContainerLayout.createSequentialGroup()
                                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(userStickersCheckbox)
                                    .addComponent(shapesCheckbox))
                                .addGap(18, 18, 18)
                                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(playerColorsCheckbox)
                                    .addGroup(generalTypesContainerLayout.createSequentialGroup()
                                        .addComponent(userCostumesCheckbox)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(musicCheckbox)
                                            .addComponent(soundsCheckbox)
                                            .addComponent(photoboothCheckbox)
                                            .addComponent(podsCheckbox)))))))
                    .addComponent(costumeMaterialsCheckbox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        generalTypesContainerLayout.setVerticalGroup(
            generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalTypesContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(materialsCheckbox)
                    .addComponent(jointsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dangerCheckbox)
                    .addComponent(musicCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(objectsCheckbox)
                    .addComponent(userObjectsCheckbox)
                    .addComponent(eyetoyCheckbox)
                    .addComponent(soundsCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(decorationsCheckbox)
                    .addComponent(backgroundsCheckbox)
                    .addComponent(gadgetsCheckbox)
                    .addComponent(photoboothCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stickersCheckbox)
                    .addComponent(gameplayKitsCheckbox)
                    .addComponent(toolsCheckbox)
                    .addComponent(podsCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(costumesCheckbox)
                    .addComponent(userStickersCheckbox)
                    .addComponent(playerColorsCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(costumeMaterialsCheckbox)
                    .addComponent(shapesCheckbox)
                    .addComponent(userCostumesCheckbox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lbp1TypesLabel.setText("LBP1 Types:");

        paintCheckbox.setText("Paint");

        planToolCheckbox.setText("Plan Tool");

        communityPhotosCheckbox.setText("Community Photo Tools");

        floodFillCheckbox.setText("Flood Fill");

        photoToolCheckbox.setText("Photo Tool");

        stickerToolCheckbox.setText("Sticker Tool");

        pictureToolsCheckbox.setText("Picture Tools");

        costumeToolCheckbox.setText("Costume Tool");

        podToolLbp1Checkbox.setText("Pod Tool (LBP1)");

        communityObjectsCheckbox.setText("Community Object Tools");

        javax.swing.GroupLayout lbp1TypeContainerLayout = new javax.swing.GroupLayout(lbp1TypeContainer);
        lbp1TypeContainer.setLayout(lbp1TypeContainerLayout);
        lbp1TypeContainerLayout.setHorizontalGroup(
            lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp1TypeContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(lbp1TypeContainerLayout.createSequentialGroup()
                        .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(paintCheckbox)
                            .addComponent(planToolCheckbox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(photoToolCheckbox)
                            .addComponent(floodFillCheckbox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stickerToolCheckbox)
                            .addComponent(pictureToolsCheckbox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(podToolLbp1Checkbox)
                            .addComponent(costumeToolCheckbox)))
                    .addGroup(lbp1TypeContainerLayout.createSequentialGroup()
                        .addComponent(communityPhotosCheckbox)
                        .addGap(18, 18, 18)
                        .addComponent(communityObjectsCheckbox)))
                .addGap(134, 134, 134))
        );
        lbp1TypeContainerLayout.setVerticalGroup(
            lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp1TypeContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(paintCheckbox)
                    .addComponent(floodFillCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stickerToolCheckbox)
                    .addComponent(costumeToolCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(planToolCheckbox)
                    .addComponent(photoToolCheckbox)
                    .addComponent(pictureToolsCheckbox)
                    .addComponent(podToolLbp1Checkbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(communityPhotosCheckbox)
                    .addComponent(communityObjectsCheckbox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lbp2TypesLabel.setText("LBP2 Types:");

        sequencerCheckbox.setText("Sequencer");

        gunItemCheckbox.setText("Gun Item");

        editModeToolCheckbox.setText("Edit Mode Tool");

        userPlanetCheckbox.setText("User Planet");

        npcCostumeCheckbox.setText("NPC Costume");

        levelKeyCheckbox.setText("Level Key");

        instrumentCheckbox.setText("Instrument");

        emittedItemCheckbox.setText("Emitted Item");

        earthToolCheckbox.setText("Earth Tool");

        podToolLbp2Checkbox.setText("Pod Tool (LBP2)");

        javax.swing.GroupLayout lbp2TypeContainerLayout = new javax.swing.GroupLayout(lbp2TypeContainer);
        lbp2TypeContainer.setLayout(lbp2TypeContainerLayout);
        lbp2TypeContainerLayout.setHorizontalGroup(
            lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp2TypeContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sequencerCheckbox)
                    .addComponent(gunItemCheckbox)
                    .addComponent(editModeToolCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userPlanetCheckbox)
                    .addComponent(npcCostumeCheckbox)
                    .addComponent(podToolLbp2Checkbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(instrumentCheckbox)
                    .addComponent(earthToolCheckbox)
                    .addGroup(lbp2TypeContainerLayout.createSequentialGroup()
                        .addComponent(levelKeyCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(emittedItemCheckbox)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        lbp2TypeContainerLayout.setVerticalGroup(
            lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp2TypeContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sequencerCheckbox)
                    .addComponent(userPlanetCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(levelKeyCheckbox)
                    .addComponent(emittedItemCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gunItemCheckbox)
                    .addComponent(npcCostumeCheckbox)
                    .addComponent(instrumentCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editModeToolCheckbox)
                    .addComponent(podToolLbp2Checkbox)
                    .addComponent(earthToolCheckbox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lbp3TypesLabel.setText("LBP3 Types:");

        sackbotMeshCheckbox.setText("Sackbot Mesh");

        creatureCharactersCheckbox.setText("Creature Characters");

        costumeTweakerCheckbox.setText("Costume Tweaker Tool");

        javax.swing.GroupLayout lbp3TypesContainerLayout = new javax.swing.GroupLayout(lbp3TypesContainer);
        lbp3TypesContainer.setLayout(lbp3TypesContainerLayout);
        lbp3TypesContainerLayout.setHorizontalGroup(
            lbp3TypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp3TypesContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sackbotMeshCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(creatureCharactersCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(costumeTweakerCheckbox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        lbp3TypesContainerLayout.setVerticalGroup(
            lbp3TypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp3TypesContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lbp3TypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sackbotMeshCheckbox)
                    .addComponent(creatureCharactersCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(costumeTweakerCheckbox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setText("Subtypes:");

        beardCheckbox.setText("Beard");

        feetCheckbox.setText("Feet");

        eyesCheckbox.setText("Eyes");

        glassesCheckbox.setText("Glasses");

        hairCheckbox.setText("Hair");

        noseCheckbox.setText("Nose");

        moustacheCheckbox.setText("Moustache");

        mouthCheckbox.setText("Mouth");

        neckCheckbox.setText("Neck");

        headCheckbox.setText("Head");

        torsoCheckbox.setText("Torso");

        legsCheckbox.setText("Legs");

        handsCheckbox.setText("Hands");

        waistCheckbox.setText("Waist");

        javax.swing.GroupLayout costumeCategoriesPaneLayout = new javax.swing.GroupLayout(costumeCategoriesPane);
        costumeCategoriesPane.setLayout(costumeCategoriesPaneLayout);
        costumeCategoriesPaneLayout.setHorizontalGroup(
            costumeCategoriesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(costumeCategoriesPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(costumeCategoriesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eyesCheckbox)
                    .addComponent(feetCheckbox)
                    .addComponent(beardCheckbox))
                .addGap(20, 20, 20)
                .addGroup(costumeCategoriesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(moustacheCheckbox)
                    .addComponent(mouthCheckbox)
                    .addComponent(noseCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(costumeCategoriesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(costumeCategoriesPaneLayout.createSequentialGroup()
                        .addComponent(torsoCheckbox)
                        .addGap(18, 18, 18)
                        .addComponent(legsCheckbox))
                    .addGroup(costumeCategoriesPaneLayout.createSequentialGroup()
                        .addGroup(costumeCategoriesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(headCheckbox)
                            .addComponent(neckCheckbox))
                        .addGap(18, 18, 18)
                        .addGroup(costumeCategoriesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(costumeCategoriesPaneLayout.createSequentialGroup()
                                .addComponent(waistCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(hairCheckbox))
                            .addGroup(costumeCategoriesPaneLayout.createSequentialGroup()
                                .addComponent(handsCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(glassesCheckbox)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        costumeCategoriesPaneLayout.setVerticalGroup(
            costumeCategoriesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(costumeCategoriesPaneLayout.createSequentialGroup()
                .addGroup(costumeCategoriesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(beardCheckbox)
                    .addComponent(mouthCheckbox)
                    .addComponent(headCheckbox)
                    .addComponent(handsCheckbox)
                    .addComponent(glassesCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(costumeCategoriesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(feetCheckbox)
                    .addComponent(moustacheCheckbox)
                    .addComponent(neckCheckbox)
                    .addComponent(waistCheckbox)
                    .addComponent(hairCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(costumeCategoriesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eyesCheckbox)
                    .addComponent(noseCheckbox)
                    .addComponent(torsoCheckbox)
                    .addComponent(legsCheckbox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        costumeCategoriesLabel.setText("Costume Categories:");

        characterMaskLabel.setText("Character Mask:");

        characterMaskCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SACKBOY", "GIANT", "DWARF", "BIRD", "QUAD" }));

        planetTypeLabel.setText("Planet Type:");

        planetTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "EARTH", "MOON", "ADVENTURE", "EXTERNAL" }));

        colorIndexLabel.setText("Color Index:");

        colorIndexSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        outfitFlagsLabel.setText("Outfit Flags:");

        madeByMeCheckbox.setText("Made by me");

        madeByOthersCheckbox.setText("Made by others");

        fullCostumeCheckbox.setText("Full Costume");

        specialCostumeCheckbox.setText("Special Costume");

        javax.swing.GroupLayout outfitFlagsPaneLayout = new javax.swing.GroupLayout(outfitFlagsPane);
        outfitFlagsPane.setLayout(outfitFlagsPaneLayout);
        outfitFlagsPaneLayout.setHorizontalGroup(
            outfitFlagsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outfitFlagsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(madeByMeCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(madeByOthersCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fullCostumeCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(specialCostumeCheckbox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        outfitFlagsPaneLayout.setVerticalGroup(
            outfitFlagsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outfitFlagsPaneLayout.createSequentialGroup()
                .addGroup(outfitFlagsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(madeByMeCheckbox)
                    .addComponent(madeByOthersCheckbox)
                    .addComponent(fullCostumeCheckbox)
                    .addComponent(specialCostumeCheckbox))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout typesPaneLayout = new javax.swing.GroupLayout(typesPane);
        typesPane.setLayout(typesPaneLayout);
        typesPaneLayout.setHorizontalGroup(
            typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typesPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(typesPaneLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(characterMaskLabel)
                            .addComponent(planetTypeLabel)
                            .addComponent(colorIndexLabel)
                            .addComponent(outfitFlagsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(characterMaskCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(planetTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(colorIndexSpinner)))
                    .addGroup(typesPaneLayout.createSequentialGroup()
                        .addGroup(typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lbp2TypeContainer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lbp1TypeContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addComponent(generalTypesLabel)
                                    .addComponent(lbp1TypesLabel)
                                    .addComponent(generalTypesContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(lbp2TypesLabel))
                            .addComponent(lbp3TypesLabel)
                            .addComponent(lbp3TypesContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4)
                            .addComponent(costumeCategoriesPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(typesPaneLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(costumeCategoriesLabel)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(outfitFlagsPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        typesPaneLayout.setVerticalGroup(
            typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typesPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalTypesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generalTypesContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbp1TypesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbp1TypeContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbp2TypesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbp2TypeContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbp3TypesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbp3TypesContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(costumeCategoriesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(costumeCategoriesPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(characterMaskLabel)
                    .addComponent(characterMaskCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(planetTypeLabel)
                    .addComponent(planetTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorIndexLabel)
                    .addComponent(colorIndexSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outfitFlagsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outfitFlagsPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        typeScrollPane.setViewportView(typesPane);

        itemSettings.addTab("Type", typeScrollPane);

        unlockSlotIDLabel.setText("Unlock Slot ID:");

        slotTypeLabel.setText("Type:");

        slotNumberLabel.setText("Number:");

        unlockSlotNumberSpinner.setModel(new javax.swing.SpinnerNumberModel(0L, 0L, null, 1L));

        highlightSoundLabel.setText("Highlight Sound:");

        dateAddedLabel.setText("Date Added:");

        dateAddedSpinner.setModel(new javax.swing.SpinnerDateModel());

        colorLabel.setText("Color (RGB):");

        colorRedSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        colorGreenSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        colorBlueSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        highlightSoundSpinner.setModel(new javax.swing.SpinnerNumberModel(0L, 0L, null, 1L));

        planFlagsLabel.setText("Flags:");

        allowEmitCheckbox.setText("Emittable");

        restrictedCheckbox.setText("Restricted");

        unusedCheckbox.setText("Unused");

        copyrightCheckbox.setText("Copyright");

        loopPreviewCheckbox.setText("Loop Preview");

        toolTypeLabel.setText("Tool Type:");

        hiddenCheckbox.setText("Hidden");

        javax.swing.GroupLayout othersPaneLayout = new javax.swing.GroupLayout(othersPane);
        othersPane.setLayout(othersPaneLayout);
        othersPaneLayout.setHorizontalGroup(
            othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(othersPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(othersPaneLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(slotNumberLabel)
                            .addComponent(slotTypeLabel))
                        .addGap(18, 18, 18)
                        .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(unlockSlotTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(unlockSlotNumberSpinner)))
                    .addGroup(othersPaneLayout.createSequentialGroup()
                        .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(highlightSoundLabel)
                            .addComponent(unlockSlotIDLabel)
                            .addComponent(dateAddedLabel)
                            .addComponent(colorLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(othersPaneLayout.createSequentialGroup()
                                .addComponent(colorRedSpinner)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(colorGreenSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(colorBlueSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(dateAddedSpinner)
                            .addComponent(highlightSoundSpinner)))
                    .addGroup(othersPaneLayout.createSequentialGroup()
                        .addComponent(toolTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(toolTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(othersPaneLayout.createSequentialGroup()
                        .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(planFlagsLabel)
                            .addGroup(othersPaneLayout.createSequentialGroup()
                                .addComponent(restrictedCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(copyrightCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(allowEmitCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(unusedCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(loopPreviewCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(hiddenCheckbox)))
                        .addGap(0, 14, Short.MAX_VALUE)))
                .addContainerGap())
        );
        othersPaneLayout.setVerticalGroup(
            othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(othersPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(unlockSlotIDLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(slotTypeLabel)
                    .addComponent(unlockSlotTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(slotNumberLabel)
                    .addComponent(unlockSlotNumberSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(highlightSoundLabel)
                    .addComponent(highlightSoundSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateAddedLabel)
                    .addComponent(dateAddedSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel)
                    .addComponent(colorRedSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colorGreenSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colorBlueSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(planFlagsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unusedCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loopPreviewCheckbox)
                    .addComponent(allowEmitCheckbox)
                    .addComponent(copyrightCheckbox)
                    .addComponent(restrictedCheckbox)
                    .addComponent(hiddenCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(othersPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(toolTypeLabel)
                    .addComponent(toolTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        itemSettings.addTab("Other", othersPane);

        photoAndEyetoyDataScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        photoDataCheckbox.setText("Photo Data");

        photoIconLabel.setText("Icon:");

        stickerLabel.setText("Sticker:");

        paintingLabel.setText("Painting:");

        photoLabel.setText("Photo:");

        photoLevelLabel.setText("Level:");

        photoLevelTypeLabel.setText("Type:");

        photoLevelNumberLabel.setText("Number:");

        photoLevelNumberSpinner.setModel(new javax.swing.SpinnerNumberModel(0L, 0L, null, 1L));

        levelHashLabel.setText("Level Hash:");

        photoUsersLabel.setText("Users:");

        photoUserList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        photoUserListScrollPane.setViewportView(photoUserList);

        photoUserPsidLabel.setText("PSID:");

        photoUserLabel.setText("User:");

        photoUserBoundsLabel.setText("Bounds:");

        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        jSpinner3.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        jSpinner4.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        jSpinner5.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        removePhotoUserButton.setText("Remove");

        addPhotoUserButton.setText("Add");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(addPhotoUserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(removePhotoUserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(photoUserListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(photoUserBoundsLabel)
                    .addComponent(photoUserLabel)
                    .addComponent(photoUserPsidLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(photoUserTextEntry)
                    .addComponent(photoPsidTextField)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSpinner3, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                            .addComponent(jSpinner2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSpinner4, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .addComponent(jSpinner5))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(photoUserPsidLabel)
                            .addComponent(photoPsidTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(photoUserLabel)
                            .addComponent(photoUserTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(photoUserBoundsLabel)
                            .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinner5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(photoUserListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinner4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addPhotoUserButton)
                    .addComponent(removePhotoUserButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout photoDataPaneLayout = new javax.swing.GroupLayout(photoDataPane);
        photoDataPane.setLayout(photoDataPaneLayout);
        photoDataPaneLayout.setHorizontalGroup(
            photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(photoDataPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(photoDataPaneLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(photoLevelNumberLabel)
                            .addComponent(photoLevelTypeLabel))
                        .addGap(25, 25, 25)
                        .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(photoLevelNumberSpinner)
                            .addComponent(photoLevelTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(photoDataPaneLayout.createSequentialGroup()
                        .addComponent(levelHashLabel)
                        .addGap(18, 18, 18)
                        .addComponent(photoLevelHashTextEntry))
                    .addGroup(photoDataPaneLayout.createSequentialGroup()
                        .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(paintingLabel)
                            .addComponent(stickerLabel)
                            .addComponent(photoIconLabel)
                            .addComponent(photoLabel)
                            .addComponent(photoLevelLabel))
                        .addGap(31, 31, 31)
                        .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stickerResourceTextEntry, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(paintingResourceTextEntry, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(photoResourceTextEntry)
                            .addComponent(photoIconResourceTextEntry)))
                    .addGroup(photoDataPaneLayout.createSequentialGroup()
                        .addComponent(photoUsersLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        photoDataPaneLayout.setVerticalGroup(
            photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(photoDataPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(photoIconLabel)
                    .addComponent(photoIconResourceTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stickerLabel)
                    .addComponent(stickerResourceTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(paintingResourceTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(paintingLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(photoLabel)
                    .addComponent(photoResourceTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(photoLevelLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(photoLevelTypeLabel)
                    .addComponent(photoLevelTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(photoLevelNumberLabel)
                    .addComponent(photoLevelNumberSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(photoDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(levelHashLabel)
                    .addComponent(photoLevelHashTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(photoUsersLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        eyetoyDataCheckbox.setText("Eyetoy Data");

        frameLabel.setText("Frame:");

        alphaMaskLabel.setText("Alpha Mask:");

        outlineLabel.setText("Outline:");

        javax.swing.GroupLayout eyetoyDataPaneLayout = new javax.swing.GroupLayout(eyetoyDataPane);
        eyetoyDataPane.setLayout(eyetoyDataPaneLayout);
        eyetoyDataPaneLayout.setHorizontalGroup(
            eyetoyDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eyetoyDataPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(eyetoyDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(alphaMaskLabel)
                    .addComponent(outlineLabel)
                    .addComponent(frameLabel))
                .addGap(12, 12, 12)
                .addGroup(eyetoyDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(frameTextEntry, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(alphaMaskTextEntry)
                    .addComponent(outlineTextEntry))
                .addContainerGap())
        );
        eyetoyDataPaneLayout.setVerticalGroup(
            eyetoyDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eyetoyDataPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(eyetoyDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(frameLabel)
                    .addComponent(frameTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(eyetoyDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(alphaMaskLabel)
                    .addComponent(alphaMaskTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(eyetoyDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outlineLabel)
                    .addComponent(outlineTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout photoAndEyetoyDataPaneLayout = new javax.swing.GroupLayout(photoAndEyetoyDataPane);
        photoAndEyetoyDataPane.setLayout(photoAndEyetoyDataPaneLayout);
        photoAndEyetoyDataPaneLayout.setHorizontalGroup(
            photoAndEyetoyDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(photoAndEyetoyDataPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(photoAndEyetoyDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(photoDataPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(photoAndEyetoyDataPaneLayout.createSequentialGroup()
                        .addGroup(photoAndEyetoyDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(photoDataCheckbox)
                            .addComponent(eyetoyDataCheckbox))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(eyetoyDataPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        photoAndEyetoyDataPaneLayout.setVerticalGroup(
            photoAndEyetoyDataPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(photoAndEyetoyDataPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(photoDataCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(photoDataPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eyetoyDataCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eyetoyDataPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        photoAndEyetoyDataScrollPane.setViewportView(photoAndEyetoyDataPane);

        itemSettings.addTab("Photo Data", photoAndEyetoyDataScrollPane);

        planLabel.setText("Plan:");

        guidLabel.setText("GUID:");

        uidLabel.setText("UID:");

        categoryIndexLabel.setText("User Category Index:");

        inventoryFlagsLabel.setText("Flags:");

        heartedCheckbox.setText("Hearted");

        cheatCheckbox.setText("Cheat");

        unsavedCheckbox.setText("Unsaved");

        erroredCheckbox.setText("Errored");

        uploadedCheckbox.setText("Uploaded");

        inventoryHiddenCheckbox.setText("Hidden");

        autosavedCheckbox.setText("Autosaved");

        javax.swing.GroupLayout inventoryFlagsContainerLayout = new javax.swing.GroupLayout(inventoryFlagsContainer);
        inventoryFlagsContainer.setLayout(inventoryFlagsContainerLayout);
        inventoryFlagsContainerLayout.setHorizontalGroup(
            inventoryFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inventoryFlagsContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inventoryFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(uploadedCheckbox)
                    .addComponent(heartedCheckbox))
                .addGap(18, 18, 18)
                .addGroup(inventoryFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(unsavedCheckbox)
                    .addComponent(cheatCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(inventoryFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(erroredCheckbox)
                    .addComponent(inventoryHiddenCheckbox))
                .addGap(18, 18, 18)
                .addComponent(autosavedCheckbox)
                .addContainerGap(150, Short.MAX_VALUE))
        );
        inventoryFlagsContainerLayout.setVerticalGroup(
            inventoryFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inventoryFlagsContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inventoryFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(heartedCheckbox)
                    .addComponent(cheatCheckbox)
                    .addComponent(erroredCheckbox)
                    .addComponent(autosavedCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inventoryFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(uploadedCheckbox)
                    .addComponent(unsavedCheckbox)
                    .addComponent(inventoryHiddenCheckbox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout inventoryPaneLayout = new javax.swing.GroupLayout(inventoryPane);
        inventoryPane.setLayout(inventoryPaneLayout);
        inventoryPaneLayout.setHorizontalGroup(
            inventoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inventoryPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inventoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inventoryPaneLayout.createSequentialGroup()
                        .addGroup(inventoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(planLabel)
                            .addComponent(guidLabel)
                            .addComponent(uidLabel))
                        .addGap(97, 97, 97)
                        .addGroup(inventoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(guidSpinner, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(uidSpinner)
                            .addComponent(planTextField)))
                    .addComponent(inventoryFlagsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(inventoryPaneLayout.createSequentialGroup()
                        .addComponent(inventoryFlagsLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(inventoryPaneLayout.createSequentialGroup()
                        .addComponent(categoryIndexLabel)
                        .addGap(18, 18, 18)
                        .addComponent(categoryIndexSpinner)))
                .addContainerGap())
        );
        inventoryPaneLayout.setVerticalGroup(
            inventoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inventoryPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inventoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(planLabel)
                    .addComponent(planTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inventoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(guidLabel)
                    .addComponent(guidSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inventoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(uidLabel)
                    .addComponent(uidSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inventoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(categoryIndexLabel)
                    .addComponent(categoryIndexSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inventoryFlagsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inventoryFlagsContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(59, Short.MAX_VALUE))
        );

        itemSettings.addTab("Inventory", inventoryPane);

        closeButton.setText("Close");

        itemsLabel.setText("Items:");

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
                            .addComponent(itemsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(itemSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addItemButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeItemButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(itemSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(itemsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(slotContainer)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addItemButton)
                    .addComponent(removeItemButton)
                    .addComponent(closeButton))
                .addContainerGap())
        );

        itemSettings.getAccessibleContext().setAccessibleName("DLC");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void removeItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeItemButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_removeItemButtonActionPerformed

    private void addItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItemButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addItemButtonActionPerformed

    private void ucdRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ucdRadioActionPerformed
        this.changeTranslationType(true);
    }//GEN-LAST:event_ucdRadioActionPerformed

    private void translationKeysRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_translationKeysRadioActionPerformed
        this.changeTranslationType(false);
    }//GEN-LAST:event_translationKeysRadioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addItemButton;
    private javax.swing.JButton addPhotoUserButton;
    private javax.swing.JCheckBox allowEmitCheckbox;
    private javax.swing.JLabel alphaMaskLabel;
    private javax.swing.JTextField alphaMaskTextEntry;
    private javax.swing.JCheckBox autosavedCheckbox;
    private javax.swing.JCheckBox backgroundsCheckbox;
    private javax.swing.JCheckBox beardCheckbox;
    private javax.swing.JLabel categoryIndexLabel;
    private javax.swing.JSpinner categoryIndexSpinner;
    private javax.swing.JLabel categoryKeyLabel;
    private javax.swing.JSpinner categoryKeySpinner;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JTextField categoryTextEntry;
    private javax.swing.JComboBox<String> characterMaskCombo;
    private javax.swing.JLabel characterMaskLabel;
    private javax.swing.JCheckBox cheatCheckbox;
    private javax.swing.JButton closeButton;
    private javax.swing.JSpinner colorBlueSpinner;
    private javax.swing.JSpinner colorGreenSpinner;
    private javax.swing.JLabel colorIndexLabel;
    private javax.swing.JSpinner colorIndexSpinner;
    private javax.swing.JLabel colorLabel;
    private javax.swing.JSpinner colorRedSpinner;
    private javax.swing.JCheckBox communityObjectsCheckbox;
    private javax.swing.JCheckBox communityPhotosCheckbox;
    private javax.swing.JCheckBox copyrightCheckbox;
    private javax.swing.JLabel costumeCategoriesLabel;
    private javax.swing.JPanel costumeCategoriesPane;
    private javax.swing.JCheckBox costumeMaterialsCheckbox;
    private javax.swing.JCheckBox costumeToolCheckbox;
    private javax.swing.JCheckBox costumeTweakerCheckbox;
    private javax.swing.JCheckBox costumesCheckbox;
    private javax.swing.JTextField creatorTextEntry;
    private javax.swing.JCheckBox creatureCharactersCheckbox;
    private javax.swing.JCheckBox dangerCheckbox;
    private javax.swing.JLabel dateAddedLabel;
    private javax.swing.JSpinner dateAddedSpinner;
    private javax.swing.JCheckBox decorationsCheckbox;
    private javax.swing.JLabel descKeyLabel;
    private javax.swing.JSpinner descKeySpinner;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionPane;
    private javax.swing.JTextArea descriptionTextEntry;
    private javax.swing.JPanel detailsPane;
    private javax.swing.JScrollPane detailsScrollPane;
    private javax.swing.JCheckBox earthToolCheckbox;
    private javax.swing.JCheckBox editModeToolCheckbox;
    private javax.swing.JCheckBox emittedItemCheckbox;
    private javax.swing.JCheckBox erroredCheckbox;
    private javax.swing.JCheckBox eyesCheckbox;
    private javax.swing.JCheckBox eyetoyCheckbox;
    private javax.swing.JCheckBox eyetoyDataCheckbox;
    private javax.swing.JPanel eyetoyDataPane;
    private javax.swing.JCheckBox feetCheckbox;
    private javax.swing.JCheckBox floodFillCheckbox;
    private javax.swing.JLabel frameLabel;
    private javax.swing.JTextField frameTextEntry;
    private javax.swing.JCheckBox fullCostumeCheckbox;
    private javax.swing.JCheckBox gadgetsCheckbox;
    private javax.swing.JCheckBox gameplayKitsCheckbox;
    private javax.swing.JPanel generalTypesContainer;
    private javax.swing.JLabel generalTypesLabel;
    private javax.swing.JCheckBox glassesCheckbox;
    private javax.swing.JLabel guidLabel;
    private javax.swing.JSpinner guidSpinner;
    private javax.swing.JCheckBox gunItemCheckbox;
    private javax.swing.JCheckBox hairCheckbox;
    private javax.swing.JCheckBox handsCheckbox;
    private javax.swing.JCheckBox headCheckbox;
    private javax.swing.JCheckBox heartedCheckbox;
    private javax.swing.JCheckBox hiddenCheckbox;
    private javax.swing.JLabel highlightSoundLabel;
    private javax.swing.JSpinner highlightSoundSpinner;
    private javax.swing.JTextField iconTextEntry;
    private javax.swing.JCheckBox instrumentCheckbox;
    private javax.swing.JPanel inventoryFlagsContainer;
    private javax.swing.JLabel inventoryFlagsLabel;
    private javax.swing.JCheckBox inventoryHiddenCheckbox;
    private javax.swing.JPanel inventoryPane;
    private javax.swing.JLabel itemIcon;
    private javax.swing.JList<String> itemList;
    private javax.swing.JTabbedPane itemSettings;
    private javax.swing.JLabel itemsLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JSpinner jSpinner4;
    private javax.swing.JSpinner jSpinner5;
    private javax.swing.JCheckBox jointsCheckbox;
    private javax.swing.JPanel lbp1TypeContainer;
    private javax.swing.JLabel lbp1TypesLabel;
    private javax.swing.JPanel lbp2TypeContainer;
    private javax.swing.JLabel lbp2TypesLabel;
    private javax.swing.JPanel lbp3TypesContainer;
    private javax.swing.JLabel lbp3TypesLabel;
    private javax.swing.JCheckBox legsCheckbox;
    private javax.swing.JLabel levelHashLabel;
    private javax.swing.JCheckBox levelKeyCheckbox;
    private javax.swing.JLabel locationKeyLabel;
    private javax.swing.JSpinner locationKeySpinner;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField locationTextEntry;
    private javax.swing.JCheckBox loopPreviewCheckbox;
    private javax.swing.JCheckBox madeByMeCheckbox;
    private javax.swing.JCheckBox madeByOthersCheckbox;
    private javax.swing.JCheckBox materialsCheckbox;
    private javax.swing.JCheckBox moustacheCheckbox;
    private javax.swing.JCheckBox mouthCheckbox;
    private javax.swing.JCheckBox musicCheckbox;
    private javax.swing.JCheckBox neckCheckbox;
    private javax.swing.JCheckBox noseCheckbox;
    private javax.swing.JCheckBox npcCostumeCheckbox;
    private javax.swing.JCheckBox objectsCheckbox;
    private javax.swing.JPanel othersPane;
    private javax.swing.JLabel outfitFlagsLabel;
    private javax.swing.JPanel outfitFlagsPane;
    private javax.swing.JLabel outlineLabel;
    private javax.swing.JTextField outlineTextEntry;
    private javax.swing.JCheckBox paintCheckbox;
    private javax.swing.JLabel paintingLabel;
    private javax.swing.JTextField paintingResourceTextEntry;
    private javax.swing.JPanel photoAndEyetoyDataPane;
    private javax.swing.JScrollPane photoAndEyetoyDataScrollPane;
    private javax.swing.JCheckBox photoDataCheckbox;
    private javax.swing.JPanel photoDataPane;
    private javax.swing.JLabel photoIconLabel;
    private javax.swing.JTextField photoIconResourceTextEntry;
    private javax.swing.JLabel photoLabel;
    private javax.swing.JTextField photoLevelHashTextEntry;
    private javax.swing.JLabel photoLevelLabel;
    private javax.swing.JLabel photoLevelNumberLabel;
    private javax.swing.JSpinner photoLevelNumberSpinner;
    private javax.swing.JComboBox<String> photoLevelTypeCombo;
    private javax.swing.JLabel photoLevelTypeLabel;
    private javax.swing.JTextField photoPsidTextField;
    private javax.swing.JTextField photoResourceTextEntry;
    private javax.swing.JCheckBox photoToolCheckbox;
    private javax.swing.JLabel photoUserBoundsLabel;
    private javax.swing.JLabel photoUserLabel;
    private javax.swing.JList<String> photoUserList;
    private javax.swing.JScrollPane photoUserListScrollPane;
    private javax.swing.JLabel photoUserPsidLabel;
    private javax.swing.JTextField photoUserTextEntry;
    private javax.swing.JLabel photoUsersLabel;
    private javax.swing.JCheckBox photoboothCheckbox;
    private javax.swing.JCheckBox pictureToolsCheckbox;
    private javax.swing.JLabel planFlagsLabel;
    private javax.swing.JLabel planLabel;
    private javax.swing.JTextField planTextField;
    private javax.swing.JCheckBox planToolCheckbox;
    private javax.swing.JComboBox<String> planetTypeCombo;
    private javax.swing.JLabel planetTypeLabel;
    private javax.swing.JCheckBox playerColorsCheckbox;
    private javax.swing.JCheckBox podToolLbp1Checkbox;
    private javax.swing.JCheckBox podToolLbp2Checkbox;
    private javax.swing.JCheckBox podsCheckbox;
    private javax.swing.JButton removeItemButton;
    private javax.swing.JButton removePhotoUserButton;
    private javax.swing.JCheckBox restrictedCheckbox;
    private javax.swing.JCheckBox sackbotMeshCheckbox;
    private javax.swing.JCheckBox sequencerCheckbox;
    private javax.swing.JCheckBox shapesCheckbox;
    private javax.swing.JScrollPane slotContainer;
    private javax.swing.JLabel slotNumberLabel;
    private javax.swing.JLabel slotTypeLabel;
    private javax.swing.JCheckBox soundsCheckbox;
    private javax.swing.JCheckBox specialCostumeCheckbox;
    private javax.swing.JLabel stickerLabel;
    private javax.swing.JTextField stickerResourceTextEntry;
    private javax.swing.JCheckBox stickerToolCheckbox;
    private javax.swing.JCheckBox stickersCheckbox;
    private javax.swing.JLabel titleKeyLabel;
    private javax.swing.JSpinner titleKeySpinner;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextEntry;
    private javax.swing.JComboBox<String> toolTypeCombo;
    private javax.swing.JLabel toolTypeLabel;
    private javax.swing.JCheckBox toolsCheckbox;
    private javax.swing.JCheckBox torsoCheckbox;
    private javax.swing.JLabel translationKeyLabel;
    private javax.swing.JTextField translationKeyTextEntry;
    private javax.swing.JRadioButton translationKeysRadio;
    private javax.swing.ButtonGroup translationTypeButtonGroup;
    private javax.swing.JScrollPane typeScrollPane;
    private javax.swing.JPanel typesPane;
    private javax.swing.JRadioButton ucdRadio;
    private javax.swing.JLabel uidLabel;
    private javax.swing.JSpinner uidSpinner;
    private javax.swing.JLabel unlockSlotIDLabel;
    private javax.swing.JSpinner unlockSlotNumberSpinner;
    private javax.swing.JComboBox<String> unlockSlotTypeCombo;
    private javax.swing.JCheckBox unsavedCheckbox;
    private javax.swing.JCheckBox unusedCheckbox;
    private javax.swing.JCheckBox uploadedCheckbox;
    private javax.swing.JCheckBox userCostumesCheckbox;
    private javax.swing.JCheckBox userObjectsCheckbox;
    private javax.swing.JCheckBox userPlanetCheckbox;
    private javax.swing.JCheckBox userStickersCheckbox;
    private javax.swing.JCheckBox waistCheckbox;
    // End of variables declaration//GEN-END:variables
}
