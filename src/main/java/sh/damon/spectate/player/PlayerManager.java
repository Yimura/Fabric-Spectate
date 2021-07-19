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

        if (this.players.containsKey(id))
            return this.players.get(id);

        final SpectatingPlayer player = new SpectatingPlayer(serverPlayerEntity);
        this.players.put(id, player);

        return player;
    }
}
