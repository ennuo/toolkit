package toolkit.functions;

import cwlib.util.FileIO;
import cwlib.types.Resource;
import cwlib.types.BigSave;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;

import java.io.File;
import javax.swing.JOptionPane;

public class ProfileCallbacks {
    public static void loadProfile() {
        File file = FileChooser.openFile("bigfart1", null, false);
        loadProfile(file);
    }

    public static void loadProfile(File file) {if (file != null) {
            BigSave profile = new BigSave(file);
            if (!profile.isParsed) return;
            Toolkit.instance.addTab(profile);
            Toolkit.instance.updateWorkspace();
        }
    }
    
    public static void extractProfile() {
        File file = FileChooser.openFile("profile.bpr", "bpr", true);
        if (file == null) return;
        BigSave save = (BigSave) Toolkit.instance.getCurrentDB();
        FileIO.write(new Resource(save.rootProfileEntry.data).handle.data, file.getAbsolutePath());
    }
    
    public static void addKey() {                                       
        String str = (String) JOptionPane.showInputDialog("Translated String");
        String hStr = (String) JOptionPane.showInputDialog("Hash");
        if (str == null || hStr == null || str.equals("") || hStr.equals(""))
            return;

        long hash = Long.parseLong(hStr);

        BigSave profile = (BigSave) Toolkit.instance.getCurrentDB();
        profile.addString(str, hash);

        profile.shouldSave = true;
        Toolkit.instance.updateWorkspace();

        System.out.println("Done!");

    } 
}
