package ennuo.craftworld.things;

public interface Part {
    public static long PART_FLAG = 0;
    public int INDEX = -1;    
    public void Serialize(Serializer serializer);
    public void Deserialize(Serializer serializer);
}
