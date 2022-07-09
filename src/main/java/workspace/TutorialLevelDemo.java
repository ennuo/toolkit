package workspace;

import cwlib.resources.RLevel;
import cwlib.types.Resource;

public class TutorialLevelDemo {
    public static void main(String[] args) {
        Resource resource = new Resource("E:/work/sample/blank_level_large.bin");
        RLevel level = resource.loadResource(RLevel.class);
    }
}
