package ennuo.toolkit;

import com.bulenkov.darcula.DarculaLaf;
import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import ennuo.toolkit.windows.Toolkit;
import java.awt.EventQueue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Launcher {
    public static void main(String args[]) {
        final String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win") || OS.contains("mac")) {
            // NOTE(Abz): Darcula crashes on Linux.
            try { UIManager.setLookAndFeel(new DarculaLaf()); } 
            catch (UnsupportedLookAndFeelException ex) { 
                System.out.println("There was an error setting the Darcula theme."); 
            }
        } else LafManager.install(new DarculaTheme()); // NOTE(Abz): I don't really like how this looks, but I don't like light themes!
        EventQueue.invokeLater(() -> new Toolkit().setVisible(true));
    }
}
