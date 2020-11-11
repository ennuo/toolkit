package ennuo.craftworld.memory;

import ennuo.craftworld.resources.enums.RType;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Data {
    
  public static int ENCODED_REVISION = 0x271;
    
  public String path;
  
  public byte[] data;
  
  public int offset;
  
  public int length;
  
  public int revision = 0x271;
  
  public Data(byte[] data) { setData(data); }
  public Data(byte[] data, int revision) { setData(data); this.revision = revision; }
  
  public Data(String path) {
    this.path = path;
    byte[] data = FileIO.read(path);
    if (data != null)
        setData(data);
    else setData(null);
  }
  
  public Data(String path, int revision) {
    this.path = path; this.revision = revision;
    byte[] data = FileIO.read(path);
    if (data != null)
        setData(data);
    else setData(null);
  }
  
  public void setData(byte[] buffer) {
    this.data = buffer;
    if (this.data != null)
      this.length = buffer.length; 
    this.offset = 0;
  }
  
  public byte[] bytes(int size) {
    this.offset += size;
    if (this.offset == (data.length + 1) || size == 0) return new byte[] {};
    return Arrays.copyOfRange(this.data, this.offset - size, this.offset);
  }
  
  public void flip32() {
      byte[] bytes = this.bytes(4);
      data[offset - 4] = bytes[3];
      data[offset - 3] = bytes[2];
      data[offset - 2] = bytes[1];
      data[offset - 1] = bytes[0];
  }
  
  public boolean bool() {
      return int8() == 1;
  }
  
  public byte int8() {
    this.offset++;
    return this.data[this.offset - 1];
  }
  
  public int peek() {
      int offset = this.offset;
      int value = int32();
      seek(offset);
      return value;
  }
  
  public short int16() {
    byte[] buffer = bytes(2);
    return (short)((buffer[0] & 0xFF) << 8 | buffer[1] & 0xFF);
  }
  
  public int int16LE() {
    byte[] buffer = bytes(2);
    return buffer[0] << 8 & 0xFF00 | buffer[1] & 0xFF;
  }
  
  public int int32() {
    if (revision > ENCODED_REVISION) return (int) varint();
    return int32f();
  }
  
  public long uint32() {
      if (revision > ENCODED_REVISION) return varint();
      return uint32f();
  }
  
  public int int32f() {
      byte[] buffer = bytes(4);
      return buffer[0] << 24 | (buffer[1] & 0xFF) << 16 | (buffer[2] & 0xFF) << 8 | buffer[3] & 0xFF;  
  }
  
  public long uint32f() {
      byte[] bytes = bytes(4);
      /*
      long value = bytes[3] & 0xFF;
      value |= (bytes[2] << 8) & 0xFFFF;
      value |= (bytes[1] << 16) & 0xFFFFFF;
      value |= (bytes[0] << 24) & 0xFFFFFFFF;
      return value;
      */
      
      return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt() & 0xFFFFFFFFL;
      
      
  }
  
  public float float32() {
      return Float.intBitsToFloat(int32f());
  }
  
  public Vector2f v2() {
      return new Vector2f(float32(), float32());
  }
  
  public Vector3f v3() {
      return new Vector3f(float32(), float32(), float32());
  }
  
  public Vector4f v4() {
      return new Vector4f(float32(), float32(), float32(), float32());
  }
  
  public float[] matrix() {
      float[] matrix = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
      int flags = 0xFFFF;
      if (revision > ENCODED_REVISION) flags = int16();
      for (int i = 0; i < 16; ++i)
          if (((flags >>> i) & 1) != 0)
              matrix[i] = float32();
      return matrix;
  }
  
  public ResourcePtr resource(RType type) { return resource(type, false); }
  public ResourcePtr resource(RType rType, boolean bit) {
      byte HASH = 1, GUID = 2;
      if (revision <= 0x180) {
          HASH = 2;
          GUID = 1;
      }
      
      byte type;
      
      if (revision < 0x230) bit = true;
      if (revision >= 0x230 && revision <= 0x25b && !bit) this.int8();
      
      if (bit) type = int8();
      else if (revision > ENCODED_REVISION) type = (byte) int16();
      else type = (byte) int32();
      
      ResourcePtr resource = new ResourcePtr();
      resource.type = rType;
      
      if (type == GUID) resource.GUID = uint32();
      else if (type == HASH) resource.hash = bytes(0x14);
      else return null;
      
      return resource;
  }
  
  public long varint() {
    long result = 0, shift = 0, i = 0;
    while (this.offset + i < this.length) {
      int b = int8();
      result |= (b & 0x7FL) << shift;
      shift += 7L;
      if ((b & 0x80L) == 0L)
        break; 
      i++;
    } 
    return result;
  }
  
  public String str(int size) {
    if (size == 0) return "";
    return new String(bytes(size)).replace("\0", "");
  }
  
  public String str16() {
      int size = int32();
      if (revision < ENCODED_REVISION) size *= 2;
      return str(size);
  }
  
  public String str8() {
      int size = int32();
      if (revision > ENCODED_REVISION) size /= 2;
      return str(size);
  }
  
  public void overwrite(byte[] data) {
    for (int i = 0; i < data.length; i++)
      this.data[this.offset + i] = data[i]; 
  }
  
  public void seek(int pos) {
    this.offset = pos;
  }
  
  public void forward(int pos) {
    this.offset += pos;
  }
  
  public void rewind(int pos) {
    this.offset -= pos;
  }
}
