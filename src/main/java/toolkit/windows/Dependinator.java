package toolkit.windows;

import cwlib.types.databases.FileEntry;
import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.Strings;
import toolkit.utilities.ResourceSystem;
import toolkit.windows.Toolkit;

import java.nio.file.Paths;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Dependinator extends javax.swing.JFrame {
    private Resource resource;
    private FileEntry entry;
    
    private ArrayList<ResourceDescriptor> dependencies;
    private ArrayList<ResourceDescriptor> modifications;
    private ArrayList<ResourceDescriptor> removed = new ArrayList<>();
    
    private DefaultListModel<String> model = new DefaultListModel<>();
    
    public Dependinator(Toolkit toolkit, FileEntry entry) {
        this.initComponents();
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        this.setTitle("Dependinator");
        
        this.entry = entry;
        
        // Get the resource data
        byte[] data = entry.data;
        if (data == null) data = ResourceSystem.extractFile(entry.hash);
        if (data == null) {
            this.dispose();
            return;
        }
        
        this.resource = new Resource(data);
        this.modifications = new ArrayList<>(resource.dependencies.size());
        
        // Create a new copy of the dependencies list, so indexes aren't offset if the user removes dependencies.
        this.dependencies = new ArrayList<>(resource.dependencies);
        
        for (int i = 0; i < this.dependencies.size(); ++i) {
            ResourceDescriptor descriptor = this.dependencies.get(i);
            this.modifications.add(descriptor);
            FileEntry dependency = ResourceSystem.findEntry(descriptor);
            if (dependency == null || dependency.getPath() == null) {
                model.addElement(descriptor.toString() + " (" + descriptor.getType().name() + ")");
                continue;
            }
            model.addElement(Paths.get(dependency.getPath()).getFileName().toString());
        }
        
        this.descriptorList.addListSelectionListener(e -> {
            ResourceDescriptor descriptor;
            int index = this.descriptorList.getSelectedIndex();
            if (index == -1) {
                this.currentDescriptorText.setText("All dependencies were removed.");
                this.currentDescriptorText.setEnabled(false);
                this.updateDescriptorButton.setEnabled(false);
                this.removeDescriptorButton.setEnabled(false);
                return;
            }
            descriptor = this.modifications.get(index);
            if (descriptor == null)
                descriptor = this.resource.dependencies.get(index);
            this.currentDescriptorText.setText(descriptor.toString());
            this.currentDescriptorText.setEnabled(true);
            this.updateDescriptorButton.setEnabled(false);
        });
        
        this.currentDescriptorText.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onTextChange(); }
            @Override public void removeUpdate(DocumentEvent e) { onTextChange(); }
            @Override public void changedUpdate(DocumentEvent e) { return; }
        });
        
        this.descriptorList.setSelectedIndex(0);

        this.setVisible(true);
    }
    
    private void onTextChange() {
        String text = this.currentDescriptorText.getText();
        text = text.replaceAll("\\s", "");

        boolean isGUID = Strings.isGUID(text);
        if (!(isGUID || Strings.isSHA1(text))) {
            this.updateDescriptorButton.setEnabled(false);
            return;
        }

        ResourceDescriptor newDescriptor = new ResourceDescriptor(
                text,
                this.dependencies.get(this.descriptorList.getSelectedIndex()).getType()
        );

        // If the resource type is music settings or fsb (filename), it can only take in GUIDs
        if ((newDescriptor.getType().equals(ResourceType.MUSIC_SETTINGS) 
            || newDescriptor.getType().equals(ResourceType.FILENAME)
            || newDescriptor.getType().equals(ResourceType.FILE_OF_BYTES) 
            || newDescriptor.getType().equals(ResourceType.SAMPLE)) && !isGUID) {
            this.updateDescriptorButton.setEnabled(false);
            return;
        }

        this.updateDescriptorButton.setEnabled(!this.modifications.contains(newDescriptor));
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        descriptorContainer = new javax.swing.JScrollPane();
        descriptorList = new javax.swing.JList<>();
        currentDescriptorText = new javax.swing.JTextField();
        saveChangesButton = new javax.swing.JButton();
        updateDescriptorButton = new javax.swing.JButton();
        removeDescriptorButton = new javax.swing.JButton();

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

        removeDescriptorButton.setText("Remove");
        removeDescriptorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDescriptorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(updateDescriptorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(removeDescriptorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(saveChangesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(descriptorContainer)
                    .addComponent(currentDescriptorText))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptorContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(currentDescriptorText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updateDescriptorButton)
                    .addComponent(removeDescriptorButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveChangesButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateDescriptorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateDescriptorButtonActionPerformed
        int index = this.descriptorList.getSelectedIndex();
        ResourceDescriptor newDescriptor = new ResourceDescriptor(
                this.currentDescriptorText.getText(),
                this.dependencies.get(index).getType()
        );
        
        if (newDescriptor.equals(this.dependencies.get(index))) return;
        if (this.modifications.get(index) != null)
            if (newDescriptor.equals(this.modifications.get(index))) 
                return;
        
        this.modifications.set(index, newDescriptor);
        
        System.out.println("Set " + this.dependencies.get(index) + " -> " + newDescriptor);
        
        FileEntry entry = ResourceSystem.findEntry(newDescriptor);
        if (entry == null || entry.getPath() == null)
            model.setElementAt(newDescriptor.toString() + " (" + newDescriptor.getType().name() + ")", index);
        else model.setElementAt(Paths.get(entry.getPath()).getFileName().toString(), index);
        
        this.updateDescriptorButton.setEnabled(false);
        this.saveChangesButton.setEnabled(true);
    }//GEN-LAST:event_updateDescriptorButtonActionPerformed

    private void saveChangesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveChangesButtonActionPerformed
        for (ResourceDescriptor descriptor : this.removed) {
            System.out.println("Removing dependency -> " + descriptor);
            this.resource.replaceDependency(descriptor, null);   
        }
        
        for (int i = 0; i < this.modifications.size(); ++i) {
            ResourceDescriptor oldDescriptor = this.dependencies.get(i);
            ResourceDescriptor newDescriptor = this.modifications.get(i);
            if (newDescriptor.equals(oldDescriptor)) continue;
            System.out.println(newDescriptor + " : " + oldDescriptor);
            this.resource.replaceDependency(oldDescriptor, newDescriptor);
        }
        
        ResourceSystem.replaceEntry(entry, resource.compressToResource());
        this.dispose();
    }//GEN-LAST:event_saveChangesButtonActionPerformed

    private void removeDescriptorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDescriptorButtonActionPerformed
        int index = this.descriptorList.getSelectedIndex();
        
        if (this.model.size() - 1 != 0) {
            if (index == 0)
                this.descriptorList.setSelectedIndex(index + 1);
            else
                this.descriptorList.setSelectedIndex(index - 1);   
        }
        
        this.removed.add(this.dependencies.get(index));
        this.dependencies.remove(index);
        this.modifications.remove(index);
        this.model.remove(index);
        
        this.saveChangesButton.setEnabled(true);
    }//GEN-LAST:event_removeDescriptorButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField currentDescriptorText;
    private javax.swing.JScrollPane descriptorContainer;
    private javax.swing.JList<String> descriptorList;
    private javax.swing.JButton removeDescriptorButton;
    private javax.swing.JButton saveChangesButton;
    private javax.swing.JButton updateDescriptorButton;
    // End of variables declaration//GEN-END:variables
}