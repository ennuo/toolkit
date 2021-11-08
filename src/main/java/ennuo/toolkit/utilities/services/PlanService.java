package ennuo.toolkit.utilities.services;

import ennuo.craftworld.resources.Plan;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.windows.Toolkit;
import javax.swing.JTree;

public class PlanService implements ResourceService  {
    public static final int[] HEADERS = { 0x504c4e62 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        Plan plan = entry.getResource("item");
        if (plan == null) {
            Resource resource = new Resource(data);
            resource.decompress(true);
            try { 
                plan = new Serializer(resource).struct(null, Plan.class); 
                entry.setResource("item", plan); 
            }
            catch (Exception e) { 
                System.err.println("There was an error processing RPlan file.");
                return;
            }
        }
        
        if (Globals.lastSelected.entry != entry || plan == null || Toolkit.instance.getCurrentTree() != tree) return;
        
        if (plan.details != null)
            Toolkit.instance.populateMetadata(plan);
        else {
            System.out.println("Attempting to guess icon of RPlan, this may not be accurate.");
            try {
                for (FileEntry dependency : entry.dependencies) {
                    if (dependency.path.contains(".tex")) {
                        ResourceDescriptor descriptor = new ResourceDescriptor();
                        descriptor.hash = dependency.SHA1;
                        Toolkit.instance.loadImage(descriptor, plan);
                    }
                }
            } catch (Exception e) { System.err.println("An error occurred processing texture."); }
            System.out.println("Couldn't find any texture file to display as icon.");
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
