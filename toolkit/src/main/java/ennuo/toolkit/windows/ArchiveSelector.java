package ennuo.toolkit.windows;

import ennuo.craftworld.types.FileArchive;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;

public class ArchiveSelector extends javax.swing.JDialog {
    
    private DefaultListModel model = new DefaultListModel();
    private Toolkit toolkit;
    
    private FileArchive[] selectedArchives;
    
    public ArchiveSelector(Toolkit toolkit, boolean modal) {
        super(toolkit, modal);
        this.toolkit = toolkit;
        initComponents();
        
        setTitle("Archive Selector");
        setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        
        for (FileArchive archive : toolkit.archives)
            model.addElement(archive.file.getName());
        
        setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        archiveList = new javax.swing.JList<>();
        selectArchive = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        archiveList.setModel(model);
        jScrollPane1.setViewportView(archiveList);

        selectArchive.setText("Select");
        selectArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectArchiveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                    .addComponent(selectArchive, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectArchive)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectArchiveActionPerformed
        int[] selected = archiveList.getSelectedIndices();
        if (selected.length == 0) return;
        selectedArchives = new FileArchive[selected.length];
        for (int i = 0; i < selected.length; ++i)
            selectedArchives[i] = toolkit.archives.get(selected[i]);
        dispose();
    }//GEN-LAST:event_selectArchiveActionPerformed

    public FileArchive[] getSelected() { return selectedArchives; }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> archiveList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton selectArchive;
    // End of variables declaration//GEN-END:variables
}
