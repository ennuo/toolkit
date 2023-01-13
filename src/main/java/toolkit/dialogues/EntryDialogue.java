package toolkit.dialogues;

import cwlib.types.data.GUID;
import cwlib.types.databases.FileDB;
import cwlib.util.Strings;
import java.awt.Frame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class EntryDialogue extends javax.swing.JDialog {
    private final FileDB database;
    private boolean submit = false;
    
    private boolean isKeyValid = false;
    private boolean isPathValid = false;
    
    public EntryDialogue(Frame parent, FileDB database, String path, GUID guid) {
        super(parent, true);
        if (guid == null)
            guid = database.getNextGUID();
        path = Strings.cleanupPath(path);
        
        this.initComponents();
        this.setLocationRelativeTo(parent);
        this.getRootPane().setDefaultButton(this.createButton);
        
        this.database = database;
        this.pathTextEntry.setText(path);
        if (guid != null)
            this.guidTextEntry.setText(guid.toString());
        
        this.isKeyValid = !database.exists(guid);
        this.isPathValid = path != null && !path.isEmpty();
        
        this.createButton.setEnabled(this.isKeyValid && this.isPathValid);
        
        this.guidTextEntry.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onGUIDChange(); }
            @Override public void removeUpdate(DocumentEvent e) { onGUIDChange(); }
            @Override public void changedUpdate(DocumentEvent e) { return; }
        });
        
        this.pathTextEntry.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onPathChange(); }
            @Override public void removeUpdate(DocumentEvent e) { onPathChange(); }
            @Override public void changedUpdate(DocumentEvent e) { return; }
        });
        
        this.setVisible(true);
    }
    
    public void onGUIDChange() {
        String text = this.guidTextEntry.getText();
        if (Strings.isGUID(text)) {
            GUID guid = Strings.getGUID(this.guidTextEntry.getText());
            this.isKeyValid = !this.database.exists(guid);
        } else this.isKeyValid = false;
        this.createButton.setEnabled(this.isKeyValid && this.isPathValid);
    }
    
    public void onPathChange() {
        String text = this.pathTextEntry.getText();
        this.isPathValid = !Strings.cleanupPath(text).isEmpty();
        this.createButton.setEnabled(this.isKeyValid && this.isPathValid);
    }
    
    public boolean wasSubmitted() { return this.submit; }
    public String getPath() { return Strings.cleanupPath(this.pathTextEntry.getText()); }
    public GUID getGUID() { return Strings.getGUID(this.guidTextEntry.getText()); }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pathLabel = new javax.swing.JLabel();
        guidLabel = new javax.swing.JLabel();
        pathTextEntry = new javax.swing.JTextField();
        guidTextEntry = new javax.swing.JTextField();
        createButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Create Entry");
        setResizable(false);

        pathLabel.setText("Path:");

        guidLabel.setText("GUID:");

        createButton.setText("Create");
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(guidLabel)
                            .addComponent(pathLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pathTextEntry)
                            .addComponent(guidTextEntry)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 238, Short.MAX_VALUE)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(createButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathLabel)
                    .addComponent(pathTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(guidLabel)
                    .addComponent(guidTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cancelButton)
                    .addComponent(createButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        this.submit = true;
        this.dispose();
    }//GEN-LAST:event_createButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.submit = false;
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton createButton;
    private javax.swing.JLabel guidLabel;
    private javax.swing.JTextField guidTextEntry;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JTextField pathTextEntry;
    // End of variables declaration//GEN-END:variables
}
