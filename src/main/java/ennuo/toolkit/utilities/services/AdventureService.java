package ennuo.toolkit.utilities.services;

import ennuo.craftworld.resources.AdventureCreateProfile;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.FileEntry;
import javax.swing.JTree;

public class AdventureService implements ResourceService  {
    public static final int[] HEADERS = { 0x41444362 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        AdventureCreateProfile profile = entry.getResource("adventure");
        if (profile == null) {
            try { 
                profile = new AdventureCreateProfile(new Resource(data));
                entry.setResource("adventure", profile);
            }
            catch (Exception e) { System.err.println("There was an error processing RAdventureCreateProfile file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
