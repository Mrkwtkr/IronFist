package machir.ironfist.event;

import machir.ironfist.IronFist;
import machir.ironfist.entity.IronFistPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PlayerEvents {
	/**
	 * Hooks in on the entity constructing to add extended properties to
	 * players.
	 * 
	 * @param event
	 *            The entity constructing event object
	 */
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event) {
		// Check if the entity is a player and if it doesn't yet have the
		// extended properties
		if (event.entity instanceof EntityPlayer
				&& IronFistPlayer.get((EntityPlayer) event.entity) == null) {
			// Register the extended properties to the player
			IronFistPlayer.register((EntityPlayer) event.entity);
		}
	}

	@SubscribeEvent
	public void onLivingDeathEvent(LivingDeathEvent event) {
		if (!event.entity.worldObj.isRemote
				&& event.entity instanceof EntityPlayer) {
			// Create a new NBT compound to store the extended properties
			NBTTagCompound playerData = new NBTTagCompound();

			// Save the extended properties to the NBT compound
			((IronFistPlayer) (event.entity
					.getExtendedProperties(IronFistPlayer.EXT_PROP_NAME)))
					.saveNBTData(playerData);

			// Store the NBT compound in the proxy under the player name
			IronFist.proxy.addExtendedProperties(
					((EntityPlayer) event.entity).getDisplayName(), playerData);
		}
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (!event.entity.worldObj.isRemote
				&& event.entity instanceof EntityPlayer) {
			// Pop the temporary stored extended properties and restore it
			NBTTagCompound playerData = IronFist.proxy
					.popExtendedProperties(((EntityPlayer) event.entity)
							.getDisplayName());

			if (playerData != null) {
				((IronFistPlayer) (event.entity
						.getExtendedProperties(IronFistPlayer.EXT_PROP_NAME)))
						.loadNBTData(playerData);
			}
		}
	}
}
