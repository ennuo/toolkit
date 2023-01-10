package toolkit.utilities;

import toolkit.windows.utilities.SlowOpGUI;

public interface SlowOp {
    /**
     * Task to run in another thread.
     * @return Error/Success code
     */
    public abstract int run(SlowOpGUI state);
}
