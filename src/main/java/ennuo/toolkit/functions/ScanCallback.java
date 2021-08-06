package ennuo.toolkit.functions;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.resources.Mesh;
import ennuo.craftworld.resources.enums.Magic;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.types.FileArchive;
import ennuo.craftworld.types.FileDB;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ScanCallback {
    public static void scanRawData() {
        Toolkit toolkit = Toolkit.instance;
        File dump = toolkit.fileChooser.openFile("data.bin", "", "", false);
        if (dump == null) return;

        System.out.println("Loading Image into memory, this may take a while.");

        Data data = new Data(dump.getAbsolutePath());

        System.out.println("Image loaded");


        File dumpMap = toolkit.fileChooser.openFile("dump.map", "", "", true);
        if (dumpMap == null) return;

        FileIO.write(new byte[] {
            0x00,
            0x00,
            0x01,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00
        }, dumpMap.getAbsolutePath());

        FileDB out = new FileDB(dumpMap);

        File file = toolkit.fileChooser.openFile("dump.farc", "farc", "FileArchive", true);
        if (file == null) return;
        FileIO.write(new byte[] {
            0,
            0,
            0,
            0,
            0x46,
            0x41,
            0x52,
            0x43
        }, file.getAbsolutePath());
        FileArchive farc = new FileArchive(file);

        String[] headers = new String[60];
        Byte[] chars = new Byte[60];
        int m = 0;
        for (Magic magic: Magic.values()) {
            headers[m] = magic.value.substring(0, 3);
            chars[m] = (byte) magic.value.charAt(0);
            m++;
        }
        headers[56] = "TEX";
        chars[56] = (byte)
        "T".charAt(0);
        headers[57] = "FSB";
        chars[57] = (byte)
        "F".charAt(0);
        headers[58] = "BIK";
        chars[58] = (byte)
        "B".charAt(0);
        headers[59] = "GTF";
        chars[59] = (byte) " ".charAt(0);
        
        Set < String > HEADERS = new HashSet<String> (Arrays.asList(headers));
        Set < Byte > VALUES = new HashSet<Byte> (Arrays.asList(chars));

        toolkit.databaseService.submit(() -> {
            System.out.println("Started scanning this may take a while, please wait.");
            toolkit.progressBar.setVisible(true);
            toolkit.progressBar.setMaximum(data.length);

            int resourceCount = 0;
            int GUID = 0x00150000;
            while ((data.offset + 4) <= data.length) {
                toolkit.progressBar.setValue(data.offset + 1);
                int begin = data.offset;

                if (!VALUES.contains(data.data[data.offset])) {
                    data.offset++;
                    continue;
                }

                String magic = data.str(3);

                if (!HEADERS.contains(magic)) {
                    data.seek(begin + 1);
                    continue;
                }

                String type = data.str(1);

                try {
                    byte[] buffer = null;

                    switch (type) {
                        case "i":
                            {
                                if (!magic.equals("BIK")) break;
                                int size = Integer.reverseBytes(data.int32());
                                data.offset -= 8;
                                buffer = data.bytes(size + 8);
                            }
                        case "t":
                            {
                                if (magic.equals("FSB") || magic.equals("TEX")) break;
                                int end = 0;
                                while ((data.offset + 4) <= data.length) {
                                    String mag = data.str(3);
                                    if (HEADERS.contains(mag)) {
                                        String t = data.str(1);
                                        if (t.equals(" ") || t.equals("4") || t.equals("b") || t.equals("t") || t.equals("i")) {
                                            data.offset -= 4;
                                            end = data.offset;
                                            data.seek(begin);
                                            break;
                                        }
                                    } else data.offset -= 2;
                                }

                                buffer = data.bytes(end - begin);

                                final String converted = new String(buffer, StandardCharsets.UTF_8);
                                final byte[] output = converted.getBytes(StandardCharsets.UTF_8);

                                if (!Arrays.equals(buffer, output))
                                    buffer = null;

                                break;
                            }


                        case "4":
                            {
                                if (!magic.equals("FSB")) break;
                                int count = Integer.reverseBytes(data.int32());
                                data.forward(0x4);
                                int size = Integer.reverseBytes(data.int32());
                                data.forward(0x20);
                                for (int i = 0; i < count; ++i)
                                    data.forward(Short.reverseBytes(data.int16()) - 2);
                                if (data.data[data.offset] == 0) {
                                    while (data.int8() == 0);
                                    data.offset -= 1;
                                }
                                data.forward(size);
                                size = data.offset - begin;
                                data.seek(begin);
                                buffer = data.bytes(size);
                                break;
                            }

                        case "b":
                            {
                                if (magic.equals("FSB") || magic.equals("TEX")) break;
                                int revision = data.int32f();
                                if (revision > 0x021803F9 || revision < 0) {
                                    data.seek(begin + 1);
                                    continue;
                                }
                                int dependencyOffset = data.int32f();
                                data.forward(dependencyOffset - 12);
                                int count = data.int32f();
                                for (int j = 0; j < count; ++j) {
                                    data.resource(RType.FILE_OF_BYTES, true);
                                    data.int32f();
                                }

                                int size = data.offset - begin;
                                data.seek(begin);

                                buffer = data.bytes(size);
                            }

                        case " ":
                            {
                                if (magic.equals("TEX")) data.forward(2);
                                else if (magic.equals("GTF")) data.forward(0x1a);
                                else break;
                                int count = data.int16();
                                int size = 0;
                                for (int j = 0; j < count; ++j) {
                                    size += data.int16();
                                    data.int16();
                                }
                                data.forward(size);


                                if (data.offset < 0 || ((data.offset + 1) >= data.length)) {
                                    data.seek(begin + 1);
                                    continue;
                                }

                                size = data.offset - begin;
                                data.seek(begin);
                                buffer = data.bytes(size);
                            }


                    }

                    data.seek(begin + 1);

                    if (buffer != null) {
                        int querySize = ((buffer.length * 10) + farc.queueSize + farc.hashTable.length + 8 + (farc.entries.size() * 0x1C)) * 2;
                        if (querySize < 0 || querySize >= Integer.MAX_VALUE) {
                            System.out.println("Ran out of memory, flushing current changes...");
                            farc.save(toolkit.progressBar);
                            toolkit.progressBar.setMaximum(data.length);
                            toolkit.progressBar.setValue(data.offset + 1);
                        }

                        resourceCount++;
                        farc.add(buffer);

                        byte[] sha1 = Bytes.SHA1(buffer);
                        FileEntry entry = Globals.findEntry(sha1);
                        if (entry != null) {
                            System.out.println("Found Resource : " + entry.path + " (0x" + Bytes.toHex(begin) + ")");
                            out.add(entry);
                        }
                        //System.out.println("Found Resource : " + entry.path + " (0x" + Bytes.toHex(begin) + ")");
                        else {
                          
                            FileEntry e = new FileEntry(buffer, Bytes.SHA1(buffer));

                            String name = "" + begin;

                            switch (magic) {
                                case "PLN":
                                    name += ".plan";
                                    break;
                                case "LVL":
                                    name += ".bin";
                                    break;
                                default:
                                    name += "." + magic.toLowerCase();
                                    break;
                            }

                            if (magic.equals("MSH")) {
                                Mesh mesh = new Mesh("mesh", buffer);
                                name = mesh.bones[0].name + ".mol";
                            }


                            e.path = "resources/" + magic.toLowerCase() + "/" + name;
                            e.GUID = GUID;

                            GUID++;

                            out.add(e);


                            System.out.println("Found Resource : " + magic + type + " (0x" + Bytes.toHex(begin) + ")");
                        }

                    }

                } catch (Exception e) {
                    data.seek(begin + 1);
                }
            }


            toolkit.progressBar.setVisible(false);
            toolkit.progressBar.setMaximum(0);
            toolkit.progressBar.setValue(0);

            farc.save(toolkit.progressBar);
            out.save(out.path);
        });
    }
}