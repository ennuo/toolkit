package cwlib.io.gson;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GsonRevisions {
    GsonRevision[] value();
}
