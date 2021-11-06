package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;

public class UserCreatedDetails {
    public static int MAX_SIZE = 0x400;
    
    public String title;
    public String description;
    
    public UserCreatedDetails() {}
    public UserCreatedDetails(Data data) {
        title = data.str16();
        description = data.str16();
    }
    
    public void serialize(Output output) {
        output.str16(title);
        output.str16(description);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof UserCreatedDetails)) return false;
        UserCreatedDetails d = (UserCreatedDetails)o;
        return (d.title.equals(title) && d.description.equals(description));
    }
    
    
}
