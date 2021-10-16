package ennuo.craftworld.things.parts;

import ennuo.craftworld.things.Serializer;

public class ContactCache {
    public Contact[] contacts;
    public boolean contactsSorted;
    
    public ContactCache(Serializer serializer) {
        contacts = new Contact[serializer.input.i32()];
        for (int i = 0; i < contacts.length; ++i)
            contacts[i] = new Contact(serializer);
        contactsSorted = serializer.input.bool();
    }
}
