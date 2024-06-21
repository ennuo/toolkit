import instrument.Instrument;

import java.util.LinkedList;
import java.util.List;

public class Sequence {

    public static final int MIDI_TICKS_PER_QUARTER_NOTE = 96; // 1/4 note = 96 ticks
    public static final int MIDI_TICKS_PER_16TH_NOTE = MIDI_TICKS_PER_QUARTER_NOTE / 4; // 1/16 note = 24 ticks
    public static final int MIDI_TICKS_PER_12TH_NOTE = MIDI_TICKS_PER_QUARTER_NOTE / 3; // 1/12 note = 32 ticks
    public static final int MIDI_TICKS_AUTOMATION_RESOLUTION = 1; // 1/48 note = 8 ticks

    private String name;
    private float tempo;
    private float swing;
    private List<Track> tracks = new LinkedList<>();
    private int eventCounter = 0;

    public Sequence(String name, int tempo, int swing) {
        this.name = name;
        this.tempo = tempo;
        this.swing = swing;
    }

    public Sequence() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getTempo() {
        return tempo;
    }

    public void setTempo(float tempo) {
        this.tempo = tempo;
    }

    public float getSwing() {
        return swing;
    }

    public void setSwing(float swing) {
        this.swing = swing;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public Track addTrack(float yPosition, Instrument instrument) {
        Track track = new Track(tracks.size(), yPosition, instrument);
        tracks.add(track);
        return track;
    }

    public Track getTrack(float y, Instrument instrument) {
        for (Track track : tracks) {
            if (track.yPosition == y && instrument.equals(track.instrument)) {
                return track;
            }
        }
        return null;
    }

    public class Track {
        private final int id;
        private float yPosition;
        private List<Note> notes = new LinkedList<>();
        private Instrument instrument;

        protected Track(int id, float yPosition, Instrument instrument) {
            this.id = id;
            this.yPosition = yPosition;
            this.instrument = instrument;
        }

        public Sequence getParent() {
            return Sequence.this;
        }

        public int getId() {
            return id;
        }

        public float getyPosition() {
            return yPosition;
        }

        public void setyPosition(float yPosition) {
            this.yPosition = yPosition;
        }

        public List<Note> getNotes() {
            return notes;
        }

        public void setNotes(List<Note> notes) {
            this.notes = notes;
        }

        public Instrument getInstrument() {
            return instrument;
        }

        public void setInstrument(Instrument instrument) {
            this.instrument = instrument;
        }

        public Note addNote() {
            Note note = new Note(notes.size());
            this.notes.add(note);
            return note;
        }

        public class Note {
            private final int id;
            private final LinkedList<Point> points;

            protected Note(int id) {
                this.id = id;
                points = new LinkedList<>();
            }

            public Track getParent() {
                return Track.this;
            }

            public int getId() {
                return id;
            }

            public LinkedList<Point> getPoints() {
                return points;
            }

            public Point getPoint(int index) {
                return points.get(index);
            }

            public Point getStart() {
                return points.getFirst();
            }

            public Point getEnd() {
                return points.getLast();
            }

            public int getIndex(Point point) {
                return points.indexOf(point);
            }

            public Point pushPoint(int step, int note, int volume, int timbre, boolean triplet) {
                Point point = new Point(eventCounter++, step, note, volume, timbre, triplet);
                points.add(point);
                return point;
            }

            public boolean hasPitchBend() {
                Point previous = null;
                for (Point point : points) {
                    if (previous != null) {
                        if (previous.note != point.note) {
                            return true;
                        }
                    }
                    previous = point;
                }
                return false;
            }

            public boolean hasVolumeAutomation() {
                Point previous = null;
                for (Point point : points) {
                    if (previous != null) {
                        if (previous.volume != point.volume) {
                            return true;
                        }
                    }
                    previous = point;
                }
                return false;
            }

            public boolean hasTimbreAutomation() {
                Point previous = null;
                for (Point point : points) {
                    if (previous != null) {
                        if (previous.timbre != point.timbre) {
                            return true;
                        }
                    }
                    previous = point;
                }
                return false;
            }

            public boolean hasAutomation() {
                return hasPitchBend() || hasVolumeAutomation() || hasTimbreAutomation();
            }

            public class Point {
                private final int eventId;
                private final int step;
                private final int note;
                private final int volume;
                private final int timbre;
                private final boolean triplet;

                protected Point(int eventId, int step, int note, int volume, int timbre, boolean triplet) {
                    this.eventId = eventId;
                    this.step = step;
                    this.note = note;
                    this.volume = volume;
                    this.timbre = timbre;
                    this.triplet = triplet;
                }

                public Note getParent() {
                    return Note.this;
                }

                public Point getPrevious() {
                    if (isStart()) {
                        return null;
                    }
                    return getParent().getPoint(getNoteIndex() - 1);
                }

                public boolean isStart() {
                    return getParent().getStart().equals(this);
                }

                public boolean isEnd() {
                    return getParent().getEnd().equals(this);
                }

                public int getEventId() {
                    return eventId;
                }

                public int getNoteIndex() {
                    return getParent().getIndex(this);
                }

                public int getStep() {
                    return step;
                }

                public int getNote() {
                    return note;
                }

                public int getVolume() {
                    return volume;
                }

                public int getTimbre() {
                    return timbre;
                }

                public boolean isTriplet() {
                    return triplet;
                }

                public long toMidiTick() {
                    long baseTick;
                    if (isTriplet()) {
                        // Calculate the base position for triplet notes
                        int tripletGroup = step / 4; // Each quarter note contains 4 16th notes
                        int tripletPosition = step % 4; // Position within the 16th note group
                        baseTick = (long) tripletGroup * MIDI_TICKS_PER_QUARTER_NOTE + tripletPosition * MIDI_TICKS_PER_12TH_NOTE;
                    } else {
                        baseTick = (long) step * MIDI_TICKS_PER_16TH_NOTE;
                    }
                    return baseTick;
                }
            }
        }
    }
}
