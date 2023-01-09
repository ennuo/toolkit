package toolkit.functions;

import cwlib.util.FileIO;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.types.save.BigSave;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;

import java.io.File;

import javax.swing.JOptionPane;

public class ProfileCallbacks {
    public static void loadProfile() {
        File file = FileChooser.openFile("bigfart1", null, false);
        loadProfile(file);
    }

    public static void loadProfile(File file) {
        if (file != null) {
            BigSave profile = null;
            try { profile = new BigSave(file); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(Toolkit.INSTANCE, ex.getMessage(), "An error occurred", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Toolkit.INSTANCE.addTab(profile);
            Toolkit.INSTANCE.updateWorkspace();
        }
    }
    
    public static void extractProfile() {
        File file = FileChooser.openFile("profile.bpr", "bpr", true);
        if (file == null) return;
        BigSave save = ResourceSystem.getSelectedDatabase();
        byte[] entry = save.getArchive().extract(save.getArchive().getKey().getRootHash());
        FileIO.write(new Resource(entry).getStream().getBuffer(), file.getAbsolutePath());
    }
}
