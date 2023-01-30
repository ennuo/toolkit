package toolkit.windows.utilities;

import cwlib.enums.ResourceType;
import cwlib.singleton.ResourceSystem;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.types.swing.FileData;
import cwlib.util.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class ResourcePicker extends javax.swing.JDialog {
    private final ResourceType type;
    private ResourceDescriptor selected;
    private boolean submit;
    private final TableRowSorter<TableModel> sorter;
    
    private ResourcePicker(JFrame frame, ResourceType type) {
        super(frame, true);
        this.initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        this.setTitle("Resource Picker");
        this.setLocationRelativeTo(frame);
        this.getRootPane().setDefaultButton(this.selectButton);
        this.type = type;
        
        this.sorter = new TableRowSorter<>(this.resourceTable.getModel());
        this.resourceTable.setRowSorter(this.sorter);
        
        this.resourceTable.getColumnModel().getSelectionModel().addListSelectionListener(event -> {
            int index = this.resourceTable.getSelectedRow();
            this.selectButton.setEnabled(index != -1);
            if (index == -1) return;
            
            if (index == 0) {
                this.selected = null;
                return;
            }
            
            index = this.resourceTable.convertRowIndexToModel(index);
            GUID guid = (GUID) this.resourceTable.getModel().getValueAt(index, 0);
            this.selected = new ResourceDescriptor(guid, this.type);
        });
        
        this.searchText.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onTextChange(); }
            @Override public void removeUpdate(DocumentEvent e) { onTextChange(); }
            @Override public void changedUpdate(DocumentEvent e) { return; }
        });
        
        this.initialize();
        
        this.setVisible(true);
    }
    
    private void initialize() {
        DefaultTableModel model = (DefaultTableModel) this.resourceTable.getModel();
        
        this.resourceTable.getColumnModel().getColumn(0).setPreferredWidth(15);
        this.resourceTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        
        model.addRow(new Object[] { "N/A", "<No Resource>" });
        HashMap<GUID, String> unique = new HashMap<>();
        
        FileData database = ResourceSystem.getSelectedDatabase();
        if (database == null) return;
        
        for (FileData data : ResourceSystem.getDatabases()) {
            if (data == database || (!(data instanceof FileDB))) continue;
            for (FileDBRow row : (FileDB) data)
                unique.put(row.getGUID(), row.getPath());
        }
        
        if (database instanceof FileDB) {
            for (FileDBRow row : (FileDB) database) 
                unique.put(row.getGUID(), row.getPath());
        }
        
        ArrayList<GUID> guids = new ArrayList<>(unique.keySet());
        guids.sort((a, z) -> Long.compareUnsigned(a.getValue(), z.getValue()));
        for (GUID guid : guids) {
            String path = unique.get(guid);
            if (path.endsWith(this.type.getExtension()))
               model.addRow(new Object[] { guid, path });
        }
    }
    
    private void onTextChange() {
        String text = this.searchText.getText().toLowerCase();
        
        RowFilter<TableModel, Integer> filter = new RowFilter<>() {
            @Override
            public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
                if (entry.getStringValue(0).equals("N/A")) return true;
                return (entry.getStringValue(1).toLowerCase().contains(text));
            }
        };
        
        if (text.length() == 0) sorter.setRowFilter(null);
        else sorter.setRowFilter(filter);
    }
    
    public static ResourceDescriptor getResource(JFrame frame, ResourceDescriptor descriptor, ResourceType type) {
        ResourcePicker picker = new ResourcePicker(frame, type);
        return picker.submit ? picker.selected : descriptor;
    }
    
    public static String getResourceString(JFrame frame, String text, ResourceType type) {
        ResourceDescriptor descriptor = null;
        if (Strings.isGUID(text) || Strings.isSHA1(text))
            descriptor = new ResourceDescriptor(text, ResourceType.TEXTURE);
        descriptor = ResourcePicker.getResource(frame, descriptor, type);
        if (descriptor == null) return "";
        return descriptor.toString();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchLabel = new javax.swing.JLabel();
        searchText = new javax.swing.JTextField();
        resourceScrollPane = new javax.swing.JScrollPane();
        resourceTable = new javax.swing.JTable();
        selectButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        searchLabel.setText("Search:");

        resourceTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "GUID", "Path"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        resourceTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        resourceTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        resourceTable.getTableHeader().setReorderingAllowed(false);
        resourceScrollPane.setViewportView(resourceTable);

        selectButton.setText("Select");
        selectButton.setEnabled(false);
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resourceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 631, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(searchLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchText))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchLabel)
                    .addComponent(searchText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resourceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        this.submit = true;
        this.dispose();
    }//GEN-LAST:event_selectButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane resourceScrollPane;
    private javax.swing.JTable resourceTable;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchText;
    private javax.swing.JButton selectButton;
    // End of variables declaration//GEN-END:variables
}
