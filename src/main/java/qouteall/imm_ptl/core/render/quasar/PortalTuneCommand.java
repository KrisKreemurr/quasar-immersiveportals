package qouteall.imm_ptl.core.render.quasar;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import qouteall.imm_ptl.core.CHelper;

/**
 * Quasar MC: client command to tune portal views live (values reset on restart; defaults move to config later).
 * <pre>
 * /quasarportals                  show current values
 * /quasarportals update 1..8      redraw at most every Nth frame (adaptive, never under 60/s)
 * /quasarportals res 0.1..1.0     portal view resolution relative to the screen
 * /quasarportals distance 0..64   portal render distance in chunks (0 = unlimited)
 * /quasarportals live 0..8        how many portals get a live view
 * </pre>
 */
public final class PortalTuneCommand {
    private PortalTuneCommand() {}
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("quasarportals")
            .executes(c -> show())
            .then(Commands.literal("update").then(Commands.argument("frames", IntegerArgumentType.integer(1, 8))
                .executes(c -> {
                    PortalViewRenderer.maxUpdateEvery = IntegerArgumentType.getInteger(c, "frames");
                    return show();
                })))
            .then(Commands.literal("res").then(Commands.argument("scale", FloatArgumentType.floatArg(0.1f, 1.0f))
                .executes(c -> {
                    PortalViewRenderer.resolutionScale = FloatArgumentType.getFloat(c, "scale");
                    return show();
                })))
            .then(Commands.literal("distance").then(Commands.argument("chunks", IntegerArgumentType.integer(0, 64))
                .executes(c -> {
                    PortalViewRenderer.renderDistance = IntegerArgumentType.getInteger(c, "chunks");
                    return show();
                })))
            .then(Commands.literal("live").then(Commands.argument("portals", IntegerArgumentType.integer(0, 8))
                .executes(c -> {
                    PortalViewRenderer.liveLimit = IntegerArgumentType.getInteger(c, "portals");
                    return show();
                }))));
    }
    
    private static int show() {
        CHelper.printChat(Component.literal(String.format(java.util.Locale.ROOT,
            "[Quasar Portals] update: at most every %d frames (60/s floor), res: %.2f, distance: %s, live: %d",
            PortalViewRenderer.maxUpdateEvery, PortalViewRenderer.resolutionScale,
            PortalViewRenderer.renderDistance == 0 ? "unlimited" : PortalViewRenderer.renderDistance + " chunks",
            PortalViewRenderer.liveLimit)));
        return 1;
    }
}
