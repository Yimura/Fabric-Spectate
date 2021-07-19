package sh.damon.spectate.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
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
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity serverPlayerEntity = source.getPlayer();

        Spectate sp = Spectate.getInstance();
        SpectatingPlayer player = sp.playerManager.getPlayer(serverPlayerEntity);

        if (player.isSpectating()) {
            serverPlayerEntity.changeGameMode(GameMode.SURVIVAL);

            final SpectatingPlayer.SavedPosition savedPosition = player.getSavedPosition();
            if (savedPosition != null) {
                Vec3d position = savedPosition.getPosition();

                ChunkPos chunkPos = new ChunkPos(new BlockPos(position.x, position.y, position.z));
                source.getWorld().getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, serverPlayerEntity.getId());
                serverPlayerEntity.stopRiding();

                if (source.getWorld() == savedPosition.getWorld())
                    serverPlayerEntity.networkHandler.requestTeleport(position.x, position.y, position.z, savedPosition.getYaw(), savedPosition.getPitch());
                else
                    serverPlayerEntity.teleport(savedPosition.getWorld(), position.x, position.y, position.z, savedPosition.getYaw(), savedPosition.getPitch());
            }
        }
        else {
            serverPlayerEntity.changeGameMode(GameMode.SPECTATOR);

            player.savePosition();
        }

        player.setSpectating(!player.isSpectating());

        return 0;
    }
}
