package cwlib.enums;

public enum SerializationType {
    UNKNOWN(null),
    BINARY("b"),
    TEXT("t"),
    ENCRYPTED_BINARY("e"),
    TEXTURE(" "),
    GXT_SIMPLE("s"),
    GXT_EXTENDED("S");
    
    public final String value;
    
    private SerializationType(String value) { this.value = value; }
    
    public static SerializationType getValue(String value) {
        for (SerializationType type : SerializationType.values()) {
            if (value.equals(type.value)) 
                return type;
        }
        return SerializationType.UNKNOWN;
    }
    
}
