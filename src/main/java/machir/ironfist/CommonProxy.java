package machir.ironfist;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

public class CommonProxy {
	// Used to store the extended properties between death and respawn
	private final Map<String, NBTTagCompound> extendedProperties = new HashMap<String, NBTTagCompound>();

	/**
	 * Adds extended properties to the temporary storage.
	 * 
	 * @param name
	 *            The entity name
	 * @param compound
	 *            The extended properties compound
	 */
	public void addExtendedProperties(String name,
			NBTTagCompound compound) {
		extendedProperties.put(name, compound);
	}

	/**
	 * Pops the extended properties of the specified entity name off the
	 * temporary storage and returns it.
	 * 
	 * @param name
	 *            The entity name
	 *            
	 * @return The extended properties after removing it from the map
	 */
	public NBTTagCompound popExtendedProperties(String name) {
		return extendedProperties.remove(name);
	}
}
