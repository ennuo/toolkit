package toolkit.utilities;

import cwlib.util.Bytes;
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
        Toolkit toolkit = Toolkit.instance;
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
            toolkit.updateWorkspace();
            return;
        }

        // ResourceSystem.getResourceService().submit(() -> {
            if (!ResourceSystem.canExtract()) return;

            byte[] data = ResourceSystem.extract(entry);

            toolkit.updateWorkspace();
            toolkit.setHexEditor(data);
            
            if (data == null || data.length < 4) return;
            
            int magic = Bytes.toIntegerBE(data);

            if (entry.getInfo() == null) {
                ResourceSystem.println("Loading " + entry.getPath());
                entry.setInfo(new ResourceInfo(data));
            }
            
            // if (entry.dependencyModel == null || entry.dependencies == null || entry.hasMissingDependencies) {
            //     FileModel model = new FileModel(new FileNode("x", null, entry));
            //     boolean recursive = !(resource.type == ResourceType.PACKS || 
            //                         resource.type == ResourceType.SLOT_LIST || 
            //                         resource.type == ResourceType.LEVEL || 
            //                         resource.type == ResourceType.ADVENTURE_CREATE_PROFILE || 
            //                         resource.type == ResourceType.PALETTE);
                
            //     entry.hasMissingDependencies = resource.registerDependencies(recursive) != 0;
                
            //     toolkit.generateDependencyTree(entry, model);
            //     entry.dependencyModel = model;
            // }

            // if (ResourceSystem.getSelected() == node && entry.dependencyModel != null && tree == currentTree)
            //     toolkit.dependencyTree.setModel(entry.dependencyModel);
            
            toolkit.setEditorPanel(node);
            
            if (services.containsKey(magic))
                ((ResourceService)services.get(magic)).process(tree, entry, data);
        // });
    }
}
