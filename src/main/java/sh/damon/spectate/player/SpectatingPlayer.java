package sh.damon.spectate.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.UUID;

public class SpectatingPlayer {
    private ServerPlayerEntity player;
    private boolean spectating = false;
    private SaveData saveData = null;

    public SpectatingPlayer(ServerPlayerEntity serverPlayerEntity) {
        this.player = serverPlayerEntity;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public UUID getId()
    {
        return this.player.getUuid();
    }

    public boolean isSpectating() {
        return this.spectating;
    }

    public void setSpectating(boolean newState) {
        this.spectating = newState;
    }

    public SaveData getSavedPosition() {
        return this.saveData;
    }

    public boolean restore(ServerWorld sourceWorld) {
        if (this.saveData != null) {
            Vec3d position = this.saveData.getPosition();

            ChunkPos chunkPos = new ChunkPos(new BlockPos(position.x, position.y, position.z));

            ServerWorld world = this.saveData.getWorld();

            world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, this.player.getId());
            this.player.stopRiding();

            if (sourceWorld == this.saveData.getWorld())
                this.player.networkHandler.requestTeleport(position.x, position.y, position.z, this.saveData.getYaw(), this.saveData.getPitch());
            else
                this.player.teleport(this.saveData.getWorld(), position.x, position.y, position.z, this.saveData.getYaw(), this.saveData.getPitch());

            this.player.changeGameMode(this.saveData.getGameMode());

            return true;
        }

        return false;
    }

    public void setSaveData(SaveData saveData) {
        this.saveData = saveData;
    }

    public void save() {
        this.saveData = new SaveData(this.player);

        this.player.changeGameMode(GameMode.SPECTATOR);
    }

    public void updatePlayerEntity(ServerPlayerEntity serverPlayerEntity) {
        this.player = serverPlayerEntity;
    }

    public static class SaveData {
        private final GameMode gameMode;
        private final ServerWorld world;
        private final Vec3d position;
        private final float pitch;
        private final float yaw;

        public SaveData(ServerPlayerEntity player) {
            this.gameMode = player.interactionManager.getGameMode();

            this.world = player.getServerWorld();

            this.position = player.getPos();
            this.pitch = player.getPitch();
            this.yaw = player.getYaw();
        }

        public SaveData(GameMode gameMode, ServerWorld world, Vec3d position, float yaw, float pitch) {
            this.gameMode = gameMode;

            this.world = world;

            this.position = position;
            this.pitch = pitch;
            this.yaw = yaw;
        }

        public Vec3d getPosition() {
            return position;
        }

        public float getPitch() {
            return pitch;
        }

        public float getYaw() {
            return yaw;
        }

        public ServerWorld getWorld() {
            return world;
        }

        public String toString() {
            return String.format("%s { x: %f, y: %f, z: %f } => { yaw: %f, pitch: %f } ", this.world.toString(), this.position.x, this.position.y, this.position.z, this.yaw, this.pitch);
        }

        public GameMode getGameMode() {
            return this.gameMode;
        }
    }
}
