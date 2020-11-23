package ennuo.toolkit.windows;

import ennuo.craftworld.memory.Images;
import ennuo.craftworld.types.Mod;
import ennuo.toolkit.utilities.FileChooser;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ModEditor extends javax.swing.JFrame {
    
    private FileChooser chooser = new FileChooser(this);
    private Mod mod;
    
    private String password;
    private boolean setPassword = false;
    
    
    public ModEditor(Mod mod) {
        initComponents();
        setResizable(false);
        this.mod = mod;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        setTitle("Mod Editor");
        
        
        modID.setText(mod.modID);
        major.setValue(mod.major);
        minor.setValue(mod.minor);
        
        title.setText(mod.title);
        creator.setText(mod.author);
        description.setText(mod.description);
        
        isProtected.setSelected(mod.isProtected);
        
        modIcon.setText("");
        
        if (mod.icon != null)
            modIcon.setIcon(mod.icon);
        else modIcon.setText("Mod has no icon.");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modIcon = new javax.swing.JLabel();
        description = new javax.swing.JTextArea();
        title = new javax.swing.JTextField();
        creator = new javax.swing.JTextField();
        modID = new javax.swing.JTextField();
        minor = new javax.swing.JSpinner();
        major = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        passwordPrompt = new javax.swing.JButton();
        save = new javax.swing.JButton();
        isProtected = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        modIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        modIcon.setText("This mod has no icon.");
        modIcon.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        modIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                modIconMouseClicked(evt);
            }
        });

        description.setColumns(20);
        description.setLineWrap(true);
        description.setRows(5);
        description.setWrapStyleWord(true);

        minor.setModel(new javax.swing.SpinnerNumberModel(0, 0, 99, 1));

        major.setModel(new javax.swing.SpinnerNumberModel(0, 0, 99, 1));

        jLabel1.setText("Title");

        jLabel2.setText("Author");

        jLabel3.setText("Description");

        jLabel4.setText("Mod ID");

        jLabel5.setText("Mod Version");

        passwordPrompt.setText("Set Password");
        passwordPrompt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordPromptActionPerformed(evt);
            }
        });

        save.setText("Save");
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        isProtected.setText("Is Protected");
        isProtected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isProtectedActionPerformed(evt);
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
                        .addComponent(modIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(isProtected)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(passwordPrompt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel1))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(creator, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                                            .addComponent(title)))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(jLabel5)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(major, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(minor))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(jLabel4)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(modID))
                                        .addComponent(description, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(save, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(modIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(creator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(description)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(modID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(major, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(passwordPrompt)
                            .addComponent(isProtected))
                        .addGap(6, 6, 6)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(save)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        mod.icon = (ImageIcon) modIcon.getIcon();
        mod.modID = modID.getText();
        mod.major = (int) major.getValue();
        mod.minor = (int) minor.getValue();
        mod.title = title.getText();
        mod.author = creator.getText();
        mod.description = description.getText();
        mod.shouldSave = true;
        if (setPassword)
            mod.password = password;
        mod.isProtected = isProtected.isSelected();
        System.out.println("Updated mod metadata!");
        dispose();
    }//GEN-LAST:event_saveActionPerformed

    private void modIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modIconMouseClicked
        try {
            File file = chooser.openFile("icon.png","png", "Portable Network Graphics (.PNG)", false);
            if (file == null) return;
            BufferedImage image = ImageIO.read(file);
            if (image == null) return;
            modIcon.setIcon(Images.getImageIcon(image, 256, 256));
        } catch (IOException ex) { System.err.println("Failed to set icon."); }
    }//GEN-LAST:event_modIconMouseClicked

    private void passwordPromptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordPromptActionPerformed
        String password = JOptionPane.showInputDialog(this, "Set new password", "password");  
        if (password == null) return;
        setPassword = true;
        this.password = password;
        System.out.println("Set password = " + password);
    }//GEN-LAST:event_passwordPromptActionPerformed

    private void isProtectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isProtectedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_isProtectedActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField creator;
    private javax.swing.JTextArea description;
    private javax.swing.JCheckBox isProtected;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JSpinner major;
    private javax.swing.JSpinner minor;
    private javax.swing.JTextField modID;
    private javax.swing.JLabel modIcon;
    private javax.swing.JButton passwordPrompt;
    private javax.swing.JButton save;
    private javax.swing.JTextField title;
    // End of variables declaration//GEN-END:variables
}
