package ennuo.toolkit.utilities;

import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.Animation;
import ennuo.craftworld.resources.GfxMaterial;
import ennuo.craftworld.resources.Mesh;
import ennuo.craftworld.resources.Pack;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.things.Serializer;
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
            entryBuffer = Globals.extractFile(entry.hash);
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
            if (entry.dependencyModel == null || entry.dependencies == null || entry.missingDependencies) {
                FileModel model = new FileModel(new FileNode("x", null, null));
                Resource resource = new Resource(entryBuffer);
                boolean recursive = !(resource.magic.equals("PCKb") || resource.magic.equals("SLTb") || resource.magic.equals("LVLb") || resource.magic.equals("ADCb") || resource.magic.equals("PALb"));
                entry.missingDependencies = resource.getDependencies(entry, recursive) != 0;
                entry.dependencies = resource.dependencies;
                toolkit.generateDependencyTree(entry, model);
                entry.dependencyModel = model;
            }

            if (Globals.lastSelected == selected && entry.dependencyModel != null && tree == currentTree)
                toolkit.dependencyTree.setModel(entry.dependencyModel);
            String path = entry.path.toLowerCase();

            String ext = path.substring(path.lastIndexOf(".") + 1);

            toolkit.preview.setDividerLocation(325);
            switch (ext) {
                case "pck":
                    if (entry.pack == null) {
                        Resource res = new Resource(entryBuffer);
                        res.decompress(true);
                        entry.revision = res.revision;
                        try {
                            Pack pack = new Pack(res);
                            entry.pack = pack;
                        } catch (Exception e) {
                            System.err.println("There was an error processing the RPack file! -> ");
                            System.err.println(e);
                        }
                    }
                    break;
                case "slt":
                    if (entry.slots == null) {
                        Resource res = new Resource(entryBuffer);
                        if (res.magic.equals("SLTt")) return;
                        res.decompress(true);
                        entry.revision = res.revision;

                        int count = res.int32();
                        entry.slots = new ArrayList < Slot > (count);
                        for (int i = 0; i < count; ++i) {
                            Slot slot = new Slot(res, true, false);
                            entry.slots.add(slot);
                            if (slot.root != null) {
                                FileEntry e = Globals.findEntry(slot.root);
                                e.revision = res.revision;
                                if (e != null)
                                    e.slot = slot;
                            }
                        }
                    }
                    break;
                case "bin":
                    if (entry.slot != null) {
                        if (entry.slot.renderedIcon == null)
                            entry.slot.renderIcon(entry);
                        toolkit.setImage(entry.slot.renderedIcon);
                    }
                    break;
                case "tex":
                case "gtf":
                case "dds":
                case "jpg":
                case "jpeg":
                case "png":
                case "jfif":
                    if (entry.texture == null)
                        entry.texture = new Texture(entryBuffer);
                    ImageIcon icon = entry.texture.getImageIcon(320, 320);
                    if (icon != null) toolkit.setImage(icon);
                    else System.out.println("Failed to set icon, it's null?");
                    break;
                case "mol":
                    if (entry.mesh == null) {
                        String fileName = Paths.get(entry.path).getFileName().toString();
                        entry.mesh = new Mesh(fileName.replaceFirst("[.][^.]+$", ""), entryBuffer);   
                    }
                    System.out.println("Failed to set Mesh preview, does functionality even exist?");
                    break;
                case "anim": {
                    Resource resource = new Resource(entryBuffer);
                    resource.decompress(true);
                    new Animation(resource);
                    break;
                }
                case "gmat":
                    if (entry.gfxMaterial == null) {
                        Resource resource = new Resource(entryBuffer);
                        resource.decompress(true);
                        entry.gfxMaterial = new GfxMaterial(resource);
                    }
                    break;
                case "plan":
                    if (selected.entry.item == null) {
                        try {
                            Resource resource = new Resource(entryBuffer);
                            resource.decompress(true);
                            selected.entry.item = new Serializer(resource, Globals.LAMS).DeserializeItem();
                        } catch (Exception e) {
                            System.err.println("There was an error parsing the InventoryItem!");
                            return;
                        }
                    }
                    if (Globals.lastSelected.entry == entry) {
                        if (entry.item != null && tree == currentTree) {
                            if (entry.item.metadata != null)
                                toolkit.populateMetadata(entry.item);
                            else {
                                System.out.println("Attempting to guess icon of RPlan, this may not be accurate.");
                                try {
                                    for (FileEntry e: entry.dependencies) {
                                        if (e.path.contains(".tex")) {
                                            ResourcePtr ptr = new ResourcePtr();
                                            ptr.hash = e.hash;
                                            toolkit.loadImage(ptr, entry.item);
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
            toolkit.preview.setDividerLocation(325);
        });
    }
}
