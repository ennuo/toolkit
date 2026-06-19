package configurations;

public class SyncSettings 
{
    public boolean enabled = false;
    public String address = "127.0.0.1";
    public int port = 16723;
    public String username = "";
    public String password = "";
    
    public boolean hasCredentials()
    {
        return 
            username != null && !username.isEmpty() &&
            password != null && !password.isEmpty();
    }
}
