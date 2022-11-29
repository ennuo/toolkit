package toolkit.utilities.services;

import cwlib.enums.ResourceType;
import cwlib.resources.RPlan;
import cwlib.singleton.ResourceSystem;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.ResourceInfo;
import cwlib.types.databases.FileEntry;
import toolkit.windows.Toolkit;

import javax.swing.JTree;

public class PlanService implements ResourceService  {
    public static final int[] HEADERS = { 0x504c4e62 };

    @Override public void process(JTree tree, FileEntry entry, byte[] data) {
        ResourceInfo info = entry.getInfo();
        if (info == null || info.getType() != ResourceType.PLAN || info.getResource() == null);
        RPlan plan = info.getResource();
        JTree selected = ResourceSystem.getSelectedDatabase().getTree();
        if (ResourceSystem.getSelected().getEntry() != entry || selected != tree) return;

        if (plan.inventoryData == null) {
            ResourceSystem.println("Attempting to guess icon of RPlan, this may not be accurate.");
            try {
                for (ResourceDescriptor dependency : info.getDependencies()) {
                    if (dependency.getType().equals(ResourceType.TEXTURE))
                        Toolkit.INSTANCE.loadImage(dependency, plan);
                }
            } catch (Exception ex) {
                ResourceSystem.println("An error occurred processing texture for plan icon. Exiting.");
            }
        }
        else Toolkit.INSTANCE.populateMetadata(plan);
        
    }

    @Override public int[] getSupportedHeaders() { return HEADERS; }
}
