package ennuo.toolkit.functions;

import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.BigProfile;
import ennuo.toolkit.utilities.FileChooser;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;
import javax.swing.JOptionPane;

public class ProfileCallbacks {
    public static void loadProfile() {
        File file = FileChooser.openFile("bigfart1", null, false);
        loadProfile(file);
    }

    public static void loadProfile(File file) {if (file != null) {
            BigProfile profile = new BigProfile(file);
            if (!profile.isParsed) return;
            Toolkit.instance.addTab(profile);
            Toolkit.instance.updateWorkspace();
        }
    }
    
    public static void extractProfile() {
        File file = FileChooser.openFile("profile.bpr", "bpr", true);
        if (file == null) return;
        BigProfile save = (BigProfile) Toolkit.instance.getCurrentDB();
        FileIO.write(new Resource(save.profile.data).handle.data, file.getAbsolutePath());
    }
    
    public static void addKey() {                                       
        String str = (String) JOptionPane.showInputDialog("Translated String");
        String hStr = (String) JOptionPane.showInputDialog("Hash");
        if (str == null || hStr == null || str.equals("") || hStr.equals(""))
            return;

        long hash = Long.parseLong(hStr);

        BigProfile profile = (BigProfile) Toolkit.instance.getCurrentDB();
        profile.addString(str, hash);

        profile.shouldSave = true;
        Toolkit.instance.updateWorkspace();

        System.out.println("Done!");

    } 
}
