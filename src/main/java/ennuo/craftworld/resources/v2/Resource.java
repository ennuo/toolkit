package ennuo.craftworld.resources.v2;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.enums.SerializationMethod;

public class Resource {
    public ResourceType type = ResourceType.INVALID;
    public SerializationMethod method = SerializationMethod.UNKNOWN;
    public int revision = 0x132;
    private int dependenciesOffset = -1;
    private int branchDescription = 0;
    private boolean unknownFlag = true;
    private byte compressionFlags = 0x7;
    public Data handle = null;
    public ResourcePtr[] dependencies = new ResourcePtr[0];
    
    public Resource(String path) {
        this.handle = new Data(path);
        this.process();
    }
    
    public Resource(byte[] data) {
        this.handle = new Data(data);
        this.process();
    }
    
    private void process() {
        if (this.handle == null || this.handle.length < 0xb) return;
        this.type = ResourceType.fromMagic(this.handle.str(3));
        if (type == ResourceType.INVALID) { this.handle.seek(0); return; }
        this.method = SerializationMethod.getValue(this.handle.str(1));
        if (this.method == SerializationMethod.UNKNOWN) { this.handle.seek(0); return; }
        if (this.method == SerializationMethod.BINARY || this.method == SerializationMethod.ENCRYPTED_BINARY) {
            this.revision = this.handle.i32f();
            if (this.revision >= 0x109) {
                this.dependenciesOffset = this.handle.i32f();
                if (this.revision >= 0x189) {
                    if (this.revision >= 0x271) this.branchDescription = this.handle.i32f();
                    if (this.revision >= 0x297 || (this.revision == 0x272 && this.branchDescription != 0))
                        this.compressionFlags = this.handle.i8();
                    this.unknownFlag = this.handle.bool();
                }
            } 
        }
    }
}
