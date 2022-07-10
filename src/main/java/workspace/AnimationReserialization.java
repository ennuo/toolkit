package workspace;

import cwlib.enums.CompressionFlags;
import cwlib.resources.RAnimation;
import cwlib.types.Resource;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;

public class AnimationReserialization {
    public static void main(String[] args) {
        RAnimation animation = new Resource("C:/Users/Rueezus/Desktop/static_pose.anim").loadResource(RAnimation.class);
        byte[] data = Resource.compress(animation.build(new Revision(0x000703e7), CompressionFlags.USE_NO_COMPRESSION));
        FileIO.write(data, "E:/ps3/dev_hdd0/game/lbp3debug/usrdir/content_library/mods/htr/animations/3rddegree.anim");


    }
}
