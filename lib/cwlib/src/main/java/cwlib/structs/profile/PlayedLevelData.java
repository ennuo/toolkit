package cwlib.structs.profile;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;

public class PlayedLevelData implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public SlotID slotID;

    @GsonRevision(min = 0x1fe)
    public long lastPlayedTimestamp;

    @GsonRevision(min = 0x201)
    public int[] localHighScore;

    public short playCount, completionCount, acedCount;

    @GsonRevision(min = 0x1c2)
    public CollectableData[] collectables;

    @GsonRevision(min = 0x1e4)
    public int[] videos;

    @GsonRevision(min = 0x364)
    public SlotID[] linkedLevels;

    @GsonRevision(min = 0x364)
    public SlotID[] subLevels;

    @GsonRevision(min = 0x266)
    public SHA1 levelHash;

    @GsonRevision(min = 0x399)
    public short flags;

    /* LBP3 */

    @GsonRevision(lbp3 = true, min = 0x1ad)
    public int bestTime;

    @GsonRevision(lbp3 = true, min = 0x1ad)
    public short multiplayerCompletionCount;

    /* Vita */
    @GsonRevision(branch = 0x4431, min = Revisions.D1_DEFERRED_PLAYS)
    public short deferredPlayCount, deferredPlayCountUploaded;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_UPLOADED_HIGH_SCORE)
    public int[] uploadedLocalHighScore;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_TROPHIES)
    public short goldTrophyCount, silverTrophyCount, bronzeTrophyCount;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();


        slotID = serializer.struct(slotID, SlotID.class);
        if (version > 0x1fd)
            lastPlayedTimestamp = serializer.s64(lastPlayedTimestamp);
        if (version > 0x200)
            localHighScore = serializer.intarray(localHighScore);

        if (version > 0x268 && version < 0x399)
        {
            serializer.bool(false); // Discovered
            serializer.bool(false); // Unlocked
        }

        if (version < 0x269)
        {
            playCount = (short) serializer.i32(playCount);
            completionCount = (short) serializer.i32(completionCount);
            acedCount = (short) serializer.i32(acedCount);
        }
        else
        {
            playCount = serializer.i16(playCount);
            completionCount = serializer.i16(completionCount);
            acedCount = serializer.i16(acedCount);
        }

        if (version > 0x1c1)
            collectables = serializer.array(collectables, CollectableData.class);

        if (version > 0x1e3 && subVersion < 0x106)
            videos = serializer.intarray(videos);

        if (version > 0x363)
        {
            linkedLevels = serializer.array(linkedLevels, SlotID.class);
            subLevels = serializer.array(subLevels, SlotID.class);
        }

        if (version > 0x265)
            levelHash = serializer.sha1(levelHash);

        if (version > 0x265 && version < 0x399)
            serializer.bool(false); // levelHasDLC

        if (version > 0x398)
            flags = serializer.i16(flags);

        if (revision.has(Branch.DOUBLE11, Revisions.D1_DEFERRED_PLAYS))
        {
            deferredPlayCount = serializer.i16(deferredPlayCount);
            deferredPlayCountUploaded = serializer.i16(deferredPlayCountUploaded);
            if (revision.has(Branch.DOUBLE11, Revisions.D1_UPLOADED_HIGH_SCORE))
                uploadedLocalHighScore = serializer.intarray(uploadedLocalHighScore);
            if (revision.has(Branch.DOUBLE11, Revisions.D1_TROPHIES))
            {
                goldTrophyCount = serializer.i16(goldTrophyCount);
                silverTrophyCount = serializer.i16(silverTrophyCount);
                bronzeTrophyCount = serializer.i16(bronzeTrophyCount);
            }
        }

        if (subVersion >= 0x1ad)
        {
            bestTime = serializer.i32(bestTime);
            multiplayerCompletionCount = serializer.i16(multiplayerCompletionCount);
        }
    }

    @Override
    public int getAllocatedSize()
    {
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
