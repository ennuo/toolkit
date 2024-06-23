import cwlib.enums.Part;
import cwlib.structs.instrument.Note;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.CompactComponent;
import cwlib.structs.things.parts.PInstrument;
import cwlib.structs.things.parts.PMicrochip;
import cwlib.structs.things.parts.PSequencer;
import instrument.Instrument;
import utils.MathUtils;
import utils.MidiChannelStack;
import utils.MidiUtils;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MidiDumper {

    public static final float GRID_UNIT_SIZE = 52.5F; // Grid size is min unit size / 2
    public static final int GRID_UNIT_STEPS = 16; // Steps per grid unit is min unit steps / 2

    private final Thing thing;
    private final PMicrochip microchip;
    private final PSequencer sequencer;

    private final Sequence result;

    public MidiDumper(Thing thing) {
        this.thing = thing;
        this.microchip = Objects.requireNonNull(thing.getPart(Part.MICROCHIP), "Thing isn't a microchip!");
        this.sequencer = Objects.requireNonNull(thing.getPart(Part.SEQUENCER), "Thing isn't a sequencer!");
        if (!this.sequencer.musicSequencer) {
            throw new IllegalArgumentException("Thing isn't a music sequencer!");
        }
        this.result = new Sequence();
    }

    public Sequence getSequence() {
        return result;
    }

    public void loadFromThing() {
        // Extract metadata
        String name = microchip.name.trim();
        if (name.isEmpty()) {
            name = "Unnamed_" + thing.UID;
        }
        result.setName(name);
        result.setTempo(sequencer.tempo);
        result.setSwing(sequencer.swing);

        System.out.println(MessageFormat.format("Found sequencer: {0}", result.getName()));

        // Snap to grid
        // LBP positioning isn't very precise, round to fraction of 2 to have a bit of tolerance
        for (CompactComponent component : microchip.components) {
            component.x = MathUtils.roundToFraction(component.x, 2);
            component.y = MathUtils.roundToFraction(component.y, 2);
        }

        // Sort components, makes debugging easier
        Arrays.sort(microchip.components, (a, b) -> {
            if (a.x == b.x) {
                return Float.compare(b.y, a.y);
            }
            return Float.compare(a.x, b.x);
        });

        // Process components, extract note data and group them by track
        for (CompactComponent component : microchip.components) {
            PInstrument instrument = component.thing.getPart(Part.INSTRUMENT);
            if (instrument == null) {
                continue; // Skip non-instruments
            }

            Instrument instrumentType = Instrument.fromGUID(instrument.instrument.getGUID());

            // Since lbp doesn't group instruments into channels, treat instruments of the same type on same row as a channel
            Sequence.Track track = result.getTrack(component.y, instrumentType);
            if (track == null) {
                track = result.addTrack(component.y, instrumentType);
            }

            // Calculate current instrument grid index and tick
            int componentGridIndex = (int) Math.floor(component.x / GRID_UNIT_SIZE);
            int componentInitialStep = componentGridIndex * GRID_UNIT_STEPS;

            Sequence.Track.Note currentNote = null;
            for (Note note : instrument.notes) {
                int noteStep = componentInitialStep + note.x;
                if (currentNote == null) {
                    currentNote = track.addNote();
                }
                currentNote.pushPoint(noteStep, note.y, note.volume, note.timbre, note.triplet);
                if (note.end) {
                    // Push end
                    currentNote.pushPoint(noteStep + 1, note.y, note.volume, note.timbre, note.triplet);
                    // Fix point order, sometimes this isn't correct...
                    currentNote.getPoints().sort(Comparator.comparingInt(Sequence.Track.Note.Point::getStep));
                    currentNote = null;
                }
            }
            if (currentNote != null) {
                throw new RuntimeException("Should never happen!");
            }
        }
    }

    public void writeMidiTracks(File outputFolder) throws InvalidMidiDataException, IOException {
        outputFolder.mkdirs();

        for (Sequence.Track track : result.getTracks()) {
            javax.sound.midi.Sequence midiOut = new javax.sound.midi.Sequence(
                    javax.sound.midi.Sequence.PPQ,
                    Sequence.MIDI_TICKS_PER_QUARTER_NOTE
            );
            javax.sound.midi.Track defaultTrack = midiOut.createTrack();
            MidiUtils.channelPressure(defaultTrack, 0, 0, MidiUtils.MAX_CC_VALUE); // Default track volume
            MidiUtils.timbre(defaultTrack, 0, 0, MidiUtils.MIN_CC_VALUE); // Default mod wheel
            MidiUtils.pitchBend(defaultTrack, 0, 0, MidiUtils.ZERO_PITCH_VALUE); // Default mod wheel
            // TODO: set bpm

            List<Sequence.Track.Note.Point> points = track.getNotes().stream()
                    .flatMap(note -> note.getPoints().stream())
                    .sorted(Comparator.comparingLong(Sequence.Track.Note.Point::toMidiTick))
                    .toList();

            MidiChannelStack automationChannels = new MidiChannelStack(15, 1);

            for (Sequence.Track.Note.Point point : points) {
                Sequence.Track.Note note = point.getParent();

                Integer channel;
                if (note.hasAutomation()) {
                    if (automationChannels.isFull()) {
                        // Max polyphony for automation notes reached!
                        System.err.println("Max polyphony reached! Note will be skipped!");
                        continue;
                    } else if (point.isStart()) {
                        channel = automationChannels.push(note.getId()); // Add note to channel stack and get channel
                    } else {
                        channel = automationChannels.peek(note.getId()); // Get note channel
                        if (channel == null) {
                            // Note start was skipped due tue max polyphony, skip updates/end
                            continue;
                        }
                    }
                } else {
                    channel = 0;
                }

                if (point.isStart()) {
                    // Note on
                    if (!note.hasAutomation()) {
                        MidiUtils.channelPressure(defaultTrack, point.toMidiTick(), channel, MidiUtils.MAX_CC_VALUE);
                    }
                    ShortMessage message = new ShortMessage(
                            ShortMessage.NOTE_ON,
                            channel,
                            point.getNote(),
                            Math.max(1, note.hasAutomation() ? 127 : point.getVolume())
                    );
                    defaultTrack.add(new MidiEvent(message, point.toMidiTick()));

                    //MidiUtils.metaText(defaultTrack, point.toMidiTick(), "ON(t:" + point.toMidiTick() + "ch:" + channel + ";nt:" + point.getNote() + ";ni:" + point.getParent().getId() + ")");
                } else {
                    // Update
                    Sequence.Track.Note.Point start = note.getStart();
                    Sequence.Track.Note.Point previous = point.getPrevious();

                    long automationSteps = (point.toMidiTick() - previous.toMidiTick()) / Sequence.MIDI_TICKS_AUTOMATION_RESOLUTION;

                    int deltaVolume = point.getVolume() - previous.getVolume();
                    if (deltaVolume != 0) {
                        double volumeIncrement = (double) deltaVolume / automationSteps;
                        double currentVolume = previous.getVolume();
                        for (
                                long currentTick = previous.toMidiTick();
                                currentTick < point.toMidiTick();
                                currentTick += Sequence.MIDI_TICKS_AUTOMATION_RESOLUTION
                        ) {
                            currentVolume += volumeIncrement;

                            var actualValue = (int) Math.round(currentVolume);
                            MidiUtils.channelPressure(defaultTrack, currentTick, channel, actualValue);
                        }
                    }

                    int deltaTimbre = point.getTimbre() - previous.getTimbre();
                    if (deltaTimbre != 0) {
                        MidiUtils.timbre(defaultTrack, point.toMidiTick(), channel, point.getTimbre());

                        double timbreIncrement = (double) deltaTimbre / automationSteps;
                        double currentTimbre = previous.getVolume();
                        for (
                                long currentTick = previous.toMidiTick();
                                currentTick < point.toMidiTick();
                                currentTick += Sequence.MIDI_TICKS_AUTOMATION_RESOLUTION
                        ) {
                            currentTimbre += timbreIncrement;

                            var actualValue = (int) Math.round(currentTimbre);
                            MidiUtils.timbre(defaultTrack, currentTick, channel, actualValue);
                        }
                    }

                    double deltaNote = point.getNote() - previous.getNote();
                    if (deltaNote != 0) {
                        double previousBend = previous.getNote() - start.getNote();
                        double semitonesIncrement = deltaNote / automationSteps;

                        double currentSemitones = previousBend;
                        for (
                                long currentTick = previous.toMidiTick();
                                currentTick < point.toMidiTick();
                                currentTick += Sequence.MIDI_TICKS_AUTOMATION_RESOLUTION
                        ) {
                            currentSemitones += semitonesIncrement;

                            int bendValue = MidiUtils.ZERO_PITCH_VALUE + (int) Math.round(
                                    currentSemitones * MidiUtils.SEMITONE_PITCH_VALUE
                            );
                            if (bendValue < 0 || bendValue > MidiUtils.MAX_PITCH_VALUE) {
                                System.err.println("Note exceeds the maximum pitch bend range! (" + bendValue + ") Will be constrained.");
                            }
                            MidiUtils.pitchBend(defaultTrack, currentTick, channel, bendValue);
                        }
                    }
                }

                if (point.isEnd()) {
                    // Note off
                    ShortMessage message = new ShortMessage(
                            ShortMessage.NOTE_OFF,
                            channel,
                            note.getStart().getNote(),
                            0
                    );
                    defaultTrack.add(new MidiEvent(message, point.toMidiTick()));
                    automationChannels.pull(note.getId()); // Remove note from channel stack

                    //MidiUtils.metaText(defaultTrack, point.toMidiTick(), "OFF(t:" + point.toMidiTick() + "ch:" + channel + ";nt:" + point.getNote() + ";ni:" + point.getParent().getId() + ")");

                    if (note.hasPitchBend()) {
                        MidiUtils.pitchBend(defaultTrack, point.toMidiTick(), channel, MidiUtils.ZERO_PITCH_VALUE); // Reset
                    }
                }
            }

            File outputFile = new File(outputFolder, "%d %s.mid".formatted(track.getId(), track.getInstrument().getName()));
            MidiSystem.write(midiOut, 1, outputFile);
        }
    }
}
