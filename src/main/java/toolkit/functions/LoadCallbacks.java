package toolkit.functions;

import cwlib.enums.ResourceType;
import cwlib.resources.RLevel;
import cwlib.resources.RPalette;
import cwlib.resources.RPlan;
import cwlib.resources.RTranslationTable;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.ResourceInfo;
import cwlib.types.databases.FileEntry;
import editor.gl.RenderSystem;
import java.awt.event.ActionEvent;

public class LoadCallbacks {
    public static void loadTranslationTable(ActionEvent event) {
        byte[] data = ResourceSystem.extract(ResourceSystem.getSelected().getEntry());
        if (data == null) return;
        ResourceSystem.setLAMS(new RTranslationTable(data));
    }
    
    public static void loadPalette3D(ActionEvent event) {
        RLevel level = new RLevel();
        RPalette palette = ResourceSystem.getSelectedResource();
        for (ResourceDescriptor descriptor : palette.planList) {
            byte[] planData = ResourceSystem.extract(descriptor);
            if (planData == null) continue;
            level.addPlan(new Resource(planData).loadResource(RPlan.class));
        }
        RenderSystem.setLevel(level);
    }
    
    public static void loadLevel3D(ActionEvent event) {
        ResourceInfo info = ResourceSystem.getSelected().getEntry().getInfo();
        if (info == null) return;
        RLevel level = info.getResource();
        if (level == null) return;
        RenderSystem.setLevel(level);
    }
    
    public static void loadModel3D(ActionEvent event) {
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        ResourceDescriptor descriptor = new ResourceDescriptor(entry.getSHA1(), ResourceType.MESH);
        RenderSystem.getSceneGraph().addMesh(descriptor);
    }
}
