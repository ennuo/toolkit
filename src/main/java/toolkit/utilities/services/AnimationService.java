package toolkit.utilities.services;

import cwlib.resources.RAnimation;
import cwlib.types.Resource;
import cwlib.types.databases.FileEntry;
import javax.swing.JTree;

public class AnimationService implements ResourceService  {
    public static final int[] HEADERS = { 0x414e4d62 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        RAnimation animation = entry.getResource("animation");
        if (animation == null) {
            try { animation = new RAnimation(new Resource(data).handle); entry.setResource("animation", animation); }
            catch (Exception e) { System.err.println("There was an error processing RAnimation file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
