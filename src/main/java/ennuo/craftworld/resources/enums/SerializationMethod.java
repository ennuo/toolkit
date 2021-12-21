package ennuo.craftworld.resources.enums;

public enum SerializationMethod {
    UNKNOWN(null),
    BINARY("b"),
    TEXT("t"),
    ENCRYPTED_BINARY("e"),
    TEXTURE(" "),
    GXT_SIMPLE("s"),
    GXT_EXTENDED("S");
    
    public final String value;
    
    private SerializationMethod(String value) { this.value = value; }
    
    public static SerializationMethod getValue(String value) {
        for (SerializationMethod type : SerializationMethod.values()) {
            if (value.equals(type.value)) 
                return type;
        }
        return SerializationMethod.UNKNOWN;
    }
    
}
