package toolkit.windows;

import configurations.Config;
import cwlib.CwlibConfiguration;
import cwlib.enums.CellGcmEnumForGtf;
import cwlib.enums.CompressionFlags;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.io.serializer.SerializationData;
import cwlib.io.streams.MemoryInputStream;
import cwlib.resources.RMesh;
import cwlib.resources.RPlan;
import cwlib.resources.RTexture;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.mesh.Bone;
import cwlib.structs.texture.CellGcmTexture;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PGeneratedMesh;
import cwlib.structs.things.parts.PGroup;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.structs.things.parts.PShape;
import cwlib.types.SerializedResource;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.ResourceInfo;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.types.swing.FileNode;
import cwlib.util.BinaryPrimitives;
import cwlib.util.Bytes;
import cwlib.util.Compressor;
import cwlib.util.DDS;
import cwlib.util.FileIO;
import cwlib.util.Images;
import cwlib.util.Strings;
import gr.zdimensions.jsquish.Squish;
import gr.zdimensions.jsquish.Squish.CompressionType;
import scelib.Gxm;
import scelib.SeImageUtil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.joml.Matrix4f;
import toolkit.functions.PusherCallbacks;
import toolkit.utilities.FileChooser;

import static toolkit.functions.PusherCallbacks.PUSHER_PORT;

public class RemoteIconRendererGUI extends javax.swing.JDialog {
    private String _name;
    private byte[] _planData = {};
    private byte[] _responseData = {};
    private RTexture _texture;
   
    public RemoteIconRendererGUI(String name, byte[] planData) {
        super(Toolkit.INSTANCE, true);
        setLocationRelativeTo(Toolkit.INSTANCE);
        setResizable(false);
        initComponents();
        _planData = planData;
        _name = name;

        render();
    }
    
    public static RemoteIconRendererGUI GetRenderer()
    {
        FileNode node = ResourceSystem.getSelected();
        ResourceInfo info = node.getEntry().getInfo();
        if (info == null || (info.getType() != ResourceType.PLAN && info.getType() != ResourceType.MESH && info.getType() != ResourceType.GFX_MATERIAL) || info.getResource() == null)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Resource is either NULL or invalid!", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        byte[] resourceData = ResourceSystem.extract(node.getEntry());
        if (resourceData == null)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Failed to extract data for render!", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        byte[] planData = resourceData;
        if (info.getType() == ResourceType.MESH)
        {
            // Matrix4f transform = new Matrix4f().identity().rotate((float) Math.toRadians(-90.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Matrix4f());
            Matrix4f transform = new Matrix4f().identity();

            ResourceDescriptor descriptor = new ResourceDescriptor((GUID)node.getEntry().getKey(), ResourceType.MESH);
            RMesh mesh = info.getResource();
            ArrayList<Thing> things = new ArrayList<>();
            int thingUIDCounter = 0;
            Bone[] bones = mesh.getBones();
            for (Bone bone : bones)
            {
                Thing thing = new Thing(++thingUIDCounter);
                Matrix4f wpos = transform.mul(bone.skinPoseMatrix, new Matrix4f());
                thing.setPart(Part.POS, new PPos(null, bone.animHash, wpos));
                things.add(thing);
            }

            Thing root = things.get(0);
            root.setPart(Part.GROUP, new PGroup());
            root.setPart(Part.RENDER_MESH, new PRenderMesh(descriptor, things.toArray(Thing[]::new)));

            for (int i = 0; i < bones.length; ++i)
            {
                Bone bone = bones[i];
                Thing thing = things.get(i);
                PPos pos = thing.getPart(Part.POS);
                pos.thingOfWhichIAmABone = root;

                if (i != 0)
                    thing.groupHead = root;

                if (bone.parent != -1)
                {
                    thing.parent = things.get(bone.parent);
                    pos.recomputeLocalPos(thing);
                }
            }

            RPlan plan = new RPlan(new Revision(0x132), CompressionFlags.USE_NO_COMPRESSION, things.toArray(Thing[]::new), new InventoryItemDetails());
            planData = SerializedResource.compress(plan.build());
        }
        else if (info.getType() == ResourceType.GFX_MATERIAL)
        {
            ResourceDescriptor descriptor = new ResourceDescriptor((GUID)node.getEntry().getKey(), ResourceType.GFX_MATERIAL);
            Thing root = new Thing(1);
            root.setPart(Part.POS, new PPos());
            root.setPart(Part.SHAPE, new PShape());
            root.setPart(Part.GENERATED_MESH, new PGeneratedMesh(descriptor, null));

            RPlan plan = new RPlan(new Revision(0x132), CompressionFlags.USE_NO_COMPRESSION, root, new InventoryItemDetails());
            planData = SerializedResource.compress(plan.build());            
        }
        
        return new RemoteIconRendererGUI(node.getName(), planData);
    }

    private void mergeMipChain()
    {
        if (_responseData.length > 4 && _responseData[0] == 'R' && _responseData[1] == 'G' && _responseData[2] == 'B' && _responseData[3] == 'A')
        {
            int width = BinaryPrimitives.readInt32BigEndian(_responseData, 4);
            int height = BinaryPrimitives.readInt32BigEndian(_responseData, 8);

            byte[] mipData = {};
            _responseData = Arrays.copyOfRange(_responseData, 0x80, _responseData.length);
            int mipCount = 0;

            // source format i assume is ARGB, we want RGBA
            for (int i = 0; i < _responseData.length; i += 4)
            {
                byte tmp = _responseData[i];
                _responseData[i] = _responseData[i + 1];
                _responseData[i + 1] = _responseData[i + 2];
                _responseData[i + 2] = _responseData[i + 3];
                _responseData[i + 3] = tmp;
            }

            int w0 = 0, h0 = 0;
            while (true)
            {
                if (width <= Config.pusher().targetResolution)
                {
                    if (mipCount == 0)
                    {
                        w0 = width;
                        h0 = height;
                    }

                    if (Config.pusher().squish)
                    {
                        mipData = Bytes.combine(mipData, Squish.compressImage(_responseData, width, height, null, CompressionType.DXT5));
                    }
                    else
                    {
                        mipData = Bytes.combine(mipData, SeImageUtil.compress(_responseData, SeImageUtil.DXGI_FORMAT_R8G8B8A8_UNORM, SeImageUtil.DXGI_FORMAT_BC3_UNORM, width, height));
                    }

                    mipCount += 1;
                }

                width >>>= 1;
                height >>>= 1;
                
                if (width == 1 || height == 1) break;

                _responseData = SeImageUtil.resize(_responseData, SeImageUtil.DXGI_FORMAT_R8G8B8A8_UNORM, width << 1, height << 1, width, height);
            }

            _responseData = SerializedResource.compress(
                new SerializationData(
                    Bytes.combine(DDS.getDDSHeader(CellGcmEnumForGtf.DXT5, w0, h0, mipCount, false),
                    mipData)
                )
            );

            return;
        }
    }

    public boolean setPusherResolution()
    {
        var config = Config.pusher();
        try (Socket client = new Socket(config.address, PUSHER_PORT))
        {
            byte[] message = new byte[0x1 + 0x4];
            message[0] = PusherCallbacks.PusherMessageType.SET_PUSHER_RESOLUTION;
            message[0x1] = (byte)(config.sourceResolution >>> 24);
            message[0x2] = (byte)(config.sourceResolution >>> 16);
            message[0x3] = (byte)(config.sourceResolution >>> 8);
            message[0x4] = (byte)(config.sourceResolution & 0xFF);
            client.getOutputStream().write(message);

            return true;
        }
        catch (UnknownHostException ex)
        {
            JOptionPane.showMessageDialog(this, "Please make sure to use a valid IP address!", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "An error occurred while connecting to PS3, make sure you're on a build that supports the pusher!", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "An unknown error occurred", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    
    public void render()
    {
        var config = Config.pusher();
        System.out.printf("requesting render. squish=%s, target_res=%d, source_res=%d\n", 
            config.squish ? "true" : "false",
            config.targetResolution,
            config.sourceResolution
        );

        if (!setPusherResolution()) return;

        try (Socket client = new Socket(config.address, PUSHER_PORT))
        {
            byte[] message = new byte[0x1 + 0x14 + 0x4 + _planData.length];
            message[0] = PusherCallbacks.PusherMessageType.GET_ITEM_ICON;
            System.arraycopy(SHA1.fromBuffer(_planData).getHash(), 0x0, message, 0x1, 0x14);
            message[0x15] = (byte)(_planData.length >>> 24);
            message[0x16] = (byte)(_planData.length >>> 16);
            message[0x17] = (byte)(_planData.length >>> 8);
            message[0x18] = (byte)(_planData.length & 0xFF);
            System.arraycopy(_planData, 0, message, 0x19, _planData.length);

            client.getOutputStream().write(message);

            _responseData = null;
            try
            {
                DataInputStream stream = new DataInputStream(new BufferedInputStream(client.getInputStream()));
                byte[] sha1 = stream.readNBytes(0x14);
                int len = stream.readInt();
                if (len != 0)
                {
                    _responseData = stream.readNBytes(len);
                }
            }
            catch (IOException ioex) { /* Ignore this error, we'll handle it in the next part */ }
            
            if (_responseData == null)
            {    
                JOptionPane.showMessageDialog(this, "PS3 didn't send back any data!", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
                setVisible(false);
                this.dispose();
                return;
            }

            mergeMipChain();

            _texture = new RTexture(_responseData);

            imagePreviewLabel.setText("");
            imagePreviewLabel.setIcon(Images.getImageIcon(_texture.getImage(), 128, 128));
            setVisible(true);
        }
        catch (UnknownHostException ex)
        {
            JOptionPane.showMessageDialog(this, "Please make sure to use a valid IP address!", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
            setVisible(false);
            this.dispose();
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "An error occurred while connecting to PS3, make sure you're on a build that supports the pusher!", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
            setVisible(false);
            this.dispose();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "An unknown error occurred", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
            setVisible(false);
            this.dispose();
        }
    }
    
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        imagePreviewLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        saveAsTEXMenuItem = new javax.swing.JMenuItem();
        saveAsGTFMenuItem = new javax.swing.JMenuItem();
        saveAsPNGMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        rerenderMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        imagePreviewLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imagePreviewLabel.setText("No preview");

        jMenu1.setText("File");

        saveAsTEXMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveAsTEXMenuItem.setText("Save as .TEX");
        saveAsTEXMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsTEXMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(saveAsTEXMenuItem);

        saveAsGTFMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveAsGTFMenuItem.setText("Save as .GTF");
        saveAsGTFMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsGTFMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(saveAsGTFMenuItem);

        saveAsPNGMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveAsPNGMenuItem.setText("Save as .PNG");
        saveAsPNGMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsPNGMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(saveAsPNGMenuItem);
        jMenu1.add(jSeparator1);

        rerenderMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        rerenderMenuItem.setText("Re-render");
        rerenderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rerenderMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(rerenderMenuItem);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(imagePreviewLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(imagePreviewLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void rerenderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rerenderMenuItemActionPerformed
        render();
    }//GEN-LAST:event_rerenderMenuItemActionPerformed

    private void saveAsTEXMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsTEXMenuItemActionPerformed
        if (_responseData == null) return;
        
        File file = FileChooser.openFile(Strings.setExtension(_name, ".tex"), "tex", true);
        if (file == null) return;

        try
        {
            FileIO.write(_responseData, file.getAbsolutePath());
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "An error occurred while reading resulting image data", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }//GEN-LAST:event_saveAsTEXMenuItemActionPerformed

    private void saveAsGTFMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsGTFMenuItemActionPerformed
        if (_responseData == null) return;
        
        File file = FileChooser.openFile(Strings.setExtension(_name, ".tex"), "tex", true);
        if (file == null) return;

        try
        {
            var data = _texture.getImageData();
            var gcm = new CellGcmTexture(data, false);
            data = Arrays.copyOfRange(data, 0x80, data.length);
            data = SerializedResource.compress(new SerializationData(data, gcm));
            FileIO.write(data, file.getAbsolutePath());
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "An error occurred while reading resulting image data", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }//GEN-LAST:event_saveAsGTFMenuItemActionPerformed

    private void saveAsPNGMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsPNGMenuItemActionPerformed
        if (_responseData == null) return;
        
        File file = FileChooser.openFile(Strings.setExtension(_name, ".png"), "png", true);
        if (file == null) return;

        try
        {
            ImageIO.write(_texture.getImage(), "png", file);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "An error occurred while reading resulting image data", "PS3 Pusher", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }//GEN-LAST:event_saveAsPNGMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel imagePreviewLabel;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuItem rerenderMenuItem;
    private javax.swing.JMenuItem saveAsGTFMenuItem;
    private javax.swing.JMenuItem saveAsPNGMenuItem;
    private javax.swing.JMenuItem saveAsTEXMenuItem;
    // End of variables declaration//GEN-END:variables
}
