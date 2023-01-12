package toolkit.utilities;

import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

public class Swing {
    public static JMenuItem createMenuItem(String name, String tooltip, ActionListener event, JComponent parent) {
        JMenuItem item = Swing.createMenuItem(name, event, parent);
        item.setToolTipText(tooltip);
        return item;
    }
    
    public static JMenuItem createMenuItem(String name, ActionListener event, JComponent parent) {
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
