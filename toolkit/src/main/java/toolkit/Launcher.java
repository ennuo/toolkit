package toolkit;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import configurations.Config;
import cwlib.singleton.ResourceSystem;
import toolkit.windows.Toolkit;

import java.awt.*;

public class Launcher
{
    public static void main(String[] args)
    {
        Config.initialize();
        LafManager.install(new DarculaTheme());
        ResourceSystem.GUI_MODE = true;
        EventQueue.invokeLater(() ->
        {
            new Toolkit().run(args).setVisible(true);
        });
    }
}
