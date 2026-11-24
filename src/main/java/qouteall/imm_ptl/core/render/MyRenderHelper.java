// QUASAR STUB: real code parked in src/parked (rendering is replaced for Quasar MC).
package qouteall.imm_ptl.core.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.joml.Matrix4f;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.miscellaneous.IPVanillaCopy;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;
import qouteall.q_misc_util.my_util.SignalBiArged;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glReadPixels;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class MyRenderHelper {

    public static Minecraft client;
    public static void init() {}

    private static ResourceProvider getResourceFactory(ResourceProvider resourceManager) { return null; }

    public static class DrawFbInAreaShader {

        public DrawFbInAreaShader(
        ) throws IOException { super(factory, name, format); }

        void loadWidthHeight(int w, int h) {}

        protected DrawFbInAreaShader() {}

    }

    public static DrawFbInAreaShader drawFbInAreaShader;
    public static void drawPortalAreaWithFramebuffer(
        Portal portal,
        RenderTarget textureProvider,
        Matrix4f modelViewMatrix,
        Matrix4f projectionMatrix
    ) {}

    public static void renderScreenTriangle() {}

    public static void renderScreenTriangle(Vec3 color) {}

    public static void testOneTriangle(int r, int g, int b, int a) {}

    @IPVanillaCopy
    public static void renderScreenTriangle(int r, int g, int b, int a) {}

    public static void drawScreenFrameBuffer(
        RenderTarget textureProvider,
        boolean doUseAlphaBlend,
        boolean doEnableModifyAlpha
    ) {}

    public static void drawFramebuffer(
            RenderTarget textureProvider, boolean doUseAlphaBlend, boolean doEnableModifyAlpha,
            float xMin, float xMax, float yMin, float yMax
    ) {}

    public static void drawFramebufferWithViewport(
            RenderTarget textureProvider, boolean doUseAlphaBlend, boolean doEnableModifyAlpha,
            float left, float right, float bottom, float up,
            int viewportWidth, int viewportHeight
    ) {}

    public static void drawFramebufferWithBounds(
        RenderTarget textureProvider, boolean doUseAlphaBlend, boolean doEnableModifyAlpha,
        int xMin, int xMax, int yMin, int yMax
    ) {}

    @IPVanillaCopy
    public static void drawFramebufferWithCoordinatesAndDimensions(
        RenderTarget textureProvider, boolean doUseAlphaBlend, boolean doEnableModifyAlpha,
        int x, int y, int viewportWidth, int viewportHeight
    ) {}

    public static void lateUpdateLight() {}

    public static void earlyRemoteUpload() {}

    public static void applyMirrorFaceCulling() {}

    public static void recoverFaceCulling() {}

    public static void clearAlphaTo1(RenderTarget mcFrameBuffer) {}

    public static void restoreViewPort() {}

    public static float transformFogDistance(float value) { return 0F; }

    private static boolean debugEnabled;
    public static void debugFramebufferDepth() {}

    public static void debugFramebufferColorRed() {}

}

