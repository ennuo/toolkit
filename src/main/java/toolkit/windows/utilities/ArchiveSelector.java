package toolkit.windows.utilities;

import cwlib.singleton.ResourceSystem;
import cwlib.types.archives.Fart;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import toolkit.windows.Toolkit;

public class ArchiveSelector extends javax.swing.JDialog {
    private DefaultListModel<String> model = new DefaultListModel<>();
    private Fart[] selectedArchives;
    
    public ArchiveSelector(Toolkit toolkit, boolean modal) {
        super(toolkit, modal);
        this.initComponents();
        
        this.setTitle("Archive Selector");
        this.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        this.setResizable(false);
        
        for (Fart archive : ResourceSystem.getArchives())
            this.model.addElement(archive.getFile().getAbsolutePath());
        
        this.cancelButton.addActionListener(e -> this.dispose());
        this.archiveList.setSelectedIndex(0);
        
        this.setVisible(true);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        archiveContainer = new javax.swing.JScrollPane();
        archiveList = new javax.swing.JList<>();
        selectButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        archiveLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        archiveList.setModel(model);
        archiveContainer.setViewportView(archiveList);

        selectButton.setText("Select");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");

        archiveLabel.setText("Archives:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(archiveContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(archiveLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(selectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(archiveLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(archiveContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        int[] selected = this.archiveList.getSelectedIndices();
        if (selected.length == 0) return;
        this.selectedArchives = new Fart[selected.length];
        for (int i = 0; i < selected.length; ++i)
            this.selectedArchives[i] = ResourceSystem.getArchives().get(selected[i]);
        this.dispose();
    }//GEN-LAST:event_selectButtonActionPerformed

    public Fart[] getSelected() { return this.selectedArchives; }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane archiveContainer;
    private javax.swing.JLabel archiveLabel;
    private javax.swing.JList<String> archiveList;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton selectButton;
    // End of variables declaration//GEN-END:variables
}
