package machir.ironfist.event;

import machir.ironfist.IronFist;
import machir.ironfist.IronFistConstants;
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
	// The average milliseconds between two 1f hardness blocks at lvl 0
	private static final int millisecondsHardnessOne = 2000;
	private EntityPlayer entityPlayer = null;
	private NBTTagCompound entityData = null;
	private int fistLevel = 1;
	private double fistXP = 0.0D;
	private double obtainedXP = 0.0D;
	
	private double fatigue = 0.0D;
	private float cumulativeWork = 0f;
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		entityPlayer = event.getPlayer();
		
		// Skip if no player is found
		if (entityPlayer == null) {
			return;
		}
		
		// Skip the check if the current item is a tool
		if (entityPlayer.getCurrentEquippedItem() != null && entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemTool) {
			return;
		}
		
		loadIronFistData();
		
		float hardness = event.block.getBlockHardness(event.world, event.x, event.y, event.z);
		calculateFatigue(hardness);

		IronFist.log.info("Fatigue: " + fatigue);
		IronFist.log.info("Level: " + fistLevel);
		IronFist.log.info("Experience: " + fistXP);
		if (fatigue < 0.2) {
			if (entityPlayer.attackEntityFrom(new DamageSource("fist").setDamageBypassesArmor().setDamageIsAbsolute(), 2f) && !entityPlayer.isDead) {
				entityPlayer.addChatMessage(new ChatComponentText("You're bleeding, take a break or use tools!"));
			}
		}
		
		calculateXP(hardness);
		
		// Stop when no experience is to be obtained
		if (obtainedXP == 0) {
			return;
		}
		fistXP += obtainedXP;
		 
		if (fistXP > getLevelUpXP()) {
			levelUp();
		}
		
		storeIronFistData();
	}
	
	/**
	 * Loads the various entity properties from the player entity data.
	 * If a property does not exist it'll use it's default value
	 */
	private void loadIronFistData() {
		NBTTagCompound entityData = entityPlayer.getEntityData();
	
		if (entityData.hasKey(IronFistConstants.ModPrefix + "fistXP")) {
			fistXP = entityData.getDouble(IronFistConstants.ModPrefix + "fistXP");
		}
		
		if (entityData.hasKey(IronFistConstants.ModPrefix + "fistLevel")) {
			fistLevel = entityData.getInteger(IronFistConstants.ModPrefix + "fistLevel");
		}
		
		if (entityData.hasKey(IronFistConstants.ModPrefix + "fatigue")) {
			fatigue = entityData.getDouble(IronFistConstants.ModPrefix + "fatigue");
		}
		
		if (entityData.hasKey(IronFistConstants.ModPrefix + "cumulativeWork")) {
		    cumulativeWork = entityData.getFloat(IronFistConstants.ModPrefix + "cumulativeWork");
		}
	}
	
	private void calculateFatigue(float blockHardness) {
		float recoveryTime = 30000 * fistLevel;
		
		long currentMillis = System.currentTimeMillis();
		long lastMillis = entityData.getLong(IronFistConstants.ModPrefix + "lastBreakMillis");
		
		long deltaMillis = (currentMillis - lastMillis);
		long delta = Math.round(Math.min(recoveryTime, deltaMillis));
		
		IronFist.log.info("DeltaMillis: " + deltaMillis);
		
		float workTime = millisecondsHardnessOne * blockHardness;
		float restTime = delta - workTime;
		
		IronFist.log.info("workTime: " + workTime);
		IronFist.log.info("restTime: " + restTime);
		
		//cumulativeWork = 0;
		if (delta == recoveryTime) {
			cumulativeWork = 0;
		} else {
			cumulativeWork = Math.min(Math.max(0, workTime - restTime + cumulativeWork), recoveryTime);
		}
		
		fatigue = (recoveryTime - cumulativeWork) / recoveryTime;
		
		
		entityData.setLong(IronFistConstants.ModPrefix + "lastBreakMillis", currentMillis);
	}
	
	private void calculateXP(float blockHardness) {
		obtainedXP = blockHardness * fatigue;
	}
	
	private void storeIronFistData() {
		entityData.setDouble(IronFistConstants.ModPrefix + "fistXP", fistXP);
		entityData.setDouble(IronFistConstants.ModPrefix + "fatigue", fatigue);
		entityData.setFloat(IronFistConstants.ModPrefix + "cumulativeWork",  cumulativeWork);
	}
	
	private double getLevelUpXP() {
		return 6.95997 * Math.pow(Math.E, (1.97241 * fistLevel));
	}
	
	/**
	 * Increases the player fist level by one
	 */
	private void levelUp() {
		fistLevel++;
		entityPlayer.addChatMessage(new ChatComponentText("Fist level up!"));
		entityPlayer.addChatMessage(new ChatComponentText("New level: " + fistLevel));
		entityData.setInteger("fistLevel", fistLevel);
	}
	
	@SubscribeEvent
	public void getBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (event.entityPlayer == null || event.entityPlayer.getCurrentEquippedItem() != null) {
			return;
		}
		
		entityData = event.entityPlayer.getEntityData();
		if (entityData.hasKey(IronFistConstants.ModPrefix + "fistLevel")) {
			fistLevel = entityData.getInteger(IronFistConstants.ModPrefix + "fistLevel");
		}
		
		// If a tool is required and the required level is reached or if no tool is required update the speed
		if ((!event.block.getMaterial().isToolNotRequired() 
				&& event.block.getHarvestLevel(event.metadata) - (fistLevel - 1) <= 0)
				|| (event.block.getMaterial().isToolNotRequired())) {
				event.newSpeed = Math.max(((fistLevel - 1) * 2), 1f);
				
		}
	}
	
	@SubscribeEvent
	public void canHarvestBlock(PlayerEvent.HarvestCheck event) {
		if (event.entityPlayer == null) {
			return;
		}
		
		entityData = event.entityPlayer.getEntityData();
		if (entityData.hasKey(IronFistConstants.ModPrefix + "fistLevel")) {
			fistLevel = entityData.getInteger(IronFistConstants.ModPrefix + "fistLevel");
		}
		
		// If a tool is required and the required level is reached the block may be harvested
		// Use metadata 0 to get harvest level because I can't seem to find a way to get it
		if (!event.block.getMaterial().isToolNotRequired()) {
			int harvestLevel = event.block.getHarvestLevel(0);
			
			// If the harvest lvl is 0 check by material else check if lvl is reached
			if (harvestLevel == 0 && canBreakMaterial(fistLevel, event.block.getMaterial())
			|| (harvestLevel != 0 && harvestLevel - (fistLevel - 1) <= 0)) {
				event.success = true;
			}
		}
	}
	
	private boolean canBreakMaterial(int level, Material material) {
		return (material == Material.rock || material == Material.iron || material == Material.anvil) ? level > 1 : true;
	}
}
