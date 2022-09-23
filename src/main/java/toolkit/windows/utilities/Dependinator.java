package toolkit.windows.utilities;

import cwlib.types.databases.FileEntry;
import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.singleton.ResourceSystem;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.Strings;
import toolkit.windows.Toolkit;

import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Dependinator extends javax.swing.JFrame {
    private Resource resource;
    private FileEntry entry;

    private static class Dependentry {
        private final ResourceDescriptor original;
        private ResourceDescriptor replacement;
        private FileEntry entry;

        private Dependentry(ResourceDescriptor descriptor) {
            this.original = descriptor;
            this.setDescriptor(descriptor);
        }

        private void setDescriptor(ResourceDescriptor descriptor) {
            this.replacement = descriptor;
            this.entry = ResourceSystem.get(descriptor);
        }

        @Override public String toString() {
            String postfix = String.format(" [%s]", this.replacement.getType().toString());
            if (this.entry != null)
                return this.entry.getName() + postfix;
            return this.replacement.toString() + postfix;
        }

        @Override public int hashCode() { return this.replacement.hashCode(); }
        @Override public boolean equals(Object other) { 
            if (other == this) return true;
            if (!(other instanceof Dependentry)) return false;
            return ((Dependentry)other).replacement.equals(this.replacement);
        }
    }
    
    private ArrayList<ResourceDescriptor> removed = new ArrayList<>();
    
    private DefaultListModel<Dependentry> model = new DefaultListModel<>();
    
    public Dependinator(Toolkit toolkit, FileEntry entry) {
        this.initComponents();
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        this.setTitle("Dependinator");
        
        this.entry = entry;
        
        // Get the resource data
        byte[] data = ResourceSystem.extract(entry);
        if (data == null) {
            this.dispose();
            return;
        }
        
        this.resource = new Resource(data);

        for (ResourceDescriptor descriptor : this.resource.getDependencies())
            this.model.addElement(new Dependentry(descriptor));
        
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
            descriptor = ((Dependentry)this.model.getElementAt(index)).replacement;
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

        boolean isSHA1 = Strings.isSHA1(text);
        boolean isGUID = Strings.isGUID(text);
        if (isSHA1) isGUID = false;

        if (!(isGUID || isSHA1)) {
            this.updateDescriptorButton.setEnabled(false);
            return;
        }
        
        Dependentry dependentry = ((Dependentry)this.model.getElementAt(this.descriptorList.getSelectedIndex()));
        ResourceDescriptor newDescriptor = new ResourceDescriptor(
                text,
                dependentry.replacement.getType()
        );

        // If the resource type is music settings or fsb (filename), it can only take in GUIDs
        if ((newDescriptor.getType().equals(ResourceType.MUSIC_SETTINGS) 
            || newDescriptor.getType().equals(ResourceType.FILENAME)
            || newDescriptor.getType().equals(ResourceType.FILE_OF_BYTES) 
            || newDescriptor.getType().equals(ResourceType.SAMPLE)) && !isGUID) {
            this.updateDescriptorButton.setEnabled(false);
            return;
        }

        this.updateDescriptorButton.setEnabled(
            !dependentry.original.equals(newDescriptor) &&
            !dependentry.replacement.equals(newDescriptor)
        );
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
        if (index == -1) return;

        Dependentry dependentry = (Dependentry) this.model.getElementAt(index);

        String text = this.currentDescriptorText.getText();
        text = text.replaceAll("\\s", "");

        dependentry.setDescriptor(new ResourceDescriptor(
            text,
            dependentry.replacement.getType()
        ));

        this.descriptorList.repaint();
        
        this.updateDescriptorButton.setEnabled(false);
        this.saveChangesButton.setEnabled(true);
    }//GEN-LAST:event_updateDescriptorButtonActionPerformed

    private void saveChangesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveChangesButtonActionPerformed
        for (ResourceDescriptor descriptor : this.removed) {
            System.out.println("Removing dependency -> " + descriptor);
            this.resource.replaceDependency(descriptor, null);   
        }

        
        for (int i = 0; i < this.model.size(); ++i) {
            Dependentry dependentry = (Dependentry) this.model.getElementAt(i);

            ResourceDescriptor oldDescriptor = dependentry.original;
            ResourceDescriptor newDescriptor = dependentry.replacement;

            if (newDescriptor.equals(oldDescriptor)) continue;

            System.out.println(newDescriptor + " : " + oldDescriptor);
            this.resource.replaceDependency(oldDescriptor, newDescriptor);
        }

        byte[] data = resource.compress(resource.getStream().getBuffer());
        ResourceSystem.replace(this.entry, data);

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

        Dependentry dependentry = (Dependentry) this.model.getElementAt(index);
        
        this.removed.add(dependentry.original);
        this.model.remove(index);
        
        this.saveChangesButton.setEnabled(true);
    }//GEN-LAST:event_removeDescriptorButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField currentDescriptorText;
    private javax.swing.JScrollPane descriptorContainer;
    private javax.swing.JList<Dependentry> descriptorList;
    private javax.swing.JButton removeDescriptorButton;
    private javax.swing.JButton saveChangesButton;
    private javax.swing.JButton updateDescriptorButton;
    // End of variables declaration//GEN-END:variables
}