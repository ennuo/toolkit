package toolkit.utilities.services;

import cwlib.enums.ResourceType;
import cwlib.resources.RPlan;
import cwlib.types.data.ResourceInfo;
import cwlib.types.databases.FileEntry;
import toolkit.utilities.ResourceSystem;
import toolkit.windows.Toolkit;

import javax.swing.JTree;

public class PlanService implements ResourceService  {
    public static final int[] HEADERS = { 0x504c4e62 };

    @Override public void process(JTree tree, FileEntry entry, byte[] data) {
        ResourceInfo info = entry.getInfo();
        if (info == null || info.getType() != ResourceType.PLAN || info.getResource() == null);
        RPlan plan = info.getResource();
        JTree selected = ResourceSystem.getSelectedDatabase().getTree();
        if (ResourceSystem.getSelected().getEntry() != entry || selected != tree) return;
        Toolkit.instance.populateMetadata(plan);
    }

    @Override public int[] getSupportedHeaders() { return HEADERS; }
}
