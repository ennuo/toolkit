package ennuo.craftworld.resources.enums;

import java.util.HashMap;
import java.util.Map;

public class Metadata {
  public enum CompressionType {
    LEGACY_TEXTURE, GTF_TEXTURE, GXT_SIMPLE_TEXTURE, GXT_EXTENDED_TEXTURE, CUSTOM_COMPRESSION, CUSTOM_COMPRESSION_LEGACY, ENCRYPTED, FISH_MOD, UNKNOWN, STATIC_MESH;
  }
  
  public enum Identifier {
    HASH, RESOURCE;
  }
  
  
  public static Map<String, CompressionType> magic;
  static {
      magic = new HashMap<String, CompressionType>();
              magic.put("TEX ", CompressionType.LEGACY_TEXTURE); magic.put("GTF ", CompressionType.GTF_TEXTURE); magic.put("GTFs", CompressionType.GXT_SIMPLE_TEXTURE); magic.put("GTFS", CompressionType.GXT_EXTENDED_TEXTURE); 
        magic.put("PLNb", CompressionType.CUSTOM_COMPRESSION); magic.put("BEVb", CompressionType.CUSTOM_COMPRESSION); magic.put("ANMb", CompressionType.CUSTOM_COMPRESSION); magic.put("LVLb", CompressionType.CUSTOM_COMPRESSION); 
        magic.put("CLDb", CompressionType.CUSTOM_COMPRESSION); magic.put("FSHb", CompressionType.CUSTOM_COMPRESSION); magic.put("GMTb", CompressionType.CUSTOM_COMPRESSION); magic.put("MATb", CompressionType.CUSTOM_COMPRESSION); 
        magic.put("OATb", CompressionType.CUSTOM_COMPRESSION); magic.put("SLTb", CompressionType.CUSTOM_COMPRESSION); magic.put("PALb", CompressionType.CUSTOM_COMPRESSION); magic.put("PCKb", CompressionType.CUSTOM_COMPRESSION); 
        magic.put("BPRb", CompressionType.CUSTOM_COMPRESSION); magic.put("INSb", CompressionType.CUSTOM_COMPRESSION); magic.put("MSHb", CompressionType.CUSTOM_COMPRESSION); magic.put("VOPb", CompressionType.CUSTOM_COMPRESSION);
        magic.put("IPRe", CompressionType.ENCRYPTED); magic.put("IPRb", CompressionType.CUSTOM_COMPRESSION);  magic.put("FRMb", CompressionType.FISH_MOD); magic.put("FRMe", CompressionType.ENCRYPTED); magic.put("SMHb", CompressionType.STATIC_MESH);
  }
  
  public static CompressionType getType(String header, int revision) {
    if (magic.containsKey(header)) {
      CompressionType key = magic.get(header);
      if (key == CompressionType.CUSTOM_COMPRESSION && revision < 0x272)
        key = CompressionType.CUSTOM_COMPRESSION_LEGACY; 
      return key;
    } 
    return CompressionType.UNKNOWN;
  }
}
