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
    // Quasar MC: on again. It was off because ticking a background world moved the main camera to the far side
    // of a nearby portal every tick (for its particles), which made the player's own animation stutter; that hack
    // is gone (background worlds have their own particle engine, see PortalParticles). Without ticking, background
    // entities never advance their previous position (they jumped back and forth in portal views), the world's
    // queued light packets are never applied, and its particles never spawn or age.
    // 2026-11-26: back off: with it on, the player's own animations were choppy again (cause still unknown) and
    // background mobs still stood still (so their movement problem is elsewhere, likely server side).
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
