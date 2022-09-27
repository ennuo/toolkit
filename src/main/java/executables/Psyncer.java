package executables;

import java.io.File;
import java.nio.charset.StandardCharsets;

import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.resources.RLocalProfile;
import cwlib.resources.RSyncedProfile;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.types.archives.SaveArchive;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;

public class Psyncer {
    public static class PsyncState {
        public RSyncedProfile synced;
        public RLocalProfile local;
    }
    

    public static void main(String[] args) {
        ResourceSystem.DISABLE_LOGS = true;
        if (args.length < 2 || args.length > 3) {
            System.out.println("java -jar psyncer.jar <littlefart> <*.json> [-x]");
            return;
        }

        boolean extractJSON = false;
        if (args.length == 3 && args[2].toUpperCase().equals("-X"))
            extractJSON = true;

        if (!new File(args[0]).exists()) {
            System.err.println("Littlefart file doesn't exist!");
            return;
        }

        if (!extractJSON && !new File(args[1]).exists()) {
            System.err.println("JSON file doesn't exist!");
            return;
        }

        SaveArchive archive = null;
        try { archive = new SaveArchive(args[0]); } 
        catch (Exception ex) {
            System.err.println("An error occurred processing archive!");
            return;
        }

        if (archive.getKey().getRootType() != ResourceType.LOCAL_PROFILE) {
            System.err.println("Archive didn't contain RLocalProfile!");
            return;
        }

        RLocalProfile local = null;
        try { local = archive.loadResource(archive.getKey().getRootHash(), RLocalProfile.class); }
        catch (Exception ex) {
            System.err.println("An error occurred processing RLocalProfile!");
            return;
        }
        if (local == null) {
            System.err.println("Archive didn't contain RLocalProfile!");
            return;
        }

        RSyncedProfile synced = null;
        try { synced = archive.loadResource(local.syncedProfile.getSHA1(), RSyncedProfile.class); }
        catch (Exception ex) {
            System.err.println("An error occurred processing RSyncedProfile!");
            return;
        }

        if (synced == null) {
            System.err.println("Archive didn't contain RSyncedProfile!");
            return;
        }

        GsonUtils.REVISION = archive.getGameRevision();
        
        if (extractJSON) {
            FileIO.write(GsonUtils.toJSON(synced).getBytes(StandardCharsets.UTF_8), args[1]);
            return;
        }
        
        byte[] jsonData = FileIO.read(args[1]);
        if (jsonData == null) {
            System.err.println("Failed to read JSON file!");
            return;
        }

        try { 
            synced = GsonUtils.fromJSON(new String(jsonData, StandardCharsets.UTF_8), RSyncedProfile.class);
        }
        catch (Exception ex) {
            System.err.println("Failed to convert JSON to RSyncedProfile!");
            return;
        }

        local.syncedProfile = new ResourceDescriptor(
            archive.add(Resource.compress(synced, archive.getGameRevision(), CompressionFlags.USE_NO_COMPRESSION)),
            ResourceType.SYNCED_PROFILE
        );

        archive.getKey().setRootHash(archive.add(Resource.compress(local, archive.getGameRevision(), CompressionFlags.USE_NO_COMPRESSION)));

        archive.save();

        System.out.println("Saved!");
    }
}
