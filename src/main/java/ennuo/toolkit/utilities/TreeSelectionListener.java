package ennuo.toolkit.utilities;

import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.enums.SerializationMethod;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.toolkit.utilities.services.*;
import ennuo.toolkit.utilities.services.ResourceService;
import ennuo.toolkit.windows.Toolkit;
import java.util.HashMap;
import javax.swing.JTree;

public class TreeSelectionListener {
    public static HashMap<Integer, ResourceService> services = new HashMap<>();
    static {
        TreeSelectionListener.addService(new LevelService());
        TreeSelectionListener.addService(new TextureService());
        TreeSelectionListener.addService(new PackService());
        TreeSelectionListener.addService(new SlotService());
        TreeSelectionListener.addService(new MeshService());
        TreeSelectionListener.addService(new AnimationService());
        TreeSelectionListener.addService(new GfxMaterialService());
        TreeSelectionListener.addService(new PlanService());
    }
    
    public static void addService(ResourceService service) {
        for (int header : service.getSupportedHeaders())
            services.put(header, service);
    }
    
    public static void listener(JTree tree) {
        Toolkit toolkit = Toolkit.instance;
        toolkit.setImage(null);
        JTree currentTree = toolkit.getCurrentTree();
        
        if (tree == currentTree) {
            toolkit.entryModifiers.setEnabledAt(1, false);
            toolkit.entryModifiers.setSelectedIndex(0);
        }
        if (tree == currentTree)
            toolkit.dependencyTree.setModel(null);
        if (tree.getSelectionPath() == null)
            return;

        FileNode node = toolkit.getLastSelected(tree);
        FileEntry entry = node.entry;

        toolkit.setEditorPanel(node);
        if (entry == null) {
            toolkit.updateWorkspace();
            return;
        }

        toolkit.resourceService.submit(() -> {
            if (!Globals.canExtract()) return;

            byte[] extractedData = Globals.extractFile(entry.hash);
            if (extractedData == null && 
                    toolkit.getCurrentDB().USRDIR != null && 
                    Globals.currentWorkspace == Globals.WorkspaceType.MAP) {
                    System.out.println("Attempting to extract from disk...");
                    extractedData = FileIO.read(toolkit.getCurrentDB().USRDIR + entry.path.replace("/", "\\"));
            }
            
            entry.data = extractedData;
            toolkit.updateWorkspace();
            toolkit.setHexEditor(extractedData);
            
            if (extractedData == null || extractedData.length < 4) return;
            
            int magic = (extractedData[0] & 0xFF) << 24 | 
                        (extractedData[1] & 0xFF) << 16 | 
                        (extractedData[2] & 0xFF) << 8 | 
                        (extractedData[3] & 0xFF) << 0;
            
            Resource resource = new Resource(extractedData);
            if (resource.method == SerializationMethod.BINARY && resource.type != ResourceType.STATIC_MESH)
                entry.canReplaceDecompressed = true;
            entry.revision = resource.revision;
            entry.compressionFlags = resource.compressionFlags;
            entry.dependencies = resource.dependencies;
            
            if (entry.dependencyModel == null || entry.dependencies == null || entry.hasMissingDependencies) {
                FileModel model = new FileModel(new FileNode("x", null, entry));
                boolean recursive = !(resource.type == ResourceType.PACKS || 
                                    resource.type == ResourceType.SLOT_LIST || 
                                    resource.type == ResourceType.LEVEL || 
                                    resource.type == ResourceType.ADVENTURE_CREATE_PROFILE || 
                                    resource.type == ResourceType.PALETTE);
                
                entry.hasMissingDependencies = resource.registerDependencies(recursive) != 0;
                
                toolkit.generateDependencyTree(entry, model);
                entry.dependencyModel = model;
            }

            if (Globals.lastSelected == node && entry.dependencyModel != null && tree == currentTree)
                toolkit.dependencyTree.setModel(entry.dependencyModel);
            
            toolkit.setEditorPanel(node);
            
            if (services.containsKey(magic))
                ((ResourceService)services.get(magic)).process(tree, entry, extractedData);
       });
    }
}
