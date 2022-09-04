package executables.gfx.dialogues;

import cwlib.enums.ResourceType;
import cwlib.enums.TextureWrap;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.Strings;
import executables.gfx.GfxGUI.TextureEntry;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class TextureDialogue extends javax.swing.JDialog {
    private final TextureEntry entry;
    
    public TextureDialogue(Frame parent, TextureEntry entry) {
        super(parent, true);
        this.initComponents();
        
        this.setTitle("Set Texture");
        
        this.setLocationRelativeTo(parent);
        
        this.entry = entry;
        
        this.textureTextEntry.setText(this.entry.descriptor != null ? this.entry.descriptor.toString() : "");
        this.wrapSCombo.setSelectedItem(this.entry.wrapS);
        this.wrapTCombo.setSelectedItem(this.entry.wrapT);
        
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

        textureLabel = new javax.swing.JLabel();
        textureTextEntry = new javax.swing.JTextField();
        wrapLabel = new javax.swing.JLabel();
        wrapSCombo = new javax.swing.JComboBox(TextureWrap.values());
        editButton = new javax.swing.JButton();
        wrapTCombo = new javax.swing.JComboBox(TextureWrap.values());
        editButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        textureLabel.setText("Texture:");
        textureLabel.setMaximumSize(new java.awt.Dimension(55, 16));
        textureLabel.setMinimumSize(new java.awt.Dimension(55, 16));
        textureLabel.setPreferredSize(new java.awt.Dimension(55, 16));

        wrapLabel.setText("Wrap S/T:");
        wrapLabel.setMaximumSize(new java.awt.Dimension(55, 16));
        wrapLabel.setMinimumSize(new java.awt.Dimension(55, 16));
        wrapLabel.setPreferredSize(new java.awt.Dimension(55, 16));

        editButton.setText("Edit");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        editButton1.setText("Cancel");
        editButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButton1ActionPerformed(evt);
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
                        .addComponent(textureLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textureTextEntry))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(wrapLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(wrapSCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(editButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                            .addComponent(wrapTCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textureLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textureTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wrapLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wrapSCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wrapTCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editButton)
                    .addComponent(editButton1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        String input = this.textureTextEntry.getText();
        input = input.replaceAll("\\s", "");
        if (input.isEmpty()) entry.descriptor = null;
        else if (Strings.isGUID(input) || Strings.isSHA1(input))
            entry.descriptor = new ResourceDescriptor(input, ResourceType.TEXTURE);
        else {
            JOptionPane.showMessageDialog(this, "Invalid texture resource reference!", "An error occurred", JOptionPane.ERROR_MESSAGE);
            return;
        }
        entry.wrapS = (TextureWrap) this.wrapSCombo.getSelectedItem();
        entry.wrapT = (TextureWrap) this.wrapTCombo.getSelectedItem();
        
        this.dispose();
    }//GEN-LAST:event_editButtonActionPerformed

    private void editButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButton1ActionPerformed
        this.dispose();
    }//GEN-LAST:event_editButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton editButton;
    private javax.swing.JButton editButton1;
    private javax.swing.JLabel textureLabel;
    private javax.swing.JTextField textureTextEntry;
    private javax.swing.JLabel wrapLabel;
    private javax.swing.JComboBox<TextureWrap> wrapSCombo;
    private javax.swing.JComboBox<TextureWrap> wrapTCombo;
    // End of variables declaration//GEN-END:variables
}
