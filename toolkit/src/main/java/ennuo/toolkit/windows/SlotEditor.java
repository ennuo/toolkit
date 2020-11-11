package ennuo.toolkit.windows;

import ennuo.craftworld.types.FileArchive;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Compressor;
import ennuo.craftworld.memory.FileIO;
import ennuo.craftworld.memory.Images;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.memory.Vector4f;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.enums.GameMode;
import ennuo.craftworld.resources.enums.LevelType;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.swing.FileData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import ennuo.craftworld.resources.enums.Crater;

public class SlotEditor extends javax.swing.JFrame {
    
    ArrayList<Slot> slotInstances = new ArrayList<Slot>();

    Vector slots = new Vector();
    final DefaultComboBoxModel model = new DefaultComboBoxModel(slots);
    
    Toolkit toolkit;
    FileEntry entry;
    
    private boolean isSlotFile = false;
    private boolean madeChanges = false;
    
    private int internalCount = 0;
    private int revision = 3;
    
    public SlotEditor(Toolkit toolkit, FileEntry entry, int revision) { 
        this(toolkit, entry);
        this.revision = revision;
    } 
    
    public SlotEditor(Toolkit toolkit, FileEntry entry) {
        isSlotFile = false;
        this.toolkit = toolkit; this.entry = entry;
        initComponents();
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        setTitle("Slot Editor");
        
        slotInstances.add(entry.slot);
        slots.add(entry.slot.title);
        
        combo.setEnabled(false);
        combo.setSelectedIndex(0);
        
        loadSlotAt(0);
    }
    
    public SlotEditor(Toolkit toolkit, FileEntry entry, boolean isSlotsFile, int revision) {
        this(toolkit, entry, isSlotsFile);
        this.revision = revision;
    }
    
    public SlotEditor(Toolkit toolkit, FileEntry entry, boolean isSlotsFile) {
        this.toolkit = toolkit; this.entry = entry;
        this.isSlotFile = isSlotsFile;
        initComponents();
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        setTitle("Slot Editor");
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (!madeChanges || !isSlotsFile) return;
                Output output = new Output(0x5 + Slot.MAX_SIZE * (slotInstances.size() + 1),  entry.revision);
                output.int32(slotInstances.size());
                for (Slot slot : slotInstances)
                    slot.serialize(output, true, false);
                output.shrinkToFit();
                
                ResourcePtr[] dependencies = new ResourcePtr[slotInstances.size()];
                dependencies = output.dependencies.toArray(dependencies);
                
                byte[] compressed = Compressor.Compress(output.buffer, "SLTb", entry.revision, dependencies);
                
                toolkit.replaceEntry(entry, compressed);
            }
        });
        
        
        combo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadSlotAt(combo.getSelectedIndex());
            }
        });
        
        
        slotInstances = entry.slots;
        for (int i = 0; i < slotInstances.size(); ++i) {
            Slot slot = slotInstances.get(i);
            String title = slot.title;
            if (title.equals("Unnamed Level") && slot.translationKey != null && !slot.translationKey.equals(""))
                title = slot.translationKey;
            this.slots.add(title + " | " + i);   
        }
        
        combo.setEnabled(false);
        remove.setEnabled(false);
        if (slots.size() != 0) {
            combo.setSelectedIndex(0);
            combo.setEnabled(true);
            if (slots.size() != 1)
                remove.setEnabled(true);
            loadSlotAt(0);
        }
        
    }
    
    
    private void loadSlotAt(int index) {
        Slot slot = slotInstances.get(index);
        
        if (slot == null) return;
        
        backgroundGUID.setText("g" + slot.backgroundGUID);
        
        if (slot.planetDecorations != null) {
            if (slot.planetDecorations.hash != null) 
                planetDecoration.setText(Bytes.toHex(slot.planetDecorations.hash));
            else if (slot.planetDecorations.GUID != -1) 
                planetDecoration.setText("g" + slot.planetDecorations.GUID);
        }
        
        if (slot.root != null) {
            if (slot.root.hash != null) 
                rootLevel.setText(Bytes.toHex(slot.root.hash));
            else if (slot.root.GUID != -1) 
                rootLevel.setText("g" + slot.root.GUID);
        } else rootLevel.setText("");
        
        if (slot.adventure != null) {
            if (slot.adventure.hash != null) {
                adventure.setText(Bytes.toHex(slot.adventure.hash));
            } else if (slot.adventure.GUID != -1) {
                adventure.setText("g" + slot.adventure.GUID);
            }
        } else adventure.setText("");
        
        if (slot.trailer != null) {
            if (slot.trailer.hash != null) 
                trailer.setText(Bytes.toHex(slot.trailer.hash));
            else if (slot.trailer.GUID != -1)
                trailer.setText("g" + slot.trailer.GUID);
        } else trailer.setText("");
        
        if (slot.icon != null) {
            
            byte[] data = null;
            if (slot.icon.GUID != -1)
                data = toolkit.extractFile(slot.icon.GUID);
            else if (slot.icon.hash != null)
                data = toolkit.extractFile(slot.icon.hash);
            if (data != null) {
                Texture texture = new Texture(data);
                if (texture != null) {
                    if (slot.slot.type.equals(SlotType.DEVELOPER_GROUP) || slot.slot.type.equals(SlotType.DLC_PACK))
                        slot.renderedIcon = Images.getGroupIcon(texture.getImage());
                    else slot.renderedIcon = Images.getSlotIcon(texture.getImage(), entry.revision);
                }
            }
            
            
            
            if (slot.icon.hash != null) 
                iconPtr.setText(Bytes.toHex(slot.icon.hash));
            else if (slot.icon.GUID != -1)
                iconPtr.setText("g" + slot.icon.GUID);
            
        } else iconPtr.setText("");
        
        levelType.setSelectedItem(slot.developerLevelType);
        gameMode.setSelectedItem(slot.gameMode);
        minPlayer.setValue(slot.minPlayers);
        maxPlayer.setValue(slot.maxPlayers);
        
        author.setText(slot.authorName);
        
        slotIcon.setIcon(slot.renderedIcon);
        name.setText(slot.title);
        description.setText(slot.description);
        
        slotID.setText("" + slot.slot.ID);
        slotTypeCombo.setSelectedItem(slot.slot.type);
        
        linkSlotID.setText("" + slot.primaryLinkLevel.ID);
        linkSlot.setSelectedItem(slot.primaryLinkLevel.type);
        
        if (slot.translationKey != null && !slot.translationKey.equals(""))
            translationKey.setText(slot.translationKey);
        else translationKey.setText("TRANSLATION_KEY_NONE");
        
        X.setText("" + slot.location.x);
        Y.setText("" + slot.location.y);
        Z.setText("" + slot.location.z);
        W.setText("" + slot.location.w);
        
        
        groupSlot.setSelectedItem(slot.primaryLinkGroup.type);
        groupSlotID.setText("" + slot.primaryLinkGroup.ID);
        
        moveRecommended.setSelected(slot.moveRecommended);
        crossCompatible.setSelected(slot.crossCompatible);
        showOnPlanet.setSelected(slot.showOnPlanet);
        isGamekit.setSelected(slot.isGameKit);
        isSubLevel.setSelected(slot.isSubLevel);
        isLocked.setSelected(slot.isLocked);
        isCopyable.setSelected(slot.copyable);

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar();
        slotIcon = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        description = new javax.swing.JTextArea();
        slotTypeCombo = new javax.swing.JComboBox(SlotType.values());
        slotID = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        isLocked = new javax.swing.JCheckBox();
        isSubLevel = new javax.swing.JCheckBox();
        isGamekit = new javax.swing.JCheckBox();
        isCopyable = new javax.swing.JCheckBox();
        moveRecommended = new javax.swing.JCheckBox();
        crossCompatible = new javax.swing.JCheckBox();
        showOnPlanet = new javax.swing.JCheckBox();
        author = new javax.swing.JTextField();
        levelType = new javax.swing.JComboBox(LevelType.values());
        gameMode = new javax.swing.JComboBox(GameMode.values());
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        minPlayer = new javax.swing.JSpinner();
        maxPlayer = new javax.swing.JSpinner();
        combo = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        rootLevel = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        adventure = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        iconPtr = new javax.swing.JTextField();
        groupSlot = new javax.swing.JComboBox(SlotType.values());
        groupSlotID = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        translationKey = new javax.swing.JTextField();
        advSize = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        Z = new javax.swing.JTextField();
        W = new javax.swing.JTextField();
        X = new javax.swing.JTextField();
        Y = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        trailer = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        planetDecoration = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        backgroundGUID = new javax.swing.JTextField();
        save = new javax.swing.JButton();
        addSlot = new javax.swing.JButton();
        remove = new javax.swing.JButton();
        linkSlotID = new javax.swing.JTextField();
        linkSlot = new javax.swing.JComboBox(SlotType.values());
        jLabel12 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        slotIcon.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        description.setColumns(20);
        description.setLineWrap(true);
        description.setRows(5);
        description.setWrapStyleWord(true);

        slotID.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        slotID.setText("0");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        isLocked.setText("Locked");

        isSubLevel.setText("Sublevel");
        isSubLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isSubLevelActionPerformed(evt);
            }
        });

        isGamekit.setText("Gamekit");

        isCopyable.setText("Share");

        moveRecommended.setText("Move");

        crossCompatible.setText("Cross-Control");

        showOnPlanet.setText("Show Slot on Planet");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(moveRecommended)
                        .addGap(18, 18, 18)
                        .addComponent(crossCompatible))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(isLocked)
                            .addComponent(isCopyable, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(isGamekit)
                            .addComponent(isSubLevel)))
                    .addComponent(showOnPlanet))
                .addGap(0, 4, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isLocked)
                    .addComponent(isSubLevel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isGamekit)
                    .addComponent(isCopyable))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(moveRecommended)
                    .addComponent(crossCompatible))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showOnPlanet)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jLabel1.setText("Level Type");

        jLabel2.setText("Gamemode");

        jLabel3.setText("Player Limit");

        minPlayer.setModel(new javax.swing.SpinnerNumberModel(1, 1, 4, 1));

        maxPlayer.setModel(new javax.swing.SpinnerNumberModel(4, 1, 4, 1));
        maxPlayer.setToolTipText("");

        combo.setModel(model);
        combo.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        combo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setText("Root Level");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rootLevel)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rootLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel5.setText("Adventure");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(adventure)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(adventure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setText("Icon");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iconPtr)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(iconPtr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        groupSlotID.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        groupSlotID.setText("0");

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText("Group Slot");

        advSize.setModel(new javax.swing.SpinnerNumberModel(1, 1, 255, 1));

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setText("Badge Size");

        Z.setText("0.0000");
        Z.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ZActionPerformed(evt);
            }
        });

        W.setText("0.0000");

        X.setText("0.000");
        X.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                XActionPerformed(evt);
            }
        });

        Y.setText("0.000");
        Y.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                YActionPerformed(evt);
            }
        });

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setText("Trailer");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(trailer)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(trailer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        jLabel10.setText("Planet Decoration");

        jLabel11.setText("Background GUID");

        save.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        save.setText("Save");
        save.setMargin(new java.awt.Insets(0, 0, 0, 0));
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        addSlot.setFont(new java.awt.Font("Segoe UI", 0, 8)); // NOI18N
        addSlot.setText("+");
        addSlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSlotActionPerformed(evt);
            }
        });

        remove.setFont(new java.awt.Font("Segoe UI", 0, 8)); // NOI18N
        remove.setText("-");
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });

        linkSlotID.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        linkSlotID.setText("0");

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("Link Slot");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(description, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(slotIcon, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(translationKey)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(author, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(slotTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(slotID, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(combo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(remove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addSlot)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(save, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(X, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Y, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Z, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(W, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(advSize))
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(minPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(maxPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(gameMode, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(levelType, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(planetDecoration, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backgroundGUID))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(groupSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(groupSlotID, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(linkSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(linkSlotID, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(levelType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gameMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(7, 7, 7)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(maxPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(advSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(addSlot, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(combo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(save)
                                        .addComponent(remove)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(slotIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(name)
                                    .addComponent(author))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(translationKey, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(planetDecoration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel11)
                                    .addComponent(backgroundGUID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel8)
                                    .addComponent(groupSlot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(groupSlotID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(linkSlot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(linkSlotID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(description, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(slotTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(slotID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(X, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(W, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void YActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_YActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_YActionPerformed

    private void ZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ZActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ZActionPerformed

    private void XActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_XActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_XActionPerformed

    private void isSubLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isSubLevelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_isSubLevelActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        Slot slot = slotInstances.get(combo.getSelectedIndex());
        madeChanges = true;
        
        slot.backgroundGUID = parseInteger(backgroundGUID.getText());
        
        String deco = planetDecoration.getText();
        if (deco == null || deco.equals("")) slot.planetDecorations = null;
        else {
            if (deco.startsWith("g")) 
                slot.planetDecorations = new ResourcePtr(parseInteger(deco), RType.LEVEL);
            else slot.planetDecorations = new ResourcePtr(Bytes.toBytes(deco), RType.LEVEL);
        }
        
        String root = rootLevel.getText();
        if (root == null || root.equals("")) slot.root = null;
        else {
            if (root.startsWith("g")) 
                slot.root = new ResourcePtr(parseInteger(root), RType.LEVEL);
            else slot.root = new ResourcePtr(Bytes.toBytes(root), RType.LEVEL);
        }
        
        String adv = adventure.getText();
        if (adv == null || adv.equals("")) slot.adventure = null;
        else {
            if (adv.startsWith("g")) 
                slot.adventure = new ResourcePtr(parseInteger(adv), RType.ADVENTURE_CREATE_PROFILE);
            else slot.adventure = new ResourcePtr(Bytes.toBytes(adv), RType.ADVENTURE_CREATE_PROFILE);
        }
        
        String trailer = this.trailer.getText();
        if (trailer == null || trailer.equals("")) slot.trailer = null;
        else {
            if (trailer.startsWith("g")) 
                slot.trailer = new ResourcePtr(parseInteger(trailer), RType.FILE_OF_BYTES);
            else slot.trailer = new ResourcePtr(Bytes.toBytes(trailer), RType.FILE_OF_BYTES);
        }
        
        
        String icon = iconPtr.getText();
        if (icon == null || icon.equals("")) slot.icon = null;
        else {
            if (icon.startsWith("g")) 
                slot.icon = new ResourcePtr(parseInteger(icon), RType.TEXTURE);
            else slot.icon = new ResourcePtr(Bytes.toBytes(icon), RType.TEXTURE);
        }
        
        slot.developerLevelType = (LevelType) levelType.getSelectedItem();
        slot.gameMode = (GameMode) gameMode.getSelectedItem();
        
        slot.minPlayers = (int) minPlayer.getValue();
        slot.maxPlayers = (int) maxPlayer.getValue();
        
        slot.authorName = author.getText();
        
        slot.title = name.getText();
        slot.description = description.getText();
        
        slot.slot.type = (SlotType) slotTypeCombo.getSelectedItem();
        slot.slot.ID = parseInteger(slotID.getText());
        
        slot.group.type = slot.slot.type;
        slot.group.ID = slot.slot.ID;
        
        slot.primaryLinkLevel.type = (SlotType) linkSlot.getSelectedItem();
        slot.primaryLinkLevel.ID = parseInteger(linkSlotID.getText());
        
        String key = translationKey.getText();
        if (key.equals("TRANSLATION_KEY_NONE")) { slot.translationKey = null; }
        else slot.translationKey = key;
        
        
        slot.location.x = Float.parseFloat(X.getText());
        slot.location.y = Float.parseFloat(Y.getText());
        slot.location.z = Float.parseFloat(Z.getText());
        slot.location.w = Float.parseFloat(W.getText());
        
        slot.primaryLinkGroup.type = (SlotType) groupSlot.getSelectedItem();
        slot.primaryLinkGroup.ID = parseInteger(groupSlotID.getText());
        
        
        slot.moveRecommended = moveRecommended.isSelected();
        slot.crossCompatible = crossCompatible.isSelected();
        slot.showOnPlanet = showOnPlanet.isSelected();
        slot.isGameKit = isGamekit.isSelected();
        slot.isSubLevel = isSubLevel.isSelected();
        slot.isLocked = isLocked.isSelected();
        slot.copyable = isCopyable.isSelected();
        
        if (!isSlotFile) dispose();
        
        loadSlotAt(combo.getSelectedIndex());
        
    }//GEN-LAST:event_saveActionPerformed

    private void comboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboActionPerformed

    }//GEN-LAST:event_comboActionPerformed

    int loop = 0;
    
    private void addSlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSlotActionPerformed
        madeChanges = true;
        combo.setEnabled(true);
        remove.setEnabled(true);
        Slot slot = new Slot();
        slot.slot.ID = internalCount;
        slot.slot.type = SlotType.USER_CREATED_STORED_LOCAL;
        slot.group.ID = internalCount;
        slot.group.type = SlotType.USER_CREATED_STORED_LOCAL;
        if (internalCount < 82)
            slot.location = Crater.valueOf("SLOT_" + internalCount + "_LBP" + revision).value;
        else {
            Vector4f v = Crater.valueOf("SLOT_" + (internalCount - (81 * loop)) + "LBP_" + revision).value;
            v.x += 0.05 * loop;
            v.y += 0.05 * loop;
            slot.location = v;
        }
        if (internalCount >= 81 && internalCount % 81 == 0) loop++;
        slot.title = "New Slot " + internalCount;
        slots.add(slot.title + " | " + slots.size());
        slotInstances.add(slot);
        combo.setSelectedIndex(combo.getItemCount() - 1);
        loadSlotAt(combo.getItemCount() - 1);
        internalCount++;
    }//GEN-LAST:event_addSlotActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        madeChanges = true;
        int index = combo.getSelectedIndex();
        slots.remove(index);
        slotInstances.remove(index);
        if (slots.size() == 0) {
            remove.setEnabled(false);
            combo.setEnabled(false);
            return;
        }
        if (index == 0) {
            combo.setSelectedIndex(0);
            loadSlotAt(0);
            return;
        }
        
        combo.setSelectedIndex(index - 1);
        loadSlotAt(index - 1);
    }//GEN-LAST:event_removeActionPerformed

    private long parseInteger(String str) {
        long number = -1;
        if (str.startsWith("0x"))
            number = Long.parseLong(str.substring(2), 16);
        else if (str.startsWith("g"))
            number = Long.parseLong(str.substring(1));
        else number = Long.parseLong(str);
        return number;
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField W;
    private javax.swing.JTextField X;
    private javax.swing.JTextField Y;
    private javax.swing.JTextField Z;
    private javax.swing.JButton addSlot;
    private javax.swing.JSpinner advSize;
    private javax.swing.JTextField adventure;
    private javax.swing.JTextField author;
    private javax.swing.JTextField backgroundGUID;
    private javax.swing.JComboBox<String> combo;
    private javax.swing.JCheckBox crossCompatible;
    private javax.swing.JTextArea description;
    private javax.swing.JComboBox<String> gameMode;
    private javax.swing.JComboBox<String> groupSlot;
    private javax.swing.JTextField groupSlotID;
    private javax.swing.JTextField iconPtr;
    private javax.swing.JCheckBox isCopyable;
    private javax.swing.JCheckBox isGamekit;
    private javax.swing.JCheckBox isLocked;
    private javax.swing.JCheckBox isSubLevel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JComboBox<String> levelType;
    private javax.swing.JComboBox<String> linkSlot;
    private javax.swing.JTextField linkSlotID;
    private javax.swing.JSpinner maxPlayer;
    private javax.swing.JSpinner minPlayer;
    private javax.swing.JCheckBox moveRecommended;
    private javax.swing.JTextField name;
    private javax.swing.JTextField planetDecoration;
    private javax.swing.JButton remove;
    private javax.swing.JTextField rootLevel;
    private javax.swing.JButton save;
    private javax.swing.JCheckBox showOnPlanet;
    private javax.swing.JTextField slotID;
    private javax.swing.JLabel slotIcon;
    private javax.swing.JComboBox<String> slotTypeCombo;
    private javax.swing.JTextField trailer;
    private javax.swing.JTextField translationKey;
    // End of variables declaration//GEN-END:variables
}
