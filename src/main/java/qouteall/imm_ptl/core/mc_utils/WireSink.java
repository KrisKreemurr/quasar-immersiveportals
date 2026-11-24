package qouteall.imm_ptl.core.mc_utils;

import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Quasar MC: stands in for the line {@code VertexConsumer}s the original Immersive Portals drew its wand previews into
 * ({@code RenderType.lines()} / {@code debugLineStrip}). It accepts the same {@code addVertex(matrix, x, y, z).setColor(c)
 * .setNormal(...)} calls (positions relative to the camera, as before) and turns every finished line - or line-strip
 * segment, where an alpha-0 vertex "jumps" without drawing - into a 26.x world gizmo line. Must be used while the
 * frame's gizmos are being collected (RenderFrameEvent.Pre).
 */
public final class WireSink {
    private final Vec3 cameraPos;
    private final boolean strip;
    private final float width;
    
    private final Vector3f pending = new Vector3f();
    private boolean hasPending;
    
    private final Vector3f prev = new Vector3f();
    private int prevColor;
    private boolean hasPrev;
    
    private WireSink(Vec3 cameraPos, boolean strip, float width) {
        this.cameraPos = cameraPos;
        this.strip = strip;
        this.width = width;
    }
    
    /** pairs of vertices are separate lines */
    public static WireSink lines(Vec3 cameraPos) {
        return new WireSink(cameraPos, false, 2.5f);
    }
    
    /** every vertex continues the line from the previous one; alpha-0 vertices break it */
    public static WireSink lineStrip(Vec3 cameraPos) {
        return new WireSink(cameraPos, true, 1.5f);
    }
    
    public WireSink addVertex(Matrix4f matrix, float x, float y, float z) {
        matrix.transformPosition(x, y, z, pending);
        hasPending = true;
        return this;
    }
    
    public WireSink addVertex(float x, float y, float z) {
        pending.set(x, y, z);
        hasPending = true;
        return this;
    }
    
    public WireSink setColor(int argb) {
        if (!hasPending) return this;
        hasPending = false;
        if (hasPrev) {
            int lineColor = strip ? (alpha(prevColor) == 0 || alpha(argb) == 0 ? 0 : argb) : prevColor;
            if (alpha(lineColor) != 0) {
                Gizmos.line(world(prev), world(pending), lineColor, width);
            }
        }
        if (strip || !hasPrev) {
            prev.set(pending);
            prevColor = argb;
            hasPrev = true;
        }
        else {
            hasPrev = false; // a line is two vertices
        }
        return this;
    }
    
    public WireSink setColor(float r, float g, float b, float a) {
        return setColor(((int) (a * 255) & 0xff) << 24 | ((int) (r * 255) & 0xff) << 16
            | ((int) (g * 255) & 0xff) << 8 | ((int) (b * 255) & 0xff));
    }
    
    public WireSink setNormal(float x, float y, float z) {
        return this;
    }
    
    public WireSink setLineWidth(float w) {
        return this;
    }
    
    private Vec3 world(Vector3f v) {
        return new Vec3(cameraPos.x + v.x, cameraPos.y + v.y, cameraPos.z + v.z);
    }
    
    private static int alpha(int argb) {
        return (argb >>> 24) & 0xff;
    }
}
