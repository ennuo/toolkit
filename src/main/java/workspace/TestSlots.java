package workspace;

import cwlib.enums.CompressionFlags;
import cwlib.enums.GameProgressionStatus;
import cwlib.resources.RSlotList;
import cwlib.structs.slot.Slot;
import cwlib.types.Resource;
import cwlib.types.archives.FileArchive;
import cwlib.types.data.Revision;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileEntry;

public class TestSlots {
    public static void main(String[] args) {
        FileArchive archive = new FileArchive("E:/PS3/dev_hdd0/game/NPUA80472/USRDIR/data.farc");
        FileDB database = new FileDB("E:/PS3/dev_hdd0/game/NPUA80472/USRDIR/output/brg_patch.map");

        FileEntry entry = database.get(21480);

        RSlotList list = archive.loadResource(entry.getSHA1(), RSlotList.class);
        for (Slot slot : list)
            slot.gameProgressionState = GameProgressionStatus.GAME_PROGRESSION_COMPLETED;

        Revision revision = new Revision(0x272, 0x4c44, 0x0017);

        byte[] data = Resource.compress(list.build(revision, CompressionFlags.USE_NO_COMPRESSION), false);

        entry.setDetails(data);
        archive.add(data);

        archive.save();
        database.save();
    }
}
