import cwlib.enums.Part;
import cwlib.resources.RLevel;
import cwlib.resources.RPlan;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.CompactComponent;
import cwlib.structs.things.parts.*;
import cwlib.types.SerializedResource;
import cwlib.types.data.WrappedResource;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;
import org.joml.Vector3f;
import utils.PositionUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class SequencerDump {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("java -jar sequencerdump.java <input> <outputPath>");
            return;
        }

        File input = new File(args[0]);
        File outputPath = new File(args[1]);

        if (!input.exists()) {
            System.err.println("Input file doesn't exist!");
            return;
        }

        processFile(input, outputPath);
    }

    private static void processFile(File input, File outputPath) {
        System.out.println("Processing file " + input.getName());

        if (input.isDirectory()) {
            for (File current : Objects.requireNonNull(input.listFiles())) {
                processFile(current, outputPath);
            }
            return;
        }

        WrappedResource wrapper;
        if (input.getAbsolutePath().toLowerCase().endsWith(".json")) {
            System.out.println("[MODE] JSON -> MIDI");
            wrapper = GsonUtils.fromJSON(
                    FileIO.readString(Path.of(input.getAbsolutePath())),
                    WrappedResource.class
            );
        } else {
            System.out.println("[MODE] RESOURCE -> MIDI");
            SerializedResource resource = new SerializedResource(input.getAbsolutePath());
            wrapper = new WrappedResource(resource);
        }

        List<Thing> things;
        if (wrapper.resource instanceof RLevel level) {
            PWorld world = level.worldThing.getPart(Part.WORLD);
            things = world.things;
        } else if (wrapper.resource instanceof RPlan plan) {
            things = List.of(plan.getThings());
        } else {
            throw new RuntimeException("Unsupported resource " + wrapper.type);
        }

        for (Thing thing : things) {
            if (thing == null) {
                // Skip null things
                continue;
            }

            PMicrochip microchip = thing.getPart(Part.MICROCHIP);
            if (microchip == null) {
                // Not a Microchip
                continue;
            }

            // Include instruments of sequencers with an open circuit board
            Thing circuitBoardThing = microchip.circuitBoardThing;
            if (circuitBoardThing.hasPart(Part.POS)) {
                microchip.components = things.stream().filter(current ->
                        current != null
                        && current.parent != null
                        && current.parent.UID == circuitBoardThing.UID
                        && current.hasPart(Part.INSTRUMENT)
                ).map(child -> {
                    Vector3f instrumentPosition = PositionUtils.getRelativePosition(circuitBoardThing, child);
                    CompactComponent component = new CompactComponent();
                    component.x = instrumentPosition.x;
                    component.y = instrumentPosition.y;
                    component.thing = child;

                    return component;
                }).toArray(CompactComponent[]::new);
            }

            PSequencer sequencer = thing.getPart(Part.SEQUENCER);
            if (sequencer == null) {
                // Not a sequencer
                continue;
            }
            if (!sequencer.musicSequencer) {
                //  Not a music sequencer
                continue;
            }
            MidiDumper dumper = new MidiDumper(thing);
            dumper.loadFromThing();
            Sequence sequence = dumper.getSequence();
            File sequenceOutputFolder = new File(outputPath, sequence.getName());
            try {
                dumper.writeMidiTracks(sequenceOutputFolder);
            } catch (Exception e) {
                throw new RuntimeException("Unable to export midi!", e);
            }
        }
    }
}
