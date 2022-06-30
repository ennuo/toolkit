package cwlib.structs.things.components.shapes;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.parts.PShape;

public class Contact implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public PShape shape;
    public byte flags;

    @SuppressWarnings("unchecked")
    @Override public Contact serialize(Serializer serializer, Serializable structure) {
        Contact contact = (structure == null) ? new Contact() : (Contact) structure;

        contact.shape = serializer.reference(contact.shape, PShape.class);
        contact.flags = serializer.i8(contact.flags);

        return contact;
    }

    @Override public int getAllocatedSize() {
        int size = Contact.BASE_ALLOCATION_SIZE;
        if (this.shape != null)
            size += shape.getAllocatedSize();
        return size;
    }
}
