package toolkit;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;

import toolkit.configurations.Config;
import toolkit.utilities.ResourceSystem;
import toolkit.windows.Toolkit;

import java.awt.EventQueue;

public class Launcher {
    public static void main(String args[]) {
        LafManager.install(new DarculaTheme());
        Config.initialize();
        ResourceSystem.GUI_MODE = true;
        EventQueue.invokeLater(() -> new Toolkit().run(args).setVisible(true));
    }
}
