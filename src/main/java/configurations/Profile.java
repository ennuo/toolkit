package configurations;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    public String name = "Unnamed Profile";
    public List<String> archives = new ArrayList<>();
    public List<String> databases = new ArrayList<>();
    public List<String> saves = new ArrayList<>();
    public long language = 0;
    
    public Profile() {};
    public Profile(String name) { this.name = name; }
    
    @Override public String toString() { return this.name; }
}
