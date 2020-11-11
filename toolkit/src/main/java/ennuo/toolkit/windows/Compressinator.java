package ennuo.toolkit.windows;

import ennuo.craftworld.resources.enums.Magic;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Compressor;
import ennuo.craftworld.memory.FileIO;
import ennuo.craftworld.memory.Strings;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.toolkit.utilities.FileChooser;
import java.io.File;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Compressinator extends javax.swing.JFrame {
    
    
    ArrayList<ResourcePtr> dependencies = new ArrayList<ResourcePtr>();
    DefaultListModel model = new DefaultListModel();
    
    FileChooser fileChooser;
    
    byte[] file;
    
    
    public Compressinator() {
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        setTitle("Compressinator");
        fileChooser = new FileChooser(this);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        refType = new javax.swing.ButtonGroup();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        dependencyList = new javax.swing.JList<>();
        hashButton = new javax.swing.JRadioButton();
        guidButton = new javax.swing.JRadioButton();
        ref = new javax.swing.JTextField();
        typeCombo = new javax.swing.JComboBox(RType.values());
        addEntry = new javax.swing.JButton();
        removeEntry = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        resourceCombo = new javax.swing.JComboBox(ennuo.craftworld.resources.enums.Magic.values());
        revisionLabel = new javax.swing.JLabel();
        revision = new javax.swing.JTextField();
        compress = new javax.swing.JButton();
        openFile = new javax.swing.JButton();
        fileLabel = new javax.swing.JLabel();

        refType.add(hashButton);
        refType.add(guidButton);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setDividerSize(2);

        dependencyList.setModel(model);
        jScrollPane1.setViewportView(dependencyList);

        hashButton.setText("Hash");

        guidButton.setSelected(true);
        guidButton.setText("GUID");
        guidButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guidButtonActionPerformed(evt);
            }
        });

        ref.setText("2551");

        addEntry.setText("Add");
        addEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEntryActionPerformed(evt);
            }
        });

        removeEntry.setText("Rem");
        removeEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeEntryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 351, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(ref)
                        .addComponent(typeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(hashButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(guidButton, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(addEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(removeEntry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addContainerGap()))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 123, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(hashButton)
                                .addComponent(guidButton))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(ref, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(typeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(addEntry)
                                .addComponent(removeEntry))))
                    .addContainerGap(13, Short.MAX_VALUE)))
        );

        jSplitPane1.setRightComponent(jPanel1);

        revisionLabel.setText("Revision");

        revision.setText("0x272");

        compress.setText("Compress");
        compress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compressActionPerformed(evt);
            }
        });

        openFile.setText("Open...");
        openFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileActionPerformed(evt);
            }
        });

        fileLabel.setText("No file selected.");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(openFile, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(compress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(resourceCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(revisionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(revision, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resourceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(revision, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(revisionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openFile, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void guidButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guidButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_guidButtonActionPerformed

    
    private boolean GUIDExists(int GUID) {
        for (ResourcePtr ptr : dependencies)
            if (ptr.GUID == GUID) return true;
        return false;
    }
    
    
    
    private void addEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEntryActionPerformed
        String value = ref.getText();
        ResourcePtr resource = new ResourcePtr();
        if (guidButton.isSelected()) {
            int GUID;
            if (value.startsWith("0x"))
                GUID = Integer.parseInt(value.substring(2), 16);
            else GUID = Integer.parseInt(value);
            
            if (GUIDExists(GUID)) {
                System.out.println(String.format("Dependency with value %s already exists, skipping.", "g" + GUID));
                return;
            }
            
            resource.type = (RType) typeCombo.getSelectedItem();
            resource.GUID = GUID;
            
            model.add(model.size(), "g" + GUID);
        } else {
            value = Strings.leftPad(value, 40);
            resource.hash = Bytes.toBytes(value);
            model.add(model.size(), "h" + Bytes.toHex(resource.hash));
        }
        System.out.println(String.format("Adding dependency of type %s with value %s", resource.type, model.get(dependencies.size())));
        dependencies.add(dependencies.size(), resource);
    }//GEN-LAST:event_addEntryActionPerformed

    private void openFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileActionPerformed
       File file = fileChooser.openFile("Uncompressed Data", "", "",  false);
       if (file != null) {
            this.file = FileIO.read(file.getAbsolutePath());
            if (this.file != null)
                fileLabel.setText(file.getName());
       }
    }//GEN-LAST:event_openFileActionPerformed

    private void compressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compressActionPerformed
        String revisionString = revision.getText();
        int revision;
        if (revisionString.startsWith("0x"))
            revision = Integer.parseInt(revisionString.substring(2), 16);
        else revision = Integer.parseInt(revisionString);
        
        String header = ((Magic) resourceCombo.getSelectedItem()).value;
        
        ResourcePtr[] dependencies = new ResourcePtr[this.dependencies.size()];
        dependencies = this.dependencies.toArray(dependencies);
        
        if (file == null) {
            System.err.println("You need to specify a file to compress!");
            return;
        }
        
        byte[] compressed = Compressor.Compress(file, header, revision, dependencies);
        
        File output = fileChooser.openFile("output." + header, "", "", true);
        if (output != null)
            FileIO.write(compressed, output.getAbsolutePath());
    }//GEN-LAST:event_compressActionPerformed

    private void removeEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeEntryActionPerformed
        int index = dependencyList.getSelectedIndex();
        if (index != -1) {
            
            ResourcePtr ptr = dependencies.get(index);
            String str = (String) model.get(index);
            
            System.out.println(String.format("Removing dependency of type %s with value %s", ptr.type, str));
            
            dependencies.remove(index);
            model.remove(index);
        }
    }//GEN-LAST:event_removeEntryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addEntry;
    private javax.swing.JButton compress;
    private javax.swing.JList<String> dependencyList;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JRadioButton guidButton;
    private javax.swing.JRadioButton hashButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton openFile;
    private javax.swing.JTextField ref;
    private javax.swing.ButtonGroup refType;
    private javax.swing.JButton removeEntry;
    private javax.swing.JComboBox<String> resourceCombo;
    private javax.swing.JTextField revision;
    private javax.swing.JLabel revisionLabel;
    private javax.swing.JComboBox<String> typeCombo;
    // End of variables declaration//GEN-END:variables
}
