package machir.ironfist;

import machir.ironfist.command.CommandIronFist;
import machir.ironfist.event.BlockBreakingEvents;
import machir.ironfist.event.PlayerEvents;
import machir.ironfist.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = "IronFist")
public class IronFist {
	public static final String ModPrefix = "IronFist:";

	@Instance("ironfist")
	public static IronFist instance;

	@SidedProxy(clientSide = "machir.ironfist.client.ClientProxy", serverSide = "machir.ironfist.CommonProxy")
	public static CommonProxy proxy;

	public static Logger log;

	/**
	 * Handles the pre-initialization event of the mod
	 * 
	 * @param evt
	 *            The pre-initialization event object
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		// Initialize the mod log
		log = evt.getModLog();
	}

	/**
	 * Handles the initialization event of the mod
	 * 
	 * @param evt
	 *            The initialization event object
	 */
	@EventHandler
	public void init(FMLInitializationEvent evt) {
		// Register the block breaking handler
		FMLCommonHandler.instance().bus().register(new BlockBreakingEvents());
		MinecraftForge.EVENT_BUS.register(new BlockBreakingEvents());
		
		// Register the player handler
		FMLCommonHandler.instance().bus().register(new PlayerEvents());
		MinecraftForge.EVENT_BUS.register(new PlayerEvents());
		
		PacketHandler.init();
	}

	/**
	 * Handles the server start up event of the mod
	 * 
	 * @param evt
	 *            The server start up event object
	 */
	@EventHandler
	public void serverStartup(FMLServerStartingEvent evt) {
		// Register the mod command which includes every command
		
		// TODO Check if there is a cleaner way of accomplishing /fist <command>
		//      than using one command
		evt.registerServerCommand(new CommandIronFist());
	}
}
