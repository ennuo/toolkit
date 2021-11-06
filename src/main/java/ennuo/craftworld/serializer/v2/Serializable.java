package ennuo.craftworld.serializer.v2;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface Serializable {
    public static <T extends Serializable> T serialize(Serializer serializer, T structure, Class<T> clazz) {
        // NOTE(Abz): Holy fuck that's a lot of exceptions, don't I always initialize the structure
        // in the serialization functions anyway, do I really need this?
        
        try {
            if (structure == null) structure = clazz.getDeclaredConstructor().newInstance();
            return structure.serialize(serializer, structure);
        } catch (InstantiationException ex) {
            Logger.getLogger(Serializable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Serializable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Serializable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(Serializable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(Serializable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Serializable.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public abstract <T extends Serializable> T serialize(Serializer serializer, T structure);
}
