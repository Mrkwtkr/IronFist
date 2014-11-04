package machir.ironfist.event;

import machir.ironfist.IronFist;
import machir.ironfist.entity.IronFistPlayer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class BlockBreakingEvents {
	private static final int MILLISECONDS_HARDNESS_ONE = 2000;

	/**
	 * Hooks in on the block break event to handle XP gain and fatigue
	 * measurement.
	 * 
	 * @param event
	 *            The block break event data
	 */
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		EntityPlayer entityPlayer = event.getPlayer();

		// Skip if no player is found
		if (entityPlayer == null) {
			return;
		}

		// Skip the check if the current item is a tool
		if (entityPlayer.getCurrentEquippedItem() != null
				&& entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemTool) {
			return;
		}

		// Load the iron fist properties of the player
		IronFistPlayer properties = IronFistPlayer.get(entityPlayer);

		double fistXP = properties.getFistXP();
		int fistLevel = properties.getFistLevel();
		double fatigue = properties.getFatigue();
		float cumulativeWork = properties.getCumulativeWork();

		// TODO Let Proger comment on this
		/** START OF MAGIC CALCULATIONS BY PROGER **/
		float hardness = event.block.getBlockHardness(event.world, event.x,
				event.y, event.z);
		float recoveryTime = 30000 * fistLevel;

		long currentMillis = System.currentTimeMillis();
		long lastMillis = properties.getLastBreakMillis();

		long deltaMillis = (currentMillis - lastMillis);
		long delta = Math.round(Math.min(recoveryTime, deltaMillis));

		float workTime = MILLISECONDS_HARDNESS_ONE * hardness;
		float restTime = delta - workTime;

		if (delta == recoveryTime) {
			cumulativeWork = 0;
		} else {
			cumulativeWork = Math.min(
					Math.max(0, workTime - restTime + cumulativeWork),
					recoveryTime);
		}

		fatigue = (recoveryTime - cumulativeWork) / recoveryTime;

		/** END OF MAGIC CALCULATIONS BY PROGER **/

		// Store the new time in milliseconds
		properties.setLastBreakMillis(currentMillis);

		// If the fatigue is low, damage the player
		if (fatigue < 0.2) {
			if (entityPlayer.attackEntityFrom(new DamageSource("fist")
					.setDamageBypassesArmor().setDamageIsAbsolute(), 2f)
					&& !entityPlayer.isDead) {
				entityPlayer.addChatMessage(new ChatComponentText(
						"You're bleeding, take a break or use tools!"));
			}
		}

		// Calculate the XP to be obtained
		double obtainedXP = hardness * fatigue;

		// Add the obtained XP and check if a level up should occur
		fistXP += obtainedXP;
		if (fistXP > getLevelUpXP(fistLevel)) {
			fistLevel++;
			entityPlayer
					.addChatMessage(new ChatComponentText("Fist level up!"));
			entityPlayer.addChatMessage(new ChatComponentText("New level: "
					+ fistLevel));
			properties.setFistLevel(fistLevel);
		}

		// Store the new fist properties
		properties.setFistXP(fistXP);
		properties.setFatigue(fatigue);
		properties.setCumulativeWork(cumulativeWork);
	}

	/**
	 * @param fistLevel
	 *            The current fist level
	 * 
	 * @return The required XP to level up
	 */
	private double getLevelUpXP(int fistLevel) {
		return 6.95997 * Math.pow(Math.E, (1.97241 * fistLevel));
	}

	/**
	 * Hooks in to the break speed calculation and modifies it depending on the
	 * fist level. Depending on if a tool is required and the level is reached.
	 * 
	 * @param event
	 *            The break speed event data
	 */
	@SubscribeEvent
	public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (event.entity.worldObj.isRemote || event.entityPlayer == null
				|| event.entityPlayer.getCurrentEquippedItem() != null) {
			return;
		}

		// Load the iron fist properties of the player
		IronFistPlayer properties = IronFistPlayer.get(event.entityPlayer);
		int fistLevel = properties.getFistLevel();

		// If a tool is required and the required level is reached or if no tool
		// is required update the speed
		if (!event.block.getMaterial().isToolNotRequired()) {
			if (event.block.getHarvestLevel(event.metadata) - (fistLevel - 1) <= 0) {
				event.newSpeed = Math.max(((fistLevel - 1) * 2), 1f);
			}
		} else {
			event.newSpeed = Math.max(((fistLevel - 1) * 2), 1f);
		}
	}

	/**
	 * Hooks in to the check to see if a player can harvest a block and uses the
	 * fist level to determine it.
	 * 
	 * @param event
	 *            The can harvest check data
	 */
	@SubscribeEvent
	public void canHarvestBlock(PlayerEvent.HarvestCheck event) {
		if (event.entity.worldObj.isRemote || event.entityPlayer == null) {
			return;
		}

		// Load the iron fist properties of the player
		IronFistPlayer properties = IronFistPlayer.get(event.entityPlayer);
		int fistLevel = properties.getFistLevel();

		// If a tool is required and the required level is reached the block may
		// be harvested, meta data 0 is used as I could find no way to find the
		// actual metadata.
		if (!event.block.getMaterial().isToolNotRequired()) {
			int harvestLevel = event.block.getHarvestLevel(0);

			// If the harvest level is -1 check by material else check if level
			// is
			// reached
			if (harvestLevel == 0
					&& canBreakMaterial(fistLevel, event.block.getMaterial())
					|| (harvestLevel != 0 && harvestLevel - (fistLevel - 1) <= 0)) {
				event.success = true;
			}
		}
	}

	/**
	 * Checks if the fist level can break the material.
	 * 
	 * @param level
	 *            The fist level to check against
	 * @param material
	 *            The material to check for
	 * @return Whether or not the material can be broken
	 */
	private boolean canBreakMaterial(int level, Material material) {
		return (material == Material.rock || material == Material.iron || material == Material.anvil) ? level > 1
				: true;
	}
}
