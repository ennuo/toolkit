
package ennuo.toolkit.windows;

import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.utilities.Compressor;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.toolkit.utilities.Globals;
import java.nio.file.Paths;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Dependinator extends javax.swing.JFrame {
    private Resource resource;
    
    private FileEntry entry;
    
    private ResourceDescriptor[] modifications;
    
    DefaultListModel model = new DefaultListModel();
    
    public Dependinator(Toolkit toolkit, FileEntry entry) {
        initComponents();
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource("/legacy_icon.png")).getImage());
        setTitle("Dependinator");
        
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ResourceDescriptor ptr;
                int index = list.getSelectedIndex();
                if (modifications[index] != null)
                    ptr = modifications[index];
                else ptr = resource.resources[index];
                pointer.setText(ptr.toString());
                pointer.setEnabled(true);
                update.setEnabled(true);
            }
        });
        
        
        this.entry = entry;
        
        byte[] data = Globals.extractFile(entry.GUID);
        if (data == null) data = Globals.extractFile(entry.SHA1);
        
        if (data == null) {
            dispose();
            return;
        }
        
        resource = new Resource(data);
        resource.getDependencies(entry, false);
        
        modifications = new ResourceDescriptor[resource.resources.length];
        
        for (int i = 0; i < resource.resources.length; ++i) {
            ResourceDescriptor ptr = resource.resources[i];
            modifications[i] = ptr;
            FileEntry dependency = resource.dependencies[i];
            if (dependency == null) model.addElement(ptr.toString());
            else {
                if (dependency.path == null) model.addElement(ptr.toString());
                else model.addElement(Paths.get(dependency.path).getFileName().toString());
            }
        }
        setVisible(true);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        pointer = new javax.swing.JTextField();
        replace = new javax.swing.JButton();
        update = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        list.setModel(model);
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(list);

        pointer.setEnabled(false);

        replace.setText("Save");
        replace.setEnabled(false);
        replace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceActionPerformed(evt);
            }
        });

        update.setText("Update");
        update.setEnabled(false);
        update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                    .addComponent(replace, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pointer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(update, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pointer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(update))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(replace)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateActionPerformed
        int index = list.getSelectedIndex();
        
        ResourceDescriptor newRes = new ResourceDescriptor(resource.resources[index].type, pointer.getText());
        
        if (newRes.equals(resource.resources[index])) return;
        if (modifications[index] != null)
            if (newRes.equals(modifications[index])) return;
        
        modifications[index] = newRes;
        
        System.out.println("Set " + resource.resources[index].toString() + " -> " + newRes.toString());
        
        FileEntry entry = Globals.findEntry(newRes);
        if (entry == null || entry.path == null)
            model.setElementAt(newRes.toString(), index);
        else model.setElementAt(Paths.get(entry.path).getFileName().toString(), index);
        
        
        replace.setEnabled(true);
        
        
    }//GEN-LAST:event_updateActionPerformed

    private void replaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceActionPerformed
        resource.decompress(true);
        
        for (int i = 0; i < modifications.length; ++i) {
            System.out.println(modifications[i].toString() + " : " + resource.resources[i].toString());
            if (modifications[i].equals(resource.resources[i])) continue;
            resource.replaceDependency(i, modifications[i], false);
        }
        
        resource.setData(Compressor.Compress(resource.data, resource.magic, resource.revision, modifications));
        
        Globals.replaceEntry(entry, resource.data);
        
        dispose();
    }//GEN-LAST:event_replaceActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> list;
    private javax.swing.JTextField pointer;
    private javax.swing.JButton replace;
    private javax.swing.JButton update;
    // End of variables declaration//GEN-END:variables
}
