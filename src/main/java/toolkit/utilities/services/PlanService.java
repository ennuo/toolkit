package toolkit.utilities.services;

import cwlib.resources.RPlan;
import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.io.serializer.Serializer;
import cwlib.types.databases.FileEntry;
import cwlib.types.data.ResourceReference;
import toolkit.utilities.ResourceSystem;
import toolkit.windows.Toolkit;

import javax.swing.JTree;

public class PlanService implements ResourceService  {
    public static final int[] HEADERS = { 0x504c4e62 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        RPlan plan = entry.getResource("item");
        if (plan == null) {
            try { 
                plan = new RPlan(new Resource(data));
                entry.setResource("item", plan); 
            }
            catch (Exception e) { 
                System.err.println("There was an error processing RPlan file.");
                return;
            }
        }
        
        if (ResourceSystem.lastSelected.entry != entry || plan == null || Toolkit.instance.getCurrentTree() != tree) return;
        
        if (plan.details != null)
            Toolkit.instance.populateMetadata(plan);
        else {
            System.out.println("Attempting to guess icon of RPlan, this may not be accurate.");
            try {
                for (ResourceReference dependency : entry.dependencies) {
                    if (dependency.type == ResourceType.TEXTURE)
                        Toolkit.instance.loadImage(dependency, plan);
                }
            } catch (Exception e) { System.err.println("An error occurred processing texture."); }
            System.out.println("Couldn't find any texture file to display as icon.");
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
