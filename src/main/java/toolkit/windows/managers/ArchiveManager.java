package toolkit.windows.managers;

import cwlib.ex.SerializationException;
import cwlib.singleton.ResourceSystem;
import cwlib.types.archives.Fart;
import cwlib.types.archives.FileArchive;
import toolkit.utilities.FileChooser;

import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import toolkit.windows.Toolkit;

public class ArchiveManager extends javax.swing.JDialog {
    private final DefaultListModel<String> archiveModel = new DefaultListModel<>();
    
    public ArchiveManager(JFrame parent) {
        super(parent, "Archive Manager", true);
        this.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        this.setResizable(false);
        this.initComponents();
        
        for (Fart archive : ResourceSystem.getArchives())
            archiveModel.addElement(archive.getFile().getAbsolutePath());
        this.archivesList.setModel(this.archiveModel);
        
        this.saveChangesButton.setEnabled(false);
        if (ResourceSystem.getArchives().size() == 0)
            this.removeButton.setEnabled(false);
        
        this.addButton.addActionListener(e -> {
            File file = FileChooser.openFile("data.farc", "farc", false);
            if (file == null) return;
            
            if (Toolkit.INSTANCE.isArchiveLoaded(file) != -1) {
                JOptionPane.showMessageDialog(this, "This archive is already loaded!", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Fart archive = null;
            try { archive = new FileArchive(file); }
            catch (SerializationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "An error occurred", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            ResourceSystem.getArchives().add(archive);
            this.archiveModel.addElement(file.getAbsolutePath());
            
            this.removeButton.setEnabled(true);
            
            Toolkit.INSTANCE.updateWorkspace();
        });
        
        this.removeButton.addActionListener(e -> {
            int index = this.archivesList.getSelectedIndex();
            if (index == -1) return;
            
            Fart archive = ResourceSystem.getArchives().get(index);
            
            if (archive.shouldSave()) {
                int result = JOptionPane.showConfirmDialog(null, "Do you want to save changes before closing this archive?", "Pending changes", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) 
                    archive.save();
            }
            
            if (this.archiveModel.size() - 1 != 0) {
                if (index == 0)
                    this.archivesList.setSelectedIndex(index + 1);
                else
                    this.archivesList.setSelectedIndex(index - 1);   
            }
            
            ResourceSystem.getArchives().remove(index);
            this.archiveModel.removeElementAt(index);
            
            if (this.archiveModel.size() == 0)
                this.removeButton.setEnabled(false);
            
            Toolkit.INSTANCE.updateWorkspace();
        });
        
        this.saveChangesButton.addActionListener(e -> {
            int index = this.archivesList.getSelectedIndex();
            if (index == -1) return;
            
            Fart archive = ResourceSystem.getArchives().get(index);
            
            if (archive.shouldSave())
                archive.save();
            
            this.saveChangesButton.setEnabled(false);
        });
        
        this.archivesList.addListSelectionListener(e -> {
            int index = this.archivesList.getSelectedIndex();
            if (index == -1) return;
            Fart archive = ResourceSystem.getArchives().get(index);
            this.saveChangesButton.setEnabled(archive.shouldSave());
        });
        
        this.closeButton.addActionListener(e -> this.dispose());
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        archivesLabel = new javax.swing.JLabel();
        archiveContainer = new javax.swing.JScrollPane();
        archivesList = new javax.swing.JList<>();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        saveChangesButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Archive Manager");

        archivesLabel.setText("Archives:");

        archivesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        archiveContainer.setViewportView(archivesList);

        addButton.setText("Add");

        removeButton.setText("Remove");

        closeButton.setText("Close");

        saveChangesButton.setText("Save");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(archiveContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveChangesButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(archivesLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(archivesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(archiveContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(removeButton)
                    .addComponent(closeButton)
                    .addComponent(saveChangesButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JScrollPane archiveContainer;
    private javax.swing.JLabel archivesLabel;
    private javax.swing.JList<String> archivesList;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton saveChangesButton;
    // End of variables declaration//GEN-END:variables
}
