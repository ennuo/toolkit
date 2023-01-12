package toolkit.utilities;

import cwlib.util.Bytes;
import cwlib.singleton.ResourceSystem;
import cwlib.types.data.ResourceInfo;
import cwlib.types.swing.FileNode;
import cwlib.types.databases.FileEntry;
import toolkit.utilities.services.*;
import toolkit.windows.Toolkit;

import java.util.HashMap;
import javax.swing.JTree;

public class TreeSelectionListener {
    public static HashMap<Integer, ResourceService> services = new HashMap<>();
    static { 
        TreeSelectionListener.addService(new TextureService()); 
        TreeSelectionListener.addService(new PlanService());
    }
    public static void addService(ResourceService service) {
        for (int header : service.getSupportedHeaders())
            services.put(header, service);
    }
    
    public static void listener(JTree tree) {
        ResourceSystem.setCanExtractSelected(false);

        Toolkit toolkit = Toolkit.INSTANCE;
        toolkit.setImage(null);
        JTree currentTree = ResourceSystem.getSelectedDatabase().getTree();
        
        if (tree == currentTree) {
            toolkit.entryModifiers.setEnabledAt(1, false);
            toolkit.entryModifiers.setSelectedIndex(0);
        }
        if (tree == currentTree)
            toolkit.dependencyTree.setModel(null);
        if (tree.getSelectionPath() == null)
            return;

        FileNode node = ResourceSystem.updateSelections(tree);
        FileEntry entry = node.getEntry();

        toolkit.setEditorPanel(node);
        if (entry == null) {
            if (ResourceSystem.getAllSelected().length > 1)
                ResourceSystem.setCanExtractSelected(true);
            toolkit.updateWorkspace();
            return;
        }

        ResourceSystem.getResourceService().submit(() -> {
            if (!ResourceSystem.canExtract()) return;

            byte[] data = ResourceSystem.extract(entry);

            toolkit.updateWorkspace();
            toolkit.setHexEditor(data);
            
            if (data == null || data.length < 4) return;
            ResourceSystem.setCanExtractSelected(true);
            
            int magic = Bytes.toIntegerBE(data);

            ResourceInfo info = entry.getInfo();
            if (info == null) {
                ResourceSystem.println("Loading " + entry.getPath());
                
                info = new ResourceInfo(node.getName(), data);
                entry.setInfo(info);

                ResourceSystem.addCache(node);
            }

            if (ResourceSystem.getSelected() == node && info.getModel() != null && tree == currentTree)
                toolkit.dependencyTree.setModel(info.getModel());
            
            toolkit.setEditorPanel(node);
            
            if (services.containsKey(magic))
                ((ResourceService)services.get(magic)).process(tree, entry, data);
            
            if (info != null)
                Toolkit.INSTANCE.generateEntryContext2(tree);
        });
    }
}
