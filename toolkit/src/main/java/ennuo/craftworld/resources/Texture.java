package ennuo.craftworld.resources;

import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.FileIO;
import ennuo.craftworld.memory.Images;
import ennuo.craftworld.memory.Morton2D;
import ennuo.craftworld.resources.enums.Metadata.CompressionType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import net.npe.dds.DDSReader;

public class Texture extends Resource {
    
    
    
  private int DDSType = -1;
  public short width = -1, height = -1;
  
  public BufferedImage cached;
  
  public boolean parsed = true;
  
  private static byte[] DXTPre = new byte[] { 
      0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 
      4, 0, 0, 0 };
  
  private static byte[] DXTPost = new byte[] { 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      8, 16, 64, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
  
  private static byte[] DDSRGBA = new byte[] { 
      0, 64, 0, 0, 0, 0, 0, 0, 7, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 
      65, 0, 0, 0, 0, 0, 0, 0, 32, 0, 
      0, 0, 0, 0, -1, 0, 0, -1, 0, 0, 
      -1, 0, 0, 0, 0, 0, 0, -1, 10, 16, 
      64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0 };
  
  public Texture(byte[] data) {
    super(data);
    if (data == null || data.length < 4) {
        System.out.println("No data provided to Texture constructor");
        return;
    }
    
    
    String magic = str(3); seek(0);
    switch (magic) {
        case "ÿØÿ":
	case "‰PN":
            InputStream stream = new ByteArrayInputStream(data);
            try {
                cached = ImageIO.read(stream); stream.close(); parsed = true;
            } catch (IOException ex) { System.err.println("An error occured reading BufferedImage"); parsed = false; }
            return;
        case "DDS":
            cached = Images.fromDDS(data);
            parsed = true; return;
    }
    
    if (null == this.type) parsed = false; 
    else switch (this.type) {
          case LEGACY_TEXTURE:
              System.out.println("Decompressing TEX to DDS");
              decompress(true);
              break;
          case GTF_TEXTURE:
          case GXT_SIMPLE_TEXTURE:
          case GXT_EXTENDED_TEXTURE:
              System.out.println("Converting GTF texture to DDS");
              parseGTF();
              break;
          default:
              parsed = false;
              break;
      }
    
    
  }
  
  public void parseGTF() {
    byte[] header = getDDSHeader();
    decompress(true);
    byte[] DDS = new byte[this.data.length + header.length];
    System.arraycopy(header, 0, DDS, 0, header.length);
    System.arraycopy(this.data, 0, DDS, header.length, this.length);
    setData(DDS);
    if (type == CompressionType.GXT_EXTENDED_TEXTURE || type == CompressionType.GXT_SIMPLE_TEXTURE)
        unswizzle();
    else cached = getImage();
  }
  
  public void unswizzle() {
      
      int[] pixels = DDSReader.read(data, DDSReader.ARGB, 0);
      
      pixels = unswizzleData(pixels);
      
      cached = new BufferedImage(width, height, 2);
      if (cached != null)
          cached.setRGB(0, 0, width, height, pixels, 0, width);
  }
  
  public int[] unswizzleData(int[] data) {
      int[] pixels = new int[data.length];
      
      int min = width < height ? width : height;
      int k = (int) (Math.log(min) / Math.log(2));
      for (int i = 0; i < data.length; ++i) {
          int x, y;
          if (height < width) {
              int j = i >> (2 * k) << (2 * k)
                      | (Morton2D.decodeY(i) & (min - 1)) << k
                      | (Morton2D.decodeX(i) & (min - 1)) << 0;
              x = j / height;
              y = j % height;
          } else {
              int j = i >> (2 * k) << (2 * k)
                      | (Morton2D.decodeX(i) & (min - 1)) << k
                      | (Morton2D.decodeY(i) & (min - 1)) << 0;
              x = j % width;
              y = j / width;
          }
          
          if (y >= height || x >= width) continue;
          
          pixels[(x + (width * y))] = data[i];
      }
      
      return pixels;
  }
  
  public BufferedImage getImage() {
      if (cached != null) return cached;
      return Images.fromDDS(data); 
  }
  
  public ImageIcon getImageIcon() { return getImageIcon(320, 320); }
  public ImageIcon getImageIcon(int width, int height) {
    if (cached == null)
        cached = getImage();
    if (cached != null)
        return Images.getImageIcon(cached, width, height);
    else return null;
  }
  
  public static byte[] getDDSHeader(int type, short width, short height) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream(128);
    try {
      stream.write(new byte[] { 
            68, 68, 83, 32, 124, 0, 0, 0, 7, 16, 
            10, 0 });
      stream.write(Bytes.reverseShort(Bytes.toBytes(height)));
      stream.write(new byte[] { 0, 0 });
      stream.write(Bytes.reverseShort(Bytes.toBytes(width)));
      stream.write(new byte[] { 0, 0 });
      switch (type) {
        case 134:
        case 136:
          stream.write(DXTPre);
          if (type == 136) {
            stream.write(new byte[] { 68, 88, 84, 53 });
          } else {
            stream.write(new byte[] { 68, 88, 84, 49 });
          } 
          stream.write(DXTPost);
          break;
        case 133:
          stream.write(DDSRGBA);
          break;
      } 
    } catch (IOException ex) {
      Logger.getLogger(Texture.class.getName()).log(Level.SEVERE, (String)null, ex);
      return null;
    } 
    return stream.toByteArray();
  }
  
  public byte[] getDDSHeader() {
    if (this.type == CompressionType.GTF_TEXTURE) {
        this.DDSType = this.data[4] & 0xFF; 
        seek(12);   
    } else if (this.type == CompressionType.GXT_EXTENDED_TEXTURE) {
        this.DDSType = this.data[24] & 0xFF;
        seek(32);
    } else {
        this.DDSType = this.data[4] & 0xFF;
        seek(12);
    }
    System.out.println("DDS Type: " + Bytes.toHex(this.DDSType) + " (" + getDDSType() + ")");
    byte[] width = bytes(2), height = bytes(2);
    this.width = Bytes.toShort(width); this.height = Bytes.toShort(height);
    System.out.println("Image Width: " + this.width + "px");
    System.out.println("Image Height: " + this.height + "px");
    return getDDSHeader(DDSType, this.width, this.height);
  }
  
  private int getBBP() {
      switch (this.DDSType) {
          case 0x85:
              return 4;
          case 0x87:
          case 0x88:
              return 1;
          case 0x86:
              return 4;
      }
      return 4;
  }
  
  private String getDDSType() {
      switch (this.DDSType) {
          case 0x83: return "GR16";
          case 0x84: return "BGRA32 Swizzled";
          case 0x85: return "ARGB32 Swizzled";
          case 0x86: return "DXT1";
          case 0x87: return "DXT3";
          case 0x88: return "DXT5";
      }
      return "UNKNOWN";
  }
}
