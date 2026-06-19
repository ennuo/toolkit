package sync;

public class MessageResponse 
{
    public static final int OK = 0;
    public static final int FAIL = 0x8000;
    public static final int BAD_REQUEST = 0x8001;
    public static final int UNAUTHORIZED = 0x8002;
    public static final int FORBIDDEN = 0x8003;
    public static final int NOT_FOUND = 0x8004;
    public static final int METHOD_NOT_ALLOWED = 0x8005;
}
