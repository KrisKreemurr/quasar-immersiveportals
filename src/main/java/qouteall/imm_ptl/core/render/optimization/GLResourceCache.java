// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render.optimization;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import java.util.function.Consumer;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class GLResourceCache {

    public static Minecraft client;
    private Consumer<int[]> generator;
    private IntList bufferIds;
    public static GLResourceCache bufferCache;
    public static GLResourceCache vertexArrayCache;
    public GLResourceCache(Consumer<int[]> generator) {}

    public int getNewResourceId() { return 0; }

    public static void init() {}

    private void reserve(int num) {}

    private int getExpectedBufferSize() { return 0; }

    protected GLResourceCache() {}

}

