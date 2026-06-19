import java.io.File;
import java.util.HashMap;

import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.resources.RBigProfile;
import cwlib.resources.RLocalProfile;
import cwlib.resources.RPlan;
import cwlib.structs.profile.InventoryItem;
import cwlib.structs.slot.Slot;
import cwlib.types.SerializedResource;
import cwlib.types.archives.SaveArchive;
import cwlib.types.data.NetworkPlayerID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.SHA1;
import cwlib.util.Bytes;

public class GregBasedDynamics 
{
    static HashMap<SHA1, SHA1> LatestHashes = new HashMap<>();
    static SaveArchive LocalCache;
    static SaveArchive BigCache;
    static RLocalProfile LocalProfile;
    static RBigProfile BigProfile;

    public static SHA1 ReplaceAuthors(ResourceDescriptor item, NetworkPlayerID from, NetworkPlayerID to)
    {
        byte[] resourceData = BigCache.extract(item.getSHA1());
        if (resourceData == null) return item.getSHA1();
        SerializedResource resource = new SerializedResource(resourceData);

        // Replace all our dependencies first
        for (var descriptor : resource.getDependencies())
        {
            if (descriptor.isGUID() || descriptor.getType() != ResourceType.PLAN) continue;

            SHA1 sha1 = descriptor.getSHA1();
            if (!LatestHashes.containsKey(sha1))
            {
                sha1 = ReplaceAuthors(descriptor, from, to);
            }
            else
            {
                sha1 = LatestHashes.get(sha1);
            }

            resource.replaceDependency(descriptor, new ResourceDescriptor(sha1, ResourceType.PLAN));
        }

        RPlan plan = resource.loadResource(RPlan.class);
        plan.ReplaceAuthorsFast(from, to);


        resourceData = SerializedResource.compress(plan.build());
        SHA1 sha1 = BigCache.add(resourceData);
        LatestHashes.put(item.getSHA1(), sha1);
        return sha1;
    }

    public static void main(String[] args) 
    {
        if (args.length != 3 && args.length != 4)
        {
            System.out.println("Ingregnation");

            System.out.println("Usage:");
            System.out.println("java -jar ingregnation.jar <littlefart> <bigfart> <npid> (?ident)");

            return;
        }

        File littleFartFile = new File(args[0]);
        File bigFartFile = new File(args[1]);

        NetworkPlayerID to = new NetworkPlayerID(args[2]);
        if (args.length == 4)
            to.setOpt(args[3]);

        if (!littleFartFile.exists())
        {
            System.out.printf("%s does not exist!\n", littleFartFile.getAbsolutePath());
            return;
        }

        if (!bigFartFile.exists())
        {
            System.out.printf("%s does not exist!\n", bigFartFile.getAbsolutePath());
            return;
        }

        try
        {
            LocalCache = new SaveArchive(littleFartFile.getAbsolutePath());
        }
        catch (Exception ex)
        {
            System.out.printf("Failed to load littlefart cache file!\n");
            return;
        }

        try
        {
            BigCache = new SaveArchive(bigFartFile.getAbsolutePath());
        }
        catch (Exception ex)
        {
            System.out.printf("Failed to load bigfart cache file!\n");
            return;
        }

        if (LocalCache.getKey().getRootType() != ResourceType.LOCAL_PROFILE)
        {
            System.out.printf("littlefart cache file root resource is not of type RTYPE_LOCAL_PROFILE!\n");
            return;
        }

        if (BigCache.getKey().getRootType() != ResourceType.BIG_PROFILE)
        {
            System.out.printf("bigfart cache file root resource is not of type RTYPE_BIG_PROFILE!\n");
            return;
        }

        try
        {
            LocalProfile = LocalCache.loadResource(LocalCache.getKey().getRootHash(), RLocalProfile.class);
        }
        catch (Exception ex)
        {
            System.out.printf("Failed to parse RLocalProfile resource in local profile!\n");
            return;
        }


        try
        {
            BigProfile = BigCache.loadResource(BigCache.getKey().getRootHash(), RBigProfile.class);
        }
        catch (Exception ex)
        {
            System.out.printf("Failed to parse RBigProfile resource in big profile!\n");
            return;
        }

        NetworkPlayerID from = LocalProfile.playerId;

        var from_bytes = Bytes.GetHandleBytes(from);
        var to_bytes = Bytes.GetHandleBytes(to);

        for (InventoryItem item : BigProfile.inventory)
        {
            item.details.ReplaceAuthors(from, to);

            SHA1 sha1;
            if (item.plan == null || !item.plan.isHash()) continue;
            sha1 = item.plan.getSHA1();

            if (LatestHashes.containsKey(sha1))
            {
                item.plan = new ResourceDescriptor(LatestHashes.get(sha1), ResourceType.PLAN);
            }
            else
            {
                item.plan = new ResourceDescriptor(ReplaceAuthors(item.plan, from, to), ResourceType.PLAN);
            }
        }

        for (Slot slot : BigProfile.myMoonSlots.values())
        {
            slot.authorID = to.getHandle();

            SHA1 sha1;
            if (slot.root == null || !slot.root.isHash()) continue;
            sha1 = slot.root.getSHA1();

            byte[] resourceData = BigCache.extract(sha1);
            if (resourceData == null)
            {
                System.out.printf("could not find %s for %s (%s)???, skipping\n", sha1.toString(), slot.name, slot.id.toString());
                continue;
            }

            SerializedResource resource = new SerializedResource(resourceData);
            byte[] decompressedData = resource.getStream().getBuffer();

            var indices = cwlib.util.Matcher.indicesOf(decompressedData, from_bytes);
            if (indices.length != 0)
            {
                for (int index : indices)
                    System.arraycopy(to_bytes, 0, decompressedData, index, to_bytes.length);
                
                resourceData = resource.compress(decompressedData);
                sha1 = BigCache.add(resourceData);
                slot.root = new ResourceDescriptor(sha1, ResourceType.LEVEL);
            }
        }

        LocalProfile.copiedFromAnotherUser = false;
        LocalProfile.playerId = to;
        LocalProfile.ownerPlayerId = to;

        LocalCache.getKey().setRootHash(LocalCache.add(SerializedResource.compress(LocalProfile.build(LocalCache.getGameRevision(), CompressionFlags.USE_ALL_COMPRESSION))));
        BigCache.getKey().setRootHash(BigCache.add(SerializedResource.compress(BigProfile.build(BigCache.getGameRevision(), CompressionFlags.USE_ALL_COMPRESSION))));

        LocalCache.save();
        BigCache.save();
    }
}
