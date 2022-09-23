package toolkit;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;

import configurations.Config;
import cwlib.singleton.ResourceSystem;
import executables.Jsoninator;
import executables.gfx.GfxGUI;
import toolkit.windows.Toolkit;

import java.awt.EventQueue;
import java.util.Arrays;

public class Launcher {
    public static void main(String args[]) {
        Config.initialize();
        
        // Executable switcher
        if (args.length != 0) {
            String mode = args[0];
            String[] programArgs = Arrays.copyOfRange(args, 1, args.length);
            
            switch (mode) {
                case "--jsoninator": case "-j": {
                    Jsoninator.main(programArgs);
                    return;
                }
                case "--gfx": case "-g": {
                    GfxGUI.main(programArgs);
                    return;
                }
            }
            
            // If argument isn't a switcher, pass it to Toolkit::run
        }

        // If no switcher is provided, use Toolkit GUI.
        LafManager.install(new DarculaTheme());
        ResourceSystem.GUI_MODE = true;
        EventQueue.invokeLater(() -> new Toolkit().run(args).setVisible(true));
    }
}
