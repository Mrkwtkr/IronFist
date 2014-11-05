package machir.ironfist.network.messages;

import io.netty.buffer.ByteBuf;
import machir.ironfist.entity.IronFistPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageFistLevelSync implements IMessage, IMessageHandler<MessageFistLevelSync, IMessage> {
    private int fistLevel;
    private int entityId;
    
    public MessageFistLevelSync() {
    }
    
    public MessageFistLevelSync(int entityId, int fistLevel) {
    	this.fistLevel = fistLevel;
    	this.entityId = entityId;
    }
	
	@Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.fistLevel);
        buf.writeInt(this.entityId);
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
    	this.fistLevel = buf.readInt();
    	this.entityId = buf.readInt();
    }
    
    @Override
    public IMessage onMessage(MessageFistLevelSync msg, MessageContext ctx) {
    	Entity entity = FMLClientHandler.instance().getClient().theWorld.getEntityByID(msg.entityId);
    	if (entity instanceof EntityPlayer) {
    		IronFistPlayer properties = IronFistPlayer.get((EntityPlayer)entity);
    		properties.setFistLevel(msg.fistLevel);
    	}
    	
        return null;
    }
    
}
