package toolkit.windows;

import configurations.ApplicationFlags;
import configurations.Config;
import configurations.Profile;
import cwlib.enums.*;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.imports.AnimationImporter;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.resources.*;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.inventory.UserCreatedDetails;
import cwlib.structs.profile.InventoryItem;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PStickers;
import cwlib.structs.things.parts.PWorld;
import cwlib.types.SerializedResource;
import cwlib.types.archives.Fart;
import cwlib.types.archives.SaveArchive;
import cwlib.types.data.*;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.types.databases.FileEntry;
import cwlib.types.databases.RemapDB;
import cwlib.types.databases.RemapDB.RemapDBRow;
import cwlib.types.mods.Mod;
import cwlib.types.save.BigSave;
import cwlib.types.save.SaveEntry;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileModel;
import cwlib.types.swing.FileNode;
import cwlib.types.swing.SearchParameters;
import cwlib.util.*;
import executables.gfx.GfxGUI;
import toolkit.functions.*;
import toolkit.streams.CustomPrintStream;
import toolkit.streams.TextAreaOutputStream;
import toolkit.utilities.EasterEgg;
import toolkit.utilities.FileChooser;
import toolkit.utilities.Swing;
import toolkit.utilities.TreeSelectionListener;
import toolkit.windows.bundlers.TextureImporter;
import toolkit.windows.managers.*;
import toolkit.windows.utilities.ArchiveSelector;
import toolkit.windows.utilities.AssetExporter;
import toolkit.windows.utilities.Compressinator;
import toolkit.windows.utilities.Dependinator;
import tv.porst.jhexview.JHexView;
import tv.porst.jhexview.SimpleDataProvider;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Toolkit extends javax.swing.JFrame
{
    public static Toolkit INSTANCE;
    private boolean isTreeRowSelected = false;

    public Toolkit()
    {
        /* Reset the state in case of a reboot. */
        ResourceSystem.reset();
        Toolkit.INSTANCE = this;

        ResourceSystem.TriggerWorkSpaceUpdate = () -> updateWorkspace();
        ResourceSystem.GetSelectedCaches = () -> getSelectedArchives();
        ResourceSystem.TreeSelectionListener = (tree) -> TreeSelectionListener.listener(tree);

        this.initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());

        EasterEgg.initialize(this);
        this.disable3DView();

        this.entryTable.getActionMap().put("copy", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                String copied = "";
                for (int i = 0; i < entryTable.getSelectedRowCount(); ++i)
                {
                    copied += String.valueOf(entryTable.getModel().getValueAt(entryTable.getSelectedRows()[i], 1));
                    if (i + 1 != entryTable.getSelectedRowCount())
                        copied += '\n';
                }
                StringSelection selection = new StringSelection(copied);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection,
                    selection);
            }

        });

        this.progressBar.setVisible(false);
        this.fileDataTabs.addChangeListener(l ->
        {
            FileData database =
                ResourceSystem.setSelectedDatabase(this.fileDataTabs.getSelectedIndex());
            if (database == null)
            {
                this.search.setEnabled(false);
                this.search.setForeground(Color.GRAY);
                this.search.setText("Search is currently disabled.");

                this.updateWorkspace();

                return;
            }

            this.search.setEnabled(true);
            this.search.setText(database.getLastSearch());

            if (this.search.getText().equals("Search..."))
                search.setForeground(Color.GRAY);
            else
                this.search.setForeground(Color.WHITE);

            this.updateWorkspace();
        });

        /* Disable tabs since nothing is selected yet */
        this.entryModifiers.setEnabledAt(1, false);
        this.StringMetadata.setEnabled(false);
        this.updateWorkspace();

        this.search.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                if (search.getText().equals("Search..."))
                {
                    search.setText("");
                    search.setForeground(Color.WHITE);
                }
            }

            public void focusLost(FocusEvent e)
            {
                if (search.getText().isEmpty())
                {
                    search.setText("Search...");
                    search.setForeground(Color.GRAY);
                }
            }
        });

        this.dependencyTree.addMouseListener(showContextMenu);
        this.dependencyTree.addTreeSelectionListener(e -> TreeSelectionListener.listener(dependencyTree));

        /*
         * Don't let the user close the program without
         * confirming if they want to save or discard changes.
         */
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                checkForChanges();
            }
        });

        /* Auto-load configurations from whatever profile is currently enabled. */

        Profile profile = Config.instance.getCurrentProfile();
        if (profile == null)
            return;

        if (profile.archives != null)
        {
            for (String path : profile.archives)
            {
                if (Files.exists(Paths.get(path)))
                    ArchiveCallbacks.loadFileArchive(new File(path));
            }
        }

        if (profile.databases != null)
        {
            for (String path : profile.databases)
            {
                if (Files.exists(Paths.get(path)))
                    DatabaseCallbacks.loadFileDB(new File(path));
            }
        }

        if (profile.saves != null)
        {
            Pattern bigRegex = Pattern.compile("bigfart\\d+");
            for (String path : profile.saves)
            {
                File folder = new File(path);
                if (!Files.exists(Paths.get(path)))
                    continue;
                File[] profiles =
                    folder.listFiles((dir, name) -> bigRegex.matcher(name).matches());
                for (File file : profiles)
                    ProfileCallbacks.loadProfile(file);
            }
        }

        PrintStream out = new CustomPrintStream(new TextAreaOutputStream(console));
        System.setOut(out);
        System.setErr(out);
    }

    private void disable3DView()
    {
        this.renderPane.removeTabAt(1);
        this.resourceTabs.removeTabAt(1);

        // Disable 3D specific tools
        this.exportWorld.setVisible(false);
        this.exportSceneGraph.setVisible(false);
    }

    private final MouseListener showContextMenu = new MouseAdapter()
    {
        public void mousePressed(MouseEvent e)
        {
            if (SwingUtilities.isRightMouseButton(e))
            {
                JTree tree = (JTree) e.getComponent();
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selPath != null)
                {
                    isTreeRowSelected = true;
                    tree.setSelectionPath(selPath);
                }
                else
                {
                    isTreeRowSelected = false;
                    tree.setSelectionPath(null);
                    ResourceSystem.resetSelections();
                }
                if (selRow > -1)
                {
                    tree.setSelectionRow(selRow);
                    isTreeRowSelected = true;
                }
                else
                    isTreeRowSelected = false;
                ResourceSystem.updateSelections(tree);
                generateEntryContext2(tree);
                if (entryContext.getComponentCount() != 0)
                    entryContext.show(tree, e.getX(), e.getY());
            }
        }
    };

    private void checkForChanges()
    {
        for (FileData data : ResourceSystem.getDatabases())
        {
            if (data.hasChanges())
            {
                int result = JOptionPane.showConfirmDialog(null,
                    String.format("Your %s (%s) has pending changes, do you want to " +
                                  "save?",
                        data.getType().getName(), data.getFile().getAbsolutePath()),
                    "Pending changes", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION)
                    data.save();
            }
        }

        for (Fart archive : ResourceSystem.getArchives())
        {
            if (archive.shouldSave())
            {
                int result = JOptionPane
                    .showConfirmDialog(null,
                        String.format("Your FileArchive (%s) has pending changes," +
                                      " do you " +
                                      "want to save?",
                            archive.getFile().getAbsolutePath()),
                        "Pending changes", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION)
                    archive.save();
            }
        }
    }

    public void updateWorkspace()
    {
        closeTab.setVisible(fileDataTabs.getTabCount() != 0);
        installProfileMod.setVisible(false);
        int archiveCount = ResourceSystem.getArchives().size();

        // Set save status for all databases
        ArrayList<FileData> databases = ResourceSystem.getDatabases();
        for (int i = 0; i < databases.size(); ++i)
        {
            FileData database = databases.get(i);
            if (database.hasChanges())
                fileDataTabs.setTitleAt(i, database.getName() + " *");
            else
                fileDataTabs.setTitleAt(i, database.getName());
        }

        FileData database = ResourceSystem.getSelectedDatabase();
        if (database != null)
        {
            editMenu.setVisible(true);
            saveMenu.setEnabled(database.hasChanges());
        }
        else
            editMenu.setVisible(false);

        if (archiveCount != 0 || database != null)
        {
            saveDivider.setVisible(true);
            saveMenu.setVisible(true);
        }
        else
        {
            saveDivider.setVisible(false);
            saveMenu.setVisible(false);
        }

        addFolder.setVisible(false);

        if (ResourceSystem.getDatabaseType() == DatabaseType.NONE && ResourceSystem.getArchives().size() != 0)
        {
            archiveMenu.setVisible(true);
            addFolder.setVisible(true);
        }

        else if (ResourceSystem.canExtract() && ResourceSystem.getDatabaseType() != DatabaseType.MOD)
        {
            archiveMenu.setVisible(true);
            addFolder.setVisible(ResourceSystem.getDatabaseType() == DatabaseType.FILE_DATABASE);
        }
        else
            archiveMenu.setVisible(false);

        if (ResourceSystem.getDatabaseType() != DatabaseType.NONE)
        {
            saveDivider.setVisible(true);
            saveAs.setVisible(true);
        }
        else
        {
            saveDivider.setVisible(false);
            saveAs.setVisible(false);
        }

        databaseMenu.setVisible(ResourceSystem.getDatabaseType() == DatabaseType.FILE_DATABASE);
        if (ResourceSystem.getDatabaseType() == DatabaseType.FILE_DATABASE)
        {
            if (archiveCount != 0)
                installProfileMod.setVisible(true);
            dumpRLST.setVisible(true);
        }

        saveDivider.setVisible(database != null);

        if (ResourceSystem.getDatabaseType() == DatabaseType.BIGFART)
        {
            profileMenu.setVisible(true);
            installProfileMod.setVisible(true);
        }
        else
            profileMenu.setVisible(false);

        modMenu.setVisible(ResourceSystem.getDatabaseType() == DatabaseType.MOD);
    }

    public void generateEntryContext2(JTree tree)
    {
        this.entryContext.removeAll();
        this.exportGroup.removeAll();
        this.copyGroup.removeAll();

        boolean isDependencyTree = tree == this.dependencyTree;

        if (!isTreeRowSelected && isDependencyTree)
            return;

        FileNode node = ResourceSystem.getSelected();
        FileEntry entry = node == null ? null : node.getEntry();
        ResourceInfo info = entry == null ? null : entry.getInfo();
        ResourceType type = info == null ? ResourceType.INVALID : info.getType();

        boolean canExtract = ResourceSystem.canExtract() && ResourceSystem.canExtractSelected();
        boolean isFolder = (isTreeRowSelected && entry == null);
        boolean isFile = (isTreeRowSelected && entry != null);
        boolean isLoadedResource = isFile && (info != null && (info.getResource() != null));
        boolean isCompressed = info != null && info.isCompressedResource();

        int contextSize = 0;

        if (!isDependencyTree && isLoadedResource)
        {
            switch (type)
            {
                case TRANSLATION:
                {
                    Swing.createMenuItem(
                        "Load",
                        "Uses this translation table to translate LAMS keys in " +
                        "other " +
                        "resources in Toolkit, such as in plans and slots",
                        LoadCallbacks::loadTranslationTable,
                        this.entryContext);
                    break;
                }
                case PLAN:
                {
                    Swing.createMenuItem(
                        "Edit Item Details",
                        "Edit the properties of this item",
                        EditCallbacks::editItem,
                        this.entryContext);

                    if (ApplicationFlags.ALEAR_INTEGRATION)
                    {
                        Swing.createMenuItem(
                            "Upload",
                            "Sends the item to the local Alear server",
                            AlearCallbacks::upload,
                            this.entryContext);
                    }

                    if (ApplicationFlags.CAN_USE_3D)
                    {
                        try
                        {
                            boolean enableDecalExport = false;
                            RPlan plan = info.getResource();
                            if (plan != null)
                            {
                                Thing[] things = plan.getThings();
                                for (Thing thing : things)
                                {
                                    if (thing == null)
                                        continue;
                                    if (thing.hasPart(Part.STICKERS))
                                    {
                                        enableDecalExport = true;
                                        break;
                                    }
                                }
                            }

                            if (enableDecalExport)
                            {
                                Swing.createMenuItem(
                                    "Export Decal Maps",
                                    "Exports all decal maps in the item",
                                    ExportCallbacks::exportDecalMaps,
                                    this.entryContext);
                            }
                        }
                        catch (Exception ex)
                        {
                            System.out.println(
                                "An error occurred while checking the resource " +
                                "for decals. " +
                                "Exporting will be disabled.");
                        }
                    }

                    break;
                }
                case SLOT_LIST:
                case PACKS:
                {
                    String tooltip = "Add and edit levels in this slot list";
                    if (type == ResourceType.PACKS)
                        tooltip = "Add and edit DLC packs";

                    Swing.createMenuItem(
                        "Edit Slots",
                        tooltip,
                        EditCallbacks::editSlot,
                        this.entryContext);
                    break;
                }
                case PALETTE:
                {
                    if (Config.instance.enable3D)
                    {
                        Swing.createMenuItem(
                            "Load",
                            "Loads this palette into the 3D viewer",
                            LoadCallbacks::loadPalette3D,
                            this.entryContext);
                    }
                    break;
                }
                case LEVEL:
                {
                    if (Config.instance.enable3D)
                    {
                        Swing.createMenuItem(
                            "Load",
                            "Loads this level into the 3D viewer",
                            LoadCallbacks::loadLevel3D,
                            this.entryContext);
                    }
                    break;
                }
                case MESH:
                {
                    if (Config.instance.enable3D)
                    {
                        Swing.createMenuItem(
                            "Load",
                            "Load this model into the 3D viewer at origin",
                            LoadCallbacks::loadModel3D,
                            this.entryContext);
                    }
                    break;
                }
            }
        }

        if (contextSize != this.entryContext.getComponentCount())
        {
            this.entryContext.add(new JSeparator());
            contextSize = this.entryContext.getComponentCount();
        }

        if (canExtract && isTreeRowSelected)
        {
            this.entryContext.add(this.extractGroup);

            // Maybe I should check if at least one resource is compressed?
            this.extractDecompressedContext.setVisible(
                !isFile || (isCompressed && type != ResourceType.STATIC_MESH && type != ResourceType.FONTFACE));

            if (isFile && info != null)
            {
                switch (type)
                {
                    case FONTFACE:
                    {
                        Swing.createMenuItem(
                            "PNG",
                            "Export all font glyphs as a PNG",
                            ExportCallbacks::exportFont,
                            this.exportGroup);
                        break;
                    }
                    case TEXTURE:
                    case GTF_TEXTURE:
                    {
                        this.exportGroup.add(this.exportTextureGroupContext);
                        break;
                    }
                    case STATIC_MESH:
                    {
                        this.exportGroup.add(this.exportModelGroup);
                        this.exportOBJ.setVisible(false);
                        break;
                    }
                    case MESH:
                    {
                        this.exportGroup.add(this.exportModelGroup);

                        RMesh mesh = info.getResource();
                        int count = mesh.getAttributeCount();
                        this.exportOBJ.setVisible(count != 0);
                        this.exportOBJTEXCOORD0.setVisible((count > 0));
                        this.exportOBJTEXCOORD1.setVisible((count > 1));
                        this.exportOBJTEXCOORD2.setVisible((count > 2));

                        break;
                    }
                    case ANIMATION:
                    {
                        this.exportGroup.add(this.exportAnimation);
                        break;
                    }
                    case PALETTE:
                    {
                        this.exportGroup.add(this.exportPaletteContext);
                        break;
                    }
                    case LEVEL:
                    case PLAN:
                    {
                        this.exportGroup.add(this.exportBackupGroup);
                        break;
                    }
                    default:
                        break;
                }

                if (info != null && info.getDependencies().length != 0)
                    this.exportGroup.add(this.exportModGroup);
            }

            boolean canExportJSON = type == ResourceType.TRANSLATION
                                    || (isLoadedResource && info.getMethod().equals(SerializationType.BINARY));
            if (canExportJSON)
            {
                this.exportGroup.add(this.exportJSONContext);
            }

            if (this.exportGroup.getMenuComponentCount() != 0)
                this.entryContext.add(this.exportGroup);
        }

        if (contextSize != this.entryContext.getComponentCount())
        {
            this.entryContext.add(new JSeparator());
            contextSize = this.entryContext.getComponentCount();
        }

        if ((ResourceSystem.getDatabaseType().containsData() || ResourceSystem.getDatabases().size() != 0)
            && !isDependencyTree)
        {
            if (!isFile)
                this.entryContext.add(this.newEntryGroup);

            boolean canAddItems =
                ResourceSystem.getSelectedDatabase().getType().containsData();
            if (!canAddItems)
                canAddItems = ResourceSystem.getArchives().size() != 0;

            this.newResourceGroup.setVisible(canAddItems);
            this.newItemGroup.setVisible(canAddItems);

            if (ResourceSystem.getDatabaseType().hasGUIDs())
            {
                if (!isTreeRowSelected || isFolder)
                    this.entryContext.add(this.newFolderContext);
                if (isFolder)
                    this.entryContext.add(this.renameFolder);
            }
        }

        if (contextSize != this.entryContext.getComponentCount())
        {
            this.entryContext.add(new JSeparator());
            contextSize = this.entryContext.getComponentCount();
        }

        if (isFile && !isDependencyTree)
        {
            if (ResourceSystem.getDatabaseType().hasGUIDs())
                this.entryContext.add(editGroup);
            if (isFile && ResourceSystem.canExtract())
            {
                boolean canChangeRevision = type != ResourceType.GFX_MATERIAL
                                            && (isLoadedResource && info.getMethod().equals(SerializationType.BINARY));
                if (canChangeRevision)
                    this.entryContext.add(this.changeResourceRevisionGroup);


                this.entryContext.add(this.replaceGroup);
                this.replaceImageContext.setVisible(type == ResourceType.TEXTURE || type == ResourceType.GTF_TEXTURE);
                this.replaceDecompressedContext.setVisible(isCompressed && type != ResourceType.STATIC_MESH);
                this.replaceJSONContext.setVisible(type == ResourceType.TRANSLATION || (type != ResourceType.INVALID
                                                                                        && info.getMethod() == SerializationType.BINARY && info.getResource() != null));
                boolean hasDependencies =
                    isCompressed && info.getDependencies().length != 0;
                this.replaceDependenciesContext.setVisible(hasDependencies);
                if (hasDependencies)
                    this.entryContext.add(this.dependencyGroup);
            }
        }

        if (contextSize != this.entryContext.getComponentCount())
        {
            this.entryContext.add(new JSeparator());
            contextSize = this.entryContext.getComponentCount();
        }

        if (isFile && ResourceSystem.getSelectedDatabase().getType().hasGUIDs())
            this.entryContext.add(this.duplicateContext);

        FileData source = ResourceSystem.getSelectedDatabase();
        if (isTreeRowSelected && source.getType().hasGUIDs())
        {
            for (FileData data : ResourceSystem.getDatabases())
            {
                if (data == source || !data.getType().hasGUIDs())
                    continue;
                Swing.createMenuItem(data.getName(), (e) ->
                {
                    DatabaseCallbacks.copyItems((FileDB) data);
                }, this.copyGroup);
            }

            if (this.copyGroup.getMenuComponentCount() != 0)
                this.entryContext.add(this.copyGroup);
        }

        if (contextSize != this.entryContext.getComponentCount())
        {
            this.entryContext.add(new JSeparator());
            contextSize = this.entryContext.getComponentCount();
        }

        if (isTreeRowSelected && !isDependencyTree)
        {
            if (ResourceSystem.getSelectedDatabase().getType().hasGUIDs())
                this.entryContext.add(this.zeroContext);
            this.entryContext.add(this.deleteContext);
        }

        // Remove any extra separators if they exist.
        while (true)
        {
            int count = this.entryContext.getComponentCount();
            if (count == 0)
                break;
            Component component = this.entryContext.getComponent(count - 1);
            if (component instanceof JSeparator)
            {
                this.entryContext.remove(component);
                continue;
            }
            break;
        }

        if (this.entryContext.isVisible())
        {
            this.entryContext.pack();
            this.entryContext.repaint();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        entryContext = new javax.swing.JPopupMenu();
        changeResourceRevisionGroup = new javax.swing.JMenu();
        changeResourceRevisionLBP1Context = new javax.swing.JMenuItem();
        changeResourceRevisionLBP2Context = new javax.swing.JMenuItem();
        changeResourceRevisionLBP3Context = new javax.swing.JMenuItem();
        extractGroup = new javax.swing.JMenu();
        extractContext = new javax.swing.JMenuItem();
        extractDecompressedContext = new javax.swing.JMenuItem();
        editGroup = new javax.swing.JMenu();
        editPathContext = new javax.swing.JMenuItem();
        editHashContext = new javax.swing.JMenuItem();
        editGUIDContext = new javax.swing.JMenuItem();
        exportGroup = new javax.swing.JMenu();
        exportJSONContext = new javax.swing.JMenuItem();
        exportTextureGroupContext = new javax.swing.JMenu();
        exportPNG = new javax.swing.JMenuItem();
        exportDDS = new javax.swing.JMenuItem();
        exportModelGroup = new javax.swing.JMenu();
        exportOBJ = new javax.swing.JMenu();
        exportOBJTEXCOORD0 = new javax.swing.JMenuItem();
        exportOBJTEXCOORD1 = new javax.swing.JMenuItem();
        exportOBJTEXCOORD2 = new javax.swing.JMenuItem();
        exportGLTF = new javax.swing.JMenuItem();
        exportModGroup = new javax.swing.JMenu();
        exportAsModCustom = new javax.swing.JMenuItem();
        exportAsMod = new javax.swing.JMenuItem();
        exportAsModGUID = new javax.swing.JMenuItem();
        exportAnimation = new javax.swing.JMenuItem();
        exportBackupGroup = new javax.swing.JMenu();
        exportAsBackup = new javax.swing.JMenuItem();
        exportAsBackupGUID = new javax.swing.JMenuItem();
        exportPaletteContext = new javax.swing.JMenuItem();
        newEntryGroup = new javax.swing.JMenu();
        newResourceGroup = new javax.swing.JMenu();
        importJSONContext = new javax.swing.JMenuItem();
        newTextureContext = new javax.swing.JMenuItem();
        newAnimationContext = new javax.swing.JMenuItem();
        newModelContext = new javax.swing.JMenuItem();
        newItemGroup = new javax.swing.JMenu();
        newStickerContext = new javax.swing.JMenuItem();
        newEntryContext = new javax.swing.JMenuItem();
        newFolderContext = new javax.swing.JMenuItem();
        renameFolder = new javax.swing.JMenuItem();
        replaceGroup = new javax.swing.JMenu();
        replaceCompressedContext = new javax.swing.JMenuItem();
        replaceDecompressedContext = new javax.swing.JMenuItem();
        replaceDependenciesContext = new javax.swing.JMenuItem();
        replaceImageContext = new javax.swing.JMenuItem();
        replaceJSONContext = new javax.swing.JMenuItem();
        dependencyGroup = new javax.swing.JMenu();
        removeDependenciesContext = new javax.swing.JMenuItem();
        removeMissingDependenciesContext = new javax.swing.JMenuItem();
        duplicateContext = new javax.swing.JMenuItem();
        copyGroup = new javax.swing.JMenu();
        zeroContext = new javax.swing.JMenuItem();
        deleteContext = new javax.swing.JMenuItem();
        consolePopup = new javax.swing.JPopupMenu();
        clear = new javax.swing.JMenuItem();
        metadataButtonGroup = new javax.swing.ButtonGroup();
        workspace = new javax.swing.JSplitPane();
        details = new javax.swing.JSplitPane();
        previewContainer = new javax.swing.JSplitPane();
        consoleContainer = new javax.swing.JScrollPane();
        console = new javax.swing.JTextArea();
        renderPane = new javax.swing.JTabbedPane();
        overviewPane = new javax.swing.JSplitPane();
        texture = new javax.swing.JLabel();
        hex = new tv.porst.jhexview.JHexView();
        scenePanel = new javax.swing.JPanel();
        infoCardPanel = new javax.swing.JPanel();
        fileDataPane = new javax.swing.JSplitPane();
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
        pageCombo = new javax.swing.JComboBox(InventoryObjectType.values());
        creatorLabel = new javax.swing.JLabel();
        creatorField = new javax.swing.JTextField();
        subCombo = new javax.swing.JTextField();
        inspectorPane = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cameraPosX = new javax.swing.JSpinner();
        cameraPosY = new javax.swing.JSpinner();
        cameraPosZ = new javax.swing.JSpinner();
        resourceTabs = new javax.swing.JTabbedPane();
        treeContainer = new javax.swing.JSplitPane();
        search = new javax.swing.JTextField();
        fileDataTabs = new javax.swing.JTabbedPane();
        hierachyPanel = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        navigation = new javax.swing.JMenuBar();
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
        loadProfileBackup = new javax.swing.JMenuItem();
        loadVitaProfile = new javax.swing.JMenuItem();
        loadMod = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        manageProfile = new javax.swing.JMenuItem();
        manageSettings = new javax.swing.JMenuItem();
        saveDivider = new javax.swing.JPopupMenu.Separator();
        saveAs = new javax.swing.JMenuItem();
        saveMenu = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        closeTab = new javax.swing.JMenuItem();
        reboot = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        editMenuDelete = new javax.swing.JMenuItem();
        archiveMenu = new javax.swing.JMenu();
        manageArchives = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        addFile = new javax.swing.JMenuItem();
        addFolder = new javax.swing.JMenuItem();
        databaseMenu = new javax.swing.JMenu();
        patchMAP = new javax.swing.JMenuItem();
        remapDatabaseContext = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        dumpRLST = new javax.swing.JMenuItem();
        profileMenu = new javax.swing.JMenu();
        extractBigProfile = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        editProfileSlots = new javax.swing.JMenuItem();
        editProfileItems = new javax.swing.JMenuItem();
        modMenu = new javax.swing.JMenu();
        openModMetadata = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        openCompressinator = new javax.swing.JMenuItem();
        openGfxCompiler = new javax.swing.JMenuItem();
        dumpSep = new javax.swing.JPopupMenu.Separator();
        jMenu1 = new javax.swing.JMenu();
        fileArchiveIntegrityCheck = new javax.swing.JMenuItem();
        mergeFARCs = new javax.swing.JMenuItem();
        swapProfilePlatform = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        convertTexture = new javax.swing.JMenuItem();
        decompressResource = new javax.swing.JMenuItem();
        collectionD = new javax.swing.JMenu();
        collectorPresets = new javax.swing.JMenu();
        collectAllLevelDependencies = new javax.swing.JMenuItem();
        collectAllItemDependencies = new javax.swing.JMenuItem();
        customCollector = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        fixDependencyTable = new javax.swing.JMenuItem();
        generateDiff = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        installProfileMod = new javax.swing.JMenuItem();
        exportWorld = new javax.swing.JMenuItem();
        exportSceneGraph = new javax.swing.JMenuItem();
        debugMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        changeResourceRevisionGroup.setText("Change Revision");

        changeResourceRevisionLBP1Context.setText("LBP1");
        changeResourceRevisionLBP1Context.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeResourceRevisionLBP1ContextActionPerformed(evt);
            }
        });
        changeResourceRevisionGroup.add(changeResourceRevisionLBP1Context);

        changeResourceRevisionLBP2Context.setText("LBP2");
        changeResourceRevisionLBP2Context.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeResourceRevisionLBP2ContextActionPerformed(evt);
            }
        });
        changeResourceRevisionGroup.add(changeResourceRevisionLBP2Context);

        changeResourceRevisionLBP3Context.setText("LBP3");
        changeResourceRevisionLBP3Context.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeResourceRevisionLBP3ContextActionPerformed(evt);
            }
        });
        changeResourceRevisionGroup.add(changeResourceRevisionLBP3Context);

        entryContext.add(changeResourceRevisionGroup);

        extractGroup.setText("Extract...");
        extractGroup.setToolTipText("Extract selected entries");

        extractContext.setText("Extract");
        extractContext.setToolTipText("Extract entries as-is");
        extractContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractContextActionPerformed(evt);
            }
        });
        extractGroup.add(extractContext);

        extractDecompressedContext.setText("Decompress");
        extractDecompressedContext.setToolTipText("Extract entries and decompress where possible");
        extractDecompressedContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractDecompressedContextActionPerformed(evt);
            }
        });
        extractGroup.add(extractDecompressedContext);

        entryContext.add(extractGroup);

        editGroup.setText("Edit...");
        editGroup.setToolTipText("Edit entry details");

        editPathContext.setText("Path");
        editPathContext.setToolTipText("Move this entry");
        editPathContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editPathContextActionPerformed(evt);
            }
        });
        editGroup.add(editPathContext);

        editHashContext.setText("Hash");
        editHashContext.setToolTipText("Edit the hash this entry loads");
        editHashContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editHashContextActionPerformed(evt);
            }
        });
        editGroup.add(editHashContext);

        editGUIDContext.setText("GUID");
        editGUIDContext.setToolTipText("Change the unique GUID for this entry");
        editGUIDContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editGUIDContextActionPerformed(evt);
            }
        });
        editGroup.add(editGUIDContext);

        entryContext.add(editGroup);

        exportGroup.setText("Export...");
        exportGroup.setToolTipText("Export resource to different formats");

        exportJSONContext.setText("JSON");
        exportJSONContext.setToolTipText("Converts resource to JSON file");
        exportJSONContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportJSONContextActionPerformed(evt);
            }
        });
        exportGroup.add(exportJSONContext);

        exportTextureGroupContext.setText("Textures");
        exportTextureGroupContext.setToolTipText("Export texture file as image");

        exportPNG.setText("PNG");
        exportPNG.setToolTipText("Export texture as PNG file");
        exportPNG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPNGActionPerformed(evt);
            }
        });
        exportTextureGroupContext.add(exportPNG);

        exportDDS.setText("DDS");
        exportDDS.setToolTipText("Export texture as DDS file");
        exportDDS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDDSActionPerformed(evt);
            }
        });
        exportTextureGroupContext.add(exportDDS);

        exportGroup.add(exportTextureGroupContext);

        exportModelGroup.setText("Model");
        exportModelGroup.setToolTipText("Export model file");

        exportOBJ.setText("Wavefront");
        exportOBJ.setToolTipText("Export model as Wavefront OBJ");

        exportOBJTEXCOORD0.setText("TEXCOORD0");
        exportOBJTEXCOORD0.setToolTipText("Export as OBJ with first UV channel");
        exportOBJTEXCOORD0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportOBJTEXCOORD0ActionPerformed(evt);
            }
        });
        exportOBJ.add(exportOBJTEXCOORD0);

        exportOBJTEXCOORD1.setText("TEXCOORD1");
        exportOBJTEXCOORD1.setToolTipText("Export as OBJ with second UV channel");
        exportOBJTEXCOORD1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportOBJTEXCOORD1ActionPerformed(evt);
            }
        });
        exportOBJ.add(exportOBJTEXCOORD1);

        exportOBJTEXCOORD2.setText("TEXCOORD2");
        exportOBJTEXCOORD2.setToolTipText("Export as OBJ with third UV channel");
        exportOBJTEXCOORD2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportOBJTEXCOORD2ActionPerformed(evt);
            }
        });
        exportOBJ.add(exportOBJTEXCOORD2);

        exportModelGroup.add(exportOBJ);

        exportGLTF.setText("glTF 2.0");
        exportGLTF.setToolTipText("Export model as glTF 2.0");
        exportGLTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportGLTFActionPerformed(evt);
            }
        });
        exportModelGroup.add(exportGLTF);

        exportGroup.add(exportModelGroup);

        exportModGroup.setText("Mod");
        exportModGroup.setToolTipText("Exports selected resource as a mod file");

        exportAsModCustom.setToolTipText("Manual mod export, choose which resources get exported as hash/GUID, as well as re-generating gmats");
        exportAsModCustom.setActionCommand("Custom");
        exportAsModCustom.setLabel("Custom");
        exportAsModCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsModCustomActionPerformed(evt);
            }
        });
        exportModGroup.add(exportAsModCustom);

        exportAsMod.setText("Hash");
        exportAsMod.setToolTipText("Export mod with GUID references replaced with hashes. You should use this if you're exporting custom content for others to use");
        exportAsMod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsModActionPerformed(evt);
            }
        });
        exportModGroup.add(exportAsMod);

        exportAsModGUID.setText("GUID");
        exportAsModGUID.setToolTipText("Export mod without modifying resource references");
        exportAsModGUID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsModGUIDActionPerformed(evt);
            }
        });
        exportModGroup.add(exportAsModGUID);

        exportGroup.add(exportModGroup);

        exportAnimation.setText("Animation");
        exportAnimation.setToolTipText("Export selected animation as glTF2.0 file");
        exportAnimation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAnimationActionPerformed(evt);
            }
        });
        exportGroup.add(exportAnimation);

        exportBackupGroup.setText("Backup");
        exportBackupGroup.setToolTipText("Convert this resource into a level backup.");

        exportAsBackup.setText("Hash");
        exportAsBackup.setToolTipText("Export backup with GUID references replaced with hashes. You should use this if you're exporting custom content for others to use");
        exportAsBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsBackupActionPerformed(evt);
            }
        });
        exportBackupGroup.add(exportAsBackup);

        exportAsBackupGUID.setText("GUID");
        exportAsBackupGUID.setToolTipText("Export backup without modifying resource references");
        exportAsBackupGUID.setActionCommand("exportAsBackupGUID");
        exportAsBackupGUID.setName(""); // NOI18N
        exportAsBackupGUID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsBackupGUIDActionPerformed(evt);
            }
        });
        exportBackupGroup.add(exportAsBackupGUID);

        exportGroup.add(exportBackupGroup);

        exportPaletteContext.setText("BIN");
        exportPaletteContext.setToolTipText("Convert this palette to a level");
        exportPaletteContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPaletteContextActionPerformed(evt);
            }
        });
        exportGroup.add(exportPaletteContext);

        entryContext.add(exportGroup);

        newEntryGroup.setText("New...");
        newEntryGroup.setToolTipText("Add new entries to the database");

        newResourceGroup.setText("Resource");
        newResourceGroup.setToolTipText("Import resources from local files");

        importJSONContext.setText("JSON");
        importJSONContext.setToolTipText("Import resource from exported JSON data");
        importJSONContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importJSONContextActionPerformed(evt);
            }
        });
        newResourceGroup.add(importJSONContext);

        newTextureContext.setText("Texture");
        newTextureContext.setToolTipText("Import texture resource from JPG/PNG/DDS");
        newTextureContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newTextureContextActionPerformed(evt);
            }
        });
        newResourceGroup.add(newTextureContext);

        newAnimationContext.setText("Animation");
        newAnimationContext.setToolTipText("Import animation from glTF2.0 file");
        newAnimationContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newAnimationContextActionPerformed(evt);
            }
        });
        newResourceGroup.add(newAnimationContext);

        newModelContext.setText("Model");
        newModelContext.setToolTipText("Import model from glTF2.0 file");
        newModelContext.setEnabled(false);
        newResourceGroup.add(newModelContext);

        newEntryGroup.add(newResourceGroup);

        newItemGroup.setText("Item");
        newItemGroup.setToolTipText("Generate inventory items");
        newItemGroup.setEnabled(false);

        newStickerContext.setText("Sticker");
        newStickerContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newStickerContextActionPerformed(evt);
            }
        });
        newItemGroup.add(newStickerContext);

        newEntryGroup.add(newItemGroup);

        newEntryContext.setText("File");
        newEntryContext.setToolTipText("Create a new blank entry in this database");
        newEntryContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newEntryContextActionPerformed(evt);
            }
        });
        newEntryGroup.add(newEntryContext);

        entryContext.add(newEntryGroup);

        newFolderContext.setText("New Folder");
        newFolderContext.setToolTipText("Create a new folder here");
        newFolderContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFolderContextActionPerformed(evt);
            }
        });
        entryContext.add(newFolderContext);

        renameFolder.setText("Rename Folder");
        renameFolder.setToolTipText("Renames selected folder");
        renameFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameFolderActionPerformed(evt);
            }
        });
        entryContext.add(renameFolder);

        replaceGroup.setText("Replace...");
        replaceGroup.setToolTipText("Data replacement tools");

        replaceCompressedContext.setText("Replace");
        replaceCompressedContext.setToolTipText("Replace resource data with a selected file from disk");
        replaceCompressedContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceCompressedContextActionPerformed(evt);
            }
        });
        replaceGroup.add(replaceCompressedContext);

        replaceDecompressedContext.setText("Decompressed");
        replaceDecompressedContext.setToolTipText("Re-compresses file selected from disk and replaces this entry's resource data");
        replaceDecompressedContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceDecompressedContextActionPerformed(evt);
            }
        });
        replaceGroup.add(replaceDecompressedContext);

        replaceDependenciesContext.setText("Dependencies");
        replaceDependenciesContext.setToolTipText("Edit the dependencies of this resource");
        replaceDependenciesContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceDependenciesContextActionPerformed(evt);
            }
        });
        replaceGroup.add(replaceDependenciesContext);

        replaceImageContext.setText("Image");
        replaceImageContext.setToolTipText("Replaces data with image loaded from disk");
        replaceImageContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceImageContextActionPerformed(evt);
            }
        });
        replaceGroup.add(replaceImageContext);

        replaceJSONContext.setText("JSON");
        replaceJSONContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceJSONContextActionPerformed(evt);
            }
        });
        replaceGroup.add(replaceJSONContext);

        entryContext.add(replaceGroup);

        dependencyGroup.setText("Dependencies...");
        dependencyGroup.setToolTipText("Dependency management tools");

        removeDependenciesContext.setText("Remove Dependencies");
        removeDependenciesContext.setToolTipText("Removes all resources from dependency table. This can allow levels with missing dependencies to potentially load.");
        removeDependenciesContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDependenciesContextActionPerformed(evt);
            }
        });
        dependencyGroup.add(removeDependenciesContext);

        removeMissingDependenciesContext.setText("Remove Missing Dependencies");
        removeMissingDependenciesContext.setToolTipText("Removes only missing resources from dependency table. This can allow levels with missing dependencies to potentially load.");
        removeMissingDependenciesContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMissingDependenciesContextActionPerformed(evt);
            }
        });
        dependencyGroup.add(removeMissingDependenciesContext);

        entryContext.add(dependencyGroup);

        duplicateContext.setText("Duplicate");
        duplicateContext.setToolTipText("Create a duplicate entry copying this one with a new GUID");
        duplicateContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateContextActionPerformed(evt);
            }
        });
        entryContext.add(duplicateContext);

        copyGroup.setText("Copy To...");
        copyGroup.setToolTipText("Copy selected entries to another database");
        entryContext.add(copyGroup);

        zeroContext.setText("Zero");
        zeroContext.setToolTipText("Removes resource hash. Allows files to be loaded from disk rather than the FARC");
        zeroContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zeroContextActionPerformed(evt);
            }
        });
        entryContext.add(zeroContext);

        deleteContext.setText("Delete");
        deleteContext.setToolTipText("Deletes selected entries from the database");
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

        workspace.setDividerLocation(275);

        details.setResizeWeight(1);
        details.setDividerLocation(850);

        previewContainer.setDividerLocation(325);
        previewContainer.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        console.setEditable(false);
        console.setColumns(20);
        console.setLineWrap(true);
        console.setRows(5);
        console.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                consoleMouseReleased(evt);
            }
        });
        consoleContainer.setViewportView(console);

        previewContainer.setBottomComponent(consoleContainer);

        overviewPane.setDividerLocation(325);

        texture.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        texture.setText("No preview to be displayed.");
        texture.setToolTipText("");
        texture.setFocusable(false);
        overviewPane.setLeftComponent(texture);

        javax.swing.GroupLayout hexLayout = new javax.swing.GroupLayout(hex);
        hex.setLayout(hexLayout);
        hexLayout.setHorizontalGroup(
            hexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        hexLayout.setVerticalGroup(
            hexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 292, Short.MAX_VALUE)
        );

        overviewPane.setRightComponent(hex);

        renderPane.addTab("Overview", overviewPane);

        javax.swing.GroupLayout scenePanelLayout = new javax.swing.GroupLayout(scenePanel);
        scenePanel.setLayout(scenePanelLayout);
        scenePanelLayout.setHorizontalGroup(
            scenePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 850, Short.MAX_VALUE)
        );
        scenePanelLayout.setVerticalGroup(
            scenePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 292, Short.MAX_VALUE)
        );

        renderPane.addTab("Scene", scenePanel);

        previewContainer.setTopComponent(renderPane);

        details.setLeftComponent(previewContainer);

        infoCardPanel.setLayout(new java.awt.CardLayout());

        fileDataPane.setDividerLocation(204);
        fileDataPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        fileDataPane.setMaximumSize(new java.awt.Dimension(55, 2147483647));
        fileDataPane.setMinimumSize(new java.awt.Dimension(55, 102));
        fileDataPane.setPreferredSize(new java.awt.Dimension(55, 1120));

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

        fileDataPane.setLeftComponent(tableContainer);

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

        creatorLabel.setText("Creator");

        creatorField.setEditable(false);

        subCombo.setEditable(false);
        subCombo.setAutoscrolls(false);
        subCombo.setEnabled(false);

        javax.swing.GroupLayout itemMetadataLayout = new javax.swing.GroupLayout(itemMetadata);
        itemMetadata.setLayout(itemMetadataLayout);
        itemMetadataLayout.setHorizontalGroup(
            itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemMetadataLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(itemMetadataLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(LAMSMetadata, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(StringMetadata, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(descriptionLabel)
                    .addGroup(itemMetadataLayout.createSequentialGroup()
                        .addComponent(iconLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(iconField, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(itemMetadataLayout.createSequentialGroup()
                        .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(itemMetadataLayout.createSequentialGroup()
                        .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(locationLabel)
                            .addComponent(categoryLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(itemMetadataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(locationField)
                            .addComponent(categoryField)))
                    .addComponent(pageCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(itemMetadataLayout.createSequentialGroup()
                        .addComponent(creatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(creatorField, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(subCombo))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        entryModifiers.addTab("Inspector", itemMetadata);

        fileDataPane.setRightComponent(entryModifiers);

        infoCardPanel.add(fileDataPane, "card2");

        jLabel1.setText("Thing UID 0");

        jLabel2.setText("PCamera");

        jLabel3.setText("Target Position");

        cameraPosX.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 100.0f));
        cameraPosX.setToolTipText("");

        cameraPosY.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 100.0f));
        cameraPosY.setToolTipText("");

        cameraPosZ.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 100.0f));
        cameraPosZ.setToolTipText("");

        javax.swing.GroupLayout inspectorPaneLayout = new javax.swing.GroupLayout(inspectorPane);
        inspectorPane.setLayout(inspectorPaneLayout);
        inspectorPaneLayout.setHorizontalGroup(
            inspectorPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inspectorPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inspectorPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cameraPosX)
                    .addComponent(cameraPosY, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(inspectorPaneLayout.createSequentialGroup()
                        .addGroup(inspectorPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(0, 159, Short.MAX_VALUE))
                    .addComponent(cameraPosZ))
                .addContainerGap())
        );
        inspectorPaneLayout.setVerticalGroup(
            inspectorPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inspectorPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cameraPosX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cameraPosY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cameraPosZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(441, Short.MAX_VALUE))
        );

        infoCardPanel.add(inspectorPane, "card3");

        details.setRightComponent(infoCardPanel);

        workspace.setRightComponent(details);

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

        resourceTabs.addTab("Assets", treeContainer);

        javax.swing.GroupLayout hierachyPanelLayout = new javax.swing.GroupLayout(hierachyPanel);
        hierachyPanel.setLayout(hierachyPanelLayout);
        hierachyPanelLayout.setHorizontalGroup(
            hierachyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );
        hierachyPanelLayout.setVerticalGroup(
            hierachyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 558, Short.MAX_VALUE)
        );

        resourceTabs.addTab("Hierachy", hierachyPanel);

        workspace.setLeftComponent(resourceTabs);

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

        loadDB.setText("FileDB (.MAP)");
        loadDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDBActionPerformed(evt);
            }
        });
        gamedataMenu.add(loadDB);

        loadArchive.setText("File Archive (.FARC)");
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

        loadProfileBackup.setText("Profile Backup");
        loadProfileBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadProfileBackupActionPerformed(evt);
            }
        });
        savedataMenu.add(loadProfileBackup);

        loadVitaProfile.setText("Vita Profile (Readonly)");
        loadVitaProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadVitaProfileActionPerformed(evt);
            }
        });
        savedataMenu.add(loadVitaProfile);

        loadGroupMenu.add(savedataMenu);

        loadMod.setText("Mod");
        loadMod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadModActionPerformed(evt);
            }
        });
        loadGroupMenu.add(loadMod);

        fileMenu.add(loadGroupMenu);
        fileMenu.add(jSeparator9);

        manageProfile.setText("Profiles");
        manageProfile.setToolTipText("Manage boot profiles and settings");
        manageProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageProfileActionPerformed(evt);
            }
        });
        fileMenu.add(manageProfile);

        manageSettings.setText("Settings");
        manageSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageSettingsActionPerformed(evt);
            }
        });
        fileMenu.add(manageSettings);
        fileMenu.add(saveDivider);

        saveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveAs.setText("Save as...");
        saveAs.setToolTipText("Save a copy of selected database");
        saveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsActionPerformed(evt);
            }
        });
        fileMenu.add(saveAs);

        saveMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveMenu.setText("Save");
        saveMenu.setToolTipText("Save selected database and any archives that may have changes");
        saveMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenu);
        fileMenu.add(jSeparator4);

        closeTab.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        closeTab.setText("Close Tab");
        closeTab.setToolTipText("Close currently selected tab");
        closeTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeTabActionPerformed(evt);
            }
        });
        fileMenu.add(closeTab);

        reboot.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        reboot.setText("Reboot");
        reboot.setToolTipText("Restart Toolkit");
        reboot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rebootActionPerformed(evt);
            }
        });
        fileMenu.add(reboot);

        navigation.add(fileMenu);

        editMenu.setText("Edit");

        editMenuDelete.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        editMenuDelete.setText("Delete");
        editMenuDelete.setToolTipText("Deletes selected entries from the database");
        editMenuDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuDeleteActionPerformed(evt);
            }
        });
        editMenu.add(editMenuDelete);

        navigation.add(editMenu);

        archiveMenu.setText("Archive");

        manageArchives.setText("Manage Archives");
        manageArchives.setToolTipText("Add/Remove/Save loaded archives");
        manageArchives.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageArchivesActionPerformed(evt);
            }
        });
        archiveMenu.add(manageArchives);
        archiveMenu.add(jSeparator10);

        addFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        addFile.setText("Add...");
        addFile.setToolTipText("Add files to a loaded archive");
        addFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileActionPerformed(evt);
            }
        });
        archiveMenu.add(addFile);

        addFolder.setText("Add Folder");
        addFolder.setToolTipText("Add all files in a folder to a loaded archive");
        addFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFolderActionPerformed(evt);
            }
        });
        archiveMenu.add(addFolder);

        navigation.add(archiveMenu);

        databaseMenu.setText("FileDB");

        patchMAP.setText("Patch");
        patchMAP.setToolTipText("Patch another FileDB on-top of this one");
        patchMAP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                patchMAPActionPerformed(evt);
            }
        });
        databaseMenu.add(patchMAP);

        remapDatabaseContext.setText("Remap");
        remapDatabaseContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remapDatabaseContextActionPerformed(evt);
            }
        });
        databaseMenu.add(remapDatabaseContext);
        databaseMenu.add(jSeparator6);

        dumpRLST.setText("Dump RLST");
        dumpRLST.setToolTipText("Dump all entries to RLST");
        dumpRLST.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dumpRLSTActionPerformed(evt);
            }
        });
        databaseMenu.add(dumpRLST);

        navigation.add(databaseMenu);

        profileMenu.setText("Profile");

        extractBigProfile.setText("Extract Profile");
        extractBigProfile.setToolTipText("Extract RBigProfile from save, mostly used for debugging.");
        extractBigProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractBigProfileActionPerformed(evt);
            }
        });
        profileMenu.add(extractBigProfile);
        profileMenu.add(jSeparator1);

        editProfileSlots.setText("Edit Slots");
        editProfileSlots.setToolTipText("Edit and add levels to this profile");
        editProfileSlots.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProfileSlotsActionPerformed(evt);
            }
        });
        profileMenu.add(editProfileSlots);

        editProfileItems.setText("Edit Items");
        editProfileItems.setToolTipText("Edit and add collected items to this profile");
        editProfileItems.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProfileItemsActionPerformed(evt);
            }
        });
        profileMenu.add(editProfileItems);

        navigation.add(profileMenu);

        modMenu.setText("Mod");

        openModMetadata.setText("Edit");
        openModMetadata.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openModMetadataActionPerformed(evt);
            }
        });
        modMenu.add(openModMetadata);

        navigation.add(modMenu);

        toolsMenu.setText("Tools");

        openCompressinator.setText("Compressinator GUI");
        openCompressinator.setToolTipText("Compress files to resources");
        openCompressinator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openCompressinatorActionPerformed(evt);
            }
        });
        toolsMenu.add(openCompressinator);

        openGfxCompiler.setText("Gfx Compiler GUI");
        openGfxCompiler.setToolTipText("Compile GMATs from source shaders");
        openGfxCompiler.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openGfxCompilerActionPerformed(evt);
            }
        });
        toolsMenu.add(openGfxCompiler);
        toolsMenu.add(dumpSep);

        jMenu1.setText("Archive Utilities");

        fileArchiveIntegrityCheck.setText("FARC Integrity Check");
        fileArchiveIntegrityCheck.setToolTipText("Verify that all data in the FARC matches hash table");
        fileArchiveIntegrityCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileArchiveIntegrityCheckActionPerformed(evt);
            }
        });
        jMenu1.add(fileArchiveIntegrityCheck);

        mergeFARCs.setText("Merge Archives");
        mergeFARCs.setToolTipText("Merge two archives together");
        mergeFARCs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeFARCsActionPerformed(evt);
            }
        });
        jMenu1.add(mergeFARCs);

        swapProfilePlatform.setText("Swap FAR4 Endianness (PS3/PS4)");
        swapProfilePlatform.setToolTipText("Switch big/littlefart endianness, used for converting profile saves between PS3 and PS4");
        swapProfilePlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swapProfilePlatformActionPerformed(evt);
            }
        });
        jMenu1.add(swapProfilePlatform);

        toolsMenu.add(jMenu1);

        jMenu2.setText("File Utilities");

        convertTexture.setText("PNG/JPG to TEX");
        convertTexture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertTextureActionPerformed(evt);
            }
        });
        jMenu2.add(convertTexture);

        decompressResource.setText("Decompress Resource");
        decompressResource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decompressResourceActionPerformed(evt);
            }
        });
        jMenu2.add(decompressResource);

        toolsMenu.add(jMenu2);

        collectionD.setText("Collectors");
        collectionD.setToolTipText("");

        collectorPresets.setText("Presets");

        collectAllLevelDependencies.setText("RLevel");
        collectAllLevelDependencies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                collectAllLevelDependenciesActionPerformed(evt);
            }
        });
        collectorPresets.add(collectAllLevelDependencies);

        collectAllItemDependencies.setText("RPlan");
        collectAllItemDependencies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                collectAllItemDependenciesActionPerformed(evt);
            }
        });
        collectorPresets.add(collectAllItemDependencies);

        collectionD.add(collectorPresets);

        customCollector.setText("Custom");
        customCollector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customCollectorActionPerformed(evt);
            }
        });
        collectionD.add(customCollector);

        toolsMenu.add(collectionD);
        toolsMenu.add(jSeparator3);

        fixDependencyTable.setText("Calculate Dependency Table");
        fixDependencyTable.setToolTipText("Takes any compressed resource and attempts to automatically generate the dependency table");
        fixDependencyTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixDependencyTableActionPerformed(evt);
            }
        });
        toolsMenu.add(fixDependencyTable);

        generateDiff.setText("Get FileDB diffs");
        generateDiff.setToolTipText("Generates a text file showing differences between two FileDB's");
        generateDiff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateDiffActionPerformed(evt);
            }
        });
        toolsMenu.add(generateDiff);
        toolsMenu.add(jSeparator5);

        installProfileMod.setText("Install Mod(s)");
        installProfileMod.setToolTipText("Installs mod file(s) to currently selected database");
        installProfileMod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installProfileModActionPerformed(evt);
            }
        });
        toolsMenu.add(installProfileMod);

        exportWorld.setText("Export RLevel");
        exportWorld.setToolTipText("Exports the current scene graph as a level");
        exportWorld.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportWorldActionPerformed(evt);
            }
        });
        toolsMenu.add(exportWorld);

        exportSceneGraph.setText("Export Scene Graph");
        exportSceneGraph.setToolTipText("Dumps the current scene graph to a file");
        exportSceneGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportSceneGraphActionPerformed(evt);
            }
        });
        toolsMenu.add(exportSceneGraph);

        navigation.add(toolsMenu);

        debugMenu.setText("Debug");

        jMenuItem1.setText("open big profile gui");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        debugMenu.add(jMenuItem1);

        navigation.add(debugMenu);

        setJMenuBar(navigation);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(workspace, javax.swing.GroupLayout.DEFAULT_SIZE, 1385, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(workspace, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newStickerContextActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_newStickerContextActionPerformed
        byte[] texture = null;
        try
        {
            texture = TextureImporter.getTexture();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Texture failed to convert!",
                "Texture Importer",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (texture == null)
            return;

        FileData database = ResourceSystem.getSelectedDatabase();
        ResourceDescriptor textureDescriptor;
        boolean inGameDatabase = database.getType().hasGUIDs();
        if (inGameDatabase)
        {
            // Prompt the user to add the texture to the database
            FileEntry textureResourceEntry = DatabaseCallbacks.newEntry(texture);
            if (textureResourceEntry == null)
            {
                JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Sticker import was cancelled",
                "Item Importer", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            textureDescriptor = new ResourceDescriptor((GUID) textureResourceEntry.getKey(),
             ResourceType.TEXTURE);
        }
        else
        {
            ResourceSystem.add(texture);
            textureDescriptor = new ResourceDescriptor(SHA1.fromBuffer(texture),
              ResourceType.TEXTURE);
        }

        Revision revision = new Revision(Branch.LEERDAMMER.getHead(), Branch.LEERDAMMER.getID(),
            Revisions.LD_LAMS_KEYS);
        Thing thing = new Thing(1);
        thing.setPart(Part.STICKERS, new PStickers(textureDescriptor));

        InventoryItemDetails details = new InventoryItemDetails();
        details.type.add(InventoryObjectType.STICKER);
        // Assume it's a user sticker if we're in a database that supports GUIDs.
        if (!inGameDatabase)
            details.type.add(InventoryObjectType.USER_STICKER);

        details.icon = textureDescriptor;
        details.userCreatedDetails = new UserCreatedDetails("Imported Sticker", "");

        RPlan plan = new RPlan(revision, CompressionFlags.USE_ALL_COMPRESSION, thing, details);
        ItemManager manager = new ItemManager(null, plan);
        manager.setVisible(true);

        // Now that the user has configured the item details, import the finished asset to the
        // database
        byte[] planData = SerializedResource.compress(plan.build());
        FileEntry planResourceEntry = DatabaseCallbacks.newEntry(planData);
        if (inGameDatabase && planResourceEntry == null)
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Sticker import was cancelled", "Item" +
             " Importer", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Successfully imported sticker!",
            "Item Importer", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_newStickerContextActionPerformed

    private void changeResourceRevisionLBP3ContextActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_changeResourceRevisionLBP3ContextActionPerformed
        EditCallbacks.changeRevision(new Revision(0x021803f9));
    }//GEN-LAST:event_changeResourceRevisionLBP3ContextActionPerformed

    private void changeResourceRevisionLBP1ContextActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_changeResourceRevisionLBP1ContextActionPerformed
        EditCallbacks.changeRevision(new Revision(Branch.LEERDAMMER.getHead(),
         Branch.LEERDAMMER.getID(), Revisions.LD_LAMS_KEYS));
    }//GEN-LAST:event_changeResourceRevisionLBP1ContextActionPerformed

    private void changeResourceRevisionLBP2ContextActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_changeResourceRevisionLBP2ContextActionPerformed
        EditCallbacks.changeRevision(new Revision(0x3f6));
    }//GEN-LAST:event_changeResourceRevisionLBP2ContextActionPerformed

    private void loadDBActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_loadDBActionPerformed
        File file = FileChooser.openFile("blurayguids.map", "map", false);
        if (file != null)
            DatabaseCallbacks.loadFileDB(file);
    }// GEN-LAST:event_loadDBActionPerformed

    public void addTab(FileData data)
    {
        ResourceSystem.getDatabases().add(data);
        JTree tree = data.getTree();

        tree.addTreeSelectionListener(e -> TreeSelectionListener.listener(tree));
        tree.addMouseListener(showContextMenu);

        JScrollPane panel = new JScrollPane();
        panel.setViewportView(tree);

        fileDataTabs.addTab(data.getName(), panel);

        search.setEditable(true);
        search.setFocusable(true);
        search.setText("Search...");
        search.setForeground(Color.GRAY);

        fileDataTabs.setSelectedIndex(fileDataTabs.getTabCount() - 1);
        ResourceSystem.reloadSelectedModel();
    }

    public int isDatabaseLoaded(File file)
    {
        for (int i = 0; i < ResourceSystem.getDatabases().size(); ++i)
        {
            FileData data = ResourceSystem.getDatabases().get(i);
            if (data.getFile().equals(file))
                return i;
        }
        return -1;
    }

    private void loadArchiveActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_loadArchiveActionPerformed
        File[] files = FileChooser.openFiles("data.farc", "farc");
        if (files == null)
            return;
        for (File file : files)
            ArchiveCallbacks.loadFileArchive(file);
    }// GEN-LAST:event_loadArchiveActionPerformed

    public int isArchiveLoaded(File file)
    {
        String path = file.getAbsolutePath();
        for (int i = 0; i < ResourceSystem.getArchives().size(); ++i)
        {
            Fart archive = ResourceSystem.getArchives().get(i);
            if (archive.getFile().getAbsolutePath().equals(path))
                return i;
        }
        return -1;
    }

    private void openCompressinatorActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_openCompressinatorActionPerformed
        new Compressinator().setVisible(true);
    }// GEN-LAST:event_openCompressinatorActionPerformed

    private void decompressResourceActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_decompressResourceActionPerformed
        UtilityCallbacks.decompressResource();
    }// GEN-LAST:event_decompressResourceActionPerformed

    private void saveMenuActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_saveMenuActionPerformed
        FileCallbacks.save();
    }// GEN-LAST:event_saveMenuActionPerformed

    private void addFileActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_addFileActionPerformed
        ArchiveCallbacks.addFile();
    }// GEN-LAST:event_addFileActionPerformed

    private void clearActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_clearActionPerformed
        console.selectAll();
        console.replaceSelection("");
    }// GEN-LAST:event_clearActionPerformed

    private void consoleMouseReleased(java.awt.event.MouseEvent evt)
    {// GEN-FIRST:event_consoleMouseReleased
        if (evt.isPopupTrigger())
            consolePopup.show(console, evt.getX(), evt.getY());
    }// GEN-LAST:event_consoleMouseReleased

    private void extractContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_extractContextActionPerformed
        ArchiveCallbacks.extract(false);
    }// GEN-LAST:event_extractContextActionPerformed

    private void extractDecompressedContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_extractDecompressedContextActionPerformed
        ArchiveCallbacks.extract(true);
    }// GEN-LAST:event_extractDecompressedContextActionPerformed

    private void exportOBJTEXCOORD0ActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportOBJTEXCOORD0ActionPerformed
        ExportCallbacks.exportOBJ(0);
    }// GEN-LAST:event_exportOBJTEXCOORD0ActionPerformed

    private void locationFieldActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_locationFieldActionPerformed

    }// GEN-LAST:event_locationFieldActionPerformed

    private void LAMSMetadataActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_LAMSMetadataActionPerformed
        ResourceInfo info = ResourceSystem.getSelected().getEntry().getInfo();
        if (info == null || info.getType() != ResourceType.PLAN || info.getResource() == null)
            return;
        InventoryItemDetails details = info.<RPlan>getResource().inventoryData;

        titleField.setText("" + details.titleKey);
        descriptionField.setText("" + details.descriptionKey);
        locationField.setText("" + details.location);
        categoryField.setText("" + details.category);
    }// GEN-LAST:event_LAMSMetadataActionPerformed

    private void StringMetadataActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_StringMetadataActionPerformed
        ResourceInfo info = ResourceSystem.getSelected().getEntry().getInfo();
        if (info == null || info.getType() != ResourceType.PLAN || info.getResource() == null)
            return;
        InventoryItemDetails details = info.<RPlan>getResource().inventoryData;

        titleField.setText("");
        categoryField.setText("");
        locationField.setText("");
        categoryField.setText("");

        RTranslationTable LAMS = ResourceSystem.getLAMS();
        if (LAMS != null)
        {
            details.translatedTitle = LAMS.translate(details.titleKey);
            details.translatedDescription = LAMS.translate(details.descriptionKey);
            details.translatedCategory = LAMS.translate(details.category);
            details.translatedLocation = LAMS.translate(details.location);

            titleField.setText(details.translatedTitle);
            descriptionField.setText(details.translatedDescription);
            locationField.setText(details.translatedLocation);
            categoryField.setText(details.translatedCategory);
        }

        if (details.userCreatedDetails != null)
        {
            titleField.setText(details.userCreatedDetails.name);
            descriptionField.setText(details.userCreatedDetails.description);
        }
    }// GEN-LAST:event_StringMetadataActionPerformed

    private void loadBigProfileActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_loadBigProfileActionPerformed
        ProfileCallbacks.loadProfile();
    }// GEN-LAST:event_loadBigProfileActionPerformed

    public Fart[] getSelectedArchives()
    {
        if (ResourceSystem.getArchives().size() == 0)
            return null;
        if (ResourceSystem.getArchives().size() > 1)
        {
            Fart[] archives = new ArchiveSelector(this, true).getSelected();
            if (archives == null)
                System.out.println("User did not select any FileArchives, cancelling " +
                                   "operation.");
            return archives;
        }
        return new Fart[] {
            ResourceSystem.getArchives().get(0)
        };
    }

    private void searchActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_searchActionPerformed
        FileData database = ResourceSystem.getSelectedDatabase();
        database.setLastSearch(search.getText());
        JTree tree = database.getTree();
        Nodes.filter((FileNode) tree.getModel().getRoot(),
            new SearchParameters(search.getText()));
        ((FileModel) tree.getModel()).reload();
        tree.updateUI();
    }// GEN-LAST:event_searchActionPerformed

    private void extractBigProfileActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_extractBigProfileActionPerformed
        ProfileCallbacks.extractProfile();
    }// GEN-LAST:event_extractBigProfileActionPerformed

    private void exportDDSActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportDDSActionPerformed
        ExportCallbacks.exportDDS();
    }// GEN-LAST:event_exportDDSActionPerformed

    private void exportPNGActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportPNGActionPerformed
        ExportCallbacks.exportTexture("png");
    }// GEN-LAST:event_exportPNGActionPerformed

    private void patchMAPActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_patchMAPActionPerformed
        DatabaseCallbacks.patchDatabase();
    }// GEN-LAST:event_patchMAPActionPerformed

    private void replaceCompressedContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_replaceCompressedContextActionPerformed
        ReplacementCallbacks.replaceCompressed();
    }// GEN-LAST:event_replaceCompressedContextActionPerformed

    private void mergeFARCsActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_mergeFARCsActionPerformed
        UtilityCallbacks.mergeFileArchives();
    }// GEN-LAST:event_mergeFARCsActionPerformed

    private void saveAsActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_saveAsActionPerformed
        FileCallbacks.saveAs();
    }// GEN-LAST:event_saveAsActionPerformed

    private void deleteContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_deleteContextActionPerformed
        DatabaseCallbacks.delete();
    }// GEN-LAST:event_deleteContextActionPerformed

    private void zeroContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_zeroContextActionPerformed
        DatabaseCallbacks.zero();
    }// GEN-LAST:event_zeroContextActionPerformed

    private void newFolderContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_newFolderContextActionPerformed
        DatabaseCallbacks.newFolder();
    }// GEN-LAST:event_newFolderContextActionPerformed

    private void generateDiffActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_generateDiffActionPerformed
        UtilityCallbacks.generateFileDBDiff();
    }// GEN-LAST:event_generateDiffActionPerformed

    private void loadModActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_loadModActionPerformed
        File file = FileChooser.openFile("example.mod", "mod", false);
        if (file == null)
            return;
        Mod mod = ModCallbacks.loadMod(file);
        if (mod != null)
        {
            addTab(mod);
            updateWorkspace();
        }
    }// GEN-LAST:event_loadModActionPerformed

    private void openModMetadataActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_openModMetadataActionPerformed
        new ModManager(ResourceSystem.getSelectedDatabase(), false).setVisible(true);
    }// GEN-LAST:event_openModMetadataActionPerformed

    private void editProfileSlotsActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_editProfileSlotsActionPerformed
        new SlotManager(ResourceSystem.getSelectedDatabase(), null).setVisible(true);
    }// GEN-LAST:event_editProfileSlotsActionPerformed

    private void newVitaDBActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_newVitaDBActionPerformed
        DatabaseCallbacks.newFileDB(936);
    }// GEN-LAST:event_newVitaDBActionPerformed

    private void newModernDBActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_newModernDBActionPerformed
        DatabaseCallbacks.newFileDB(21496064);
    }// GEN-LAST:event_newModernDBActionPerformed

    private void newLegacyDBActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_newLegacyDBActionPerformed
        DatabaseCallbacks.newFileDB(256);
    }// GEN-LAST:event_newLegacyDBActionPerformed

    public boolean confirmOverwrite(File file)
    {
        if (file.exists())
        {
            int result = JOptionPane.showConfirmDialog(null,
                "This file already exists, are you sure you want to override it?",
                "Confirm " +
                "overwrite",
                JOptionPane.YES_NO_OPTION);
            return result != JOptionPane.NO_OPTION;
        }
        return true;
    }

    private void createFileArchiveActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_createFileArchiveActionPerformed
        ArchiveCallbacks.newFileArchive();
    }// GEN-LAST:event_createFileArchiveActionPerformed

    private void editProfileItemsActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_editProfileItemsActionPerformed
        new ItemManager(ResourceSystem.getSelectedDatabase()).setVisible(true);
    }// GEN-LAST:event_editProfileItemsActionPerformed

    private void installProfileModActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_installProfileModActionPerformed
        UtilityCallbacks.installMod();
    }// GEN-LAST:event_installProfileModActionPerformed

    private void duplicateContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_duplicateContextActionPerformed
        DatabaseCallbacks.duplicateItem();
    }// GEN-LAST:event_duplicateContextActionPerformed

    private void newEntryContextActionPerformed(java.awt.event.ActionEvent evt)
    {
        if (ResourceSystem.getDatabaseType() == DatabaseType.BIGFART)
        {
            ArchiveCallbacks.addFile();
            return;
        }
        DatabaseCallbacks.newEntry(null);
    }

    private void replaceDecompressedContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_replaceDecompressedContextActionPerformed
        ReplacementCallbacks.replaceDecompressed();
    }// GEN-LAST:event_replaceDecompressedContextActionPerformed

    private void replaceDependenciesContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_replaceDependenciesContextActionPerformed
        new Dependinator(this, ResourceSystem.getSelected().getEntry());
    }// GEN-LAST:event_replaceDependenciesContextActionPerformed

    private void exportAsModActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportAsModActionPerformed
        ExportCallbacks.exportMod(true);
    }// GEN-LAST:event_exportAsModActionPerformed

    private void dumpRLSTActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_dumpRLSTActionPerformed
        DatabaseCallbacks.dumpRLST();
    }// GEN-LAST:event_dumpRLSTActionPerformed

    private void removeDependenciesContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_removeDependenciesContextActionPerformed
        DependencyCallbacks.removeDependencies();
    }// GEN-LAST:event_removeDependenciesContextActionPerformed

    private void removeMissingDependenciesContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_removeMissingDependenciesContextActionPerformed
        DependencyCallbacks.removeMissingDependencies();
    }// GEN-LAST:event_removeMissingDependenciesContextActionPerformed

    private void exportOBJTEXCOORD1ActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportOBJTEXCOORD1ActionPerformed
        ExportCallbacks.exportOBJ(1);
    }// GEN-LAST:event_exportOBJTEXCOORD1ActionPerformed

    private void exportOBJTEXCOORD2ActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportOBJTEXCOORD2ActionPerformed
        ExportCallbacks.exportOBJ(2);
    }// GEN-LAST:event_exportOBJTEXCOORD2ActionPerformed

    private void replaceImageContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_replaceImageContextActionPerformed
        ReplacementCallbacks.replaceImage();
    }// GEN-LAST:event_replaceImageContextActionPerformed

    private void rebootActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_rebootActionPerformed
        this.search.getParent().remove(this.search);
        this.checkForChanges();
        this.dispose();
        EventQueue.invokeLater(() -> new Toolkit().setVisible(true));
    }// GEN-LAST:event_rebootActionPerformed

    private void closeTabActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_closeTabActionPerformed
        FileCallbacks.closeTab();
    }// GEN-LAST:event_closeTabActionPerformed

    private void exportGLTFActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportGLTFActionPerformed
        ExportCallbacks.exportGLB();
    }// GEN-LAST:event_exportGLTFActionPerformed

    private void newModActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_newModActionPerformed
        UtilityCallbacks.newMod();
    }// GEN-LAST:event_newModActionPerformed

    private void exportAsModGUIDActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportAsModGUIDActionPerformed
        ExportCallbacks.exportMod(false);
    }// GEN-LAST:event_exportAsModGUIDActionPerformed

    private void editPathContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_editPathContextActionPerformed
        DatabaseCallbacks.renameItem();
    }// GEN-LAST:event_editPathContextActionPerformed

    private void editGUIDContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_editGUIDContextActionPerformed
        DatabaseCallbacks.changeGUID();
    }// GEN-LAST:event_editGUIDContextActionPerformed

    private void editHashContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_editHashContextActionPerformed
        DatabaseCallbacks.changeHash();
    }// GEN-LAST:event_editHashContextActionPerformed

    private void exportAnimationActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportAnimationActionPerformed
        ExportCallbacks.exportAnimation();
    }// GEN-LAST:event_exportAnimationActionPerformed

    private void swapProfilePlatformActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_swapProfilePlatformActionPerformed
        File FAR4 = FileChooser.openFile("bigfart", null, false);
        if (FAR4 == null)
            return;
        if (FAR4.exists())
        {
            SaveArchive archive = null;
            try
            {
                archive = new SaveArchive(FAR4);
            }
            catch (SerializationException ex)
            {
                System.err.println(ex.getMessage());
                return;
            }
            if (archive.getArchiveRevision() != 4)
            {
                System.out.println("FileArchive isn't a FAR4!");
                return;
            }
            boolean wasPS4 = archive.isLittleEndian();
            archive.setLittleEndian(!wasPS4);

            FileIO.write(archive.build(false), FAR4.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                String.format("FAR4 has been swapped to %s endianness.",
                    (!wasPS4) ? "PS4" : "PS3"));
        }
        else
            System.out.printf("%s does not exist!%n", FAR4.getAbsolutePath());
    }// GEN-LAST:event_swapProfilePlatformActionPerformed

    private void addFolderActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_addFolderActionPerformed
        ArchiveCallbacks.addFolder();
    }// GEN-LAST:event_addFolderActionPerformed

    private void fileArchiveIntegrityCheckActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_fileArchiveIntegrityCheckActionPerformed
        ArchiveCallbacks.integrityCheck();
    }// GEN-LAST:event_fileArchiveIntegrityCheckActionPerformed

    enum ExportMode
    {
        GUID,
        Hash
    }

    private void exportAsBackupActionPerformed(java.awt.event.ActionEvent evt)
    {
        exportAsBackupTpl(ExportMode.Hash);
    }

    private void exportAsBackupTpl(ExportMode mode)
    {
        // TODO: Actually reimplement this
        // FileEntry entry = ResourceSystem.getSelected().getEntry();
        // String name =
        // Paths.get(ResourceSystem.getSelected().getEntry().path).getFileName().toString();

        // String titleID = JOptionPane.showInputDialog(Toolkit.instance, "TitleID",
        // "BCUS98148");
        // if (titleID == null) return;

        // String directory = FileChooser.openDirectory();
        // if (directory == null) return;

        // Revision fartRevision = new Revision(0x272, 0x4c44, 0x0017);
        // FileArchive archive = new FileArchive();

        // Slot slot = new Slot();
        // slot.title = name;
        // slot.id = new SlotID(SlotType.FAKE, 0);
        // slot.icon = new ResourceDescriptor(10682, ResourceType.TEXTURE);

        // // NOTE(Aidan): Cheap trick, but it's what I'm doing for now.
        // Resource resource = null;
        // switch (mode) {
        // case Hash:
        // resource = new Resource(ResourceSystem.extract(entry.hash));
        // break;
        // case GUID:
        // resource = new Resource(ResourceSystem.extract(entry.GUID));
        // break;
        // }
        // Mod mod = new Mod();
        // SHA1 hash = Bytes.hashinate(mod, resource, entry, null);

        // archive.entries = mod.entries;

        // if (entry.path.endsWith(".bin"))
        // switch (mode) {
        // case Hash:
        // slot.root = new ResourceDescriptor(hash, ResourceType.LEVEL);
        // break;
        // case GUID:
        // slot.root = new ResourceDescriptor(entry.GUID, ResourceType.LEVEL);
        // break;
        // }
        // else if (entry.path.endsWith(".plan")) {
        // Resource level = new Resource(FileIO.getResourceFile("/prize_template"));
        // switch (mode) {
        // case Hash:
        // level.replaceDependency(level.dependencies.get(0xB), new
        // ResourceDescriptor(hash, ResourceType.PLAN));
        // break;
        // case GUID:
        // level.replaceDependency(level.dependencies.get(0xB), new
        // ResourceDescriptor(entry.GUID, ResourceType.PLAN));
        // break;
        // }
        // byte[] levelData = level.compressToResource();
        // archive.add(levelData);
        // slot.root = new ResourceDescriptor(SHA1.fromBuffer(levelData),
        // ResourceType.LEVEL);
        // }

        // Serializer serializer = new Serializer(1024, fartRevision, (byte) 0x7);
        // serializer.array(new Slot[] { slot }, Slot.class);
        // byte[] slotList = Resource.compressToResource(serializer.output,
        // ResourceType.SLOT_LIST);
        // archive.add(slotList);
        // SHA1 rootHash = SHA1.fromBuffer(slotList);

        // archive.setFatDataSource(rootHash);
        // archive.setFatResourceType(ResourceType.SLOT_LIST);
        // archive.setFatRevision(fartRevision);

        // Random random = new Random();
        // byte[] UID = new byte[4];
        // random.nextBytes(UID);
        // titleID += "LEVEL" + Bytes.toHex(UID).toUpperCase();

        // Path saveDirectory = Path.of(directory, titleID);
        // try { Files.createDirectories(saveDirectory); }
        // catch (IOException ex) {
        // System.err.println("There was an error creating directory!");
        // return;
        // }

        // FileIO.write(new ParamSFO(titleID, name).build(),
        // Path.of(saveDirectory.toString(), "PARAM.SFO").toString());
        // FileIO.write(FileIO.getResourceFile("/images/slots/backdrop.png"),
        // Path.of(saveDirectory.toString(), "ICON0.PNG").toString());

        // // NOTE(Aidan): This seems terribly inefficient in terms of memory cost,
        // // but the levels exported should be low, so it's not entirely an issue,
        // // FOR NOW.

        // byte[][] profiles = null;
        // {
        // byte[] profile = archive.build();
        // profile = Arrays.copyOfRange(profile, 0, profile.length - 4);
        // profiles = Bytes.split(profile, 0x240000);
        // }

        // for (int i = 0; i < profiles.length; ++i) {
        // byte[] part = profiles[i];
        // part = Crypto.XXTEA(part, false);
        // if (i + 1 == profiles.length)
        // part = Bytes.combine(part, new byte[] { 0x46, 0x41, 0x52, 0x34 });
        // FileIO.write(part, (Path.of(saveDirectory.toString(),
        // String.valueOf(i))).toString());
        // }
    }

    private void convertTextureActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_convertTextureActionPerformed
        byte[] texture = null;
        try
        {
            texture = TextureImporter.getTexture();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Texture failed to convert!",
                "Texture Importer",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (texture == null)
            return;

        File save = FileChooser.openFile("image.tex", "tex", true);
        if (save == null)
            return;

        FileIO.write(texture, save.getAbsolutePath());
    }// GEN-LAST:event_convertTextureActionPerformed

    private void collectAllItemDependenciesActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_collectAllItemDependenciesActionPerformed
        DebugCallbacks.CollectDependencies(".plan");
    }// GEN-LAST:event_collectAllItemDependenciesActionPerformed

    private void collectAllLevelDependenciesActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_collectAllLevelDependenciesActionPerformed
        DebugCallbacks.CollectDependencies(".bin");
    }// GEN-LAST:event_collectAllLevelDependenciesActionPerformed

    private void customCollectorActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_customCollectorActionPerformed
        String extension = JOptionPane.showInputDialog(Toolkit.INSTANCE, "File extension",
            ".plan");
        if (extension == null)
            return;
        DebugCallbacks.CollectDependencies(extension);
    }// GEN-LAST:event_customCollectorActionPerformed

    private void renameFolderActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_renameFolderActionPerformed
        FileNode node = ResourceSystem.getSelected();
        FileNode[] selected = ResourceSystem.getAllSelected();
        String parent = node.getFilePath() + node.getName();

        String newFolder = JOptionPane.showInputDialog(Toolkit.INSTANCE, "Folder", parent);
        if (newFolder == null)
            return;
        newFolder = Strings.cleanupPath(newFolder);
        if (newFolder.equals(parent) || newFolder.isEmpty())
            return;

        FileData database = node.getSource();

        for (FileNode child : selected)
        {
            FileEntry entry = child.getEntry();
            if (entry != null)
            {
                String folder = entry.getFolder();
                folder = newFolder + folder.substring(parent.length());
                entry.setPath(folder + "/" + child.getName());
            }
        }

        node.removeAnyEmptyNodes();

        database.setHasChanges();

        ResourceSystem.reloadSelectedModel();
        JTree tree = node.getSource().getTree();
        TreePath path = new TreePath(Nodes.addFolder((FileNode) tree.getModel().getRoot(),
            newFolder));
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);

        Toolkit.INSTANCE.updateWorkspace();
    }// GEN-LAST:event_renameFolderActionPerformed

    private void editMenuDeleteActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_editMenuDeleteActionPerformed
        DatabaseCallbacks.delete();
    }// GEN-LAST:event_editMenuDeleteActionPerformed

    private void loadProfileBackupActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_loadProfileBackupActionPerformed
        String directoryString = FileChooser.openDirectory();
        if (directoryString == null)
            return;
        File directory = new File(directoryString);
        Pattern regex = Pattern.compile("BIG\\d+");
        File[] fragments = directory.listFiles((dir, name) -> regex.matcher(name).matches());
        if (fragments.length == 0)
        {
            JOptionPane.showMessageDialog(this, "Couldn't find a profile backup in this " +
                                                "directory!",
                "An error occurred", JOptionPane.ERROR_MESSAGE);
            return;
        }
        byte[][] data = new byte[fragments.length + 1][];
        data[fragments.length] = new byte[] { 0x46, 0x41, 0x52, 0x34 };
        for (int i = 0; i < fragments.length; ++i)
        {
            byte[] fragment = FileIO.read(fragments[i].getAbsolutePath());
            if (i + 1 == fragments.length)
                fragment = Arrays.copyOfRange(fragment, 0, fragment.length - 4);
            data[i] = Crypto.XXTEA(fragment, true);
        }
        
        File save = FileChooser.openFile("bigfart", null, true);
        if (save == null) return;

        FileIO.write(Bytes.combine(data), save.getAbsolutePath());
        ProfileCallbacks.loadProfile(save);
    }// GEN-LAST:event_loadProfileBackupActionPerformed

    private void manageProfileActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_manageProfileActionPerformed
        ProfileManager manager = new ProfileManager(this);
        manager.setVisible(true);
        Config.save();
    }// GEN-LAST:event_manageProfileActionPerformed

    private void manageArchivesActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_manageArchivesActionPerformed
        ArchiveManager manager = new ArchiveManager(this);
        manager.setVisible(true);
    }// GEN-LAST:event_manageArchivesActionPerformed

    private void exportAsBackupGUIDActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportAsBackupGUIDActionPerformed
        exportAsBackupTpl(ExportMode.GUID);
    }// GEN-LAST:event_exportAsBackupGUIDActionPerformed

    private void exportAsModCustomActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportAsModCustomActionPerformed
        new AssetExporter(ResourceSystem.getSelected().getEntry()).setVisible(true);
    }// GEN-LAST:event_exportAsModCustomActionPerformed

    private void openGfxCompilerActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_openGfxCompilerActionPerformed
        new GfxGUI().setVisible(true);
    }// GEN-LAST:event_openGfxCompilerActionPerformed

    private void exportWorldActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportWorldActionPerformed

    }// GEN-LAST:event_exportWorldActionPerformed

    private void exportSceneGraphActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportSceneGraphActionPerformed

    }// GEN-LAST:event_exportSceneGraphActionPerformed

    private void loadVitaProfileActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_loadVitaProfileActionPerformed
        String folder = FileChooser.openDirectory();
        if (folder == null)
            return;
        Pattern bigRegex = Pattern.compile("bigfart\\d+");
        File[] fragments =
            new File(folder).listFiles((dir, name) -> bigRegex.matcher(name).matches());
        if (fragments.length == 0)
        {
            JOptionPane.showMessageDialog(this, "No Vita BigProfile's were found!", "An " +
                                                                                    "error " +
                                                                                    "occurred",
                JOptionPane.ERROR_MESSAGE);
            return;
        }


        SaveArchive[] archives = new SaveArchive[fragments.length];
        HashMap<Integer, SaveArchive> archiveIDs = new HashMap<>(fragments.length);
        SaveArchive master = null;

        for (int i = 0; i < fragments.length; ++i)
        {
            File fragment = fragments[i];
            SaveArchive archive = null;
            try
            {
                archive = new SaveArchive(fragment);

                // Not entirely sure how Vita decides which profile gets the root resource,
                // so we'll just go based on biggest ID that actually has a root resource.
                if (archive.getKey().getRootType() == ResourceType.BIG_PROFILE)
                {
                    if (master != null)
                    {
                        if (archive.getID() > master.getID())
                            master = archive;
                    }
                    else master = archive;
                }

            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(this, ex.getMessage(), fragment.getName(),
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            archives[i] = archive;
            archiveIDs.put(archive.getID(), archive);
        }

        if (master == null)
        {
            JOptionPane.showMessageDialog(this, "No valid RBigProfile found in any archive!", "An error occurred", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (SaveArchive archive : archives)
        {
            if (archive == master) continue;
            master.add(archive);
        }

        // Patch all moon level data into master archive
        Pattern moonRegex = Pattern.compile("moon\\d+_\\d+");
        fragments =
            new File(folder).listFiles((dir, name) -> moonRegex.matcher(name).matches());
        ArrayList<File> extras = new ArrayList<>(Arrays.asList(fragments));

        // Downloads may be a bit excessive depending on the profile,
        // so let's just prompt the user if they want to load them
        Pattern downloadRegex = Pattern.compile("slot\\d+_\\d+");
        fragments =
            new File(folder).listFiles((dir, name) -> downloadRegex.matcher(name).matches());


        ArrayList<SaveArchive> downloadedLevels = new ArrayList<>();
        if (fragments.length != 0)
        {
            boolean shouldLoadDownloads = JOptionPane.showConfirmDialog(this, "Do you want " +
                                                                              "to " +
                                                                              "load " +
                                                                              "downloaded " +
                                                                              "slots?",
                "Load", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            if (shouldLoadDownloads)
            {
                for (File file : fragments)
                {
                    SaveArchive archive = null;
                    try
                    {
                        archive = new SaveArchive(file);
                    }
                    catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), file.getName(),
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
        
                    downloadedLevels.add(archive);
                }
            }
        }

        for (File file : extras)
        {
            SaveArchive archive = null;
            try
            {
                archive = new SaveArchive(file);
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(this, ex.getMessage(), file.getName(),
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            master.add(archive);
        }

        BigSave profile = null;
        try
        {
            profile = new BigSave(new File(folder), master, downloadedLevels);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "An error occurred",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        Toolkit.INSTANCE.addTab(profile);
        Toolkit.INSTANCE.updateWorkspace();
    }// GEN-LAST:event_loadVitaProfileActionPerformed

    private void exportPaletteContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportPaletteContextActionPerformed
        RPalette palette = ResourceSystem.getSelectedResource();
        if (palette == null)
            return;

        FileEntry entry = ResourceSystem.getSelected().getEntry();
        ResourceInfo info = entry.getInfo();

        File file = FileChooser.openFile(entry.getName() + ".bin", "bin", true);
        if (file == null)
            return;

        Revision revision = info.getRevision();
        byte compressionFlags = info.getCompressionFlags();

        RLevel level = new RLevel();

        PWorld world = level.worldThing.getPart(Part.WORLD);
        ArrayList<Thing> things = world.things;

        for (ResourceDescriptor descriptor : palette.planList)
        {
            byte[] planData = ResourceSystem.extract(descriptor);
            if (planData == null)
                continue;
            RPlan plan = new SerializedResource(planData).loadResource(RPlan.class);
            Thing[] planThings = plan.getThings();
            for (Thing thing : planThings)
            {
                if (thing == null)
                    continue;
                thing.UID = ++world.thingUIDCounter;
                things.add(thing);
            }
        }

        byte[] levelData = SerializedResource.compress(level.build(revision, compressionFlags));
        FileIO.write(levelData, file.getAbsolutePath());
    }// GEN-LAST:event_exportPaletteContextActionPerformed

    private void fixDependencyTableActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_fixDependencyTableActionPerformed
        File file = FileChooser.openFile("resource.bin", null, false);
        if (file == null)
            return;

        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data == null)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Failed to read file, is it " +
                                                            "protected?",
                "Calculate Dependency Table", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SerializedResource resource = null;
        try
        {
            resource = new SerializedResource(data);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Failed to deserialize resource!",
                "Calculate Dependency Table", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Serializer serializer = resource.getSerializer();
        Class<? extends Serializable> type = resource.getResourceType().getCompressable();
        if (type == null)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "This resource is unsupported!",
                "Calculate Dependency Table", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try
        {
            serializer.struct(null, type);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Failed to deserialize resource!",
                "Calculate Dependency Table", JOptionPane.ERROR_MESSAGE);
            return;
        }

        data = SerializedResource.compress(new SerializationData(
            resource.getStream().getBuffer(),
            resource.getRevision(),
            resource.getCompressionFlags(),
            resource.getResourceType(),
            resource.getSerializationType(),
            serializer.getDependencies()));

        File out = FileChooser.openFile(file.getName(), null, true);
        if (out != null)
            FileIO.write(data, out.getAbsolutePath());
    }// GEN-LAST:event_fixDependencyTableActionPerformed

    private void newTextureContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_newTextureContextActionPerformed
        byte[] texture = null;
        try
        {
            texture = TextureImporter.getTexture();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Texture failed to convert!",
                "Texture Importer",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (texture == null)
            return;

        DatabaseCallbacks.newEntry(texture);
    }// GEN-LAST:event_newTextureContextActionPerformed

    private void newAnimationContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_newAnimationContextActionPerformed
        File file = FileChooser.openFile("animation.glb", "glb", false);
        if (file == null)
            return;

        byte[] resource = null;
        try
        {
            AnimationImporter importer = new AnimationImporter(file.getAbsolutePath());
            RAnimation animation = importer.getAnimation();
            resource = SerializedResource.compress(animation.build(new Revision(0x132),
                CompressionFlags.USE_NO_COMPRESSION));
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Animation failed to convert!",
                "Animation Importer",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (resource == null)
            return;

        DatabaseCallbacks.newEntry(resource);
    }// GEN-LAST:event_newAnimationContextActionPerformed

    private void exportJSONContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_exportJSONContextActionPerformed
        FileEntry selected = ResourceSystem.getSelected().getEntry();
        if (selected == null)
            return;
        ResourceInfo info = selected.getInfo();
        if (info == null)
            return;

        if (info.getType() == ResourceType.TRANSLATION)
        {
            ExportCallbacks.exportTranslations();
            return;
        }

        byte[] data = ResourceSystem.extract(selected);
        if (data == null)
            return;

        SerializedResource resource = null;
        try
        {
            resource = new SerializedResource(data);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Failed to process resource, could not " +
                                                "export.",
                "An error occurred",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        File file = FileChooser.openFile(
            Strings.setExtension(selected.getName(), "json"),
            "json",
            true);

        if (file == null)
            return;

        WrappedResource wrapper = null;
        try
        {
            wrapper = new WrappedResource(resource);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "An error occurred wrapping resource, could" +
                                                " not " +
                                                "export.",
                "An error occurred", JOptionPane.ERROR_MESSAGE);
        }

        if (wrapper != null)
            FileIO.write(wrapper.toJSON(), file.getAbsolutePath());
    }// GEN-LAST:event_exportJSONContextActionPerformed

    private byte[] loadWrappedResource()
    {
        File file = FileChooser.openFile("resource.json", "json", false);
        if (file == null)
            return null;

        byte[] data = null;
        try
        {
            WrappedResource wrapper = GsonUtils.fromJSON(
                FileIO.readString(Path.of(file.getAbsolutePath())),
                WrappedResource.class);

            data = wrapper.build();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "An error occurred loaded wrapped resource," +
                                                " could" +
                                                " not import.",
                "An error occurred", JOptionPane.ERROR_MESSAGE);
        }

        return data;
    }

    private void importJSONContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_importJSONContextActionPerformed
        byte[] data = this.loadWrappedResource();
        if (data == null)
            return;
        DatabaseCallbacks.newEntry(data);
    }// GEN-LAST:event_importJSONContextActionPerformed

    private void replaceJSONContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_replaceJSONContextActionPerformed
        FileNode selected = ResourceSystem.getSelected();
        FileEntry entry = selected.getEntry();
        ResourceInfo info = entry.getInfo();

        byte[] data = null;
        if (info.getType() == ResourceType.TRANSLATION)
        {
            File file = FileChooser.openFile("translations.json", "json", false);
            if (file == null)
                return;

            RTranslationTable table = null;
            try
            {
                table = RTranslationTable.fromJSON(file.getAbsolutePath());
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(this, "An error occurred loaded " +
                                                    "translations, could" +
                                                    " not import.",
                    "An error occurred", JOptionPane.ERROR_MESSAGE);
                return;
            }

            data = table.build();
        }
        else
            data = this.loadWrappedResource();

        if (data == null)
            return;
        ResourceSystem.replace(ResourceSystem.getSelected().getEntry(), data);
    }// GEN-LAST:event_replaceJSONContextActionPerformed

    private void manageSettingsActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_manageSettingsActionPerformed
        new SettingsManager(this).setVisible(true);
        Config.save();
    }// GEN-LAST:event_manageSettingsActionPerformed

    private void remapDatabaseContextActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_remapDatabaseContextActionPerformed
        File file = FileChooser.openFile("blurayguids.remap", "remap", false);
        if (file == null)
            return;

        RemapDB remap = null;
        try
        {
            remap = new RemapDB(file);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "An error occurred",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        FileDB database = ResourceSystem.getSelectedDatabase();
        for (RemapDBRow row : remap)
        {
            FileDBRow source = database.get(row.getFrom());
            if (source == null)
                continue;
            source.setPath(row.getPath());
            source.setGUID(row.getTo());
        }

        ResourceSystem.reloadSelectedModel();
    }// GEN-LAST:event_remapDatabaseContextActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_jMenuItem1ActionPerformed
        new BigProfileGUI().setVisible(true);
    }// GEN-LAST:event_jMenuItem1ActionPerformed

    public void populateMetadata(RPlan item)
    {
        if (item == null || !ResourceSystem.canExtract())
            return;
        InventoryItemDetails details = item.inventoryData;
        if (details == null)
            return;

        iconField.setText("");
        if (details.icon != null)
            loadImage(details.icon, item);

        if (ResourceSystem.getSelected().getEntry().getInfo().getResource() != item)
            return;

        setPlanDescriptions(details);

        if (details.type.isEmpty())
            pageCombo.setSelectedItem(InventoryObjectType.NONE);
        else
            pageCombo.setSelectedItem(details.type.iterator().next());
        subCombo.setText(InventoryObjectSubType.getTypeString(details.type, details.subType));

        if (details.creator != null)
            creatorField.setText(details.creator.toString());
        else
            creatorField.setText("");

        entryModifiers.setEnabledAt(1, true);
        entryModifiers.setSelectedIndex(1);
    }

    public void setPlanDescriptions(InventoryItemDetails metadata)
    {
        titleField.setText("" + metadata.titleKey);
        descriptionField.setText("" + metadata.descriptionKey);

        locationField.setText("" + metadata.location);
        categoryField.setText("" + metadata.category);

        RTranslationTable LAMS = ResourceSystem.getLAMS();
        if (LAMS != null)
        {
            StringMetadata.setEnabled(true);
            StringMetadata.setSelected(true);

            metadata.translatedTitle = LAMS.translate(metadata.titleKey);
            metadata.translatedDescription = LAMS.translate(metadata.descriptionKey);
            metadata.translatedCategory = LAMS.translate(metadata.category);
            metadata.translatedLocation = LAMS.translate(metadata.location);

            titleField.setText(metadata.translatedTitle);
            descriptionField.setText(metadata.translatedDescription);
            locationField.setText(metadata.translatedLocation);
            categoryField.setText(metadata.translatedCategory);
        }
        else
        {
            LAMSMetadata.setSelected(true);
            LAMSMetadata.setEnabled(true);
            StringMetadata.setEnabled(false);
        }

        if (metadata.userCreatedDetails != null && metadata.titleKey == 0 && metadata.descriptionKey == 0)
        {
            StringMetadata.setEnabled(true);
            StringMetadata.setSelected(true);
            if (metadata.userCreatedDetails.name != null)
                titleField.setText(metadata.userCreatedDetails.name);

            if (metadata.userCreatedDetails.description != null)
                descriptionField.setText(metadata.userCreatedDetails.description);

            locationField.setText("");
            categoryField.setText("");
        }
    }

    public void loadImage(ResourceDescriptor resource, RPlan item)
    {
        if (resource == null)
            return;
        iconField.setText(resource.toString());
        FileEntry entry = ResourceSystem.get(resource);
        if (entry == null)
            return;

        byte[] data = ResourceSystem.extract(resource);
        if (data == null)
            return;
        RTexture texture = new RTexture(data);
        setImage(texture.getImageIcon(320, 320));
    }

    public void setImage(ImageIcon image)
    {
        if (image == null)
        {
            texture.setText("No preview to be displayed");
            texture.setIcon(null);
        }
        else
        {
            texture.setText(null);
            texture.setIcon(image);
        }
    }

    public void setEditorPanel(FileNode node)
    {
        FileEntry entry = node.getEntry();

        if (entry == null)
        {
            entryTable.setValueAt(node.getFilePath() + node.getName(), 0, 1);
            for (int i = 1; i < 8; ++i)
                entryTable.setValueAt("N/A", i, 1);
            return;
        }

        entryTable.setValueAt(entry.getPath(), 0, 1);

        String timestamp = "N/A";
        if (entry instanceof FileDBRow row)
            timestamp = new Timestamp(row.getDate() * 1000L).toString();
        else if (entry instanceof SaveEntry row && row.isItem())
        {
            InventoryItem item = row.getItem();
            if (item.details != null)
                timestamp = new Timestamp(item.details.dateAdded * 1000L).toString();
        }

        entryTable.setValueAt(timestamp, 1, 1);
        entryTable.setValueAt(entry.getSHA1(), 2, 1);
        entryTable.setValueAt(entry.getSize(), 3, 1);

        GUID guid = (GUID) entry.getKey();
        if (guid != null)
        {
            entryTable.setValueAt(guid.toString(), 4, 1);
            entryTable.setValueAt(Bytes.toHex((int) guid.getValue()), 5, 1);
            entryTable.setValueAt(Bytes.toHex(Bytes.packULEB128(guid.getValue())), 6, 1);
        }
        else
        {
            entryTable.setValueAt("N/A", 4, 1);
            entryTable.setValueAt("N/A", 5, 1);
            entryTable.setValueAt("N/A", 6, 1);
        }

        ResourceInfo info = entry.getInfo();
        if (info != null && info.getType() != ResourceType.INVALID && info.getRevision() != null)
            entryTable.setValueAt(Bytes.toHex(info.getRevision().getHead()), 7, 1);
        else
            entryTable.setValueAt("N/A", 7, 1);
    }

    public void setHexEditor(byte[] bytes)
    {
        if (bytes == null)
        {
            hex.setData(null);
            hex.setDefinitionStatus(JHexView.DefinitionStatus.UNDEFINED);
            hex.setEnabled(false);
        }
        else
        {
            hex.setData(new SimpleDataProvider(bytes));
            hex.setDefinitionStatus(JHexView.DefinitionStatus.DEFINED);
            hex.setEnabled(true);
        }
        hex.repaint();
    }

    public Toolkit run(String[] args)
    {
        for (String arg : args)
        {
            if (arg.endsWith(".farc"))
                ArchiveCallbacks.loadFileArchive(new File(arg));
            if (arg.endsWith(".map"))
                DatabaseCallbacks.loadFileDB(new File(arg));
            if (arg.contains("bigfart"))
                ProfileCallbacks.loadProfile(new File(arg));
        }
        return this;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton LAMSMetadata;
    private javax.swing.JRadioButton StringMetadata;
    private javax.swing.JMenuItem addFile;
    private javax.swing.JMenuItem addFolder;
    public javax.swing.JMenu archiveMenu;
    private javax.swing.JSpinner cameraPosX;
    private javax.swing.JSpinner cameraPosY;
    private javax.swing.JSpinner cameraPosZ;
    private javax.swing.JTextField categoryField;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JMenu changeResourceRevisionGroup;
    private javax.swing.JMenuItem changeResourceRevisionLBP1Context;
    private javax.swing.JMenuItem changeResourceRevisionLBP2Context;
    private javax.swing.JMenuItem changeResourceRevisionLBP3Context;
    private javax.swing.JMenuItem clear;
    private javax.swing.JMenuItem closeTab;
    private javax.swing.JMenuItem collectAllItemDependencies;
    private javax.swing.JMenuItem collectAllLevelDependencies;
    private javax.swing.JMenu collectionD;
    private javax.swing.JMenu collectorPresets;
    private javax.swing.JTextArea console;
    private javax.swing.JScrollPane consoleContainer;
    private javax.swing.JPopupMenu consolePopup;
    private javax.swing.JMenuItem convertTexture;
    private javax.swing.JMenu copyGroup;
    private javax.swing.JMenuItem createFileArchive;
    private javax.swing.JTextField creatorField;
    private javax.swing.JLabel creatorLabel;
    private javax.swing.JMenuItem customCollector;
    private javax.swing.JMenu databaseMenu;
    public javax.swing.JMenu debugMenu;
    private javax.swing.JMenuItem decompressResource;
    private javax.swing.JMenuItem deleteContext;
    private javax.swing.JMenu dependencyGroup;
    public javax.swing.JTree dependencyTree;
    private javax.swing.JScrollPane dependencyTreeContainer;
    private javax.swing.JTextArea descriptionField;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JSplitPane details;
    private javax.swing.JMenuItem dumpRLST;
    private javax.swing.JPopupMenu.Separator dumpSep;
    private javax.swing.JMenuItem duplicateContext;
    private javax.swing.JMenuItem editGUIDContext;
    private javax.swing.JMenu editGroup;
    private javax.swing.JMenuItem editHashContext;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editMenuDelete;
    private javax.swing.JMenuItem editPathContext;
    private javax.swing.JMenuItem editProfileItems;
    private javax.swing.JMenuItem editProfileSlots;
    private javax.swing.JPopupMenu entryContext;
    public javax.swing.JTabbedPane entryModifiers;
    public javax.swing.JTable entryTable;
    private javax.swing.JMenuItem exportAnimation;
    private javax.swing.JMenuItem exportAsBackup;
    private javax.swing.JMenuItem exportAsBackupGUID;
    private javax.swing.JMenuItem exportAsMod;
    private javax.swing.JMenuItem exportAsModCustom;
    private javax.swing.JMenuItem exportAsModGUID;
    private javax.swing.JMenu exportBackupGroup;
    private javax.swing.JMenuItem exportDDS;
    private javax.swing.JMenuItem exportGLTF;
    private javax.swing.JMenu exportGroup;
    private javax.swing.JMenuItem exportJSONContext;
    private javax.swing.JMenu exportModGroup;
    private javax.swing.JMenu exportModelGroup;
    private javax.swing.JMenu exportOBJ;
    private javax.swing.JMenuItem exportOBJTEXCOORD0;
    private javax.swing.JMenuItem exportOBJTEXCOORD1;
    private javax.swing.JMenuItem exportOBJTEXCOORD2;
    private javax.swing.JMenuItem exportPNG;
    private javax.swing.JMenuItem exportPaletteContext;
    private javax.swing.JMenuItem exportSceneGraph;
    private javax.swing.JMenu exportTextureGroupContext;
    private javax.swing.JMenuItem exportWorld;
    private javax.swing.JMenuItem extractBigProfile;
    private javax.swing.JMenuItem extractContext;
    private javax.swing.JMenuItem extractDecompressedContext;
    private javax.swing.JMenu extractGroup;
    private javax.swing.JMenuItem fileArchiveIntegrityCheck;
    private javax.swing.JSplitPane fileDataPane;
    public javax.swing.JTabbedPane fileDataTabs;
    public javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem fixDependencyTable;
    private javax.swing.JMenu gamedataMenu;
    private javax.swing.JMenuItem generateDiff;
    private tv.porst.jhexview.JHexView hex;
    private javax.swing.JPanel hierachyPanel;
    private javax.swing.JTextField iconField;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JMenuItem importJSONContext;
    private javax.swing.JPanel infoCardPanel;
    private javax.swing.JPanel inspectorPane;
    private javax.swing.JMenuItem installProfileMod;
    private javax.swing.JPanel itemMetadata;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JMenuItem loadArchive;
    public javax.swing.JMenuItem loadBigProfile;
    public javax.swing.JMenuItem loadDB;
    private javax.swing.JMenu loadGroupMenu;
    private javax.swing.JMenuItem loadMod;
    private javax.swing.JMenuItem loadProfileBackup;
    private javax.swing.JMenuItem loadVitaProfile;
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JMenuItem manageArchives;
    private javax.swing.JMenuItem manageProfile;
    private javax.swing.JMenuItem manageSettings;
    private javax.swing.JMenu menuFileMenu;
    private javax.swing.JMenuItem mergeFARCs;
    private javax.swing.ButtonGroup metadataButtonGroup;
    public javax.swing.JMenu modMenu;
    private javax.swing.JMenuBar navigation;
    private javax.swing.JMenuItem newAnimationContext;
    private javax.swing.JMenuItem newEntryContext;
    private javax.swing.JMenu newEntryGroup;
    public javax.swing.JMenu newFileDBGroup;
    private javax.swing.JMenuItem newFolderContext;
    private javax.swing.JMenu newGamedataGroup;
    private javax.swing.JMenu newItemGroup;
    private javax.swing.JMenuItem newLegacyDB;
    private javax.swing.JMenuItem newMod;
    private javax.swing.JMenuItem newModelContext;
    private javax.swing.JMenuItem newModernDB;
    private javax.swing.JMenu newResourceGroup;
    private javax.swing.JMenuItem newStickerContext;
    private javax.swing.JMenuItem newTextureContext;
    private javax.swing.JMenuItem newVitaDB;
    private javax.swing.JMenuItem openCompressinator;
    private javax.swing.JMenuItem openGfxCompiler;
    public javax.swing.JMenuItem openModMetadata;
    public javax.swing.JSplitPane overviewPane;
    private javax.swing.JComboBox<String> pageCombo;
    private javax.swing.JMenuItem patchMAP;
    private javax.swing.JSplitPane previewContainer;
    private javax.swing.JMenu profileMenu;
    public javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem reboot;
    private javax.swing.JMenuItem remapDatabaseContext;
    private javax.swing.JMenuItem removeDependenciesContext;
    private javax.swing.JMenuItem removeMissingDependenciesContext;
    private javax.swing.JMenuItem renameFolder;
    private javax.swing.JTabbedPane renderPane;
    private javax.swing.JMenuItem replaceCompressedContext;
    private javax.swing.JMenuItem replaceDecompressedContext;
    private javax.swing.JMenuItem replaceDependenciesContext;
    private javax.swing.JMenu replaceGroup;
    private javax.swing.JMenuItem replaceImageContext;
    private javax.swing.JMenuItem replaceJSONContext;
    private javax.swing.JTabbedPane resourceTabs;
    public javax.swing.JMenuItem saveAs;
    private javax.swing.JPopupMenu.Separator saveDivider;
    public javax.swing.JMenuItem saveMenu;
    public javax.swing.JMenu savedataMenu;
    private javax.swing.JPanel scenePanel;
    public javax.swing.JTextField search;
    private javax.swing.JTextField subCombo;
    private javax.swing.JMenuItem swapProfilePlatform;
    private javax.swing.JScrollPane tableContainer;
    public javax.swing.JLabel texture;
    private javax.swing.JTextField titleField;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JSplitPane treeContainer;
    private javax.swing.JSplitPane workspace;
    private javax.swing.JMenuItem zeroContext;
    // End of variables declaration//GEN-END:variables
}
