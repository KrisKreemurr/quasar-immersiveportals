package qouteall.imm_ptl.core.render.quasar;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Reused native buffers for the portal renderer's per-draw uniform uploads (render thread only).
 *
 * <p>Allocating a direct ByteBuffer per draw is slow and is freed only by the garbage collector, which can
 * cause hitches. {@code transientMemory().uploadGpu} copies the bytes straight into mapped memory, so a buffer
 * can be refilled right after each upload. Rule: fill, upload, then the buffer may be handed out again.</p>
 */
final class Scratch {
    private static final ByteBuffer SMALL = ByteBuffer.allocateDirect(256).order(ByteOrder.nativeOrder());
    private static ByteBuffer large = ByteBuffer.allocateDirect(64 * 1024).order(ByteOrder.nativeOrder());

    private Scratch() {}

    /** A zeroed buffer of exactly {@code bytes} (at most 256) remaining. */
    static ByteBuffer small(int bytes) {
        ByteBuffer b = SMALL;
        b.clear();
        for (int i = 0; i < 256; i += 8) b.putLong(i, 0L);
        b.limit(bytes);
        return b;
    }

    /** A buffer of exactly {@code bytes} remaining (contents undefined; write every byte you upload that matters). */
    static ByteBuffer large(int bytes) {
        if (large.capacity() < bytes) {
            int cap = large.capacity();
            while (cap < bytes) cap *= 2;
            large = ByteBuffer.allocateDirect(cap).order(ByteOrder.nativeOrder());
        }
        large.clear();
        large.limit(bytes);
        return large;
    }
}
