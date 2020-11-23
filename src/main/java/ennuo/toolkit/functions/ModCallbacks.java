package ennuo.toolkit.functions;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.FileIO;
import ennuo.craftworld.types.Mod;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;
import javax.swing.JOptionPane;

public class ModCallbacks {
    public static Mod loadMod(File file) {
        Mod mod;
        try {
            Data data = new Data(FileIO.read(file.getAbsolutePath()), 0xFFFFFFFF);
            data.revision = 0xFFFF;
            String password = null;
            if (data.str(0x4).equals("MODe") && data.bool() == true)
                password = JOptionPane.showInputDialog(Toolkit.instance, "Mod is encrypted! Please input password.", "password");
            data.seek(0);

            mod = new Mod(file, data, password);
        } catch (Exception e) {
            return null;
        }
        return mod;
    }
}
