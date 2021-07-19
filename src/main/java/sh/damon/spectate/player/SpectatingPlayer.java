package sh.damon.spectate.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class SpectatingPlayer {
    private final ServerPlayerEntity player;
    private boolean spectating = false;
    private SavedPosition savedPosition = null;

    public SpectatingPlayer(ServerPlayerEntity serverPlayerEntity) {
        this.player = serverPlayerEntity;
    }

    public double getX() {
        return this.player.getX();
    }

    public double getY() {
        return this.player.getY();
    }

    public double getZ() {
        return this.player.getZ();
    }

    public boolean isSpectating() {
        return this.spectating;
    }

    public void setSpectating(boolean newState) {
        this.spectating = newState;
    }

    public SavedPosition getSavedPosition() {
        return this.savedPosition;
    }

    public void setSavedPosition(SavedPosition savedPosition) {
        this.savedPosition = savedPosition;
    }

    public void savePosition() {
        this.savedPosition = new SavedPosition(this.player);
    }

    public static class SavedPosition {
        private final ServerWorld world;
        private final Vec3d position;
        private final float pitch;
        private final float yaw;

        public SavedPosition(ServerPlayerEntity player) {
            this.world = player.getServerWorld();

            this.position = player.getPos();
            this.pitch = player.getPitch();
            this.yaw = player.getYaw();
        }

        public SavedPosition(ServerWorld world, Vec3d position, float yaw, float pitch) {
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
    }
}
