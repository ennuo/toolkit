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

public class PlayerMetrics implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    @GsonRevision(min=0x170)
    private int totalTime, editingTime, playingTime, idlingTime;
    
    @GsonRevision(min=0x184)
    private int multiplayerGamesCount;

    @GsonRevision(branch=0x4431, min=Revisions.D1_LEVEL_TIMES_MAP)
    private HashMap<Integer, Integer> levelTimesMap;

    @GsonRevision(branch=0x4431, min=Revisions.D1_LEVEL_TIMES_MAP)
    private int totalLevelTime;

    @GsonRevision(min=0x1ca)
    private float playLadderPoints;

    @GsonRevision(min=0x1ac)
    private int storyLevelCompletionCount;

    @GsonRevision(min=0x1f8)
    private int communityLevelCompletionCount;

    @GsonRevision(min=0x1ac)
    private int levelCompletionCount;

    @GsonRevision(min=0x1f8)
    private int levelsTaggedCount;

    @GsonRevision(min=0x1ca)
    private int gamesWithRandomPlayersCount;

    @GsonRevision(min=0x1df)
    private float pointsCollected;

    @GsonRevision(min=0x1ea)
    private long[] stats;
    
    @SuppressWarnings("unchecked")
    @Override public PlayerMetrics serialize(Serializer serializer, Serializable structure) {
        PlayerMetrics metrics = (structure == null) ? new PlayerMetrics() : (PlayerMetrics) structure;

        Revision revision = serializer.getRevision();
        int head = revision.getVersion();

        if (head > 0x16f) {
            metrics.totalTime = serializer.i32(metrics.totalTime);
            metrics.editingTime = serializer.i32(metrics.editingTime);
            metrics.playingTime = serializer.i32(metrics.playingTime);
            metrics.idlingTime = serializer.i32(metrics.idlingTime);
        }

        if (head > 0x183)
            metrics.multiplayerGamesCount = serializer.i32(metrics.multiplayerGamesCount);

        if (revision.has(Branch.DOUBLE11, Revisions.D1_LEVEL_TIMES_MAP)) {
            if (serializer.isWriting()) {
                MemoryOutputStream stream = serializer.getOutput();
                Set<Integer> keys = metrics.levelTimesMap.keySet();
                stream.i32(keys.size());
                for (Integer key : keys) {
                    stream.i32(key);
                    stream.i32(metrics.levelTimesMap.get(key));
                }
            } else {
                MemoryInputStream stream = serializer.getInput();
                int count = stream.i32();
                metrics.levelTimesMap = new HashMap<Integer, Integer>(count);
                for (int i = 0; i < count; ++i)
                    metrics.levelTimesMap.put(stream.i32(), stream.i32());
            }

            metrics.totalLevelTime = serializer.i32(metrics.totalLevelTime);
        }

        if (head > 0x1c9)
            metrics.playLadderPoints = serializer.f32(metrics.playLadderPoints);

        if (head > 0x1ab)
            metrics.storyLevelCompletionCount = serializer.i32(metrics.storyLevelCompletionCount);

        if (head > 0x1f7)
            metrics.communityLevelCompletionCount = serializer.i32(metrics.communityLevelCompletionCount);

        if (head > 0x1ab)
            metrics.levelCompletionCount = serializer.i32(metrics.levelCompletionCount);

        if (head > 0x1f7)
            metrics.levelsTaggedCount = serializer.i32(metrics.levelsTaggedCount);

        if (head > 0x1c9)
            metrics.gamesWithRandomPlayersCount = serializer.i32(metrics.gamesWithRandomPlayersCount);
        
        if (((head > 0x1de || revision.isLeerdammer()) && head < 0x2cb) && revision.before(Branch.LEERDAMMER, Revisions.LD_REMOVED_ENEMY_STAT))
            serializer.i32(0); // enemiesKilled

        if (head > 0x1de)
            metrics.pointsCollected = serializer.f32(metrics.pointsCollected);

        if (head >= 0x1ea) 
            metrics.stats = serializer.longarray(metrics.stats);

        return metrics;
    }

    @Override public int getAllocatedSize() {
        int size = PlayerMetrics.BASE_ALLOCATION_SIZE;
        if (this.stats != null) 
            size += this.stats.length * 0x8;
        if (this.levelTimesMap != null)
            size += (this.levelTimesMap.size() * 0x8);
        return size;
    }
}
