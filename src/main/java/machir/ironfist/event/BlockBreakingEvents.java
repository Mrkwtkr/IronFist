package machir.ironfist.event;

import machir.ironfist.IronFist;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class BlockBreakingEvents {
	private static final int millisecondsHardnessOne = 2000;
	private EntityPlayer entityPlayer;
	private NBTTagCompound entityData;
	private int fistLevel = 1;
	private double fistXP;
	private double obtainedXP;

	private double fatigue;
	private float cumulativeWork;

	/**
	 * Hooks in on the block break event to handle the fist xp and fatigue
	 */
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		entityPlayer = event.getPlayer();

		// Skip if no player is found
		if (entityPlayer == null) {
			return;
		}

		// Skip the check if the current item is a tool
		if (entityPlayer.getCurrentEquippedItem() != null
				&& entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemTool) {
			return;
		}

		// Load the player's fist data to use later on
		loadIronFistData();

		// Calculate the fatigue using the block hardness
		float hardness = event.block.getBlockHardness(event.world, event.x,
				event.y, event.z);
		calculateFatigue(hardness);

		// If the fatigue is low, damage the player
		if (fatigue < 0.2) {
			if (entityPlayer.attackEntityFrom(new DamageSource("fist")
					.setDamageBypassesArmor().setDamageIsAbsolute(), 2f)
					&& !entityPlayer.isDead) {
				entityPlayer.addChatMessage(new ChatComponentText(
						"You're bleeding, take a break or use tools!"));
			}
		}

		calculateXP(hardness);

		// Add the obtained xp and check if a level up should occur
		fistXP += obtainedXP;
		if (fistXP > getLevelUpXP()) {
			levelUp();
		}

		// Store the new fist data again
		storeIronFistData();
	}

	/**
	 * Loads the various entity properties from the player entity data. If a
	 * property does not exist it'll use it's default value
	 */
	private void loadIronFistData() {
		NBTTagCompound entityData = entityPlayer.getEntityData();

		if (entityData.hasKey(IronFist.ModPrefix + "fistXP")) {
			fistXP = entityData.getDouble(IronFist.ModPrefix + "fistXP");
		}

		if (entityData.hasKey(IronFist.ModPrefix + "fistLevel")) {
			fistLevel = entityData.getInteger(IronFist.ModPrefix + "fistLevel");
		}

		if (entityData.hasKey(IronFist.ModPrefix + "fatigue")) {
			fatigue = entityData.getDouble(IronFist.ModPrefix + "fatigue");
		}

		if (entityData.hasKey(IronFist.ModPrefix + "cumulativeWork")) {
			cumulativeWork = entityData.getFloat(IronFist.ModPrefix
					+ "cumulativeWork");
		}
	}

	/**
	 * Calculates the fatigue of the player
	 * 
	 * @param blockHardness
	 *            The hardness of the block broken
	 */
	private void calculateFatigue(float blockHardness) {
		float recoveryTime = 30000 * fistLevel;

		long currentMillis = System.currentTimeMillis();
		long lastMillis = entityData.getLong(IronFist.ModPrefix
				+ "lastBreakMillis");

		long deltaMillis = (currentMillis - lastMillis);
		long delta = Math.round(Math.min(recoveryTime, deltaMillis));

		float workTime = millisecondsHardnessOne * blockHardness;
		float restTime = delta - workTime;

		if (delta == recoveryTime) {
			cumulativeWork = 0;
		} else {
			cumulativeWork = Math.min(
					Math.max(0, workTime - restTime + cumulativeWork),
					recoveryTime);
		}

		fatigue = (recoveryTime - cumulativeWork) / recoveryTime;

		entityData.setLong(IronFist.ModPrefix + "lastBreakMillis",
				currentMillis);
	}

	/**
	 * Calculates the obtained XP using the fatigue and block hardness
	 * 
	 * @param blockHardness
	 *            The hardness of the block broken
	 */
	private void calculateXP(float blockHardness) {
		obtainedXP = blockHardness * fatigue;
	}

	/**
	 * Stores the various fist data of the player
	 */
	private void storeIronFistData() {
		entityData.setDouble(IronFist.ModPrefix + "fistXP", fistXP);
		entityData.setDouble(IronFist.ModPrefix + "fatigue", fatigue);
		entityData.setFloat(IronFist.ModPrefix + "cumulativeWork",
				cumulativeWork);
	}

	/**
	 * @return The required xp to level up
	 */
	private double getLevelUpXP() {
		return 6.95997 * Math.pow(Math.E, (1.97241 * fistLevel));
	}

	/**
	 * Increases the player fist level by one
	 */
	private void levelUp() {
		fistLevel++;
		entityPlayer.addChatMessage(new ChatComponentText("Fist level up!"));
		entityPlayer.addChatMessage(new ChatComponentText("New level: "
				+ fistLevel));
		entityData.setInteger("fistLevel", fistLevel);
	}

	/**
	 * Hooks in to the break speed calculation and modifies it depending on the
	 * fist level
	 */
	@SubscribeEvent
	public void getBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (event.entityPlayer == null
				|| event.entityPlayer.getCurrentEquippedItem() != null) {
			return;
		}

		entityData = event.entityPlayer.getEntityData();
		if (entityData.hasKey(IronFist.ModPrefix + "fistLevel")) {
			fistLevel = entityData.getInteger(IronFist.ModPrefix + "fistLevel");
		}

		// If a tool is required and the required level is reached or if no tool
		// is required update the speed
		if ((!event.block.getMaterial().isToolNotRequired() && event.block
				.getHarvestLevel(event.metadata) - (fistLevel - 1) <= 0)
				|| (event.block.getMaterial().isToolNotRequired())) {
			event.newSpeed = Math.max(((fistLevel - 1) * 2), 1f);

		}
	}

	/**
	 * Hooks in to the check to see if a player can harvest a block and uses the
	 * fist level to determine it
	 */
	@SubscribeEvent
	public void canHarvestBlock(PlayerEvent.HarvestCheck event) {
		if (event.entityPlayer == null) {
			return;
		}

		entityData = event.entityPlayer.getEntityData();
		if (entityData.hasKey(IronFist.ModPrefix + "fistLevel")) {
			fistLevel = entityData.getInteger(IronFist.ModPrefix + "fistLevel");
		}

		// If a tool is required and the required level is reached the block may
		// be harvested
		// Use metadata 0 to get harvest level because I can't seem to find a
		// way to get it
		if (!event.block.getMaterial().isToolNotRequired()) {
			int harvestLevel = event.block.getHarvestLevel(0);

			// If the harvest lvl is 0 check by material else check if lvl is
			// reached
			if (harvestLevel == 0
					&& canBreakMaterial(fistLevel, event.block.getMaterial())
					|| (harvestLevel != 0 && harvestLevel - (fistLevel - 1) <= 0)) {
				event.success = true;
			}
		}
	}

	/**
	 * Checks if the fist level can break the material
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
