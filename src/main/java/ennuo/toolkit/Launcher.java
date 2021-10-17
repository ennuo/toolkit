package ennuo.toolkit;

import com.bulenkov.darcula.DarculaLaf;
import ennuo.toolkit.windows.Toolkit;
import java.awt.EventQueue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Launcher {
    public static void main(String args[]) {
        try { UIManager.setLookAndFeel(new DarculaLaf()); } 
        catch (UnsupportedLookAndFeelException ex) { 
            System.out.println("There was an error setting the Darcula theme."); 
        }
        EventQueue.invokeLater(() -> new Toolkit().setVisible(true));
    }
}
