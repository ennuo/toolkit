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
  
  public static Map<String, CompressionType> MAGIC;
  static {
      MAGIC = new HashMap<String, CompressionType>();
      MAGIC.put("OFTb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("PINb", CompressionType.CUSTOM_COMPRESSION);
      MAGIC.put("FNTb", CompressionType.CUSTOM_COMPRESSION_LEGACY);
      MAGIC.put("ADCb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("ADSb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("QSTb", CompressionType.CUSTOM_COMPRESSION); 
      MAGIC.put("TEX ", CompressionType.LEGACY_TEXTURE); MAGIC.put("GTF ", CompressionType.GTF_TEXTURE); MAGIC.put("GTFs", CompressionType.GXT_SIMPLE_TEXTURE); MAGIC.put("GTFS", CompressionType.GXT_EXTENDED_TEXTURE); 
      MAGIC.put("JNTb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("PLNb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("BEVb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("ANMb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("LVLb", CompressionType.CUSTOM_COMPRESSION); 
      MAGIC.put("CLDb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("FSHb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("GMTb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("MATb", CompressionType.CUSTOM_COMPRESSION); 
      MAGIC.put("OATb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("SLTb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("PALb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("PCKb", CompressionType.CUSTOM_COMPRESSION); 
      MAGIC.put("BPRb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("INSb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("MSHb", CompressionType.CUSTOM_COMPRESSION); MAGIC.put("VOPb", CompressionType.CUSTOM_COMPRESSION);
      MAGIC.put("IPRe", CompressionType.ENCRYPTED); MAGIC.put("IPRb", CompressionType.CUSTOM_COMPRESSION);  MAGIC.put("FRMb", CompressionType.FISH_MOD); MAGIC.put("FRMe", CompressionType.ENCRYPTED); MAGIC.put("SMHb", CompressionType.STATIC_MESH);
  }
  
  public static CompressionType getType(String header, int revision) {
    if (Metadata.MAGIC.containsKey(header)) {
      CompressionType key = MAGIC.get(header);
      if (key == CompressionType.CUSTOM_COMPRESSION && revision < 0x272)
        key = CompressionType.CUSTOM_COMPRESSION_LEGACY; 
      return key;
    } 
    return CompressionType.UNKNOWN;
  }
}
