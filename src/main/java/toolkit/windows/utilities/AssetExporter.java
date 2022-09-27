package toolkit.windows.utilities;

import configurations.ApplicationFlags;
import cwlib.enums.CompressionFlags;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.types.data.SHA1;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.resources.RGfxMaterial;
import cwlib.types.databases.FileEntry;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.mods.Mod;
import executables.gfx.CgAssembler;
import executables.gfx.GfxAssembler;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class AssetExporter extends JDialog {
    private enum MaterialLibrary {
        NONE,
        LBP1,
        LBP2,
        LBP_PS4,
        LBP_VITA
    }
    
    private static MaterialLibrary[] libraries;
    static {
        ArrayList<MaterialLibrary> collection = new ArrayList<>();
        collection.add(MaterialLibrary.NONE);
        
        if (ApplicationFlags.CAN_COMPILE_CELL_SHADERS) {
            collection.add(MaterialLibrary.LBP1);
            collection.add(MaterialLibrary.LBP2);
        }
        if (ApplicationFlags.CAN_COMPILE_ORBIS_SHADERS) {
            collection.add(MaterialLibrary.LBP_PS4);
        }
        
        libraries = collection.toArray(MaterialLibrary[]::new);
    }

    public static enum PackageType {
        MOD("Mod"), 
        LEVEL_BACKUP("Level Backup");
        
        private final String value;
        private PackageType(String value) { this.value = value; }
        @Override public String toString() { return this.value; }
    }
    
    public static enum RootExportType {
        GUID,
        HASH
    };
    
    private class Asset {
        /**
         * Original descriptor of this asset.
         */
        private final ResourceDescriptor descriptor;
        
        /**
         * Associated file entry of this asset.
         */
        private FileEntry entry;
        
        /**
         * Cached data used during recursion phase.
         */
        private byte[] data;
        
        /**
         * Whether or not this resource will be referenced by a hash.
         */
        private boolean hashinate;
        
        /**
         * This user is locked as either a hash or GUID and
         * cannot be switched.
         */
        private boolean locked = false;
        
        /**
         * Whether or not this resource has already been recursed,
         * used for when a resource is referenced by multiple assets,
         * so we don't have to hashinate it again.
         */
        private boolean recursed = false;
        
        public Asset(ResourceDescriptor descriptor) {
            this.descriptor = descriptor;
            this.entry = ResourceSystem.get(descriptor);
        }
        
        @Override public String toString() {
            String prefix = "[GUID] ";
            if (this.hashinate) prefix = "[HASH] ";
            
            if (this.entry != null) {
                String path = this.entry.getPath();
                if (!path.isEmpty()) {
                    String[] files = path.split("/");
                    return prefix + files[files.length - 1];
                }
            }

            return prefix + this.descriptor.toString();
        }
        
        @Override public int hashCode() { return this.descriptor.hashCode(); }
        @Override public boolean equals(Object other) { 
            if (other == this) return true;
            if (!(other instanceof Asset)) return false;
            return ((Asset)other).descriptor.equals(this.descriptor);
        }
    }
    
    private final DefaultListModel<Asset> assetModel = new DefaultListModel<>();
    
    private final Asset root;
    
    // Resources that get treated as GUID by default.
    private static final HashSet<GUID> defaults = new HashSet<>();
    static {
        defaults.add(new GUID(9877l)); // naked_to.mol
        defaults.add(new GUID(9876l)); // naked_he.mol
        defaults.add(new GUID(11166l)); // palette_identity.gmat
        defaults.add(new GUID(3465l)); // general_infinite_mass.mat
        defaults.add(new GUID(11987l)); // general_infinite_mass.mat
    }
    
    private FileEntry entry;
    
    public AssetExporter(FileEntry entry) {
        super(Toolkit.instance, true);
        this.initComponents();
        this.setLocationRelativeTo(Toolkit.instance);
        this.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        this.setResizable(false);
        
        this.entry = entry;
        
        // Backup export is currently disabled/not implemented.
        this.packagingTypeCombo.setEnabled(false);
        
        this.assetList.setModel(this.assetModel);
        
        
        byte[] rootData = ResourceSystem.extract(entry.getSHA1());
        if (rootData == null) {
            JOptionPane.showMessageDialog(this, "Unable to obtain resource data, do you have the archive mounted?", "An error occurred", JOptionPane.ERROR_MESSAGE);
            this.dispose();
            this.root = null;
            return;
        }
        
        Resource resource = null;
        try { resource = new Resource(rootData); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Resource was unable to deserialize!", "An error occurred", JOptionPane.ERROR_MESSAGE);
            this.dispose();
            this.root = null;
            return;
        }
        
        HashSet<Asset> descriptors = new HashSet<>();
        this.getDescriptors(rootData, descriptors);
        
        if (entry.getSource().getType().hasGUIDs())
            this.root = new Asset(new ResourceDescriptor((GUID) entry.getKey() , resource.getResourceType()));
        else
            this.root = new Asset(new ResourceDescriptor(entry.getSHA1() , resource.getResourceType()));
        this.root.data = rootData;
        
        boolean containsGmat = resource.getResourceType().equals(ResourceType.GFX_MATERIAL);  
        for (Asset asset : descriptors) {
            ResourceType type = asset.descriptor.getType();
            
            if (type.equals(ResourceType.GFX_MATERIAL))
                containsGmat = true;
            
            // These resources can only use GUIDs
            boolean isLocked = type.equals(ResourceType.MUSIC_SETTINGS) || 
                                type.equals(ResourceType.FILE_OF_BYTES) || 
                                type.equals(ResourceType.SAMPLE) || 
                                type.equals(ResourceType.FILENAME) ||
                                type.equals(ResourceType.SCRIPT); // These don't technically *have* to be GUID, but they're mostly useless otherwise.
            asset.locked = isLocked;
            boolean shouldUseGUID = isLocked || defaults.contains(asset.descriptor.getGUID());
            
            if (shouldUseGUID)
                asset.hashinate = false;
            else {
                asset.hashinate = true;
                if (asset.entry == null || asset.entry.getKey() == null)
                    asset.locked = true;
            }
            
            if (asset.data == null) {
                asset.hashinate = asset.descriptor.isHash();
                asset.locked = true;
            }
            
            this.assetModel.addElement(asset);
        }
        
	this.materialLibraryCombo.setEnabled(containsGmat && libraries.length != 1);
        
        this.assetList.addListSelectionListener(listener -> {
            this.updateButtonStates();
        });
        
        this.assetList.setSelectedIndex(0);
        
        this.pack();
    }
    
    private void finalize(String path) {
        ArrayList<Asset> assets = new ArrayList<>(this.assetModel.size() + 1);
        assets.add(this.root);
        for (int i = 0; i < this.assetModel.size(); ++i)
            assets.add((Asset) this.assetModel.getElementAt(i));
        MaterialLibrary library = (MaterialLibrary) this.materialLibraryCombo.getSelectedItem();
        
        // Add remap support later
        this.recurse(this.root, assets, library);
        
        Mod mod = new Mod();
        
        for (Asset asset : assets) {
            if (asset.data == null) continue;
            if (asset.entry == null || asset.entry.getKey() == null) {
                if (asset.entry != null && !asset.entry.getPath().isEmpty())
                    mod.add(asset.entry.getPath(), asset.data);
                else {
                    String sha1 = SHA1.fromBuffer(asset.data).toString();
                    mod.add("resources/" + sha1, asset.data);
                }
            } else mod.add(asset.entry.getPath(), asset.data, (GUID) asset.entry.getKey());
        }
        
        mod.save(new File(path));
        
        this.dispose();
    }
    
    private Asset getAsset(ArrayList<Asset> assets, ResourceDescriptor descriptor) {
        for (Asset asset : assets)
            if (asset.descriptor.equals(descriptor))
                return asset;
        return null;
    }
    
    private ResourceDescriptor recurse(Asset asset, ArrayList<Asset> assets, MaterialLibrary remap) {
        if (asset.data == null) return asset.descriptor;

        Resource resource = new Resource(asset.data);
        if (resource.getSerializationType() != SerializationType.BINARY || asset.recursed) {
            if (asset.hashinate)
                return new ResourceDescriptor(SHA1.fromBuffer(asset.data), asset.descriptor.getType());
            return new ResourceDescriptor((GUID) asset.entry.getKey(), asset.descriptor.getType());
        }

        ResourceDescriptor[] dependencies = resource.getDependencies();
        for (int i = 0; i < dependencies.length; ++i) {
            ResourceDescriptor dependencyDescriptor = dependencies[i];
            if (dependencyDescriptor.getType() == ResourceType.SCRIPT) continue;
            Asset dependencyAsset = this.getAsset(assets, dependencyDescriptor);
            if (dependencyAsset != null)
                resource.replaceDependency(dependencyDescriptor, this.recurse(dependencyAsset, assets, remap));
        }

        // if (resource.getResourceType() == ResourceType.PLAN && asset.hashinate && asset.entry.GUID != -1)
        //     RPlan.removePlanDescriptors(resource, asset.entry.GUID);

        byte[] data = null;
        if (remap != MaterialLibrary.NONE && resource.getResourceType().equals(ResourceType.GFX_MATERIAL)) {
            RGfxMaterial gfx = resource.loadResource(RGfxMaterial.class);

            boolean isCGB = remap != MaterialLibrary.LBP1;
            boolean isOrbis = remap == MaterialLibrary.LBP_PS4;

            gfx.flags = gfx.flags & ~(0x10000);
            Revision revision = new Revision(0x272, 0x4c44, 0x13);
            if (isCGB) {
                revision = new Revision(0x393);
                gfx.shaders = new byte[10][];
            }
            else gfx.shaders = new byte[4][];

            try {
                CgAssembler.compile(GfxAssembler.generateBRDF(gfx, -1), gfx, isCGB, isOrbis);
                data =  Resource.compress(gfx.build(revision, CompressionFlags.USE_ALL_COMPRESSION));
            } catch (Exception ex)  { data = resource.compress(); }
            
        } else data = resource.compress();
        asset.data = data;


        if (asset.hashinate)
            return new ResourceDescriptor(SHA1.fromBuffer(asset.data), asset.descriptor.getType());
        return new ResourceDescriptor((GUID) asset.entry.getKey(), asset.descriptor.getType());
    }
    
    private void getDescriptors(byte[] resource, HashSet<Asset> descriptors) {
        if (resource == null) return;
        MemoryInputStream data = new MemoryInputStream(resource);
        // Parse the resource manually, since the resource class auto-decompresses,
        // saves some time.
        ResourceType type = ResourceType.fromMagic(data.str(3));
        if (type == ResourceType.INVALID) return;
        SerializationType method = SerializationType.fromValue(data.str(1));
        if (method != SerializationType.BINARY && method != SerializationType.ENCRYPTED_BINARY)
            return;
        int revision = data.i32();
        // Dependency table didn't exist before this revision.
        if (revision < 0x109) return;
        data.seek(data.i32(), SeekMode.Begin);
        int count = data.i32();
        for (int i = 0; i < count; ++i) {
            Asset asset = null;
            switch (data.i8()) {
                case 1: 
                    asset = new Asset(new ResourceDescriptor(data.sha1(), ResourceType.fromType(data.i32())));
                    break;
                case 2:
                    asset = new Asset(new ResourceDescriptor(data.u32(), ResourceType.fromType(data.i32())));
                    break;
            }
            if (asset != null) {
                if (descriptors.contains(asset)) continue;
                descriptors.add(asset);
                asset.data = ResourceSystem.extract(asset.descriptor);
                if (asset.data != null)
                    this.getDescriptors(asset.data, descriptors);
            }
        }
        
    }
    
    private boolean areAllAssetsHash() {
        for (int i = 0; i < this.assetModel.size(); ++i) {
            Asset asset = (Asset) this.assetModel.getElementAt(i);
            if (!asset.locked & !asset.hashinate)
                return false;
        }
        return true;
    }
    
    private boolean areAllAssetsGUID() {
        for (int i = 0; i < this.assetModel.size(); ++i) {
            Asset asset = (Asset) this.assetModel.getElementAt(i);
            if (!asset.locked && asset.hashinate)
                return false;
        }
        return true;
    }
    
    private void updateButtonStates() {
        this.markAllAsGUIDButton.setEnabled(!this.areAllAssetsGUID());
        this.markAllAsHashButton.setEnabled(!this.areAllAssetsHash());
        int index = this.assetList.getSelectedIndex();
        if (index == -1) {
            this.switchReferenceTypeButton.setEnabled(false);
            return;
        }
        Asset asset = (Asset) this.assetModel.getElementAt(index);
        this.switchReferenceTypeButton.setText("Switch to " + ((asset.hashinate) ? "GUID" : "HASH"));
        this.switchReferenceTypeButton.setEnabled(!asset.locked);
            
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        packagingTypeLabel = new javax.swing.JLabel();
        packagingTypeCombo = new javax.swing.JComboBox<>(PackageType.values());
        materialLibraryLabel = new javax.swing.JLabel();
        materialLibraryCombo = new javax.swing.JComboBox<>(libraries);
        exportButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        switchReferenceTypeButton = new javax.swing.JButton();
        markAllAsGUIDButton = new javax.swing.JButton();
        markAllAsHashButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        assetList = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Asset Exporter");

        packagingTypeLabel.setText("Packaging:");

        materialLibraryLabel.setText("Material Transfer Library:");

        exportButton.setText("Export");
        exportButton.setMaximumSize(new java.awt.Dimension(125, 25));
        exportButton.setMinimumSize(new java.awt.Dimension(125, 25));
        exportButton.setPreferredSize(new java.awt.Dimension(125, 25));
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Resource References:");

        switchReferenceTypeButton.setText("Switch to HASH");
        switchReferenceTypeButton.setMaximumSize(new java.awt.Dimension(125, 25));
        switchReferenceTypeButton.setMinimumSize(new java.awt.Dimension(125, 25));
        switchReferenceTypeButton.setPreferredSize(new java.awt.Dimension(125, 25));
        switchReferenceTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchReferenceTypeButtonActionPerformed(evt);
            }
        });

        markAllAsGUIDButton.setText("Mark All as GUID");
        markAllAsGUIDButton.setMaximumSize(new java.awt.Dimension(125, 25));
        markAllAsGUIDButton.setMinimumSize(new java.awt.Dimension(125, 25));
        markAllAsGUIDButton.setPreferredSize(new java.awt.Dimension(125, 25));
        markAllAsGUIDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markAllAsGUIDButtonActionPerformed(evt);
            }
        });

        markAllAsHashButton.setText("Mark All as HASH");
        markAllAsHashButton.setMaximumSize(new java.awt.Dimension(125, 25));
        markAllAsHashButton.setMinimumSize(new java.awt.Dimension(125, 25));
        markAllAsHashButton.setPreferredSize(new java.awt.Dimension(125, 25));
        markAllAsHashButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markAllAsHashButtonActionPerformed(evt);
            }
        });

        assetList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(assetList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(exportButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(materialLibraryCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(packagingTypeCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(packagingTypeLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(materialLibraryLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(switchReferenceTypeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(5, 5, 5)
                        .addComponent(markAllAsHashButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(markAllAsGUIDButton, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(packagingTypeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(packagingTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(materialLibraryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(materialLibraryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(markAllAsGUIDButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(markAllAsHashButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(switchReferenceTypeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(exportButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void markAllAsGUIDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markAllAsGUIDButtonActionPerformed
        for (int i = 0; i < this.assetModel.size(); ++i) {
            Asset asset = (Asset) this.assetModel.getElementAt(i);
            if (!asset.locked)
                asset.hashinate = false;
        }
        this.updateButtonStates();
        this.assetList.repaint();
    }//GEN-LAST:event_markAllAsGUIDButtonActionPerformed

    private void markAllAsHashButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markAllAsHashButtonActionPerformed
        for (int i = 0; i < this.assetModel.size(); ++i) {
            Asset asset = (Asset) this.assetModel.getElementAt(i);
            if (!asset.locked)
                asset.hashinate = true;
        }
        this.updateButtonStates();
        this.assetList.repaint();
    }//GEN-LAST:event_markAllAsHashButtonActionPerformed

    private void switchReferenceTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchReferenceTypeButtonActionPerformed
        int index = this.assetList.getSelectedIndex();
        if (index == -1) return;
        Asset asset = (Asset) this.assetModel.getElementAt(index);
        if (asset.locked) return;
        asset.hashinate = !asset.hashinate;
        this.updateButtonStates();
        this.assetList.repaint();
    }//GEN-LAST:event_switchReferenceTypeButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        String name = this.entry.getName();
        int extIndex = name.lastIndexOf(".");
        if (extIndex != -1)
            name = name.substring(0, extIndex);
        
        File file = FileChooser.openFile(name + ".mod", "mod", true);
        if (file == null) return;
        
        this.finalize(file.getAbsolutePath());
    }//GEN-LAST:event_exportButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<Asset> assetList;
    private javax.swing.JButton exportButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton markAllAsGUIDButton;
    private javax.swing.JButton markAllAsHashButton;
    private javax.swing.JComboBox<MaterialLibrary> materialLibraryCombo;
    private javax.swing.JLabel materialLibraryLabel;
    private javax.swing.JComboBox<PackageType> packagingTypeCombo;
    private javax.swing.JLabel packagingTypeLabel;
    private javax.swing.JButton switchReferenceTypeButton;
    // End of variables declaration//GEN-END:variables
}
