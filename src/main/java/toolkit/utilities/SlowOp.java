package toolkit.utilities;

import toolkit.windows.utilities.SlowOpGUI;

public abstract class SlowOp {
    /**
     * Task to run in another thread.
     * @return Error/Success code
     */
    public abstract int run(SlowOpGUI state);

    /**
     * Get the progress of the current task.
     * @return Task progress clamped between 0/100, -1 is indeterminate
     */
    public int getProgress() { return -1; }
}
