package machir.ironfist.event;

import machir.ironfist.IronFistConstants;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class BlockBreakingEvents {
	private EntityPlayer entityPlayer = null;
	private NBTTagCompound entityData = null;
	private int fistLevel = 1;
	private double fistXP = 0.0D;
	private double obtainedXP = 0.0D;
	
	private double fatigue = 0.0D;
	private long cumulativeWork = 0L;
	
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
		calculateFatigue();

		if (fatigue > 1) {
			if (entityPlayer.attackEntityFrom(new DamageSource("fist").setDamageBypassesArmor().setDamageIsAbsolute(), 2f) && !entityPlayer.isDead) {
				entityPlayer.addChatMessage(new ChatComponentText("You're bleeding, take a break or use tools!"));
			}
		}
		
		calculateXP(event.block.getBlockHardness(event.world, event.x, event.y, event.z));
		
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
		
		if (entityData.hasKey("IronFist:cumulativeWork")) {
		    cumulativeWork = entityData.getLong("IronFist:cumulativeWork");
		}
	}
	
	private void calculateFatigue() {
		long currentMillis = System.currentTimeMillis();
		long lastMillis = entityData.getLong(IronFistConstants.ModPrefix + "lastBreakMillis");
		
		float deltaMinutes = (currentMillis - lastMillis) / 60000.0f;
		float delta = Math.min(deltaMinutes, fistLevel);
		
		if (delta != fistLevel) {
		    cumulativeWork += deltaMinutes < 0.5 ? deltaMinutes : 0;
		}
		
		double log = Math.log(cumulativeWork);
		fatigue = Math.min((1 / log == -0.0 ? 1 : 1 / log), 1);
		
		entityData.setLong(IronFistConstants.ModPrefix + "lastBreakMillis", currentMillis);
	}
	
	private void calculateXP(float blockHardness) {
		obtainedXP = blockHardness * fatigue;
	}
	
	private void storeIronFistData() {
		entityData.setDouble(IronFistConstants.ModPrefix + "fistXP", fistXP);
		entityData.setDouble(IronFistConstants.ModPrefix + "fatigue", fatigue);
		entityData.setLong(IronFistConstants.ModPrefix + "cumulativeWork",  cumulativeWork);
	}
	
	private double getLevelUpXP() {
		return Math.pow(fistLevel, 1/5);
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
		if (event.entityPlayer == null) {
			return;
		}
		
		entityData = event.entityPlayer.getEntityData();
		if (entityData.hasKey(IronFistConstants.ModPrefix + "fistLevel")) {
			fistLevel = entityData.getInteger(IronFistConstants.ModPrefix + "fistLevel");
		}
		
		// If a tool is required and the required level is reached or if no tool is required update the speed
		if ((!event.block.getMaterial().isToolNotRequired()
				&& event.block.getHarvestLevel(event.metadata) - (fistLevel / 10) <= 0)
				|| (event.block.getMaterial().isToolNotRequired())) {
				// Calculate the fist breaking speed by dividing the level by 10 and adding 1 for default speed
				event.newSpeed = (fistLevel / 10) + 1f;
				
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
		if ((!event.block.getMaterial().isToolNotRequired()
				&& event.block.getHarvestLevel(0) - (fistLevel / 10) <= 0)) {
				event.success = true;
				
		}
	}
}
