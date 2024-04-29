package cwlib.io;

import cwlib.enums.CompressionFlags;
import cwlib.io.FMOD.FMODEventFile.EventEnvelope.EffectType;
import cwlib.io.FMOD.FMODSampleBank.Sample;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.singleton.ResourceSystem;
import cwlib.util.FileIO;

import java.io.File;
import java.util.*;

public class FMOD
{

    public static String[] CONSOLES = { "PC", "XBOX", "XBOX360", "GC", "PS2", "PSP", "PS3",
        "WII" };
    public static String[] TEMPLATE_PROPS = {
        "LAYERS",
        "KEEP_EFFECTS_PARAMS",
        "VOLUME",
        "PITCH",
        "PITCH_RANDOMIZATION",
        "VOLUME_RANDOMIZATION",
        "PRIORITY",
        "MAX_PLAYBACKS",
        "MAX_PLAYBACKS_BEHAVIOR",
        "STEAL_PRIORITY",
        "MODE",
        "IGNORE_GEOMETRY",
        "X_3D_ROLLOFF",
        "X_3D_MIN_DISTANCE",
        "X_3D_MAX_DISTANCE",
        "X_3D_POSITION",
        "X_3D_POSITION_RANDOMIZATION",
        "X_3D_CONE_INSIDE_ANGLE",
        "X_3D_CONE_OUTSIDE_ANGLE",
        "X_3D_CONE_OUTSIDE_VOLUME",
        "X_3D_DOPPLER_FACTOR",
        "REVERB_WET_LEVEL",
        "REVERB_DRY_LEVEL",
        "X_3D_SPEAKER_SPREAD",
        "X_3D_PAN_LEVEL",
        "X_2D_SPEAKER_L",
        "X_2D_SPEAKER_C",
        "X_2D_SPEAKER_R",
        "X_2D_SPEAKER_LS",
        "X_2D_SPEAKER_RS",
        "X_2D_SPEAKER_LR",
        "X_2D_SPEAKER_RR",
        "X_SPEAKER_LFE",
        "ONESHOT",
        "FADEIN_TIME",
        "FADEOUT_TIME",
        "NOTES",
        "USER_PROPERTIES",
    };

    public static class FMODEventFile
    {

        public enum EventMode
        {_2D, _3D}

        public enum Rollof3DType
        {LOGARITHMIC, LINEAR, CUSTOM, UNSPECIFIED}

        public enum Position3DType
        {WORLD_RELATIVE, HEAD_RELATIVE}

        public enum StreamingType
        {
            DECOMPRESS_INTO_MEMORY,
            LOAD_INTO_MEMORY,
            STREAM_FROM_DISK
        }

        public enum PlayMode
        {
            SEQUENTIAL, RANDOM, RANDOM_NO_REPEAT, SEQUENTIAL_NO_REPEAT, SHUFFLE,
            PROGRAMMER_SELECTED
        }

        public enum PropertyType
        {INT, FLOAT, STRING}

        public static class Property
        {
            public String name;
            public PropertyType type;
            public Object value;

            public String toXML()
            {
                String xml = "<userproperty>\n";
                xml += tag("name", this.name);
                xml += guid();
                xml += tag("description", "");
                if (this.type == PropertyType.INT)
                    xml += tag("data_int", this.value);
                else if (this.type == PropertyType.FLOAT)
                    xml += tag("data_float", this.value);
                else if (this.type == PropertyType.STRING)
                    xml += tag("data_string", this.value);
                return xml + "</userproperty>\n";
            }
        }

        public static class SoundBank
        {
            public int maxStreams;
            public StreamingType streamingType;
            public String name;

            public String getHeaderXML()
            {
                String xml = "<soundbank>\n";

                xml += tag("name", this.name);
                xml += guid();
                xml += tag("load_into_rsx", "0");
                xml += tag("disable_seeking", "0");
                xml += tag("enable_syncpoints", "1");
                xml += tag("hasbuiltwithsyncpoints", "0");

                String type = "DecompressedSample";
                if (this.streamingType == StreamingType.STREAM_FROM_DISK)
                    type = "Stream";
                else if (this.streamingType == StreamingType.LOAD_INTO_MEMORY)
                    type = "Sample";

                xml += tag("_PC_banktype", type);
                xml += tag("_XBOX_banktype", "DecompressedSample");
                xml += tag("_XBOX360_banktype", "DecompressedSample");
                xml += tag("_GC_banktype", "DecompressedSample");
                xml += tag("_PS2_banktype", "DecompressedSample");
                xml += tag("_PSP_banktype", "DecompressedSample");
                xml += tag("_PS3_banktype", type);
                xml += tag("_WII_banktype", "DecompressedSample");
                xml += tag("notes", "");
                xml += tag("rebuild", 0);

                return xml;
            }

            public String getFooterXML(String format)
            {
                String xml = "";
                for (String console : CONSOLES)
                {
                    xml += tag("_" + console + "_format", format);
                    xml += tag("_" + console + "_quality", 50);
                    xml += tag("_" + console + "_optimisesamplerate", 0);
                    xml += tag("_" + console + "_forcesoftware",
                        (console.equals("PC") || console.equals("XBOX360") || console.equals(
                            "PS3")) ? "1" : "0");
                    xml += tag("_" + console + "_maxstreams", this.maxStreams);
                }
                return xml + "</soundbank>\n";
            }
        }

        private static String tag(String name, Object value)
        {
            return String.format("<%s>%s</%s>\n", name, value.toString(), name);
        }

        private static String guid()
        {
            return "<guid>{" + UUID.randomUUID() + "}</guid>\n";
        }

        private static float fieldRatioToDecibel(float fieldRatio)
        {
            if (fieldRatio <= 0.001f)
                return -60.0f;
            return (float) (20.0f * Math.log10(fieldRatio));
        }

        public static class EventCategory
        {
            public String name;
            public float volume;
            public float pitch;
            public int maxPlaybacks;
            public int maxPlaybacksBehavior;
            public ArrayList<EventCategory> subCategories = new ArrayList<>();

            public String toXML()
            {
                String xml = "<eventcategory>\n";
                xml += tag("name", this.name);
                xml += guid();
                xml += tag("volume_db", fieldRatioToDecibel(this.volume));
                xml += tag("pitch", this.pitch * 4.0f);
                xml += tag("maxplaybacks", this.maxPlaybacks);

                String behavior;
                switch (this.maxPlaybacksBehavior)
                {
                    case 1:
                        behavior = "Steal_newest";
                        break;
                    case 2:
                        behavior = "Steal_quietest";
                        break;
                    case 3:
                        behavior = "Just_fail";
                        break;
                    case 4:
                        behavior = "Just_fail_if_quietest";
                        break;
                    default:
                        behavior = "Steal_oldest";
                        break;
                }
                xml += tag("maxplaybacks_behavior", behavior);
                xml += tag("notes", "");
                xml += tag("open", 0);
                for (EventCategory category : subCategories)
                    xml += category.toXML();
                xml += "</eventcategory>\n";
                return xml;
            }
        }

        public static class EventGroup
        {
            public String name;
            public ArrayList<Property> userProperties = new ArrayList<>();
            public ArrayList<EventGroup> subGroups = new ArrayList<>();
            public ArrayList<Event> events = new ArrayList<>();

            public String toXML(ArrayList<SoundDef> soundDefs)
            {
                String xml = "<eventgroup>\n";
                xml += tag("name", this.name);
                xml += guid();
                xml += tag("eventgroup_nextid", this.subGroups.size());
                xml += tag("event_nextid", this.events.size());
                xml += tag("open", "0");
                xml += tag("notes", "");

                for (Property property : userProperties)
                    xml += property.toXML();
                for (EventGroup subGroup : this.subGroups)
                    xml += subGroup.toXML(soundDefs);
                for (Event event : events)
                    xml += event.toXML(soundDefs);

                return xml + "</eventgroup>\n";
            }
        }

        public static class EventSound
        {
            public short soundDefIndex;
            public float start;
            public float length;
            public int startMode;
            public byte loopMode;
            public byte autoPitchParam;
            public int loopCount;
            public int autoPitchEnabled;
            public float autoPitchReference;
            public float autopitchAtMin;
            public float fineTune;
            public float volume;
            public float fadeInLength;
            public float fadeOutLength;
            public int fadeInType;
            public int fadeOutType;

            public String toXML(SoundDef soundDef)
            {
                String xml = "<sound>\n";

                xml += tag("name", soundDef.name);
                xml += tag("x", this.start);
                xml += tag("width", this.length);
                xml += tag("startmode", this.startMode);
                xml += tag("loopmode", this.loopMode);
                xml += tag("loopcount2", this.loopCount);
                xml += tag("autopitchenabled", this.autoPitchEnabled);
                xml += tag("autopitchparameter", this.autoPitchParam == 0 ? 0 : 1);
                xml += tag("autopitchreference", this.autoPitchReference);
                xml += tag("autopitchatzero", this.autopitchAtMin);
                xml += tag("finetune", this.fineTune);
                xml += tag("volume", this.volume);
                xml += tag("fadeintype", this.fadeInType);
                xml += tag("fadeouttype", this.fadeOutType);
                return xml + "</sound>\n";
            }
        }

        public static class EventEnvelopePoint
        {
            public enum CurveShape
            {
                FLAT_ENDED, // 1
                LINEAR, // 2
                LOGARITHMIC, // 4
                FLAT_MIDDLE // 8
            }

            public float x;
            public float y;
            public int curveShape;

            public String toXML(int index)
            {
                return tag("point",
                    this.x + "," + this.y + "," + (index == 0 ? "1" : "0") + "," + this.curveShape);
            }
        }

        public static class EventEnvelope
        {
            public enum EffectType
            {
                DSP_EFFECT("", 0x0004),
                VOLUME("Volume", 0x000c),
                PITCH("Pitch", 0x0014),
                PAN("Pan", 0x0024),
                SURROUND_PAN("Surround pan", 0x0048),
                THREE_DIM_PAN_LEVEL("3D Pan Level", 0x0404),
                THREE_DIM_SPEAKER_SPREAD("3D Speaker spread", 0x0104),
                OCCLUSION("Occlusion", 0x2004),
                REVERB_LEVEL("Reverb Level", 0x0204),
                REVERB_BALANCE("Reverb Balance", 0x0804),
                TIME_OFFSET("Time offset", 0x0044),
                SPAWN_INTENSITY("Spawn Intensity", 0x1004);

                private final int flags;
                private final String name;

                EffectType(String name, int flags)
                {
                    this.flags = flags;
                    this.name = name;
                }

                public int getFlags()
                {
                    return this.flags;
                }

                public String getName()
                {
                    return this.name;
                }

                public static EffectType fromFlags(int flags)
                {
                    for (EffectType type : EffectType.values())
                    {
                        if (type.flags == flags)
                            return type;
                    }
                    return null;
                }

            }

            public int parentIndex;
            public String name;
            public int effectParameterIndex;
            public EffectType effect;
            public boolean isMuted;
            public int flags;
            public ArrayList<EventEnvelopePoint> points = new ArrayList<EventEnvelopePoint>();
            public int controlParameterIndex;
            public int mappingMethod;

            public String toXML(String name, ArrayList<EventEnvelope> envelopes,
                                ArrayList<EventParameter> parameters)
            {
                EventEnvelope parent = null;
                if (this.parentIndex != -1)
                    parent = envelopes.get(this.parentIndex);

                EventEnvelope envelope = parent != null ? parent : this;
                String dspName = this.effect != EffectType.DSP_EFFECT ?
                    this.effect.getName() :
                    envelope.name;

                String xml = "<envelope>\n";

                xml += tag("name", name);
                xml += tag("dsp_name", dspName);
                xml += tag("dsp_paramindex", this.effectParameterIndex);
                xml += tag("colour", "#7f0000");
                if (parent != null)
                    xml += tag("parentname", "env00" + this.parentIndex);
                for (int i = 0; i < this.points.size(); i++)
                    xml += this.points.get(i).toXML(i);
                xml += tag("parametername",
                    parameters.get(this.controlParameterIndex).name);
                xml += tag("controlparameter",
                    parameters.get(this.controlParameterIndex).name);
                for (String console : CONSOLES)
                    xml += tag("_" + console + "_enable", 1);
                xml += tag("mute", this.isMuted ? 1 : 0);
                xml += tag("visible", "1");
                xml += tag("hidden", "0");
                xml += tag("fromtemplate", "No");
                xml += tag("mappingmethod", this.mappingMethod);
                xml += tag("flags", this.flags);
                xml += tag("exflags", "0");

                return xml + "</envelope>\n";
            }
        }

        public static class Layer
        {
            public short priority;
            public short controlParameter;
            public ArrayList<EventSound> soundInstances = new ArrayList<>();
            public ArrayList<EventEnvelope> envelopes = new ArrayList<>();

            public String toXML(String layerName, ArrayList<EventParameter> parameters,
                                ArrayList<SoundDef> soundDefs)
            {
                String xml = "<layer>\n";

                xml += tag("name", layerName);
                xml += tag("height", 100);
                xml += tag("envelope_nextid", this.envelopes.size());
                xml += tag("mute", "0");
                xml += tag("solo", "0");
                xml += tag("soundlock", "0");
                xml += tag("envlock", "0");
                xml += tag("priority", this.priority);

                if (this.controlParameter != -1)
                    xml += tag("controlparameter",
                        parameters.get(this.controlParameter).name);
                for (EventSound instance : soundInstances)
                    xml += instance.toXML(soundDefs.get(instance.soundDefIndex));
                for (int i = 0; i < this.envelopes.size(); ++i)
                    xml += this.envelopes.get(i).toXML("env00" + i, this.envelopes,
                        parameters);
                for (String console : CONSOLES)
                    xml += tag("_" + console + "_enable", 1);

                return xml + "</layer>\n";
            }
        }

        public static class EventParameter
        {
            public String name;
            public float velocity;
            public float min, max;
            public boolean isPrimary;
            public int loopBehavior;
            public float seekSpeed;
            public int controlledEnvelopeCount;
            public ArrayList<Float> sustainPoints = new ArrayList<Float>();

            public String toXML()
            {
                String xml = "<parameter>\n";
                xml += tag("name", this.name);
                xml += guid();
                xml += tag("primary", isPrimary ? 1 : 0);

                String loopMode;
                if ((loopBehavior & 0x2) != 0) loopMode = "0";
                else if ((loopBehavior & 0x4) != 0) loopMode = "1";
                else loopMode = "2";

                xml += tag("loopmode", loopMode);
                xml += tag("rangeunits", "");
                xml += tag("rangemin", this.min);
                xml += tag("rangemax", this.max);
                xml += tag("rangespacing", (this.max - this.min) / 10.0f);
                xml += tag("keyoffonsilence", "0");
                xml += tag("velocity", this.velocity);
                xml += tag("seekspeed", this.seekSpeed);

                for (float value : this.sustainPoints)
                {
                    xml += "<sustainpoint>\n";
                    xml += tag("value", value);
                    xml += tag("enabled", "1");
                    xml += "</sustainpoint>\n";
                }

                return xml + "</parameter>\n";
            }
        }

        public static class Event
        {
            public String name;

            public float volume;
            public float pitch;

            public float volumeRandomization;
            public float pitchRandomization;

            public int priority;
            public int mode;

            public boolean ignoreGeometry;

            public int maxPlaybacks;
            public int maxPlaybacksBehavior;

            public int rollof3D;
            public int position3D;

            public int positionRandomization3D;

            public float coneInsideAngle3D;
            public float coneOutsideAngle3D;
            public float coneOutsideVolume3D;

            public float dopplerFactor3D;
            public float speakerSpread3D;
            public float panLevel3D;

            public float minDistance3D;
            public float maxDistance3D;

            public boolean oneshot;
            public int pitchRandUnits;

            public float speaker2DL;
            public float speaker2DR;
            public float speaker2DC;

            public float speakerLFE;

            public float speaker2DLR;
            public float speaker2DRR;
            public float speaker2DLS;
            public float speaker2DRS;

            public float reverbDryLevel;
            public float reverbWetLevel;

            public int fadeInTime;
            public int fadeOutTime;

            public float spawnIntensity;
            public float spawnIntensityRandomization;

            public ArrayList<Layer> layers = new ArrayList<>();
            public ArrayList<EventParameter> parameters = new ArrayList<>();
            public ArrayList<Property> userProperties = new ArrayList<>();
            public ArrayList<String> categories = new ArrayList<String>();

            public String toXML(ArrayList<SoundDef> soundDefs)
            {
                String xml = "<event>\n";
                xml += tag("name", this.name);
                xml += guid();
                xml += tag("parameter_nextid", this.parameters.size());
                xml += tag("layer_nextid", this.layers.size());

                for (int i = 0; i < this.layers.size(); ++i)
                    xml += this.layers.get(i).toXML("layer0" + i, this.parameters,
                        soundDefs);
                for (EventParameter parameter : this.parameters)
                    xml += parameter.toXML();
                xml += tag("car_rpm", 0);
                xml += tag("car_rpmsmooth", 0.075f);
                xml += tag("car_loadsmooth", 0.05f);
                xml += tag("car_loadscale", 6);
                xml += tag("car_dialog", 0);
                xml += tag("volume_db", fieldRatioToDecibel(this.volume));
                xml += tag("pitch", this.pitch * 4.0f);

                String units = "Octaves";
                if (this.pitchRandUnits == 0x40)
                    units = "Semitones";
                else if (this.pitchRandUnits == 0x80)
                    units = "Tones";

                xml += tag("pitch_units", units);
                xml += tag("pitch_randomization", this.pitchRandomization * 4.0f);
                xml += tag("pitch_randomization_units", units);
                xml += tag("volume_randomization",
                    fieldRatioToDecibel(1.0f - this.volumeRandomization));
                xml += tag("priority", this.priority);
                xml += tag("maxplaybacks", this.maxPlaybacks);
                String behavior;
                switch (this.maxPlaybacksBehavior)
                {
                    case 1:
                        behavior = "Steal_newest";
                        break;
                    case 2:
                        behavior = "Steal_quietest";
                        break;
                    case 3:
                        behavior = "Just_fail";
                        break;
                    case 4:
                        behavior = "Just_fail_if_quietest";
                        break;
                    default:
                        behavior = "Steal_oldest";
                        break;
                }
                xml += tag("maxplaybacks_behavior", behavior);
                xml += tag("stealpriority", "10000");
                xml += tag("mode", this.mode == 0x08 ? "x_2d" : "x_3d");
                xml += tag("ignoregeometry", this.ignoreGeometry ? "Yes" : "No");

                String rolloff = "Logarithmic";
                if (this.rollof3D == 0x0020)
                    rolloff = "Linear";
                else if (this.rollof3D == 0x0400)
                    rolloff = "Custom";

                xml += tag("rolloff", rolloff);
                xml += tag("mindistance", this.minDistance3D);
                xml += tag("maxdistance", this.maxDistance3D);
                xml += tag("headrelative", position3D == 0x04 ? "Head_relative" :
                    "World_relative");
                xml += tag("oneshot", this.oneshot ? "Yes" : "No");

                xml += tag("istemplate", "No");
                xml += tag("usetemplate", "");
                xml += tag("notes", "");

                for (Property property : this.userProperties)
                    xml += property.toXML();

                xml += tag("category", this.categories.get(0));
                xml += tag("position_randomization", this.positionRandomization3D);
                xml += tag("speaker_l", this.speaker2DL);
                xml += tag("speaker_c", this.speaker2DC);
                xml += tag("speaker_r", this.speaker2DR);
                xml += tag("speaker_ls", this.speaker2DLS);
                xml += tag("speaker_rs", this.speaker2DRS);
                xml += tag("speaker_lb", this.speaker2DLR);
                xml += tag("speaker_rb", this.speaker2DRR);
                xml += tag("speaker_lfe", this.speakerLFE);
                xml += tag("speaker_config", "0");
                xml += tag("speaker_pan_r", "1");
                xml += tag("speaker_pan_theta", "0");
                xml += tag("cone_inside_angle", this.coneInsideAngle3D);
                xml += tag("cone_outside_angle", this.coneOutsideAngle3D);
                xml += tag("cone_outside_volumedb",
                    fieldRatioToDecibel(this.coneOutsideVolume3D));
                xml += tag("doppler_scale", this.dopplerFactor3D);
                xml += tag("reverbdrylevel_db", this.reverbDryLevel);
                xml += tag("reverblevel_db", this.reverbWetLevel);
                xml += tag("speaker_spread", this.speakerSpread3D);
                xml += tag("panlevel3d", this.panLevel3D);
                xml += tag("fadein_time", this.fadeInTime);
                xml += tag("fadeout_time", this.fadeOutTime);
                xml += tag("spawn_intensity", this.spawnIntensity);
                xml += tag("spawn_intensity_randomization",
                    this.spawnIntensityRandomization);

                for (String propName : TEMPLATE_PROPS)
                    xml += tag("TEMPLATE_PROP_" + propName, "1");

                return xml + "</event>\n";
            }
        }

        public static class SoundDefWaveform
        {
            public boolean isNull;
            public boolean isOscillator;
            public boolean isProgrammerSound;

            public int weight;

            public float frequency;
            public int waveType;


            public String name;
            public String bank;
            public int indexInBank;
            public int playtime;

            public String toXML()
            {
                if (this.isNull)
                {
                    String xml = "<nullentry>\n";
                    xml += tag("weight", this.weight);
                    xml += tag("percentagelocked", "0");
                    return xml + "</nullentry>\n";
                }

                if (this.isProgrammerSound)
                {
                    String xml = "<programmer>\n";
                    xml += tag("weight", this.weight);
                    xml += tag("percentagelocked", "0");
                    return xml + "</programmer>\n";
                }

                if (this.isOscillator)
                {
                    String xml = "<oscillator>\n";

                    String type = (new String[] { "Sine", "Square", "Saw up", "Saw " +
                                                                              "down",
                        "Triangle"
                        , "Noise" })[this.waveType];
                    xml += tag("type", type);
                    xml += tag("frequency", this.frequency);
                    xml += tag("weight", this.weight);
                    xml += tag("percentagelocked", "0");

                    return xml + "</oscillator>\n";
                }

                String xml = "<waveform>\n";
                xml += tag("filename", this.name);
                xml += tag("soundbankname", this.bank);
                xml += tag("weight", this.weight);
                xml += tag("percentagelocked", "0");
                return xml + "</waveform>\n";
            }
        }

        public static class SoundDef
        {
            public String name;
            public int propertyIndex;
            public ArrayList<SoundDefWaveform> waveforms = new ArrayList<>();

            public String toXML(SoundDefProperty property)
            {
                String xml = "<sounddef>\n";
                xml += tag("name", this.name);
                xml += guid();
                xml += property.toXML();
                xml += tag("notes", "");
                for (SoundDefWaveform waveform : this.waveforms)
                    xml += waveform.toXML();
                return xml + "</sounddef>\n";
            }
        }

        public static class ReverbDefinition
        {
            public String name;

            public int room;
            public int roomHF;
            public float roomRollof;

            public float decayTime;
            public float decayHFRatio;

            public int reflections;
            public float reflectDelay;

            public int reverb;
            public float reverbDelay;

            public float diffusion;
            public float density;

            public float hfReference;
            public int roomLF;
            public float lfReference;
        }

        public static class SoundDefProperty
        {
            public int playMode;
            public int spawnTimeMin;
            public int spawnTimeMax;
            public int maxSpawned;
            public float volume;
            public int volumeRandMethod;
            public float volumeRandMin;
            public float volumeRandMax;
            public float volumeRand;
            public float pitch;
            public int pitchRandMethod;
            public float pitchRandMin;
            public float pitchRandMax;
            public float pitchRand;
            public float positionRandomization3D;

            public String toXML()
            {
                String xml = tag("type", (new String[] { "sequentialnoeventrestart",
                    "random",
                    "randomnorepeat", "sequential", "shuffle", "programmerselected",
                    "shuffleglobal" })[this.playMode]);
                xml += tag("spawntime_min", this.spawnTimeMin);
                xml += tag("spawntime_max", this.spawnTimeMax);
                xml += tag("spawn_max", this.maxSpawned);
                xml += tag("mode", "0");
                xml += tag("pitch", this.pitch * 4.0f);
                xml += tag("pitch_randmethod", this.pitchRandMethod);
                xml += tag("pitch_random_min", this.pitchRandMin * 4.0f);
                xml += tag("pitch_random_max", this.pitchRandMax * 4.0f);
                xml += tag("pitch_randomization", this.pitchRand * 4.0f);
                xml += tag("volume_db", fieldRatioToDecibel(this.volume));
                xml += tag("volume_randmethod", this.volumeRandMethod);
                xml += tag("volume_random_min", fieldRatioToDecibel(this.volumeRandMin));
                xml += tag("volume_random_max", fieldRatioToDecibel(this.volumeRandMax));
                xml += tag("volume_randomization", fieldRatioToDecibel(this.volumeRand));
                xml += tag("position_randomization", this.positionRandomization3D);
                return xml;
            }
        }

        public static class SoundDefFolder
        {
            String name;
            HashMap<String, SoundDefFolder> subFolders = new HashMap<>();
            ArrayList<SoundDef> soundDefs = new ArrayList<>();

            public SoundDefFolder(String name)
            {
                this.name = name;
            }

            public void addNewSoundDef(String path, SoundDef def)
            {
                // if (split_path.length == 1)
                this.soundDefs.add(def);
                // else if (split_path[0] == "") {
                //     String folderName = split_path[1];
                //     String subpath = "/";
                //     for (int i = 2; i < split_path.length; ++i) {
                //         subpath += split_path[i];
                //         if (i + 1 != split_path.length);
                //             subpath += "/";

                //     }
                //     SoundDefFolder subfolder = new SoundDefFolder(folderName);
                //     this.subFolders.put(folderName, subfolder);
                //     subfolder.addNewSoundDef(subpath, def);
                // }
            }

            public String toXML(ArrayList<SoundDefProperty> properties)
            {
                String xml = "<sounddeffolder>\n";
                xml += tag("name", this.name);
                xml += guid();
                xml += tag("open", "0");
                for (SoundDefFolder folder : this.subFolders.values())
                    xml += folder.toXML(properties);
                for (SoundDef def : this.soundDefs)
                    xml += def.toXML(properties.get(def.propertyIndex));
                return xml + "</sounddeffolder>\n";
            }
        }

        public int version;
        public String bankName;
        public ArrayList<SoundBank> waveBanks = new ArrayList<>();
        public EventCategory topEventCategory;
        public ArrayList<EventGroup> topEventGroups = new ArrayList<>();
        public ArrayList<SoundDefProperty> soundProperties = new ArrayList<>();
        public ArrayList<SoundDef> soundDefinitions = new ArrayList<>();

        private EventCategory readCategory(MemoryInputStream stream)
        {
            EventCategory category = new EventCategory();

            category.name = stream.str();
            category.volume = stream.f32();
            category.pitch = stream.f32();

            if (this.version > 0x28)
            {
                category.maxPlaybacks = stream.i32();
                category.maxPlaybacksBehavior = stream.i32();
            }

            int numSubCategories = stream.i32();
            for (int i = 0; i < numSubCategories; ++i)
                category.subCategories.add(readCategory(stream));

            return category;
        }

        private ArrayList<Property> readProperties(MemoryInputStream stream)
        {
            int numProperties = stream.i32();
            ArrayList<Property> properties = new ArrayList<>();
            for (int i = 0; i < numProperties; ++i)
            {
                Property property = new Property();
                property.name = stream.str();
                property.type = PropertyType.values()[stream.i32()];
                switch (property.type)
                {
                    case INT:
                    {
                        property.value = stream.s32();
                        break;
                    }
                    case FLOAT:
                    {
                        property.value = stream.f32();
                        break;
                    }
                    case STRING:
                    {
                        property.value = stream.str();
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Invalid property type!");

                }

                properties.add(property);
            }
            return properties;

        }

        private final HashSet<Integer> usedSoundDefIndices = new HashSet<>();

        private EventSound readEventSound(MemoryInputStream stream)
        {
            EventSound instance = new EventSound();

            if (version < 0x27)
            {
                instance.soundDefIndex = (short) stream.i32();
                stream.i32();
            }
            else instance.soundDefIndex = stream.i16();

            instance.start = stream.f32();
            instance.length = stream.f32();

            if (version > 0x1d)
                instance.startMode = stream.i32();

            instance.loopMode = stream.i8();
            instance.autoPitchParam = stream.i8();
            stream.seek(0x2); // reserved

            instance.loopCount = (version > 0x1e) ? stream.i32() : -1;

            instance.autoPitchEnabled = stream.i32();
            instance.autoPitchReference = stream.f32();
            if (version >= 0x24)
                instance.autopitchAtMin = stream.f32();

            instance.fineTune = stream.f32();

            if (version < 0x34) stream.seek(0x8);
            instance.volume = stream.f32();

            instance.fadeInLength = stream.f32();
            instance.fadeOutLength = stream.f32();
            if (this.version >= 0x18)
            {
                instance.fadeInType = stream.i32();
                instance.fadeOutType = stream.i32();
            }

            usedSoundDefIndices.add(instance.soundDefIndex & 0xffff);

            return instance;
        }

        private Event readEvent(MemoryInputStream stream)
        {
            Event event = new Event();

            boolean isSimpleEvent = false;
            if (version >= 0x34)
                isSimpleEvent = (stream.i32() & 0x10) != 0;

            event.name = stream.str();
            event.volume = stream.f32();
            event.pitch = stream.f32();

            if (this.version >= 0x1a)
                event.volumeRandomization = stream.f32();
            if (this.version >= 0x20)
            {
                event.pitchRandomization = stream.f32();
                if (this.version < 0x21)
                    event.pitchRandomization = 0.5f - event.pitchRandomization;
            }

            if (this.version > 0x9)
                event.priority = stream.i32();

            event.maxPlaybacks = stream.i32();
            if (event.maxPlaybacks < 1)
                event.maxPlaybacks = 1;

            event.mode = stream.u16();
            int geomFlags = stream.i16();
            event.ignoreGeometry = (geomFlags & 0xF000) == 0x4000;
            event.rollof3D = geomFlags & 0x0ff0;
            event.position3D = geomFlags & 0xf;

            event.minDistance3D = stream.f32();
            event.maxDistance3D = stream.f32();

            stream.seek(0x2); // technically when >= 0xf?
            event.oneshot = stream.u8() == 0x8;
            event.pitchRandUnits = stream.u8();

            if (this.version > 0x8)
            {
                event.speaker2DL = stream.f32();
                event.speaker2DR = stream.f32();
                event.speaker2DC = stream.f32();

                event.speakerLFE = stream.f32();

                event.speaker2DLR = stream.f32();
                event.speaker2DRR = stream.f32();
                event.speaker2DLS = stream.f32();
                event.speaker2DRS = stream.f32();

                event.coneInsideAngle3D = stream.f32();
                event.coneOutsideAngle3D = stream.f32();
                event.coneOutsideVolume3D = stream.f32();

                if (this.version >= 0xb)
                {
                    event.maxPlaybacksBehavior = stream.i32();

                    event.dopplerFactor3D = stream.f32();

                    if (this.version >= 0x1c)
                        event.reverbDryLevel = stream.f32();
                    event.reverbWetLevel = stream.f32();

                    if (this.version > 0x11)
                        event.speakerSpread3D = stream.f32();

                    if (this.version > 0x12)
                    {
                        event.fadeInTime = stream.i32();
                        event.fadeOutTime = stream.i32();
                    }

                    if (this.version > 0x2a)
                        event.spawnIntensity = stream.f32();

                    if (this.version > 0x2c)
                        event.spawnIntensityRandomization = stream.f32();

                    event.panLevel3D = stream.f32();

                    if (this.version > 0x27)
                        event.positionRandomization3D = stream.i32();
                }
            }

            if (isSimpleEvent)
            {
                stream.i32();
                Layer layer = new Layer();
                layer.priority = -1;
                layer.controlParameter = -1;
                layer.soundInstances.add(this.readEventSound(stream));
            }
            else
            {
                int numLayers = stream.i32();
                for (int i = 0; i < numLayers; ++i)
                {
                    Layer layer = new Layer();

                    stream.seek(0x2); // signature, always 0x0200
                    layer.priority = stream.i16();
                    layer.controlParameter = stream.i16();

                    int soundInstanceCount = stream.u16(); // sound_instance_count
                    int envelopeCount = stream.u16(); // envelope_count

                    for (int j = 0; j < soundInstanceCount; ++j)
                        layer.soundInstances.add(this.readEventSound(stream));

                    for (int j = 0; j < envelopeCount; ++j)
                    {
                        EventEnvelope envelope = new EventEnvelope();

                        if (this.version > 0x26)
                            envelope.parentIndex = stream.i32();
                        else // not bothering with the rest
                            envelope.parentIndex = -1;

                        envelope.name = stream.str();
                        envelope.effectParameterIndex = stream.i32();

                        int combinedFlags = stream.i32();
                        envelope.effect =
                            EffectType.fromFlags(combinedFlags & ~0x01 & 0xffff);
                        envelope.isMuted = (combinedFlags & 0x1) == 0x1;
                        envelope.flags = combinedFlags & ~0xffff;

                        // if (this.version > 0x25) {
                        //     int exflags = stream.i32();
                        //     if (exflags != 0)
                        //         System.out.println("weehoo!");
                        // }

                        int pointCount = stream.i32();
                        for (int k = 0; k < pointCount; ++k)
                        {
                            EventEnvelopePoint point = new EventEnvelopePoint();

                            point.x = stream.f32();
                            point.y = stream.f32();

                            if (this.version < 0xd) point.curveShape = 0x1;
                            else point.curveShape = stream.i32();

                            envelope.points.add(point);
                        }

                        envelope.controlParameterIndex = stream.i32();

                        if (this.version > 0x19)
                            envelope.mappingMethod = stream.i32();

                        layer.envelopes.add(envelope);
                    }

                    event.layers.add(layer);

                    int paramCount = stream.i32();
                    for (int j = 0; j < paramCount; ++j)
                    {
                        EventParameter parameter = new EventParameter();

                        parameter.name = stream.str();

                        if (this.version <= 0x11) stream.seek(0x8);

                        parameter.velocity = stream.f32();
                        parameter.min = stream.f32();
                        parameter.max = stream.f32();

                        int info = stream.i32(); // technically a difference when <
                        // 0x10, but
                        // dont need that much attention to detail
                        parameter.isPrimary = (info & 0x1) != 0;
                        parameter.loopBehavior = info & ~0x01;

                        if (this.version > 0x11)
                            parameter.seekSpeed = stream.f32();
                        parameter.controlledEnvelopeCount = stream.i32();

                        if (this.version >= 0xc)
                        {
                            int sustainPointCount = stream.i32();
                            for (int k = 0; k < sustainPointCount; ++k)
                                parameter.sustainPoints.add(stream.f32());
                        }

                        event.parameters.add(parameter);
                    }

                    event.userProperties = readProperties(stream);
                }
            }

            int categoryNameCount = stream.i32();
            for (int i = 0; i < categoryNameCount; i++)
                event.categories.add(stream.str());

            return event;
        }

        private EventGroup readEventGroup(MemoryInputStream stream)
        {
            EventGroup group = new EventGroup();

            group.name = stream.str();

            if (this.version > 0x16)
                group.userProperties = readProperties(stream);

            int numSubGroups = stream.i32();
            int numEvents = stream.i32();

            for (int i = 0; i < numSubGroups; ++i)
                group.subGroups.add(readEventGroup(stream));
            for (int i = 0; i < numEvents; ++i)
                group.events.add(readEvent(stream));

            return group;
        }

        public FMODEventFile(String file)
        {
            MemoryInputStream stream = new MemoryInputStream(file,
                CompressionFlags.USE_NO_COMPRESSION);
            stream.setLittleEndian(true);

            if (!stream.str(4).equals("FEV1"))
                throw new IllegalArgumentException("Not a FEV1 file!");

            this.version = stream.i32() >> 16;

            if (this.version > 0x2d) stream.i32();
            if (this.version >= 0x32) stream.i32();

            if (this.version > 0x18)
                this.bankName = stream.str();

            int numSoundBanks = stream.i32();
            for (int i = 0; i < numSoundBanks; ++i)
            {
                SoundBank wb = new SoundBank();

                int streamingType = stream.i32();
                switch (streamingType)
                {
                    default:
                    case 0x0100:
                        wb.streamingType = StreamingType.DECOMPRESS_INTO_MEMORY;
                        break;
                    case 0x0200:
                        wb.streamingType = StreamingType.LOAD_INTO_MEMORY;
                        break;
                    case 0x0080:
                        wb.streamingType = StreamingType.STREAM_FROM_DISK;
                        break;
                }

                if (this.version >= 0x14)
                    wb.maxStreams = stream.i32();

                wb.name = stream.str();

                this.waveBanks.add(wb);
            }

            this.topEventCategory = readCategory(stream);

            int numEventGroups = stream.i32();
            for (int i = 0; i < numEventGroups; i++)
                this.topEventGroups.add(readEventGroup(stream));

            if (this.version > 0x2d)
            {
                int numSoundDefProperties = stream.i32();
                for (int i = 0; i < numSoundDefProperties; ++i)
                {
                    SoundDefProperty property = new SoundDefProperty();
                    property.playMode = stream.i32();
                    property.spawnTimeMin = stream.i32();
                    property.spawnTimeMax = stream.i32();
                    property.maxSpawned = stream.i32();
                    property.volume = stream.f32();
                    property.volumeRandMethod = stream.i32();
                    property.volumeRandMin = stream.f32();
                    property.volumeRandMax = stream.f32();
                    property.volumeRand = stream.f32();
                    property.pitch = stream.f32();
                    property.pitchRandMethod = stream.i32();
                    property.pitchRandMin = stream.f32();
                    property.pitchRandMax = stream.f32();
                    property.pitchRand = stream.f32();
                    property.positionRandomization3D = stream.f32();

                    this.soundProperties.add(property);
                }
            }

            int numSoundDefs = stream.i32();
            for (int i = 0; i < numSoundDefs; ++i)
            {
                SoundDef def = new SoundDef();
                def.name = stream.str();

                if (this.version > 0x2d)
                    def.propertyIndex = stream.i32();
                // else readSoundDefProperty();

                int waveformCount = stream.i32();
                for (int j = 0; j < waveformCount; ++j)
                {
                    SoundDefWaveform waveform = new SoundDefWaveform();
                    int type = stream.i32();

                    if (this.version < 0xe) waveform.weight = 100;
                    else waveform.weight = stream.i32();

                    if (type == 0)
                    {
                        waveform.name = stream.str();
                        waveform.bank = stream.str();
                        waveform.indexInBank = stream.i32();
                        waveform.playtime = stream.i32();
                    }
                    else if (type == 1)
                    {
                        waveform.isOscillator = true;
                        waveform.waveType = stream.i32();
                        waveform.frequency = stream.f32();
                    }
                    else if (type == 2) waveform.isNull = true;
                    else if (type == 3) waveform.isProgrammerSound = true;

                    def.waveforms.add(waveform);
                }

                this.soundDefinitions.add(def);
            }
        }

        public SoundDefWaveform findWaveform(String bank, int index, String name)
        {
            for (SoundDef def : this.soundDefinitions)
            {
                for (SoundDefWaveform waveform : def.waveforms)
                {
                    if (waveform.bank != null && waveform.bank.equals(bank) && waveform.indexInBank == index && waveform.name.contains(name))
                        return waveform;
                }
            }
            return null;
        }

        public String toFDP(String bankDir, String outputDir)
        {
            StringBuilder xml = new StringBuilder(0x1000000);
            xml.append("<project>\n");
            xml.append(tag("name", this.bankName));
            xml.append(tag("version", 4));
            xml.append(tag("eventgroup_nextid", this.topEventGroups.size()));
            xml.append(tag("soundbank_nextid", this.waveBanks.size()));
            xml.append(tag("sounddef_nextid", this.soundDefinitions.size()));
            xml.append(tag("build_project", 1));
            xml.append(tag("build_headerfile", 0));
            xml.append(tag("build_banklists", 0));
            xml.append(tag("build_programmerreport", 0));
            xml.append(tag("build_applytemplate", 0));

            if (this.waveBanks.size() != 0)
                xml.append(tag("currentbank", this.waveBanks.get(0).name));
            xml.append(tag("currentlanguage", "default"));
            xml.append(tag("primarylanguage", "default"));
            xml.append(tag("language", "default"));
            xml.append(tag("templatefilename", ""));
            xml.append(tag("templatefileopen", 1));

            for (EventCategory category : this.topEventCategory.subCategories)
                xml.append(category.toXML());

            SoundDefFolder master = new SoundDefFolder("master");
            for (int i = 0; i < this.soundDefinitions.size(); ++i)
            {
                SoundDef def = this.soundDefinitions.get(i);
                if (this.usedSoundDefIndices.contains(i))
                    master.addNewSoundDef(def.name, def);
            }

            xml.append(master.toXML(this.soundProperties));

            for (EventGroup group : this.topEventGroups)
                xml.append(group.toXML(this.soundDefinitions));

            xml.append("<default_soundbank_props>\n");
            xml.append(tag("name", "default_soundbank_props"));
            xml.append(guid());
            xml.append(tag("load_into_rsx", "0"));
            xml.append(tag("disable_seeking", "0"));
            xml.append(tag("enable_syncpoints", "1"));
            xml.append(tag("hasbuiltwithsyncpoints", "0"));

            for (String console : CONSOLES)
                xml.append(tag("_" + console + "_banktype", "DecompressedSample"));
            xml.append(tag("notes", ""));
            xml.append(tag("rebuild", "0"));
            for (String console : CONSOLES)
            {
                xml.append(tag("_" + console + "_format", "PCM"));
                xml.append(tag("_" + console + "_quality", "50"));
                xml.append(tag("_" + console + "_optimisesamplerate", "0"));
                xml.append(tag("_" + console + "_forcesoftware",
                    (console.equals("PC") || console.equals("XBOX360") || console.equals("PS3"
                    )) ? "1" : "0"));
                xml.append(tag("_" + console + "_maxstreams", "10"));
            }
            xml.append("</default_soundbank_props>\n");
            // END HEADER

            for (SoundBank bank : this.waveBanks)
            {

                xml.append(bank.getHeaderXML());

                File file = new File(bankDir + "/" + bank.name + ".fsb");

                String format = "PCM";
                if (file.exists())
                {
                    FMODSampleBank fsb = new FMODSampleBank(file.getAbsolutePath());
                    int firstSampleMode = fsb.samples.get(0).mode;
                    if ((firstSampleMode & 0x00400000) != 0)
                        format = "ADPCM";
                    else if ((firstSampleMode & 0x00000200) != 0)
                        format = "MP3";

                    for (int i = 0; i < fsb.samples.size(); ++i)
                    {
                        Sample sample = fsb.samples.get(i);
                        SoundDefWaveform waveform = findWaveform(bank.name, i,
                            sample.name);

                        if (!(new File(outputDir + "/" + waveform.name).exists()))
                        {
                            byte[] wav = fsb.extract(i);
                            FileIO.write(wav, outputDir + "/" + waveform.name);
                        }

                        int mode = 0;
                        if ((sample.mode & 0x00000040) != 0 && (sample.mode & 0x00000400) != 0)
                            mode = 1;
                        else if ((sample.mode & 0x04000000) != 0)
                        {
                            if ((sample.mode & 0x00000400) != 0)
                                mode = 1;
                            else if ((sample.mode & 0x00000800) != 0)
                                mode = 2;
                            else if ((sample.mode & 0x00010000) != 0)
                                mode = 3;
                        }

                        xml.append("<waveform>\n");
                        xml.append(tag("filename", waveform.name));
                        xml.append(guid());
                        xml.append(tag("mindistance", sample.minDistance));
                        xml.append(tag("maxdistance", sample.maxDistance));
                        xml.append(tag("deffreq", sample.defFreq));
                        xml.append(tag("defvol", sample.defVol));
                        xml.append(tag("defpan", sample.defPan));
                        xml.append(tag("defpri", sample.defPri));
                        xml.append(tag("xmafiltering", "0"));
                        xml.append(tag("channelmode", mode));
                        xml.append(tag("quality_crossplatform", "0"));
                        xml.append(tag("quality", "-1"));
                        xml.append(tag("optimisedratereduction", "100"));
                        xml.append(tag("enableratereduction", "1"));
                        xml.append(tag("notes", ""));
                        xml.append("</waveform>\n");

                        // System.out.println(sample.name + " -> " + waveform.name);
                    }
                }


                xml.append(bank.getFooterXML(format));
            }


            // BEGIN FOOTER
            xml.append(tag("notes", ""));
            xml.append(tag("currentplatform", "PS3"));
            for (String console : CONSOLES)
            {
                xml.append(tag("_" + console + "_encryptionkey", ""));
                xml.append(tag("_" + console + "_builddirectory", ""));
                xml.append(tag("_" + console + "_audiosourcedirectory", ""));
                xml.append(tag("_" + console + "_prebuildcommands", ""));
                xml.append(tag("_" + console + "_postbuildcommands", ""));
                xml.append(tag("_" + console + "_buildinteractivemusic", "Yes"));
                xml.append(tag("_" + console + "_encryptionkey", ""));
            }

            xml.append(tag("presavecommands", ""));
            xml.append(tag("postsavecommands", ""));
            xml.append(tag("neweventusetemplate", 0));
            xml.append(tag("neweventlasttemplatename", ""));

            xml.append(FileIO.getResourceFileAsString("/footer.txt") + "\n");

            return xml + "</project>\n";
        }
    }

    public static class FMODSampleBank
    {
        public enum FMODSoundFormat
        {
            NONE,
            PCM8,
            PCM16,
            PCM24,
            PCM32,
            PCMFLOAT,
            GCADPCM,
            IMAAPCM,
            VAG,
            HEVAG,
            XMA,
            MPEG,
            CELT,
            AT9,
            XWMA,
            VORBIS,
            IT214,
            IT215
        }

        public static int FSOUND_LOOP_OFF = 0x00000001;
        public static final int FSOUND_LOOP_NORMAL = 0x00000002; /* For forward looping
            samples. */
        public static final int FSOUND_LOOP_BIDI = 0x00000004; /* For bidirectional looping
        samples. (no effect if in hardware). */
        public static final int FSOUND_8BITS = 0x00000008; /* For 8 bit samples. */
        public static final int FSOUND_16BITS = 0x00000010; /* For 16 bit samples. */
        public static final int FSOUND_MONO = 0x00000020; /* For mono samples. */
        public static final int FSOUND_STEREO = 0x00000040; /* For stereo samples. */
        public static final int FSOUND_UNSIGNED = 0x00000080; /* For user created source data
        containing unsigned samples. */
        public static final int FSOUND_SIGNED = 0x00000100; /* For user created source data
        containing signed data. */
        public static final int FSOUND_DELTA = 0x00000200; /* For user created source data
            stored
         as delta values. */
        public static final int FSOUND_IT214 = 0x00000400; /* For user created source data
            stored
         using IT214 compression. */
        public static final int FSOUND_IT215 = 0x00000800; /* For user created source data
            stored
         using IT215 compression. */
        public static final int FSOUND_HW3D = 0x00001000; /* Attempts to make samples use 3d
        hardware acceleration. (if the card supports it) */
        public static final int FSOUND_2D = 0x00002000; /* Tells software (not hardware) based
        sample not to be included in 3d processing. */
        public static final int FSOUND_STREAMABLE = 0x00004000; /* For a streamimg sound where
        you feed the data to it. */
        public static final int FSOUND_LOADMEMORY = 0x00008000; /* "name" will be interpreted as
        a pointer to data for streaming and samples. */
        public static final int FSOUND_LOADRAW = 0x00010000; /* Will ignore file format and
            treat
         as raw pcm. */
        public static final int FSOUND_MPEGACCURATE = 0x00020000; /* For FSOUND_Stream_Open -
             for
         accurate FSOUND_Stream_GetLengthMs/FSOUND_Stream_SetTime. WARNING, see
         FSOUND_Stream_Open for inital opening time performance issues. */
        public static final int FSOUND_FORCEMONO = 0x00040000; /* For forcing stereo streams and
        samples to be mono - needed if using FSOUND_HW3D and stereo data - incurs a small speed
        hit for streams */
        public static final int FSOUND_HW2D = 0x00080000; /* 2D hardware sounds. allows hardware
        specific effects */
        public static final int FSOUND_ENABLEFX = 0x00100000; /* Allows DX8 FX to be played back
        on a sound. Requires DirectX 8 - Note these sounds cannot be played more than once, be 8
        bit, be less than a certain size, or have a changing frequency */
        public static final int FSOUND_MPEGHALFRATE = 0x00200000; /* For FMODCE only - decodes
        mpeg streams using a lower quality decode, but faster execution */
        public static final int FSOUND_IMAADPCM = 0x00400000; /* Contents are stored compressed
        as IMA ADPCM */
        public static final int FSOUND_VAG = 0x00800000; /* For PS2 only - Contents are
        compressed as Sony VAG format */
        //public static final int FSOUND_NONBLOCKING      0x01000000 /* For
        // FSOUND_Stream_Open/FMUSIC_LoadSong - Causes stream or music to open in the background
        // and not block the foreground app. See FSOUND_Stream_GetOpenState or
        // FMUSIC_GetOpenState to determine when it IS ready. */
        public static final int FSOUND_XMA = 0x01000000;
        public static final int FSOUND_GCADPCM = 0x02000000; /* For Gamecube only - Contents are
        compressed as Gamecube DSP-ADPCM format */
        public static final int FSOUND_MULTICHANNEL = 0x04000000; /* For PS2 and Gamecube only -
        Contents are interleaved into a multi-channel (more than stereo) format */
        public static final int FSOUND_USECORE0 = 0x08000000; /* For PS2 only - Sample/Stream is
        forced to use hardware voices 00-23 */
        public static final int FSOUND_USECORE1 = 0x10000000; /* For PS2 only - Sample/Stream is
        forced to use hardware voices 24-47 */
        public static final int FSOUND_LOADMEMORYIOP = 0x20000000; /* For PS2 only - "name" will
        be interpreted as a pointer to data for streaming and samples. The address provided will
        be an IOP address */
        public static final int FSOUND_IGNORETAGS = 0x40000000; /* Skips id3v2 etc tag checks
        when opening a stream, to reduce seek/read overhead when opening files (helps with CD
        performance) */
        public static final int FSOUND_STREAM_NET = 0x80000000; /* Specifies an internet
            stream */
        public static final int FSOUND_NORMAL = (FSOUND_16BITS | FSOUND_SIGNED | FSOUND_MONO);

        public static class Sample
        {
            public String name = ".wav";
            public int sampleLength;
            public int compressedDataLength;
            public int loopStart = 0;
            public int loopEnd;
            public int mode = -2147483072;
            public int defFreq = 44100;
            public int defVol = 255;
            public short defPan = 128;
            public int defPri = 128;
            public short numChannels = 2;
            public float minDistance = 1.0f;
            public float maxDistance = 10000.0f;
            public int varFreq = 0;
            public int varVol = 0;
            public short varPan = 0;
            public ArrayList<Integer> cuePoints = new ArrayList<>();
        }

        public int mode;
        public ArrayList<Sample> samples = new ArrayList<>();
        public transient byte[] sampleData;
        public transient boolean isAligned;
        public transient boolean isBigEndian;

        public static byte[] fromAudioFile(String path)
        {
            File destination = new File(path + ".mp3");
            ProcessBuilder builder = new ProcessBuilder("ffmpeg", "-y", "-i", path,
                destination.getAbsolutePath(), "-nostats", "-loglevel", "0");
            builder.redirectErrorStream(true);
            try { builder.start().waitFor(); }
            catch (Exception ex) { return null; }
            if (!destination.exists()) return null;
            byte[] sourceMP3 = FileIO.read(destination.getAbsolutePath());
            destination.delete();

            MemoryOutputStream paddedMP3 = new MemoryOutputStream(sourceMP3.length * 2);
            MemoryInputStream stream = new MemoryInputStream(sourceMP3);

            int[] BITRATE_INDEX_V1L3 = new int[] {
                0, 32000, 40000, 48000, 56000, 64000, 80000, 96000, 112000, 128000,
                160000,
                192000, 224000, 256000, 320000
            };

            int[] SAMPLERATE_INDEX_V1 = new int[] {
                44100, 48000, 32000
            };

            if (sourceMP3[0] == 0x49)
                stream.seek(0xa + sourceMP3[0x9]);

            int frameCount = 0;
            while (stream.getOffset() < stream.getLength())
            {
                byte[] header = stream.bytes(4);
                if (header[0] != -1) break;
                frameCount++;

                float bitrate = BITRATE_INDEX_V1L3[((header[2] & 0xff) >> 4)];
                float samplerate = SAMPLERATE_INDEX_V1[((header[2] & 0b1100) >> 2)];
                int padding = ((header[2] & 0xff) >> 1) & 1;
                int frameLength = (int) (((144.0f * bitrate) / samplerate) + padding);

                paddedMP3.bytes(header);
                paddedMP3.bytes(stream.bytes(frameLength - 4));
                for (int n = paddedMP3.getOffset(); (n % 0x2) != 0; n++)
                    paddedMP3.pad(0x1);
            }

            for (int n = paddedMP3.getOffset(); (n & 0xf) != 0; n++)
                paddedMP3.seek(0x1);
            paddedMP3.shrink();
            sourceMP3 = paddedMP3.getBuffer();

            FMODSampleBank bank = new FMODSampleBank();
            bank.mode = FSOUND_MONO;
            Sample sample = new Sample();
            sample.compressedDataLength = sourceMP3.length;

            sample.sampleLength = frameCount * 1152;
            sample.loopEnd = sample.sampleLength - 1;

            int cue = sample.sampleLength / 9;
            for (int i = 0; i < 8; ++i)
                sample.cuePoints.add((i + 1) * cue);

            bank.sampleData = sourceMP3;

            bank.samples.add(sample);

            return bank.build();
        }

        public byte[] build()
        {
            int size = 0x30;
            for (Sample sample : this.samples)
            {
                size += 0x50;
                if (sample.cuePoints.size() != 0)
                {
                    size += 0x8 + (0x104 * sample.cuePoints.size());
                }
            }
            if ((size % 0x10) != 0)
                size += 0x10 - (size % 0x10);

            int sampleHeaderSize = size - 0x30;
            int sampleDataSize = sampleData.length;

            size += sampleDataSize;

            MemoryOutputStream stream = new MemoryOutputStream(size);
            stream.setLittleEndian(true);
            stream.str("FSB4", 4);
            stream.i32(this.samples.size());
            stream.i32(sampleHeaderSize);
            stream.i32(sampleDataSize);
            stream.i32(262144);
            stream.i32(this.mode);
            stream.seek(8 + 16);

            int dataOffset = stream.getOffset() + sampleHeaderSize;

            for (Sample sample : this.samples)
            {
                int sampleSize = 0x50;
                if (sample.cuePoints.size() != 0)
                    sampleSize += 0x8 + (0x104 * sample.cuePoints.size());

                stream.u16(sampleSize);
                stream.str(sample.name, 30);
                stream.i32(sample.sampleLength);
                stream.i32(sample.compressedDataLength);
                stream.i32(sample.loopStart);
                stream.i32(sample.loopEnd);
                stream.i32(sample.mode);
                stream.i32(sample.defFreq);
                stream.u16(sample.defVol);
                stream.i16(sample.defPan);
                stream.u16(sample.defPri);
                stream.i16(sample.numChannels);
                stream.f32(sample.minDistance);
                stream.f32(sample.maxDistance);
                stream.i32(sample.varFreq);
                stream.u16(sample.varVol);
                stream.i16(sample.varPan);

                if (sample.cuePoints.size() != 0)
                {
                    stream.str("SYNC", 4);
                    stream.i32(sample.cuePoints.size());
                    for (Integer cue : sample.cuePoints)
                    {
                        stream.i32(cue);
                        stream.str("startpoint", 0x100);
                    }
                }
            }

            stream.seek(dataOffset, SeekMode.Begin);
            stream.bytes(this.sampleData);

            return stream.getBuffer();
        }

        public FMODSampleBank() { }

        public FMODSampleBank(String path)
        {
            MemoryInputStream stream = new MemoryInputStream(path,
                CompressionFlags.USE_NO_COMPRESSION);
            stream.setLittleEndian(true);

            if (!stream.str(4).equals("FSB4"))
                throw new IllegalArgumentException("invalid!");
            int numSamples = stream.i32();
            int sampleHeaderSize = stream.i32();
            int sampleDataSize = stream.i32();
            int version = stream.i32();

            this.mode = stream.i32();

            isBigEndian = (this.mode & 0x08) != 0;
            isAligned = (this.mode & 0x40) != 0;

            stream.seek(8 + 16);

            int sampleDataOffset = stream.getOffset() + sampleHeaderSize;

            for (int i = 0; i < numSamples; ++i)
            {
                stream.i16();
                Sample sample = new Sample();
                sample.name = stream.str(30);
                sample.sampleLength = stream.i32();
                sample.compressedDataLength = stream.i32();
                sample.loopStart = stream.i32();
                sample.loopEnd = stream.i32();
                sample.mode = stream.i32();
                sample.defFreq = stream.i32();
                sample.defVol = stream.u16();
                sample.defPan = stream.i16();
                sample.defPri = stream.u16();
                sample.numChannels = stream.i16();
                sample.minDistance = stream.f32();
                sample.maxDistance = stream.f32();
                sample.varFreq = stream.i32();
                sample.varVol = stream.u16();
                sample.varPan = stream.i16();

                this.samples.add(sample);
            }

            stream.seek(sampleDataOffset, SeekMode.Begin);
            this.sampleData = stream.bytes(sampleDataSize);
        }

        public byte[] extract(int index)
        {
            int offset = 0;
            for (int i = 0; i < index; ++i)
            {
                offset += samples.get(i).compressedDataLength;
                if (this.isAligned && ((offset & 0x1f) != 0))
                    offset = (offset + 0x1f) & (~0x1f);
            }

            Sample sample = this.samples.get(index);

            int mode = sample.mode;
            int freq = sample.defFreq;
            int channels = sample.numChannels;
            int bits = 16;
            FMODSoundFormat codec = FMODSoundFormat.PCM16;

            if ((mode & FSOUND_8BITS) != 0) bits = 8;
            if ((mode & FSOUND_16BITS) != 0) bits = 16;

            if ((mode & FSOUND_DELTA) != 0) codec = FMODSoundFormat.MPEG;
            if ((mode & FSOUND_IT214) != 0) codec = FMODSoundFormat.IT214;
            if ((mode & FSOUND_IT215) != 0) codec = FMODSoundFormat.IT215;
            if ((mode & FSOUND_IMAADPCM) != 0) codec = FMODSoundFormat.IMAAPCM;
            if ((mode & FSOUND_VAG) != 0) codec = FMODSoundFormat.VAG;
            if ((mode & FSOUND_GCADPCM) != 0) codec = FMODSoundFormat.GCADPCM;
            if ((mode & FSOUND_XMA) != 0) codec = FMODSoundFormat.XMA;
            if ((mode & FSOUND_USECORE0) != 0) codec = FMODSoundFormat.CELT;
            if ((mode & FSOUND_8BITS) != 0)
            {
                if (codec == FMODSoundFormat.PCM16)
                    codec = FMODSoundFormat.PCM8;
            }

            if (!(codec == FMODSoundFormat.MPEG || codec == FMODSoundFormat.IMAAPCM))
                throw new RuntimeException("unsupported codec!");


            if (codec == FMODSoundFormat.IMAAPCM)
            {
                MemoryOutputStream stream =
                    new MemoryOutputStream(sample.compressedDataLength + 0x1000);
                stream.setLittleEndian(true);

                short formatTag = 0x0011;
                int samplesPerSec = freq;
                short bitsPerSample = 4;
                short blockAlign = (short) (channels * 36);
                int avgBytesPerSec = (689 * blockAlign) + 4;

                stream.str("RIFF", 4);
                stream.i32(4 + 8 + 16 + 4 + 8 + sample.compressedDataLength);
                stream.str("WAVE", 4);
                stream.str("fmt ", 4);
                stream.i32(16 + 4);

                stream.i16(formatTag);
                stream.i16((short) channels);
                stream.i32(samplesPerSec);
                stream.i32(avgBytesPerSec);
                stream.i16(blockAlign);
                stream.i16(bitsPerSample);

                stream.bytes(new byte[] { 0x02, 0x00, 0x40, 0x00 });

                stream.str("data", 4);
                stream.i32(sample.compressedDataLength);

                stream.bytes(Arrays.copyOfRange(this.sampleData, offset,
                    offset + sample.compressedDataLength));

                stream.shrink();
                return stream.getBuffer();
            }

            if (isBigEndian) throw new RuntimeException("unsupported endian!");

            int[] BITRATE_INDEX_V1L3 = new int[] {
                0, 32000, 40000, 48000, 56000, 64000, 80000, 96000, 112000, 128000,
                160000,
                192000, 224000, 256000, 320000
            };

            int[] SAMPLERATE_INDEX_V1 = new int[] {
                44100, 48000, 32000
            };

            byte[] mp3 = Arrays.copyOfRange(this.sampleData, offset,
                offset + sample.compressedDataLength);
            MemoryInputStream stream = new MemoryInputStream(mp3);
            MemoryOutputStream handle = new MemoryOutputStream(mp3.length);

            while (stream.getOffset() < stream.getLength())
            {
                byte[] header = stream.bytes(4);
                if (header[0] != -1) break;

                float bitrate = BITRATE_INDEX_V1L3[((header[2] & 0xff) >> 4)];
                float samplerate = SAMPLERATE_INDEX_V1[((header[2] & 0b1100) >> 2)];
                int padding = ((header[2] & 0xff) >> 1) & 1;
                int frameLength = (int) (((144.0f * bitrate) / samplerate) + padding);

                handle.bytes(header);
                handle.bytes(stream.bytes(frameLength - 4));

                if ((sample.mode & FSOUND_MULTICHANNEL) != 0)
                {
                    for (int n = stream.getOffset(); (n & 0xf) != 0; n++)
                        stream.seek(0x1);
                }
                else
                {
                    for (int n = stream.getOffset(); (n % 0x2) != 0; n++)
                        stream.seek(0x1);
                }
            }

            handle.shrink();
            mp3 = handle.getBuffer();

            File directory = ResourceSystem.getWorkingDirectory();
            File inputFile = new File(directory, "test.mp3");
            File outputFile = new File(directory, "test.wav");
            boolean isMono = channels == 1;
            FileIO.write(mp3, inputFile.getAbsolutePath());
            ProcessBuilder builder = new ProcessBuilder("python",
                "E:/scratch/fmod/conv.py",
                inputFile.getAbsolutePath(),
                outputFile.getAbsolutePath(),
                isMono ? "true" : "false");

            try
            {
                builder.start().waitFor();
                if (inputFile.exists()) inputFile.delete();

                byte[] data = FileIO.read(outputFile.getAbsolutePath());
                if (outputFile.exists()) outputFile.delete();

                return data;
            }
            catch (Exception ex)
            {
                return null;
            }
        }
    }


    public static void main(String[] args)
    throws Exception
    {
        byte[] data = FMODSampleBank.fromAudioFile("C:/~/australia_neopolitan_dreams_inst.at3");
        FileIO.write(data, "C:/~/australia_neopolitan_dreams_inst.fsb");

        // FMODEventFile file = new FMODEventFile
        // ("F:/ps3/game/NPUA80662/USRDIR/gamedata/audio/ps3_main.fev");
        // FileIO.write(file.toFDP("F:/ps3/game/NPUA80662/USRDIR/gamedata/audio", "E:/art/
        // .lbp2/audio").getBytes(), "E:/art/.lbp2/audio/main.fdp");
        // file.extract("E:/scratch/fmod", "E:/art/audio");

        // FMODEventFile file = new FMODEventFile("C:/users/aidan/desktop/ps3_move.fev");
        // FileIO.write(file.toFDP("C:/users/aidan/desktop", "C:/users/aidan/desktop")
        // .getBytes()
        // , "c:/users/aidan/desktop/move.fdp");


        // FMODSampleBank bank = new FMODSampleBank("C:/~/voiceover.fsb");
        // for (int i = 0; i < bank.samples.size(); ++i) {
        //     Sample sample = bank.samples.get(i);
        //     if (sample.name.equals("pod_select_pins.wav")) {
        //         byte[] wav = bank.extract(i);
        //         FileIO.write(wav, "C:/Users/Aidan/Desktop/test.wav");
        //     }

        // }

        // FMODSampleBank fsb = new FMODSampleBank("C:/~/sfxbank_compressed.fsb");
        // System.out.println(fsb.samples.size());
        // for (int i = 0; i < fsb.samples.size(); ++i) {
        //     String name = fsb.samples.get(i).name.split("\\.")[0] + ".wav";

        //     int dupeIndex = 1;
        //     File file = new File("C:/Users/Aidan/Desktop/sfxbank_compressed/", name);
        //     while (file.exists()) {
        //         name = fsb.samples.get(i).name.split("\\.")[0] + String.format("_%02d",
        //         dupeIndex) + ".wav";
        //         file = new File("C:/Users/Aidan/Desktop/sfxbank_compressed/", name);
        //         dupeIndex++;
        //     }

        //     byte[] data = fsb.extract(i);
        //     FileIO.write(data, "C:/Users/Aidan/Desktop/sfxbank_compressed/" + name);
        // }


    }
}
