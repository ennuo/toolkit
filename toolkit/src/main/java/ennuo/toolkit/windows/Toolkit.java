package ennuo.toolkit.windows;

import com.bulenkov.darcula.DarculaLaf;
import ennuo.craftworld.types.BigProfile;
import ennuo.craftworld.types.FileArchive;
import ennuo.craftworld.types.FileDB;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.Mod;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Compressor;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.FileIO;
import ennuo.craftworld.memory.Images;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.resources.Animation;
import ennuo.craftworld.resources.Mesh;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.Pack;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.TranslationTable;
import ennuo.craftworld.resources.enums.Magic;
import ennuo.craftworld.resources.enums.Metadata.CompressionType;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.resources.structs.UserCreatedDetails;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.swing.Nodes;
import ennuo.craftworld.things.InventoryItem;
import ennuo.craftworld.things.InventoryMetadata;
import ennuo.craftworld.things.Serializer;
import ennuo.toolkit.streams.CustomPrintStream;
import ennuo.toolkit.streams.TextAreaOutputStream;
import ennuo.toolkit.utilities.FileChooser;
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import tv.porst.jhexview.JHexView;
import tv.porst.jhexview.SimpleDataProvider;

public class Toolkit extends javax.swing.JFrame {
    ExecutorService databaseService = Executors.newSingleThreadExecutor();
    ExecutorService resourceService = Executors.newSingleThreadExecutor();
    public final FileChooser fileChooser = new FileChooser(this);
   
    public ArrayList<FileNode> entries = new ArrayList<FileNode>();
    
    private FileNode lastSelected;
    
    ArrayList<FileData> databases = new ArrayList<FileData>();
    ArrayList<JTree> trees = new ArrayList<JTree>();
    
    ArrayList<FileArchive> archives = new ArrayList<FileArchive>();
    private TranslationTable LAMS;
    
    private boolean isBigProfile = false;
    private boolean isMod = false;
    private boolean fileExists = false;
    
    private boolean useContext = false;
        
    MouseListener ml = new MouseAdapter() {
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
                    lastSelected = null;
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
        
        debugMenu.setVisible(false);
        
        String username = System.getProperty("user.name").toLowerCase();
        
        if (username.equals("veryc")) {
            debugMenu.setVisible(true);
            setTitle("VeryCoolMe's Modding Emporium");
        }
        
        if (username.equals("shan") || username.equals("aidan")) {
            setTitle("BAZINGA!");
            debugMenu.setVisible(true);
        }
        
        if (username.equals("joele")) {
            setTitle("Acrosnus Toolkit");
            debugMenu.setVisible(true);
        }
        
        if (username.equals("etleg") || username.equals("eddie")) {
            setTitle("GregTool");
            debugMenu.setVisible(true);
        }
        
        if (debugMenu.isVisible())
            setTitle(getTitle() + " | Debug");
           
        setResizable(false);
        progressBar.setVisible(false);
        fileDataTabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int index = fileDataTabs.getSelectedIndex();
                if (index == -1) {
                    search.setEnabled(false);
                    isMod = false; isBigProfile = false;
                    search.setForeground(Color.GRAY);
                    search.setText("Search is currently disabled.");
                } else {
                    search.setEnabled(true);
                    FileData data = databases.get(fileDataTabs.getSelectedIndex()); 
                    search.setText(data.query);
                    if (search.getText().equals("Search...")) search.setForeground(Color.GRAY);
                    else search.setForeground(Color.WHITE);
                    isBigProfile = data.type.equals("Big Profile");
                    isMod = data.type.equals("Mod");
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
        
        dependencyTree.addMouseListener(ml);
        dependencyTree.addTreeSelectionListener(e -> treeSelectionListener(dependencyTree));
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                checkForChanges();
            }
        });
    }
    
    
    private void checkForChanges() {
       for (FileData data : databases) {
                    if (data.shouldSave) {
                        int result = JOptionPane.showConfirmDialog(null, String.format("Your %s (%s) has pending changes, do you want to save?", data.type, data.path), "Pending changes", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) data.save(data.path);
                    }
                }
                
                for (FileArchive archive : archives) {
                    if (archive.shouldSave) {
                        int result = JOptionPane.showConfirmDialog(null, String.format("Your FileArchive (%s) has pending changes, do you want to save?", archive.file.getAbsolutePath()), "Pending changes", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) archive.save();
                    }
                }
    }
    
    private FileData getCurrentDB() {
        if (databases.size() == 0) return null;
        return databases.get(fileDataTabs.getSelectedIndex());
    }
    
    private JTree getCurrentTree() {
        if (trees.size() == 0) return null;
        return trees.get(fileDataTabs.getSelectedIndex());
    }
    
    public void updateWorkspace() {
        closeTab.setVisible(fileDataTabs.getTabCount() != 0);
        installProfileMod.setVisible(false);
        int archiveCount = archives.size();
        FileData db = getCurrentDB();
        
        if (db != null) {
            if (db.shouldSave) {
                fileDataTabs.setTitleAt(fileDataTabs.getSelectedIndex(), db.name + " *");
                saveMenu.setEnabled(true);
            }
            else  {
                fileDataTabs.setTitleAt(fileDataTabs.getSelectedIndex(), db.name);
                saveMenu.setEnabled(false);
            }
        }
        
        fileExists = false;
        if (lastSelected != null && lastSelected.entry != null) {
            if (lastSelected.entry.data != null)
                fileExists = true;
        } else if (entries.size() > 1) fileExists = true;
       
        
        if (archiveCount != 0 || db != null) {
            saveDivider.setVisible(true);
            saveMenu.setVisible(true);
        } else {
            saveDivider.setVisible(false);
            saveMenu.setVisible(false);
        }
        
        if (archiveCount != 0 || isBigProfile || isMod)
            FARMenu.setVisible(true);
        else FARMenu.setVisible(false);
         
        
        if (isBigProfile || isMod || db != null) {
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
        }
        else {
            saveDivider.setVisible(true);
            dumpHashes.setVisible(true);
            dumpRLST.setVisible(false);
            if (!isBigProfile && !isMod) {
                if (archiveCount != 0)
                    installProfileMod.setVisible(true);
                MAPMenu.setVisible(true);
                dumpHashes.setVisible(true);
                dumpRLST.setVisible(true);
            }
        }
        
        if (isBigProfile) { ProfileMenu.setVisible(true); installProfileMod.setVisible(true); }
        else ProfileMenu.setVisible(false);
        
        if (isMod) modMenu.setVisible(true);
        else modMenu.setVisible(false);
        
    }
    
    private void generateEntryContext(JTree tree, int x, int y) {
        exportOBJ.setVisible(false);
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
        removeDependencies.setVisible(false);
        removeMissingDependencies.setVisible(false);
        exportAsMod.setVisible(false);
        exportOBJTEXCOORD0.setVisible(false);
        exportOBJTEXCOORD1.setVisible(false);
        exportOBJTEXCOORD2.setVisible(false);
        replaceImage.setVisible(false);
        
       
        if (isBigProfile && useContext) deleteContext.setVisible(true);
        
        if (!isBigProfile && databases.size() != 0) {
            if ((useContext && lastSelected.entry == null)) {
                 newItemContext.setVisible(true);
                 newFolderContext.setVisible(true);
            } else if (!useContext) newFolderContext.setVisible(true);
            if (useContext) {
                zeroContext.setVisible(true);
                deleteContext.setVisible(true);
                if (lastSelected.entry != null)
                    duplicateContext.setVisible(true);
            }
        }
        
        if ((archives.size() != 0 || isBigProfile || isMod) && lastSelected != null && lastSelected.entry != null)  {
            replaceContext.setVisible(true);
            if (lastSelected.header.endsWith(".tex"))
                replaceImage.setVisible(true);
        }
        
        if ((archives.size() != 0 || isBigProfile || isMod) && fileExists && useContext) {
            
            extractContextMenu.setVisible(true);
            
            if (lastSelected.entry != null) {
                if ((isBigProfile || databases.size() != 0) && lastSelected.entry.canReplaceDecompressed)  {
                    replaceDecompressed.setVisible(true);
                    if (lastSelected.entry.dependencies != null && lastSelected.entry.dependencies.length != 0) {
                        if (!isMod) exportAsMod.setVisible(true);
                        removeDependencies.setVisible(true);
                        replaceDependencies.setVisible(true);
                        removeMissingDependencies.setVisible(true);
                    }
                }
                
                if (lastSelected.header.endsWith(".mol")) {
                    if (lastSelected.entry.mesh != null) {
                    exportOBJ.setVisible(true);
                    int count = lastSelected.entry.mesh.attributeCount;
                    if (count > 0)
                        exportOBJTEXCOORD0.setVisible(true);
                    if (count > 1)
                        exportOBJTEXCOORD1.setVisible(true);
                    if (count > 2)
                        exportOBJTEXCOORD2.setVisible(true);
                    }
                }
                if (lastSelected.header.endsWith(".tex")) {
                    exportTextureGroupContext.setVisible(true);
                    replaceImage.setVisible(true);
                }
        
                if ( (lastSelected.header.endsWith(".bin") && isBigProfile) || (lastSelected.header.endsWith(".slt")) || (lastSelected.header.endsWith(".pck"))) 
                    editSlotContext.setVisible(true);
        
                if (lastSelected.header.endsWith(".trans")) {
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
        loadLAMSContext = new javax.swing.JMenuItem();
        exportLAMSContext = new javax.swing.JMenuItem();
        editSlotContext = new javax.swing.JMenuItem();
        exportOBJ = new javax.swing.JMenu();
        exportOBJTEXCOORD0 = new javax.swing.JMenuItem();
        exportOBJTEXCOORD1 = new javax.swing.JMenuItem();
        exportOBJTEXCOORD2 = new javax.swing.JMenuItem();
        exportAsMod = new javax.swing.JMenuItem();
        exportTextureGroupContext = new javax.swing.JMenu();
        exportPNG = new javax.swing.JMenuItem();
        exportDDS = new javax.swing.JMenuItem();
        replaceContext = new javax.swing.JMenu();
        replaceCompressed = new javax.swing.JMenuItem();
        replaceDecompressed = new javax.swing.JMenuItem();
        replaceDependencies = new javax.swing.JMenuItem();
        replaceImage = new javax.swing.JMenuItem();
        newItemContext = new javax.swing.JMenuItem();
        newFolderContext = new javax.swing.JMenuItem();
        duplicateContext = new javax.swing.JMenuItem();
        zeroContext = new javax.swing.JMenuItem();
        deleteContext = new javax.swing.JMenuItem();
        removeDependencies = new javax.swing.JMenuItem();
        removeMissingDependencies = new javax.swing.JMenuItem();
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
        loadGroupMenu = new javax.swing.JMenu();
        gamedataMenu = new javax.swing.JMenu();
        loadDB = new javax.swing.JMenuItem();
        loadArchive = new javax.swing.JMenuItem();
        savedataMenu = new javax.swing.JMenu();
        loadBigProfile = new javax.swing.JMenuItem();
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
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mergeFARCs = new javax.swing.JMenuItem();
        installProfileMod = new javax.swing.JMenuItem();
        debugMenu = new javax.swing.JMenu();
        addAllPlansToInventory = new javax.swing.JMenuItem();
        convertAllToGUID = new javax.swing.JMenuItem();
        dumpBPRToMod = new javax.swing.JMenuItem();

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

        loadLAMSContext.setText("Load LAMS");
        loadLAMSContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadLAMSContextActionPerformed(evt);
            }
        });
        entryContext.add(loadLAMSContext);

        exportLAMSContext.setText("Export as Text Document");
        exportLAMSContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportLAMSContextActionPerformed(evt);
            }
        });
        entryContext.add(exportLAMSContext);

        editSlotContext.setText("Edit Slot");
        editSlotContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSlotContextActionPerformed(evt);
            }
        });
        entryContext.add(editSlotContext);

        exportOBJ.setText("Export as OBJ");

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

        entryContext.add(exportOBJ);

        exportAsMod.setText("Export as Mod");
        exportAsMod.setToolTipText("");
        exportAsMod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsModActionPerformed(evt);
            }
        });
        entryContext.add(exportAsMod);

        exportTextureGroupContext.setText("Export Texture");

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

        entryContext.add(exportTextureGroupContext);

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

        removeDependencies.setText("Remove Dependencies");
        removeDependencies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDependenciesActionPerformed(evt);
            }
        });
        entryContext.add(removeDependencies);

        removeMissingDependencies.setText("Remove Missing Dependencies");
        removeMissingDependencies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMissingDependenciesActionPerformed(evt);
            }
        });
        entryContext.add(removeMissingDependencies);

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

        workspaceDivider.setDividerLocation(275);

        treeContainer.setDividerLocation(30);
        treeContainer.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

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

        details.setDividerLocation(850);

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

        entryData.setDividerLocation(132);
        entryData.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        entryData.setMaximumSize(new java.awt.Dimension(55, 2147483647));
        entryData.setMinimumSize(new java.awt.Dimension(55, 102));
        entryData.setPreferredSize(new java.awt.Dimension(55, 1120));

        entryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Path", "N/A"},
                {"Timestamp", "N/A"},
                {"SHA1", "N/A"},
                {"Size", "N/A"},
                {"GUID", "N/A"},
                {"GUID (Hex)", "N/A"},
                {"GUID (7-bit)", "N/A"}
            },
            new String [] {
                "Field", "Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
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

        entryModifiers.setMinimumSize(new java.awt.Dimension(55, 81));
        entryModifiers.setPreferredSize(new java.awt.Dimension(55, 713));

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
                                .addComponent(locationField, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
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
                .addContainerGap(36, Short.MAX_VALUE))
        );

        entryModifiers.addTab("Metadata", itemMetadata);

        entryData.setRightComponent(entryModifiers);

        details.setRightComponent(entryData);

        workspaceDivider.setRightComponent(details);

        progressBar.setBorder(null);
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

        toolkitMenu.add(debugMenu);

        setJMenuBar(toolkitMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(workspaceDivider, javax.swing.GroupLayout.DEFAULT_SIZE, 1385, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(workspaceDivider, javax.swing.GroupLayout.PREFERRED_SIZE, 517, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loadDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadDBActionPerformed
        File file = fileChooser.openFile("blurayguids.map", "map", "FileDB", false);
        if (file != null) loadFileDB(file);
    }//GEN-LAST:event_loadDBActionPerformed

    
    private void loadFileDB(File file) {
        databaseService.submit(() -> {
            
                savedataMenu.setEnabled(false);
                newFileDBGroup.setEnabled(false);
                loadDB.setEnabled(false);
                
                FileDB db = new FileDB(file, progressBar);
                
                if (db.isParsed) {
                    
                    int dbIndex = isDatabaseLoaded(file);
                    if (isDatabaseLoaded(file) != -1) {
                        
                        databases.set(dbIndex, db);
                        
                        JTree tree = trees.get(dbIndex);
                        tree.setModel(databases.get(dbIndex).model);
                        ((FileModel)tree.getModel()).reload();
                        
                        fileDataTabs.setSelectedIndex(dbIndex);
                        
                        search.setEditable(true);
                        search.setFocusable(true);
                        search.setText("Search...");
                        search.setForeground(Color.GRAY);
                    } else addTab(db);
                    
                    updateWorkspace();
                }
                
                savedataMenu.setEnabled(true);
                newFileDBGroup.setEnabled(true);
                loadDB.setEnabled(true);
                
        });
    }
    
    
    private void addTab(FileData data) {
        databases.add(data);
        
        JTree tree = new JTree();
        
        trees.add(tree);
        
        tree.setRootVisible(false);
        tree.setModel(data.model);
        tree.getSelectionModel().setSelectionMode(4);
        
        tree.addTreeSelectionListener(e -> treeSelectionListener(tree));
        tree.addMouseListener(ml);
        
        JScrollPane panel = new JScrollPane();
        panel.setViewportView(tree);
        
        fileDataTabs.addTab(data.name, panel);
        
        search.setEditable(true);
        search.setFocusable(true);
        search.setText("Search...");
        search.setForeground(Color.GRAY);
        
        fileDataTabs.setSelectedIndex(fileDataTabs.getTabCount() - 1);
    }
    
    
    private int isDatabaseLoaded(File file) {
        String path = file.getAbsolutePath();
        for (int i = 0; i < databases.size(); ++i) {
            FileData data = databases.get(i);
            if (data.path.equals(path))
                return i;
        }
        return -1;
    }
    
    private void loadArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadArchiveActionPerformed
        File[] files = fileChooser.openFiles("farc", "File Archive");
        if (files == null) return;
        for (File file : files)
            loadFileArchive(file);
    }//GEN-LAST:event_loadArchiveActionPerformed

    private void loadFileArchive(File file) {
        int index = isArchiveLoaded(file);
        if (index == -1) {
            FileArchive archive = new FileArchive(file);
            if (archive.isParsed)
                archives.add(archive);
        } else archives.get(index).process();
        updateWorkspace();
    }
    
    public FileEntry findEntry(ResourcePtr res) {
        if (res.GUID != -1) return findEntry(res.GUID);
        else if (res.hash != null) return findEntry(res.hash);
        return null;
    }
    
    public FileEntry findEntry(long GUID) {
        if (databases.size() == 0) return null;
        FileData db = getCurrentDB();
        if (!isBigProfile && !isMod) {
           FileEntry entry =  ((FileDB)db).find(GUID);
           if (entry != null)
               return entry;
        } else if (isMod) {
            FileEntry entry = ((Mod)db).find(GUID);
            if (entry != null)
                return entry;
        }
        for (FileData data : databases) {
            if (data.type.equals("FileDB")) {
                FileEntry entry = ((FileDB)data).find(GUID);
                if (entry != null) 
                    return entry;
            }
        }
        return null;
    }
    
    public FileEntry findEntry(byte[] sha1) {
        if (databases.size() == 0) return null;
        FileData db = getCurrentDB();
        if (!isBigProfile && !isMod) {
           FileEntry entry =  ((FileDB)db).find(sha1);
           if (entry != null)
               return entry;
        } else if (isBigProfile) {
            FileEntry entry = ((BigProfile)db).find(sha1);
            if (entry != null)
                return entry;
        } else if (isMod) {
            FileEntry entry = ((Mod)db).find(sha1);
            if (entry != null)
                return entry;
        }
        
        for (FileData data : databases) {
            if (data.type.equals("FileDB")) {
                FileEntry entry = ((FileDB)data).find(sha1);
                if (entry != null) 
                    return entry;
            }
        }
        return null;
    }
    
    public byte[] extractFile(long GUID) {
        FileData db = getCurrentDB();
        
        if (!isBigProfile && !isMod) {
           FileEntry entry =  ((FileDB)db).find(GUID);
           if (entry != null)
               return extractFile(entry.hash);
        } else if (isMod) {
            FileEntry entry = ((Mod)db).find(GUID);
            if (entry != null)
                return entry.data;
        }
        
        for (FileData data : databases) {
            if (data.type.equals("FileDB")) {
                FileEntry entry = ((FileDB)data).find(GUID);
                if (entry != null) {
                    byte[] buffer = extractFile(entry.hash);
                    if (buffer != null) return buffer;
                }
                    
            }
        }
        
        System.out.println("Could not extract g" + GUID);
        
        return null;
        
        
    }
    
    public byte[] extractFile(byte[] sha1) {
        FileData db = getCurrentDB();
        if (isBigProfile) {
            byte[] data = ((BigProfile)db).extract(sha1);
            if (data != null) return data;
        } else if (isMod) {
            byte[] data = ((Mod)db).extract(sha1);
            if (data != null) return data;
        }
        for (FileArchive archive : archives) {
            byte[] data = archive.extract(sha1);
            if (data != null) return data;
        }
        System.out.println("Could not extract h" + Bytes.toHex(sha1));
        return null;        
    }
    
    private int isArchiveLoaded(File file) {
        String path = file.getAbsolutePath();
        for (int i = 0; i < archives.size(); ++i) {
            FileArchive archive = archives.get(i);
            if (archive.file.getAbsolutePath().equals(path))
                return i;
        }
        return -1;
    }
    
    
    public void exportMesh(int channel) {
        File file = fileChooser.openFile(lastSelected.header.substring(0, lastSelected.header.length() - 4) + ".obj", "obj", "Wavefront Object (.OBJ)", true);
        if (file != null)
            lastSelected.entry.mesh.export(file.getAbsolutePath(), channel);
    }
    
    private void openCompressinatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openCompressinatorActionPerformed
        new Compressinator().setVisible(true);        
    }//GEN-LAST:event_openCompressinatorActionPerformed

    private void decompressResourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decompressResourceActionPerformed
        File file = fileChooser.openFile("data.bin", "", "", false);
        if (file == null) return;
        
        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data == null) return;
           
        Resource resource = new Resource(data);
        byte[] decompressed = resource.decompress();
        
        if (decompressed == null) {
            System.err.println("Failed to decompress resource.");
            return;
        }
        
        File out = fileChooser.openFile(file.getName() + ".dec", "", "", true);
        if (out != null)
            FileIO.write(decompressed, out.getAbsolutePath());
    }//GEN-LAST:event_decompressResourceActionPerformed

    private void saveMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuActionPerformed
        FileData db = getCurrentDB();
        if (db == null && archives.size() == 0) return;
        System.out.println("Saving workspace...");
        if (db != null) {
            if (db.shouldSave) {
                System.out.println("Saving " + db.type + " at " + db.path);
                db.save(db.path);     
            } else System.out.println(db.type + " has no pending changes, skipping save.");
        }
        
        for (FileArchive archive : archives) {
            if (archive.shouldSave) {
                System.out.println("Saving FileArchive at " + archive.file.getAbsolutePath());
                archive.save();   
            } else System.out.println("FileArchive has no pending changes, skipping save."); 
        }
        updateWorkspace();
    }//GEN-LAST:event_saveMenuActionPerformed

    private void addFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileActionPerformed
        if (archives.size() == 0 && !isBigProfile) return;
        
        File[] files = fileChooser.openFiles("data.bin", "");
        if (files == null) return;
        
        
        for (File file : files) {
            byte[] data = FileIO.read(file.getAbsolutePath());
            if (data == null) return;
        
            if (isBigProfile)
                ((BigProfile)getCurrentDB()).add(data);
            else addFile(data);
        }
        
        updateWorkspace();
        
        
        if (isBigProfile) {
            JTree tree  = getCurrentTree();
            TreePath selectionPath = tree.getSelectionPath();
            ((FileModel) tree.getModel()).reload();
            tree.setSelectionPath(selectionPath);
        }
        
        System.out.println("Added file to queue, make sure to save your workspace!");
    }//GEN-LAST:event_addFileActionPerformed

    public void addFile(byte[] data) {
        if (isBigProfile) {
            ((BigProfile)getCurrentDB()).add(data);
            updateWorkspace();
            return;
        }
        
        FileArchive[] archives = getSelectedArchives();
        if (archives == null) return;
        
        addFile(data, archives);
        
    }
    
    public void addFile(byte[] data, FileArchive[] archives) {
        for (FileArchive archive : archives)
            archive.add(data);
        updateWorkspace();
        System.out.println("Added file to queue, make sure to save your workspace!");
    }
    
    public void replaceEntry(FileEntry entry, byte[] data) {
        if (!isBigProfile) {
            entry.resetResources();
            addFile(data);
        }
        
        getCurrentDB().replace(entry, data);
        updateWorkspace();
        
        JTree tree  = getCurrentTree();
        TreePath selectionPath = tree.getSelectionPath();
        ((FileModel) tree.getModel()).reload();
        tree.setSelectionPath(selectionPath);
    }
    
    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
       console.selectAll();
       console.replaceSelection("");
    }//GEN-LAST:event_clearActionPerformed

    private void consoleMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_consoleMouseReleased
        if (evt.isPopupTrigger())
            consolePopup.show(console, evt.getX(), evt.getY());
    }//GEN-LAST:event_consoleMouseReleased

    private void extractContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractContextActionPerformed
        extract(false);
    }//GEN-LAST:event_extractContextActionPerformed

    private void extractDecompressedContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractDecompressedContextActionPerformed
        extract(true);
    }//GEN-LAST:event_extractDecompressedContextActionPerformed

    private void exportOBJTEXCOORD0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportOBJTEXCOORD0ActionPerformed
        exportMesh(0);
    }//GEN-LAST:event_exportOBJTEXCOORD0ActionPerformed

    private void loadLAMSContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadLAMSContextActionPerformed
        LAMS = new TranslationTable(new Data(lastSelected.entry.data));
        if (LAMS != null) StringMetadata.setEnabled(true);
    }//GEN-LAST:event_loadLAMSContextActionPerformed

    private void locationFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationFieldActionPerformed

    }//GEN-LAST:event_locationFieldActionPerformed

    private void LAMSMetadataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LAMSMetadataActionPerformed
        InventoryMetadata metadata = lastSelected.entry.item.metadata;
        titleField.setText("" + metadata.titleKey);
        descriptionField.setText("" + metadata.descriptionKey);
        locationField.setText("" + metadata.location);
        categoryField.setText("" + metadata.category);
    }//GEN-LAST:event_LAMSMetadataActionPerformed

    private void StringMetadataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StringMetadataActionPerformed
        InventoryMetadata metadata = lastSelected.entry.item.metadata;
        
        if (LAMS != null) {
            titleField.setText(LAMS.Translate(metadata.titleKey));
            descriptionField.setText(LAMS.Translate(metadata.descriptionKey));
            locationField.setText(LAMS.Translate(metadata.location));
            categoryField.setText(LAMS.Translate(metadata.category));
        }
        
        if (metadata.userCreatedDetails.title != null)
            titleField.setText(metadata.userCreatedDetails.title);
        
        if (metadata.userCreatedDetails.description != null)
            descriptionField.setText(metadata.userCreatedDetails.description);
        
        if (LAMS != null) return;
        
        locationField.setText("");
        categoryField.setText("");
            
        if (metadata.category == 1737521) categoryField.setText("My Photos");
        else if (metadata.category == 1598223) categoryField.setText("My Pods");
        else if (metadata.category == 928006) categoryField.setText("My Objects");
        else if (metadata.category == 578814) categoryField.setText("My Costumes");
    }//GEN-LAST:event_StringMetadataActionPerformed

    private void loadBigProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadBigProfileActionPerformed
        File file = fileChooser.openFile("bigfart1", "", "Big Profile", false);
        if (file != null) {
            BigProfile profile = new BigProfile(file);
            if (!profile.isParsed) return;
            addTab(profile);
            updateWorkspace();
        }
    }//GEN-LAST:event_loadBigProfileActionPerformed

    public FileArchive[] getSelectedArchives() {
        if (archives.size() == 0) return null;
        if (archives.size() > 1) {
            FileArchive[] archives = new ArchiveSelector(this, true).getSelected();
            if (archives == null) System.out.println("User did not select any FileArchives, cancelling operation.");
            return archives;
        }
        return new FileArchive[] { this.archives.get(0) };
    }
    
    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        getCurrentDB().query = search.getText();
        JTree tree = getCurrentTree();
        Nodes.filter((FileNode) tree.getModel().getRoot(), search.getText());
        ((FileModel) tree.getModel()).reload();
        tree.updateUI();
    }//GEN-LAST:event_searchActionPerformed

    private void dumpHashesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dumpHashesActionPerformed
        File file = fileChooser.openFile("hashes.txt", "txt", "Text File", true);
        if (file == null) return;
        FileDB db = (FileDB) getCurrentDB();
        StringBuilder builder = new StringBuilder(0x100 * db.entries.size());
        for (FileEntry entry : db.entries)
            builder.append(Bytes.toHex(entry.hash) + '\n');
        FileIO.write(builder.toString().getBytes(), file.getAbsolutePath());
    }//GEN-LAST:event_dumpHashesActionPerformed

    private void extractBigProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractBigProfileActionPerformed
        File file = fileChooser.openFile("profile.bpr", "bpr", "Big Profile", true);
        if (file == null) return;
        BigProfile save = (BigProfile) getCurrentDB();
        FileIO.write(save.profile.data, file.getAbsolutePath());
    }//GEN-LAST:event_extractBigProfileActionPerformed

    private void editSlotContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSlotContextActionPerformed
        if (isBigProfile) {
             getCurrentDB().shouldSave = true;
             new SlotEditor(this, lastSelected.entry).setVisible(true);
        }
        else
        {
            boolean isSlotsFile = lastSelected.entry.pack == null;
            new SlotEditor(this, lastSelected.entry, (isSlotsFile) ? SlotEditor.EditorType.SLOTS : SlotEditor.EditorType.PACKS).setVisible(true);
        }
            
    }//GEN-LAST:event_editSlotContextActionPerformed

    private void scanRawDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanRawDataActionPerformed
        File dump = fileChooser.openFile("data.bin", "", "", false);
        if (dump == null) return;

        System.out.println("Loading Image into memory, this may take a while.");

        Data data = new Data(dump.getAbsolutePath());

        System.out.println("Image loaded");
        
        
        File dumpMap = fileChooser.openFile("dump.map", "", "", true);
        if (dumpMap == null) return;
        
        FileIO.write(new byte[] { 0x00, 0x00, 0x01, 0x00, 0x00, 0x00 , 0x00, 0x00 }, dumpMap.getAbsolutePath());
        
        FileDB out = new FileDB(dumpMap);
        
        File file = fileChooser.openFile("dump.farc", "farc", "FileArchive", true);
        if (file == null) return;
        FileIO.write(new byte[] {
            0,
            0,
            0,
            0,
            0x46,
            0x41,
            0x52,
            0x43
        }, file.getAbsolutePath());
        FileArchive farc = new FileArchive(file);

        String[] headers = new String[59];
        Byte[] chars = new Byte[59];
        int m = 0;
        for (Magic magic: Magic.values()) {
            headers[m] = magic.value.substring(0, 3);
            chars[m] = (byte) magic.value.charAt(0);
            m++;
        } 
        headers[56] = "TEX"; chars[56] = (byte) "T".charAt(0);
        headers[57] = "FSB"; chars[57] = (byte) "F".charAt(0);
        headers[58] = "BIK"; chars[58] = (byte) "B".charAt(0);

        Set <String> HEADERS = new HashSet <String> (Arrays.asList(headers));
        Set <Byte> VALUES = new HashSet <Byte> (Arrays.asList(chars));

        databaseService.submit(() -> {
            System.out.println("Started scanning this may take a while, please wait.");
            progressBar.setVisible(true);
            progressBar.setMaximum(data.length);

            int resourceCount = 0;
            int GUID = 0x00150000;
            while ((data.offset + 4) <= data.length) {
                progressBar.setValue(data.offset + 1);
                int begin = data.offset;

                if (!VALUES.contains(data.data[data.offset])) {
                    data.offset++;
                    continue;
                }

                String magic = data.str(3);

                if (!HEADERS.contains(magic)) {
                    data.seek(begin + 1);
                    continue;
                }

                String type = data.str(1);

                try {
                    byte[] buffer = null;

                    switch (type) {
                        case "i": {
                            if (!magic.equals("BIK")) break;
                            int size = Integer.reverseBytes(data.int32());
                            data.offset -= 8;
                            buffer =  data.bytes(size + 8);
                        }
                        case "t":
                            {
                                if (magic.equals("FSB") || magic.equals("TEX")) break;
                                int end = 0;
                                while ((data.offset + 4) <= data.length) {
                                    String mag = data.str(3);
                                    if (HEADERS.contains(mag)) {
                                        String t = data.str(1);
                                        if (t.equals(" ") || t.equals("4") || t.equals("b") || t.equals("t") || t.equals("i")) {
                                            data.offset -= 4;
                                            end = data.offset;
                                            data.seek(begin);
                                            break;
                                        }
                                    } else data.offset -= 2;
                                }
                                
                                buffer = data.bytes(end - begin);
                                
                                final String converted = new String(buffer, StandardCharsets.UTF_8);
                                final byte[] output = converted.getBytes(StandardCharsets.UTF_8);
                                
                                if (!Arrays.equals(buffer, output))
                                    buffer = null;
                                
                                break;
                            }


                        case "4":
                            {
                                if (!magic.equals("FSB")) break;
                                int count = Integer.reverseBytes(data.int32());
                                data.forward(0x4);
                                int size = Integer.reverseBytes(data.int32());
                                data.forward(0x20);
                                for (int i = 0; i < count; ++i)
                                    data.forward(Short.reverseBytes(data.int16()) - 2);
                                if (data.data[data.offset] == 0) {
                                    while (data.int8() == 0);
                                    data.offset -= 1;
                                }
                                data.forward(size);
                                size = data.offset - begin;
                                data.seek(begin);
                                buffer = data.bytes(size);
                                break;
                            }

                        case "b":
                            {
                                if (magic.equals("FSB") || magic.equals("TEX")) break;
                                int revision = data.int32f();
                                if (revision > 0x021803F9 || revision < 0) {
                                    data.seek(begin + 1);
                                    continue;
                                }
                                int dependencyOffset = data.int32f();
                                data.forward(dependencyOffset - 12);
                                int count = data.int32f();
                                for (int j = 0; j < count; ++j) {
                                    data.resource(RType.FILE_OF_BYTES, true);
                                    data.int32f();
                                }

                                int size = data.offset - begin;
                                data.seek(begin);

                                buffer = data.bytes(size);
                            }

                        case " ":
                            {
                                if (!magic.equals("TEX")) break;
                                data.forward(2);
                                int count = data.int16();
                                int size = 0;
                                for (int j = 0; j < count; ++j) {
                                    size += data.int16();
                                    data.int16();
                                }
                                data.forward(size);


                                if (data.offset < 0 || ((data.offset + 1) >= data.length)) {
                                    data.seek(begin + 1);
                                    continue;
                                }

                                size = data.offset - begin;
                                data.seek(begin);
                                buffer = data.bytes(size);
                            }


                    }

                    data.seek(begin + 1);

                    if (buffer != null) {
                        int querySize = ((buffer.length * 10) + farc.queueSize + farc.hashTable.length + 8 + (farc.entries.size() * 0x1C)) * 2;
                        if (querySize < 0 || querySize >= Integer.MAX_VALUE) {
                            System.out.println("Ran out of memory, flushing current changes...");
                            farc.save(progressBar);
                            progressBar.setMaximum(data.length);
                            progressBar.setValue(data.offset + 1);
                        }

                        resourceCount++;
                        farc.add(buffer);

                        byte[] sha1 = Bytes.SHA1(buffer);
                        FileEntry entry = findEntry(sha1);
                        if (entry != null) {
                            System.out.println("Found Resource : " + entry.path + " (0x" + Bytes.toHex(begin) + ")");
                            out.add(entry);
                        }
                        // System.out.println("Found Resource : " + entry.path + " (0x" + Bytes.toHex(begin) + ")");
                        else {
                            
                            
                            FileEntry e = new FileEntry(buffer, Bytes.SHA1(buffer));
                            
                            String name = "" + begin;
                            
                            switch (magic) {
                                case "PLN": name += ".plan"; break;
                                case "LVL": name += ".bin"; break;
                                default: name += "." + magic.toLowerCase(); break;
                            }
                            
                            if (magic.equals("MSH")) {
                                Mesh mesh = new Mesh(buffer);
                                name = mesh.bones[0].name + ".mol";
                            }
                            
                            
                            e.path = "resources/" + magic.toLowerCase() + "/" + name;
                            e.GUID = GUID;
                            
                            GUID++;

                            out.add(e);
                            
                            
                            System.out.println("Found Resource : " + magic + type + " (0x" + Bytes.toHex(begin) + ")");
                        }

                    }

                } catch (Exception e) {
                    data.seek(begin + 1);
                }
            }


            progressBar.setVisible(false);
            progressBar.setMaximum(0);
            progressBar.setValue(0);

            farc.save(progressBar);
            out.save(out.path);
        });
    }//GEN-LAST:event_scanRawDataActionPerformed

    private void exportLAMSContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportLAMSContextActionPerformed
        if (LAMS == null)
            LAMS = new TranslationTable(new Data(lastSelected.entry.data));
        if (LAMS != null) {
            Output out = new Output(0xFEFF * LAMS.map.size());
            for (Map.Entry<Long, String> entry : LAMS.map.entrySet())
                out.string(entry.getKey() + "\n\t" + entry.getValue() + "\n");
            File file = fileChooser.openFile(lastSelected.header.substring(0, lastSelected.header.length() - 5) + ".txt", "txt", "Text Document", true);
            if (file == null) return;
            out.shrinkToFit();
            FileIO.write(out.buffer, file.getAbsolutePath());
        }
    }//GEN-LAST:event_exportLAMSContextActionPerformed

    private void exportDDSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDDSActionPerformed
        File file = fileChooser.openFile(lastSelected.header.substring(0, lastSelected.header.length() - 4) + "dds", "DDS", "Image", true);
        if (file == null) return;
        
        if (lastSelected.entry.texture == null || !lastSelected.entry.texture.parsed) return;
        
        FileIO.write(lastSelected.entry.texture.data, file.getAbsolutePath());
    }//GEN-LAST:event_exportDDSActionPerformed

    private void exportPNGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPNGActionPerformed
        export("png");
    }//GEN-LAST:event_exportPNGActionPerformed

    private void patchMAPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_patchMAPActionPerformed
        File file = fileChooser.openFile("brg_patch.map", "map", "FileDB", false);
        FileDB newDB = new FileDB(file);
        FileDB db = (FileDB) getCurrentDB();
        int added = 0, patched = 0;
        for (FileEntry entry : newDB.entries) {
            int old = db.entries.size();
            db.add(entry);
            if (old == db.entries.size()) patched++;
            else added++;
        }
        updateWorkspace();
        System.out.println(String.format("Succesfully updated FileDB (added = %d, patched = %d)", added, patched));
        ((FileModel) getCurrentTree().getModel()).reload();
    }//GEN-LAST:event_patchMAPActionPerformed

    private void replaceCompressedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceCompressedActionPerformed
        File file = fileChooser.openFile(lastSelected.header, "", "Resource", false);
        if (file == null) return;
        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data != null) replaceEntry(lastSelected.entry, data);
    }//GEN-LAST:event_replaceCompressedActionPerformed

    private void mergeFARCsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeFARCsActionPerformed
        File base = fileChooser.openFile("base.farc", "farc", "Base FileArchive", false);
        if (base == null) return;
        FileArchive archive;
        int index = isArchiveLoaded(base);
        if (index != -1) archive = archives.get(index);
        else archive = new FileArchive(base);
        
        if (archive == null) {
            System.err.println("Base FileArchive is null! Aborting!");
            return;
        }
        
        File patch = fileChooser.openFile("patch.farc", "farc", "Patch FileArchive", false);
        if (patch == null) return;
        FileArchive pArchive = new FileArchive(patch);
        
        if (pArchive == null) {
            System.err.println("Patch FileArchive is null! Aborting!");
            return;
        }
        
        resourceService.submit(() -> { 
            progressBar.setVisible(true);
            progressBar.setMaximum(pArchive.entries.size());
            progressBar.setValue(0);
            int count = 0;
            for (FileEntry entry : pArchive.entries) {
                progressBar.setValue(count + 1);
                
                
                byte[] data = pArchive.extract(entry);
                
                int querySize = ((data.length * 10) + archive.queueSize +  archive.hashTable.length + 8 + (archive.entries.size() * 0x1C)) * 2;
                if (querySize < 0 || querySize >= Integer.MAX_VALUE) {
                    System.out.println("Ran out of memory, flushing current changes...");
                    archive.save(progressBar);
                    progressBar.setMaximum(pArchive.entries.size());
                    progressBar.setValue(count + 1);
                }
                
                archive.add(pArchive.extract(entry));
                count++;
            }
            
            archive.save(progressBar); 
            
            progressBar.setVisible(false);
            progressBar.setMaximum(0); progressBar.setValue(0);
        });
        
        JOptionPane.showMessageDialog(this, "Please wait..");
    }//GEN-LAST:event_mergeFARCsActionPerformed

    private void saveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsActionPerformed
        String ext = "", type = "";
        if (isBigProfile) type = "Big Profile";
        else if (isMod) { ext = ".mod"; type = "Mod"; }
        else { ext = ".map"; type = "FileDB"; } 
        
        FileData db = getCurrentDB();
        
        File file = fileChooser.openFile(db.name, ext, type, true);
        if (file == null) return;
        db.save(file.getAbsolutePath());
    }//GEN-LAST:event_saveAsActionPerformed

    private void deleteContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteContextActionPerformed
        
        JTree tree = getCurrentTree();

        TreePath[] paths = tree.getSelectionPaths();
        TreeSelectionModel model = tree.getSelectionModel();
        int[] rows = tree.getSelectionRows();

        if (!isBigProfile) {
            FileData db = getCurrentDB();
            for (FileNode node : entries) {
                FileEntry entry = node.entry;
                node.removeFromParent();
                if (node.entry == null) continue;
                if (isMod) ((Mod)db).remove(entry);
                else ((FileDB)db).remove(entry);
            }   
        }
        
        if (isBigProfile) {
            BigProfile profile = (BigProfile) getCurrentDB();
            for (FileNode node : entries) {
                FileEntry entry = node.entry;
                node.removeFromParent();
                if (entry == null) continue;
                if (entry.slot != null) profile.slots.remove(entry.slot);
                if (entry.profileItem != null) profile.inventoryCollection.remove(entry.profileItem);
                profile.entries.remove(entry);
                profile.shouldSave = true;
            }
        }

        ((FileModel) tree.getModel()).reload();
        
        updateWorkspace();

        tree.setSelectionPaths(paths);
        tree.setSelectionRows(rows);
        tree.setSelectionModel(model);
    }//GEN-LAST:event_deleteContextActionPerformed

    private void zeroContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zeroContextActionPerformed
        FileDB db = (FileDB) getCurrentDB();
        int zero = 0;
        for (FileNode node : entries) {
            if (node.entry != null) {
                db.zero(node.entry);
                zero++;
            }
        }
        updateWorkspace();
        System.out.println("Successfuly zeroed " + zero + " entries.");
    }//GEN-LAST:event_zeroContextActionPerformed

    private void newFolderContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newFolderContextActionPerformed
        String folder = (String) JOptionPane.showInputDialog("Please input a name for the folder.");
        if (folder == null || folder.equals("")) return;
        
        TreePath treePath = null;
        if (lastSelected == null)
            treePath = new TreePath(getCurrentDB().addNode(folder).getPath());
        else if (lastSelected.entry == null)
            treePath = new TreePath(getCurrentDB().addNode(lastSelected.path + lastSelected.header + "/" + folder).getPath());
        
        JTree tree = getCurrentTree();
        
        FileModel m = (FileModel) tree.getModel();
        m.reload((FileNode) m.getRoot());
        
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
    }//GEN-LAST:event_newFolderContextActionPerformed

    private void generateDiffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateDiffActionPerformed
        File base = fileChooser.openFile("blurayguids.map", "map", "FileDB", false);
        if (base == null) return;
        
        FileDB baseDB = new FileDB(base);
        if (baseDB == null) {
            System.err.println("Why is the FileDB null?!");
            return;
        }
        
        File update = fileChooser.openFile("blurayguids.map", "map", "FileDB", false);
        if (update == null) return;
        
        FileDB updateDB = new FileDB(update);
        if (updateDB == null) {
            System.err.println("Why is the FileDB null?!");
            return;
        }
        
        Output output = new Output(updateDB.entryCount * 0x100);
        for (FileEntry entry : updateDB.entries) {
            FileEntry baseEntry = baseDB.find(entry.GUID);
            if (baseEntry == null)
                output.string("[+] " + entry.path + " " + Bytes.toHex(entry.size) + " " + Bytes.toHex(entry.hash) + " " + Bytes.toHex(entry.GUID) + '\n');
            else if (baseEntry.size != entry.size) {
                output.string("[U] " + entry.path + " " + Bytes.toHex(baseEntry.size) + " -> " + Bytes.toHex(entry.size) + " " + Bytes.toHex(baseEntry.hash) + " -> " + Bytes.toHex(entry.hash) + " " + Bytes.toHex(entry.GUID) +  '\n');
            }
            
        }
        output.shrinkToFit();
        
        File out = fileChooser.openFile("diff.txt", ".txt", "Text Document", true);
        if (out == null) return;
        
        FileIO.write(output.buffer, out.getAbsolutePath());
    }//GEN-LAST:event_generateDiffActionPerformed

    private void loadModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadModActionPerformed
        File file = fileChooser.openFile("example.mod", "mod", "Mod", false);
        if (file == null) return;
        
        Mod mod = loadMod(file);
        if (mod != null && mod.isParsed) {
            addTab(mod);
            updateWorkspace();
        }
    }//GEN-LAST:event_loadModActionPerformed

    private Mod loadMod(File file) {
        Mod mod;
        try {
            Data data = new Data(FileIO.read(file.getAbsolutePath()), 0xFFFFFFFF);
            data.revision = 0xFFFF;
            String password = null;
            if (data.str(0x4).equals("MODe") && data.bool() == true)
                password = JOptionPane.showInputDialog(this, "Mod is encrypted! Please input password.", "password");
            data.seek(0);
        
            mod = new Mod(file, data, password);
        } catch (Exception e) { return null; }
        return mod;
    }
    
    
    private void openModMetadataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openModMetadataActionPerformed
        new ModEditor((Mod) getCurrentDB()).setVisible(true);
    }//GEN-LAST:event_openModMetadataActionPerformed

    private void editProfileSlotsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProfileSlotsActionPerformed
        getCurrentDB().shouldSave = true;
        new SlotEditor(this, ((BigProfile)getCurrentDB()).profile, SlotEditor.EditorType.BIG_PROFILE_SLOTS, ((BigProfile)getCurrentDB()).revision).setVisible(true);
    }//GEN-LAST:event_editProfileSlotsActionPerformed

    private void newVitaDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newVitaDBActionPerformed
        newDB(936);
    }//GEN-LAST:event_newVitaDBActionPerformed

    private void newModernDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newModernDBActionPerformed
        newDB(21496064);
    }//GEN-LAST:event_newModernDBActionPerformed

    private void newLegacyDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newLegacyDBActionPerformed
        newDB(256);
    }//GEN-LAST:event_newLegacyDBActionPerformed

    private boolean confirmOverwrite(File file) {
        if (file.exists()) {
            int result = JOptionPane.showConfirmDialog(null, "This file already exists, are you sure you want to override it?", "Confirm overwrite", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) return false;
            return true;
        }
        return true;
    }
    
    
    private void createFileArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createFileArchiveActionPerformed
        File file = fileChooser.openFile("data.farc", "farc", "File Archive", true);
        if (file == null) return;
        if (confirmOverwrite(file)) {
            FileIO.write(new byte[] { 0, 0, 0, 0,0x46, 0x41, 0x52, 0x43 }, file.getAbsolutePath());
            loadFileArchive(file);
        }
    }//GEN-LAST:event_createFileArchiveActionPerformed

    private void editProfileItemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProfileItemsActionPerformed
        new MetadataEditor(this, ((BigProfile)getCurrentDB())).setVisible(true);
    }//GEN-LAST:event_editProfileItemsActionPerformed

    private void installProfileModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installProfileModActionPerformed
        File[] files = fileChooser.openFiles("mod", "File Mod");
        if (files == null) return;
        
        for (int i = 0; i < files.length; ++i) {
            File file = files[i];
            
            Mod mod = loadMod(file);
            if (mod != null) {
                
                if (isBigProfile) {
                    BigProfile profile = (BigProfile) getCurrentDB();
                    for (FileEntry entry : mod.entries)
                        profile.add(entry.data, false);
                    for (InventoryMetadata item : mod.items)
                        profile.addItem(item.resource, item);
                    for (Slot slot : mod.slots)
                        profile.addSlot(slot);
                }
                
                else if (!isBigProfile && !isMod) {
                    if (mod.entries.size() == 0) return;
                    FileDB db = (FileDB) getCurrentDB();
                    FileArchive[] archives = getSelectedArchives();
                    if (archives == null) return;
                    for (FileEntry entry : mod.entries) {
                        if (db.add(entry))
                            db.addNode(entry);
                        addFile(entry.data, archives);
                    }
                }
                
            }
        }
        
        getCurrentDB().shouldSave = true;
        updateWorkspace();
        
        JTree tree = getCurrentTree();
        TreePath[] treePath = tree.getSelectionPaths();
        
        FileModel m = (FileModel) tree.getModel();
        m.reload((FileNode) m.getRoot());
        
        tree.setSelectionPaths(treePath);
    }//GEN-LAST:event_installProfileModActionPerformed

    private void addKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addKeyActionPerformed
        String str = (String) JOptionPane.showInputDialog("Translated String");
        String hStr = (String) JOptionPane.showInputDialog("Hash");
        if (str == null || hStr == null || str.equals("") || hStr.equals(""))
            return;
        
        long hash = Long.parseLong(hStr);
        
        
        BigProfile profile = (BigProfile) getCurrentDB();
        profile.addString(str, hash);
        
        profile.shouldSave = true;
        updateWorkspace();
        
        System.out.println("Done!");
        
    }//GEN-LAST:event_addKeyActionPerformed

    private void duplicateContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateContextActionPerformed
        FileEntry entry = lastSelected.entry;
        String path = (String) JOptionPane.showInputDialog(this, "Duplicate", entry.path);
        if (path == null) return;
        
        JTree tree = getCurrentTree();

        FileDB db = (FileDB) getCurrentDB();
        FileEntry duplicate = new FileEntry(entry);
        duplicate.path = path;
        duplicate.GUID = db.getNextGUID();
        
        db.add(duplicate);
        TreePath treePath = new TreePath(db.addNode(duplicate).getPath());
        
        db.shouldSave = true;
        
        byte[] data = extractFile(entry.GUID);
        if (data != null) {
            Resource resource = new Resource(data);
            if (resource.magic.equals("PLNb")) {
                resource.getDependencies(entry, this);
                resource.removePlanDescriptors(entry.GUID, true);
                addFile(resource.data);
                duplicate.hash = Bytes.SHA1(resource.data);
            }
        }
        
        
        FileModel m = (FileModel) tree.getModel();
        m.reload((FileNode) m.getRoot());
        
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
        
        updateWorkspace();
        
        System.out.println("Duplicated entry!");
        System.out.println(entry.path + " -> " + duplicate.path);
    }//GEN-LAST:event_duplicateContextActionPerformed

    private void newItemContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newItemContextActionPerformed
        String file = JOptionPane.showInputDialog(this, "New Item", "");
        if (file == null) return;
        
        JTree tree = getCurrentTree();
        
        
        
        FileData db = getCurrentDB();
        
        FileEntry entry = new FileEntry(lastSelected.path + lastSelected.header + "/" + file, db.getNextGUID());
                
        if (isMod)
            ((Mod)db).add(entry);
        else ((FileDB)db).add(entry);
         
        db.shouldSave = true;
        
        TreePath treePath = new TreePath(db.addNode(entry).getPath());
        
        
        FileModel m = (FileModel) tree.getModel();
        m.reload((FileNode) m.getRoot());
        
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
        
        updateWorkspace();
        
        System.out.println("Added entry! -> " + entry.path);
        
    }//GEN-LAST:event_newItemContextActionPerformed

    private void replaceDecompressedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceDecompressedActionPerformed
        File file = fileChooser.openFile(lastSelected.header, "", "Resource", false);
        if (file == null) return;
        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data != null) {
            byte[] original = extractFile(lastSelected.entry.GUID);
            if (original == null) original = extractFile(lastSelected.entry.hash);
            if (original == null) { System.out.println("Couldn't find entry, can't replace."); return; }
            Resource resource = new Resource(original); resource.getDependencies(lastSelected.entry, this);
            byte[] out = Compressor.Compress(data, resource.magic, resource.revision, resource.resources);
            if (out == null) { System.err.println("Error occurred when compressing data."); return; }
            replaceEntry(lastSelected.entry, out);
            System.out.println("Data compressed and added!");
        }
    }//GEN-LAST:event_replaceDecompressedActionPerformed

    private void replaceDependenciesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceDependenciesActionPerformed
        new Dependinator(this, lastSelected.entry);
    }//GEN-LAST:event_replaceDependenciesActionPerformed

    private void encodeIntegerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encodeIntegerActionPerformed
        String number = JOptionPane.showInputDialog(this, "Integer", "");
        if (number == null) return;
        
        long integer;
        if (number.startsWith("0x"))
            integer = Long.parseLong(number.substring(2), 16);
        else if (number.startsWith("g"))
            integer = Long.parseLong(number.substring(1));
        else 
            integer = Long.parseLong(number);
        
        Output output = new Output(5, 0xFFFFFFFF);
        output.varint(integer);
        output.shrinkToFit();
        
        System.out.println("0x" + Bytes.toHex(integer) + " (" + integer + ")" + " -> " + "0x" + Bytes.toHex(output.buffer));
        
    }//GEN-LAST:event_encodeIntegerActionPerformed

    private void exportAsModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsModActionPerformed
        FileEntry entry = lastSelected.entry;
        String name = Paths.get(lastSelected.entry.path).getFileName().toString();
        if (lastSelected.entry.item != null)
            name = name.substring(0, name.length() - 5);
        else name = name.substring(0, name.length() - 4);
        
        File file = fileChooser.openFile(name + ".mod", "mod", "Mod", true);
        if (file == null) return;
        
        Resource resource = new Resource(extractFile(entry.hash));
        Mod mod = resource.hashinate(entry, this);
        
        mod.title = name;

        byte[] compressed = mod.entries.get(mod.entries.size() - 1).data;
        
        if (entry.item != null) {
            resource.setData(compressed); resource.decompress(true);
            InventoryMetadata metadata = new Serializer(resource).DeserializeItem().metadata;
            if (LAMS != null) {
                metadata.translatedLocation = LAMS.Translate(metadata.location);
                metadata.translatedCategory = LAMS.Translate(metadata.category);
            }
            if (metadata == null) {
                metadata = new InventoryMetadata();
                metadata.userCreatedDetails = new UserCreatedDetails();
                metadata.userCreatedDetails.title = name;
            }
            metadata.resource = new ResourcePtr(Bytes.SHA1(compressed), RType.PLAN);
            mod.items.add(metadata);
        } else if (lastSelected.entry.path.toLowerCase().endsWith(".bin")) {
            Slot slot = new Slot();
            slot.root = new ResourcePtr(Bytes.SHA1(compressed), RType.LEVEL);
            slot.title = name;
            mod.slots.add(slot);
        }
        
        if (file.exists()) {
            int result = JOptionPane.showConfirmDialog(null, "This mod already exists, do you want to merge them?", "Existing mod!", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                Mod oldMod = loadMod(file);
                if (oldMod != null) {
                    for (FileEntry e : oldMod.entries)
                        mod.add(e.path, e.data, e.GUID);
                    for (InventoryMetadata m : oldMod.items)
                        mod.items.add(m);
                    for (Slot slot : oldMod.slots)
                        mod.slots.add(slot);
                }
            }
            else if (result != JOptionPane.NO_OPTION) return;
        }
        
        mod.save(file.getAbsolutePath());
    }//GEN-LAST:event_exportAsModActionPerformed

    private void dumpRLSTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dumpRLSTActionPerformed
        FileDB db = (FileDB) getCurrentDB();
        String str = db.toRLST();
        
        File file = fileChooser.openFile("poppet_inventory_empty.rlst", "rlst", "RLST", true);
        if (file == null) return;
        
        FileIO.write(str.getBytes(), file.getAbsolutePath());
    }//GEN-LAST:event_dumpRLSTActionPerformed

    private void removeDependenciesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDependenciesActionPerformed
        FileEntry entry = lastSelected.entry;
        byte[] data = extractFile(entry.hash);
        
        if (data == null) return;
        
        Resource resource = new Resource(data);
        Output output = new Output(resource.length);
        
        output.bytes(resource.bytes(0x8));
        int offset = resource.int32f();
        output.int32f(offset);
        output.bytes(resource.bytes(offset - resource.offset));
        output.int32f(0); output.shrinkToFit();
        
        replaceEntry(entry, output.buffer);
    }//GEN-LAST:event_removeDependenciesActionPerformed

    private void removeMissingDependenciesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMissingDependenciesActionPerformed
        FileEntry entry = lastSelected.entry;
        byte[] data = extractFile(entry.hash);
        
        if (data == null) return;
        
        Resource resource = new Resource(data);
        resource.getDependencies(entry, this);
        resource.seek(0);
        Output output = new Output(resource.length);
        
        output.bytes(resource.bytes(0x8));
        int offset = resource.int32f();
        output.int32f(offset);
        output.bytes(resource.bytes(offset - resource.offset));

        ArrayList<ResourcePtr> dependencies = new ArrayList<ResourcePtr>(resource.dependencies.length);
        for (int i = 0; i < resource.dependencies.length; ++i) {
            if (resource.dependencies[i] != null) {
                if (extractFile(resource.dependencies[i].hash) != null)
                    dependencies.add(resource.resources[i]);
            }
        }
        
        output.int32f(dependencies.size());
        for (ResourcePtr ptr : dependencies) {
            output.resource(ptr, true);
            output.int32f(ptr.type.value);
        }
        
        output.shrinkToFit();
        
        replaceEntry(entry, output.buffer);
    }//GEN-LAST:event_removeMissingDependenciesActionPerformed

    private void exportOBJTEXCOORD1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportOBJTEXCOORD1ActionPerformed
        exportMesh(1);
    }//GEN-LAST:event_exportOBJTEXCOORD1ActionPerformed

    private void exportOBJTEXCOORD2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportOBJTEXCOORD2ActionPerformed
        exportMesh(2);
    }//GEN-LAST:event_exportOBJTEXCOORD2ActionPerformed

    private void replaceImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceImageActionPerformed
        FileEntry entry = lastSelected.entry;
        
        File file = fileChooser.openFile("image.png", "png", "Portable Network Graphics (PNG)", false);
        if (file == null) return;
        
        BufferedImage image;
        if (file.getAbsolutePath().toLowerCase().endsWith(".dds"))
            image = Images.fromDDS(FileIO.read(file.getAbsolutePath()));
        else image = FileIO.readBufferedImage(file.getAbsolutePath());
        
        if (image == null) {
            System.err.println("Image was null, cancelling replacement operation.");
            return;
        }
        
        Resource oldImage = new Resource(extractFile(entry.hash));
        
        Resource newImage = null;
        if (oldImage.type == CompressionType.GTF_TEXTURE)
            newImage = Images.toGTF(image);
        else if ((oldImage.type == null && oldImage.data == null) || oldImage.type == CompressionType.LEGACY_TEXTURE)
            newImage = Images.toTEX(image);
        
        if (newImage != null) {
            replaceEntry(entry, newImage.data);
            return;
        }
        
        System.out.println("Could not replace texture.");
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
        if (!isMod) return;
        
        Mod mod = (Mod) getCurrentDB();
        for (FileEntry entry : mod.entries) {
            if (entry.path.contains(".plan")) {
                Resource resource = new Resource(entry.data);
                resource.decompress(true);
                
                Serializer serializer = new Serializer(resource);
                InventoryItem item = serializer.DeserializeItem();
                        
                if (item != null && item.metadata != null) {
                    item.metadata.resource = new ResourcePtr(entry.hash, RType.PLAN);
                    mod.items.add(item.metadata);
                }
            }
        }
    }//GEN-LAST:event_addAllPlansToInventoryActionPerformed

    private void convertAllToGUIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertAllToGUIDActionPerformed
        if (!isMod) return;
        
        Map<String, Long> map = new HashMap<String, Long>();
        
        Mod mod = (Mod) getCurrentDB();
        
        for (FileEntry entry : mod.entries)
            map.put(Bytes.toHex(entry.hash), entry.GUID);
        
        for (FileEntry entry : mod.entries) {
            Resource resource = new Resource(entry.data);
            resource.getDependencies(entry, this);
            if (resource.resources != null) {
                resource.decompress(true);
            
                for (int i = 0; i < resource.resources.length; ++i) {
                    ResourcePtr res = resource.resources[i];
                    String SHA1 = Bytes.toHex(res.hash);
                    if (!map.containsKey(SHA1)) continue;
                    ResourcePtr newRes = new ResourcePtr(map.get(SHA1), res.type);
                    resource.replaceDependency(i, newRes, false);
                }
                
                mod.replace(entry, Compressor.Compress(resource.data, resource.magic, resource.revision, resource.resources));
            }
        }
    }//GEN-LAST:event_convertAllToGUIDActionPerformed

    private void closeTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeTabActionPerformed
        int index = fileDataTabs.getSelectedIndex();
        
        FileData data = getCurrentDB();
        if (data.shouldSave) {
            int result = JOptionPane.showConfirmDialog(null, String.format("Your %s (%s) has pending changes, do you want to save?", data.type, data.path), "Pending changes", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) data.save(data.path);
        }
        
        databases.remove(index);
        trees.remove(index);
        fileDataTabs.removeTabAt(index);
    }//GEN-LAST:event_closeTabActionPerformed

    
    private void newDB(int header) {
        Output output = new Output(0x8);
        output.int32(header); output.int32(0);
        File file = fileChooser.openFile("blurayguids.map", "map", "FileDB", true);
        if (file == null) return;
        if (confirmOverwrite(file)) {
            FileIO.write(output.buffer, file.getAbsolutePath());
            loadFileDB(file);   
        }
    }
    
    private void export(String extension) {
        File file = fileChooser.openFile(lastSelected.header.substring(0, lastSelected.header.length() - 4) + "." + extension, extension, "Image", true);
        if (file == null) return;
        
        if (lastSelected.entry.texture == null || !lastSelected.entry.texture.parsed) return;
        
        try { ImageIO.write(lastSelected.entry.texture.getImage(), extension, file); } 
        catch (IOException ex) { System.err.println("There was an error exporting the image."); return; }
        
        System.out.println("Successfully exported textures!");
    }
    
    
    private void extract(boolean decompress) {
        if (this.entries.size() == 0) { System.out.println("You need to select files to extract."); return; }
        if (this.entries.size() != 1) {
            int success = 0; int total = 0;
            String path = fileChooser.openDirectory();
            if (path == null) return;
            for (int i = 0; i < this.entries.size(); ++i) {
                FileNode node = this.entries.get(i);
                if (node.entry != null) {
                    total++;
                    byte[] data = extractFile(node.entry.hash);
                    if (data != null) {
                        Resource resource = new Resource(data);
                        if (decompress) resource.decompress(true);
                        String output = Paths.get(path, node.path, node.header).toString();
                        File file = new File(output);
                        if (file.getParentFile() != null)
                            file.getParentFile().mkdirs();
                        if (FileIO.write(resource.data, output))
                            success++;
                    }
                }
            }
            System.out.println("Finished extracting " + success + "/" + total + " entries.");
        } else {
            FileNode node = this.entries.get(0);
            if (node.entry != null) {
                byte[] data = extractFile(node.entry.hash);
                if (data != null) {
                    Resource resource = new Resource(data);
                    if (decompress) resource.decompress(true);
                    File file = fileChooser.openFile(node.header, "", "", true);
                    if (file != null)
                        if (FileIO.write(resource.data, file.getAbsolutePath()))
                            System.out.println("Successfully extracted entry!");
                        else System.err.println("Failed to extract entry.");
                } else System.err.println("Could not extract! Entry is missing data!");
            } else System.err.println("Node is missing an entry! Can't extract!");
        }
        
    }
    
    private void generateDependencyTree(FileEntry entry, FileModel model) {       
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
    
    private FileNode getLastSelected(JTree tree) {
        this.entries.clear();
        TreePath[] treePaths = tree.getSelectionPaths();
        setImage(null);
        if (treePaths == null) {
            lastSelected = null;
            return null;
        }
        for (int i = 0; i < treePaths.length; ++i) {
            FileNode node = (FileNode)treePaths[i].getLastPathComponent();
            if (node == null) { lastSelected = null; return null; };
            if (node.getChildCount() > 0)
                Nodes.loadChildren(entries, node, true);
            entries.add(node);
        }
        FileNode selected = 
            (FileNode)treePaths[treePaths.length - 1].getLastPathComponent();
        lastSelected = selected;
        return selected;
    }
    
    private void treeSelectionListener(JTree tree) {
        JTree currentTree = getCurrentTree();
        if (tree == currentTree) {
            entryModifiers.setEnabledAt(1, false);
            entryModifiers.setSelectedIndex(0);   
        }
        if (tree == currentTree)
            dependencyTree.setModel(null);
        if (tree.getSelectionPath() == null)
            return;
        
        
        FileNode selected = getLastSelected(tree);
        FileEntry entry = selected.entry;
        
        setEditorPanel(selected);
        if (selected.entry == null) {
            updateWorkspace();
            return;
        }
        
       resourceService.submit(() -> {
                if (archives.size() != 0 || isBigProfile || isMod) {
                    byte[] entryBuffer = null;
                entryBuffer = extractFile(selected.entry.hash);
                selected.entry.data = entryBuffer; updateWorkspace();
                if (entryBuffer == null) { setHexEditor(null); return; }
                setHexEditor(entryBuffer);
                if (entry.dependencyModel == null || entry.dependencies == null || entry.missingDependencies) {
                    FileModel model = new FileModel(new FileNode("x", null, null));
                    Resource resource = new Resource(entryBuffer);
                    boolean recursive = !(resource.magic.equals("PCKb") || resource.magic.equals("SLTb") || resource.magic.equals("LVLb"));
                    entry.missingDependencies = resource.getDependencies(entry, this, recursive) != 0;
                    entry.dependencies = resource.dependencies;
                    generateDependencyTree(entry, model);
                    entry.dependencyModel = model;
                }
                
                if (lastSelected == selected && entry.dependencyModel != null && tree == currentTree)
                    dependencyTree.setModel(entry.dependencyModel);
                String path = selected.entry.path.toLowerCase();
            
                String ext = path.substring(path.lastIndexOf(".") + 1);
            
                preview.setDividerLocation(325);
                switch (ext) {
                    case "pck": 
                        if (selected.entry.pack == null) {
                            Resource res = new Resource(entryBuffer);
                            res.decompress(true);
                            selected.entry.revision = res.revision;
                            try {
                                Pack pack = new Pack(res);
                                selected.entry.pack = pack;
                            } catch (Exception e) {
                                System.err.println("There was an error processing the RPack file! -> ");
                                System.err.println(e);
                            }
                        }
                        break;
                    case "slt":
                            if (selected.entry.slots == null) {
                                Resource res = new Resource(entryBuffer);
                                if (res.magic.equals("SLTt")) return;
                                res.decompress(true);
                                selected.entry.revision = res.revision;
                                
                                int count = res.int32();
                                selected.entry.slots = new ArrayList<Slot>(count);
                                for (int i = 0; i < count; ++i) 
                                    selected.entry.slots.add(new Slot(res, true, false));
                            }
                            break;
                    case "bin":
                        if (selected.entry.slot == null) break;
                        if (selected.entry.slot.renderedIcon == null) {
                            setImage(null); break;
                        }
                        setImage(selected.entry.slot.renderedIcon);
                        break;
                    case "tex": case "gtf": case "dds": case "jpg": case "jpeg": case "png": case "jfif":
                        if (selected.entry.texture == null)
                            selected.entry.texture = new Texture(entryBuffer);
                        ImageIcon icon = selected.entry.texture.getImageIcon(320, 320);
                        if (icon != null) setImage(icon);
                        else System.out.println("Failed to set icon, it's null?");
                        break;
                    case "mol": 
                        if (selected.entry.mesh == null)
                            selected.entry.mesh = new Mesh(entryBuffer);
                        System.out.println("Failed to set Mesh preview, does functionality even exist?");
                        break;
                    case "anim":
                        Animation animation = new Animation(entryBuffer);
                        break; 
                    case "plan":
                        if (selected.entry.item == null) {
                            try {
                                Resource resource = new Resource(entryBuffer); resource.decompress(true);
                                selected.entry.item = new Serializer(resource, LAMS).DeserializeItem();   
                            } catch (Exception e) { System.err.println("There was an error parsing the InventoryItem!"); return; }
                        }
                        if (lastSelected.entry == selected.entry) {
                            if (selected.entry.item != null && tree == currentTree) {
                                if (selected.entry.item.metadata != null)
                                    populateMetadata(selected.entry.item);
                                else {
                                    System.out.println("Attempting to guess icon of RPlan, this may not be accurate.");
                                    try {
                                        for (FileEntry e : selected.entry.dependencies) {
                                            if (e.path.contains(".tex")) {
                                                ResourcePtr ptr = new ResourcePtr();
                                                ptr.hash = e.hash;
                                                loadImage(ptr, selected.entry.item);
                                                return;
                                            }
                                        }
                                    } catch (Exception e) { System.err.println("An error occured procesing texture."); }
                                    System.out.println("Could not find any texture file to display as icon.");
                                }
                            }
                        }
                        break;
                }
                preview.setDividerLocation(325);
            }
        });
    }
    
    private void populateMetadata(InventoryItem item) {
        if (item == null || (archives.size() == 0 && !isBigProfile && !isMod)) return;
        InventoryMetadata metadata = item.metadata;
        if (metadata == null) return;
        
        iconField.setText("");
        if (metadata.icon != null && (metadata.icon.hash != null || metadata.icon.GUID != -1))
            loadImage(metadata.icon, item);
        
        if (lastSelected.entry.item != item) return;
        
        setPlanDescriptions(metadata);
        
        pageCombo.setSelectedItem(metadata.type);
        subCombo.setSelectedItem(metadata.subType);
        creatorField.setText(metadata.creator.PSID);
        
        entryModifiers.setEnabledAt(1, true);
        entryModifiers.setSelectedIndex(1);
    }
    
    private void setPlanDescriptions(InventoryMetadata metadata) {
        
        if (lastSelected.entry.item.revision < 0x272) {
            titleField.setText(metadata.translationKey);
            descriptionField.setText("");
            categoryField.setText(metadata.legacyCategoryKey);
            locationField.setText(metadata.legacyLocationKey);
            
            LAMSMetadata.setEnabled(false);
            LAMSMetadata.setSelected(false); StringMetadata.setEnabled(true);
            StringMetadata.setSelected(true);
            return;
        }
        
        titleField.setText("" + metadata.titleKey);
        descriptionField.setText("" + metadata.descriptionKey);
        
        locationField.setText("" + metadata.location);
        categoryField.setText("" + metadata.category);
        
        if (LAMS != null) {
            StringMetadata.setEnabled(true);
            StringMetadata.setSelected(true);
            
            titleField.setText(LAMS.Translate(metadata.titleKey));
            
            descriptionField.setText(LAMS.Translate(metadata.descriptionKey));
            
            metadata.translatedLocation = LAMS.Translate(metadata.location);
            metadata.translatedCategory = LAMS.Translate(metadata.category);
            locationField.setText(metadata.translatedLocation);
            categoryField.setText(metadata.translatedCategory);
        } else { LAMSMetadata.setSelected(true); LAMSMetadata.setEnabled(true); StringMetadata.setEnabled(false); }
        
        
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
    
    private void loadImage(ResourcePtr resource, InventoryItem item) {
        FileEntry entry = null;
            byte[] hash = null;
            if (resource == null) return;
            iconField.setText(resource.toString());
            if (resource.hash != null)
                hash = resource.hash; 
            else {
                entry = findEntry(resource.GUID);
                if (entry != null)
                    hash = entry.hash;
            }
            
            if (hash == null) return;
            if (entry == null) entry = findEntry(hash);
            
            if (entry != null && entry.texture != null) 
                setImage(entry.texture.getImageIcon(320, 320));
            else {
                byte[] data = extractFile(hash);
                if (data == null) return;
                Texture texture = new Texture(data);
                if (entry != null) entry.texture = texture;
                if (texture.parsed == true)
                    if (lastSelected.entry.item == item)
                        setImage(texture.getImageIcon(320, 320));
            }
    }
    
    private void setImage(ImageIcon image) {
        preview.setDividerLocation(325);
        if (image == null) {
            texture.setText("No preview to be displayed");
            texture.setIcon(null);
        } else {
            texture.setText(null);
            texture.setIcon(image);
        }
        preview.setDividerLocation(325);
    }
    
    private void setEditorPanel(FileNode node) {
        FileEntry entry = node.entry;
        if (entry == null) {
            entryTable.setValueAt(node.path + node.header, 0, 1);
            for (int i = 1; i < 7; ++i)
                entryTable.setValueAt("N/A", i, 1);
            return;
        }
        
        entryTable.setValueAt(entry.path, 0, 1);
        
        if (entry.timestamp != 0) {
            Timestamp timestamp = new Timestamp(entry.timestamp * 1000L);
            entryTable.setValueAt(timestamp.toString(), 1, 1);   
        } else entryTable.setValueAt("N/A", 1, 1);
        entryTable.setValueAt(Bytes.toHex(entry.hash), 2, 1);
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
    }
    
    private void setHexEditor(byte[] bytes) {
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
    
    public static void main(String args[]) {
        try { UIManager.setLookAndFeel(new DarculaLaf()); } 
        catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Toolkit.class.getName()).log(Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Toolkit().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu FARMenu;
    private javax.swing.JRadioButton LAMSMetadata;
    private javax.swing.JMenu MAPMenu;
    private javax.swing.JMenu ProfileMenu;
    private javax.swing.JRadioButton StringMetadata;
    private javax.swing.JMenuItem addAllPlansToInventory;
    private javax.swing.JMenuItem addFile;
    private javax.swing.JMenuItem addKey;
    private javax.swing.JTextField categoryField;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JMenuItem clear;
    private javax.swing.JMenuItem closeTab;
    private javax.swing.JTextArea console;
    private javax.swing.JScrollPane consoleContainer;
    private javax.swing.JPopupMenu consolePopup;
    private javax.swing.JMenuItem convertAllToGUID;
    private javax.swing.JMenuItem createFileArchive;
    private javax.swing.JTextField creatorField;
    private javax.swing.JLabel creatorLabel;
    private javax.swing.JMenu debugMenu;
    private javax.swing.JMenuItem decompressResource;
    private javax.swing.JMenuItem deleteContext;
    private javax.swing.JTree dependencyTree;
    private javax.swing.JScrollPane dependencyTreeContainer;
    private javax.swing.JTextArea descriptionField;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JSplitPane details;
    private javax.swing.JMenuItem dumpBPRToMod;
    private javax.swing.JMenuItem dumpHashes;
    private javax.swing.JMenuItem dumpRLST;
    private javax.swing.JPopupMenu.Separator dumpSep;
    private javax.swing.JMenuItem duplicateContext;
    private javax.swing.JMenuItem editProfileItems;
    private javax.swing.JMenuItem editProfileSlots;
    private javax.swing.JMenuItem editSlotContext;
    private javax.swing.JMenuItem encodeInteger;
    private javax.swing.JPopupMenu entryContext;
    private javax.swing.JSplitPane entryData;
    private javax.swing.JTabbedPane entryModifiers;
    private javax.swing.JTable entryTable;
    private javax.swing.JMenuItem exportAsMod;
    private javax.swing.JMenuItem exportDDS;
    private javax.swing.JMenuItem exportLAMSContext;
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
    private javax.swing.JTabbedPane fileDataTabs;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu gamedataMenu;
    private javax.swing.JMenuItem generateDiff;
    private tv.porst.jhexview.JHexView hex;
    private javax.swing.JTextField iconField;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JMenuItem installProfileMod;
    private javax.swing.JPanel itemMetadata;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JMenuItem loadArchive;
    private javax.swing.JMenuItem loadBigProfile;
    private javax.swing.JMenuItem loadDB;
    private javax.swing.JMenu loadGroupMenu;
    private javax.swing.JMenuItem loadLAMSContext;
    private javax.swing.JMenuItem loadMod;
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JMenu menuFileMenu;
    private javax.swing.JMenuItem mergeFARCs;
    private javax.swing.ButtonGroup metadataButtonGroup;
    private javax.swing.JMenu modMenu;
    private javax.swing.JMenu newFileDBGroup;
    private javax.swing.JMenuItem newFolderContext;
    private javax.swing.JMenu newGamedataGroup;
    private javax.swing.JMenuItem newItemContext;
    private javax.swing.JMenuItem newLegacyDB;
    private javax.swing.JMenuItem newModernDB;
    private javax.swing.JMenuItem newVitaDB;
    private javax.swing.JMenuItem openCompressinator;
    private javax.swing.JMenuItem openModMetadata;
    private javax.swing.JComboBox<String> pageCombo;
    private javax.swing.JMenuItem patchMAP;
    private javax.swing.JSplitPane preview;
    private javax.swing.JSplitPane previewContainer;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem reboot;
    private javax.swing.JMenuItem removeDependencies;
    private javax.swing.JMenuItem removeMissingDependencies;
    private javax.swing.JMenuItem replaceCompressed;
    private javax.swing.JMenu replaceContext;
    private javax.swing.JMenuItem replaceDecompressed;
    private javax.swing.JMenuItem replaceDependencies;
    private javax.swing.JMenuItem replaceImage;
    private javax.swing.JMenuItem saveAs;
    private javax.swing.JPopupMenu.Separator saveDivider;
    private javax.swing.JMenuItem saveMenu;
    private javax.swing.JMenu savedataMenu;
    private javax.swing.JMenuItem scanRawData;
    private javax.swing.JTextField search;
    private javax.swing.JComboBox<String> subCombo;
    private javax.swing.JScrollPane tableContainer;
    private javax.swing.JLabel texture;
    private javax.swing.JTextField titleField;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JMenuBar toolkitMenu;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JSplitPane treeContainer;
    private javax.swing.JSplitPane workspaceDivider;
    private javax.swing.JMenuItem zeroContext;
    // End of variables declaration//GEN-END:variables
}
