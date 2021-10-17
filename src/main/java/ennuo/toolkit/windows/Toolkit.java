package ennuo.toolkit.windows;

import ennuo.craftworld.types.BigProfile;
import ennuo.craftworld.types.FileArchive;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.Mod;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.TranslationTable;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.swing.Nodes;
import ennuo.craftworld.swing.SearchParameters;
import ennuo.craftworld.things.InventoryItem;
import ennuo.craftworld.things.InventoryMetadata;
import ennuo.craftworld.types.indev.FileSave;
import ennuo.craftworld.types.FileDB;
import ennuo.toolkit.functions.ArchiveCallbacks;
import ennuo.toolkit.functions.DatabaseCallbacks;
import ennuo.toolkit.functions.DebugCallbacks;
import ennuo.toolkit.functions.DependencyCallbacks;
import ennuo.toolkit.utilities.EasterEgg;
import ennuo.toolkit.functions.ExportCallbacks;
import ennuo.toolkit.functions.FileCallbacks;
import ennuo.toolkit.functions.ModCallbacks;
import ennuo.toolkit.functions.ProfileCallbacks;
import ennuo.toolkit.functions.ReplacementCallbacks;
import ennuo.toolkit.functions.ScanCallback;
import ennuo.toolkit.functions.UtilityCallbacks;
import ennuo.toolkit.streams.CustomPrintStream;
import ennuo.toolkit.streams.TextAreaOutputStream;
import ennuo.toolkit.utilities.Config;
import ennuo.toolkit.utilities.FileChooser;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.utilities.Globals.WorkspaceType;
import ennuo.toolkit.utilities.TreeSelectionListener;
import java.awt.Color;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;
import tv.porst.jhexview.JHexView;
import tv.porst.jhexview.SimpleDataProvider;

public class Toolkit extends javax.swing.JFrame {
    public static Toolkit instance;

    public ExecutorService databaseService = Executors.newSingleThreadExecutor();
    public ExecutorService resourceService = Executors.newSingleThreadExecutor();
    public final FileChooser fileChooser = new FileChooser(this);

    public static ArrayList <JTree> trees = new ArrayList <JTree>();

    public boolean fileExists = false;
    public boolean useContext = false;

    MouseListener showContextMenu = new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                JTree tree = (JTree) e.getComponent();
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selPath != null) {
                    useContext = true;
                    tree.setSelectionPath(selPath);
                } else {
                    useContext = false;
                    tree.setSelectionPath(null);
                    Globals.lastSelected = null;
                }
                if (selRow > -1) {
                    tree.setSelectionRow(selRow);
                    useContext = true;
                } else useContext = false;
                getLastSelected(tree);
                generateEntryContext(tree, e.getX(), e.getY());
            }
        }
    };

    public Toolkit() {
        initComponents();
        EasterEgg.initialize(this);
        instance = this;
        
        if (Config.ENABLE_NEW_SAVEDATA)
            this.loadSavedata.setVisible(true);
       
        entryTable.getActionMap().put("copy", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String copied = "";
                for (int i = 0; i < entryTable.getSelectedRowCount(); ++i) {
                    copied += String.valueOf(entryTable.getModel().getValueAt(entryTable.getSelectedRows()[i], 1));
                    if (i + 1 != entryTable.getSelectedRowCount()) copied += '\n';
                }
                StringSelection selection = new StringSelection(copied);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            }

        });
        
        progressBar.setVisible(false);
        fileDataTabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int index = fileDataTabs.getSelectedIndex();
                if (index == -1) {
                    search.setEnabled(false);
                    Globals.currentWorkspace = WorkspaceType.NONE;
                    search.setForeground(Color.GRAY);
                    search.setText("Search is currently disabled.");
                } else {
                    search.setEnabled(true);
                    FileData data = Globals.databases.get(fileDataTabs.getSelectedIndex());
                    search.setText(data.query);
                    if (search.getText().equals("Search...")) search.setForeground(Color.GRAY);
                    else search.setForeground(Color.WHITE);
                    if (data.type.equals("Big Profile")) Globals.currentWorkspace = WorkspaceType.PROFILE;
                    else if (data.type.equals("Mod")) Globals.currentWorkspace = WorkspaceType.MOD;
                    else if (data.type.equals("File Save")) Globals.currentWorkspace = WorkspaceType.SAVE;
                    else Globals.currentWorkspace = WorkspaceType.MAP;
                }
                updateWorkspace();
            }
        });

        entryModifiers.setEnabledAt(1, false);
        StringMetadata.setEnabled(false);
        updateWorkspace();

        setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        search.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (search.getText().equals("Search...")) {
                    search.setText("");
                    search.setForeground(Color.WHITE);
                }
            }

            public void focusLost(FocusEvent e) {
                if (search.getText().isEmpty()) {
                    search.setText("Search...");
                    search.setForeground(Color.GRAY);
                }
            }

        });

        dependencyTree.addMouseListener(showContextMenu);
        dependencyTree.addTreeSelectionListener(e -> TreeSelectionListener.listener(dependencyTree));

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                checkForChanges();
            }
        });
    }

    private void checkForChanges() {
        for (FileData data: Globals.databases) {
            if (data.shouldSave) {
                int result = JOptionPane.showConfirmDialog(null, String.format("Your %s (%s) has pending changes, do you want to save?", data.type, data.path), "Pending changes", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) data.save(data.path);
            }
        }

        for (FileArchive archive: Globals.archives) {
            if (archive.shouldSave) {
                int result = JOptionPane.showConfirmDialog(null, String.format("Your FileArchive (%s) has pending changes, do you want to save?", archive.file.getAbsolutePath()), "Pending changes", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) archive.save();
            }
        }
    }

    public FileData getCurrentDB() {
        if (Globals.databases.size() == 0) return null;
        return Globals.databases.get(fileDataTabs.getSelectedIndex());
    }

    public JTree getCurrentTree() {
        if (trees.size() == 0) return null;
        return trees.get(fileDataTabs.getSelectedIndex());
    }

    public void updateWorkspace() {
        closeTab.setVisible(fileDataTabs.getTabCount() != 0);
        installProfileMod.setVisible(false);
        int archiveCount = Globals.archives.size();
        FileData db = getCurrentDB();

        if (db != null) {
            if (db.shouldSave) {
                fileDataTabs.setTitleAt(fileDataTabs.getSelectedIndex(), db.name + " *");
                saveMenu.setEnabled(true);
            } else {
                fileDataTabs.setTitleAt(fileDataTabs.getSelectedIndex(), db.name);
                saveMenu.setEnabled(false);
            }
        } 

        fileExists = false;
        if (Globals.lastSelected != null && Globals.lastSelected.entry != null) {
            if (Globals.lastSelected.entry.data != null)
                fileExists = true;
        } else if (Globals.entries.size() > 1) fileExists = true;

        if (archiveCount != 0 || db != null) {
            saveDivider.setVisible(true);
            saveMenu.setVisible(true);
        } else {
            saveDivider.setVisible(false);
            saveMenu.setVisible(false);
        }

        if (Globals.canExtract() && Globals.currentWorkspace != WorkspaceType.MOD)
            FARMenu.setVisible(true);
        else FARMenu.setVisible(false);

        if (Globals.currentWorkspace != WorkspaceType.NONE) {
            saveDivider.setVisible(true);
            saveAs.setVisible(true);
        } else {
            saveDivider.setVisible(false);
            saveAs.setVisible(false);
        }

        if (db == null) {
            saveDivider.setVisible(false);
            dumpHashes.setVisible(false);
            MAPMenu.setVisible(false);
            scanFileArchive.setVisible(false);
        } else {
            saveDivider.setVisible(true);
            dumpHashes.setVisible(true);
            dumpRLST.setVisible(false);
            if (Globals.currentWorkspace == WorkspaceType.MAP) {
                if (archiveCount != 0)
                    installProfileMod.setVisible(true);
                MAPMenu.setVisible(true);
                dumpHashes.setVisible(true);
                dumpRLST.setVisible(true);
                scanFileArchive.setVisible(true);
            }
        }

        if (Globals.currentWorkspace == WorkspaceType.PROFILE) {
            ProfileMenu.setVisible(true);
            installProfileMod.setVisible(true);
        } else ProfileMenu.setVisible(false);

        if (Globals.currentWorkspace == WorkspaceType.MOD) modMenu.setVisible(true);
        else modMenu.setVisible(false);

    }

    private void generateEntryContext(JTree tree, int x, int y) {
        exportTextureGroupContext.setVisible(false);
        editSlotContext.setVisible(false);
        exportLAMSContext.setVisible(false);
        loadLAMSContext.setVisible(false);
        replaceContext.setVisible(false);
        newFolderContext.setVisible(false);
        deleteContext.setVisible(false);
        zeroContext.setVisible(false);
        duplicateContext.setVisible(false);
        extractContextMenu.setVisible(false);
        newItemContext.setVisible(false);
        replaceDecompressed.setVisible(false);
        replaceDependencies.setVisible(false);
        dependencyGroup.setVisible(false);
        exportModGroup.setVisible(false);
        exportAnimation.setVisible(false);
        exportModelGroup.setVisible(false);
        exportGroup.setVisible(false);
        replaceImage.setVisible(false);
        editMenuContext.setVisible(false);

        if (Globals.currentWorkspace == WorkspaceType.PROFILE && useContext) deleteContext.setVisible(true);

        if (!(Globals.currentWorkspace == WorkspaceType.PROFILE) && Globals.databases.size() != 0) {
            if ((useContext && Globals.lastSelected.entry == null)) {
                newItemContext.setVisible(true);
                newFolderContext.setVisible(true);
            } else if (!useContext) newFolderContext.setVisible(true);
            if (useContext) {
                zeroContext.setVisible(true);
                deleteContext.setVisible(true);
                if (Globals.lastSelected.entry != null) {
                    duplicateContext.setVisible(true);
                    if (Globals.currentWorkspace == WorkspaceType.MAP)
                        editMenuContext.setVisible(true);
                }
            }
        }

        if (Globals.canExtract() && Globals.lastSelected != null && Globals.lastSelected.entry != null) {
            replaceContext.setVisible(true);
            if (Globals.lastSelected.header.endsWith(".tex"))
                replaceImage.setVisible(true);
        }

        if (Globals.canExtract() && fileExists && useContext) {

            extractContextMenu.setVisible(true);

            if (Globals.lastSelected.entry != null) {
                if ((Globals.currentWorkspace == WorkspaceType.PROFILE || Globals.databases.size() != 0) && Globals.lastSelected.entry.canReplaceDecompressed) {
                    replaceDecompressed.setVisible(true);
                    if (Globals.lastSelected.entry.dependencies != null && Globals.lastSelected.entry.dependencies.length != 0) {
                        exportGroup.setVisible(true);
                        exportModGroup.setVisible(true);
                        replaceDependencies.setVisible(true);
                        dependencyGroup.setVisible(true);
                    }
                }
                
                if (Globals.lastSelected.header.endsWith(".anim")) {
                    if (Globals.lastSelected.entry.animation != null) {
                        exportGroup.setVisible(true);
                        exportAnimation.setVisible(true);
                    }
                }

                if (Globals.lastSelected.header.endsWith(".mol")) {
                    if (Globals.lastSelected.entry.mesh != null) {
                        exportGroup.setVisible(true);
                        exportModelGroup.setVisible(true);
                        int count = Globals.lastSelected.entry.mesh.attributeCount;
                        exportOBJTEXCOORD0.setVisible((count > 0));
                        exportOBJTEXCOORD1.setVisible((count > 1));
                        exportOBJTEXCOORD2.setVisible((count > 2));
                    }
                }
                if (Globals.lastSelected.header.endsWith(".tex")) {
                    exportGroup.setVisible(true);
                    exportTextureGroupContext.setVisible(true);
                    replaceImage.setVisible(true);
                }

                if ((Globals.lastSelected.header.endsWith(".bin") && Globals.currentWorkspace == WorkspaceType.PROFILE) || (Globals.lastSelected.header.endsWith(".slt")) || (Globals.lastSelected.header.endsWith(".pck")))
                    editSlotContext.setVisible(true);

                if (Globals.lastSelected.header.endsWith(".trans")) {
                    exportGroup.setVisible(true);
                    loadLAMSContext.setVisible(true);
                    exportLAMSContext.setVisible(true);
                }
            }
        }

        entryContext.show(tree, x, y);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        entryContext = new javax.swing.JPopupMenu();
        extractContextMenu = new javax.swing.JMenu();
        extractContext = new javax.swing.JMenuItem();
        extractDecompressedContext = new javax.swing.JMenuItem();
        editMenuContext = new javax.swing.JMenu();
        renameItemContext = new javax.swing.JMenuItem();
        changeHash = new javax.swing.JMenuItem();
        changeGUID = new javax.swing.JMenuItem();
        editSlotContext = new javax.swing.JMenuItem();
        loadLAMSContext = new javax.swing.JMenuItem();
        exportGroup = new javax.swing.JMenu();
        exportTextureGroupContext = new javax.swing.JMenu();
        exportPNG = new javax.swing.JMenuItem();
        exportDDS = new javax.swing.JMenuItem();
        exportModelGroup = new javax.swing.JMenu();
        exportOBJ = new javax.swing.JMenu();
        exportOBJTEXCOORD0 = new javax.swing.JMenuItem();
        exportOBJTEXCOORD1 = new javax.swing.JMenuItem();
        exportOBJTEXCOORD2 = new javax.swing.JMenuItem();
        exportGLTF = new javax.swing.JMenuItem();
        exportLAMSContext = new javax.swing.JMenuItem();
        exportModGroup = new javax.swing.JMenu();
        exportAsMod = new javax.swing.JMenuItem();
        exportAsModGUID = new javax.swing.JMenuItem();
        exportAnimation = new javax.swing.JMenuItem();
        replaceContext = new javax.swing.JMenu();
        replaceCompressed = new javax.swing.JMenuItem();
        replaceDecompressed = new javax.swing.JMenuItem();
        replaceDependencies = new javax.swing.JMenuItem();
        replaceImage = new javax.swing.JMenuItem();
        dependencyGroup = new javax.swing.JMenu();
        removeDependencies = new javax.swing.JMenuItem();
        removeMissingDependencies = new javax.swing.JMenuItem();
        newItemContext = new javax.swing.JMenuItem();
        newFolderContext = new javax.swing.JMenuItem();
        duplicateContext = new javax.swing.JMenuItem();
        zeroContext = new javax.swing.JMenuItem();
        deleteContext = new javax.swing.JMenuItem();
        consolePopup = new javax.swing.JPopupMenu();
        clear = new javax.swing.JMenuItem();
        metadataButtonGroup = new javax.swing.ButtonGroup();
        workspaceDivider = new javax.swing.JSplitPane();
        treeContainer = new javax.swing.JSplitPane();
        search = new javax.swing.JTextField();
        fileDataTabs = new javax.swing.JTabbedPane();
        details = new javax.swing.JSplitPane();
        previewContainer = new javax.swing.JSplitPane();
        consoleContainer = new javax.swing.JScrollPane();
        console = new javax.swing.JTextArea();
        preview = new javax.swing.JSplitPane();
        texture = new javax.swing.JLabel();
        hex = new tv.porst.jhexview.JHexView();
        entryData = new javax.swing.JSplitPane();
        tableContainer = new javax.swing.JScrollPane();
        entryTable = new javax.swing.JTable();
        entryModifiers = new javax.swing.JTabbedPane();
        dependencyTreeContainer = new javax.swing.JScrollPane();
        dependencyTree = new javax.swing.JTree();
        itemMetadata = new javax.swing.JPanel();
        LAMSMetadata = new javax.swing.JRadioButton();
        StringMetadata = new javax.swing.JRadioButton();
        iconLabel = new javax.swing.JLabel();
        iconField = new javax.swing.JTextField();
        titleLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        descriptionField = new javax.swing.JTextArea();
        titleField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationField = new javax.swing.JTextField();
        categoryLabel = new javax.swing.JLabel();
        categoryField = new javax.swing.JTextField();
        pageCombo = new javax.swing.JComboBox(ennuo.craftworld.resources.enums.ItemType.values());
        subCombo = new javax.swing.JComboBox(ennuo.craftworld.resources.enums.ItemSubType.values());
        creatorLabel = new javax.swing.JLabel();
        creatorField = new javax.swing.JTextField();
        progressBar = new javax.swing.JProgressBar();
        toolkitMenu = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        menuFileMenu = new javax.swing.JMenu();
        newGamedataGroup = new javax.swing.JMenu();
        newFileDBGroup = new javax.swing.JMenu();
        newLegacyDB = new javax.swing.JMenuItem();
        newVitaDB = new javax.swing.JMenuItem();
        newModernDB = new javax.swing.JMenuItem();
        createFileArchive = new javax.swing.JMenuItem();
        newMod = new javax.swing.JMenuItem();
        loadGroupMenu = new javax.swing.JMenu();
        gamedataMenu = new javax.swing.JMenu();
        loadDB = new javax.swing.JMenuItem();
        loadArchive = new javax.swing.JMenuItem();
        savedataMenu = new javax.swing.JMenu();
        loadBigProfile = new javax.swing.JMenuItem();
        loadSavedata = new javax.swing.JMenuItem();
        loadMod = new javax.swing.JMenuItem();
        saveDivider = new javax.swing.JPopupMenu.Separator();
        saveAs = new javax.swing.JMenuItem();
        saveMenu = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        closeTab = new javax.swing.JMenuItem();
        reboot = new javax.swing.JMenuItem();
        FARMenu = new javax.swing.JMenu();
        addFile = new javax.swing.JMenuItem();
        MAPMenu = new javax.swing.JMenu();
        patchMAP = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        dumpRLST = new javax.swing.JMenuItem();
        dumpHashes = new javax.swing.JMenuItem();
        ProfileMenu = new javax.swing.JMenu();
        extractBigProfile = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        editProfileSlots = new javax.swing.JMenuItem();
        editProfileItems = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        addKey = new javax.swing.JMenuItem();
        modMenu = new javax.swing.JMenu();
        openModMetadata = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        encodeInteger = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        openCompressinator = new javax.swing.JMenuItem();
        decompressResource = new javax.swing.JMenuItem();
        dumpSep = new javax.swing.JPopupMenu.Separator();
        generateDiff = new javax.swing.JMenuItem();
        scanRawData = new javax.swing.JMenuItem();
        scanFileArchive = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mergeFARCs = new javax.swing.JMenuItem();
        installProfileMod = new javax.swing.JMenuItem();
        debugMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        addAllPlansToInventory = new javax.swing.JMenuItem();
        convertAllToGUID = new javax.swing.JMenuItem();
        dumpBPRToMod = new javax.swing.JMenuItem();
        testSerializeCurrentMesh = new javax.swing.JMenuItem();
        debugJokerTest = new javax.swing.JMenuItem();
        debugAddSlots = new javax.swing.JMenuItem();
        debugRecompressAll = new javax.swing.JMenuItem();
        emittionTendency = new javax.swing.JMenuItem();

        extractContextMenu.setText("Extract");

        extractContext.setText("Extract");
        extractContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractContextActionPerformed(evt);
            }
        });
        extractContextMenu.add(extractContext);

        extractDecompressedContext.setText("Decompress");
        extractDecompressedContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractDecompressedContextActionPerformed(evt);
            }
        });
        extractContextMenu.add(extractDecompressedContext);

        entryContext.add(extractContextMenu);

        editMenuContext.setText("Edit");

        renameItemContext.setText("Path");
        renameItemContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameItemContextActionPerformed(evt);
            }
        });
        editMenuContext.add(renameItemContext);

        changeHash.setText("Hash");
        changeHash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeHashActionPerformed(evt);
            }
        });
        editMenuContext.add(changeHash);

        changeGUID.setText("GUID");
        changeGUID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeGUIDActionPerformed(evt);
            }
        });
        editMenuContext.add(changeGUID);

        entryContext.add(editMenuContext);

        editSlotContext.setText("Edit Slot");
        editSlotContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSlotContextActionPerformed(evt);
            }
        });
        entryContext.add(editSlotContext);

        loadLAMSContext.setText("Load LAMS");
        loadLAMSContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadLAMSContextActionPerformed(evt);
            }
        });
        entryContext.add(loadLAMSContext);

        exportGroup.setText("Export");

        exportTextureGroupContext.setText("Textures");

        exportPNG.setText("PNG");
        exportPNG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPNGActionPerformed(evt);
            }
        });
        exportTextureGroupContext.add(exportPNG);

        exportDDS.setText("DDS");
        exportDDS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDDSActionPerformed(evt);
            }
        });
        exportTextureGroupContext.add(exportDDS);

        exportGroup.add(exportTextureGroupContext);

        exportModelGroup.setText("Model");

        exportOBJ.setText("Wavefront");

        exportOBJTEXCOORD0.setText("TEXCOORD0");
        exportOBJTEXCOORD0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportOBJTEXCOORD0ActionPerformed(evt);
            }
        });
        exportOBJ.add(exportOBJTEXCOORD0);

        exportOBJTEXCOORD1.setText("TEXCOORD1");
        exportOBJTEXCOORD1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportOBJTEXCOORD1ActionPerformed(evt);
            }
        });
        exportOBJ.add(exportOBJTEXCOORD1);

        exportOBJTEXCOORD2.setText("TEXCOORD2");
        exportOBJTEXCOORD2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportOBJTEXCOORD2ActionPerformed(evt);
            }
        });
        exportOBJ.add(exportOBJTEXCOORD2);

        exportModelGroup.add(exportOBJ);

        exportGLTF.setText("glTF 2.0");
        exportGLTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportGLTFActionPerformed(evt);
            }
        });
        exportModelGroup.add(exportGLTF);

        exportGroup.add(exportModelGroup);

        exportLAMSContext.setText("Text Document");
        exportLAMSContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportLAMSContextActionPerformed(evt);
            }
        });
        exportGroup.add(exportLAMSContext);

        exportModGroup.setText("Mod");

        exportAsMod.setText("Hash");
        exportAsMod.setToolTipText("");
        exportAsMod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsModActionPerformed(evt);
            }
        });
        exportModGroup.add(exportAsMod);

        exportAsModGUID.setText("GUID");
        exportAsModGUID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsModGUIDActionPerformed(evt);
            }
        });
        exportModGroup.add(exportAsModGUID);

        exportGroup.add(exportModGroup);

        exportAnimation.setText("Animation");
        exportAnimation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAnimationActionPerformed(evt);
            }
        });
        exportGroup.add(exportAnimation);

        entryContext.add(exportGroup);

        replaceContext.setText("Replace");

        replaceCompressed.setText("Replace");
        replaceCompressed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceCompressedActionPerformed(evt);
            }
        });
        replaceContext.add(replaceCompressed);

        replaceDecompressed.setText("Decompressed");
        replaceDecompressed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceDecompressedActionPerformed(evt);
            }
        });
        replaceContext.add(replaceDecompressed);

        replaceDependencies.setText("Dependencies");
        replaceDependencies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceDependenciesActionPerformed(evt);
            }
        });
        replaceContext.add(replaceDependencies);

        replaceImage.setText("Image");
        replaceImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceImageActionPerformed(evt);
            }
        });
        replaceContext.add(replaceImage);

        entryContext.add(replaceContext);

        dependencyGroup.setText("Dependencies");

        removeDependencies.setText("Remove Dependencies");
        removeDependencies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDependenciesActionPerformed(evt);
            }
        });
        dependencyGroup.add(removeDependencies);

        removeMissingDependencies.setText("Remove Missing Dependencies");
        removeMissingDependencies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMissingDependenciesActionPerformed(evt);
            }
        });
        dependencyGroup.add(removeMissingDependencies);

        entryContext.add(dependencyGroup);

        newItemContext.setText("New Item");
        newItemContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newItemContextActionPerformed(evt);
            }
        });
        entryContext.add(newItemContext);

        newFolderContext.setText("New Folder");
        newFolderContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFolderContextActionPerformed(evt);
            }
        });
        entryContext.add(newFolderContext);

        duplicateContext.setText("Duplicate");
        duplicateContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateContextActionPerformed(evt);
            }
        });
        entryContext.add(duplicateContext);

        zeroContext.setText("Zero");
        zeroContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zeroContextActionPerformed(evt);
            }
        });
        entryContext.add(zeroContext);

        deleteContext.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        deleteContext.setText("Delete");
        deleteContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteContextActionPerformed(evt);
            }
        });
        entryContext.add(deleteContext);

        clear.setText("Clear");
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });
        consolePopup.add(clear);

        metadataButtonGroup.add(LAMSMetadata);
        metadataButtonGroup.add(StringMetadata);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Craftworld Toolkit");
        setMinimumSize(new java.awt.Dimension(698, 296));

        workspaceDivider.setDividerLocation(275);

        treeContainer.setDividerLocation(30);
        treeContainer.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        treeContainer.setMinimumSize(new java.awt.Dimension(150, 200));
        treeContainer.setPreferredSize(new java.awt.Dimension(150, 200));

        search.setEditable(false);
        search.setText(" Search is currently disabled.");
        search.setBorder(null);
        search.setFocusable(false);
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });
        treeContainer.setLeftComponent(search);
        treeContainer.setRightComponent(fileDataTabs);

        workspaceDivider.setLeftComponent(treeContainer);

        details.setResizeWeight(1);
        details.setDividerLocation(850);
        details.setEnabled(false);

        previewContainer.setDividerLocation(325);
        previewContainer.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        console.setEditable(false);
        console.setColumns(20);
        console.setLineWrap(true);
        console.setRows(5);
        PrintStream out = new CustomPrintStream(new TextAreaOutputStream(console));
        System.setOut(out);
        System.setErr(out);
        console.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                consoleMouseReleased(evt);
            }
        });
        consoleContainer.setViewportView(console);

        previewContainer.setBottomComponent(consoleContainer);

        preview.setDividerLocation(325);

        texture.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        texture.setText("No preview to be displayed.");
        texture.setToolTipText("");
        texture.setFocusable(false);
        preview.setLeftComponent(texture);

        javax.swing.GroupLayout hexLayout = new javax.swing.GroupLayout(hex);
        hex.setLayout(hexLayout);
        hexLayout.setHorizontalGroup(
            hexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        hexLayout.setVerticalGroup(
            hexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 322, Short.MAX_VALUE)
        );

        preview.setRightComponent(hex);

        previewContainer.setLeftComponent(preview);

        details.setLeftComponent(previewContainer);

        entryData.setDividerLocation(148);
        entryData.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        entryData.setMaximumSize(new java.awt.Dimension(55, 2147483647));
        entryData.setMinimumSize(new java.awt.Dimension(55, 102));
        entryData.setPreferredSize(new java.awt.Dimension(55, 1120));

        tableContainer.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tableContainer.setMaximumSize(new java.awt.Dimension(452, 32767));
        tableContainer.setMinimumSize(new java.awt.Dimension(452, 6));

        entryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Path", "N/A"},
                {"Timestamp", "N/A"},
                {"SHA1", "N/A"},
                {"Size", "N/A"},
                {"GUID", "N/A"},
                {"GUID (Hex)", "N/A"},
                {"GUID (7-bit)", "N/A"},
                {"Revision", "N/A"}
            },
            new String [] {
                "Field", "Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        entryTable.getTableHeader().setReorderingAllowed(false);
        tableContainer.setViewportView(entryTable);
        if (entryTable.getColumnModel().getColumnCount() > 0) {
            entryTable.getColumnModel().getColumn(0).setResizable(false);
            entryTable.getColumnModel().getColumn(1).setResizable(false);
        }

        entryData.setLeftComponent(tableContainer);

        entryModifiers.setMaximumSize(new java.awt.Dimension(452, 32767));
        entryModifiers.setMinimumSize(new java.awt.Dimension(452, 81));
        entryModifiers.setPreferredSize(new java.awt.Dimension(452, 713));

        dependencyTreeContainer.setAlignmentX(2.0F);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        dependencyTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        dependencyTree.setRootVisible(false);
        dependencyTreeContainer.setViewportView(dependencyTree);

        entryModifiers.addTab("Dependencies", dependencyTreeContainer);

        LAMSMetadata.setText("LAMS");
        LAMSMetadata.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LAMSMetadataActionPerformed(evt);
            }
        });

        StringMetadata.setText("String");
        StringMetadata.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StringMetadataActionPerformed(evt);
            }
        });

        iconLabel.setText("Icon");

        iconField.setEditable(false);

        titleLabel.setText("Title");

        descriptionLabel.setText("Description");

        descriptionField.setEditable(false);
        descriptionField.setColumns(20);
        descriptionField.setLineWrap(true);
        descriptionField.setRows(5);
        descriptionField.setWrapStyleWord(true);

        titleField.setEditable(false);

        locationLabel.setText("Location");

        locationField.setEditable(false);
        locationField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationFieldActionPerformed(evt);
            }
        });

        categoryLabel.setText("Category");

        categoryField.setEditable(false);

        pageCombo.setEnabled(false);

        subCombo.setEnabled(false);

        creatorLabel.setText("Creator");

        creatorField.setEditable(false);

        javax.swing.GroupLayout itemMetadataLayout = new javax.swing.GroupLayout(itemMetadata);
        itemMetadata.setLayout(itemMetadataLayout);
        itemMetadataLayout.setHorizontalGroup(
            itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemMetadataLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, itemMetadataLayout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addComponent(LAMSMetadata, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(StringMetadata, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, itemMetadataLayout.createSequentialGroup()
                            .addComponent(iconLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(iconField, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, itemMetadataLayout.createSequentialGroup()
                            .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(descriptionField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, itemMetadataLayout.createSequentialGroup()
                            .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(locationLabel)
                                .addComponent(categoryLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(locationField)
                                .addComponent(categoryField))))
                    .addComponent(pageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(itemMetadataLayout.createSequentialGroup()
                            .addComponent(creatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(creatorField))
                        .addComponent(subCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        itemMetadataLayout.setVerticalGroup(
            itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemMetadataLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iconLabel)
                    .addComponent(iconField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(StringMetadata)
                    .addComponent(LAMSMetadata))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(categoryLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(categoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(subCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(creatorLabel)
                    .addComponent(creatorField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(41, Short.MAX_VALUE))
        );

        entryModifiers.addTab("Metadata", itemMetadata);

        entryData.setRightComponent(entryModifiers);

        details.setRightComponent(entryData);

        workspaceDivider.setRightComponent(details);

        progressBar.setEnabled(false);

        fileMenu.setText("File");

        menuFileMenu.setText("New");

        newGamedataGroup.setText("Gamedata");

        newFileDBGroup.setText("FileDB");

        newLegacyDB.setText("LBP1/2");
        newLegacyDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newLegacyDBActionPerformed(evt);
            }
        });
        newFileDBGroup.add(newLegacyDB);

        newVitaDB.setText("LBP Vita");
        newVitaDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newVitaDBActionPerformed(evt);
            }
        });
        newFileDBGroup.add(newVitaDB);

        newModernDB.setText("LBP3");
        newModernDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newModernDBActionPerformed(evt);
            }
        });
        newFileDBGroup.add(newModernDB);

        newGamedataGroup.add(newFileDBGroup);

        createFileArchive.setText("File Archive");
        createFileArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createFileArchiveActionPerformed(evt);
            }
        });
        newGamedataGroup.add(createFileArchive);

        menuFileMenu.add(newGamedataGroup);

        newMod.setText("Mod");
        newMod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newModActionPerformed(evt);
            }
        });
        menuFileMenu.add(newMod);

        fileMenu.add(menuFileMenu);

        loadGroupMenu.setText("Load");

        gamedataMenu.setText("Gamedata");

        loadDB.setText("FileDB");
        loadDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDBActionPerformed(evt);
            }
        });
        gamedataMenu.add(loadDB);

        loadArchive.setText("File Archive");
        loadArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadArchiveActionPerformed(evt);
            }
        });
        gamedataMenu.add(loadArchive);

        loadGroupMenu.add(gamedataMenu);

        savedataMenu.setText("Savedata");

        loadBigProfile.setText("Big Profile");
        loadBigProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadBigProfileActionPerformed(evt);
            }
        });
        savedataMenu.add(loadBigProfile);

        loadSavedata.setText("Savedata (PREVIEW)");
        loadSavedata.setVisible(false);
        loadSavedata.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSavedataActionPerformed(evt);
            }
        });
        savedataMenu.add(loadSavedata);

        loadGroupMenu.add(savedataMenu);

        loadMod.setText("Mod");
        loadMod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadModActionPerformed(evt);
            }
        });
        loadGroupMenu.add(loadMod);

        fileMenu.add(loadGroupMenu);
        fileMenu.add(saveDivider);

        saveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveAs.setText("Save as...");
        saveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsActionPerformed(evt);
            }
        });
        fileMenu.add(saveAs);

        saveMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveMenu.setText("Save");
        saveMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenu);
        fileMenu.add(jSeparator4);

        closeTab.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        closeTab.setText("Close Tab");
        closeTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeTabActionPerformed(evt);
            }
        });
        fileMenu.add(closeTab);

        reboot.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        reboot.setText("Reboot");
        reboot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rebootActionPerformed(evt);
            }
        });
        fileMenu.add(reboot);

        toolkitMenu.add(fileMenu);

        FARMenu.setText("Archive");

        addFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        addFile.setText("Add...");
        addFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileActionPerformed(evt);
            }
        });
        FARMenu.add(addFile);

        toolkitMenu.add(FARMenu);

        MAPMenu.setText("FileDB");

        patchMAP.setText("Patch");
        patchMAP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                patchMAPActionPerformed(evt);
            }
        });
        MAPMenu.add(patchMAP);
        MAPMenu.add(jSeparator6);

        dumpRLST.setText("Dump RLST");
        dumpRLST.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dumpRLSTActionPerformed(evt);
            }
        });
        MAPMenu.add(dumpRLST);

        dumpHashes.setText("Dump Hashes");
        dumpHashes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dumpHashesActionPerformed(evt);
            }
        });
        MAPMenu.add(dumpHashes);

        toolkitMenu.add(MAPMenu);

        ProfileMenu.setText("Profile");

        extractBigProfile.setText("Extract Profile");
        extractBigProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractBigProfileActionPerformed(evt);
            }
        });
        ProfileMenu.add(extractBigProfile);
        ProfileMenu.add(jSeparator1);

        editProfileSlots.setText("Edit Slots");
        editProfileSlots.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProfileSlotsActionPerformed(evt);
            }
        });
        ProfileMenu.add(editProfileSlots);

        editProfileItems.setText("Edit Items");
        editProfileItems.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProfileItemsActionPerformed(evt);
            }
        });
        ProfileMenu.add(editProfileItems);
        ProfileMenu.add(jSeparator2);

        addKey.setText("Add Key");
        addKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addKeyActionPerformed(evt);
            }
        });
        ProfileMenu.add(addKey);

        toolkitMenu.add(ProfileMenu);

        modMenu.setText("Mod");

        openModMetadata.setText("Edit");
        openModMetadata.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openModMetadataActionPerformed(evt);
            }
        });
        modMenu.add(openModMetadata);

        toolkitMenu.add(modMenu);

        toolsMenu.setText("Tools");

        encodeInteger.setText("Encode Integer");
        encodeInteger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encodeIntegerActionPerformed(evt);
            }
        });
        toolsMenu.add(encodeInteger);
        toolsMenu.add(jSeparator5);

        openCompressinator.setText("Compress");
        openCompressinator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openCompressinatorActionPerformed(evt);
            }
        });
        toolsMenu.add(openCompressinator);

        decompressResource.setText("Decompress");
        decompressResource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decompressResourceActionPerformed(evt);
            }
        });
        toolsMenu.add(decompressResource);
        toolsMenu.add(dumpSep);

        generateDiff.setText("Generate FileDB diff");
        generateDiff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateDiffActionPerformed(evt);
            }
        });
        toolsMenu.add(generateDiff);

        scanRawData.setText("Scan Raw Data");
        scanRawData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanRawDataActionPerformed(evt);
            }
        });
        toolsMenu.add(scanRawData);

        scanFileArchive.setText("Scan File Archive");
        scanFileArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanFileArchiveActionPerformed(evt);
            }
        });
        toolsMenu.add(scanFileArchive);
        toolsMenu.add(jSeparator3);

        mergeFARCs.setText("Merge FARCs");
        mergeFARCs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeFARCsActionPerformed(evt);
            }
        });
        toolsMenu.add(mergeFARCs);

        installProfileMod.setText("Install Mod");
        installProfileMod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installProfileModActionPerformed(evt);
            }
        });
        toolsMenu.add(installProfileMod);

        toolkitMenu.add(toolsMenu);

        debugMenu.setText("Debug");

        jMenuItem1.setText("create fake table");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        debugMenu.add(jMenuItem1);

        addAllPlansToInventory.setText("add all plans to inv table");
        addAllPlansToInventory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAllPlansToInventoryActionPerformed(evt);
            }
        });
        debugMenu.add(addAllPlansToInventory);

        convertAllToGUID.setText("convert all to guid");
        convertAllToGUID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertAllToGUIDActionPerformed(evt);
            }
        });
        debugMenu.add(convertAllToGUID);

        dumpBPRToMod.setText("dump bpr to mod");
        dumpBPRToMod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dumpBPRToModActionPerformed(evt);
            }
        });
        debugMenu.add(dumpBPRToMod);

        testSerializeCurrentMesh.setText("test serialize current mesh");
        testSerializeCurrentMesh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testSerializeCurrentMeshActionPerformed(evt);
            }
        });
        debugMenu.add(testSerializeCurrentMesh);

        debugJokerTest.setText("joker test");
        debugJokerTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debugJokerTestActionPerformed(evt);
            }
        });
        debugMenu.add(debugJokerTest);

        debugAddSlots.setText("add slots");
        debugAddSlots.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debugAddSlotsActionPerformed(evt);
            }
        });
        debugMenu.add(debugAddSlots);

        debugRecompressAll.setText("recompress all slots");
        debugRecompressAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debugRecompressAllActionPerformed(evt);
            }
        });
        debugMenu.add(debugRecompressAll);

        emittionTendency.setText("when you have a tendency to emit");
        emittionTendency.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emittionTendencyActionPerformed(evt);
            }
        });
        debugMenu.add(emittionTendency);

        toolkitMenu.add(debugMenu);

        setJMenuBar(toolkitMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(workspaceDivider, javax.swing.GroupLayout.DEFAULT_SIZE, 1385, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(workspaceDivider, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loadDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadDBActionPerformed
        File file = fileChooser.openFile("blurayguids.map", "map", "FileDB", false);
        if (file != null) DatabaseCallbacks.loadFileDB(file);
    }//GEN-LAST:event_loadDBActionPerformed

    public void addTab(FileData data) {
        Globals.databases.add(data);

        JTree tree = new JTree();

        trees.add(tree);

        tree.setRootVisible(false);
        tree.setModel(data.model);
        tree.getSelectionModel().setSelectionMode(4);

        tree.addTreeSelectionListener(e -> TreeSelectionListener.listener(tree));
        tree.addMouseListener(showContextMenu);

        JScrollPane panel = new JScrollPane();
        panel.setViewportView(tree);

        fileDataTabs.addTab(data.name, panel);

        search.setEditable(true);
        search.setFocusable(true);
        search.setText("Search...");
        search.setForeground(Color.GRAY);

        fileDataTabs.setSelectedIndex(fileDataTabs.getTabCount() - 1);
    }

    public int isDatabaseLoaded(File file) {
        String path = file.getAbsolutePath();
        for (int i = 0; i < Globals.databases.size(); ++i) {
            FileData data = Globals.databases.get(i);
            if (data.path.equals(path))
                return i;
        }
        return -1;
    }

    private void loadArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadArchiveActionPerformed
        File[] files = fileChooser.openFiles("farc", "File Archive");
        if (files == null) return;
        for (File file: files)
            ArchiveCallbacks.loadFileArchive(file);
    }//GEN-LAST:event_loadArchiveActionPerformed

    public int isArchiveLoaded(File file) {
        String path = file.getAbsolutePath();
        for (int i = 0; i < Globals.archives.size(); ++i) {
            FileArchive archive = Globals.archives.get(i);
            if (archive.file.getAbsolutePath().equals(path))
                return i;
        }
        return -1;
    }

    private void openCompressinatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openCompressinatorActionPerformed
        new Compressinator().setVisible(true);
    }//GEN-LAST:event_openCompressinatorActionPerformed

    private void decompressResourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decompressResourceActionPerformed
        UtilityCallbacks.decompressResource();
    }//GEN-LAST:event_decompressResourceActionPerformed

    private void saveMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuActionPerformed
        FileCallbacks.save();
    }//GEN-LAST:event_saveMenuActionPerformed

    private void addFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileActionPerformed
        ArchiveCallbacks.addFile();
    }//GEN-LAST:event_addFileActionPerformed


    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        console.selectAll();
        console.replaceSelection("");
    }//GEN-LAST:event_clearActionPerformed

    private void consoleMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_consoleMouseReleased
        if (evt.isPopupTrigger())
            consolePopup.show(console, evt.getX(), evt.getY());
    }//GEN-LAST:event_consoleMouseReleased

    private void extractContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractContextActionPerformed
        ArchiveCallbacks.extract(false);
    }//GEN-LAST:event_extractContextActionPerformed

    private void extractDecompressedContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractDecompressedContextActionPerformed
        ArchiveCallbacks.extract(true);
    }//GEN-LAST:event_extractDecompressedContextActionPerformed

    private void exportOBJTEXCOORD0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportOBJTEXCOORD0ActionPerformed
        ExportCallbacks.exportOBJ(0);
    }//GEN-LAST:event_exportOBJTEXCOORD0ActionPerformed

    private void loadLAMSContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadLAMSContextActionPerformed
        Globals.LAMS = new TranslationTable(new Data(Globals.lastSelected.entry.data));
        if (Globals.LAMS != null) StringMetadata.setEnabled(true);
    }//GEN-LAST:event_loadLAMSContextActionPerformed

    private void locationFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationFieldActionPerformed

    }//GEN-LAST:event_locationFieldActionPerformed

    private void LAMSMetadataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LAMSMetadataActionPerformed
        InventoryMetadata metadata = Globals.lastSelected.entry.item.metadata;
        titleField.setText("" + metadata.titleKey);
        descriptionField.setText("" + metadata.descriptionKey);
        locationField.setText("" + metadata.location);
        categoryField.setText("" + metadata.category);
    }//GEN-LAST:event_LAMSMetadataActionPerformed

    private void StringMetadataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StringMetadataActionPerformed
        InventoryMetadata metadata = Globals.lastSelected.entry.item.metadata;

        if (Globals.LAMS != null) {
            titleField.setText(Globals.Translate(metadata.titleKey));
            descriptionField.setText(Globals.Translate(metadata.descriptionKey));
            locationField.setText(Globals.Translate(metadata.location));
            categoryField.setText(Globals.Translate(metadata.category));
        }

        if (metadata.userCreatedDetails.title != null)
            titleField.setText(metadata.userCreatedDetails.title);

        if (metadata.userCreatedDetails.description != null)
            descriptionField.setText(metadata.userCreatedDetails.description);

        if (Globals.LAMS != null) return;

        locationField.setText("");
        categoryField.setText("");

        if (metadata.category == 1737521) categoryField.setText("My Photos");
        else if (metadata.category == 1598223) categoryField.setText("My Pods");
        else if (metadata.category == 928006) categoryField.setText("My Objects");
        else if (metadata.category == 578814) categoryField.setText("My Costumes");
    }//GEN-LAST:event_StringMetadataActionPerformed

    private void loadBigProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadBigProfileActionPerformed
        ProfileCallbacks.loadProfile();
    }//GEN-LAST:event_loadBigProfileActionPerformed

    public FileArchive[] getSelectedArchives() {
        if (Globals.archives.size() == 0) return null;
        if (Globals.archives.size() > 1) {
            FileArchive[] archives = new ArchiveSelector(this, true).getSelected();
            if (archives == null) System.out.println("User did not select any FileArchives, cancelling operation.");
            return archives;
        }
        return new FileArchive[] {
            Globals.archives.get(0)
        };
    }

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        getCurrentDB().query = search.getText();
        JTree tree = getCurrentTree();
        Nodes.filter((FileNode) tree.getModel().getRoot(), new SearchParameters(search.getText()));
        ((FileModel) tree.getModel()).reload();
        tree.updateUI();
    }//GEN-LAST:event_searchActionPerformed

    private void dumpHashesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dumpHashesActionPerformed
        DatabaseCallbacks.dumpHashes();
    }//GEN-LAST:event_dumpHashesActionPerformed

    private void extractBigProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractBigProfileActionPerformed
        ProfileCallbacks.extractProfile();
    }//GEN-LAST:event_extractBigProfileActionPerformed

    private void editSlotContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSlotContextActionPerformed
        if (Globals.currentWorkspace == WorkspaceType.PROFILE) {
            getCurrentDB().shouldSave = true;
            new SlotEditor(this, Globals.lastSelected.entry).setVisible(true);
        } else {
            boolean isSlotsFile = Globals.lastSelected.entry.pack == null;
            new SlotEditor(this, Globals.lastSelected.entry, (isSlotsFile) ? SlotEditor.EditorType.SLOTS : SlotEditor.EditorType.PACKS).setVisible(true);
        }
    }//GEN-LAST:event_editSlotContextActionPerformed

    private void scanRawDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanRawDataActionPerformed
        ScanCallback.scanRawData();
    }//GEN-LAST:event_scanRawDataActionPerformed

    private void exportLAMSContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportLAMSContextActionPerformed
        ExportCallbacks.exportTranslations();
    }//GEN-LAST:event_exportLAMSContextActionPerformed

    private void exportDDSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDDSActionPerformed
        ExportCallbacks.exportDDS();
    }//GEN-LAST:event_exportDDSActionPerformed

    private void exportPNGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPNGActionPerformed
        ExportCallbacks.exportTexture("png");
    }//GEN-LAST:event_exportPNGActionPerformed

    private void patchMAPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_patchMAPActionPerformed
        DatabaseCallbacks.patchDatabase();
    }//GEN-LAST:event_patchMAPActionPerformed

    private void replaceCompressedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceCompressedActionPerformed
        ReplacementCallbacks.replaceCompressed();
    }//GEN-LAST:event_replaceCompressedActionPerformed

    private void mergeFARCsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeFARCsActionPerformed
        UtilityCallbacks.mergeFileArchives();
    }//GEN-LAST:event_mergeFARCsActionPerformed

    private void saveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsActionPerformed
        FileCallbacks.saveAs();
    }//GEN-LAST:event_saveAsActionPerformed

    private void deleteContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteContextActionPerformed
        DatabaseCallbacks.delete();
    }//GEN-LAST:event_deleteContextActionPerformed

    private void zeroContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zeroContextActionPerformed
        DatabaseCallbacks.zero();
    }//GEN-LAST:event_zeroContextActionPerformed

    private void newFolderContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newFolderContextActionPerformed
        DatabaseCallbacks.newFolder();
    }//GEN-LAST:event_newFolderContextActionPerformed

    private void generateDiffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateDiffActionPerformed
        UtilityCallbacks.generateFileDBDiff();
    }//GEN-LAST:event_generateDiffActionPerformed

    private void loadModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadModActionPerformed
        File file = fileChooser.openFile("example.mod", "mod", "Mod", false);
        if (file == null) return;
        Mod mod = ModCallbacks.loadMod(file);
        if (mod != null && mod.isParsed) {
            addTab(mod);
            updateWorkspace();
        }
    }//GEN-LAST:event_loadModActionPerformed

    private void openModMetadataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openModMetadataActionPerformed
        new ModEditor((Mod) getCurrentDB(), false).setVisible(true);
    }//GEN-LAST:event_openModMetadataActionPerformed

    private void editProfileSlotsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProfileSlotsActionPerformed
        getCurrentDB().shouldSave = true;
        new SlotEditor(this, ((BigProfile) getCurrentDB()).profile, SlotEditor.EditorType.BIG_PROFILE_SLOTS, ((BigProfile) getCurrentDB()).revision).setVisible(true);
    }//GEN-LAST:event_editProfileSlotsActionPerformed

    private void newVitaDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newVitaDBActionPerformed
        DatabaseCallbacks.newFileDB(936);
    }//GEN-LAST:event_newVitaDBActionPerformed

    private void newModernDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newModernDBActionPerformed
        DatabaseCallbacks.newFileDB(21496064);
    }//GEN-LAST:event_newModernDBActionPerformed

    private void newLegacyDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newLegacyDBActionPerformed
        DatabaseCallbacks.newFileDB(256);
    }//GEN-LAST:event_newLegacyDBActionPerformed

    public boolean confirmOverwrite(File file) {
        if (file.exists()) {
            int result = JOptionPane.showConfirmDialog(null, "This file already exists, are you sure you want to override it?", "Confirm overwrite", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) return false;
            return true;
        }
        return true;
    }

    private void createFileArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createFileArchiveActionPerformed
        ArchiveCallbacks.newFileArchive();
    }//GEN-LAST:event_createFileArchiveActionPerformed

    private void editProfileItemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProfileItemsActionPerformed
        new MetadataEditor(this, ((BigProfile) getCurrentDB())).setVisible(true);
    }//GEN-LAST:event_editProfileItemsActionPerformed

    private void installProfileModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installProfileModActionPerformed
        UtilityCallbacks.installMod();
    }//GEN-LAST:event_installProfileModActionPerformed

    private void addKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addKeyActionPerformed
        ProfileCallbacks.addKey();
    }//GEN-LAST:event_addKeyActionPerformed

    private void duplicateContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateContextActionPerformed
        DatabaseCallbacks.duplicateItem();
    }//GEN-LAST:event_duplicateContextActionPerformed

    private void newItemContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newItemContextActionPerformed
        DatabaseCallbacks.newItem();
    }//GEN-LAST:event_newItemContextActionPerformed

    private void replaceDecompressedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceDecompressedActionPerformed
        ReplacementCallbacks.replaceDecompressed();
    }//GEN-LAST:event_replaceDecompressedActionPerformed

    private void replaceDependenciesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceDependenciesActionPerformed
        new Dependinator(this, Globals.lastSelected.entry);
    }//GEN-LAST:event_replaceDependenciesActionPerformed

    private void encodeIntegerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encodeIntegerActionPerformed
        UtilityCallbacks.encodeInteger();
    }//GEN-LAST:event_encodeIntegerActionPerformed

    private void exportAsModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsModActionPerformed
        ExportCallbacks.exportMod(true);
    }//GEN-LAST:event_exportAsModActionPerformed

    private void dumpRLSTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dumpRLSTActionPerformed
        DatabaseCallbacks.dumpRLST();
    }//GEN-LAST:event_dumpRLSTActionPerformed

    private void removeDependenciesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDependenciesActionPerformed
        DependencyCallbacks.removeDependencies();
    }//GEN-LAST:event_removeDependenciesActionPerformed

    private void removeMissingDependenciesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMissingDependenciesActionPerformed
        DependencyCallbacks.removeMissingDependencies();
    }//GEN-LAST:event_removeMissingDependenciesActionPerformed

    private void exportOBJTEXCOORD1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportOBJTEXCOORD1ActionPerformed
        ExportCallbacks.exportOBJ(1);
    }//GEN-LAST:event_exportOBJTEXCOORD1ActionPerformed

    private void exportOBJTEXCOORD2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportOBJTEXCOORD2ActionPerformed
        ExportCallbacks.exportOBJ(2);
    }//GEN-LAST:event_exportOBJTEXCOORD2ActionPerformed

    private void replaceImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceImageActionPerformed
        ReplacementCallbacks.replaceImage();
    }//GEN-LAST:event_replaceImageActionPerformed

    private void dumpBPRToModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dumpBPRToModActionPerformed
        BigProfile profile = (BigProfile) getCurrentDB();
        profile.dumpToMod();
    }//GEN-LAST:event_dumpBPRToModActionPerformed

    private void rebootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rebootActionPerformed
        checkForChanges(); dispose();
        Toolkit toolkit = new Toolkit();
        toolkit.setVisible(true);
    }//GEN-LAST:event_rebootActionPerformed

    private void addAllPlansToInventoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAllPlansToInventoryActionPerformed
        DebugCallbacks.addAllPlansToInventoryTable();
    }//GEN-LAST:event_addAllPlansToInventoryActionPerformed

    private void convertAllToGUIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertAllToGUIDActionPerformed
        DebugCallbacks.convertAllToGUID();
    }//GEN-LAST:event_convertAllToGUIDActionPerformed

    private void closeTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeTabActionPerformed
        FileCallbacks.closeTab();
    }//GEN-LAST:event_closeTabActionPerformed

    private void testSerializeCurrentMeshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testSerializeCurrentMeshActionPerformed
        DebugCallbacks.reserializeCurrentMesh();
    }//GEN-LAST:event_testSerializeCurrentMeshActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        FileDB mod = (FileDB) getCurrentDB();
        
        Output output = new Output((mod.entries.size() * 0x1C) + 0x4);
        output.i32(mod.entries.size());
        for (FileEntry e : mod.entries) {
            output.i32(1);
            output.bytes(e.SHA1);
            output.i32(13);
        }
        
        FileIO.write(output.buffer, "C:/Users/Shan/Desktop/table");
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void exportGLTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportGLTFActionPerformed
        ExportCallbacks.exportGLB();
    }//GEN-LAST:event_exportGLTFActionPerformed

    private void debugJokerTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debugJokerTestActionPerformed
        DebugCallbacks.jokerTest();
    }//GEN-LAST:event_debugJokerTestActionPerformed

    private void debugAddSlotsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debugAddSlotsActionPerformed
        DebugCallbacks.addSlots();
    }//GEN-LAST:event_debugAddSlotsActionPerformed

    private void debugRecompressAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debugRecompressAllActionPerformed
        DebugCallbacks.recompressAllSlots();
    }//GEN-LAST:event_debugRecompressAllActionPerformed

    private void newModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newModActionPerformed
        UtilityCallbacks.newMod();
    }//GEN-LAST:event_newModActionPerformed

    private void exportAsModGUIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsModGUIDActionPerformed
        ExportCallbacks.exportMod(false);
    }//GEN-LAST:event_exportAsModGUIDActionPerformed

    private void renameItemContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameItemContextActionPerformed
        DatabaseCallbacks.renameItem();
    }//GEN-LAST:event_renameItemContextActionPerformed

    private void changeGUIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeGUIDActionPerformed
        DatabaseCallbacks.changeGUID();
    }//GEN-LAST:event_changeGUIDActionPerformed

    private void changeHashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeHashActionPerformed
        DatabaseCallbacks.changeHash();
    }//GEN-LAST:event_changeHashActionPerformed

    private void scanFileArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanFileArchiveActionPerformed
        UtilityCallbacks.scanFileArchive();
    }//GEN-LAST:event_scanFileArchiveActionPerformed

    private void emittionTendencyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emittionTendencyActionPerformed
        DebugCallbacks.emittionTendency();
    }//GEN-LAST:event_emittionTendencyActionPerformed

    private void exportAnimationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAnimationActionPerformed
        ExportCallbacks.exportAnimation();
    }//GEN-LAST:event_exportAnimationActionPerformed

    private void loadSavedataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSavedataActionPerformed
        String directory = fileChooser.openDirectory();
        if (directory.isEmpty()) return;
        FileSave save = new FileSave(new File(directory));
        this.addTab(save);
        this.updateWorkspace();
        
    }//GEN-LAST:event_loadSavedataActionPerformed

    public void generateDependencyTree(FileEntry entry, FileModel model) {
        if (entry.dependencies != null) {
            FileNode root = (FileNode) model.getRoot();
            for (int i = 0; i < entry.dependencies.length; ++i) {
                if (entry.dependencies[i] == null || entry.dependencies[i].path == null) continue;
                Nodes.addNode(root, entry.dependencies[i]);
                if (entry.dependencies[i].dependencies != null) // These files have way too many dependencies
                    generateDependencyTree(entry.dependencies[i], model);
            }
        }
    }

    public FileNode getLastSelected(JTree tree) {
        Globals.entries.clear();
        TreePath[] treePaths = tree.getSelectionPaths();
        setImage(null);
        if (treePaths == null) {
            Globals.lastSelected = null;
            return null;
        }
        for (int i = 0; i < treePaths.length; ++i) {
            FileNode node = (FileNode) treePaths[i].getLastPathComponent();
            if (node == null) {
                Globals.lastSelected = null;
                return null;
            };
            if (node.getChildCount() > 0)
                Nodes.loadChildren(Globals.entries, node, true);
            Globals.entries.add(node);
        }
        FileNode selected = (FileNode) treePaths[treePaths.length - 1].getLastPathComponent();
        Globals.lastSelected = selected;
        return selected;
    }

    public void populateMetadata(InventoryItem item) {
        if (item == null || !Globals.canExtract()) return;
        InventoryMetadata metadata = item.metadata;
        if (metadata == null) return;

        iconField.setText("");
        if (metadata.icon != null && (metadata.icon.hash != null || metadata.icon.GUID != -1))
            loadImage(metadata.icon, item);

        if (Globals.lastSelected.entry.item != item) return;

        setPlanDescriptions(metadata);

        pageCombo.setSelectedItem(metadata.type);
        subCombo.setSelectedItem(metadata.subType);
        creatorField.setText(metadata.creator.PSID);

        entryModifiers.setEnabledAt(1, true);
        entryModifiers.setSelectedIndex(1);
    }

    public void setPlanDescriptions(InventoryMetadata metadata) {
        titleField.setText("" + metadata.titleKey);
        descriptionField.setText("" + metadata.descriptionKey);

        locationField.setText("" + metadata.location);
        categoryField.setText("" + metadata.category);

        if (Globals.LAMS != null) {
            StringMetadata.setEnabled(true);
            StringMetadata.setSelected(true);

            titleField.setText(Globals.Translate(metadata.titleKey));

            descriptionField.setText(Globals.Translate(metadata.descriptionKey));

            metadata.translatedLocation = Globals.Translate(metadata.location);
            metadata.translatedCategory = Globals.Translate(metadata.category);
            locationField.setText(metadata.translatedLocation);
            categoryField.setText(metadata.translatedCategory);
        } else {
            LAMSMetadata.setSelected(true);
            LAMSMetadata.setEnabled(true);
            StringMetadata.setEnabled(false);
        }

        if (metadata.userCreatedDetails != null) {
            StringMetadata.setEnabled(true);
            StringMetadata.setSelected(true);
            if (metadata.userCreatedDetails.title != null)
                titleField.setText(metadata.userCreatedDetails.title);

            if (metadata.userCreatedDetails.description != null)
                descriptionField.setText(metadata.userCreatedDetails.description);

            locationField.setText("");
            categoryField.setText("");

            if (metadata.category == 1737521) categoryField.setText("My Photos");
            else if (metadata.category == 1598223) categoryField.setText("My Pods");
            else if (metadata.category == 928006) categoryField.setText("My Objects");
            else if (metadata.category == 578814) categoryField.setText("My Costumes");
        }
    }

    public void loadImage(ResourcePtr resource, InventoryItem item) {
        FileEntry entry = null;
        byte[] hash = null;
        if (resource == null) return;
        iconField.setText(resource.toString());
        if (resource.hash != null)
            hash = resource.hash;
        else {
            entry = Globals.findEntry(resource.GUID);
            if (entry != null)
                hash = entry.SHA1;
        }

        if (hash == null) return;
        if (entry == null) entry = Globals.findEntry(hash);

        if (entry != null && entry.texture != null)
            setImage(entry.texture.getImageIcon(320, 320));
        else {
            byte[] data = Globals.extractFile(hash);
            if (data == null) return;
            Texture texture = new Texture(data);
            if (entry != null) entry.texture = texture;
            if (texture.parsed == true)
                if (Globals.lastSelected.entry.item == item)
                    setImage(texture.getImageIcon(320, 320));
        }
    }

    public void setImage(ImageIcon image) {
        if (image == null) {
            texture.setText("No preview to be displayed");
            texture.setIcon(null);
        } else {
            texture.setText(null);
            texture.setIcon(image);
        }
    }

    public void setEditorPanel(FileNode node) {
        FileEntry entry = node.entry;
        if (entry == null) {
            entryTable.setValueAt(node.path + node.header, 0, 1);
            for (int i = 1; i < 8; ++i)
                entryTable.setValueAt("N/A", i, 1);
            return;
        }

        entryTable.setValueAt(entry.path, 0, 1);

        if (entry.timestamp != 0) {
            Timestamp timestamp = new Timestamp(entry.timestamp * 1000L);
            entryTable.setValueAt(timestamp.toString(), 1, 1);
        } else entryTable.setValueAt("N/A", 1, 1);
        entryTable.setValueAt(Bytes.toHex(entry.SHA1), 2, 1);
        entryTable.setValueAt(Integer.valueOf(entry.size), 3, 1);
        if (entry.GUID != -1) {
            entryTable.setValueAt("g" + Long.valueOf(entry.GUID), 4, 1);
            entryTable.setValueAt(Bytes.toHex(entry.GUID), 5, 1);
            entryTable.setValueAt(Bytes.toHex(Bytes.encode(entry.GUID)), 6, 1);
        } else {
            entryTable.setValueAt("N/A", 4, 1);
            entryTable.setValueAt("N/A", 5, 1);
            entryTable.setValueAt("N/A", 6, 1);
        }
        if (entry.revision != 0)
            entryTable.setValueAt(Bytes.toHex(entry.revision), 7, 1);
    }

    public void setHexEditor(byte[] bytes) {
        if (bytes == null) {
            hex.setData(null);
            hex.setDefinitionStatus(JHexView.DefinitionStatus.UNDEFINED);
            hex.setEnabled(false);
        } else {
            hex.setData(new SimpleDataProvider(bytes));
            hex.setDefinitionStatus(JHexView.DefinitionStatus.DEFINED);
            hex.setEnabled(true);
        }
        hex.repaint();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JMenu FARMenu;
    private javax.swing.JRadioButton LAMSMetadata;
    private javax.swing.JMenu MAPMenu;
    private javax.swing.JMenu ProfileMenu;
    private javax.swing.JRadioButton StringMetadata;
    private javax.swing.JMenuItem addAllPlansToInventory;
    private javax.swing.JMenuItem addFile;
    private javax.swing.JMenuItem addKey;
    private javax.swing.JTextField categoryField;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JMenuItem changeGUID;
    private javax.swing.JMenuItem changeHash;
    private javax.swing.JMenuItem clear;
    private javax.swing.JMenuItem closeTab;
    private javax.swing.JTextArea console;
    private javax.swing.JScrollPane consoleContainer;
    private javax.swing.JPopupMenu consolePopup;
    private javax.swing.JMenuItem convertAllToGUID;
    private javax.swing.JMenuItem createFileArchive;
    private javax.swing.JTextField creatorField;
    private javax.swing.JLabel creatorLabel;
    private javax.swing.JMenuItem debugAddSlots;
    private javax.swing.JMenuItem debugJokerTest;
    public javax.swing.JMenu debugMenu;
    private javax.swing.JMenuItem debugRecompressAll;
    private javax.swing.JMenuItem decompressResource;
    private javax.swing.JMenuItem deleteContext;
    private javax.swing.JMenu dependencyGroup;
    public javax.swing.JTree dependencyTree;
    private javax.swing.JScrollPane dependencyTreeContainer;
    private javax.swing.JTextArea descriptionField;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JSplitPane details;
    private javax.swing.JMenuItem dumpBPRToMod;
    private javax.swing.JMenuItem dumpHashes;
    private javax.swing.JMenuItem dumpRLST;
    private javax.swing.JPopupMenu.Separator dumpSep;
    private javax.swing.JMenuItem duplicateContext;
    private javax.swing.JMenu editMenuContext;
    private javax.swing.JMenuItem editProfileItems;
    private javax.swing.JMenuItem editProfileSlots;
    private javax.swing.JMenuItem editSlotContext;
    private javax.swing.JMenuItem emittionTendency;
    private javax.swing.JMenuItem encodeInteger;
    private javax.swing.JPopupMenu entryContext;
    private javax.swing.JSplitPane entryData;
    public javax.swing.JTabbedPane entryModifiers;
    public javax.swing.JTable entryTable;
    private javax.swing.JMenuItem exportAnimation;
    private javax.swing.JMenuItem exportAsMod;
    private javax.swing.JMenuItem exportAsModGUID;
    private javax.swing.JMenuItem exportDDS;
    private javax.swing.JMenuItem exportGLTF;
    private javax.swing.JMenu exportGroup;
    private javax.swing.JMenuItem exportLAMSContext;
    private javax.swing.JMenu exportModGroup;
    private javax.swing.JMenu exportModelGroup;
    private javax.swing.JMenu exportOBJ;
    private javax.swing.JMenuItem exportOBJTEXCOORD0;
    private javax.swing.JMenuItem exportOBJTEXCOORD1;
    private javax.swing.JMenuItem exportOBJTEXCOORD2;
    private javax.swing.JMenuItem exportPNG;
    private javax.swing.JMenu exportTextureGroupContext;
    private javax.swing.JMenuItem extractBigProfile;
    private javax.swing.JMenuItem extractContext;
    private javax.swing.JMenu extractContextMenu;
    private javax.swing.JMenuItem extractDecompressedContext;
    public javax.swing.JTabbedPane fileDataTabs;
    public javax.swing.JMenu fileMenu;
    private javax.swing.JMenu gamedataMenu;
    private javax.swing.JMenuItem generateDiff;
    private tv.porst.jhexview.JHexView hex;
    private javax.swing.JTextField iconField;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JMenuItem installProfileMod;
    private javax.swing.JPanel itemMetadata;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JMenuItem loadArchive;
    public javax.swing.JMenuItem loadBigProfile;
    public javax.swing.JMenuItem loadDB;
    private javax.swing.JMenu loadGroupMenu;
    private javax.swing.JMenuItem loadLAMSContext;
    private javax.swing.JMenuItem loadMod;
    private javax.swing.JMenuItem loadSavedata;
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JMenu menuFileMenu;
    private javax.swing.JMenuItem mergeFARCs;
    private javax.swing.ButtonGroup metadataButtonGroup;
    public javax.swing.JMenu modMenu;
    public javax.swing.JMenu newFileDBGroup;
    private javax.swing.JMenuItem newFolderContext;
    private javax.swing.JMenu newGamedataGroup;
    private javax.swing.JMenuItem newItemContext;
    private javax.swing.JMenuItem newLegacyDB;
    private javax.swing.JMenuItem newMod;
    private javax.swing.JMenuItem newModernDB;
    private javax.swing.JMenuItem newVitaDB;
    private javax.swing.JMenuItem openCompressinator;
    public javax.swing.JMenuItem openModMetadata;
    private javax.swing.JComboBox<String> pageCombo;
    private javax.swing.JMenuItem patchMAP;
    public javax.swing.JSplitPane preview;
    private javax.swing.JSplitPane previewContainer;
    public javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem reboot;
    private javax.swing.JMenuItem removeDependencies;
    private javax.swing.JMenuItem removeMissingDependencies;
    private javax.swing.JMenuItem renameItemContext;
    private javax.swing.JMenuItem replaceCompressed;
    private javax.swing.JMenu replaceContext;
    private javax.swing.JMenuItem replaceDecompressed;
    private javax.swing.JMenuItem replaceDependencies;
    private javax.swing.JMenuItem replaceImage;
    public javax.swing.JMenuItem saveAs;
    private javax.swing.JPopupMenu.Separator saveDivider;
    public javax.swing.JMenuItem saveMenu;
    public javax.swing.JMenu savedataMenu;
    private javax.swing.JMenuItem scanFileArchive;
    private javax.swing.JMenuItem scanRawData;
    public javax.swing.JTextField search;
    private javax.swing.JComboBox<String> subCombo;
    private javax.swing.JScrollPane tableContainer;
    private javax.swing.JMenuItem testSerializeCurrentMesh;
    public javax.swing.JLabel texture;
    private javax.swing.JTextField titleField;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JMenuBar toolkitMenu;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JSplitPane treeContainer;
    private javax.swing.JSplitPane workspaceDivider;
    private javax.swing.JMenuItem zeroContext;
    // End of variables declaration//GEN-END:variables
}
