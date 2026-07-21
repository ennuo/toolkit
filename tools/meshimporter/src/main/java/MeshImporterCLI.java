import cwlib.enums.Branch;
import cwlib.enums.CompressionFlags;
import cwlib.enums.CostumePieceCategory;
import cwlib.enums.InventoryObjectType;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SkeletonType;
import cwlib.io.imports.ModelImporter;
import cwlib.resources.RAnimation;
import cwlib.resources.RMesh;
import cwlib.resources.RPlan;
import cwlib.singleton.ResourceSystem;
import cwlib.singleton.ResourceSystem.ResourceLogLevel;
import cwlib.structs.inventory.CreationHistory;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.inventory.UserCreatedDetails;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.CostumePiece;
import cwlib.structs.things.components.Decoration;
import cwlib.structs.things.parts.PCostume;
import cwlib.structs.things.parts.PDecorations;
import cwlib.structs.things.parts.PGroup;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.types.SerializedResource;
import cwlib.types.data.NetworkPlayerID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.util.FileIO;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MeshImporterCLI 
{
    public static void main(String[] args) 
    {
        ResourceSystem.LOG_LEVEL = ResourceLogLevel.NONE;
        
        if (args.length == 0) 
        {
            System.out.println("Mesh Importer CLI\n");

            System.out.println("Usage:");
            System.out.println("java -jar meshimporter.java -i <input> <...options> -o <output>\n");
            
            System.out.println("Arguments:");
            System.out.println("\t--input, -i <path>\n\tSpecifies the glTF 2.0 Binary (.glb) source file to import\n");
            System.out.println("\t--output, -o <path>\n\tPath to write built model file to\n");
            System.out.println("\t--map, -m <...overrides>\n\tSpecifies material override to existing game resources, pairs are separated by commas in the format of material_name=resource\n\tFor example, a single override would be \"Material.001=g916\",\n\twhile multiple overrides would be \"Material.001=g916,Material.002=g916\"\n");
            System.out.println("\t--skeleton, -s <skeleton>\n\tSpecifies the skeleton to use for a costume model\n\tSupported values:\n\tsackboy, oddsock, swoop, small_toggle, big_toggle\n");
            System.out.println("\t--categories, -c <...categories>\n\tSpecifies the categories used by a costume, list is separated by commas\n\tSupported values:\n\tbeard, feet, eyes, glasses, mouth, moustache, nose\n\thair, head, neck, torso, legs, hands, waist\n");
            System.out.println("\t--regions, -r <..regions>\n\tSpecifies the regions to hide when equipping a costume piece, list is separated by commas\n\tBuilt-in regions for Sackboy:\n\t_scalp, _brow, _torso, _torso1, _pants, _shorts, _socks, _zip, _zippull\n\t_armr, _arml, _sleever, _sleevel, _legs, _glover, _glovel, _eyes\n");
            System.out.println("\t--plan, -p <path>\n\tSpecifies the path to build a costume piece plan file to\n");


            return;
        }

        ModelImporter.ModelImportConfig config = new ModelImporter.ModelImportConfig();
        config.skeleton = null;

        File inputFilePath = null, outputFilePath = null, planFilePath = null;
        CostumePieceCategory primaryCostumePieceCategory = null;

        for (int i = 0; i < args.length; ++i)
        {
            if (args[i].startsWith("-"))
            {
                switch (args[i].toLowerCase())
                {
                    case "-o": case "--output":
                    {
                        if (outputFilePath != null)
                        {
                            System.out.println("Output file was already specified!");
                            return;
                        }

                        outputFilePath = new File(args[++i]);
                        break;
                    }
                    case "-i": case "--input":
                    {
                        if (inputFilePath != null)
                        {
                            System.out.println("Input file was already specified!");
                            return;
                        }

                        inputFilePath = new File(args[++i]);
                        if (!inputFilePath.exists())
                        {
                            System.out.println("No file exists at: " + inputFilePath.getAbsolutePath());
                            return;
                        }

                        config.glbSourcePath = inputFilePath.getAbsolutePath();

                        break;
                    }
                    case "-m": case "--map":
                    {
                        String[] overrides = args[++i].split(",");
                        for (String override : overrides) 
                        {
                            String[] segments = override.split("=");
                            if (segments.length != 2)
                            {
                                System.out.println("Material overrides contains invalid key/value pair!");
                                return;
                            }

                            config.materialOverrides.put(segments[0], new ResourceDescriptor(segments[1], ResourceType.GFX_MATERIAL));
                        }

                        break;
                    }
                    case "-s": case "--skeleton":
                    {
                        String name = args[++i].toLowerCase();
                        switch (name)
                        {
                            case "quad": case "oddsock":
                                config.skeleton = SkeletonType.QUAD;
                                break;
                            case "sack": case "sackboy":
                                config.skeleton = SkeletonType.SACKBOY;
                                break;
                            case "bird": case "swoop":
                                config.skeleton = SkeletonType.BIRD;
                                break;
                            case "dwarf": case "small_toggle": case "smalltoggle":
                                config.skeleton = SkeletonType.DWARF;
                                break;
                            case "giant": case "big_toggle": case "bigtoggle":
                                config.skeleton = SkeletonType.GIANT;
                                break;
                            default:
                                System.out.println("Invalid skeleton type specified!");
                                break;
                        }

                        break;
                    }
                    case "-c": case "--categories":
                    {
                        String[] categories = args[++i].split(",");
                        for (String category : categories)
                        {
                            try
                            {
                                CostumePieceCategory piece = CostumePieceCategory.valueOf(category.toUpperCase());
                                if (primaryCostumePieceCategory == null)
                                    primaryCostumePieceCategory = piece;
                                config.categories.add(piece);
                            }
                            catch (IllegalArgumentException ex)
                            {
                                System.out.println(category + " is not a valid costume piece category!");
                                return;
                            }
                        }

                        break;
                    }
                    case "-r": case "--regions":
                    {
                        String[] regions = args[++i].split(",");
                        for (String s : regions)
                        {
                            try
                            {
                                int region = Integer.parseInt(s);
                                config.regionsIDsToHide.add(region);
                            }
                            catch (NumberFormatException ex)
                            {
                                config.regionsIDsToHide.add(RAnimation.calculateAnimationHash(s));
                            }
                        }

                        break;
                    }
                    case "-p": case "--plan":
                    {
                        planFilePath = new File(args[++i]);
                        break;
                    }
                    default:
                    {
                        System.out.println("Unknown command switch: " + args[i]);
                        return;
                    }
                }
            }
        }
        
        boolean isCostume = config.skeleton != null;

        if (inputFilePath == null)
        {
            System.out.println("No input file was specified!");
            return;
        }

        if (outputFilePath == null)
        {
            System.out.println("No output file was specified!");
            return;
        }

        if ((!config.categories.isEmpty() || !config.regionsIDsToHide.isEmpty()) && !isCostume)
        {
            System.out.println("No character skeleton type was specified, can't use regions/categories!");
            return;
        }

        if (isCostume && config.categories.isEmpty())
        {
            System.out.println("Costume model must contain at least one category!");
            return;
        }

        ModelImporter importer = null;
        try 
        {
            importer = new ModelImporter(config);
        } 
        catch (Exception ex) 
        {
            System.err.println("An error occurred while importing the model!");
            System.err.println(ex.getMessage());
            return;
        }

        RMesh mesh = importer.getMesh();
        byte[] resourceData = SerializedResource.compress(mesh.build(new Revision(0x132), CompressionFlags.USE_NO_COMPRESSION));
        FileIO.write(resourceData, outputFilePath.getAbsolutePath());

        if (planFilePath != null)
        {
            ResourceDescriptor descriptor = new ResourceDescriptor(SHA1.fromBuffer(resourceData), ResourceType.MESH);

            // Setup the inventory item details based on the import config
            InventoryItemDetails details = new InventoryItemDetails();
            details.creationHistory = new CreationHistory("MM_Studio");
            details.creator = new NetworkPlayerID("MM_Studio");
            details.userCreatedDetails = new UserCreatedDetails(inputFilePath.getName(), "");

            Thing root;
            ArrayList<Thing> things = new ArrayList<>();
            if (isCostume)
            {
                // Meshes by default are in Maya's coordinate system, this just rotates it into LBP's coordinate system, which is Y-Up
                Matrix4f transform = new Matrix4f().identity().rotate((float) Math.toRadians(-90.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Matrix4f());

                // Build the costume piece bone hierarchy
                int thingUIDCounter = 0;
                Bone[] bones = mesh.getBones();
                for (Bone bone : bones)
                {
                    Thing thing = new Thing(++thingUIDCounter);
                    Matrix4f wpos = transform.mul(bone.skinPoseMatrix, new Matrix4f());
                    thing.setPart(Part.POS, new PPos(null, bone.animHash, wpos));
                    things.add(thing);
                }

                // Fixup the parenting / positions
                root = things.get(0);
                for (int i = 0; i < bones.length; ++i)
                {
                    Bone bone = bones[i];
                    Thing thing = things.get(i);
                    PPos pos = thing.getPart(Part.POS);
                    pos.thingOfWhichIAmABone = root;

                    if (i != 0)
                        thing.groupHead = root;
                    
                    if (bone.parent != -1)
                    {
                        thing.parent = things.get(bone.parent);
                        pos.recomputeLocalPos(thing);
                    }
                }

                root.setPart(Part.GROUP, new PGroup());
                root.setPart(Part.RENDER_MESH, new PRenderMesh(descriptor, things.toArray(Thing[]::new)));

                PCostume costume = new PCostume();
                costume.mesh = descriptor;
                costume.primitives = mesh.getPrimitives().toArray(Primitive[]::new);
                
                CostumePiece costumePiece = costume.costumePieces[primaryCostumePieceCategory.getIndex()];
                costumePiece.mesh = descriptor;
                costumePiece.categoriesUsed = mesh.getCostumeCategoriesUsed();
                costumePiece.primitives = costume.primitives;

                root.setPart(Part.COSTUME, costume);

                details.type = EnumSet.of(InventoryObjectType.COSTUME);
                details.subType = mesh.getCostumeCategoriesUsed();
            }
            else
            {
                root = new Thing(1);
                root.setPart(Part.DECORATIONS, new PDecorations(new Decoration(descriptor)));
                details.type = EnumSet.of(InventoryObjectType.DECORATION);
                things.add(root);
            }

            // Build the actual plan itself, use LAMS keys as the earliest revision,
            // just because tags are annoying, and Toolkit locks you to only editing tags if you don't change the revision.
            RPlan plan = new RPlan(new Revision(
                Branch.LEERDAMMER.getHead(), Branch.LEERDAMMER.getID(), Revisions.LD_LAMS_KEYS), 
                CompressionFlags.USE_ALL_COMPRESSION, 
                things.toArray(Thing[]::new), 
                details);
            
            byte[] planData = SerializedResource.compress(plan.build());
            FileIO.write(planData, planFilePath.getAbsolutePath());
        }

    }
}
