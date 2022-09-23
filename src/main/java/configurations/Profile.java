package toolkit.configurations;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    public String name = "Unnamed Profile";
    public List<String> archives = new ArrayList<>();
    public List<String> databases = new ArrayList<>();
    public boolean useLegacyFileDialogue = false;
    public boolean debug = false;
    public long language = 0;
    
    @Override public String toString() { return this.name; }
}
