package toolkit;

import cwlib.resources.RLevel;
import cwlib.resources.RPlan;
import cwlib.singleton.ResourceSystem;
import cwlib.types.SerializedResource;

public class Scratch {
    public static void main(String[] args) {
        ResourceSystem.DISABLE_LOGS = false;
        ResourceSystem.LOG_LEVEL = -1;
        RLevel level = new SerializedResource("C:/Users/Aidan/Desktop/touchnrollstage3.bin").loadResource(RLevel.class);









    }
}
