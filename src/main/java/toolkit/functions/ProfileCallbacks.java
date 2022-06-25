package toolkit.functions;

import cwlib.util.FileIO;
import cwlib.types.Resource;
import cwlib.types.save.BigSave;
import toolkit.utilities.FileChooser;
import toolkit.utilities.ResourceSystem;
import toolkit.windows.Toolkit;

import java.io.File;

public class ProfileCallbacks {
    public static void loadProfile() {
        File file = FileChooser.openFile("bigfart1", null, false);
        loadProfile(file);
    }

    public static void loadProfile(File file) {if (file != null) {
            BigSave profile = new BigSave(file);
            Toolkit.instance.addTab(profile);
            Toolkit.instance.updateWorkspace();
        }
    }
    
    public static void extractProfile() {
        File file = FileChooser.openFile("profile.bpr", "bpr", true);
        if (file == null) return;
        BigSave save = ResourceSystem.getSelectedDatabase();
        byte[] entry = save.getArchive().extract(save.getArchive().getKey().getRootHash());
        FileIO.write(new Resource(entry).getStream().getBuffer(), file.getAbsolutePath());
    }
    
    public static void addKey() {                                       


    } 
}
