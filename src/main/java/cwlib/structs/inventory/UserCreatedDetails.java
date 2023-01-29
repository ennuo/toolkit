package cwlib.structs.inventory;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class UserCreatedDetails implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public String name;
    public String description;

    public UserCreatedDetails(){};
    public UserCreatedDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @SuppressWarnings("unchecked")
    @Override public UserCreatedDetails serialize(Serializer serializer, Serializable structure) {
        UserCreatedDetails details = (structure == null) ? new UserCreatedDetails() : (UserCreatedDetails) structure;

        details.name = serializer.wstr(details.name);
        details.description = serializer.wstr(details.description);

        return details;
    }

    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof UserCreatedDetails)) return false;
        UserCreatedDetails details = (UserCreatedDetails) other;
        return details.name.equals(this.name) && details.description.equals(this.description);
    }

    @Override public int getAllocatedSize() {
        int size = BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += (this.name.length() * 2);
        if (this.description != null)
            size += (this.description.length() * 2);
        return size;
    }
}
