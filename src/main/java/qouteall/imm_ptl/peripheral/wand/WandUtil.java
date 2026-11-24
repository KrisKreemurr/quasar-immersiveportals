// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.wand;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.mc_utils.WireRenderingHelper;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.my_util.IntBox;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class WandUtil {

    @Nullable
    
    public static Portal getClientPortalByUUID(UUID portalId) { return null; }

    @Nullable
    public static Portal getPortalByUUID(Level world, UUID portalId) { return null; }

    public static Vec3 alignOnBlocks(
        Level world, Vec3 vec3, int gridCount
    ) { return null; }

    public static void renderPortalAreaGridNew(
        VertexConsumer vertexConsumer, Vec3 cameraPos,
        ProtoPortalSide protoPortalSide,
        int color1, int color2, PoseStack matrixStack
    ) {}

    public static void renderPortalAreaGrid(
        VertexConsumer vertexConsumer, Vec3 cameraPos,
        ProtoPortalSide protoPortalSide,
        int color, PoseStack matrixStack
    ) {}

}

