package sh.damon.spectate.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
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
        // Don't do anything unless player has SwitchPosition set
        if (!nbt.contains(SWITCH_POSITION)) return;

        final ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) (Object) this;

        final Spectate sp = Spectate.getInstance();
        final SpectatingPlayer player = sp.playerManager.getPlayer(serverPlayerEntity);

        if (!serverPlayerEntity.isSpectator()) return;

        final NbtCompound spectatingNbt = nbt.getCompound(SWITCH_POSITION);

        final MinecraftServer server = serverPlayerEntity.getServer();

        assert server != null;
        final ServerWorld world = server.getWorld(
            RegistryKey.of(Registry.WORLD_KEY, new Identifier(spectatingNbt.getString("world")))
        );

        final NbtCompound camera = spectatingNbt.getCompound("camera");
        final NbtCompound position = spectatingNbt.getCompound("position");

        final GameMode gameMode = GameMode.getOrNull(spectatingNbt.getInt("gameMode"));

        assert world != null;
        final SpectatingPlayer.SaveData saveData = new SpectatingPlayer.SaveData(
            gameMode,
            world,
            new Vec3d(position.getDouble("x"), position.getDouble("y"), position.getDouble("z")),
            camera.getFloat("yaw"),
            camera.getFloat("pitch")
        );

        player.setSpectating(true);
        player.setSaveData(saveData);
    }

    @Inject(at = @At("RETURN"), method = "writeCustomDataToNbt")
    public void onWriteNbtData(NbtCompound nbt, CallbackInfo ci) {
        final ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) (Object) this;

        final Spectate sp = Spectate.getInstance();
        final SpectatingPlayer player = sp.playerManager.getPlayer(serverPlayerEntity);

        if (!serverPlayerEntity.isSpectator()) return;

        final SpectatingPlayer.SaveData saveData = player.getSavedPosition();
        final Vec3d pos = saveData.getPosition();

        NbtCompound camera = new NbtCompound();
        camera.putFloat("pitch", saveData.getPitch());
        camera.putFloat("yaw", saveData.getYaw());

        NbtCompound position = new NbtCompound();
        position.putDouble("x", pos.getX());
        position.putDouble("y", pos.getY());
        position.putDouble("z", pos.getZ());

        NbtCompound spectatingNbt = new NbtCompound();
        spectatingNbt.put("camera", camera);
        spectatingNbt.put("position", position);
        spectatingNbt.putInt("gameMode", saveData.getGameMode().getId());
        spectatingNbt.putString("world", saveData.getWorld().getRegistryKey().getValue().toString());

        nbt.put(SWITCH_POSITION, spectatingNbt);
    }
}
