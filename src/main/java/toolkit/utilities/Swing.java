package toolkit.utilities;

import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class Swing {
    
    public static JMenuItem createMenuItem(String name, ActionListener event, JMenu parent) {
        JMenuItem item = Swing.createMenuItem(name, event);
        parent.add(item);
        return item;
    }
    
    public static JMenuItem createMenuItem(String name, ActionListener event) {
        JMenuItem item = new JMenuItem();
        item.setText(name);
        item.addActionListener(event);
        return item;
    }
}
