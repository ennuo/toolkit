package cwlib.io.gson;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(GsonRevisions.class)
public @interface GsonRevision {
    public int min() default -1;
    public int max() default -1;
    public int branch() default -1;
    public boolean lbp3() default false;
}
