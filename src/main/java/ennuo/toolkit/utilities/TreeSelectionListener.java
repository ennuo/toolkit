package ennuo.toolkit.utilities;

import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.resources.Animation;
import ennuo.craftworld.resources.GfxMaterial;
import ennuo.craftworld.resources.Mesh;
import ennuo.craftworld.resources.Pack;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.resources.InventoryItem;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.FileEntry;
import static ennuo.toolkit.utilities.Globals.currentWorkspace;
import ennuo.toolkit.windows.Toolkit;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JTree;

public class TreeSelectionListener {
    public static void listener(JTree tree) {
        Toolkit toolkit = Toolkit.instance;
        JTree currentTree = toolkit.getCurrentTree();
        if (tree == currentTree) {
            toolkit.entryModifiers.setEnabledAt(1, false);
            toolkit.entryModifiers.setSelectedIndex(0);
        }
        if (tree == currentTree)
            toolkit.dependencyTree.setModel(null);
        if (tree.getSelectionPath() == null)
            return;

        FileNode selected = toolkit.getLastSelected(tree);
        FileEntry entry = selected.entry;

        toolkit.setEditorPanel(selected);
        if (selected.entry == null) {
            toolkit.updateWorkspace();
            return;
        }

        toolkit.resourceService.submit(() -> {
            if (!Globals.canExtract()) return;

            byte[] entryBuffer = null;
            entryBuffer = Globals.extractFile(entry.SHA1);
            if (entryBuffer == null) {
                if (Toolkit.instance.getCurrentDB().USRDIR != null && currentWorkspace == Globals.WorkspaceType.MAP) {
                    System.out.println("Attempting to extract from disk...");
                    entryBuffer = FileIO.read(Toolkit.instance.getCurrentDB().USRDIR + entry.path.replace("/", "\\"));
                }
            }
            entry.data = entryBuffer; toolkit.updateWorkspace();
            if (entryBuffer == null) {
                toolkit.setHexEditor(null);
                return;
            }
            toolkit.setHexEditor(entryBuffer);
            if (entry.dependencyModel == null || entry.dependencies == null || entry.hasMissingDependencies) {
                FileModel model = new FileModel(new FileNode("x", null, null));
                Resource resource = new Resource(entryBuffer);
                boolean recursive = !(resource.magic.equals("PCKb") || resource.magic.equals("SLTb") || resource.magic.equals("LVLb") || resource.magic.equals("ADCb") || resource.magic.equals("PALb"));
                entry.hasMissingDependencies = resource.getDependencies(entry, recursive) != 0;
                entry.dependencies = resource.dependencies;
                toolkit.generateDependencyTree(entry, model);
                entry.dependencyModel = model;
            }

            if (Globals.lastSelected == selected && entry.dependencyModel != null && tree == currentTree)
                toolkit.dependencyTree.setModel(entry.dependencyModel);
            String path = entry.path.toLowerCase();

            String ext = path.substring(path.lastIndexOf(".") + 1);
            
            Resource res = new Resource(entryBuffer);
            entry.revision = res.revision;
            toolkit.setEditorPanel(selected);
            switch (ext) {
                case "pck":
                    if (entry.getResource("pack") == null) {
                        res.decompress(true);
                        try {
                            Pack pack = new Pack(res);
                            entry.setResource("pack", pack);
                        } catch (Exception e) {
                            System.err.println("There was an error processing the RPack file! -> ");
                            System.err.println(e);
                        }
                    }
                    break;
                case "slt":
                    ArrayList<Slot> slots = entry.getResource("slots");
                    if (slots == null) {
                        if (res.magic.equals("SLTt")) return;
                        res.decompress(true);
                        entry.revision = res.revision;

                        int count = res.i32();
                        slots = new ArrayList<Slot>(count);
                        for (int i = 0; i < count; ++i) {
                            Slot slot = new Slot(res, true, false);
                            slots.add(slot);
                            if (slot.root != null) {
                                FileEntry e = Globals.findEntry(slot.root);
                                e.revision = res.revision;
                                if (e != null)
                                    e.setResource("slot", slot);
                            }
                        }
                        entry.setResource("slots", slots);
                    }
                    break;
                case "bin":
                    Slot slot = entry.getResource("slot");
                    if (slot != null) {
                        if (slot.renderedIcon == null)
                            slot.renderIcon(entry);
                        toolkit.setImage(slot.renderedIcon);
                    }
                    break;
                case "tex":
                case "gtf":
                case "dds":
                case "jpg":
                case "jpeg":
                case "png":
                case "jfif":
                    if (entry.getResource("texture") == null)
                        entry.setResource("texture", new Texture(entryBuffer));
                    ImageIcon icon = entry.<Texture>getResource("texture").getImageIcon(320, 320);
                    if (icon != null) toolkit.setImage(icon);
                    else System.out.println("Failed to set icon, it's null?");
                    break;
                case "mol":
                    if (entry.getResource("mesh") == null) {
                        String fileName = Paths.get(entry.path).getFileName().toString();
                        entry.setResource("mesh", new Mesh(fileName.replaceFirst("[.][^.]+$", ""), entryBuffer));   
                    }
                    System.out.println("Failed to set Mesh preview, does functionality even exist?");
                    break;
                case "anim": {
                    res.decompress(true);
                    if (entry.getResource("animation") == null)
                        entry.setResource("animation", new Animation(res));
                    break;
                }
                case "gmat":
                    if (entry.getResource("gfxMaterial") == null) {
                        res.decompress(true);
                        entry.setResource("gfxMaterial", new GfxMaterial(res));
                    }
                    break;
                case "plan":
                    if (selected.entry.getResource("item") == null) {
                        try {
                            res.decompress(true);
                            selected.entry.setResource("item", new Serializer(res, Globals.LAMS).DeserializeItem());
                        } catch (Exception e) {
                            System.err.println("There was an error parsing the InventoryItem!");
                            return;
                        }
                    }
                    if (Globals.lastSelected.entry == entry) {
                        InventoryItem item = entry.getResource("item");
                        if (item != null && tree == currentTree) {
                            if (item.metadata != null)
                                toolkit.populateMetadata(item);
                            else {
                                System.out.println("Attempting to guess icon of RPlan, this may not be accurate.");
                                try {
                                    for (FileEntry e: entry.dependencies) {
                                        if (e.path.contains(".tex")) {
                                            ResourcePtr ptr = new ResourcePtr();
                                            ptr.hash = e.SHA1;
                                            toolkit.loadImage(ptr, item);
                                            return;
                                        }
                                    }
                                } catch (Exception e) {
                                    System.err.println("An error occured procesing texture.");
                                }
                                System.out.println("Could not find any texture file to display as icon.");
                            }
                        }
                    }
                    break;
            }
       });
    }
}
