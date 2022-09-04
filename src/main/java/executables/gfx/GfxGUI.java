package executables.gfx;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import cwlib.enums.CompressionFlags;
import cwlib.enums.GfxMaterialFlags;
import cwlib.enums.ResourceType;
import cwlib.enums.ShadowCastMode;
import cwlib.enums.TextureWrap;
import cwlib.resources.RGfxMaterial;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;
import executables.gfx.NVCompiler.GfxFlags;
import executables.gfx.dialogues.ErrorDialogue;
import executables.gfx.dialogues.TextureDialogue;

import java.io.File;
import java.nio.file.Path;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import toolkit.configurations.Config;
import toolkit.configurations.Profile;
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
    
    private static final File SCE_CGC = new File(Config.jarDirectory, "sce/sce-cgc.exe");
    
    private RGfxMaterial gmat;
    
    private DefaultListModel textureModel = new DefaultListModel();
    
    private String normal;
    private String color;
    private String decal;
    private String color1;
    
    private GfxGUI() {
        this.initComponents();
        this.textureList.setModel(this.textureModel);
        
        this.reset();
        
        // Hack for Toolkit FileChooser
        Config.instance = new Config();
        Config.instance.profiles.add(new Profile());
        Config.instance.currentProfile = 0;
        
        NVCompiler.USE_ENV_VARIABLES = true;
        
        if (!System.getProperty("os.name").toLowerCase().contains("win"))
            JOptionPane.showMessageDialog(this, "This program is only functional on Windows!", "Error", JOptionPane.WARNING_MESSAGE);
        
        if (!SCE_CGC.exists())
            JOptionPane.showMessageDialog(this, String.format("Unable to find SCE-CGC compiler! This program will not function! (Expected location is %s)", SCE_CGC.getAbsolutePath()), "Error", JOptionPane.WARNING_MESSAGE);
    }
    
    private void reset() {
        this.gmat = new RGfxMaterial();
        
        this.textureModel.clear();
        for (int i = 0; i < 8; ++i)
            this.textureModel.addElement(new TextureEntry(i));
        
        this.normal = null;
        this.color = null;
        this.decal = null;
        this.color1 = null;
        
        this.normalShaderPathLabel.setText("Please select a CG source file!");
        this.colorShaderPathLabel.setText("Please select a CG source file!");
        this.decalShaderPathLabel.setText("Please select a CG source file!");
        
        
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
        
        // Update properties
        this.alphaTestLevelSpinner.setValue(this.gmat.alphaTestLevel);
        this.alphaLayerSpinner.setValue((int) (this.gmat.alphaLayer & 0xFF));
        this.shadowCastComboBox.setSelectedItem(this.gmat.shadowCastMode);
        this.bumpLevelSpinner.setValue(this.gmat.bumpLevel);
        this.cosinePowerSpinner.setValue(this.gmat.cosinePower);
        this.reflectionBlurSpinner.setValue(this.gmat.reflectionBlur);
        this.refractiveIndexSpinner.setValue(this.gmat.refractiveIndex);
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
            String shader = NVCompiler.generateBRDF(this.gmat, -1);
            this.normal = shader.replace("ENV.COMPILE_FLAGS", "" + (GfxFlags.LEGACY | GfxFlags.LEGACY_NORMAL_PASS));
            this.color = shader.replace("ENV.COMPILE_FLAGS", "" + (GfxFlags.LEGACY));
            this.decal = shader.replace("ENV.COMPILE_FLAGS", "" + (GfxFlags.LEGACY | GfxFlags.DECALS));
            this.color1 = shader.replace("ENV.COMPILE_FLAGS", "" + (GfxFlags.LEGACY | GfxFlags.WATER_TWEAKS));
            
            this.normalShaderPathLabel.setText(name + ".normal.cg");
            this.colorShaderPathLabel.setText(name + ".color.cg");
            this.decalShaderPathLabel.setText(name + ".decal.cg");
            
            if (JOptionPane.showConfirmDialog(this, "Do you want to save generated shader?", "Shader Dump", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                File dest = FileChooser.openFile(name + ".cg", "cg", true);
                if (dest != null)
                    FileIO.write(shader.getBytes(), dest.getAbsolutePath());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid shader graph configuration! Can't generate shaders.", "Error", JOptionPane.WARNING_MESSAGE);
            this.normal = null;
            this.color = null;
            this.decal = null;
            this.color1 = null;
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
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        openNormalButton = new javax.swing.JButton();
        openColorButton = new javax.swing.JButton();
        openDecalButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        normalShaderPathLabel = new javax.swing.JLabel();
        colorShaderPathLabel = new javax.swing.JLabel();
        decalShaderPathLabel = new javax.swing.JLabel();
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
                        .addComponent(twoSidedCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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

        javax.swing.GroupLayout propertiesContainerLayout = new javax.swing.GroupLayout(propertiesContainer);
        propertiesContainer.setLayout(propertiesContainerLayout);
        propertiesContainerLayout.setHorizontalGroup(
            propertiesContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesContainerLayout.createSequentialGroup()
                .addContainerGap()
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
                    .addComponent(shadowCastComboBox, 0, 116, Short.MAX_VALUE)
                    .addComponent(bumpLevelSpinner)
                    .addComponent(cosinePowerSpinner)
                    .addComponent(reflectionBlurSpinner))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setText("Shaders:");

        openNormalButton.setText("Open");
        openNormalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openNormalButtonActionPerformed(evt);
            }
        });

        openColorButton.setText("Open");
        openColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openColorButtonActionPerformed(evt);
            }
        });

        openDecalButton.setText("Open");
        openDecalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDecalButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Normal:");
        jLabel1.setMaximumSize(new java.awt.Dimension(50, 16));
        jLabel1.setMinimumSize(new java.awt.Dimension(50, 16));
        jLabel1.setPreferredSize(new java.awt.Dimension(50, 16));

        jLabel11.setText("Color:");
        jLabel11.setMaximumSize(new java.awt.Dimension(50, 16));
        jLabel11.setMinimumSize(new java.awt.Dimension(50, 16));
        jLabel11.setPreferredSize(new java.awt.Dimension(50, 16));

        jLabel13.setText("Decal:");
        jLabel13.setMaximumSize(new java.awt.Dimension(50, 16));
        jLabel13.setMinimumSize(new java.awt.Dimension(50, 16));
        jLabel13.setPreferredSize(new java.awt.Dimension(50, 16));

        normalShaderPathLabel.setText("Please select a CG source file!");
        normalShaderPathLabel.setFocusable(false);

        colorShaderPathLabel.setText("Please select a CG source file!");
        colorShaderPathLabel.setFocusable(false);

        decalShaderPathLabel.setText("Please select a CG source file!");
        decalShaderPathLabel.setFocusable(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(openNormalButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(normalShaderPathLabel))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(openColorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(colorShaderPathLabel))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(openDecalButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(decalShaderPathLabel)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openNormalButton)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(normalShaderPathLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openColorButton)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colorShaderPathLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openDecalButton)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(decalShaderPathLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(editTextureButton, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(gmatFlagsContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(propertiesContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        shader = shader.replaceAll("ENV.BUMP_LEVEL", String.format("%f", (float) this.bumpLevelSpinner.getValue()));
        shader = shader.replaceAll("ENV.COSINE_POWER", String.format("%f", ((float) this.cosinePowerSpinner.getValue()) * 22.0f));
        shader = shader.replaceAll("ENV.ALPHA_TEST_LEVEL", String.format("%f", (float) this.alphaTestLevelSpinner.getValue()));
        shader = shader.replaceAll("ENV.REFLECTION_BLUR", String.format("%f", ((float) this.alphaTestLevelSpinner.getValue()) - 1.0f));
        shader = shader.replaceAll("ENV.REFRACTIVE_INDEX", String.format("%f", (float) this.refractiveIndexSpinner.getValue()));
        return shader;

        // BUMP1 - 0.5 + BUMP2
        // (s2 - 0.5) + s3
    }
    
    
    private void compileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compileButtonActionPerformed
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            JOptionPane.showMessageDialog(this, "This program is only functional on Windows!", "Error", JOptionPane.WARNING_MESSAGE);   
            return;
        }
        
        if (!SCE_CGC.exists()) {
            JOptionPane.showMessageDialog(this, String.format("Unable to find SCE-CGC compiler! Cannot compile! (Expected location is %s)", SCE_CGC.getAbsolutePath()), "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (this.normal == null || this.color == null || this.decal == null || this.color1 == null) {
            JOptionPane.showMessageDialog(this, "A shader is missing! Can't compile!", "Error", JOptionPane.WARNING_MESSAGE);
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
        
        this.gmat.flags = flags;
        
        this.gmat.alphaTestLevel = (float) this.alphaTestLevelSpinner.getValue();
        this.gmat.alphaLayer = ((Integer)this.alphaLayerSpinner.getValue()).byteValue();
        this.gmat.shadowCastMode = (ShadowCastMode) this.shadowCastComboBox.getSelectedItem();
        this.gmat.bumpLevel = (float) this.bumpLevelSpinner.getValue();
        this.gmat.cosinePower = (float) this.cosinePowerSpinner.getValue();
        this.gmat.reflectionBlur = (float) this.reflectionBlurSpinner.getValue();
        this.gmat.refractiveIndex = (float) this.refractiveIndexSpinner.getValue();
        
        
        byte[] normal = null;
        try { normal = NVCompiler.getShader(this.fixupEnvVar(this.normal)); }
        catch (Exception ex) { 
            new ErrorDialogue(this, true, "An error occurred while compiling normal shader.", ex.getMessage());
            return;
        }
        
        byte[] color = null;
        try { color = NVCompiler.getShader(this.fixupEnvVar(this.color)); }
        catch (Exception ex) { 
            new ErrorDialogue(this, true, "An error occurred while compiling color shader.", ex.getMessage());
            return;
        }
        
        byte[] decal = null;
        try { decal = NVCompiler.getShader(this.fixupEnvVar(this.decal)); }
        catch (Exception ex) { 
            new ErrorDialogue(this, true, "An error occurred while compiling decal shader.", ex.getMessage());
            return;
        }
        
        
        byte[] color1 = null;
        try { color1 = NVCompiler.getShader(this.fixupEnvVar(this.color1)); }
        catch (Exception ex) { 
            new ErrorDialogue(this, true, "An error occurred while compiling alt. color shader, this is probably Aidan's fault.", ex.getMessage());
            return;
        }
        
        gmat.shaders = new byte[][] { normal, color, decal, color1 };
        gmat.code = null;
        
        gmat.textures = new ResourceDescriptor[8];
        gmat.wrapS = new TextureWrap[8];
        gmat.wrapT = new TextureWrap[8];
        for (int i = 0; i < 8; ++i) {
            TextureEntry entry = ((TextureEntry)this.textureModel.get(i));
            gmat.textures[i] = entry.descriptor;
            gmat.wrapS[i] = entry.wrapS;
            gmat.wrapT[i] = entry.wrapT;
        }
        
        byte[] resource = Resource.compress(gmat.build(new Revision(0x272, 0x4c44, 0x0017), CompressionFlags.USE_ALL_COMPRESSION));
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

    private void openNormalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openNormalButtonActionPerformed
        File file = FileChooser.openFile("normal.cg", "cg", false);
        if (file == null || !file.exists()) return;
        String data = FileIO.readString(Path.of(file.getAbsolutePath()));
        if (data == null) return;
        this.normal = data.replace("ENV.COMPILE_FLAGS", "" + (GfxFlags.LEGACY | GfxFlags.LEGACY_NORMAL_PASS));
        this.normalShaderPathLabel.setText(file.getName());
    }//GEN-LAST:event_openNormalButtonActionPerformed

    private void openColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openColorButtonActionPerformed
        File file = FileChooser.openFile("color.cg", "cg", false);
        if (file == null || !file.exists()) return;
        String data = FileIO.readString(Path.of(file.getAbsolutePath()));
        if (data == null) return;

        this.color = data.replace("ENV.COMPILE_FLAGS", "" + (GfxFlags.LEGACY));
        
        String color1 = data;
        color1 = color1.replace("0.00078125f", "0.003125f");
        color1 = color1.replace("0.00138889f", "0.00555556f");
        color1 = color1.replace("0.000195313f", "0.00078125f");
        color1 = color1.replace("0.0498047f", "0.199219f");
        this.color1 = color1.replace("ENV.COMPILE_FLAGS", "" + (GfxFlags.LEGACY | GfxFlags.WATER_TWEAKS));
        
        
        this.colorShaderPathLabel.setText(file.getName());
    }//GEN-LAST:event_openColorButtonActionPerformed

    private void openDecalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDecalButtonActionPerformed
        File file = FileChooser.openFile("decal.cg", "cg", false);
        if (file == null || !file.exists()) return;
        String data = FileIO.readString(Path.of(file.getAbsolutePath()));
        if (data == null) return;
        this.decal = data.replace("ENV.COMPILE_FLAGS", "" + (GfxFlags.LEGACY | GfxFlags.DECALS));
        this.decalShaderPathLabel.setText(file.getName());
    }//GEN-LAST:event_openDecalButtonActionPerformed

    public static void main(String args[]) {
        LafManager.install(new DarculaTheme());
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GfxGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner alphaLayerSpinner;
    private javax.swing.JSpinner alphaTestLevelSpinner;
    private javax.swing.JSpinner bumpLevelSpinner;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel colorShaderPathLabel;
    private javax.swing.JButton compileButton;
    private javax.swing.JSpinner cosinePowerSpinner;
    private javax.swing.JLabel decalShaderPathLabel;
    private javax.swing.JButton editTextureButton;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JCheckBox furryCheckbox;
    private javax.swing.JPanel gmatFlagsContainer;
    private javax.swing.JLabel gmatFlagsLabel;
    private javax.swing.JMenuItem importMenuItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JLabel normalShaderPathLabel;
    private javax.swing.JButton openColorButton;
    private javax.swing.JButton openDecalButton;
    private javax.swing.JButton openNormalButton;
    private javax.swing.JPanel propertiesContainer;
    private javax.swing.JLabel propertiesLabel;
    private javax.swing.JCheckBox receiveShadowCheckbox;
    private javax.swing.JCheckBox receiveSpritelightCheckbox;
    private javax.swing.JCheckBox receiveSunCheckbox;
    private javax.swing.JSpinner reflectionBlurSpinner;
    private javax.swing.JSpinner refractiveIndexSpinner;
    private javax.swing.JMenuItem resetMenuItem;
    private javax.swing.JComboBox<ShadowCastMode> shadowCastComboBox;
    private javax.swing.JCheckBox squishyCheckbox;
    private javax.swing.JList<String> textureList;
    private javax.swing.JCheckBox twoSidedCheckbox;
    private javax.swing.JCheckBox wireCheckbox;
    // End of variables declaration//GEN-END:variables
}
