package ennuo.toolkit.windows;

import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.toolkit.utilities.Globals;
import java.nio.file.Paths;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Dependinator extends javax.swing.JFrame {
    private Resource resource;
    private FileEntry entry;
    
    private ResourceDescriptor[] dependencies;
    private ResourceDescriptor[] modifications;
    
    private  DefaultListModel model = new DefaultListModel();
    
    public Dependinator(Toolkit toolkit, FileEntry entry) {
        this.initComponents();
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        this.setTitle("Dependinator");
        
        this.entry = entry;
        
        // Get the resource data
        byte[] data = entry.data;
        if (data == null)
            data = Globals.extractFile(entry.GUID);
        if (data == null)
            data = Globals.extractFile(entry.hash);
        if (data == null) {
            this.dispose();
            return;
        }
        
        this.resource = new Resource(data);
        this.modifications = new ResourceDescriptor[resource.dependencies.size()];
        
        // Create a new copy of the dependencies list, so indexes aren't offset if the user removes dependencies.
        this.dependencies = resource.dependencies.stream().toArray(ResourceDescriptor[]::new);
        
        for (int i = 0; i < this.dependencies.length; ++i) {
            ResourceDescriptor descriptor = this.dependencies[i];
            this.modifications[i] = descriptor;
            FileEntry dependency = Globals.findEntry(descriptor);
            if (dependency == null || dependency.path == null) {
                model.addElement(descriptor.toString());
                continue;
            }
            model.addElement(Paths.get(dependency.path).getFileName().toString());
        }
        
        this.descriptorList.addListSelectionListener(e -> {
            ResourceDescriptor descriptor;
            int index = this.descriptorList.getSelectedIndex();
            descriptor = this.modifications[index];
            if (descriptor == null)
                descriptor = this.resource.dependencies.get(index);
            this.currentDescriptorText.setText(descriptor.toString());
            this.currentDescriptorText.setEnabled(true);
            this.updateDescriptorButton.setEnabled(true);
        });
       
        this.setVisible(true);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        descriptorContainer = new javax.swing.JScrollPane();
        descriptorList = new javax.swing.JList<>();
        currentDescriptorText = new javax.swing.JTextField();
        saveChangesButton = new javax.swing.JButton();
        updateDescriptorButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        descriptorList.setModel(model);
        descriptorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        descriptorContainer.setViewportView(descriptorList);

        currentDescriptorText.setEnabled(false);

        saveChangesButton.setText("Save");
        saveChangesButton.setEnabled(false);
        saveChangesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveChangesButtonActionPerformed(evt);
            }
        });

        updateDescriptorButton.setText("Update");
        updateDescriptorButton.setEnabled(false);
        updateDescriptorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateDescriptorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(descriptorContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                    .addComponent(saveChangesButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(currentDescriptorText)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updateDescriptorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptorContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentDescriptorText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateDescriptorButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveChangesButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateDescriptorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateDescriptorButtonActionPerformed
        int index = this.descriptorList.getSelectedIndex();
        ResourceDescriptor newDescriptor = new ResourceDescriptor(
                this.dependencies[index].type,
                this.currentDescriptorText.getText()
        );
        
        if (newDescriptor.equals(this.dependencies[index])) return;
        if (this.modifications[index] != null)
            if (newDescriptor.equals(this.modifications[index])) 
                return;
        
        this.modifications[index] = newDescriptor;
        
        System.out.println("Set " + this.dependencies[index] + " -> " + newDescriptor);
        
        FileEntry entry = Globals.findEntry(newDescriptor);
        if (entry == null || entry.path == null)
            model.setElementAt(newDescriptor.toString(), index);
        else model.setElementAt(Paths.get(entry.path).getFileName().toString(), index);
        
        this.saveChangesButton.setEnabled(true);
    }//GEN-LAST:event_updateDescriptorButtonActionPerformed

    private void saveChangesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveChangesButtonActionPerformed
        for (int i = 0; i < this.modifications.length; ++i) {
            ResourceDescriptor old = this.dependencies[i];
            if (this.modifications[i].equals(old)) continue;
            System.out.println(modifications[i] + " : " + old);
            this.resource.replaceDependency(old, this.modifications[i]);
        }
        
        Globals.replaceEntry(entry, resource.compressToResource());
        this.dispose();
    }//GEN-LAST:event_saveChangesButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField currentDescriptorText;
    private javax.swing.JScrollPane descriptorContainer;
    private javax.swing.JList<String> descriptorList;
    private javax.swing.JButton saveChangesButton;
    private javax.swing.JButton updateDescriptorButton;
    // End of variables declaration//GEN-END:variables
}