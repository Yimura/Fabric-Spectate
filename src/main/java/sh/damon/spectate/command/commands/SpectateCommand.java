package sh.damon.spectate.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.GameMode;
import sh.damon.spectate.Spectate;
import sh.damon.spectate.command.BaseCommand;
import sh.damon.spectate.player.SpectatingPlayer;

import static net.minecraft.server.command.CommandManager.literal;

public class SpectateCommand implements BaseCommand {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("s").executes(this)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();
        final ServerPlayerEntity serverPlayerEntity = source.getPlayer();

        final Spectate sp = Spectate.getInstance();
        final SpectatingPlayer player = sp.playerManager.getPlayer(serverPlayerEntity);

        if (serverPlayerEntity.isSpectator()) {
            if (!player.restore(source.getWorld())) {
                player.save();

                source.sendFeedback(new LiteralText("[Spectate] No saved location, do /s again to switch on your current position."), false);
            }
            else source.sendFeedback(new LiteralText("[Spectate] Position successfully restored."), false);
        }
        else {
            player.save();

            source.sendFeedback(new LiteralText("[Spectate] To switch back use /s"), false);
        }

        return 0;
    }
}
