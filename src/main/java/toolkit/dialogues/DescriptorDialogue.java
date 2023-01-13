package toolkit.dialogues;

import cwlib.enums.ResourceType;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.Strings;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class DescriptorDialogue extends javax.swing.JDialog {
    private ResourceDescriptor descriptor;
    
    private DescriptorDialogue(Frame parent, ResourceDescriptor descriptor) {
        super(parent, true);
        this.initComponents();
        
        this.setTitle("Set Descriptor");
        
        this.setLocationRelativeTo(parent);
        
        this.descriptor = descriptor;
        this.resourceTextEntry.setText(descriptor != null ? descriptor.toString() : "");
        if (descriptor != null)
            this.typeCombo.setSelectedItem(descriptor.getType());
            
        
        this.getRootPane().setDefaultButton(this.editButton);
        
        InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        am.put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        this.setVisible(true);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        resourceLabel = new javax.swing.JLabel();
        resourceTextEntry = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        editButton = new javax.swing.JButton();
        cancelEditButton = new javax.swing.JButton();
        typeCombo = new javax.swing.JComboBox(ResourceType.values());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        resourceLabel.setText("Resource:");
        resourceLabel.setMaximumSize(new java.awt.Dimension(55, 16));
        resourceLabel.setMinimumSize(new java.awt.Dimension(55, 16));
        resourceLabel.setPreferredSize(new java.awt.Dimension(55, 16));

        typeLabel.setText("Type:");
        typeLabel.setMaximumSize(new java.awt.Dimension(55, 16));
        typeLabel.setMinimumSize(new java.awt.Dimension(55, 16));
        typeLabel.setPreferredSize(new java.awt.Dimension(55, 16));

        editButton.setText("Set");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        cancelEditButton.setText("Cancel");
        cancelEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelEditButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resourceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(resourceTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(typeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cancelEditButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(typeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resourceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resourceTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(typeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editButton)
                    .addComponent(cancelEditButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static ResourceDescriptor doDescriptorEntry(JFrame frame, ResourceDescriptor descriptor) {
        DescriptorDialogue dialogue = new DescriptorDialogue(frame, descriptor);
        return dialogue.descriptor;
    }
    
    
    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        String input = this.resourceTextEntry.getText();
        input = input.replaceAll("\\s", "");
        if (input.isEmpty()) descriptor = null;
        else if (Strings.isGUID(input) || Strings.isSHA1(input))
           descriptor = new ResourceDescriptor(input, (ResourceType) this.typeCombo.getSelectedItem());
        else {
            JOptionPane.showMessageDialog(this, "Invalid resource reference!", "An error occurred", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        this.dispose();
    }//GEN-LAST:event_editButtonActionPerformed

    private void cancelEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelEditButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelEditButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelEditButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel resourceLabel;
    private javax.swing.JTextField resourceTextEntry;
    private javax.swing.JComboBox<ResourceType> typeCombo;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
}
