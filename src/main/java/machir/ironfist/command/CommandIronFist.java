package machir.ironfist.command;

import java.util.ArrayList;
import java.util.List;

import machir.ironfist.entity.IronFistPlayer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public class CommandIronFist extends CommandBase {
	private String[] commands = new String[] { "addxp", "levelup", "showxp", "showlevel" };

	/**
	 * @return The command name
	 */
	@Override
	public String getCommandName() {
		return "fist";
	}

	/**
	 * @return The command usage string
	 */
	@Override
	public String getCommandUsage(ICommandSender sender) {
		StringBuilder builder = new StringBuilder("/fist [");
		for (int i = commands.length - 1; i >= 0; i--) {
			// Add each command to the string
			builder.append(commands[i]);

			// While we're not at the last command, add a | to divide the
			// commands
			if (i >= 1) {
				builder.append("|");
			}
		}
		builder.append("]");

		// Build the final string and return it
		return builder.toString();
	}

	/**
	 * Adds the strings available in this command to the given list of tab
	 * completion options.
	 * 
	 * @param sender
	 *            The command sender data
	 * @param args
	 *            The command arguments
	 */
	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender,
			String[] args) {
		ArrayList<String> options = new ArrayList<String>();

		// Only process if at least one argument is (partially) given
		if (args.length == 1) {
			// If a command could be made out of the current argument,
			// add it to the list
			for (String command : commands) {
				if (command.startsWith(args[0])) {
					options.add(command);
				}
			}
		}

		return options;
	}

	/**
	 * Processes an issued command.
	 * 
	 * @param sender
	 *            The command sender data
	 * @param args
	 *            The command arguments
	 */
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length == 1) {
			if (args[0].equals("showlevel")) {
				EntityPlayerMP player = getPlayer(sender,
						sender.getCommandSenderName());

				// Load the iron fist properties
				IronFistPlayer properties = IronFistPlayer.get(player);
				int fistLevel = properties.getFistLevel();

				// Show the fist level
				player.addChatMessage(new ChatComponentText("Fist level: "
						+ fistLevel));
				return;
			} else if (args[0].equals("showxp")) {
				EntityPlayerMP player = getPlayer(sender,
						sender.getCommandSenderName());

				// Load the iron fist properties
				IronFistPlayer properties = IronFistPlayer.get(player);
				double fistXP = properties.getFistXP();

				// Show the fist level
				player.addChatMessage(new ChatComponentText("Fist xp: "
						+ fistXP));
				return;
			} else if (args[0].equals("levelup")) {
				EntityPlayerMP player = getPlayer(sender,
						sender.getCommandSenderName());

				// Load the iron fist properties
				IronFistPlayer properties = IronFistPlayer.get(player);

				// Increase the fist level by one
				int fistLevel = properties.getFistLevel();
				fistLevel++;
				properties.setFistLevel(fistLevel);

				player.addChatMessage(new ChatComponentText("Fist level up!"));
				player.addChatMessage(new ChatComponentText("New level: "
						+ fistLevel));
				return;
			}
		} else if (args.length == 2) {
			if (args[0].equals("addxp")) {
				EntityPlayerMP player = getPlayer(sender,
						sender.getCommandSenderName());

				// Load the iron fist properties
				IronFistPlayer properties = IronFistPlayer.get(player);
				double currentXP = properties.getFistXP();
				double additionalXP = 0.0D;
				try {
					additionalXP = Double.parseDouble(args[1]);
				} catch(NumberFormatException e) {
					player.addChatMessage(new ChatComponentText("Invalid number!"));
				}

				// Set the new XP
				double newXP = currentXP + additionalXP;
				properties.setFistXP(newXP);
				player.addChatMessage(new ChatComponentText("XP increased to " + newXP));
				return;
			}
		} 
		
		throw new WrongUsageException(this.getCommandUsage(sender),
				new Object[0]);
	}

	/**
	 * @return The required permission level
	 */
	@Override
	public int getRequiredPermissionLevel() {
		// Operator only for now, need to split the show for any level
		return 3;
	}
}
