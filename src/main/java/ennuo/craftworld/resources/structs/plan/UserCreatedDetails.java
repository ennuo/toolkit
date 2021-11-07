package ennuo.craftworld.resources.structs.plan;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class UserCreatedDetails implements Serializable {
    public static int MAX_SIZE = 0x400;
    
    public String title;
    public String description;

    public UserCreatedDetails serialize(Serializer serializer, Serializable structure) {
        UserCreatedDetails details = 
                (structure == null) ? new UserCreatedDetails() : (UserCreatedDetails) structure;
        details.title = serializer.str16(details.title);
        details.description = serializer.str16(details.description);
        return details;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof UserCreatedDetails)) return false;
        UserCreatedDetails d = (UserCreatedDetails)o;
        return (d.title.equals(title) && d.description.equals(description));
    }
}
