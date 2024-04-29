package cwlib.gl;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

public class RenderJob
{
    /**
     * Job completion latch.
     */
    public CountDownLatch latch = new CountDownLatch(1);

    /**
     * The job function to run.
     */
    public BiConsumer<RenderJobManager, Object> function;

    /**
     * User argument provided to function.
     */
    public Object userData;

    public RenderJob(BiConsumer<RenderJobManager, Object> function, Object userData)
    {
        this.function = function;
        this.userData = userData;
    }
}
