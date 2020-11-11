package ennuo.craftworld.resources;

import ennuo.craftworld.memory.Resource;

public class Animation extends Resource {
    
    public Animation(byte[] data) {
        super(data);
        if (this.data == null) {
            System.out.println("No data provided to Animation constructor");
            return;
        }
        process();
    }
    
    private void process() {
        decompress(true);
        
    }
}
