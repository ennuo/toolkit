package toolkit.windows.managers;

import configurations.ApplicationFlags;
import configurations.Config;
import java.awt.Frame;
import javax.swing.ImageIcon;

public class SettingsManager extends javax.swing.JDialog {
    public SettingsManager(Frame parent) {
        super(parent, true);
        this.initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        this.setLocationRelativeTo(parent);
        this.setResizable(false);
        
        Config config = Config.instance;
        
        // Might be better to just store an array of Option objects
        // and automate this, but whatever works honestly.
        
        this.isDebugCheckbox.setSelected(config.isDebug);
        this.useLegacyFileDialogueCheckbox.setSelected(config.useLegacyFileDialogue);
        this.displayWarningOnDeletingEntryCheckbox.setSelected(config.displayWarningOnDeletingEntry);
        this.displayWarningOnZeroEntryCheckbox.setSelected(config.displayWarningOnZeroEntry);
        this.addToArchiveOnCopyCheckbox.setSelected(config.addToArchiveOnCopy);
        this.enable3DCheckbox.setSelected(config.enable3D);
        
        this.isDebugCheckbox.addActionListener(l -> config.isDebug = this.isDebugCheckbox.isSelected());
        this.useLegacyFileDialogueCheckbox.addActionListener(l -> config.useLegacyFileDialogue = this.useLegacyFileDialogueCheckbox.isSelected());
        this.displayWarningOnDeletingEntryCheckbox.addActionListener(l -> config.displayWarningOnDeletingEntry = this.displayWarningOnDeletingEntryCheckbox.isSelected());
        this.displayWarningOnZeroEntryCheckbox.addActionListener(l -> config.displayWarningOnZeroEntry = this.displayWarningOnZeroEntryCheckbox.isSelected());
        this.addToArchiveOnCopyCheckbox.addActionListener(l -> config.addToArchiveOnCopy = this.addToArchiveOnCopyCheckbox.isSelected());
        this.enable3DCheckbox.addActionListener(l -> config.enable3D = this.enable3DCheckbox.isSelected());
        
        this.enable3DCheckbox.setEnabled(ApplicationFlags.IS_WINDOWS);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        isDebugCheckbox = new javax.swing.JCheckBox();
        isDebugLabel = new javax.swing.JLabel();
        useLegacyFileDialogueCheckbox = new javax.swing.JCheckBox();
        useLegacyFileDialogueLabel = new javax.swing.JLabel();
        displayWarningOnDeletingEntryCheckbox = new javax.swing.JCheckBox();
        displayWarningOnDeletingEntryLabel = new javax.swing.JLabel();
        displayWarningOnZeroEntryCheckbox = new javax.swing.JCheckBox();
        displayWarningOnZeroEntryLabel = new javax.swing.JLabel();
        addToArchiveOnCopyCheckbox = new javax.swing.JCheckBox();
        addToArchiveOnCopyLabel = new javax.swing.JLabel();
        enable3DCheckbox = new javax.swing.JCheckBox();
        enable3DLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Settings");

        isDebugCheckbox.setText("Debug Mode");
        isDebugCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isDebugCheckboxActionPerformed(evt);
            }
        });

        isDebugLabel.setText("Toolkit will launch in debug mode, this generally has no impact on user experience.");

        useLegacyFileDialogueCheckbox.setText("Use Legacy File Dialogue");

        useLegacyFileDialogueLabel.setText("A fallback dialogue will be used when opening files, this should be used on macOS, as the native dialogue has issues.");

        displayWarningOnDeletingEntryCheckbox.setText("Delete Entry Confirmation");

        displayWarningOnDeletingEntryLabel.setText("A confirmation prompt will appear when deleting entries.");

        displayWarningOnZeroEntryCheckbox.setText("Zero Entry Confirmation");

        displayWarningOnZeroEntryLabel.setText("A confirmation prompt will appear when zeroing entries.");

        addToArchiveOnCopyCheckbox.setText("Add to Archive on Copy");

        addToArchiveOnCopyLabel.setText("When copying entries between databases, a prompt will appear asking if you want to copy data to a new archive.");

        enable3DCheckbox.setText("Enable 3D (Experimental)");

        enable3DLabel.setText("Enables 3D viewport, as well as other 3D features. This only works on Windows!");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(useLegacyFileDialogueCheckbox)
                    .addComponent(isDebugCheckbox)
                    .addComponent(displayWarningOnDeletingEntryCheckbox)
                    .addComponent(displayWarningOnZeroEntryCheckbox)
                    .addComponent(addToArchiveOnCopyCheckbox)
                    .addComponent(enable3DCheckbox)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(enable3DLabel)
                            .addComponent(addToArchiveOnCopyLabel)
                            .addComponent(displayWarningOnZeroEntryLabel)
                            .addComponent(displayWarningOnDeletingEntryLabel)
                            .addComponent(useLegacyFileDialogueLabel)
                            .addComponent(isDebugLabel))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(isDebugCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(isDebugLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useLegacyFileDialogueCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useLegacyFileDialogueLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayWarningOnDeletingEntryCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayWarningOnDeletingEntryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayWarningOnZeroEntryCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayWarningOnZeroEntryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addToArchiveOnCopyCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addToArchiveOnCopyLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(enable3DCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(enable3DLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void isDebugCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isDebugCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_isDebugCheckboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addToArchiveOnCopyCheckbox;
    private javax.swing.JLabel addToArchiveOnCopyLabel;
    private javax.swing.JCheckBox displayWarningOnDeletingEntryCheckbox;
    private javax.swing.JLabel displayWarningOnDeletingEntryLabel;
    private javax.swing.JCheckBox displayWarningOnZeroEntryCheckbox;
    private javax.swing.JLabel displayWarningOnZeroEntryLabel;
    private javax.swing.JCheckBox enable3DCheckbox;
    private javax.swing.JLabel enable3DLabel;
    private javax.swing.JCheckBox isDebugCheckbox;
    private javax.swing.JLabel isDebugLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox useLegacyFileDialogueCheckbox;
    private javax.swing.JLabel useLegacyFileDialogueLabel;
    // End of variables declaration//GEN-END:variables
}
