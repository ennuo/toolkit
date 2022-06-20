package toolkit.windows;

import cwlib.types.Resource;
import cwlib.enums.CompressionFlags;
import cwlib.enums.Magic;
import cwlib.util.FileIO;
import cwlib.util.Strings;
import toolkit.utilities.FileChooser;
import cwlib.enums.ResourceType;
import cwlib.types.data.Revision;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;

import java.io.File;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Compressinator extends javax.swing.JFrame {
    ArrayList<ResourceDescriptor> dependencies = new ArrayList<ResourceDescriptor>();
    DefaultListModel<String> model = new DefaultListModel<>();
    FileChooser fileChooser;
    byte[] fileData;
    
    public Compressinator() {
        this.initComponents();
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(new ImageIcon(this.getClass().getResource("/legacy_icon.png")).getImage());
        this.setTitle("Compressinator");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        refType = new javax.swing.ButtonGroup();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        headerPanel = new javax.swing.JPanel();
        headerCategoryLabel = new javax.swing.JLabel();
        resourceCombo = new javax.swing.JComboBox(cwlib.enums.Magic.values());
        magicLabel = new javax.swing.JLabel();
        revisionLabel = new javax.swing.JLabel();
        revision = new javax.swing.JTextField();
        branchLabel = new javax.swing.JLabel();
        branchID = new javax.swing.JTextField();
        branchRevision = new javax.swing.JTextField();
        compressionFlagsLabel = new javax.swing.JLabel();
        useCompressedIntegers = new javax.swing.JCheckBox();
        useCompressMatrices = new javax.swing.JCheckBox();
        useCompressedVectors = new javax.swing.JCheckBox();
        dependencyManagerPanel = new javax.swing.JPanel();
        dependenciesCategoryLabel = new javax.swing.JLabel();
        dependencyScrollPane = new javax.swing.JScrollPane();
        dependencyList = new javax.swing.JList<>();
        dependencyModifierPanel = new javax.swing.JPanel();
        dependencyCategoryLabel = new javax.swing.JLabel();
        hashButton = new javax.swing.JRadioButton();
        guidButton = new javax.swing.JRadioButton();
        descriptorValue = new javax.swing.JTextField();
        descriptorResourceType = new javax.swing.JComboBox(ResourceType.values());
        addEntry = new javax.swing.JButton();
        removeEntry = new javax.swing.JButton();
        compress = new javax.swing.JButton();
        dataPanel = new javax.swing.JPanel();
        dataCategoryLabel = new javax.swing.JLabel();
        openFile = new javax.swing.JButton();
        fileLabel = new javax.swing.JLabel();

        refType.add(hashButton);
        refType.add(guidButton);

        jFormattedTextField1.setText("jFormattedTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        headerPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        headerCategoryLabel.setText("Header");

        magicLabel.setText("Magic");

        revisionLabel.setText("Revision");

        revision.setText("0x272");

        branchLabel.setText("Branch");

        branchID.setText("0x4c44");

        branchRevision.setText("0x0017");

        compressionFlagsLabel.setText("Compression Flags");

        useCompressedIntegers.setSelected(true);
        useCompressedIntegers.setText("Integers");

        useCompressMatrices.setSelected(true);
        useCompressMatrices.setText("Matrices");

        useCompressedVectors.setSelected(true);
        useCompressedVectors.setText("Vectors");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(compressionFlagsLabel)
                            .addGroup(headerPanelLayout.createSequentialGroup()
                                .addComponent(useCompressedIntegers)
                                .addGap(18, 18, 18)
                                .addComponent(useCompressMatrices)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(useCompressedVectors)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(magicLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(headerCategoryLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(branchLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(revisionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
                                .addComponent(branchID, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(branchRevision))
                            .addComponent(resourceCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(revision, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerCategoryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resourceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(magicLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(revision)
                    .addComponent(revisionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(branchLabel)
                    .addComponent(branchID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(branchRevision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compressionFlagsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useCompressedIntegers)
                    .addComponent(useCompressMatrices)
                    .addComponent(useCompressedVectors))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dependencyManagerPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        dependenciesCategoryLabel.setText("Dependencies");

        dependencyList.setModel(model);
        dependencyScrollPane.setViewportView(dependencyList);

        dependencyModifierPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        dependencyCategoryLabel.setText("Dependency");

        hashButton.setText("Hash");

        guidButton.setSelected(true);
        guidButton.setText("GUID");

        descriptorValue.setText("2551");

        addEntry.setText("Add");
        addEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEntryActionPerformed(evt);
            }
        });

        removeEntry.setText("Remove");
        removeEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeEntryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dependencyModifierPanelLayout = new javax.swing.GroupLayout(dependencyModifierPanel);
        dependencyModifierPanel.setLayout(dependencyModifierPanelLayout);
        dependencyModifierPanelLayout.setHorizontalGroup(
            dependencyModifierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dependencyModifierPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dependencyModifierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dependencyModifierPanelLayout.createSequentialGroup()
                        .addGroup(dependencyModifierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dependencyCategoryLabel)
                            .addGroup(dependencyModifierPanelLayout.createSequentialGroup()
                                .addComponent(hashButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(guidButton, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(dependencyModifierPanelLayout.createSequentialGroup()
                        .addGroup(dependencyModifierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(dependencyModifierPanelLayout.createSequentialGroup()
                                .addComponent(addEntry, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(removeEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(descriptorValue)
                            .addComponent(descriptorResourceType, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        dependencyModifierPanelLayout.setVerticalGroup(
            dependencyModifierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dependencyModifierPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dependencyCategoryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dependencyModifierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hashButton)
                    .addComponent(guidButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptorValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptorResourceType, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dependencyModifierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addEntry)
                    .addComponent(removeEntry))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout dependencyManagerPanelLayout = new javax.swing.GroupLayout(dependencyManagerPanel);
        dependencyManagerPanel.setLayout(dependencyManagerPanelLayout);
        dependencyManagerPanelLayout.setHorizontalGroup(
            dependencyManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dependencyManagerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dependencyManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dependenciesCategoryLabel)
                    .addComponent(dependencyScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dependencyModifierPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        dependencyManagerPanelLayout.setVerticalGroup(
            dependencyManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dependencyManagerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dependencyManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dependencyModifierPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(dependencyManagerPanelLayout.createSequentialGroup()
                        .addComponent(dependenciesCategoryLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dependencyScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)))
                .addContainerGap())
        );

        compress.setText("Compress");
        compress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compressActionPerformed(evt);
            }
        });

        dataPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        dataCategoryLabel.setText("Data");

        openFile.setText("Open...");
        openFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileActionPerformed(evt);
            }
        });

        fileLabel.setText("No file selected.");

        javax.swing.GroupLayout dataPanelLayout = new javax.swing.GroupLayout(dataPanel);
        dataPanel.setLayout(dataPanelLayout);
        dataPanelLayout.setHorizontalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dataPanelLayout.createSequentialGroup()
                        .addComponent(dataCategoryLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(dataPanelLayout.createSequentialGroup()
                        .addComponent(openFile, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        dataPanelLayout.setVerticalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataCategoryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openFile)
                    .addComponent(fileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(compress, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                    .addComponent(dependencyManagerPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(headerPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dataPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dependencyManagerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compress)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void removeEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeEntryActionPerformed
        int index = this.dependencyList.getSelectedIndex();
        if (index != -1) {
            ResourceDescriptor descriptor = this.dependencies.get(index);
            String value = (String) this.model.get(index);

            System.out.println(String.format("Removing dependency of type %s with value %s", descriptor.getType(), value));

            this.dependencies.remove(index);
            this.model.remove(index);
        }
    }//GEN-LAST:event_removeEntryActionPerformed

    private void addEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEntryActionPerformed
        String value = this.descriptorValue.getText();
        ResourceType type = (ResourceType) this.descriptorResourceType.getSelectedItem();

        ResourceDescriptor descriptor = null;
        if (this.guidButton.isSelected() && Strings.isGUID(value)) {
            long GUID = Strings.getLong(value);
            
            if (this.doesGUIDAlreadyExist(GUID)) {
                System.out.println(String.format("Dependency with value %s already exists, skipping.", "g" + GUID));
                return;
            }

            descriptor = new ResourceDescriptor(GUID, type);
        } else if (Strings.isSHA1(value))
            descriptor = new ResourceDescriptor(value, type);
        else return;

        this.model.add(this.model.size(), descriptor.toString());
        System.out.println(String.format("Adding dependency of type %s with value %s", descriptor.getType(), this.model.get(this.dependencies.size())));
        this.dependencies.add(this.dependencies.size(), descriptor);
    }//GEN-LAST:event_addEntryActionPerformed

    private void compressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compressActionPerformed
        int headRevision = (int) Strings.getLong(this.revision.getText());
        int branchID = (int) Strings.getLong(this.branchID.getText());
        int branchRevision = (int) Strings.getLong(this.branchRevision.getText());
        String header = ((Magic) this.resourceCombo.getSelectedItem()).getValue();
        
        byte compressionFlags = 0x0;
        if (this.useCompressedIntegers.isSelected()) 
            compressionFlags |= CompressionFlags.USE_COMPRESSED_INTEGERS;
        if (this.useCompressMatrices.isSelected()) 
            compressionFlags |= CompressionFlags.USE_COMPRESSED_MATRICES;
        if (this.useCompressedVectors.isSelected()) 
            compressionFlags |= CompressionFlags.USE_COMPRESSED_VECTORS;

        if (this.fileData == null) {
            System.err.println("You need to specify a file to compress!");
            return;
        }
        
        Revision revision = new Revision(headRevision, branchID, branchRevision);
        byte[] compressed = Resource.compressToResource(this.fileData, revision, compressionFlags, ResourceType.fromMagic(header), this.dependencies);

        File output = FileChooser.openFile("output." + header, null, true);
        if (output != null)
        FileIO.write(compressed, output.getAbsolutePath());
    }//GEN-LAST:event_compressActionPerformed

    private void openFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileActionPerformed
        File file = FileChooser.openFile("Uncompressed Data", null,  false);
        if (file != null) {
            this.fileData = FileIO.read(file.getAbsolutePath());
            if (this.fileData != null)
            this.fileLabel.setText(file.getName());
        }
    }//GEN-LAST:event_openFileActionPerformed

    private boolean doesGUIDAlreadyExist(long value) {
        GUID guid = new GUID(value);
        for (ResourceDescriptor descriptor : this.dependencies)
            if (guid.equals(descriptor.getGUID())) return true;
        return false;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addEntry;
    private javax.swing.JTextField branchID;
    private javax.swing.JLabel branchLabel;
    private javax.swing.JTextField branchRevision;
    private javax.swing.JButton compress;
    private javax.swing.JLabel compressionFlagsLabel;
    private javax.swing.JLabel dataCategoryLabel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JLabel dependenciesCategoryLabel;
    private javax.swing.JLabel dependencyCategoryLabel;
    private javax.swing.JList<String> dependencyList;
    private javax.swing.JPanel dependencyManagerPanel;
    private javax.swing.JPanel dependencyModifierPanel;
    private javax.swing.JScrollPane dependencyScrollPane;
    private javax.swing.JComboBox<String> descriptorResourceType;
    private javax.swing.JTextField descriptorValue;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JRadioButton guidButton;
    private javax.swing.JRadioButton hashButton;
    private javax.swing.JLabel headerCategoryLabel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JLabel magicLabel;
    private javax.swing.JButton openFile;
    private javax.swing.ButtonGroup refType;
    private javax.swing.JButton removeEntry;
    private javax.swing.JComboBox<String> resourceCombo;
    private javax.swing.JTextField revision;
    private javax.swing.JLabel revisionLabel;
    private javax.swing.JCheckBox useCompressMatrices;
    private javax.swing.JCheckBox useCompressedIntegers;
    private javax.swing.JCheckBox useCompressedVectors;
    // End of variables declaration//GEN-END:variables
}
