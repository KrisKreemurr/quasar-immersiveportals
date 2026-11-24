package qouteall.imm_ptl.core.compat;

import com.mojang.blaze3d.pipeline.RenderTarget;

/**
 * Quasar MC: Porting Lib is not in the pack and the stencil-based portal renderer is parked, so this is a no-op
 * with the original API (original in git history / src/parked).
 */
public class IPPortingLibCompat {
    public static boolean isPortingLibPresent = false;

    public static void init() {}

    public static boolean getIsStencilEnabled(RenderTarget renderTarget) {
        return false;
    }

    public static void setIsStencilEnabled(RenderTarget renderTarget, boolean cond) {}
}
