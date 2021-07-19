package sh.damon.spectate.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.damon.spectate.Spectate;
import sh.damon.spectate.player.SpectatingPlayer;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    private final static String SWITCH_POSITION = "SwitchPosition";

    @Inject(at = @At("RETURN"), method = "readCustomDataFromNbt")
    public void onReadNbtData(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.contains(SWITCH_POSITION)) return;

        final ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) (Object) this;

        final Spectate sp = Spectate.getInstance();
        final SpectatingPlayer player = sp.playerManager.getPlayer(serverPlayerEntity);
        // player position is still in memory no need to parse
        if (player.getSavedPosition() != null) return;

        final NbtCompound spectatingNbt = nbt.getCompound(SWITCH_POSITION);

        final MinecraftServer server = serverPlayerEntity.getServer();

        assert server != null;
        final ServerWorld world = server.getWorld(
            RegistryKey.of(Registry.WORLD_KEY, new Identifier(spectatingNbt.getString("world")))
        );

        final NbtCompound camera = spectatingNbt.getCompound("camera");
        final NbtCompound position = spectatingNbt.getCompound("position");

        assert world != null;
        final SpectatingPlayer.SavedPosition savedPosition = new SpectatingPlayer.SavedPosition(
            world,
            new Vec3d(position.getDouble("x"), position.getDouble("y"), position.getDouble("z")),
            camera.getFloat("yaw"),
            camera.getFloat("pitch")
        );

        Spectate.log.info("READ NBT TAG: "+ spectatingNbt);

        player.setSpectating(true);
        player.setSavedPosition(savedPosition);
    }

    @Inject(at = @At("RETURN"), method = "writeCustomDataToNbt")
    public void onWriteNbtData(NbtCompound nbt, CallbackInfo ci) {
        final Spectate sp = Spectate.getInstance();
        final SpectatingPlayer player = sp.playerManager.getPlayer((ServerPlayerEntity) (Object) this);

        if (!player.isSpectating()) return;
        final SpectatingPlayer.SavedPosition savedPosition = player.getSavedPosition();
        final Vec3d pos = savedPosition.getPosition();

        NbtCompound camera = new NbtCompound();
        camera.putFloat("pitch", savedPosition.getPitch());
        camera.putFloat("yaw", savedPosition.getYaw());

        NbtCompound position = new NbtCompound();
        position.putDouble("x", pos.getX());
        position.putDouble("y", pos.getY());
        position.putDouble("z", pos.getZ());

        NbtCompound spectatingNbt = new NbtCompound();
        spectatingNbt.put("camera", camera);
        spectatingNbt.put("position", position);
        spectatingNbt.putString("world", savedPosition.getWorld().getRegistryKey().getValue().toString());

        nbt.put(SWITCH_POSITION, spectatingNbt);

        Spectate.log.info("WROTE NBT TAG: "+ spectatingNbt);
    }
}
