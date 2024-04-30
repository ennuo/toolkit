package cwlib.structs.profile;

import java.util.HashMap;
import java.util.Set;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.types.data.Revision;

public class PlayerMetrics implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    @GsonRevision(min = 0x170)
    private int totalTime, editingTime, playingTime, idlingTime;

    @GsonRevision(min = 0x184)
    private int multiplayerGamesCount;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_LEVEL_TIMES_MAP)
    private HashMap<Integer, Integer> levelTimesMap;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_LEVEL_TIMES_MAP)
    private int totalLevelTime;

    @GsonRevision(min = 0x1ca)
    private float playLadderPoints;

    @GsonRevision(min = 0x1ac)
    private int storyLevelCompletionCount;

    @GsonRevision(min = 0x1f8)
    private int communityLevelCompletionCount;

    @GsonRevision(min = 0x1ac)
    private int levelCompletionCount;

    @GsonRevision(min = 0x1f8)
    private int levelsTaggedCount;

    @GsonRevision(min = 0x1ca)
    private int gamesWithRandomPlayersCount;

    @GsonRevision(min = 0x1df)
    private float pointsCollected;

    @GsonRevision(min = 0x1ea)
    private long[] stats;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int head = revision.getVersion();

        if (head > 0x16f)
        {
            totalTime = serializer.i32(totalTime);
            editingTime = serializer.i32(editingTime);
            playingTime = serializer.i32(playingTime);
            idlingTime = serializer.i32(idlingTime);
        }

        if (head > 0x183)
            multiplayerGamesCount = serializer.i32(multiplayerGamesCount);

        if (revision.has(Branch.DOUBLE11, Revisions.D1_LEVEL_TIMES_MAP))
        {
            if (serializer.isWriting())
            {
                MemoryOutputStream stream = serializer.getOutput();
                Set<Integer> keys = levelTimesMap.keySet();
                stream.i32(keys.size());
                for (Integer key : keys)
                {
                    stream.i32(key);
                    stream.i32(levelTimesMap.get(key));
                }
            }
            else
            {
                MemoryInputStream stream = serializer.getInput();
                int count = stream.i32();
                levelTimesMap = new HashMap<Integer, Integer>(count);
                for (int i = 0; i < count; ++i)
                    levelTimesMap.put(stream.i32(), stream.i32());
            }

            totalLevelTime = serializer.i32(totalLevelTime);
        }

        if (head > 0x1c9)
            playLadderPoints = serializer.f32(playLadderPoints);

        if (head > 0x1ab)
            storyLevelCompletionCount = serializer.i32(storyLevelCompletionCount);

        if (head > 0x1f7)
            communityLevelCompletionCount =
                serializer.i32(communityLevelCompletionCount);

        if (head > 0x1ab)
            levelCompletionCount = serializer.i32(levelCompletionCount);

        if (head > 0x1f7)
            levelsTaggedCount = serializer.i32(levelsTaggedCount);

        if (head > 0x1c9)
            gamesWithRandomPlayersCount =
                serializer.i32(gamesWithRandomPlayersCount);


        if (head > 0x1de && head < 0x2cb)
        {
            if (!revision.isLeerdammer() || revision.before(Branch.LEERDAMMER,
                Revisions.LD_REMOVED_ENEMY_STAT))
            {
                serializer.i32(0); // enemiesKilled
            }
        }

        if (head > 0x1de)
            pointsCollected = serializer.f32(pointsCollected);

        if (head >= 0x1ea)
            stats = serializer.longarray(stats);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PlayerMetrics.BASE_ALLOCATION_SIZE;
        if (this.stats != null)
            size += this.stats.length * 0x8;
        if (this.levelTimesMap != null)
            size += (this.levelTimesMap.size() * 0x8);
        return size;
    }
}
