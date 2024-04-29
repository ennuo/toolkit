package cwlib.io.gson;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(GsonRevisions.class)
public @interface GsonRevision
{
    int min() default -1;

    int max() default -1;

    int branch() default -1;

    boolean lbp3() default false;
}
