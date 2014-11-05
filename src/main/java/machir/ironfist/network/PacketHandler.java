package machir.ironfist.network;

import machir.ironfist.IronFist;
import machir.ironfist.network.messages.MessageFistLevelSync;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public abstract class PacketHandler {
    
    public static final SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel("IronFist");
    
    public static void init() {
        IronFist.log.info("Registering network messages");
        instance.registerMessage(MessageFistLevelSync.class, MessageFistLevelSync.class, 0, Side.CLIENT);
    }
}