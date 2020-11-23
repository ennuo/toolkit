package ennuo.toolkit.utilities;

public final class KMPMatchUtilities {
  public static int indexOf(byte[] data, byte[] pattern) { return KMPMatchUtilities.indexOf(data, pattern, 0); }
  public static int indexOf(byte[] data, byte[] pattern, int offset) {
    int[] failure = computeFailure(pattern);
    int j = 0;
    if (data.length == 0)
      return -1; 
    for (int i = offset; i < data.length; i++) {
      while (j > 0 && pattern[j] != data[i])
        j = failure[j - 1]; 
      if (pattern[j] == data[i])
        j++; 
      if (j == pattern.length)
        return i - pattern.length + 1; 
    } 
    return -1;
  }
  
  private static int[] computeFailure(byte[] pattern) {
    int[] failure = new int[pattern.length];
    int j = 0;
    for (int i = 1; i < pattern.length; i++) {
      while (j > 0 && pattern[j] != pattern[i])
        j = failure[j - 1]; 
      if (pattern[j] == pattern[i])
        j++; 
      failure[i] = j;
    } 
    return failure;
  }
}
