package toolkit.functions;

import cwlib.resources.RTranslationTable;
import cwlib.singleton.ResourceSystem;

import java.awt.event.ActionEvent;

public class LoadCallbacks
{
    public static void loadTranslationTable(ActionEvent event)
    {
        byte[] data = ResourceSystem.extract(ResourceSystem.getSelected().getEntry());
        if (data == null) return;
        ResourceSystem.setLAMS(new RTranslationTable(data));
    }

    public static void loadPalette3D(ActionEvent event)
    {
    }

    public static void loadLevel3D(ActionEvent event)
    {
    }

    public static void loadModel3D(ActionEvent event)
    {
    }
}
