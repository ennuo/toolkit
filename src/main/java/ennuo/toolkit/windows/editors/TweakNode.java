package ennuo.toolkit.windows.editors;

import javax.swing.tree.DefaultMutableTreeNode;

public class TweakNode extends DefaultMutableTreeNode implements TweakContext {
    public TweakNode(Object userObject) { super(userObject); }

    public void context() { return; }
    public void draw(TweakMenu tweak) { return; }
}
