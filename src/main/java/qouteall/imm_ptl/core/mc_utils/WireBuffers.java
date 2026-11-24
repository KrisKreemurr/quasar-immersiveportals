package qouteall.imm_ptl.core.mc_utils;

import net.minecraft.world.phys.Vec3;

/** Quasar MC: replaces the {@code MultiBufferSource} the wand previews used to get their line buffers from. */
public final class WireBuffers {
    private final Vec3 cameraPos;
    
    public WireBuffers(Vec3 cameraPos) {
        this.cameraPos = cameraPos;
    }
    
    public WireSink lines() {
        return WireSink.lines(cameraPos);
    }
    
    public WireSink lineStrip() {
        return WireSink.lineStrip(cameraPos);
    }
}
