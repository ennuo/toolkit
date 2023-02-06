package toolkit.windows.managers;

import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import configurations.Config;
import configurations.Profile;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import toolkit.utilities.FileChooser;

public class ProfileManager extends javax.swing.JDialog {
    DefaultListModel profileModel = new DefaultListModel();
    DefaultListModel archiveModel = new DefaultListModel();
    DefaultListModel databaseModel = new DefaultListModel();
    DefaultListModel saveModel = new DefaultListModel();
    
    public ProfileManager(JFrame parent) {
        super(parent, "Profile Manager", true);
        this.initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        this.setResizable(false);
        this.setLocationRelativeTo(parent);
        this.setModal(true);
        
        for (Profile profile : Config.instance.profiles)
            this.profileModel.addElement(profile);
        
        // We need at least one profile.
        if (this.profileModel.size() <= 1)
            this.removeProfileButton.setEnabled(false);
        
        this.profilesList.setModel(this.profileModel);
        this.archiveList.setModel(this.archiveModel);
        this.databaseList.setModel(this.databaseModel);
        this.saveList.setModel(this.saveModel);
        
        this.profilesList.addListSelectionListener(listener -> {
            this.selectProfile(this.profilesList.getSelectedIndex());
        });
        
        this.profileNameTextEntry.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onProfileNameChange(); }
            @Override public void removeUpdate(DocumentEvent e) { onProfileNameChange(); }
            @Override public void changedUpdate(DocumentEvent e) { return; }
        });
        
        this.removeProfileButton.addActionListener(e -> {
            Profile profile = this.getSelectedProfile();
            int index = this.profilesList.getSelectedIndex();
            
            if (index == 0)
                this.profilesList.setSelectedIndex(index + 1);
            else
                this.profilesList.setSelectedIndex(index - 1);
            
            Config.removeProfile(index);
            this.profileModel.removeElement(profile);
           
            this.profilesList.repaint();
            if (this.profileModel.size() == 1)
                this.removeProfileButton.setEnabled(false);
        });
        
        this.addProfileButton.addActionListener(e -> {
            this.profileModel.addElement(Config.newProfile());
            this.profilesList.repaint();
            this.removeProfileButton.setEnabled(true);
        });
        
        this.addArchiveButton.addActionListener(e -> {
            Profile profile = this.getSelectedProfile();
            File archive = FileChooser.openFile("data.farc", "farc", false);
            if (archive == null) return;
            String path = archive.getAbsolutePath();
            if (profile.archives.contains(path))
                return;
            profile.archives.add(path);
            this.archiveModel.addElement(path);
        });
        
        this.addDatabaseButton.addActionListener(e -> {
            Profile profile = this.getSelectedProfile();
            File database = FileChooser.openFile("blurayguids.map", "map", false);
            if (database == null) return;
            String path = database.getAbsolutePath();
            if (profile.databases.contains(path))
                return;
            profile.databases.add(path);
            this.databaseModel.addElement(path);
        });
        
        this.addSaveButton.addActionListener(e -> {
            Profile profile = this.getSelectedProfile();
            String path = FileChooser.openDirectory();
            if (path == null) return;
            if (profile.saves.contains(path))
                return;
            profile.saves.add(path);
            this.saveModel.addElement(path);
        });
        
        this.removeArchiveButton.addActionListener(e -> {
            Profile profile = this.getSelectedProfile();
            String path = this.archiveList.getSelectedValue();
            profile.archives.remove(path);
            this.archiveModel.removeElement(path);
        });
        
        this.removeDatabaseButton.addActionListener(e -> {
            Profile profile = this.getSelectedProfile();
            String path = this.databaseList.getSelectedValue();
            profile.databases.remove(path);
            this.databaseModel.removeElement(path);
        });
        
        this.removeSaveButton.addActionListener(e -> {
            Profile profile = this.getSelectedProfile();
            String path = this.databaseList.getSelectedValue();
            profile.saves.remove(path);
            this.saveModel.removeElement(path);
        });
        
        this.selectProfileButton.addActionListener(e -> {
            this.selectProfileButton.setEnabled(false);
            Config.instance.currentProfile = this.profilesList.getSelectedIndex();
        });
        
        this.closeButton.addActionListener(e -> this.dispose());
        
        this.profilesList.setSelectedIndex(Config.instance.currentProfile);
    }
    
    private void onProfileNameChange() {
        this.getSelectedProfile().name = this.profileNameTextEntry.getText();
        this.profilesList.repaint();
    }
    
    private Profile getSelectedProfile() {
        return (Profile) this.profileModel.getElementAt(this.profilesList.getSelectedIndex());
    }
    
    private void selectProfile(int index) {
        Profile profile = (Profile) this.profileModel.getElementAt(index);
        
        this.selectProfileButton.setEnabled(!(index == Config.instance.currentProfile));
        
        this.profileNameTextEntry.setText(profile.name);
        
        this.databaseModel.clear();
        if (profile.databases != null)
            for (String path : profile.databases)
                this.databaseModel.addElement(path);
        
        this.archiveModel.clear();
        if (profile.archives != null)
            for (String path : profile.archives)
                this.archiveModel.addElement(path);
        
        this.saveModel.clear();
        if (profile.saves != null)
            for (String path : profile.saves)
                this.saveModel.addElement(path);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoLabel1 = new javax.swing.JLabel();
        infoLabel2 = new javax.swing.JLabel();
        profilesLabel = new javax.swing.JLabel();
        addProfileButton = new javax.swing.JButton();
        removeProfileButton = new javax.swing.JButton();
        profilesContainer = new javax.swing.JScrollPane();
        profilesList = new javax.swing.JList<>();
        profileNameLabel = new javax.swing.JLabel();
        profileNameTextEntry = new javax.swing.JTextField();
        preloadTabPanel = new javax.swing.JTabbedPane();
        databasePanel = new javax.swing.JPanel();
        databaseContainer = new javax.swing.JScrollPane();
        databaseList = new javax.swing.JList<>();
        addDatabaseButton = new javax.swing.JButton();
        removeDatabaseButton = new javax.swing.JButton();
        archivePanel = new javax.swing.JPanel();
        archiveContainer = new javax.swing.JScrollPane();
        archiveList = new javax.swing.JList<>();
        addArchiveButton = new javax.swing.JButton();
        removeArchiveButton = new javax.swing.JButton();
        savePanel = new javax.swing.JPanel();
        saveContainer = new javax.swing.JScrollPane();
        saveList = new javax.swing.JList<>();
        addSaveButton = new javax.swing.JButton();
        removeSaveButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        selectProfileButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Profile Manager");

        infoLabel1.setText("Profiles can control various settings in Craftworld Toolkit. ");

        infoLabel2.setText("They also allow you to auto-open archives, as well as pre-loading language data.");

        profilesLabel.setText("Profiles:");

        addProfileButton.setText("Add Profile");
        addProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProfileButtonActionPerformed(evt);
            }
        });

        removeProfileButton.setText("Remove");

        profilesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        profilesContainer.setViewportView(profilesList);

        profileNameLabel.setText("Profile Name:");

        databaseContainer.setViewportView(databaseList);

        addDatabaseButton.setText("Add ");

        removeDatabaseButton.setText("Remove");

        javax.swing.GroupLayout databasePanelLayout = new javax.swing.GroupLayout(databasePanel);
        databasePanel.setLayout(databasePanelLayout);
        databasePanelLayout.setHorizontalGroup(
            databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databasePanelLayout.createSequentialGroup()
                .addComponent(databaseContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addDatabaseButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeDatabaseButton, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                .addContainerGap())
        );
        databasePanelLayout.setVerticalGroup(
            databasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(databaseContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(databasePanelLayout.createSequentialGroup()
                .addComponent(addDatabaseButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeDatabaseButton)
                .addContainerGap(77, Short.MAX_VALUE))
        );

        preloadTabPanel.addTab("Databases", databasePanel);

        archiveContainer.setViewportView(archiveList);

        addArchiveButton.setText("Add ");

        removeArchiveButton.setText("Remove");

        javax.swing.GroupLayout archivePanelLayout = new javax.swing.GroupLayout(archivePanel);
        archivePanel.setLayout(archivePanelLayout);
        archivePanelLayout.setHorizontalGroup(
            archivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(archivePanelLayout.createSequentialGroup()
                .addComponent(archiveContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(archivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addArchiveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeArchiveButton, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                .addContainerGap())
        );
        archivePanelLayout.setVerticalGroup(
            archivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(archiveContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(archivePanelLayout.createSequentialGroup()
                .addComponent(addArchiveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeArchiveButton)
                .addContainerGap(77, Short.MAX_VALUE))
        );

        preloadTabPanel.addTab("Archives", archivePanel);

        saveContainer.setViewportView(saveList);

        addSaveButton.setText("Add ");

        removeSaveButton.setText("Remove");

        javax.swing.GroupLayout savePanelLayout = new javax.swing.GroupLayout(savePanel);
        savePanel.setLayout(savePanelLayout);
        savePanelLayout.setHorizontalGroup(
            savePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(savePanelLayout.createSequentialGroup()
                .addComponent(saveContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(savePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addSaveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeSaveButton, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                .addContainerGap())
        );
        savePanelLayout.setVerticalGroup(
            savePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(saveContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(savePanelLayout.createSequentialGroup()
                .addComponent(addSaveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeSaveButton)
                .addContainerGap(77, Short.MAX_VALUE))
        );

        preloadTabPanel.addTab("Saves", savePanel);

        closeButton.setBackground(javax.swing.UIManager.getDefaults().getColor("Actions.Blue"));
        closeButton.setText("Close");

        selectProfileButton.setText("Select");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(infoLabel1)
                            .addComponent(infoLabel2)
                            .addComponent(profilesLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addProfileButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeProfileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(profilesContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(preloadTabPanel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(profileNameLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(profileNameTextEntry))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(305, 305, 305)
                                .addComponent(selectProfileButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(closeButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(profilesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(profileNameLabel)
                            .addComponent(profileNameTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(preloadTabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(profilesContainer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(selectProfileButton)
                    .addComponent(addProfileButton)
                    .addComponent(removeProfileButton))
                .addGap(5, 5, 5))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProfileButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addProfileButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addArchiveButton;
    private javax.swing.JButton addDatabaseButton;
    private javax.swing.JButton addProfileButton;
    private javax.swing.JButton addSaveButton;
    private javax.swing.JScrollPane archiveContainer;
    private javax.swing.JList<String> archiveList;
    private javax.swing.JPanel archivePanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JScrollPane databaseContainer;
    private javax.swing.JList<String> databaseList;
    private javax.swing.JPanel databasePanel;
    private javax.swing.JLabel infoLabel1;
    private javax.swing.JLabel infoLabel2;
    private javax.swing.JTabbedPane preloadTabPanel;
    private javax.swing.JLabel profileNameLabel;
    private javax.swing.JTextField profileNameTextEntry;
    private javax.swing.JScrollPane profilesContainer;
    private javax.swing.JLabel profilesLabel;
    private javax.swing.JList<String> profilesList;
    private javax.swing.JButton removeArchiveButton;
    private javax.swing.JButton removeDatabaseButton;
    private javax.swing.JButton removeProfileButton;
    private javax.swing.JButton removeSaveButton;
    private javax.swing.JScrollPane saveContainer;
    private javax.swing.JList<String> saveList;
    private javax.swing.JPanel savePanel;
    private javax.swing.JButton selectProfileButton;
    // End of variables declaration//GEN-END:variables
}
