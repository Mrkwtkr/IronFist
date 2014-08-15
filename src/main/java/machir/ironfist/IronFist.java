package machir.ironfist;

import machir.ironfist.event.BlockBreakingEvents;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "ironfist", name = "Iron Fist")
public class IronFist {
    @Instance("ironfist")
    public static IronFist instance;

    @SidedProxy(clientSide = "machir.ironfist.client.ClientProxy", serverSide = "machir.ironfist.CommonProxy")
    public static CommonProxy proxy;

    public static Logger log;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        log = evt.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent evt) {
        FMLCommonHandler.instance().bus().register(new BlockBreakingEvents());
        MinecraftForge.EVENT_BUS.register(new BlockBreakingEvents());
    }
}
