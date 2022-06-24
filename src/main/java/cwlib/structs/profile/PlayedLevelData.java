package cwlib.structs.profile;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;

public class PlayedLevelData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public SlotID slotID;
    public long lastPlayedTimestamp;
    public int[] localHighScore;
    public short playCount, completionCount, acedCount;
    public CollectableData[] collectables;
    public int[] videos;
    public SlotID[] linkedLevels;
    public SlotID[] subLevels;
    public SHA1 levelHash;
    public short flags;

    /* LBP3 */

    public int bestTime;
    public short multiplayerCompletionCount;

    /* Vita */
    public short deferredPlayCount;
    public short deferredPlayCountUploaded;
    public int[] uploadedLocalHighScore;
    public short goldTrophyCount, silverTrophyCount, bronzeTrophyCount;

    @SuppressWarnings("unchecked")
    @Override public PlayedLevelData serialize(Serializer serializer, Serializable structure) {
        PlayedLevelData data = (structure == null) ? new PlayedLevelData() : (PlayedLevelData) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();


        data.slotID = serializer.struct(data.slotID, SlotID.class);
        if (version > 0x1fd)
            data.lastPlayedTimestamp = serializer.i64(data.lastPlayedTimestamp);
        if (version > 0x200)
            data.localHighScore = serializer.intarray(data.localHighScore);

        if (version > 0x268 && version < 0x399) {
            serializer.bool(false); // Discovered
            serializer.bool(false); // Unlocked
        }

        if (version < 0x269) {
            data.playCount = (short) serializer.i32(data.playCount);
            data.completionCount = (short) serializer.i32(data.completionCount);
            data.acedCount = (short) serializer.i32(data.acedCount);
        } else {
            data.playCount = serializer.i16(data.playCount);
            data.completionCount = serializer.i16(data.completionCount);
            data.acedCount = serializer.i16(data.acedCount);
        }

        if (version > 0x1c1)
            data.collectables = serializer.array(data.collectables, CollectableData.class);

        if (version > 0x1e3 && subVersion < 0x106)
            data.videos = serializer.intarray(data.videos);

        if (version > 0x363) {
            data.linkedLevels = serializer.array(data.linkedLevels, SlotID.class);
            data.subLevels = serializer.array(data.subLevels, SlotID.class);
        }

        if (version > 0x265)
            data.levelHash = serializer.sha1(data.levelHash);

        if (version > 0x265 && version < 0x399)
            serializer.bool(false); // levelHasDLC

        if (version > 0x398)
            data.flags = serializer.i16(data.flags);

        if (revision.has(Branch.DOUBLE11, Revisions.D1_DEFERRED_PLAYS)) {
            data.deferredPlayCount = serializer.i16(data.deferredPlayCount);
            data.deferredPlayCountUploaded = serializer.i16(data.deferredPlayCountUploaded);
            if (revision.has(Branch.DOUBLE11, Revisions.D1_UPLOADED_HIGH_SCORE))
                data.uploadedLocalHighScore = serializer.intarray(data.uploadedLocalHighScore);
            if (revision.has(Branch.DOUBLE11, Revisions.D1_TROPHIES)) {
                data.goldTrophyCount = serializer.i16(data.goldTrophyCount);
                data.silverTrophyCount = serializer.i16(data.silverTrophyCount);
                data.bronzeTrophyCount = serializer.i16(data.bronzeTrophyCount);
            }
        }

        if (subVersion >= 0x1ad) {
            data.bestTime = serializer.i32(data.bestTime);
            data.multiplayerCompletionCount = serializer.i16(data.multiplayerCompletionCount);
        }

        return data;
    }

    @Override public int getAllocatedSize() {
        int size = PlayedLevelData.BASE_ALLOCATION_SIZE;
        if (this.localHighScore != null)
            size += (this.localHighScore.length * 4);
        if (this.collectables != null)
            size += (this.collectables.length * CollectableData.BASE_ALLOCATION_SIZE);
        if (this.linkedLevels != null)
            size += (this.linkedLevels.length * SlotID.BASE_ALLOCATION_SIZE);
        if (this.subLevels != null)
            size += (this.subLevels.length * SlotID.BASE_ALLOCATION_SIZE);
        if (this.uploadedLocalHighScore != null)
            size += (this.uploadedLocalHighScore.length * 4);
        return size;
    }
}
