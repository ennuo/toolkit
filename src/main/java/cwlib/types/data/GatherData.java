package cwlib.types.data;

import cwlib.enums.ResourceType;

public class GatherData {
    private final String path;
    private final SHA1 sha1;
    private final byte[] data;
    private final GUID guid;

    public GatherData(String path, ResourceDescriptor descriptor, byte[] data) {
        this.path = path;

        if (descriptor.isHash())
            this.sha1 = descriptor.getSHA1();
        else
            this.sha1 = SHA1.fromBuffer(data);

        this.data = data;
        this.guid = descriptor.getGUID();
    }

    public GatherData(String path, byte[] data) {
        this.path = path;
        this.data = data;
        this.sha1 = SHA1.fromBuffer(data);
        this.guid = null;
    }

    public GatherData(String path, SHA1 sha1, byte[] data) {
        this(path, new ResourceDescriptor(sha1, ResourceType.INVALID), data);
    }

    public GatherData(String path, GUID guid, byte[] data) {
        this(path, new ResourceDescriptor(guid, ResourceType.INVALID), data);
    }

    public GatherData(String path, GUID guid, SHA1 sha1, byte[] data) {
        this.path = path;
        this.guid = guid;
        this.sha1 = sha1;
        this.data = data;
    }

    public String getPath() { return this.path; }
    public SHA1 getSHA1() { return this.sha1; }
    public byte[] getData() { return this.data; }
    public GUID getGUID() { return this.guid; }
}