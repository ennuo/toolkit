package toolkit.windows.bundlers;

import cwlib.io.serializer.SerializationData;
import cwlib.structs.texture.CellGcmTexture;
import cwlib.types.Resource;
import cwlib.util.Bytes;
import cwlib.util.FileIO;
import cwlib.util.Images;
import gr.zdimensions.jsquish.Squish.CompressionType;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;

public class TextureImporter extends javax.swing.JDialog {
    private BufferedImage image;
    private byte[] dds;
    private boolean isDDS;
    private boolean submit = false;
    
    private TextureImporter() {
        super(Toolkit.INSTANCE, true);
        this.initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        this.setLocationRelativeTo(Toolkit.INSTANCE);
        this.getRootPane().setDefaultButton(this.importButton);
        this.setVisible(true);
    }
    
    public static byte[] getTexture() {
        TextureImporter importer = new TextureImporter();
        if (importer.image == null || !importer.submit) return null;
        
        boolean noSRGB = importer.noSRGBCheckbox.isSelected();
        boolean genMips = importer.mipmapsCheckbox.isSelected();
        boolean useGTF = importer.gtfCheckbox.isSelected();
        String format = (String) importer.textureFormatCombo.getSelectedItem();
        
        if (importer.isDDS) {
            byte[] textureData = importer.dds;

            if (useGTF) {
                CellGcmTexture info = new CellGcmTexture(textureData, noSRGB);
                textureData = Arrays.copyOfRange(textureData, 0x80, textureData.length);

                return Resource.compress(new SerializationData(textureData, info));
            }

            if (noSRGB) 
                textureData = Bytes.combine(textureData, "BUMP".getBytes());
            return Resource.compress(new SerializationData(textureData));
        }

        CompressionType type = null;
        switch (format) {
            case "DXT1": type = CompressionType.DXT1; break;
            case "DXT3": type = CompressionType.DXT3; break;
            case "DXT5": type = CompressionType.DXT5; break;
        }

        if (useGTF)
            return Images.toGTF(importer.image, type, noSRGB, genMips);

        return Images.toTEX(importer.image, type, noSRGB, genMips);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        imageLabel = new javax.swing.JLabel();
        openFileButton = new javax.swing.JButton();
        textureFormatLabel = new javax.swing.JLabel();
        textureFormatCombo = new javax.swing.JComboBox<>();
        noSRGBCheckbox = new javax.swing.JCheckBox();
        importButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        noSRGBHintLabel = new javax.swing.JLabel();
        gtfCheckbox = new javax.swing.JCheckBox();
        gtfHintLabel = new javax.swing.JLabel();
        mipmapsCheckbox = new javax.swing.JCheckBox();
        mipmapsHintLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Texture Importer");

        imageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageLabel.setText("No image available");

        openFileButton.setText("Open...");
        openFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileButtonActionPerformed(evt);
            }
        });

        textureFormatLabel.setText("Texture Format:");
        textureFormatLabel.setToolTipText("DDS compression method for texture, if unsure leave as DXT5");

        textureFormatCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "DXT1", "DXT3", "DXT5" }));
        textureFormatCombo.setSelectedIndex(2);
        textureFormatCombo.setToolTipText("");

        noSRGBCheckbox.setText("noSRGB");
        noSRGBCheckbox.setToolTipText("");

        importButton.setText("Import");
        importButton.setEnabled(false);
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        noSRGBHintLabel.setText("If importing a bump/normal map, select this!");

        gtfCheckbox.setText("GTF");

        gtfHintLabel.setText("Export as LBP2/3 GTF texture");

        mipmapsCheckbox.setSelected(true);
        mipmapsCheckbox.setText("Mipmaps");

        mipmapsHintLabel.setText("Precompute mips, if unsure, leave selected");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(imageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                    .addComponent(openFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(importButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(mipmapsCheckbox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(textureFormatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(noSRGBCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(gtfCheckbox)))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textureFormatCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(noSRGBHintLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(gtfHintLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(mipmapsHintLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(textureFormatLabel)
                            .addComponent(textureFormatCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(noSRGBCheckbox)
                            .addComponent(noSRGBHintLabel))
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gtfHintLabel)
                            .addComponent(gtfCheckbox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(mipmapsCheckbox)
                            .addComponent(mipmapsHintLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openFileButton)
                    .addComponent(importButton)
                    .addComponent(cancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileButtonActionPerformed
        File file = FileChooser.openFile("image.png", "png,jpg,jpeg,dds", false);
        if (file == null) return;
        
        byte[] imageData = FileIO.read(file.getAbsolutePath());
        if (imageData == null) {
            JOptionPane.showMessageDialog(this, "Failed to read file, does it exist?", "Texture Importer", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        this.isDDS = file.getAbsolutePath().toLowerCase().endsWith(".dds");
        if (this.isDDS) this.image = Images.fromDDS(imageData);
        else {
            try (ByteArrayInputStream stream = new ByteArrayInputStream(imageData)) {
                this.image = ImageIO.read(stream);
            } catch (IOException ioex) {
                // IO exception shouldn't be able to occur since we're using
                // a byte array input stream.
            }
        }
        
        if (this.image == null) {
            JOptionPane.showMessageDialog(this, "Failed to load image, is it valid?", "Texture Importer", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (this.isDDS) {
            // Storing the original DDS data to avoid
            // having to re-generate it.
            this.dds = imageData;
            
            // Can't adjust generation options
            // since we already have the DDS.
            this.mipmapsCheckbox.setEnabled(false);
            this.textureFormatCombo.setEnabled(false);
        } else {
            this.mipmapsCheckbox.setEnabled(true);
            this.textureFormatCombo.setEnabled(true);
        }
        
        this.importButton.setEnabled(true);
        this.imageLabel.setText(null);
        this.imageLabel.setIcon(Images.getImageIcon(this.image, 128, 128));
    }//GEN-LAST:event_openFileButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.submit = false;
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        this.submit = true;
        this.dispose();
    }//GEN-LAST:event_importButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox gtfCheckbox;
    private javax.swing.JLabel gtfHintLabel;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JButton importButton;
    private javax.swing.JCheckBox mipmapsCheckbox;
    private javax.swing.JLabel mipmapsHintLabel;
    private javax.swing.JCheckBox noSRGBCheckbox;
    private javax.swing.JLabel noSRGBHintLabel;
    private javax.swing.JButton openFileButton;
    private javax.swing.JComboBox<String> textureFormatCombo;
    private javax.swing.JLabel textureFormatLabel;
    // End of variables declaration//GEN-END:variables
}
