package sh.damon.spectate;

import net.fabricmc.api.ModInitializer;
import sh.damon.spectate.command.CommandManager;
import sh.damon.spectate.player.PlayerManager;
import sh.damon.spectate.util.Log;

public class Spectate implements ModInitializer {
	public static final String MOD_ID = "spectate";
	public static final String MOD_NAME = "Spectate";

	public final CommandManager commandManager = new CommandManager();
	public PlayerManager playerManager = new PlayerManager();

	public static Log log = new Log(MOD_NAME);

	private static Spectate instance;

	@Override
	public void onInitialize() {
		Spectate.instance = this;

		this.commandManager.registerAll();

		Spectate.log.info( "Mod is ready.");
	}

	public static Spectate getInstance() {
		return instance;
	}
}
