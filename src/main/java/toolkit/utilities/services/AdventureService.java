package toolkit.utilities.services;

import cwlib.enums.ResourceKeys;
import cwlib.resources.RAdventureCreateProfile;
import cwlib.types.Resource;
import cwlib.types.databases.FileEntry;
import javax.swing.JTree;

public class AdventureService implements ResourceService  {
    public static final int[] HEADERS = { 0x41444362 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        RAdventureCreateProfile profile = entry.getResource(ResourceKeys.RESOURCE);
        if (profile == null) {
            try { 
                profile = new RAdventureCreateProfile(new Resource(data));
                entry.setResource("adventure", profile);
            }
            catch (Exception e) { System.err.println("There was an error processing RAdventureCreateProfile file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
