package toolkit.utilities;

import javax.swing.*;
import java.awt.event.ActionListener;

public class Swing
{
    public static JMenuItem createMenuItem(String name, String tooltip, ActionListener event,
                                           JComponent parent)
    {
        JMenuItem item = Swing.createMenuItem(name, event, parent);
        item.setToolTipText(tooltip);
        return item;
    }

    public static JMenuItem createMenuItem(String name, ActionListener event, JComponent parent)
    {
        JMenuItem item = Swing.createMenuItem(name, event);
        parent.add(item);
        return item;
    }

    public static JMenuItem createMenuItem(String name, ActionListener event)
    {
        JMenuItem item = new JMenuItem();
        item.setText(name);
        item.addActionListener(event);
        return item;
    }
}
