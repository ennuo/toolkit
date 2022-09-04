package cwlib.io.gson;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import cwlib.enums.ResourceType;

@Retention(RetentionPolicy.RUNTIME)
public @interface GsonResourceType {
    public ResourceType value();
}
