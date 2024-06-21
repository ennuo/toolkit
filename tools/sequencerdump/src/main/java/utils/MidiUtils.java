package utils;

import javax.sound.midi.*;

public final class MidiUtils {

    public static final int MAX_CC_VALUE = 127;
    public static final int MIN_CC_VALUE = 0;

    public static final int MAX_PITCH_VALUE = 16383;
    public static final int ZERO_PITCH_VALUE = 8192;
    public static final int MIN_PITCH_VALUE = 0;

    public static final int PITCH_BEND_RANGE = 48;
    public static final double SEMITONE_PITCH_VALUE = (double) ZERO_PITCH_VALUE / PITCH_BEND_RANGE;

    private MidiUtils() {
    }

    public static void controlChange(Track track, long tick, int channel, int controlChange, int value) throws InvalidMidiDataException {
        value = Math.max(0, Math.min(127, value));
        ShortMessage message = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, controlChange, value);
        MidiEvent event = new MidiEvent(message, tick);
        track.add(event);
    }

    public static void channelPressure(Track track, long tick, int channel, int value) throws InvalidMidiDataException {
        value = Math.max(0, Math.min(127, value));
        ShortMessage message = new ShortMessage(ShortMessage.CHANNEL_PRESSURE, channel, value, 0);
        MidiEvent event = new MidiEvent(message, tick);
        track.add(event);
    }

    public static void timbre(Track track, long tick, int channel, int value) throws InvalidMidiDataException {
        controlChange(track, tick, channel, 74, value);
    }

    public static void pitchBend(Track track, long tick, int channel, int value) throws InvalidMidiDataException {
        value = Math.max(MIN_PITCH_VALUE, Math.min(MAX_PITCH_VALUE, value));
        int lsb = value & 0x7F; // Least significant 7 bits
        int msb = (value >> 7) & 0x7F; // Most significant 7 bits
        ShortMessage message = new ShortMessage(ShortMessage.PITCH_BEND, channel, lsb, msb);
        MidiEvent event = new MidiEvent(message, tick);
        track.add(event);
    }

    public static void metaText(Track track, long tick, String text) throws InvalidMidiDataException {
        byte[] textBytes = text.getBytes();
        MetaMessage metaMessage = new MetaMessage(0x01, textBytes, textBytes.length);
        MidiEvent event = new MidiEvent(metaMessage, tick);
        track.add(event);
    }
}
