package cwlib.structs.profile;

import java.util.HashMap;
import java.util.Set;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.types.data.Revision;

public class PlayerMetrics implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    private int totalTime, editingTime, playingTime, idlingTime;
    private int multiplayerGamesCount;
    private HashMap<Integer, Integer> levelTimesMap;
    private int totalLevelTime;
    private float playLadderPoints;
    private int storyLevelCompletionCount;
    private int communityLevelCompletionCount;
    private int levelCompletionCount;
    private int levelsTaggedCount;
    private int gamesWithRandomPlayersCount;
    private float pointsCollected;
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

        if (revision.isAfterVitaRevision(0x5f)) {
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
        
        if (((head > 0x1de || revision.isLeerdammer()) && head < 0x2cb) && !revision.isAfterLeerdamerRevision(0x10))
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
