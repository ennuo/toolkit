package cwlib.io.gson;

import cwlib.enums.ResourceType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GsonResourceType
{
    ResourceType value();
}
