package qouteall.imm_ptl.core;

import qouteall.imm_ptl.core.render.renderer.PortalRenderer;
import qouteall.imm_ptl.core.render.renderer.RendererDebug;
import qouteall.imm_ptl.core.render.renderer.RendererDummy;
import qouteall.imm_ptl.core.render.renderer.RendererUsingFrameBuffer;
import qouteall.imm_ptl.core.render.renderer.RendererUsingStencil;

//@OnlyIn(Dist.CLIENT)
public class IPCGlobal {
    
    public static PortalRenderer renderer;
    public static RendererUsingStencil rendererUsingStencil;
    public static RendererUsingFrameBuffer rendererUsingFrameBuffer;
    public static RendererDummy rendererDummy = new RendererDummy();
    public static RendererDebug rendererDebug = new RendererDebug();
    
    public static int maxIdleChunkRendererNum = 500;
    
    public static boolean doUseAdvancedFrustumCulling = true;
    public static boolean useHackedChunkRenderDispatcher = true;
    // Quasar MC: off until portal views draw other dimensions (cross-dimension step 3). Ticking a background
    // world moves the main camera to the far side of a nearby portal every tick (for its particles), which
    // made the player's own animation stutter, and nothing shows the background world yet.
    public static boolean isClientRemoteTickingEnabled = false;
    public static boolean useFrontClipping = true;
    public static boolean doDisableAlphaTestWhenRenderingFrameBuffer = true;
    public static boolean lateClientLightUpdate = true;
    public static boolean earlyRemoteUpload = true;
    
    public static boolean useSuperAdvancedFrustumCulling = true;
    public static boolean earlyFrustumCullingPortal = true;
    
    public static boolean useSeparatedStencilFormat = false;
    
    public static boolean experimentalIrisPortalRenderer = false;
    
    public static boolean debugEnableStencilWithIris = false;
}
