package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import java.util.Arrays;

public class PhotoData {
    public static int MAX_SIZE = 0x69 + PhotoMetadata.MAX_SIZE + (PhotoUser.MAX_SIZE * 4);
    
    public ResourcePtr icon;
    public ResourcePtr sticker;
    public PhotoMetadata photoMetadata = new PhotoMetadata();
    public ResourcePtr painting;
    
    public PhotoData(Data data) {
        icon = data.resource(RType.TEXTURE, true);
        sticker = data.resource(RType.TEXTURE, true);
        photoMetadata = new PhotoMetadata(data);
        if (data.revision > 0x38b)
            painting = data.resource(RType.PAINTING, true);
    }
    
    public void serialize(Output output) {
        output.resource(icon, true);
        output.resource(sticker, true);
        photoMetadata.serialize(output);
        if (output.revision > 0x38b)
            output.resource(painting, true);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PhotoData)) return false;
        PhotoData d = (PhotoData)o;
        return (
                icon.equals(d.icon) &&
                sticker.equals(d.sticker) &&
                photoMetadata.equals(d.photoMetadata) &&
                painting.equals(d.painting)
        );
    }
}
