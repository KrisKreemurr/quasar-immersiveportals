// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render;

import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.Validate;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;
import java.util.ArrayList;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class GlQueryObject {

    private int idQueryObject;
    private boolean isQuerying;
    private boolean hasResult;
    public GlQueryObject(int handle) {}

    public void performQueryAnySamplePassed(Runnable renderingFunc) {}

    public void performQuerySampleNumPassed(Runnable renderingFunc) {}

    private void performQuery(Runnable renderingFunc, int glQueryType) {}

    public boolean fetchQueryResult() { return false; }

    private void dispose() {}

    public boolean isValid() { return false; }

    private void reset() {}

    private static ArrayList<GlQueryObject> queryObjects;
    private static void prepareQueryObjects() {}

    public static GlQueryObject acquireQueryObject() { return null; }

    public static void returnQueryObject(GlQueryObject obj) {}

    protected GlQueryObject() {}

}

