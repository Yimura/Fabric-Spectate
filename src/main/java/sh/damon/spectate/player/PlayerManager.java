package sh.damon.spectate.player;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {
    private final HashMap<UUID, SpectatingPlayer> players;

    public PlayerManager() {
        this.players = new HashMap<>();
    }

    public SpectatingPlayer getPlayer(ServerPlayerEntity serverPlayerEntity) {
        final UUID id = serverPlayerEntity.getUuid();

        SpectatingPlayer player;

        if (this.players.containsKey(id)) {
            player = this.players.get(id);

            player.updatePlayerEntity(serverPlayerEntity);
        }
        else {
            player = new SpectatingPlayer(serverPlayerEntity);

            this.players.put(id, player);
        }

        return player;
    }
}
