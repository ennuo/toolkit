package ennuo.toolkit.windows;

import ennuo.craftworld.resources.Plan;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.enums.GameVersion;
import ennuo.craftworld.resources.enums.InventoryItemFlags;
import ennuo.craftworld.resources.enums.InventoryObjectSubType;
import ennuo.craftworld.resources.enums.InventoryObjectType;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.enums.ToolType;
import ennuo.craftworld.resources.structs.InventoryItem;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.resources.structs.SceNpId;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.resources.structs.plan.CreationHistory;
import ennuo.craftworld.resources.structs.plan.EyetoyData;
import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.resources.structs.plan.PhotoData;
import ennuo.craftworld.resources.structs.plan.PhotoMetadata;
import ennuo.craftworld.resources.structs.plan.UserCreatedDetails;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.BigStreamingFart;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.utilities.StringUtils;
import ennuo.toolkit.utilities.Globals;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
    private final DefaultListModel creators = new DefaultListModel();
    private final DefaultListModel photoUsers = new DefaultListModel();
    
    private boolean listenForChanges = true;
    private boolean hasMadeChanges = false;
    
    private JCheckBox[] typeCheckboxes;
    private JCheckBox[] categories;
    
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
            this.emittedItemCheckbox, this.gunItemCheckbox, this.npcCostumeCheckbox, this.instrumentCheckbox, this.podsLbp2Checkbox,
            this.costumeTweakerCheckbox, this.paintCheckbox, this.floodFillCheckbox, this.stickerToolCheckbox, this.costumeToolCheckbox,
            this.planToolCheckbox, this.photoToolCheckbox, this.pictureToolsCheckbox, this.communityPhotosCheckbox, this.communityObjectsCheckbox,
            this.podsLbp1Checkbox, this.podToolLbp1Checkbox, this.editModeToolCheckbox, this.podToolLbp2Checkbox, this.earthToolCheckbox
        };
        
        this.categories = new JCheckBox[] {
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
        
        // These types have unique sub types, so every time one of these 
        // are updated, we have to refresh the UI.
        Arrays.stream(new JCheckBox[] { 
            this.costumesCheckbox, this.playerColorsCheckbox, this.userPlanetCheckbox,
            this.userCostumesCheckbox, this.toolsCheckbox, this.fullCostumeCheckbox,
            this.specialCostumeCheckbox
        }).forEach(checkbox -> {
            checkbox.addActionListener(listener -> this.updateSubTypeVisibility());
        });
        
        /* Disable types that aren't applicable for game
           these items are built for.
        */
        int version = GameVersion.getFlag(this.revision);
        
        if ((version & GameVersion.LBP1) == 0) {
            this.lbp1TypesLabel.setVisible(false);
            this.lbp1TypeContainer.setVisible(false); 
        }
        else {
            this.lbp2TypeContainer.setVisible(false);
            this.lbp2TypesLabel.setVisible(false);
        }
        if ((version & GameVersion.LBP3) == 0) {
            this.lbp3TypesLabel.setVisible(false);
            this.lbp3TypesContainer.setVisible(false);
        } else {
            this.editModeToolCheckbox.setVisible(false);
            this.podToolLbp2Checkbox.setVisible(false);
            this.earthToolCheckbox.setVisible(false);
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
        
        // Disable plan flags if they weren't added yet.
        if (this.revision.head <= 0x334) {
            this.planFlagsLabel.setVisible(false);
            this.planFlagsContainer.setVisible(false);
        }
        
        this.itemList.setModel(this.model);
        this.creatorsList.setModel(this.creators);
        this.photoUserList.setModel(this.photoUsers);
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
        
        this.creationHistoryCheckbox.addActionListener(e -> {
            this.removeCreatorButton.setEnabled(false);
            this.creationHistoryPane.setVisible(this.creationHistoryCheckbox.isSelected());
        });
        
        this.creatorsList.addListSelectionListener(e -> {
            int index = this.creatorsList.getSelectedIndex();
            if (index == -1) this.removeCreatorButton.setEnabled(false);
            else this.removeCreatorButton.setEnabled(true);
        });
        
        this.photoDataCheckbox.addActionListener(e -> {
            this.photoDataPane.setVisible(this.photoDataCheckbox.isSelected());
        });
        
        this.eyetoyDataCheckbox.addActionListener(e -> {
            this.eyetoyDataPane.setVisible(this.eyetoyDataCheckbox.isSelected());
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
    
    private void updateSubTypeVisibility() {
        boolean isCostume = this.costumesCheckbox.isSelected();
        boolean isColour = this.playerColorsCheckbox.isSelected();
        boolean isPlanet = this.userPlanetCheckbox.isSelected();
        boolean isFullCostume = this.fullCostumeCheckbox.isSelected() || this.specialCostumeCheckbox.isSelected();
        boolean isOutfit = this.userCostumesCheckbox.isSelected();
        boolean showCharacterMask = isCostume && this.revision.isLBP3();
        boolean isTool = this.toolsCheckbox.isSelected();
        
        this.subTypesLabel.setVisible(!(!isCostume && !isColour && !isPlanet && !isOutfit && !showCharacterMask));
        
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
        
        this.toolTypeLabel.setVisible(isTool);
        this.toolTypeCombo.setVisible(isTool);
        
    }
    
    private void initializeSubTypes() {
        InventoryDetails details = this.selectedDetails;
        
        boolean isCostume = details.type.contains(InventoryObjectType.COSTUME);
        boolean isColour = details.type.contains(InventoryObjectType.PLAYER_COLOUR);
        boolean isPlanet = details.type.contains(InventoryObjectType.USER_PLANET);
        boolean isFullCostume = ((details.subType & InventoryObjectSubType.FULL_COSTUME) != 0) || 
                                ((details.subType & InventoryObjectSubType.SPECIAL_COSTUME) != 0);
        boolean isOutfit = details.type.contains(InventoryObjectType.USER_COSTUME);
        boolean showCharacterMask = isCostume && this.revision.isLBP3();
        boolean isTool = details.type.contains(InventoryObjectType.TOOL);
        
        this.subTypesLabel.setVisible(!(!isCostume && !isColour && !isPlanet && !isFullCostume && !isOutfit && !showCharacterMask));
        
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
        
        this.toolTypeLabel.setVisible(isTool);
        this.toolTypeCombo.setVisible(isTool);
        
        if (isCostume) {
            for (int i = 0; i < this.categories.length; ++i)
                this.categories[i].setSelected((details.subType & (1 << i)) != 0);
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
    }
    
    private void setItemData() {
        InventoryDetails details = this.selectedDetails;
        
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
        
        this.creators.clear();
        this.removeCreatorButton.setEnabled(false);
        if (details.creationHistory != null && details.creationHistory.creators != null 
                && details.creationHistory.creators.length != 0) {
            this.creationHistoryPane.setVisible(true);
            this.creationHistoryCheckbox.setSelected(true);
            for (String creator : details.creationHistory.creators)
                this.creators.addElement(creator);
        } else {
            this.creationHistoryCheckbox.setSelected(false);
            this.creationHistoryPane.setVisible(false);
        }
        
        // Types tab
        
        InventoryObjectType[] objectTypes = InventoryObjectType.values();
        for (int i = 1; i < objectTypes.length; ++i)
            this.typeCheckboxes[i - 1].setSelected(details.type.contains(objectTypes[i]));
        this.initializeSubTypes();
        
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
        this.restrictedDecorateCheckbox.setSelected((details.flags & InventoryItemFlags.RESTRICTED_POD) != 0);
        this.restrictedLevelCheckbox.setSelected((details.flags & InventoryItemFlags.RESTRICTED_LEVEL) != 0);
        this.loopPreviewCheckbox.setSelected((details.flags & InventoryItemFlags.DISABLE_LOOP_PREVIEW) == 0);
        
        // Photo data tab
        if (details.photoData != null) {
            PhotoData data = details.photoData;
            
            this.photoDataCheckbox.setSelected(true);
            this.photoDataPane.setVisible(true);
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
            this.photoIconResourceTextEntry.setText("");
            this.stickerResourceTextEntry.setText("");
            this.paintingResourceTextEntry.setText("");
            this.photoResourceTextEntry.setText("");
            this.photoLevelTypeCombo.setSelectedIndex(0);
            this.photoLevelNumberSpinner.setValue(0l);
            this.photoLevelHashTextEntry.setText("");
            this.photoDataCheckbox.setSelected(false);
            this.photoDataPane.setVisible(false);
            this.photoDataPane.setVisible(false);
        }
        
        if (details.eyetoyData != null) {
            this.eyetoyDataCheckbox.setSelected(true);
            this.eyetoyDataPane.setVisible(true);
            this.frameTextEntry.setText(details.eyetoyData.frame != null ? details.eyetoyData.frame.toString() : "");
            this.alphaMaskTextEntry.setText(details.eyetoyData.alphaMask != null ? details.eyetoyData.alphaMask.toString() : "");
            this.outlineTextEntry.setText(details.eyetoyData.outline != null ? details.eyetoyData.outline.toString() : "");
        } else {
            this.frameTextEntry.setText("");
            this.alphaMaskTextEntry.setText("");
            this.outlineTextEntry.setText("");
            
            this.eyetoyDataCheckbox.setSelected(false);
            this.eyetoyDataPane.setVisible(false);
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
    
    private ResourceDescriptor getDescriptor(JTextField field, ResourceType type) {
        String resource = field.getText();
        if (StringUtils.isGUID(resource) || StringUtils.isSHA1(resource))
            return new ResourceDescriptor(type, resource);
        return null;
    }
    
    private void saveItem(InventoryDetails details, InventoryItem item) {
        boolean isUsingKeys = (this.revision.isAfterLeerdammerRevision(7) || this.revision.head > 0x2ba);
        boolean isUCD = this.ucdRadio.isSelected();
        
        
        // Temporarily reset translation keys
        details.titleKey = 0;
        details.descriptionKey = 0;
        details.category = 0;
        details.location = 0;
        details.translationTag = "";
        details.locationTag = "";
        details.categoryTag = "";
        
        details.icon = this.getDescriptor(this.iconTextEntry, ResourceType.TEXTURE);
        details.creator = new SceNpId(this.creatorTextEntry.getText());
        
        // Get title and description
        if (isUCD)
            details.userCreatedDetails = new UserCreatedDetails(this.titleTextEntry.getText(), this.descriptionTextEntry.getText());
        else {
            if (isUsingKeys) {
                details.titleKey = (long) this.titleKeySpinner.getValue();
                details.descriptionKey = (long) this.descKeySpinner.getValue();
            } else 
                details.translationTag = this.translationKeyTextEntry.getText();
        }
        
        // Set category/theme of the item
        if (item == null) {
            if (isUsingKeys) {
                details.category = (long) this.categoryKeySpinner.getValue();
                details.location = (long) this.categoryKeySpinner.getValue();
            } else {
                details.categoryTag = this.categoryTextEntry.getText();
                details.locationTag = this.locationTextEntry.getText();
            }
        } else {
            // If we're editing a profile, categories/locations are resolved by index in string table
            details.categoryIndex = (short) this.profile.bigProfile.stringTable.add(this.categoryTextEntry.getText(), 0);
            details.locationIndex = (short) this.profile.bigProfile.stringTable.add(this.locationTextEntry.getText(), 0);
        }
        
        if (this.creationHistoryCheckbox.isSelected()) {
            CreationHistory history = new CreationHistory();
            history.creators = new String[this.creators.size()];
            for (int i = 0; i < this.creators.size(); ++i)
                history.creators[i] = (String) this.creators.getElementAt(i);
            details.creationHistory = history;
        } else details.creationHistory = null;
        
        details.type.clear();
        InventoryObjectType[] objectTypes = InventoryObjectType.values();
        for (int i = 1; i < objectTypes.length; ++i)
            if (this.typeCheckboxes[i - 1].isSelected())
                details.type.add(objectTypes[i]);
        
        details.subType = 0;
        if (details.type.contains(InventoryObjectType.COSTUME)) {
            if (this.madeByMeCheckbox.isSelected()) details.subType |= InventoryObjectSubType.MADE_BY_ME;
            if (this.madeByOthersCheckbox.isSelected()) details.subType |= InventoryObjectSubType.MADE_BY_OTHERS;
            if (this.fullCostumeCheckbox.isSelected()) details.subType |= InventoryObjectSubType.FULL_COSTUME;
            if (this.specialCostumeCheckbox.isSelected()) details.subType |= InventoryObjectSubType.SPECIAL_COSTUME;
            
            if (!this.fullCostumeCheckbox.isSelected() && !this.specialCostumeCheckbox.isSelected()) {
                for (int i = 0; i < this.categories.length; ++i)
                    if (categories[i].isSelected())
                        details.subType |= (1 << i);
            }
            
            if (this.revision.isLBP3()) {
                int mask = this.characterMaskCombo.getSelectedIndex();
                if (mask == 1) details.subType |= InventoryObjectSubType.CREATURE_MASK_GIANT;
                else if (mask == 2) details.subType |= InventoryObjectSubType.CREATURE_MASK_DWARF;
                else if (mask == 3) details.subType |= InventoryObjectSubType.CREATURE_MASK_BIRD;
                else if (mask == 4) details.subType |= InventoryObjectSubType.CREATURE_MASK_QUAD;
            }
        }
        
        if (details.type.contains(InventoryObjectType.USER_PLANET)) {
            int index = this.planetTypeCombo.getSelectedIndex();
            if (index != 0)
                details.subType |= (1 << (index - 1));
        }
        
        if (details.type.contains(InventoryObjectType.PLAYER_COLOUR))
            details.subType = (int) this.colorIndexSpinner.getValue();
        
        details.toolType = (ToolType) this.toolTypeCombo.getSelectedItem();
        
        details.levelUnlockSlotID = new SlotID(
                (SlotType) this.unlockSlotTypeCombo.getSelectedItem(),
                (long) this.unlockSlotNumberSpinner.getValue()
        );
        
        details.highlightSound = (long) this.highlightSoundSpinner.getValue();
        
        details.dateAdded = ((Date)this.dateAddedSpinner.getValue()).getTime() / 1000 * 2;
        
        details.colour = 
                ((Integer)this.colorRedSpinner.getValue()).longValue() << 16 |
                ((Integer)this.colorGreenSpinner.getValue()).longValue() << 8 |
                ((Integer)this.colorBlueSpinner.getValue()).longValue() << 0;

        details.flags = 0;
        if (!this.loopPreviewCheckbox.isSelected())
            details.flags |= InventoryItemFlags.DISABLE_LOOP_PREVIEW;
        if (this.allowEmitCheckbox.isSelected())
            details.flags |= InventoryItemFlags.ALLOW_EMIT;
        if (this.copyrightCheckbox.isSelected())
            details.flags |= InventoryItemFlags.COPYRIGHT;
        if (this.hiddenCheckbox.isSelected())
            details.flags |= InventoryItemFlags.HIDDEN_ITEM; 
        if (this.restrictedDecorateCheckbox.isSelected())
            details.flags |= InventoryItemFlags.RESTRICTED_POD;
        if (this.restrictedLevelCheckbox.isSelected())
            details.flags |= InventoryItemFlags.RESTRICTED_LEVEL;
        if (!this.unusedCheckbox.isSelected())
            details.flags |= InventoryItemFlags.USED;
        
        if (this.photoDataCheckbox.isSelected()) {
            PhotoData data = new PhotoData();
            data.icon = this.getDescriptor(this.photoIconResourceTextEntry, ResourceType.TEXTURE);
            data.sticker = this.getDescriptor(this.stickerResourceTextEntry, ResourceType.TEXTURE);
            data.painting = this.getDescriptor(this.paintingResourceTextEntry, ResourceType.PAINTING);
            data.photoMetadata.photo = this.getDescriptor(this.photoResourceTextEntry, ResourceType.TEXTURE);
            data.photoMetadata.level = new SlotID(
                (SlotType) this.photoLevelTypeCombo.getSelectedItem(),
                (long) this.photoLevelNumberSpinner.getValue()
            );
            
            String levelHash = this.photoLevelHashTextEntry.getText();
            if (levelHash.isEmpty() || !StringUtils.isSHA1(levelHash))
                data.photoMetadata.levelHash = new SHA1();
            else data.photoMetadata.levelHash = new SHA1(levelHash);
            
        } else details.photoData = null;
        
        if (this.eyetoyDataCheckbox.isSelected()) {
            EyetoyData data = new EyetoyData();
            data.alphaMask = this.getDescriptor(this.alphaMaskTextEntry, ResourceType.TEXTURE);
            data.frame = this.getDescriptor(this.frameTextEntry, ResourceType.TEXTURE);
            data.outline = this.getDescriptor(this.outlineTextEntry, ResourceType.TEXTURE);
        } else details.eyetoyData = null;
        
        if (this.profile != null) {
            this.profile.shouldSave = true;
            Toolkit.instance.updateWorkspace();
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
        iconLabel = new javax.swing.JLabel();
        iconTextEntry = new javax.swing.JTextField();
        creatorLabel = new javax.swing.JLabel();
        creatorTextEntry = new javax.swing.JTextField();
        translationTypeLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        translationKeysRadio = new javax.swing.JRadioButton();
        ucdRadio = new javax.swing.JRadioButton();
        translationKeyLabel = new javax.swing.JLabel();
        titleKeyLabel = new javax.swing.JLabel();
        titleDescPane = new javax.swing.JPanel();
        titleKeySpinner = new javax.swing.JSpinner();
        descKeyLabel = new javax.swing.JLabel();
        descKeySpinner = new javax.swing.JSpinner();
        categoryKeyLabel = new javax.swing.JLabel();
        catLocPane = new javax.swing.JPanel();
        categoryKeySpinner = new javax.swing.JSpinner();
        locationKeyLabel = new javax.swing.JLabel();
        locationKeySpinner = new javax.swing.JSpinner();
        categoryLabel = new javax.swing.JLabel();
        categoryTextEntry = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationTextEntry = new javax.swing.JTextField();
        translationKeyTextEntry = new javax.swing.JTextField();
        creationHistoryCheckbox = new javax.swing.JCheckBox();
        creationHistoryPane = new javax.swing.JPanel();
        creatorsLabel = new javax.swing.JLabel();
        creatorsScrollPane = new javax.swing.JScrollPane();
        creatorsList = new javax.swing.JList<>();
        addCreatorButton = new javax.swing.JButton();
        removeCreatorButton = new javax.swing.JButton();
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
        podsLbp1Checkbox = new javax.swing.JCheckBox();
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
        podsLbp2Checkbox = new javax.swing.JCheckBox();
        lbp3TypesLabel = new javax.swing.JLabel();
        lbp3TypesContainer = new javax.swing.JPanel();
        sackbotMeshCheckbox = new javax.swing.JCheckBox();
        creatureCharactersCheckbox = new javax.swing.JCheckBox();
        costumeTweakerCheckbox = new javax.swing.JCheckBox();
        subTypesLabel = new javax.swing.JLabel();
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
        toolTypeLabel = new javax.swing.JLabel();
        toolTypeCombo = new javax.swing.JComboBox(ToolType.values());
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
        planFlagsContainer = new javax.swing.JPanel();
        copyrightCheckbox = new javax.swing.JCheckBox();
        allowEmitCheckbox = new javax.swing.JCheckBox();
        unusedCheckbox = new javax.swing.JCheckBox();
        loopPreviewCheckbox = new javax.swing.JCheckBox();
        hiddenCheckbox = new javax.swing.JCheckBox();
        restrictedDecorateCheckbox = new javax.swing.JCheckBox();
        restrictedLevelCheckbox = new javax.swing.JCheckBox();
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
        saveButton = new javax.swing.JButton();

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

        iconLabel.setText("Icon:");
        iconLabel.setMaximumSize(new java.awt.Dimension(89, 16));
        iconLabel.setMinimumSize(new java.awt.Dimension(89, 16));
        iconLabel.setPreferredSize(new java.awt.Dimension(89, 16));

        creatorLabel.setText("Creator:");
        creatorLabel.setMaximumSize(new java.awt.Dimension(89, 16));
        creatorLabel.setMinimumSize(new java.awt.Dimension(89, 16));
        creatorLabel.setPreferredSize(new java.awt.Dimension(89, 16));

        translationTypeLabel.setText("Translation Type:");

        jPanel1.setMinimumSize(new java.awt.Dimension(0, 22));
        jPanel1.setPreferredSize(new java.awt.Dimension(242, 22));

        translationKeysRadio.setText("Translation Keys");
        translationKeysRadio.setMaximumSize(new java.awt.Dimension(108, 22));
        translationKeysRadio.setMinimumSize(new java.awt.Dimension(108, 22));
        translationKeysRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                translationKeysRadioActionPerformed(evt);
            }
        });

        ucdRadio.setText("User Created Details");
        ucdRadio.setMaximumSize(new java.awt.Dimension(128, 22));
        ucdRadio.setMinimumSize(new java.awt.Dimension(128, 22));
        ucdRadio.setPreferredSize(new java.awt.Dimension(128, 22));
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
                .addComponent(translationKeysRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ucdRadio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(translationKeysRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(ucdRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        translationKeyLabel.setText("Translation Key:");
        translationKeyLabel.setMaximumSize(new java.awt.Dimension(89, 16));
        translationKeyLabel.setMinimumSize(new java.awt.Dimension(89, 16));
        translationKeyLabel.setPreferredSize(new java.awt.Dimension(89, 16));

        titleKeyLabel.setText("Title Key:");
        titleKeyLabel.setMaximumSize(new java.awt.Dimension(89, 16));
        titleKeyLabel.setMinimumSize(new java.awt.Dimension(89, 16));
        titleKeyLabel.setPreferredSize(new java.awt.Dimension(89, 16));

        titleKeySpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), Long.valueOf(4294967295L), Long.valueOf(1L)));
        titleKeySpinner.setToolTipText("");
        titleKeySpinner.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        descKeyLabel.setText("Description Key:");

        descKeySpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), Long.valueOf(4294967295L), Long.valueOf(1L)));

        javax.swing.GroupLayout titleDescPaneLayout = new javax.swing.GroupLayout(titleDescPane);
        titleDescPane.setLayout(titleDescPaneLayout);
        titleDescPaneLayout.setHorizontalGroup(
            titleDescPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titleDescPaneLayout.createSequentialGroup()
                .addComponent(titleKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(descKeyLabel)
                .addGap(12, 12, 12)
                .addComponent(descKeySpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE))
        );
        titleDescPaneLayout.setVerticalGroup(
            titleDescPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titleDescPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(titleKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(descKeyLabel)
                .addComponent(descKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        categoryKeyLabel.setText("Category Key:");
        categoryKeyLabel.setMaximumSize(new java.awt.Dimension(89, 16));
        categoryKeyLabel.setMinimumSize(new java.awt.Dimension(89, 16));
        categoryKeyLabel.setPreferredSize(new java.awt.Dimension(89, 16));

        categoryKeySpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), Long.valueOf(4294967295L), Long.valueOf(1L)));

        locationKeyLabel.setText("Location Key:");
        locationKeyLabel.setMaximumSize(new java.awt.Dimension(85, 16));
        locationKeyLabel.setMinimumSize(new java.awt.Dimension(85, 16));
        locationKeyLabel.setPreferredSize(new java.awt.Dimension(85, 16));

        locationKeySpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), Long.valueOf(4294967295L), Long.valueOf(1L)));

        javax.swing.GroupLayout catLocPaneLayout = new javax.swing.GroupLayout(catLocPane);
        catLocPane.setLayout(catLocPaneLayout);
        catLocPaneLayout.setHorizontalGroup(
            catLocPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(catLocPaneLayout.createSequentialGroup()
                .addComponent(categoryKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(locationKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(locationKeySpinner))
        );
        catLocPaneLayout.setVerticalGroup(
            catLocPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(catLocPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(categoryKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(locationKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(locationKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        categoryLabel.setText("Category:");
        categoryLabel.setMaximumSize(new java.awt.Dimension(89, 16));
        categoryLabel.setMinimumSize(new java.awt.Dimension(89, 16));
        categoryLabel.setPreferredSize(new java.awt.Dimension(89, 16));

        locationLabel.setText("Location:");
        locationLabel.setMaximumSize(new java.awt.Dimension(89, 16));
        locationLabel.setMinimumSize(new java.awt.Dimension(89, 16));
        locationLabel.setPreferredSize(new java.awt.Dimension(89, 16));

        creationHistoryCheckbox.setText("Creation History");

        creatorsLabel.setText("Creators:");

        creatorsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        creatorsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        creatorsScrollPane.setViewportView(creatorsList);

        addCreatorButton.setText("Add");
        addCreatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCreatorButtonActionPerformed(evt);
            }
        });

        removeCreatorButton.setText("Remove");
        removeCreatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeCreatorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout creationHistoryPaneLayout = new javax.swing.GroupLayout(creationHistoryPane);
        creationHistoryPane.setLayout(creationHistoryPaneLayout);
        creationHistoryPaneLayout.setHorizontalGroup(
            creationHistoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(creationHistoryPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(creationHistoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(creationHistoryPaneLayout.createSequentialGroup()
                        .addComponent(creatorsLabel)
                        .addGap(77, 77, 77))
                    .addGroup(creationHistoryPaneLayout.createSequentialGroup()
                        .addComponent(creatorsScrollPane)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(creationHistoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(removeCreatorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addCreatorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        creationHistoryPaneLayout.setVerticalGroup(
            creationHistoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(creationHistoryPaneLayout.createSequentialGroup()
                .addComponent(creatorsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(creationHistoryPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(creationHistoryPaneLayout.createSequentialGroup()
                        .addComponent(addCreatorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeCreatorButton))
                    .addComponent(creatorsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(creatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(translationTypeLabel)
                    .addComponent(translationKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(titleKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(categoryKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(categoryLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iconLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iconTextEntry)
                    .addComponent(creatorTextEntry)
                    .addComponent(titleDescPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(locationTextEntry, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(catLocPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                    .addComponent(categoryTextEntry, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(translationKeyTextEntry)))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(creationHistoryCheckbox)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(creationHistoryPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iconLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iconTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(creatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(creatorTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(translationTypeLabel)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(translationKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(translationKeyTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(titleKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(titleDescPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(categoryKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(catLocPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(categoryLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(categoryTextEntry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(creationHistoryCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(creationHistoryPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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
                            .addComponent(descriptionPane))))
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
                .addGap(5, 5, 5)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        detailsScrollPane.setViewportView(detailsPane);

        itemSettings.addTab("Details", detailsScrollPane);

        typeScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        generalTypesLabel.setText("General Types:");

        materialsCheckbox.setText("Materials");
        materialsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        materialsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        materialsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        objectsCheckbox.setText("Objects");
        objectsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        objectsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        objectsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        decorationsCheckbox.setText("Decorations");
        decorationsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        decorationsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        decorationsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        stickersCheckbox.setText("Stickers");
        stickersCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        stickersCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        stickersCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        costumeMaterialsCheckbox.setText("Skins");
        costumeMaterialsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        costumeMaterialsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        costumeMaterialsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        jointsCheckbox.setText("Joints");
        jointsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        jointsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        jointsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));
        jointsCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jointsCheckboxActionPerformed(evt);
            }
        });

        userObjectsCheckbox.setText("User Objects");
        userObjectsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        userObjectsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        userObjectsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        backgroundsCheckbox.setText("Backgrounds");
        backgroundsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        backgroundsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        backgroundsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        gameplayKitsCheckbox.setText("Gameplay Kits");
        gameplayKitsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        gameplayKitsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        gameplayKitsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        costumesCheckbox.setText("Costumes");
        costumesCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        costumesCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        costumesCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        userStickersCheckbox.setText("User Stickers");
        userStickersCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        userStickersCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        userStickersCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));
        userStickersCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userStickersCheckboxActionPerformed(evt);
            }
        });

        shapesCheckbox.setText("Shapes");
        shapesCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        shapesCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        shapesCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        toolsCheckbox.setText("Tools");
        toolsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        toolsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        toolsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        dangerCheckbox.setText("Danger");
        dangerCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        dangerCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        dangerCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        eyetoyCheckbox.setText("Eyetoy");
        eyetoyCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        eyetoyCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        eyetoyCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        gadgetsCheckbox.setText("Gadgets");
        gadgetsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        gadgetsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        gadgetsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        playerColorsCheckbox.setText("Colors");
        playerColorsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        playerColorsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        playerColorsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        userCostumesCheckbox.setText("User Costumes");
        userCostumesCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        userCostumesCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        userCostumesCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        musicCheckbox.setText("Music");
        musicCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        musicCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        musicCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        soundsCheckbox.setText("Sounds");
        soundsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        soundsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        soundsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        photoboothCheckbox.setText("Photobooth");
        photoboothCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        photoboothCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        photoboothCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        javax.swing.GroupLayout generalTypesContainerLayout = new javax.swing.GroupLayout(generalTypesContainer);
        generalTypesContainer.setLayout(generalTypesContainerLayout);
        generalTypesContainerLayout.setHorizontalGroup(
            generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, generalTypesContainerLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalTypesContainerLayout.createSequentialGroup()
                        .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(decorationsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(materialsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(objectsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(stickersCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(costumesCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gameplayKitsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jointsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(userObjectsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(backgroundsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(userStickersCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(shapesCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(costumeMaterialsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalTypesContainerLayout.createSequentialGroup()
                        .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gadgetsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(eyetoyCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(toolsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dangerCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(userCostumesCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(musicCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(soundsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(photoboothCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(playerColorsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        generalTypesContainerLayout.setVerticalGroup(
            generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalTypesContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(materialsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jointsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dangerCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(musicCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(objectsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userObjectsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eyetoyCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(soundsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(decorationsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(backgroundsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gadgetsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(photoboothCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stickersCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameplayKitsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toolsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(costumesCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userStickersCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userCostumesCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(generalTypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(costumeMaterialsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(shapesCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(playerColorsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lbp1TypesLabel.setText("LBP1 Types:");

        paintCheckbox.setText("Paint");
        paintCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        paintCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        paintCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        planToolCheckbox.setText("Plan Tool");
        planToolCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        planToolCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        planToolCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        communityPhotosCheckbox.setText("C.Photo Tools");
        communityPhotosCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        communityPhotosCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        communityPhotosCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        floodFillCheckbox.setText("Flood Fill");
        floodFillCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        floodFillCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        floodFillCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        photoToolCheckbox.setText("Photo Tool");
        photoToolCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        photoToolCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        photoToolCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        stickerToolCheckbox.setText("Sticker Tools");
        stickerToolCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        stickerToolCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        stickerToolCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        pictureToolsCheckbox.setText("Picture Tools");
        pictureToolsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        pictureToolsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        pictureToolsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        costumeToolCheckbox.setText("Costume Tool");
        costumeToolCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        costumeToolCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        costumeToolCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        podToolLbp1Checkbox.setText("Pod Tool");
        podToolLbp1Checkbox.setMaximumSize(new java.awt.Dimension(120, 20));
        podToolLbp1Checkbox.setMinimumSize(new java.awt.Dimension(120, 20));
        podToolLbp1Checkbox.setPreferredSize(new java.awt.Dimension(120, 20));

        communityObjectsCheckbox.setText("C.Object Tools");
        communityObjectsCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        communityObjectsCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        communityObjectsCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        podsLbp1Checkbox.setText("Pods");
        podsLbp1Checkbox.setMaximumSize(new java.awt.Dimension(120, 20));
        podsLbp1Checkbox.setMinimumSize(new java.awt.Dimension(120, 20));
        podsLbp1Checkbox.setPreferredSize(new java.awt.Dimension(120, 20));

        javax.swing.GroupLayout lbp1TypeContainerLayout = new javax.swing.GroupLayout(lbp1TypeContainer);
        lbp1TypeContainer.setLayout(lbp1TypeContainerLayout);
        lbp1TypeContainerLayout.setHorizontalGroup(
            lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp1TypeContainerLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(communityPhotosCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(planToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(paintCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pictureToolsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stickerToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(communityObjectsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(photoToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(lbp1TypeContainerLayout.createSequentialGroup()
                        .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(costumeToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(floodFillCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(podsLbp1Checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(podToolLbp1Checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        lbp1TypeContainerLayout.setVerticalGroup(
            lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp1TypeContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(paintCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stickerToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(floodFillCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(podsLbp1Checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(planToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pictureToolsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(photoToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(podToolLbp1Checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(lbp1TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(communityPhotosCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(communityObjectsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(costumeToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lbp2TypesLabel.setText("LBP2 Types:");

        sequencerCheckbox.setText("Sequencer");
        sequencerCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        sequencerCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        sequencerCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));
        sequencerCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequencerCheckboxActionPerformed(evt);
            }
        });

        gunItemCheckbox.setText("Gun Item");
        gunItemCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        gunItemCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        gunItemCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        editModeToolCheckbox.setText("Edit Mode Tool");
        editModeToolCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        editModeToolCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        editModeToolCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        userPlanetCheckbox.setText("Planets");
        userPlanetCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        userPlanetCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        userPlanetCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        npcCostumeCheckbox.setText("NPC Costume");
        npcCostumeCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        npcCostumeCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        npcCostumeCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        levelKeyCheckbox.setText("Level Key");
        levelKeyCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        levelKeyCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        levelKeyCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        instrumentCheckbox.setText("Instrument");
        instrumentCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        instrumentCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        instrumentCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        emittedItemCheckbox.setText("Emitted");
        emittedItemCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        emittedItemCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        emittedItemCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        earthToolCheckbox.setText("Earth Tool");
        earthToolCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        earthToolCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        earthToolCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        podToolLbp2Checkbox.setText("Pod Tool");
        podToolLbp2Checkbox.setMaximumSize(new java.awt.Dimension(120, 20));
        podToolLbp2Checkbox.setMinimumSize(new java.awt.Dimension(120, 20));
        podToolLbp2Checkbox.setPreferredSize(new java.awt.Dimension(120, 20));

        podsLbp2Checkbox.setText("Pods");
        podsLbp2Checkbox.setMaximumSize(new java.awt.Dimension(120, 20));
        podsLbp2Checkbox.setMinimumSize(new java.awt.Dimension(120, 20));
        podsLbp2Checkbox.setPreferredSize(new java.awt.Dimension(120, 20));

        javax.swing.GroupLayout lbp2TypeContainerLayout = new javax.swing.GroupLayout(lbp2TypeContainer);
        lbp2TypeContainer.setLayout(lbp2TypeContainerLayout);
        lbp2TypeContainerLayout.setHorizontalGroup(
            lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp2TypeContainerLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sequencerCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gunItemCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editModeToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userPlanetCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(npcCostumeCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(podToolLbp2Checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(lbp2TypeContainerLayout.createSequentialGroup()
                        .addComponent(instrumentCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(podsLbp2Checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(earthToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(lbp2TypeContainerLayout.createSequentialGroup()
                        .addComponent(levelKeyCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(emittedItemCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        lbp2TypeContainerLayout.setVerticalGroup(
            lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp2TypeContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sequencerCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userPlanetCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(levelKeyCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(emittedItemCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gunItemCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(npcCostumeCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(instrumentCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(podsLbp2Checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(lbp2TypeContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editModeToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(podToolLbp2Checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(earthToolCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lbp3TypesLabel.setText("LBP3 Types:");

        sackbotMeshCheckbox.setText("Sackbot Mesh");
        sackbotMeshCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        sackbotMeshCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        sackbotMeshCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        creatureCharactersCheckbox.setText("Creatures");
        creatureCharactersCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        creatureCharactersCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        creatureCharactersCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        costumeTweakerCheckbox.setText("Costume Tweaker Tool");
        costumeTweakerCheckbox.setMaximumSize(new java.awt.Dimension(120, 20));
        costumeTweakerCheckbox.setMinimumSize(new java.awt.Dimension(120, 20));
        costumeTweakerCheckbox.setPreferredSize(new java.awt.Dimension(120, 20));

        javax.swing.GroupLayout lbp3TypesContainerLayout = new javax.swing.GroupLayout(lbp3TypesContainer);
        lbp3TypesContainer.setLayout(lbp3TypesContainerLayout);
        lbp3TypesContainerLayout.setHorizontalGroup(
            lbp3TypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp3TypesContainerLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(sackbotMeshCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(creatureCharactersCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(costumeTweakerCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        lbp3TypesContainerLayout.setVerticalGroup(
            lbp3TypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lbp3TypesContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lbp3TypesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sackbotMeshCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(creatureCharactersCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(costumeTweakerCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        subTypesLabel.setText("Subtypes:");

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
                .addGap(2, 2, 2)
                .addGroup(costumeCategoriesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(feetCheckbox)
                    .addComponent(moustacheCheckbox)
                    .addComponent(neckCheckbox)
                    .addComponent(waistCheckbox)
                    .addComponent(hairCheckbox))
                .addGap(2, 2, 2)
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

        toolTypeLabel.setText("Tool Type:");

        toolTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolTypeComboActionPerformed(evt);
            }
        });

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
                    .addComponent(outfitFlagsPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(typesPaneLayout.createSequentialGroup()
                        .addGroup(typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lbp2TypeContainer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(generalTypesLabel)
                            .addComponent(lbp1TypesLabel)
                            .addComponent(lbp2TypesLabel)
                            .addComponent(lbp1TypeContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbp3TypesLabel)
                            .addComponent(lbp3TypesContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(subTypesLabel)
                            .addComponent(costumeCategoriesPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(typesPaneLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(costumeCategoriesLabel))
                            .addComponent(generalTypesContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(typesPaneLayout.createSequentialGroup()
                        .addComponent(toolTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(toolTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                .addComponent(subTypesLabel)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(typesPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(toolTypeLabel)
                    .addComponent(toolTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        typeScrollPane.setViewportView(typesPane);

        itemSettings.addTab("Type", typeScrollPane);

        unlockSlotIDLabel.setText("Unlock Slot ID:");

        slotTypeLabel.setText("Type:");

        slotNumberLabel.setText("Number:");

        unlockSlotNumberSpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), Long.valueOf(4294967295L), Long.valueOf(1L)));

        highlightSoundLabel.setText("Highlight Sound:");

        dateAddedLabel.setText("Date Added:");

        dateAddedSpinner.setModel(new javax.swing.SpinnerDateModel());

        colorLabel.setText("Color (RGB):");

        colorRedSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        colorGreenSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        colorBlueSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        highlightSoundSpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), Long.valueOf(4294967295L), Long.valueOf(1L)));

        planFlagsLabel.setText("Flags:");

        copyrightCheckbox.setText("Copyright");
        copyrightCheckbox.setToolTipText("Indicates that another player created this object and locked it.");

        allowEmitCheckbox.setText("Allow Emit");
        allowEmitCheckbox.setToolTipText("Indicates that this item can be emitted.");

        unusedCheckbox.setText("Unused");
        unusedCheckbox.setToolTipText("Indicates that this item has never been used by the player.");

        loopPreviewCheckbox.setText("Loop Preview");
        loopPreviewCheckbox.setToolTipText("Indicates that the highlight sound of this item can repeat indefinitely");

        hiddenCheckbox.setText("Hidden");

        restrictedDecorateCheckbox.setText("Restricted (Decorate)");
        restrictedDecorateCheckbox.setToolTipText("Indicates that this item is limited to decorate menus (adventure maps and planet decorations)");

        restrictedLevelCheckbox.setText("Restricted (Level)");
        restrictedLevelCheckbox.setToolTipText("Indicates that the item cannot be used in decorate menus, (pod decoration and adventure decoration)");

        javax.swing.GroupLayout planFlagsContainerLayout = new javax.swing.GroupLayout(planFlagsContainer);
        planFlagsContainer.setLayout(planFlagsContainerLayout);
        planFlagsContainerLayout.setHorizontalGroup(
            planFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(planFlagsContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(planFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(restrictedDecorateCheckbox)
                    .addComponent(loopPreviewCheckbox))
                .addGap(6, 6, 6)
                .addGroup(planFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(allowEmitCheckbox)
                    .addComponent(restrictedLevelCheckbox))
                .addGap(6, 6, 6)
                .addGroup(planFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(unusedCheckbox)
                    .addGroup(planFlagsContainerLayout.createSequentialGroup()
                        .addComponent(copyrightCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(hiddenCheckbox)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        planFlagsContainerLayout.setVerticalGroup(
            planFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(planFlagsContainerLayout.createSequentialGroup()
                .addGroup(planFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(copyrightCheckbox)
                    .addComponent(loopPreviewCheckbox)
                    .addComponent(allowEmitCheckbox)
                    .addComponent(hiddenCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(planFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(restrictedDecorateCheckbox)
                    .addComponent(restrictedLevelCheckbox)
                    .addComponent(unusedCheckbox)))
        );

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
                                .addComponent(colorRedSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(colorGreenSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(colorBlueSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(dateAddedSpinner)
                            .addComponent(highlightSoundSpinner)))
                    .addGroup(othersPaneLayout.createSequentialGroup()
                        .addComponent(planFlagsLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(planFlagsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(planFlagsContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        itemSettings.addTab("Other", othersPane);

        photoAndEyetoyDataScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        photoDataCheckbox.setText("Photo Data");
        photoDataCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                photoDataCheckboxActionPerformed(evt);
            }
        });

        photoIconLabel.setText("Icon:");

        stickerLabel.setText("Sticker:");

        paintingLabel.setText("Painting:");

        photoLabel.setText("Photo:");

        photoLevelLabel.setText("Level:");

        photoLevelTypeLabel.setText("Type:");

        photoLevelNumberLabel.setText("Number:");

        photoLevelNumberSpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), Long.valueOf(4294967295L), Long.valueOf(1L)));

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
                .addContainerGap(197, Short.MAX_VALUE))
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
                .addContainerGap(129, Short.MAX_VALUE))
        );

        itemSettings.addTab("Inventory", inventoryPane);

        closeButton.setText("Close");

        itemsLabel.setText("Items:");

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
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
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(itemSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(itemsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(slotContainer)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addItemButton)
                    .addComponent(removeItemButton)
                    .addComponent(closeButton)
                    .addComponent(saveButton))
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

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        this.saveItem(this.selectedDetails, this.selectedItem);
    }//GEN-LAST:event_saveButtonActionPerformed

    private void toolTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolTypeComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_toolTypeComboActionPerformed

    private void photoDataCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_photoDataCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_photoDataCheckboxActionPerformed

    private void sequencerCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequencerCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sequencerCheckboxActionPerformed

    private void jointsCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jointsCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jointsCheckboxActionPerformed

    private void userStickersCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userStickersCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_userStickersCheckboxActionPerformed

    private void addCreatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCreatorButtonActionPerformed
        String creator = JOptionPane.showInputDialog(this, "Creator", "MM_Studio");
        if (creator == null) return;
        if (creator.length() > 0x14)
            creator = creator.substring(0, 0x14);
        
        // Use case-insensitive comparison to see if the user is already in the list.
        String upper = creator.toUpperCase();
        for (int i = 0; i < this.creators.size(); ++i)
            if (((String)this.creators.getElementAt(i)).toUpperCase().equals(upper))
                return;
        
        if (this.creators.size() == 0)
            this.removeCreatorButton.setEnabled(true);
        
        this.creators.addElement(creator);
    }//GEN-LAST:event_addCreatorButtonActionPerformed

    private void removeCreatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeCreatorButtonActionPerformed
        int index = this.creatorsList.getSelectedIndex();
        if (index == -1) return;
        if (this.creators.size() - 1 != 0) {
            if (index == 0)
                this.creatorsList.setSelectedIndex(index + 1);
            else
                this.creatorsList.setSelectedIndex(index - 1);   
        }
        
        this.creators.remove(index);
        
        if (this.creators.size() == 0)
            this.removeCreatorButton.setEnabled(false);
    }//GEN-LAST:event_removeCreatorButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCreatorButton;
    private javax.swing.JButton addItemButton;
    private javax.swing.JButton addPhotoUserButton;
    private javax.swing.JCheckBox allowEmitCheckbox;
    private javax.swing.JLabel alphaMaskLabel;
    private javax.swing.JTextField alphaMaskTextEntry;
    private javax.swing.JCheckBox autosavedCheckbox;
    private javax.swing.JCheckBox backgroundsCheckbox;
    private javax.swing.JCheckBox beardCheckbox;
    private javax.swing.JPanel catLocPane;
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
    private javax.swing.JCheckBox creationHistoryCheckbox;
    private javax.swing.JPanel creationHistoryPane;
    private javax.swing.JLabel creatorLabel;
    private javax.swing.JTextField creatorTextEntry;
    private javax.swing.JLabel creatorsLabel;
    private javax.swing.JList<String> creatorsList;
    private javax.swing.JScrollPane creatorsScrollPane;
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
    private javax.swing.JLabel iconLabel;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
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
    private javax.swing.JPanel planFlagsContainer;
    private javax.swing.JLabel planFlagsLabel;
    private javax.swing.JLabel planLabel;
    private javax.swing.JTextField planTextField;
    private javax.swing.JCheckBox planToolCheckbox;
    private javax.swing.JComboBox<String> planetTypeCombo;
    private javax.swing.JLabel planetTypeLabel;
    private javax.swing.JCheckBox playerColorsCheckbox;
    private javax.swing.JCheckBox podToolLbp1Checkbox;
    private javax.swing.JCheckBox podToolLbp2Checkbox;
    private javax.swing.JCheckBox podsLbp1Checkbox;
    private javax.swing.JCheckBox podsLbp2Checkbox;
    private javax.swing.JButton removeCreatorButton;
    private javax.swing.JButton removeItemButton;
    private javax.swing.JButton removePhotoUserButton;
    private javax.swing.JCheckBox restrictedDecorateCheckbox;
    private javax.swing.JCheckBox restrictedLevelCheckbox;
    private javax.swing.JCheckBox sackbotMeshCheckbox;
    private javax.swing.JButton saveButton;
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
    private javax.swing.JLabel subTypesLabel;
    private javax.swing.JPanel titleDescPane;
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
    private javax.swing.JLabel translationTypeLabel;
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
