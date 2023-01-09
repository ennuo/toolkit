package toolkit.utilities;

import toolkit.windows.utilities.SlowOpGUI;

public abstract class SlowOp {
    /**
     * Task to run in another thread.
     * @return Error/Success code
     */
    public abstract int run(SlowOpGUI state);
}
