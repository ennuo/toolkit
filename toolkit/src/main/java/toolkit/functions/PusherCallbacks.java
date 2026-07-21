package toolkit.functions;

import cwlib.enums.CompressionFlags;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.io.Resource;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.resources.RMesh;
import cwlib.resources.RPlan;
import cwlib.resources.RTexture;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.mesh.Bone;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PGeneratedMesh;
import cwlib.structs.things.parts.PGroup;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.structs.things.parts.PShape;
import cwlib.types.SerializedResource;
import cwlib.types.data.*;
import cwlib.types.databases.FileEntry;
import cwlib.types.swing.FileNode;
import cwlib.util.FileIO;
import cwlib.util.Resources;
import cwlib.util.Strings;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import configurations.Config;
import toolkit.windows.RemoteIconRendererGUI;

public class PusherCallbacks 
{
    /**
     * The port of the pusher server on the PS3.
     */
    public static final int PUSHER_PORT = 6504;

    public static class PusherMessageType
    {
        /**
         * Creates/removes/edits a thing in the world.
         */
        public static final int THING_DATA = 0x2;

        /**
         * Sets the current camera matrix.
         */
        public static final int SET_CAMERA_MATRIX = 0x3;

        /**
         * Sets the game to quit, reboots on debug builds.
         */
        public static final int SET_WANT_QUIT = 0x4;

        /**
         * Pushes a new set of color correction presets to PS3.
         */
        public static final int SET_COLOR_CORRECTION_PRESETS = 0x7;

        /**
         * Pushes a level to the PS3.
         */
        public static final int PUSH_LEVEL = 0x8;

        /**
         * Gets the current level data from the PS3.
         */
        public static final int GET_CURRENT_LEVEL_DATA = 0x9;

        /**
         * Renders a plan to an icon and returns the result.
         */
        public static final int GET_ITEM_ICON = 0xa;

        /**
         * Gets the resource data associated with a SHA1.
         */
        public static final int GET_RESOURCE_DATA = 0xb;

        public static final byte RELOAD_RESOURCE = (byte)0x80;
        public static final byte PUSH_RESOURCE = (byte)0x81;
        public static final byte SET_PUSHER_RESOLUTION = (byte)0x82;
    }

    public static void getItemRender(ActionEvent event)
    {
        RemoteIconRendererGUI.GetRenderer();
    }
}
