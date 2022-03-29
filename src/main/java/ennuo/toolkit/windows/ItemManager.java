package ennuo.toolkit.windows;

import ennuo.craftworld.resources.Plan;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.resources.structs.plan.UserCreatedDetails;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.utilities.Globals;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;

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
    
    
    private FileEntry entry;
    private Plan plan;
    
    private ArrayList<InventoryDetails> items;
    private InventoryDetails selectedItem;
    private final DefaultListModel model = new DefaultListModel();
    
    public ItemManager(FileEntry entry, Plan plan) {
        this.entry = entry;
        this.items = new ArrayList<>(1);
        this.plan = plan;
        this.items.add(plan.details);
        this.setup();
        
        
        /* Remove the list related elemenets, since we're only dealing with a single item. */
        
        this.itemsLabel.getParent().remove(this.itemsLabel);
        this.itemList.getParent().remove(this.itemList);
        this.addItemButton.getParent().remove(this.addItemButton);
        this.removeItemButton.getParent().remove(this.removeItemButton);
        
        this.setSize(this.itemSettings.getSize().width, this.getSize().height);
        this.setContentPane(this.itemSettings);
    }
    
    /**
     * Debug
     */
    public ItemManager() {
        this.items = new ArrayList<>();
            
        Arrays.asList(1046484, 412572, 1113767, 1022588, 1022587).stream().forEach(GUID -> {
            byte[] data = Globals.extractFile(GUID);
            Plan plan = new Serializer(new Resource(data).handle).struct(null, Plan.class);
            this.items.add(plan.details);
        });
       

        this.setup();
    }
    
    private void setup() {
        this.initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        this.setResizable(false);
        this.typePane.getVerticalScrollBar().setUnitIncrement(15);
        
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
            this.selectedItem = this.items.get(index);
            this.setItemData();
        });
        
        this.addItemButton.addActionListener(e -> {
            if (this.items.size() == 0) {
                this.removeItemButton.setEnabled(true);
                this.itemSettings.setEnabled(true);
                this.itemSettings.setSelectedIndex(0);
            }
            
            InventoryDetails item = new InventoryDetails();

            this.items.add(item);
            this.model.addElement(item);
            this.itemList.setSelectedValue(item, true);
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
        
        this.itemList.setSelectedIndex(0);
    }
    
    private void updateTranslations() {
        InventoryDetails details = this.selectedItem;
        if (details.titleKey == 0) {
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
            this.descriptionTextEntry.setText("A valid translation table needs to be loaded for the title and description to appear. Alternatively, remove the translation key, and set your own title/description.");

            if (Globals.LAMS != null) {
                this.titleTextEntry.setText(Globals.LAMS.translate(details.titleKey));
                this.descriptionTextEntry.setText(Globals.LAMS.translate(details.descriptionKey));   
            }
        }
    }
    
    private void resetIcon() {
        this.itemIcon.setIcon(null);
        this.itemIcon.setText("No icon available.");
    }
    
    private void setItemData() {
        InventoryDetails details = this.selectedItem;
        
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
        
        this.titleKeySpinner.setValue(details.titleKey);
        this.descKeySpinner.setValue(details.descriptionKey);
        this.categoryTextEntry.setText(details.category + "");
        this.locationTextEntry.setText(details.location + "");
        
        this.updateTranslations();
    }
  
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        slotContainer = new javax.swing.JScrollPane();
        itemList = new javax.swing.JList<>();
        addItemButton = new javax.swing.JButton();
        removeItemButton = new javax.swing.JButton();
        itemSettings = new javax.swing.JTabbedPane();
        detailsPane = new javax.swing.JPanel();
        itemIcon = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        titleTextEntry = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionPane = new javax.swing.JScrollPane();
        descriptionTextEntry = new javax.swing.JTextArea();
        iconLabel = new javax.swing.JLabel();
        iconTextEntry = new javax.swing.JTextField();
        creatorLabel = new javax.swing.JLabel();
        creatorTextEntry = new javax.swing.JTextField();
        titleKeyLabel = new javax.swing.JLabel();
        descKeyLabel = new javax.swing.JLabel();
        titleKeySpinner = new javax.swing.JSpinner();
        descKeySpinner = new javax.swing.JSpinner();
        categoryLabel = new javax.swing.JLabel();
        categoryTextEntry = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationTextEntry = new javax.swing.JTextField();
        typePane = new javax.swing.JScrollPane();
        typesContainer = new javax.swing.JPanel();
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
        closeButton = new javax.swing.JButton();
        itemsLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Item Manager");

        itemList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        slotContainer.setViewportView(itemList);

        addItemButton.setText("Add Item");

        removeItemButton.setText("Remove");

        itemIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        itemIcon.setText("No icon available.");

        titleLabel.setText("Title:");

        descriptionLabel.setText("Description:");

        descriptionTextEntry.setColumns(20);
        descriptionTextEntry.setLineWrap(true);
        descriptionTextEntry.setRows(5);
        descriptionTextEntry.setWrapStyleWord(true);
        descriptionPane.setViewportView(descriptionTextEntry);

        iconLabel.setText("Icon:");

        creatorLabel.setText("Creator:");

        titleKeyLabel.setText("Title Key:");

        descKeyLabel.setText("Desc. Key:");

        titleKeySpinner.setModel(new javax.swing.SpinnerNumberModel(0L, 0L, null, 1L));

        descKeySpinner.setModel(new javax.swing.SpinnerNumberModel(0L, 0L, null, 1L));

        categoryLabel.setText("Category:");

        locationLabel.setText("Location:");

        javax.swing.GroupLayout detailsPaneLayout = new javax.swing.GroupLayout(detailsPane);
        detailsPane.setLayout(detailsPaneLayout);
        detailsPaneLayout.setHorizontalGroup(
            detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                            .addComponent(descriptionPane, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)))
                    .addGroup(detailsPaneLayout.createSequentialGroup()
                        .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(creatorLabel)
                            .addComponent(iconLabel)
                            .addComponent(titleKeyLabel)
                            .addComponent(descKeyLabel))
                        .addGap(42, 42, 42)
                        .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(creatorTextEntry, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(detailsPaneLayout.createSequentialGroup()
                                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(titleKeySpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                                    .addComponent(descKeySpinner))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(categoryLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(locationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(categoryTextEntry, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                                    .addComponent(locationTextEntry)))
                            .addComponent(iconTextEntry, javax.swing.GroupLayout.Alignment.TRAILING))))
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
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iconLabel)
                    .addComponent(iconTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(creatorLabel)
                    .addComponent(creatorTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleKeyLabel)
                    .addComponent(titleKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(categoryLabel)
                    .addComponent(categoryTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(descKeyLabel)
                        .addComponent(descKeySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(detailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(locationTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(locationLabel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        itemSettings.addTab("Details", detailsPane);

        typePane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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
                    .addComponent(communityPhotosCheckbox))
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
                .addComponent(communityPhotosCheckbox)
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

        javax.swing.GroupLayout typesContainerLayout = new javax.swing.GroupLayout(typesContainer);
        typesContainer.setLayout(typesContainerLayout);
        typesContainerLayout.setHorizontalGroup(
            typesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typesContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(typesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lbp2TypeContainer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, typesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(typesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lbp1TypeContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(generalTypesLabel)
                            .addComponent(lbp1TypesLabel)
                            .addComponent(generalTypesContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(lbp2TypesLabel))
                    .addComponent(lbp3TypesLabel)
                    .addComponent(lbp3TypesContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        typesContainerLayout.setVerticalGroup(
            typesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typesContainerLayout.createSequentialGroup()
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        typePane.setViewportView(typesContainer);

        itemSettings.addTab("Type", typePane);

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
                        .addComponent(itemSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
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
                    .addComponent(itemSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addItemButton;
    private javax.swing.JCheckBox backgroundsCheckbox;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JTextField categoryTextEntry;
    private javax.swing.JButton closeButton;
    private javax.swing.JCheckBox communityPhotosCheckbox;
    private javax.swing.JCheckBox costumeMaterialsCheckbox;
    private javax.swing.JCheckBox costumeToolCheckbox;
    private javax.swing.JCheckBox costumeTweakerCheckbox;
    private javax.swing.JCheckBox costumesCheckbox;
    private javax.swing.JLabel creatorLabel;
    private javax.swing.JTextField creatorTextEntry;
    private javax.swing.JCheckBox creatureCharactersCheckbox;
    private javax.swing.JCheckBox dangerCheckbox;
    private javax.swing.JCheckBox decorationsCheckbox;
    private javax.swing.JLabel descKeyLabel;
    private javax.swing.JSpinner descKeySpinner;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionPane;
    private javax.swing.JTextArea descriptionTextEntry;
    private javax.swing.JPanel detailsPane;
    private javax.swing.JCheckBox earthToolCheckbox;
    private javax.swing.JCheckBox editModeToolCheckbox;
    private javax.swing.JCheckBox emittedItemCheckbox;
    private javax.swing.JCheckBox eyetoyCheckbox;
    private javax.swing.JCheckBox floodFillCheckbox;
    private javax.swing.JCheckBox gadgetsCheckbox;
    private javax.swing.JCheckBox gameplayKitsCheckbox;
    private javax.swing.JPanel generalTypesContainer;
    private javax.swing.JLabel generalTypesLabel;
    private javax.swing.JCheckBox gunItemCheckbox;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JTextField iconTextEntry;
    private javax.swing.JCheckBox instrumentCheckbox;
    private javax.swing.JLabel itemIcon;
    private javax.swing.JList<String> itemList;
    private javax.swing.JTabbedPane itemSettings;
    private javax.swing.JLabel itemsLabel;
    private javax.swing.JCheckBox jointsCheckbox;
    private javax.swing.JPanel lbp1TypeContainer;
    private javax.swing.JLabel lbp1TypesLabel;
    private javax.swing.JPanel lbp2TypeContainer;
    private javax.swing.JLabel lbp2TypesLabel;
    private javax.swing.JPanel lbp3TypesContainer;
    private javax.swing.JLabel lbp3TypesLabel;
    private javax.swing.JCheckBox levelKeyCheckbox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField locationTextEntry;
    private javax.swing.JCheckBox materialsCheckbox;
    private javax.swing.JCheckBox musicCheckbox;
    private javax.swing.JCheckBox npcCostumeCheckbox;
    private javax.swing.JCheckBox objectsCheckbox;
    private javax.swing.JCheckBox paintCheckbox;
    private javax.swing.JCheckBox photoToolCheckbox;
    private javax.swing.JCheckBox photoboothCheckbox;
    private javax.swing.JCheckBox pictureToolsCheckbox;
    private javax.swing.JCheckBox planToolCheckbox;
    private javax.swing.JCheckBox playerColorsCheckbox;
    private javax.swing.JCheckBox podToolLbp1Checkbox;
    private javax.swing.JCheckBox podToolLbp2Checkbox;
    private javax.swing.JCheckBox podsCheckbox;
    private javax.swing.JButton removeItemButton;
    private javax.swing.JCheckBox sackbotMeshCheckbox;
    private javax.swing.JCheckBox sequencerCheckbox;
    private javax.swing.JCheckBox shapesCheckbox;
    private javax.swing.JScrollPane slotContainer;
    private javax.swing.JCheckBox soundsCheckbox;
    private javax.swing.JCheckBox stickerToolCheckbox;
    private javax.swing.JCheckBox stickersCheckbox;
    private javax.swing.JLabel titleKeyLabel;
    private javax.swing.JSpinner titleKeySpinner;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextEntry;
    private javax.swing.JCheckBox toolsCheckbox;
    private javax.swing.JScrollPane typePane;
    private javax.swing.JPanel typesContainer;
    private javax.swing.JCheckBox userCostumesCheckbox;
    private javax.swing.JCheckBox userObjectsCheckbox;
    private javax.swing.JCheckBox userPlanetCheckbox;
    private javax.swing.JCheckBox userStickersCheckbox;
    // End of variables declaration//GEN-END:variables
}
