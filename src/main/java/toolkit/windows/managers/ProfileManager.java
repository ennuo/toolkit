package toolkit.windows.managers;

import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import configurations.Config;
import configurations.Profile;
import toolkit.utilities.FileChooser;

public class ProfileManager extends javax.swing.JDialog {
    DefaultListModel profileModel = new DefaultListModel();
    DefaultListModel archiveModel = new DefaultListModel();
    DefaultListModel databaseModel = new DefaultListModel();
    
    public ProfileManager(JFrame parent) {
        super(parent, "Profile Manager", true);
        this.initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        this.setResizable(false);
        this.setModal(true);
        
        for (Profile profile : Config.instance.profiles)
            this.profileModel.addElement(profile);
        
        // We need at least one profile.
        if (this.profileModel.size() <= 1)
            this.removeProfileButton.setEnabled(false);
        
        this.profilesList.setModel(this.profileModel);
        this.archiveList.setModel(this.archiveModel);
        this.databaseList.setModel(this.databaseModel);
        
        this.profilesList.addListSelectionListener(listener -> {
            this.selectProfile(this.profilesList.getSelectedIndex());
        });
        
        this.profileNameTextEntry.addActionListener(e -> {
            this.getSelectedProfile().name = this.profileNameTextEntry.getText();
            this.profilesList.repaint();
        });
        
        this.debugModeToggle.addActionListener(e -> this.getSelectedProfile().debug = this.debugModeToggle.isSelected());
        this.useLegacyFileDialogueToggle.addActionListener(e -> 
                this.getSelectedProfile().useLegacyFileDialogue = this.useLegacyFileDialogueToggle.isSelected());
        
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
        
        this.selectProfileButton.addActionListener(e -> {
            this.selectProfileButton.setEnabled(false);
            Config.instance.currentProfile = this.profilesList.getSelectedIndex();
        });
        
        this.closeButton.addActionListener(e -> this.dispose());
        
        this.profilesList.setSelectedIndex(Config.instance.currentProfile);
    }
    
    private Profile getSelectedProfile() {
        return (Profile) this.profileModel.getElementAt(this.profilesList.getSelectedIndex());
    }
    
    private void selectProfile(int index) {
        Profile profile = (Profile) this.profileModel.getElementAt(index);
        
        this.selectProfileButton.setEnabled(!(index == Config.instance.currentProfile));
        
        this.profileNameTextEntry.setText(profile.name);
        this.debugModeToggle.setSelected(profile.debug);
        this.useLegacyFileDialogueToggle.setSelected(profile.useLegacyFileDialogue);
        
        this.databaseModel.clear();
        if (profile.databases != null)
            for (String path : profile.databases)
                this.databaseModel.addElement(path);
        
        this.archiveModel.clear();
        if (profile.archives != null)
            for (String path : profile.archives)
                this.archiveModel.addElement(path);
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
        debugModeToggle = new javax.swing.JCheckBox();
        useLegacyFileDialogueToggle = new javax.swing.JCheckBox();
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

        debugModeToggle.setText("Debug Mode");
        debugModeToggle.setToolTipText("Reveals debug menus not normally visible.");

        useLegacyFileDialogueToggle.setText("Use Legacy File Dialogue");
        useLegacyFileDialogueToggle.setToolTipText("Fall back to the built-in Java Swing file chooser dialogue in case tinyfd fails.");

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
                        .addComponent(profilesContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(preloadTabPanel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(debugModeToggle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(useLegacyFileDialogueToggle)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(profileNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(profileNameTextEntry))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(infoLabel1)
                            .addComponent(infoLabel2)
                            .addComponent(profilesLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addProfileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeProfileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(selectProfileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(profilesContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(profileNameLabel)
                            .addComponent(profileNameTextEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(debugModeToggle)
                            .addComponent(useLegacyFileDialogueToggle))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(preloadTabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addProfileButton)
                    .addComponent(removeProfileButton)
                    .addComponent(closeButton)
                    .addComponent(selectProfileButton))
                .addContainerGap())
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
    private javax.swing.JScrollPane archiveContainer;
    private javax.swing.JList<String> archiveList;
    private javax.swing.JPanel archivePanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JScrollPane databaseContainer;
    private javax.swing.JList<String> databaseList;
    private javax.swing.JPanel databasePanel;
    private javax.swing.JCheckBox debugModeToggle;
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
    private javax.swing.JButton selectProfileButton;
    private javax.swing.JCheckBox useLegacyFileDialogueToggle;
    // End of variables declaration//GEN-END:variables
}
