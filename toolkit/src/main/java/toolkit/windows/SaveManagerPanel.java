package toolkit.windows;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import cwlib.enums.ResourceType;
import cwlib.io.Resource;
import cwlib.resources.RBigProfile;
import cwlib.resources.RLocalProfile;
import cwlib.types.archives.FartManyRO;
import cwlib.types.archives.SaveArchive;
import cwlib.types.swing.SaveDataTreeNode;
import cwlib.util.Nodes;

public class SaveManagerPanel extends javax.swing.JPanel 
{
    private static enum InventorySortMode
    {
        List,
        Type,
        Category,
        Theme
    };
    
    /**
     * How nodes in the inventory list are sorted.
     */
    private InventorySortMode _currentInventorySortMode = InventorySortMode.Category;

    private SaveArchive _cache;

    /**
     * The currently loaded big profile.
     */
    private RBigProfile _bpr;

    /**
     * The currently loaded local profile.
     */
    private RLocalProfile _ipr;

    /**
     * The raw node list for the inventory in the big profile.
     */
    private ArrayList<SaveDataTreeNode> _nodes = new ArrayList<>();
    private ArrayList<DefaultMutableTreeNode> _strings = new ArrayList<>();

    protected SaveManagerPanel() 
    {
        initComponents();

        _jInventoryTree.setRootVisible(false);
        

    }

    private void setupStringTableNodes()
    {
        _strings.clear();
        _strings.ensureCapacity(_bpr.stringTable.size());
        for (int i = 0; i < _bpr.stringTable.size(); ++i)
        {
            var string = _bpr.stringTable.get(i);
            if (string.isEmpty()) string = "None";
            _strings.add(new DefaultMutableTreeNode(string));
        }
    }

    private void sortInventoryNodes()
    {
        var root = (DefaultMutableTreeNode)(_jInventoryTree.getModel().getRoot());
        root.removeAllChildren();
        switch (_currentInventorySortMode)
        {
            case List:
            {
                System.out.println("sorting");
                for (var node : _nodes)
                    root.insert(node, root.getChildCount());
                break;
            }
            case Category:
            {
                setupStringTableNodes();
                for (var node : _nodes)
                {
                    var category = _strings.get(node.getItem().details.categoryIndex);
                    category.insert(node, category.getChildCount());
                }

                for (var node : _strings)
                {
                    if (node.getChildCount() == 0) continue;
                    root.insert(node, root.getChildCount());
                }
            }
        }

        ((DefaultTreeModel)_jInventoryTree.getModel()).reload();
    }

    public static SaveManagerPanel loadVitaProfile(File savegame)
    {
        var mgr = new SaveManagerPanel();

        savegame = new File(savegame, "output/savegame");
        if (!savegame.exists())
            throw new RuntimeException("The savegame path specified did not contain a valid save!");

        var files = savegame.listFiles();
        var archiveIDs = new HashMap<Integer, SaveArchive>();
        var archives = new ArrayList<SaveArchive>(files.length);

        for (var file : files)
        {
            // No idea why Vita even writes a USRDIR folder here,
            // it doesn't get used for anything.
            if (file.isDirectory()) continue;

            var name = file.getName();
            var path = file.getAbsolutePath();

            if (name.startsWith("bigfart"))
            {
                var cache = new SaveArchive(path);
                if (cache.getKey().getRootType() == ResourceType.BIG_PROFILE)
                {
                    if (mgr._cache != null)
                    {
                        if (cache.getID() > mgr._cache.getID())
                            mgr._cache = cache;
                    }
                    else mgr._cache = cache;
                }

                archives.add(cache);
                archiveIDs.put(cache.getID(), cache);
            }
        }

        for (var archive : archives)
        {
            if (archive == mgr._cache) continue;
            mgr._cache.add(archive);
        }

        mgr._bpr = mgr._cache.loadResource(mgr._cache.getKey().getRootHash(), RBigProfile.class);


        mgr._nodes = new ArrayList<>(mgr._bpr.inventory.size());
        for (var item : mgr._bpr.inventory)
            mgr._nodes.add(new SaveDataTreeNode(item));
        mgr.sortInventoryNodes();

        return mgr;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _jRoot = new javax.swing.JTabbedPane();
        _jInventoryPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        _jInventoryScrollPane = new javax.swing.JScrollPane();
        _jInventoryTree = new javax.swing.JTree();
        itemManagerApplet1 = new toolkit.windows.ItemManagerApplet();
        _jLevelListPanel = new javax.swing.JPanel();
        _jDownloadedLevelsPanel = new javax.swing.JPanel();
        _jFileListPanel = new javax.swing.JPanel();

        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jSplitPane1.setDividerLocation(280);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        _jInventoryTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        _jInventoryTree.setScrollsOnExpand(false);
        _jInventoryScrollPane.setViewportView(_jInventoryTree);

        jSplitPane1.setLeftComponent(_jInventoryScrollPane);
        jSplitPane1.setRightComponent(itemManagerApplet1);

        javax.swing.GroupLayout _jInventoryPanelLayout = new javax.swing.GroupLayout(_jInventoryPanel);
        _jInventoryPanel.setLayout(_jInventoryPanelLayout);
        _jInventoryPanelLayout.setHorizontalGroup(
            _jInventoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
        _jInventoryPanelLayout.setVerticalGroup(
            _jInventoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        _jRoot.addTab("Inventory", _jInventoryPanel);

        javax.swing.GroupLayout _jLevelListPanelLayout = new javax.swing.GroupLayout(_jLevelListPanel);
        _jLevelListPanel.setLayout(_jLevelListPanelLayout);
        _jLevelListPanelLayout.setHorizontalGroup(
            _jLevelListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 871, Short.MAX_VALUE)
        );
        _jLevelListPanelLayout.setVerticalGroup(
            _jLevelListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 497, Short.MAX_VALUE)
        );

        _jRoot.addTab("Level List", _jLevelListPanel);

        javax.swing.GroupLayout _jDownloadedLevelsPanelLayout = new javax.swing.GroupLayout(_jDownloadedLevelsPanel);
        _jDownloadedLevelsPanel.setLayout(_jDownloadedLevelsPanelLayout);
        _jDownloadedLevelsPanelLayout.setHorizontalGroup(
            _jDownloadedLevelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 871, Short.MAX_VALUE)
        );
        _jDownloadedLevelsPanelLayout.setVerticalGroup(
            _jDownloadedLevelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 497, Short.MAX_VALUE)
        );

        _jRoot.addTab("Downloaded Levels", _jDownloadedLevelsPanel);

        javax.swing.GroupLayout _jFileListPanelLayout = new javax.swing.GroupLayout(_jFileListPanel);
        _jFileListPanel.setLayout(_jFileListPanelLayout);
        _jFileListPanelLayout.setHorizontalGroup(
            _jFileListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 871, Short.MAX_VALUE)
        );
        _jFileListPanelLayout.setVerticalGroup(
            _jFileListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 497, Short.MAX_VALUE)
        );

        _jRoot.addTab("File List", _jFileListPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(_jRoot)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(_jRoot)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel _jDownloadedLevelsPanel;
    private javax.swing.JPanel _jFileListPanel;
    private javax.swing.JPanel _jInventoryPanel;
    private javax.swing.JScrollPane _jInventoryScrollPane;
    private javax.swing.JTree _jInventoryTree;
    private javax.swing.JPanel _jLevelListPanel;
    private javax.swing.JTabbedPane _jRoot;
    private toolkit.windows.ItemManagerApplet itemManagerApplet1;
    private javax.swing.JSplitPane jSplitPane1;
    // End of variables declaration//GEN-END:variables
}
