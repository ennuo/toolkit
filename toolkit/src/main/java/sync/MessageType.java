package sync;

public final class MessageType 
{
    public static final int SERVER_INFO = 0x80000000;
    public static final int LOGIN = 0x80000001;
    public static final int USER_INFO = 0x80000002;

    public static final int DEPOT_LIST = 0x80100000;
    public static final int DEPOT = 0x80100001;
    public static final int CREATE_DEPOT = 0x80100002;
    public static final int DELETE_DEPOT = 0x80100003;
    public static final int UPDATE_DEPOT = 0x80100004;
    public static final int COMMIT = 0x80100005;
    public static final int RECENT_ACTIVITY = 0x80100006;
    public static final int COMMIT_INFO = 0x80100007;

    public static final int FILTER_RESOURCES = 0x80300000;
    public static final int UPLOAD_RESOURCE = 0x80300001;
    public static final int DOWNLOAD = 0x80300002;

    public static final int ON_DEPOT_CREATED = 0x80101000;
    public static final int ON_DEPOT_UPDATED = 0x80101001;
    public static final int ON_DEPOT_DELETED = 0x80101002;
    public static final int ON_DEPOT_COMMIT = 0x80101003;
}
