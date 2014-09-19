package machir.ironfist.command;

import machir.ironfist.IronFist;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public class CommandIronFist extends CommandBase {

	@Override
	public String getCommandName() {
		return "fist";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/fist <command>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length > 0) {
			if (args[0].equals("levelup")) {
				EntityPlayerMP player = getPlayer(sender,
						sender.getCommandSenderName());
				int fistLevel = 1;
				if (player.getEntityData().hasKey(
						IronFist.ModPrefix + "fistLevel")) {
					fistLevel = player.getEntityData().getInteger(
							IronFist.ModPrefix + "fistLevel");
				}
				player.getEntityData().setInteger(
						IronFist.ModPrefix + "fistLevel", ++fistLevel);
				player.addChatMessage(new ChatComponentText("Fist level up!"));
				player.addChatMessage(new ChatComponentText("New level: "
						+ fistLevel));
			}
		} else {
			throw new WrongUsageException(this.getCommandUsage(sender),
					new Object[0]);
		}
	}
	
	@Override
    public int getRequiredPermissionLevel() {
        return 3;
    }
}
