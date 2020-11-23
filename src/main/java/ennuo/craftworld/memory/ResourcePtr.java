package ennuo.craftworld.memory;

import ennuo.craftworld.resources.enums.RType;

public class ResourcePtr {

    public ResourcePtr() {}
    public ResourcePtr(RType type, String str) {
        long number = -1;
        byte[] bytes = null;

        if (str.startsWith("0x"))
            number = Long.parseLong(str.substring(2), 16);
        else if (str.startsWith("g"))
            number = Long.parseLong(str.substring(1));
        else if (str.startsWith("h"))
            bytes = Bytes.toBytes(str.substring(1));
        else bytes = Bytes.toBytes(str);

        this.type = type;

        if (bytes != null) hash = bytes;
        else if (number != -1) GUID = number;

    }
    public ResourcePtr(long GUID, RType type) {
        this.GUID = GUID;
        this.type = type;
    }
    public ResourcePtr(byte[] SHA1, RType type) {
        this.hash = SHA1;
        this.type = type;
    }

    public RType type = RType.FILE_OF_BYTES;
    public long GUID = -1;
    public byte[] hash = null;

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        return ((ResourcePtr) o).toString().equals(toString());
    }

    @Override
    public String toString() {
        if (hash != null) return "h" + Bytes.toHex(hash);
        else if (GUID != -1) return "g" + GUID;
        return "null";
    }
}