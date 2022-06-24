package cwlib.enums;

public enum SerializationType {
    UNKNOWN(null),
    BINARY("b"),
    TEXT("t"),
    ENCRYPTED_BINARY("e"),
    COMPRESSED_TEXTURE(" "),
    GTF_SWIZZLED("s"),
    GXT_SWIZZLED("S");
    
    private final String value;
    
    private SerializationType(String value) { this.value = value; }
    
    public String getValue() { return this.value; }
    public static SerializationType fromValue(String value) {
        for (SerializationType type : SerializationType.values()) {
            if (value.equals(type.value)) 
                return type;
        }
        return SerializationType.UNKNOWN;
    }
    
}
