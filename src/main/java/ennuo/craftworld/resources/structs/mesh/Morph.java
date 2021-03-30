package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.Vector3f;

public class Morph {
    public Vector3f[] vertices;
    public String name;
    
    public Morph(String name) {
        this.name = name;
    }
    
    public void serialize(Output output) {
        for (Vector3f vertex : vertices) {
            output.v3(vertex);
            output.float32(0);
        }
    }
    
    
}
