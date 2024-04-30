package toolkit.windows;

import cwlib.resources.RBigProfile;
import cwlib.types.archives.SaveArchive;

import javax.swing.*;

public class BigProfileGUI extends javax.swing.JFrame
{

    private SaveArchive archive;
    private RBigProfile profile;

    public BigProfileGUI()
    {
        initComponents();
        setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        setLocationRelativeTo(Toolkit.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        pContentPanel = new javax.swing.JPanel();
        pTabbedPanel = new javax.swing.JTabbedPane();
        pInventoryPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jTextField1 = new javax.swing.JTextField();
        itemManagerApplet2 = new toolkit.windows.ItemManagerApplet();
        pLevelsPanel = new javax.swing.JPanel();
        pFilePanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        hex = new tv.porst.jhexview.JHexView();
        pStringPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        pMenuBar = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Big Profile");

        jList2.setModel(new javax.swing.AbstractListModel<String>()
        {
            final String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

            public int getSize()
            {
                return strings.length;
            }

            public String getElementAt(int i)
            {
                return strings[i];
            }
        });
        jScrollPane1.setViewportView(jList2);

        jTextField1.setText("jTextField1");

        javax.swing.GroupLayout itemManagerApplet2Layout =
            new javax.swing.GroupLayout(itemManagerApplet2);
        itemManagerApplet2.setLayout(itemManagerApplet2Layout);
        itemManagerApplet2Layout.setHorizontalGroup(
            itemManagerApplet2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 496, Short.MAX_VALUE)
        );
        itemManagerApplet2Layout.setVerticalGroup(
            itemManagerApplet2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pInventoryPanelLayout =
            new javax.swing.GroupLayout(pInventoryPanel);
        pInventoryPanel.setLayout(pInventoryPanelLayout);
        pInventoryPanelLayout.setHorizontalGroup(
            pInventoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pInventoryPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(pInventoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane1)
                        .addComponent(jTextField1,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 156,
                            Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(itemManagerApplet2,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
        );
        pInventoryPanelLayout.setVerticalGroup(
            pInventoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pInventoryPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(pInventoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(itemManagerApplet2,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE)
                        .addGroup(pInventoryPanelLayout.createSequentialGroup()
                            .addComponent(jTextField1,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane1,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                374
                                , Short.MAX_VALUE)))
                    .addContainerGap())
        );

        pTabbedPanel.addTab("Inventory", pInventoryPanel);

        javax.swing.GroupLayout pLevelsPanelLayout = new javax.swing.GroupLayout(pLevelsPanel);
        pLevelsPanel.setLayout(pLevelsPanelLayout);
        pLevelsPanelLayout.setHorizontalGroup(
            pLevelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 670, Short.MAX_VALUE)
        );
        pLevelsPanelLayout.setVerticalGroup(
            pLevelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 414, Short.MAX_VALUE)
        );

        pTabbedPanel.addTab("Levels", pLevelsPanel);

        jList1.setModel(new javax.swing.AbstractListModel<String>()
        {
            final String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

            public int getSize()
            {
                return strings.length;
            }

            public String getElementAt(int i)
            {
                return strings[i];
            }
        });
        jScrollPane2.setViewportView(jList1);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][] {
                { null, null },
                { null, null },
                { null, null },
                { null, null }
            },
            new String[] {
                "Field", "Value"
            }
        )
        {
            final Class[] types = new Class[] {
                java.lang.String.class, java.lang.String.class
            };
            final boolean[] canEdit = new boolean[] {
                false, false
            };

            public Class getColumnClass(int columnIndex)
            {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });
        jScrollPane3.setViewportView(jTable1);

        jLabel1.setText("Hashes");

        javax.swing.GroupLayout hexLayout = new javax.swing.GroupLayout(hex);
        hex.setLayout(hexLayout);
        hexLayout.setHorizontalGroup(
            hexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 0, Short.MAX_VALUE)
        );
        hexLayout.setVerticalGroup(
            hexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pFilePanelLayout = new javax.swing.GroupLayout(pFilePanel);
        pFilePanel.setLayout(pFilePanelLayout);
        pFilePanelLayout.setHorizontalGroup(
            pFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pFilePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(pFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2,
                            javax.swing.GroupLayout.PREFERRED_SIZE, 166,
                            javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(pFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane3,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 486,
                            Short.MAX_VALUE)
                        .addComponent(hex, javax.swing.GroupLayout.DEFAULT_SIZE,
                            486, Short.MAX_VALUE))
                    .addContainerGap())
        );
        pFilePanelLayout.setVerticalGroup(
            pFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                    pFilePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pFilePanelLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, 380
                                    , Short.MAX_VALUE))
                            .addGroup(pFilePanelLayout.createSequentialGroup()
                                .addComponent(hex,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, 276
                                    , Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane3,
                                    javax.swing.GroupLayout.PREFERRED_SIZE,
                                    120,
                                    javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
        );

        pTabbedPanel.addTab("Files", pFilePanel);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][] {
                { null, null, null, null },
                { null, null, null, null },
                { null, null, null, null },
                { null, null, null, null }
            },
            new String[] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(jTable2);

        javax.swing.GroupLayout pStringPanelLayout = new javax.swing.GroupLayout(pStringPanel);
        pStringPanel.setLayout(pStringPanelLayout);
        pStringPanelLayout.setHorizontalGroup(
            pStringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pStringPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane4,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        658, Short.MAX_VALUE)
                    .addContainerGap())
        );
        pStringPanelLayout.setVerticalGroup(
            pStringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pStringPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane4,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        402, Short.MAX_VALUE)
                    .addContainerGap())
        );

        pTabbedPanel.addTab("Strings", pStringPanel);

        javax.swing.GroupLayout pContentPanelLayout =
            new javax.swing.GroupLayout(pContentPanel);
        pContentPanel.setLayout(pContentPanelLayout);
        pContentPanelLayout.setHorizontalGroup(
            pContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pTabbedPanel)
        );
        pContentPanelLayout.setVerticalGroup(
            pContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pTabbedPanel)
        );

        jMenu1.setText("File");
        pMenuBar.add(jMenu1);

        jMenu2.setText("Edit");
        pMenuBar.add(jMenu2);

        setJMenuBar(pMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private tv.porst.jhexview.JHexView hex;
    private toolkit.windows.ItemManagerApplet itemManagerApplet2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList<String> jList1;
    private javax.swing.JList<String> jList2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel pContentPanel;
    private javax.swing.JPanel pFilePanel;
    private javax.swing.JPanel pInventoryPanel;
    private javax.swing.JPanel pLevelsPanel;
    private javax.swing.JMenuBar pMenuBar;
    private javax.swing.JPanel pStringPanel;
    private javax.swing.JTabbedPane pTabbedPanel;
    // End of variables declaration//GEN-END:variables
}
