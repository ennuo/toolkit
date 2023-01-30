package executables.gfx;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;

import configurations.ApplicationFlags;
import cwlib.enums.CompressionFlags;
import cwlib.enums.GameShader;
import cwlib.enums.GfxMaterialFlags;
import cwlib.enums.ResourceType;
import cwlib.enums.ShadowCastMode;
import cwlib.enums.TextureWrap;
import cwlib.resources.RGfxMaterial;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;
import executables.gfx.GfxAssembler.BrdfPort;
import executables.gfx.dialogues.ErrorDialogue;
import executables.gfx.dialogues.TextureDialogue;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import toolkit.utilities.FileChooser;

public class GfxGUI extends javax.swing.JFrame {
    public static class TextureEntry {
        public ResourceDescriptor descriptor;
        public TextureWrap wrapS = TextureWrap.WRAP;
        public TextureWrap wrapT = TextureWrap.WRAP;
        public final int index;
        
        private TextureEntry(int index) {
            this.index = index;
            this.descriptor = null;
        }
        
        private TextureEntry(int index, ResourceDescriptor descriptor) {
            this.index = index;
            this.descriptor = descriptor;
        }
        
        @Override public String toString() {
            if (this.descriptor == null) return String.format("[%d] Empty texture slot", this.index);
            return String.format("[%d] %s", this.index, this.descriptor);
        }
    }
    
    private RGfxMaterial gmat;
    
    private DefaultListModel textureModel = new DefaultListModel();
    
    private String brdf;
    
    public GfxGUI() {
        this.initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        
        this.textureList.setModel(this.textureModel);
        
        this.reset();
        this.getRootPane().setDefaultButton(this.compileButton);
        
        if (!System.getProperty("os.name").toLowerCase().contains("win"))
            JOptionPane.showMessageDialog(this, "This program is only functional on Windows!", "Error", JOptionPane.WARNING_MESSAGE);
        
        if (!ApplicationFlags.CAN_COMPILE_CELL_SHADERS)
            JOptionPane.showMessageDialog(this, String.format("Unable to find SCE-CGC compiler! This program will not function! (Expected location is %s)", ApplicationFlags.SCE_CGC_EXECUTABLE.getAbsolutePath()), "Error", JOptionPane.WARNING_MESSAGE);
    }
    
    private void reset() {
        this.gmat = new RGfxMaterial();
        
        this.textureModel.clear();
        for (int i = 0; i < 8; ++i)
            this.textureModel.addElement(new TextureEntry(i));
        
        this.brdf = null;
        
        this.brdfShaderPathLabel.setText("Please select a CG source file!");
        
        this.update();
    }
    
    private void update() {
        // Update flag checkboxes
        this.receiveShadowCheckbox.setSelected((this.gmat.flags & GfxMaterialFlags.RECEIVE_SHADOWS) != 0);
        this.receiveSunCheckbox.setSelected((this.gmat.flags & GfxMaterialFlags.RECEIVE_SUN) != 0);
        this.receiveSpritelightCheckbox.setSelected((this.gmat.flags & GfxMaterialFlags.RECEIVE_SPRITELIGHTS) != 0);
        this.maxPriorityCheckbox.setSelected((this.gmat.flags & GfxMaterialFlags.MAX_PRIORITY) != 0);
        this.squishyCheckbox.setSelected((this.gmat.flags & GfxMaterialFlags.SQUISHY) != 0);
        this.noInstanceTextureCheckbox.setSelected((this.gmat.flags & GfxMaterialFlags.NO_INSTANCE_TEXTURE) != 0);
        this.wireCheckbox.setSelected((this.gmat.flags & GfxMaterialFlags.WIRE) != 0);
        this.furryCheckbox.setSelected((this.gmat.flags & GfxMaterialFlags.FURRY) != 0);
        this.twoSidedCheckbox.setSelected((this.gmat.flags & GfxMaterialFlags.TWO_SIDED) != 0);
        this.alphaClipCheckbox.setSelected((this.gmat.flags & GfxMaterialFlags.ALPHA_CLIP) != 0);
        
        // Update properties
        this.alphaTestLevelSpinner.setValue(this.gmat.alphaTestLevel);
        this.alphaLayerSpinner.setValue((int) (this.gmat.alphaLayer & 0xFF));
        this.shadowCastComboBox.setSelectedItem(this.gmat.shadowCastMode);
        this.bumpLevelSpinner.setValue(this.gmat.bumpLevel);
        this.cosinePowerSpinner.setValue(this.gmat.cosinePower);
        this.reflectionBlurSpinner.setValue(this.gmat.reflectionBlur);
        this.refractiveIndexSpinner.setValue(this.gmat.refractiveIndex);
        this.alphaModeCombo.setSelectedIndex(this.gmat.alphaMode & 0xff);
    }
    
    private void load() {
        File file = FileChooser.openFile("generatedmesh.gmat", "gmat", false);
        if (file == null || !file.exists()) return;
        
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if (index != -1) name = name.substring(0, index);
        
        Resource resource = null;
        try { resource = new Resource(file.getAbsolutePath()); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while processing compressed resource!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (resource.getResourceType() != ResourceType.GFX_MATERIAL) {
            JOptionPane.showMessageDialog(this, "Resource isn't of type RGfxMaterial!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        RGfxMaterial gmat = null;
        try { gmat = resource.loadResource(RGfxMaterial.class); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while reading RGfxMaterial, is resource corrupted?", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        this.reset();
        
        this.gmat = gmat;
        
        this.textureModel.clear();
        for (int i = 0; i < 8; ++i) {
            TextureEntry entry = new TextureEntry(i, this.gmat.textures[i]);
            entry.wrapS = this.gmat.wrapS[i];
            entry.wrapT = this.gmat.wrapT[i];
            this.textureModel.addElement(entry);
        }
        
        try {
            this.brdf = GfxAssembler.generateShaderSource(this.gmat, -1, true);
            
            this.brdfShaderPathLabel.setText(name + ".cg");

            if (JOptionPane.showConfirmDialog(this, "Do you want to save generated shader?", "Shader Dump", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                File dest = FileChooser.openFile(name + ".cg", "cg", true);
                if (dest != null)
                    FileIO.write(this.brdf.getBytes(), dest.getAbsolutePath());
            }

            if (this.gmat.getBoxConnectedToPort(this.gmat.getOutputBox(), BrdfPort.ALPHA_CLIP) != null)
                this.gmat.flags |= GfxMaterialFlags.ALPHA_CLIP;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid shader graph configuration! Can't generate shader.", "Error", JOptionPane.WARNING_MESSAGE);
            this.brdf = null;
        }
        
        this.update();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        gmatFlagsContainer = new javax.swing.JPanel();
        gmatFlagsLabel = new javax.swing.JLabel();
        receiveShadowCheckbox = new javax.swing.JCheckBox();
        receiveSunCheckbox = new javax.swing.JCheckBox();
        receiveSpritelightCheckbox = new javax.swing.JCheckBox();
        maxPriorityCheckbox = new javax.swing.JCheckBox();
        squishyCheckbox = new javax.swing.JCheckBox();
        noInstanceTextureCheckbox = new javax.swing.JCheckBox();
        wireCheckbox = new javax.swing.JCheckBox();
        furryCheckbox = new javax.swing.JCheckBox();
        twoSidedCheckbox = new javax.swing.JCheckBox();
        alphaClipCheckbox = new javax.swing.JCheckBox();
        propertiesContainer = new javax.swing.JPanel();
        propertiesLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        alphaTestLevelSpinner = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        alphaLayerSpinner = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        shadowCastComboBox = new javax.swing.JComboBox(ShadowCastMode.values());
        jLabel7 = new javax.swing.JLabel();
        bumpLevelSpinner = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        cosinePowerSpinner = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        reflectionBlurSpinner = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        refractiveIndexSpinner = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        alphaModeCombo = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        openBRDFButton = new javax.swing.JButton();
        shaderLabel = new javax.swing.JLabel();
        brdfShaderPathLabel = new javax.swing.JLabel();
        gameLabel = new javax.swing.JLabel();
        gameComboBox = new javax.swing.JComboBox<>();
        compileButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textureList = new javax.swing.JList<>();
        editTextureButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        importMenuItem = new javax.swing.JMenuItem();
        resetMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Gfx Toolkit");
        setResizable(false);

        gmatFlagsLabel.setText("Gmat Flags");

        receiveShadowCheckbox.setText("Receive Shadows");
        receiveShadowCheckbox.setMaximumSize(new java.awt.Dimension(140, 20));
        receiveShadowCheckbox.setMinimumSize(new java.awt.Dimension(140, 20));
        receiveShadowCheckbox.setPreferredSize(new java.awt.Dimension(140, 20));

        receiveSunCheckbox.setText("Receive Sun");
        receiveSunCheckbox.setMaximumSize(new java.awt.Dimension(140, 20));
        receiveSunCheckbox.setMinimumSize(new java.awt.Dimension(140, 20));
        receiveSunCheckbox.setPreferredSize(new java.awt.Dimension(140, 20));

        receiveSpritelightCheckbox.setText("Receive Spritelight");
        receiveSpritelightCheckbox.setMaximumSize(new java.awt.Dimension(140, 20));
        receiveSpritelightCheckbox.setMinimumSize(new java.awt.Dimension(140, 20));
        receiveSpritelightCheckbox.setPreferredSize(new java.awt.Dimension(140, 20));

        maxPriorityCheckbox.setText("Max Priority");
        maxPriorityCheckbox.setMaximumSize(new java.awt.Dimension(140, 20));
        maxPriorityCheckbox.setMinimumSize(new java.awt.Dimension(140, 20));
        maxPriorityCheckbox.setPreferredSize(new java.awt.Dimension(140, 20));
        maxPriorityCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxPriorityCheckboxActionPerformed(evt);
            }
        });

        squishyCheckbox.setText("Squishy");
        squishyCheckbox.setMaximumSize(new java.awt.Dimension(140, 20));
        squishyCheckbox.setMinimumSize(new java.awt.Dimension(140, 20));
        squishyCheckbox.setPreferredSize(new java.awt.Dimension(140, 20));

        noInstanceTextureCheckbox.setText("No Instance Texture");
        noInstanceTextureCheckbox.setMaximumSize(new java.awt.Dimension(140, 20));
        noInstanceTextureCheckbox.setMinimumSize(new java.awt.Dimension(140, 20));
        noInstanceTextureCheckbox.setPreferredSize(new java.awt.Dimension(140, 20));

        wireCheckbox.setText("Wire");
        wireCheckbox.setMaximumSize(new java.awt.Dimension(90, 20));
        wireCheckbox.setMinimumSize(new java.awt.Dimension(90, 20));
        wireCheckbox.setPreferredSize(new java.awt.Dimension(90, 20));

        furryCheckbox.setText("Furry");
        furryCheckbox.setMaximumSize(new java.awt.Dimension(90, 20));
        furryCheckbox.setMinimumSize(new java.awt.Dimension(90, 20));
        furryCheckbox.setPreferredSize(new java.awt.Dimension(90, 20));

        twoSidedCheckbox.setText("Two Sided");
        twoSidedCheckbox.setMaximumSize(new java.awt.Dimension(90, 20));
        twoSidedCheckbox.setMinimumSize(new java.awt.Dimension(90, 20));
        twoSidedCheckbox.setPreferredSize(new java.awt.Dimension(90, 20));

        alphaClipCheckbox.setText("Alpha Clip");
        alphaClipCheckbox.setEnabled(false);
        alphaClipCheckbox.setMaximumSize(new java.awt.Dimension(140, 20));
        alphaClipCheckbox.setMinimumSize(new java.awt.Dimension(140, 20));
        alphaClipCheckbox.setPreferredSize(new java.awt.Dimension(140, 20));

        javax.swing.GroupLayout gmatFlagsContainerLayout = new javax.swing.GroupLayout(gmatFlagsContainer);
        gmatFlagsContainer.setLayout(gmatFlagsContainerLayout);
        gmatFlagsContainerLayout.setHorizontalGroup(
            gmatFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gmatFlagsContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gmatFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gmatFlagsLabel)
                    .addGroup(gmatFlagsContainerLayout.createSequentialGroup()
                        .addComponent(receiveShadowCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(maxPriorityCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(wireCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(gmatFlagsContainerLayout.createSequentialGroup()
                        .addComponent(receiveSunCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(squishyCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(furryCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(gmatFlagsContainerLayout.createSequentialGroup()
                        .addComponent(receiveSpritelightCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(noInstanceTextureCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(twoSidedCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(alphaClipCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gmatFlagsContainerLayout.setVerticalGroup(
            gmatFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gmatFlagsContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gmatFlagsLabel)
                .addGap(5, 5, 5)
                .addGroup(gmatFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(receiveShadowCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPriorityCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wireCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(gmatFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(receiveSunCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(squishyCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(furryCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(gmatFlagsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(receiveSpritelightCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(noInstanceTextureCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(twoSidedCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(alphaClipCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        propertiesLabel.setText("Properties:");

        jLabel4.setText("Alpha Test Level:");
        jLabel4.setMaximumSize(new java.awt.Dimension(90, 16));
        jLabel4.setMinimumSize(new java.awt.Dimension(90, 16));
        jLabel4.setPreferredSize(new java.awt.Dimension(90, 16));

        alphaTestLevelSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        jLabel5.setText("Alpha Layer:");
        jLabel5.setMaximumSize(new java.awt.Dimension(90, 16));
        jLabel5.setMinimumSize(new java.awt.Dimension(90, 16));
        jLabel5.setPreferredSize(new java.awt.Dimension(90, 16));

        alphaLayerSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        jLabel6.setText("Shadow Cast:");
        jLabel6.setMaximumSize(new java.awt.Dimension(90, 16));
        jLabel6.setMinimumSize(new java.awt.Dimension(90, 16));
        jLabel6.setPreferredSize(new java.awt.Dimension(90, 16));

        jLabel7.setText("Bump Level:");
        jLabel7.setMaximumSize(new java.awt.Dimension(90, 16));
        jLabel7.setMinimumSize(new java.awt.Dimension(90, 16));
        jLabel7.setPreferredSize(new java.awt.Dimension(90, 16));

        bumpLevelSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        jLabel8.setText("Cosine Power:");
        jLabel8.setMaximumSize(new java.awt.Dimension(90, 16));
        jLabel8.setMinimumSize(new java.awt.Dimension(90, 16));
        jLabel8.setPreferredSize(new java.awt.Dimension(90, 16));

        cosinePowerSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        jLabel9.setText("Reflection Blur:");
        jLabel9.setMaximumSize(new java.awt.Dimension(90, 16));
        jLabel9.setMinimumSize(new java.awt.Dimension(90, 16));
        jLabel9.setPreferredSize(new java.awt.Dimension(90, 16));

        reflectionBlurSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        jLabel10.setText("Refractive Index:");
        jLabel10.setMaximumSize(new java.awt.Dimension(90, 16));
        jLabel10.setMinimumSize(new java.awt.Dimension(90, 16));
        jLabel10.setPreferredSize(new java.awt.Dimension(90, 16));

        refractiveIndexSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0f, null, null, 1.0f));

        jLabel11.setText("Alpha Mode:");

        alphaModeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "DISABLE", "ALPHA_BLEND", "ADDITIVE", "ADDITIVE_NO_ALPHA", "PREMULTIPLIED_ALPHA" }));
        alphaModeCombo.setEnabled(false);

        javax.swing.GroupLayout propertiesContainerLayout = new javax.swing.GroupLayout(propertiesContainer);
        propertiesContainer.setLayout(propertiesContainerLayout);
        propertiesContainerLayout.setHorizontalGroup(
            propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(propertiesContainerLayout.createSequentialGroup()
                        .addGroup(propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(propertiesLabel)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(refractiveIndexSpinner)
                            .addComponent(alphaTestLevelSpinner)
                            .addComponent(alphaLayerSpinner)
                            .addComponent(shadowCastComboBox, 0, 127, Short.MAX_VALUE)
                            .addComponent(bumpLevelSpinner)
                            .addComponent(cosinePowerSpinner)
                            .addComponent(reflectionBlurSpinner)))
                    .addGroup(propertiesContainerLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(27, 27, 27)
                        .addComponent(alphaModeCombo, 0, 1, Short.MAX_VALUE)))
                .addContainerGap())
        );
        propertiesContainerLayout.setVerticalGroup(
            propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(propertiesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(alphaTestLevelSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(alphaLayerSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(shadowCastComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bumpLevelSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cosinePowerSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reflectionBlurSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refractiveIndexSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(alphaModeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setText("Shaders:");

        openBRDFButton.setText("Open");
        openBRDFButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openBRDFButtonActionPerformed(evt);
            }
        });

        shaderLabel.setText("Shader:");
        shaderLabel.setMaximumSize(new java.awt.Dimension(50, 16));
        shaderLabel.setMinimumSize(new java.awt.Dimension(50, 16));
        shaderLabel.setPreferredSize(new java.awt.Dimension(50, 16));

        brdfShaderPathLabel.setText("Please select a CG source file!");
        brdfShaderPathLabel.setFocusable(false);

        gameLabel.setText("Game:");

        gameComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "LBP1", "LBP2 Pre-Alpha", "LBP2/3", "LBP3 PS4" }));
        gameComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gameComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(openBRDFButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(shaderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(brdfShaderPathLabel))
                            .addComponent(jLabel2)
                            .addComponent(gameLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openBRDFButton)
                    .addComponent(shaderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(brdfShaderPathLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        compileButton.setText("Compile");
        compileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compileButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("© Dr. Disney West");

        jLabel12.setText("Textures:");

        textureList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(textureList);

        editTextureButton.setText("Edit");
        editTextureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editTextureButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(editTextureButton, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editTextureButton)
                .addContainerGap())
        );

        fileMenu.setText("File");

        importMenuItem.setText("Import");
        importMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(importMenuItem);

        resetMenuItem.setText("Reset");
        resetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(resetMenuItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(gmatFlagsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(compileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(propertiesContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(gmatFlagsContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(propertiesContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compileButton)
                    .addComponent(closeButton)
                    .addComponent(jLabel3))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void maxPriorityCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxPriorityCheckboxActionPerformed
        System.out.println("I'm too lazy to remove this.");
    }//GEN-LAST:event_maxPriorityCheckboxActionPerformed

    private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importMenuItemActionPerformed
        this.load();
    }//GEN-LAST:event_importMenuItemActionPerformed

    private void resetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetMenuItemActionPerformed
        this.reset();
    }//GEN-LAST:event_resetMenuItemActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private String fixupEnvVar(String shader) {
        if (shader == null) return null;

        shader = shader.replace("ENV.ALPHA_TEST_LEVEL", String.format(Locale.ROOT, "%f", this.gmat.alphaTestLevel));
        shader = shader.replace("ENV.ALPHA_MODE", "" + this.gmat.alphaMode);

        shader = shader.replace("ENV.COSINE_POWER", String.format(Locale.ROOT, "%f", this.gmat.cosinePower * 22.0f));
        shader = shader.replace("ENV.BUMP_LEVEL", String.format(Locale.ROOT, "%f", this.gmat.bumpLevel));
        
        shader = shader.replace("ENV.REFLECTION_BLUR", String.format(Locale.ROOT, "%f", this.gmat.reflectionBlur - 1.0f));
        shader = shader.replace("ENV.REFRACTIVE_INDEX", String.format(Locale.ROOT, "%f", this.gmat.refractiveIndex));

        shader = shader.replace("ENV.FRESNEL_FALLOFF_POWER", String.format(Locale.ROOT, "%f", this.gmat.refractiveFresnelFalloffPower));
        shader = shader.replace("ENV.FRESNEL_MULTIPLIER", String.format(Locale.ROOT, "%f", this.gmat.refractiveFresnelMultiplier));
        shader = shader.replace("ENV.FRESNEL_OFFSET", String.format(Locale.ROOT, "%f", this.gmat.refractiveFresnelOffset));
        shader = shader.replace("ENV.FRESNEL_SHIFT", String.format(Locale.ROOT, "%f", this.gmat.refractiveFresnelShift));

        shader = shader.replace("ENV.FUZZ_LIGHTING_BIAS", String.format(Locale.ROOT, "%f", ((float)((int)this.gmat.fuzzLightingBias & 0xff)) / 255.0f));
        shader = shader.replace("ENV.FUZZ_LIGHTING_SCALE", String.format(Locale.ROOT, "%f", ((float)((int)this.gmat.fuzzLightingScale & 0xff)) / 255.0f));
        
        shader = shader.replace("ENV.IRIDESCENCE_ROUGHNESS", String.format(Locale.ROOT, "%f", ((float)((int)this.gmat.iridesenceRoughness & 0xff)) / 255.0f));
        
        return shader;
    }
    
    
    private void compileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compileButtonActionPerformed
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            JOptionPane.showMessageDialog(this, "This program is only functional on Windows!", "Error", JOptionPane.WARNING_MESSAGE);   
            return;
        }

        if (!ApplicationFlags.CAN_COMPILE_CELL_SHADERS)
        JOptionPane.showMessageDialog(this, String.format("Unable to find SCE-CGC compiler! Cannot compile! (Expected location is %s)", ApplicationFlags.SCE_CGC_EXECUTABLE.getAbsolutePath()), "Error", JOptionPane.WARNING_MESSAGE);
        
        if (this.brdf == null) {
            JOptionPane.showMessageDialog(this, "BRDF shader is missing! Can't compile!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        boolean isLBP2 = this.gameComboBox.getSelectedIndex() > 0;
        boolean isPreAlpha = this.gameComboBox.getSelectedIndex() == 1;
        boolean isPS4 = this.gameComboBox.getSelectedIndex() == 3;
        
        if (isPS4 && !ApplicationFlags.CAN_COMPILE_ORBIS_SHADERS) {
            JOptionPane.showMessageDialog(this, String.format("Unable to find SCE-PSSL compiler! Cannot compile! (Expected location is %s)", ApplicationFlags.SCE_PSSL_EXECUTABLE.getAbsolutePath()), "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int flags = 0;
        if (this.receiveShadowCheckbox.isSelected()) flags |= GfxMaterialFlags.RECEIVE_SHADOWS;
        if (this.receiveSunCheckbox.isSelected()) flags |= GfxMaterialFlags.RECEIVE_SUN;
        if (this.receiveSpritelightCheckbox.isSelected()) flags |= GfxMaterialFlags.RECEIVE_SPRITELIGHTS;
        if (this.maxPriorityCheckbox.isSelected()) flags |= GfxMaterialFlags.MAX_PRIORITY;
        if (this.squishyCheckbox.isSelected()) flags |= GfxMaterialFlags.SQUISHY;
        if (this.noInstanceTextureCheckbox.isSelected()) flags |= GfxMaterialFlags.NO_INSTANCE_TEXTURE;
        if (this.wireCheckbox.isSelected()) flags |= GfxMaterialFlags.WIRE;
        if (this.furryCheckbox.isSelected()) flags |= GfxMaterialFlags.FURRY;
        if (this.twoSidedCheckbox.isSelected()) flags |= GfxMaterialFlags.TWO_SIDED;
        if (isLBP2) {
            if (this.alphaClipCheckbox.isSelected()) flags |= GfxMaterialFlags.ALPHA_CLIP;
        }
        
        this.gmat.flags = flags;
        
        this.gmat.alphaTestLevel = (float) this.alphaTestLevelSpinner.getValue();
        this.gmat.alphaLayer = ((Integer)this.alphaLayerSpinner.getValue()).byteValue();
        this.gmat.shadowCastMode = (ShadowCastMode) this.shadowCastComboBox.getSelectedItem();
        this.gmat.bumpLevel = (float) this.bumpLevelSpinner.getValue();
        this.gmat.cosinePower = (float) this.cosinePowerSpinner.getValue();
        this.gmat.reflectionBlur = (float) this.reflectionBlurSpinner.getValue();
        this.gmat.refractiveIndex = (float) this.refractiveIndexSpinner.getValue();
        if (isLBP2)
            this.gmat.alphaMode = (byte) this.alphaModeCombo.getSelectedIndex();

        
        
        this.gmat.shaders = new byte[isLBP2 ? ((isPreAlpha) ? 4 : 10) : 4][];
        try { CgAssembler.compile(this.fixupEnvVar(this.brdf), this.gmat, GameShader.values()[this.gameComboBox.getSelectedIndex()]); }
        catch (Exception ex) {
            new ErrorDialogue(this, true, "An error occurred while compiling BRDF shader.", ex.getMessage());
            return;
        }
        
        gmat.textures = new ResourceDescriptor[8];
        gmat.wrapS = new TextureWrap[8];
        gmat.wrapT = new TextureWrap[8];
        for (int i = 0; i < 8; ++i) {
            TextureEntry entry = ((TextureEntry)this.textureModel.get(i));
            gmat.textures[i] = entry.descriptor;
            gmat.wrapS[i] = entry.wrapS;
            gmat.wrapT[i] = entry.wrapT;
        }
        
        Revision revision;

        if (isPreAlpha) revision = new Revision(0x332);
        else if (isLBP2) revision = new Revision(0x393);
        else revision = new Revision(0x272, 0x4c44, 0x0013);
        
        byte[] resource = Resource.compress(gmat.build(revision, CompressionFlags.USE_ALL_COMPRESSION));
        File file = FileChooser.openFile("export.gmat", "gmat", true);
        if (file == null) return;
        if (FileIO.write(resource, file.getAbsolutePath()))
            JOptionPane.showMessageDialog(this, "Succesfully compiled shader!", "Success!", JOptionPane.PLAIN_MESSAGE);
        else
            JOptionPane.showMessageDialog(this, "An error occurred writing file, is this path valid?", "Error", JOptionPane.WARNING_MESSAGE);
    }//GEN-LAST:event_compileButtonActionPerformed

    private void editTextureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editTextureButtonActionPerformed
        int index = this.textureList.getSelectedIndex();
        if (index == -1) return;
        TextureEntry entry = (TextureEntry) this.textureModel.get(index);
        new TextureDialogue(this, entry);
    }//GEN-LAST:event_editTextureButtonActionPerformed

    private void openBRDFButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openBRDFButtonActionPerformed
        File file = FileChooser.openFile("normal.cg", "cg", false);
        if (file == null || !file.exists()) return;
        String data = FileIO.readString(Path.of(file.getAbsolutePath()));
        if (data == null) return;
        this.brdf = data;
        this.brdfShaderPathLabel.setText(file.getName());
    }//GEN-LAST:event_openBRDFButtonActionPerformed

    private void gameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gameComboBoxActionPerformed
        int index = this.gameComboBox.getSelectedIndex();
        boolean isLBP2 = index > 0;
        this.alphaClipCheckbox.setEnabled(isLBP2);
        this.alphaModeCombo.setEnabled(isLBP2);
    }//GEN-LAST:event_gameComboBoxActionPerformed

    public static void main(String args[]) {
        LafManager.install(new DarculaTheme());
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GfxGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox alphaClipCheckbox;
    private javax.swing.JSpinner alphaLayerSpinner;
    private javax.swing.JComboBox<String> alphaModeCombo;
    private javax.swing.JSpinner alphaTestLevelSpinner;
    private javax.swing.JLabel brdfShaderPathLabel;
    private javax.swing.JSpinner bumpLevelSpinner;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton compileButton;
    private javax.swing.JSpinner cosinePowerSpinner;
    private javax.swing.JButton editTextureButton;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JCheckBox furryCheckbox;
    private javax.swing.JComboBox<String> gameComboBox;
    private javax.swing.JLabel gameLabel;
    private javax.swing.JPanel gmatFlagsContainer;
    private javax.swing.JLabel gmatFlagsLabel;
    private javax.swing.JMenuItem importMenuItem;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox maxPriorityCheckbox;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JCheckBox noInstanceTextureCheckbox;
    private javax.swing.JButton openBRDFButton;
    private javax.swing.JPanel propertiesContainer;
    private javax.swing.JLabel propertiesLabel;
    private javax.swing.JCheckBox receiveShadowCheckbox;
    private javax.swing.JCheckBox receiveSpritelightCheckbox;
    private javax.swing.JCheckBox receiveSunCheckbox;
    private javax.swing.JSpinner reflectionBlurSpinner;
    private javax.swing.JSpinner refractiveIndexSpinner;
    private javax.swing.JMenuItem resetMenuItem;
    private javax.swing.JLabel shaderLabel;
    private javax.swing.JComboBox<ShadowCastMode> shadowCastComboBox;
    private javax.swing.JCheckBox squishyCheckbox;
    private javax.swing.JList<String> textureList;
    private javax.swing.JCheckBox twoSidedCheckbox;
    private javax.swing.JCheckBox wireCheckbox;
    // End of variables declaration//GEN-END:variables
}
