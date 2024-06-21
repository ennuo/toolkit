package utils;

import java.util.Arrays;
import java.util.Objects;

public class MidiChannelStack {
    private final int offset;
    private final Integer[] channels;

    public MidiChannelStack(int size, int offset) {
        this.offset = offset;
        this.channels = new Integer[size];
    }

    public boolean isFull() {
        return Arrays.stream(this.channels).noneMatch(Objects::isNull);
    }

    public int push(int eventId) {
        for (int channelId = 0; channelId < this.channels.length; channelId++) {
            Integer current = this.channels[channelId];
            if (current != null) {
                continue;
            }
            this.channels[channelId] = eventId;
            return channelId + this.offset;
        }

        throw new IllegalStateException("Stack is full!");
    }

    public Integer peek(int eventId) {
        for (int channelId = 0; channelId < this.channels.length; channelId++) {
            Integer current = this.channels[channelId];
            if (current == null || current != eventId) {
                continue;
            }
            return channelId + this.offset;
        }
        return null;
    }

    public Integer pull(int eventId) {
        for (int channelId = 0; channelId < this.channels.length; channelId++) {
            Integer current = this.channels[channelId];
            if (current == null || current != eventId) {
                continue;
            }
            this.channels[channelId] = null;
            return channelId + this.offset;
        }
        return null;
    }
}
