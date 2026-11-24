// QUASAR STUB: parked peripheral feature (dimension stack / alternate dimensions / portal wand / command stick), see src/parked.
package qouteall.imm_ptl.peripheral.wand;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import qouteall.q_misc_util.my_util.Circle;
import qouteall.q_misc_util.my_util.Plane;
import qouteall.q_misc_util.my_util.WithDim;
import java.util.Objects;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class ProtoPortal {

    private static Logger LOGGER;
    public enum Stage {

        PlacingFirstSideLeftBottom, PlacingFirstSideRightBottom, PlacingFirstSideLeftTop, PlacingSecondSideLeftBottom, PlacingSecondSideRightBottom, PlacingSecondSideLeftTop, Completed;
    }

    public ProtoPortalSide firstSide;
    public ProtoPortalSide secondSide;
    public ProtoPortal() {}

    public ProtoPortal copy() { return null; }

    public void reset() {}

    public boolean isComplete() { return false; }

    public boolean isValidPlacement() { return false; }

    @Nullable
    public ResourceKey<Level> getCursorConstraintDim() { return null; }

    @Nullable
    public WithDim<Plane> getCursorConstraintPlane() { return null; }

    @Nullable
    public WithDim<Circle> getCursorConstraintCircle() { return null; }

    public boolean tryPlaceCursor(ResourceKey<Level> dimension, Vec3 pos) { return false; }

    public void undo() {}

    @Nullable
    public MutableComponent getPromptMessage(
        @Nullable ProtoPortal pendingState
    ) { return null; }

    public Stage getStage() { return null; }

}

