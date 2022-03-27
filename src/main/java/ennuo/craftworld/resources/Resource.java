package ennuo.craftworld.resources;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.SerializationMethod;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.TextureInfo;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.utilities.Compressor;
import ennuo.craftworld.utilities.TEA;
import ennuo.toolkit.utilities.Globals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Resource {
    public ResourceType type = ResourceType.INVALID;
    public SerializationMethod method = SerializationMethod.UNKNOWN;
    public TextureInfo textureInfo;
    public Revision revision;
    private boolean isCompressed = true;
    public byte compressionFlags = 0;
    public Data handle = null;
    public ArrayList<ResourceDescriptor> dependencies = new ArrayList<>();
    
    public Resource(){}
    
    public Resource(Output output) {
        output.shrink();
        this.revision = output.revision;
        this.dependencies = output.dependencies;
        this.method = SerializationMethod.BINARY;
        this.compressionFlags = output.compressionFlags;
        this.handle = new Data(output.buffer, output.revision);
        this.handle.compressionFlags = output.compressionFlags;
    }
    
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
        if (this.type == ResourceType.INVALID || this.type == ResourceType.STATIC_MESH) { this.handle.seek(0); return; }
        this.method = SerializationMethod.getValue(this.handle.str(1));
        if (this.method == SerializationMethod.UNKNOWN) { this.handle.seek(0); return; }
        switch (this.method) {
            case BINARY:
            case ENCRYPTED_BINARY:
                this.revision = new Revision(this.handle.i32f());
                this.handle.revision = this.revision;
                int dependencyTableOffset = -1;
                if (this.revision.head >= 0x109) {
                    dependencyTableOffset = this.getDependencies();
                    if (this.revision.head >= 0x189) {
                        if (this.revision.head >= 0x271) { 
                            // NOTE(Aidan): Were they actually added on 0x27a, but how can it be on 0x272 then?!
                            this.revision.branchID = this.handle.i16();
                            this.revision.branchRevision = this.handle.i16();
                            this.handle.revision = revision;
                        }
                        if (this.revision.head >= 0x297 || (this.revision.head == 0x272 && this.revision.branchID != 0)) {
                            this.compressionFlags = this.handle.i8();
                            this.handle.compressionFlags = this.compressionFlags;
                        }
                        this.isCompressed = this.handle.bool();
                    }
                }
                
                if (this.method == SerializationMethod.ENCRYPTED_BINARY) {
                    int size = this.handle.i32f(), padding = 0;
                    if (size % 4 != 0)
                        padding = 4 - (size % 4);
                    this.handle.setData(TEA.decrypt(this.handle.bytes(size + padding)));
                    this.handle.offset += padding;
                }
                
                if (this.isCompressed)
                    Compressor.decompressData(this.handle);
                else if (dependencyTableOffset != -1)
                    this.handle.setData(this.handle.bytes(dependencyTableOffset - this.handle.offset));
                else this.handle.setData(this.handle.bytes(this.handle.length - this.handle.offset));
                
                break;
            case TEXT:
                this.handle.setData(this.handle.bytes(this.handle.length - 4));
                break;
            case TEXTURE:
            case GXT_SIMPLE:
            case GXT_EXTENDED:
                if (this.type != ResourceType.TEXTURE)
                    this.textureInfo = new TextureInfo(this.handle, this.method);
                Compressor.decompressData(this.handle);
                break;
        }
    }
    
    public int registerDependencies(boolean recursive) {
        if (this.method != SerializationMethod.BINARY) return 0;
        int missingDependencies = 0;
        for (ResourceDescriptor dependency : this.dependencies) {
            FileEntry entry = Globals.findEntry(dependency);
            if (entry == null) {
                missingDependencies++;
                continue;
            }
            if (recursive && this.type != ResourceType.SCRIPT) {
                byte[] data = Globals.extractFile(dependency);
                if (data != null) {
                    Resource resource = new Resource(data);
                    if (resource.method == SerializationMethod.BINARY) {
                        entry.hasMissingDependencies = resource.registerDependencies(recursive) != 0;
                        entry.canReplaceDecompressed = true;
                        entry.dependencies = resource.dependencies;
                    }
                }
            }
        }
        return missingDependencies;
    }
    
    public void replaceDependency(ResourceDescriptor oldDescriptor, ResourceDescriptor newDescriptor) {
        if (oldDescriptor.equals(newDescriptor)) return;
        int index = this.dependencies.indexOf(oldDescriptor);
        if (index == -1) return;
        
        ResourceType type = oldDescriptor.type;
        boolean isFSB = type.equals(ResourceType.FILENAME);
        byte[] oldDescBuffer, newDescBuffer;
        
        // Music dependencies are actually the GUID dependencies of a script,
        // so they don't have the same structure for referencing.
        if (type.equals(ResourceType.MUSIC_SETTINGS) || isFSB) {
            oldDescBuffer = Bytes.createGUID(oldDescriptor.GUID, this.compressionFlags);
            newDescBuffer = Bytes.createGUID(newDescriptor.GUID, this.compressionFlags);
        } else {
            oldDescBuffer = Bytes.createResourceReference(oldDescriptor, this.revision, this.compressionFlags);
            newDescBuffer = Bytes.createResourceReference(newDescriptor, this.revision, this.compressionFlags);
        }
        
        
        if (this.type == ResourceType.PLAN) {
            Plan plan = new Plan(this);
            Data thingData = new Data(plan.thingData, this.revision);
            Bytes.ReplaceAll(thingData, oldDescBuffer, newDescBuffer);
            plan.thingData = thingData.data;
            
            if (isFSB && plan.details != null) {
                if (oldDescriptor.GUID == plan.details.highlightSound)
                    plan.details.highlightSound = newDescriptor.GUID;
            }
            
            this.handle.setData(plan.build(this.revision, this.compressionFlags, false));
        }
        Bytes.ReplaceAll(this.handle, oldDescBuffer, newDescBuffer);
        
        // Remove the dependency from the array if it's effectively null.
        if (newDescriptor == null || newDescriptor.GUID == 0)
            this.dependencies.remove(index);
        else this.dependencies.set(index, newDescriptor);
    }
    
    private int getDependencies() {
        int dependencyTableOffset = this.handle.i32f();
        int originalOffset = this.handle.offset;
        this.handle.offset = dependencyTableOffset;
        
        int size = this.handle.i32f();
        this.dependencies = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            ResourceDescriptor descriptor = new ResourceDescriptor();
            switch (this.handle.i8()) {
                case 1:
                    descriptor.hash = this.handle.sha1();
                    break;
                case 2:
                    descriptor.GUID = this.handle.u32f();
                    break;
            }
            descriptor.type = ResourceType.fromType(this.handle.i32f());
            this.dependencies.add(descriptor);
        }
        
        this.handle.offset = originalOffset;
        
        return dependencyTableOffset;
    }
    
    public static byte[] compressToResource(byte[] data, Revision revision, byte compressionFlags, ResourceType type, ArrayList<ResourceDescriptor> dependencies) {
        Resource resource = new Resource();
        resource.handle = new Data(data, revision);
        resource.compressionFlags = compressionFlags;
        resource.revision = revision;
        resource.type = type;
        if (resource.type == ResourceType.LOCAL_PROFILE)
            resource.method = SerializationMethod.ENCRYPTED_BINARY;
        else resource.method = SerializationMethod.BINARY;
        resource.dependencies = dependencies;
        return resource.compressToResource();
    }
    
    public static byte[] compressToResource(Output data, ResourceType type) {
        Resource resource = new Resource(data);
        resource.type = type;
        if (type == ResourceType.LOCAL_PROFILE)
            resource.method = SerializationMethod.ENCRYPTED_BINARY;
        return resource.compressToResource();
    }
    
    public byte[] compressToResource() {
        if (this.type == ResourceType.STATIC_MESH) return this.handle.data;
        Output output = new Output(this.dependencies.size() * 0x1c + this.handle.length + 0x50);
        
        if (this.method == SerializationMethod.TEXT) {
            output.str(this.type.header + this.method.value + '\n');
            output.bytes(this.handle.data);
            output.shrink();
            return output.buffer;
        }
        
        output.str(this.type.header + this.method.value);
        output.i32f(this.revision.head);
        if (this.revision.head >= 0x109) {
            output.i32f(0); // Dummy value for dependency table offset.
            if (this.revision.head >= 0x189) {
                if (this.revision.head >= 0x271) {
                    output.i16(this.revision.branchID);
                    output.i16(this.revision.branchRevision);
                }
                if (this.revision.head >= 0x297 || (this.revision.head == 0x272 && this.revision.branchID != 0))
                    output.i8(this.compressionFlags);
                output.bool(this.isCompressed);
            }
            
            byte[] data = this.handle.data;
            if (this.isCompressed) data = Compressor.getCompressedStream(this.handle.data);
            if (this.method == SerializationMethod.ENCRYPTED_BINARY) {
                int size = data.length;
                if (size % 4 != 0) {
                    int padding = 4 - (size % 4);
                    byte[] paddedData = new byte[padding + size];
                    System.arraycopy(data, 0, paddedData, padding, size);
                    data = paddedData;
                }
                data = TEA.encrypt(data);
                output.i32f(size);
            }
            output.bytes(data);
            
            int dependencyTableOffset = output.offset;
            output.offset = 0x8;
            output.i32f(dependencyTableOffset);
            output.offset = dependencyTableOffset;
            
            output.i32f(this.dependencies.size());
            for (ResourceDescriptor dependency : this.dependencies) {
                if (dependency.GUID != -1) {
                    output.i8((byte) 2);
                    output.u32f(dependency.GUID);
                } else if (dependency.hash != null) {
                    output.i8((byte) 1);
                    output.sha1(dependency.hash);
                }
                output.i32f(dependency.type.value);
            }
        }
        
        output.shrink();
        return output.buffer;
    }
}
