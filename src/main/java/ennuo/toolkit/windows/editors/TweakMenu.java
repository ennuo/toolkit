package ennuo.toolkit.windows.editors;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

public class TweakMenu extends javax.swing.JDialog {
    public TweakMenu(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        this.initComponents();
        this.setTitle("Tweak Menu | tapris.plan");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        this.tree.addTreeSelectionListener((e) -> {
            this.tweakContent.removeAll();
            TweakNode node = this.getLastSelected();
            if (node == null) return;
            node.draw(this);
            this.tweakContent.revalidate();
            this.tweakContent.repaint();
        });
        
        this.createTestData();
    }
    
    private void createTestData() {
        TweakNode root = new TweakNode("Root");
        TweakModel model = new TweakModel(root);
        
        TweakNode details = new TweakNode("Inventory Details");
        root.add(details);
        
        details.add(new TweakNode("Resources") {
            @Override
            public void draw(TweakMenu tweak) {
                tweak.createNamedTextbox("Icon", "hblah");
            }
        });
        
        details.add(new TweakNode("Type / Subtype") {
            @Override
            public void draw(TweakMenu tweak) {
                System.out.println("trying to shit");
                tweak.createNamedTextbox("Bazinga", "wtf");
            }
        });
        
        details.add(new TweakNode("Title / Description") {
            @Override
            public void draw(TweakMenu tweak) {
                System.out.println("trying to shit");
                tweak.createNamedTextbox("Bazinga", "wtf");
            }
        });
        
        details.add(new TweakNode("Editor List") {
            @Override
            public void draw(TweakMenu tweak) {
                System.out.println("trying to shit");
                tweak.createNamedTextbox("Bazinga", "wtf");
            }
        });
        
        details.add(new TweakNode("Photo Data") {
            @Override
            public void draw(TweakMenu tweak) {
                System.out.println("trying to shit");
                tweak.createNamedTextbox("Bazinga", "wtf");
            }
        });
        
        details.add(new TweakNode("Eyetoy Data") {
            @Override
            public void draw(TweakMenu tweak) {
                System.out.println("trying to shit");
                tweak.createNamedTextbox("Bazinga", "wtf");
            }
        });
        
        details.add(new TweakNode("Flags") {
            @Override
            public void draw(TweakMenu tweak) {
                System.out.println("trying to shit");
                tweak.createNamedTextbox("Bazinga", "wtf");
            }
        });
        
        details.add(new TweakNode("Additional Data") {
            @Override
            public void draw(TweakMenu tweak) {
                System.out.println("trying to shit");
                tweak.createSeparator("Flags");
                tweak.createNamedTextbox("Bazinga", "wtf");
                tweak.createNamedTextbox("Bazinga", "wtf");
                tweak.createNamedTextbox("Bazinga", "wtf");
                tweak.createNamedTextbox("Bazinga", "wtf");
                tweak.createNamedTextbox("Bazinga", "wtf");
                tweak.createNamedTextbox("Bazinga", "wtf");
                tweak.createNamedTextbox("Bazinga", "wtf");
                tweak.createNamedTextbox("Bazinga", "wtf");
                
            }
        });
        
        
        this.tree.setModel(model);
    }
    
    public void createSeparator(String text) {
        JLabel label = new JLabel();
        label.setText(text);
        this.tweakContent.add(label);
        this.tweakContent.add(new JSeparator());
    }
    
    public void createNamedTextbox(String text, Object value) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        
        JLabel label = new JLabel();
        label.setText(text);
        JTextField box = new JTextField();
        if (value != null)
            box.setText(value.toString());
        panel.add(label);
        panel.add(box);
        
        this.tweakContent.add(panel);
    }
    
    private TweakNode getLastSelected() {
        TreePath[] treePaths = tree.getSelectionPaths();
        if (treePaths == null) return null;
        for (int i = 0; i < treePaths.length; ++i) {
            TweakNode node = (TweakNode) treePaths[i].getLastPathComponent();
            if (node != null) return node;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolsMenu = new javax.swing.JPanel();
        applyChangesButton = new javax.swing.JButton();
        confirmButton = new javax.swing.JButton();
        treeScrollPane = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        contentScrollPane = new javax.swing.JScrollPane();
        tweakContent = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        toolsMenu.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        applyChangesButton.setText("Apply");
        applyChangesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onApplyChanges(evt);
            }
        });

        confirmButton.setText("OK");
        confirmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onClose(evt);
            }
        });

        javax.swing.GroupLayout toolsMenuLayout = new javax.swing.GroupLayout(toolsMenu);
        toolsMenu.setLayout(toolsMenuLayout);
        toolsMenuLayout.setHorizontalGroup(
            toolsMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toolsMenuLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(confirmButton, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(applyChangesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        toolsMenuLayout.setVerticalGroup(
            toolsMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolsMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(toolsMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(applyChangesButton)
                    .addComponent(confirmButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tree.setModel(null);
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) this.tree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
        this.tree.setRootVisible(false);
        treeScrollPane.setViewportView(tree);

        contentScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        contentScrollPane.setMinimumSize(new java.awt.Dimension(246, 226));

        tweakContent.setMaximumSize(new java.awt.Dimension(244, 244));
        tweakContent.setMinimumSize(new java.awt.Dimension(244, 244));
        tweakContent.setLayout(new javax.swing.BoxLayout(tweakContent, javax.swing.BoxLayout.Y_AXIS));
        contentScrollPane.setViewportView(tweakContent);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolsMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(treeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(treeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(contentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(toolsMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    
    
    
    
    private void onApplyChanges(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onApplyChanges
        
        
        this.dispose();
    }//GEN-LAST:event_onApplyChanges

    private void onClose(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onClose
        this.dispose();
    }//GEN-LAST:event_onClose

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyChangesButton;
    private javax.swing.JButton confirmButton;
    protected javax.swing.JScrollPane contentScrollPane;
    private javax.swing.JPanel toolsMenu;
    protected javax.swing.JTree tree;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JPanel tweakContent;
    // End of variables declaration//GEN-END:variables
}
