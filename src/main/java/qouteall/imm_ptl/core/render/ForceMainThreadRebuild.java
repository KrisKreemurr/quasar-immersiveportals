// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render;

import com.mojang.logging.LogUtils;
import de.nick1st.imm_ptl.events.ClientCleanupEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

@OnlyIn(Dist.CLIENT)
public class ForceMainThreadRebuild {

    private static Logger LOGGER;
    private static int forceMainThreadRebuildForFrames;
    private static boolean currentFrameForceMainThreadRebuild;
    public static void init() {}

    private static void reset() {}

    public static void onPreRender() {}

    public static void forceMainThreadRebuildFor(int frameCount) {}

    public static boolean isCurrentFrameForceMainThreadRebuild() { return false; }

}

