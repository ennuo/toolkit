package toolkit.windows.utilities;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import toolkit.utilities.SlowOp;

public class SlowOpGUI extends javax.swing.JDialog {
    private SlowOp operation;
    private boolean wantQuit = false;
    private int code;
    
    private SlowOpGUI(Frame parent, SlowOp operation) {
        super(parent, true);
        this.initComponents();
        
        this.setLocationRelativeTo(parent);
        this.operation = operation;
        
        this.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) { return; }
        });
    }
    
    public static void performSlowOperation(Frame parent, String message, int max, SlowOp operation) {
        SlowOpGUI gui = new SlowOpGUI(parent, operation);
        gui.statusLabel.setText(message);

        gui.progressBar.setMaximum(max);
        gui.progressBar.setIndeterminate(max == -1);

        SwingWorker<Void, Void> sw = new SwingWorker<Void,Void>() {
            @Override protected Void doInBackground() throws Exception {
                gui.code = operation.run(gui);
                return null;
            }

            @Override protected void done() {
                gui.dispose();
            }
        };

        sw.execute();
        gui.setVisible(true);
        
        if (gui.code != 0)
            JOptionPane.showMessageDialog(parent, "An error occurred during the operation!", "SlowOp Task", JOptionPane.ERROR_MESSAGE);
        else
            JOptionPane.showMessageDialog(parent, "Success!", "SlowOp Task", JOptionPane.INFORMATION_MESSAGE); 
    }
    
    public void setProgress(int progress) { this.progressBar.setValue(progress); }
    public boolean wantQuit() { return this.wantQuit; }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        progressBar = new javax.swing.JProgressBar();
        statusLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("SlowOp Task");
        setResizable(false);

        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        statusLabel.setText("Performing Operation...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                    .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(statusLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
}
