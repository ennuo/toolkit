package toolkit.windows.utilities;

import cwlib.enums.Branch;
import cwlib.enums.CellGcmEnumForGtf;
import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.io.serializer.SerializationData;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.texture.CellGcmTexture;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.databases.FileEntry;
import cwlib.util.Bytes;
import cwlib.util.Compressor;
import cwlib.util.FileIO;
import java.io.File;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;
import toolkit.dialogues.DescriptorDialogue;

public class Compressinator extends javax.swing.JFrame {
    static ResourceType[] types;
    static {
        ArrayList<ResourceType> collection = new ArrayList<>();

        collection.add(ResourceType.FILE_OF_BYTES);
        for (ResourceType type : ResourceType.values()) {
            if (type.equals(ResourceType.STATIC_MESH) || type.equals(ResourceType.FONTFACE))
                continue;
            if (type.getHeader() != null) 
                collection.add(type);
        }

        types = collection.toArray(ResourceType[]::new);
    }
    
    private static class Dependentry {
        private final ResourceDescriptor descriptor;
        private final FileEntry entry;
        
        private Dependentry(ResourceDescriptor descriptor) {
            this.descriptor = descriptor;
            if (descriptor.isGUID())
                this.entry = ResourceSystem.get(descriptor.getGUID());
            else
                this.entry = null;
        }
        
        @Override public String toString() {
            String postfix = String.format(" [%s]", this.descriptor.getType());
            if (this.entry != null)
                return this.entry.getName() + postfix;
            return this.descriptor.toString() + postfix;
        }

        @Override public int hashCode() { return this.descriptor.hashCode(); }
        @Override public boolean equals(Object other) { 
            if (other == this) return true;
            if (!(other instanceof Dependentry)) return false;
            return ((Dependentry)other).descriptor.equals(this.descriptor);
        }
    }
    
    private Revision revision;
    private byte[] dataSource;
    private DefaultListModel<Dependentry> model = new DefaultListModel<>();
    
    public Compressinator() {
        this.initComponents();
        this.setIconImage(new ImageIcon(this.getClass().getResource("/icon.png")).getImage());
        this.setLocationRelativeTo(Toolkit.INSTANCE);
        this.getRootPane().setDefaultButton(this.compressButton);
        
        this.dependencyList.setModel(this.model);
        
        this.revisionSpinner.setValue(0x272);
        this.branchCombo.setSelectedItem(Branch.LEERDAMMER);
        this.branchSpinner.setValue(Branch.LEERDAMMER.getRevision() & 0xffff);
        
        this.resourceCombo.addActionListener(listener -> this.update());
        this.revisionSpinner.addChangeListener(listener -> this.update());
        this.branchCombo.addActionListener(listener -> this.update());
        this.branchSpinner.addChangeListener(listener -> this.update());
        
        this.update();
    }
    
    private void update() {
        Branch branch = (Branch) this.branchCombo.getSelectedItem(); 
        this.revision = new Revision(
                (int)this.revisionSpinner.getValue(),
                branch.getID(),
                (short) ((int)this.branchSpinner.getValue())
        );
        
        ResourceType type = (ResourceType) this.resourceCombo.getSelectedItem();
        
        boolean hasRevision = type != ResourceType.FILE_OF_BYTES && type != ResourceType.TEXTURE && type != ResourceType.GTF_TEXTURE;
        boolean hasBranchRevision = hasRevision && this.revision.getVersion() >= Revisions.BRANCHES;
        boolean hasCompressionFlags = hasRevision && ((this.revision.getVersion() >= 0x297) || this.revision.after(Branch.LEERDAMMER, Revisions.LD_RESOURCES));
        
        this.revisionSpinner.setEnabled(hasRevision);
        this.branchCombo.setEnabled(hasBranchRevision);
        this.branchSpinner.setEnabled(hasBranchRevision && branch != Branch.NONE);
        
        this.integersCheckbox.setEnabled(hasCompressionFlags);
        this.matrixCheckbox.setEnabled(hasCompressionFlags);
        this.vectorsCheckbox.setEnabled(hasCompressionFlags);
        
        boolean isGTF = type == ResourceType.GTF_TEXTURE;
        
        this.formatCombo.setEnabled(isGTF);
        this.heightSpinner.setEnabled(isGTF);
        this.widthSpinner.setEnabled(isGTF);
        this.mipSpinner.setEnabled(isGTF);
        
        if (type == ResourceType.FILE_OF_BYTES) {
            this.descriptorTabs.setSelectedIndex(-1);
            this.descriptorTabs.setEnabledAt(1, false);
            this.descriptorTabs.setEnabledAt(0, false);
        }
        else if (type == ResourceType.TEXTURE || isGTF) {
            this.descriptorTabs.setEnabledAt(1, true);
            this.descriptorTabs.setEnabledAt(0, false);
            
            this.gtfPanel.setVisible(isGTF);

            this.descriptorTabs.setSelectedIndex(1);
        } else {
            this.descriptorTabs.setEnabledAt(0, true);
            this.descriptorTabs.setEnabledAt(1, false);
            
            this.descriptorTabs.setSelectedIndex(0);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        typeLabel = new javax.swing.JLabel();
        resourceCombo = new javax.swing.JComboBox(types);
        revisionLabel = new javax.swing.JLabel();
        revisionSpinner = new javax.swing.JSpinner();
        branchLabel = new javax.swing.JLabel();
        branchCombo = new javax.swing.JComboBox(Branch.values());
        branchSpinner = new javax.swing.JSpinner();
        compressionFlagsLabel = new javax.swing.JLabel();
        zlibCheckbox = new javax.swing.JCheckBox();
        integersCheckbox = new javax.swing.JCheckBox();
        matrixCheckbox = new javax.swing.JCheckBox();
        vectorsCheckbox = new javax.swing.JCheckBox();
        dataLabel = new javax.swing.JLabel();
        openButton = new javax.swing.JButton();
        dataSourceLabel = new javax.swing.JLabel();
        descriptorTabs = new javax.swing.JTabbedPane();
        dependencyPanel = new javax.swing.JPanel();
        dependencyListContainer = new javax.swing.JScrollPane();
        dependencyList = new javax.swing.JList<>();
        addDependencyButton = new javax.swing.JButton();
        removeDependencyButton = new javax.swing.JButton();
        textureInfoPanel = new javax.swing.JPanel();
        gtfPanel = new javax.swing.JPanel();
        formatLevel = new javax.swing.JLabel();
        formatCombo = new javax.swing.JComboBox(CellGcmEnumForGtf.values());
        mipsLabel = new javax.swing.JLabel();
        mipSpinner = new javax.swing.JSpinner();
        widthLabel = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        widthSpinner = new javax.swing.JSpinner();
        heightSpinner = new javax.swing.JSpinner();
        noSRGBCheckbox = new javax.swing.JCheckBox();
        compressButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Compressinator");
        setResizable(false);

        typeLabel.setText("Resource Type:");
        typeLabel.setMaximumSize(new java.awt.Dimension(105, 16));
        typeLabel.setMinimumSize(new java.awt.Dimension(105, 16));
        typeLabel.setPreferredSize(new java.awt.Dimension(105, 16));

        revisionLabel.setText("Revision:");
        revisionLabel.setMaximumSize(new java.awt.Dimension(105, 16));
        revisionLabel.setMinimumSize(new java.awt.Dimension(105, 16));
        revisionLabel.setPreferredSize(new java.awt.Dimension(105, 16));

        revisionSpinner.setModel(new javax.swing.SpinnerNumberModel(306, 306, null, 1));

        branchLabel.setText("Branch:");
        branchLabel.setMaximumSize(new java.awt.Dimension(105, 16));
        branchLabel.setMinimumSize(new java.awt.Dimension(105, 16));
        branchLabel.setPreferredSize(new java.awt.Dimension(105, 16));

        branchSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));

        compressionFlagsLabel.setText("Compression Flags:");
        compressionFlagsLabel.setMaximumSize(new java.awt.Dimension(105, 16));
        compressionFlagsLabel.setMinimumSize(new java.awt.Dimension(105, 16));
        compressionFlagsLabel.setPreferredSize(new java.awt.Dimension(105, 16));

        zlibCheckbox.setSelected(true);
        zlibCheckbox.setText("Zlib");
        zlibCheckbox.setMaximumSize(new java.awt.Dimension(50, 20));
        zlibCheckbox.setMinimumSize(new java.awt.Dimension(50, 20));
        zlibCheckbox.setPreferredSize(new java.awt.Dimension(50, 20));

        integersCheckbox.setSelected(true);
        integersCheckbox.setText("Integers");
        integersCheckbox.setMaximumSize(new java.awt.Dimension(70, 20));
        integersCheckbox.setMinimumSize(new java.awt.Dimension(70, 20));
        integersCheckbox.setPreferredSize(new java.awt.Dimension(70, 20));

        matrixCheckbox.setSelected(true);
        matrixCheckbox.setText("Matrix");
        matrixCheckbox.setMaximumSize(new java.awt.Dimension(60, 20));
        matrixCheckbox.setMinimumSize(new java.awt.Dimension(60, 20));
        matrixCheckbox.setPreferredSize(new java.awt.Dimension(60, 20));

        vectorsCheckbox.setSelected(true);
        vectorsCheckbox.setText("Vectors");
        vectorsCheckbox.setMaximumSize(new java.awt.Dimension(70, 20));
        vectorsCheckbox.setMinimumSize(new java.awt.Dimension(70, 20));
        vectorsCheckbox.setPreferredSize(new java.awt.Dimension(70, 20));

        dataLabel.setText("Data:");

        openButton.setText("Open");
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        dataSourceLabel.setText("Select a data source...");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(typeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(revisionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(branchLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resourceCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(revisionSpinner)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(branchCombo, 0, 80, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(branchSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(compressionFlagsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(zlibCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(integersCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(matrixCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(vectorsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(dataLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(openButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dataSourceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resourceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(revisionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(revisionSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(branchLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(branchCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(branchSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compressionFlagsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zlibCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(integersCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(matrixCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vectorsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openButton)
                    .addComponent(dataSourceLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dependencyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        dependencyListContainer.setViewportView(dependencyList);

        addDependencyButton.setText("Add");
        addDependencyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDependencyButtonActionPerformed(evt);
            }
        });

        removeDependencyButton.setText("Delete");
        removeDependencyButton.setEnabled(false);
        removeDependencyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDependencyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dependencyPanelLayout = new javax.swing.GroupLayout(dependencyPanel);
        dependencyPanel.setLayout(dependencyPanelLayout);
        dependencyPanelLayout.setHorizontalGroup(
            dependencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dependencyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dependencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dependencyListContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(dependencyPanelLayout.createSequentialGroup()
                        .addComponent(addDependencyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(removeDependencyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        dependencyPanelLayout.setVerticalGroup(
            dependencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dependencyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dependencyListContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dependencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addDependencyButton)
                    .addComponent(removeDependencyButton))
                .addContainerGap())
        );

        descriptorTabs.addTab("Dependencies", dependencyPanel);

        formatLevel.setText("Format:");
        formatLevel.setMaximumSize(new java.awt.Dimension(50, 16));
        formatLevel.setMinimumSize(new java.awt.Dimension(50, 16));
        formatLevel.setPreferredSize(new java.awt.Dimension(50, 16));

        mipsLabel.setText("Mips:");
        mipsLabel.setMaximumSize(new java.awt.Dimension(50, 16));
        mipsLabel.setMinimumSize(new java.awt.Dimension(50, 16));
        mipsLabel.setPreferredSize(new java.awt.Dimension(50, 16));

        mipSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 128, 1));

        widthLabel.setText("Width:");
        widthLabel.setMaximumSize(new java.awt.Dimension(50, 16));
        widthLabel.setMinimumSize(new java.awt.Dimension(50, 16));
        widthLabel.setPreferredSize(new java.awt.Dimension(50, 16));

        heightLabel.setText("Height:");
        heightLabel.setMaximumSize(new java.awt.Dimension(50, 16));
        heightLabel.setMinimumSize(new java.awt.Dimension(50, 16));
        heightLabel.setPreferredSize(new java.awt.Dimension(50, 16));

        widthSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 65535, 1));

        heightSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 65535, 1));

        javax.swing.GroupLayout gtfPanelLayout = new javax.swing.GroupLayout(gtfPanel);
        gtfPanel.setLayout(gtfPanelLayout);
        gtfPanelLayout.setHorizontalGroup(
            gtfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gtfPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gtfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(widthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(heightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mipsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(formatLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gtfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(formatCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mipSpinner)
                    .addComponent(heightSpinner)
                    .addComponent(widthSpinner))
                .addContainerGap())
        );
        gtfPanelLayout.setVerticalGroup(
            gtfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gtfPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gtfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(formatLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(formatCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gtfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mipsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mipSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gtfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(widthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(widthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gtfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(heightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(heightSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        noSRGBCheckbox.setText("Normal Texture (Linear/No sRGB)");

        javax.swing.GroupLayout textureInfoPanelLayout = new javax.swing.GroupLayout(textureInfoPanel);
        textureInfoPanel.setLayout(textureInfoPanelLayout);
        textureInfoPanelLayout.setHorizontalGroup(
            textureInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gtfPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(textureInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(noSRGBCheckbox)
                .addContainerGap(53, Short.MAX_VALUE))
        );
        textureInfoPanelLayout.setVerticalGroup(
            textureInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textureInfoPanelLayout.createSequentialGroup()
                .addComponent(gtfPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noSRGBCheckbox))
        );

        descriptorTabs.addTab("Texture Info", textureInfoPanel);

        compressButton.setText("Compress");
        compressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compressButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(descriptorTabs))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(compressButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(descriptorTabs))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(compressButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        File file = FileChooser.openFile("data.bin", null,  false);
        if (file != null) {
            this.dataSource = FileIO.read(file.getAbsolutePath());
            this.dataSourceLabel.setText(file.getName());
        }
    }//GEN-LAST:event_openButtonActionPerformed

    private void compressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compressButtonActionPerformed
        if (this.dataSource == null) {
            JOptionPane.showMessageDialog(this, "You have to select a file to compress!", "An error occurred", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        this.update();
        
        ResourceType type = (ResourceType) this.resourceCombo.getSelectedItem();
        boolean isCompressed = this.zlibCheckbox.isSelected();
        
        byte[] resource = null;
        if (type == ResourceType.TEXTURE) {
            byte[] data = this.dataSource;
            if (this.noSRGBCheckbox.isSelected())
                data = Bytes.combine(data, new byte[] { 0x42, 0x55, 0x4d, 0x50 });
            resource = Resource.compress(new SerializationData(data), isCompressed);
        } else if (type == ResourceType.GTF_TEXTURE) {
            CellGcmTexture texture = new CellGcmTexture(
                    (CellGcmEnumForGtf) this.formatCombo.getSelectedItem(),
                    (short)((int)this.widthSpinner.getValue()),
                    (short)((int)this.heightSpinner.getValue()),
                    (byte)(((int)this.mipSpinner.getValue()) & 0xff),
                    this.noSRGBCheckbox.isSelected()
            );
            resource = Resource.compress(new SerializationData(this.dataSource, texture), isCompressed);
        } else if (type == ResourceType.FILE_OF_BYTES) {
            resource = Compressor.getCompressedStream(this.dataSource, isCompressed);
        } else{
            byte compressionFlags = 0x0;
            boolean hasCompressionFlags = (this.revision.getVersion() >= Revisions.COMPRESSED_RESOURCES) || (this.revision.after(Branch.LEERDAMMER, Revisions.LD_RESOURCES));
            
            if (hasCompressionFlags) {
                if (this.integersCheckbox.isSelected()) compressionFlags |= CompressionFlags.USE_COMPRESSED_INTEGERS;
                if (this.matrixCheckbox.isSelected()) compressionFlags |= CompressionFlags.USE_COMPRESSED_MATRICES;
                if (this.vectorsCheckbox.isSelected()) compressionFlags |= CompressionFlags.USE_COMPRESSED_VECTORS;
            }
            
            ResourceDescriptor[] dependencies = new ResourceDescriptor[this.model.size()];
            for (int i = 0; i < dependencies.length; ++i)
                dependencies[i] = this.model.get(i).descriptor;
            
            SerializationData data = new SerializationData(
                    this.dataSource, 
                    this.revision, 
                    compressionFlags, 
                    type, 
                    SerializationType.BINARY, 
                    dependencies
            );
            
            resource = Resource.compress(data, isCompressed);
        }
        
        File file = FileChooser.openFile("compressed" + type.getExtension(), null, true);
        if (file == null) return;
        
        
        if (FileIO.write(resource, file.getAbsolutePath()))
            JOptionPane.showMessageDialog(this, "Successfully wrote file to " + file.getAbsolutePath(), "Compressinator", JOptionPane.INFORMATION_MESSAGE); 
        else
            JOptionPane.showMessageDialog(this, "Failed to write file, is it in use?", "Compressinator", JOptionPane.ERROR_MESSAGE);
        
    }//GEN-LAST:event_compressButtonActionPerformed

    private void addDependencyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDependencyButtonActionPerformed
        ResourceDescriptor descriptor = DescriptorDialogue.doDescriptorEntry(this, null);
        if (descriptor == null || this.model.contains(descriptor)) return;
        
        this.model.addElement(new Dependentry(descriptor));
        
        this.removeDependencyButton.setEnabled(true);
        this.dependencyList.setSelectedIndex(this.model.size() - 1);
    }//GEN-LAST:event_addDependencyButtonActionPerformed

    private void removeDependencyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDependencyButtonActionPerformed
        int index = this.dependencyList.getSelectedIndex();
        if (index == -1) return;
        if (this.model.size() - 1 != 0) {
            if (index == 0)
                this.dependencyList.setSelectedIndex(index + 1);
            else
                this.dependencyList.setSelectedIndex(index - 1);   
        }
        this.model.removeElementAt(index);
        
        if (this.model.size() == 0) this.removeDependencyButton.setEnabled(false);
    }//GEN-LAST:event_removeDependencyButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDependencyButton;
    private javax.swing.JComboBox<Branch> branchCombo;
    private javax.swing.JLabel branchLabel;
    private javax.swing.JSpinner branchSpinner;
    private javax.swing.JButton compressButton;
    private javax.swing.JLabel compressionFlagsLabel;
    private javax.swing.JLabel dataLabel;
    private javax.swing.JLabel dataSourceLabel;
    private javax.swing.JList<Dependentry> dependencyList;
    private javax.swing.JScrollPane dependencyListContainer;
    private javax.swing.JPanel dependencyPanel;
    private javax.swing.JTabbedPane descriptorTabs;
    private javax.swing.JComboBox<CellGcmEnumForGtf> formatCombo;
    private javax.swing.JLabel formatLevel;
    private javax.swing.JPanel gtfPanel;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JSpinner heightSpinner;
    private javax.swing.JCheckBox integersCheckbox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox matrixCheckbox;
    private javax.swing.JSpinner mipSpinner;
    private javax.swing.JLabel mipsLabel;
    private javax.swing.JCheckBox noSRGBCheckbox;
    private javax.swing.JButton openButton;
    private javax.swing.JButton removeDependencyButton;
    private javax.swing.JComboBox<ResourceType> resourceCombo;
    private javax.swing.JLabel revisionLabel;
    private javax.swing.JSpinner revisionSpinner;
    private javax.swing.JPanel textureInfoPanel;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JCheckBox vectorsCheckbox;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JSpinner widthSpinner;
    private javax.swing.JCheckBox zlibCheckbox;
    // End of variables declaration//GEN-END:variables
}
