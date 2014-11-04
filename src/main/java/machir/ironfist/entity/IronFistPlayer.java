package machir.ironfist.entity;

import machir.ironfist.IronFist;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class IronFistPlayer implements IExtendedEntityProperties {
	// The properties identifier
	public final static String EXT_PROP_NAME = "IronFistPlayer";
	private final EntityPlayer player;

	// Fist level and XP
	int fistLevel;
	double fistXP;

	// Fatigue information
	double fatigue;
	float cumulativeWork;
	long lastBreakMillis;

	/**
	 * Creates a new instance of extended player data for the specified player.
	 * 
	 * @param player
	 *            The player to create extended data for
	 */
	public IronFistPlayer(EntityPlayer player) {
		this.player = player;

		// Initialize default data
		this.fistLevel = 1;
		this.fistXP = 0.0D;

		this.fatigue = 0.0D;
		this.cumulativeWork = 0.0F;
		this.lastBreakMillis = 0L;
	}

	/**
	 * Allows for easy access to the extended player data.
	 * 
	 * @param entityPlayer
	 *            The player to retrieve the extended data of
	 * @return The extended data instance
	 */
	public static final IronFistPlayer get(EntityPlayer entityPlayer) {
		return (IronFistPlayer) entityPlayer
				.getExtendedProperties(EXT_PROP_NAME);
	}

	/**
	 * Called when the entity that this class is attached to is saved. Any
	 * custom entity data that needs saving should be saved here.
	 * 
	 * @param compound
	 *            The compound to save to.
	 */
	@Override
	public void saveNBTData(NBTTagCompound compound) {
		NBTTagCompound properties = new NBTTagCompound();

		// Store the iron fist properties in the tag compound
		properties.setInteger("fistLevel", this.fistLevel);
		properties.setDouble("fistXP", this.fistXP);

		properties.setDouble("fatigue", this.fatigue);
		properties.setFloat("cumulativeWork", this.cumulativeWork);
		properties.setLong("lastBreakMillis", this.lastBreakMillis);

		// Store the tag compound under the iron fist tag
		compound.setTag(EXT_PROP_NAME, properties);
	}

	/**
	 * Called when the entity that this class is attached to is loaded. In order
	 * to hook into this, you will need to subscribe to the EntityConstructing
	 * event. Otherwise, you will need to initialize manually.
	 * 
	 * @param compound
	 *            The compound to load from.
	 */
	@Override
	public void loadNBTData(NBTTagCompound compound) {
		// Retrieve the iron fist tag
		NBTTagCompound properties = (NBTTagCompound) compound
				.getTag(EXT_PROP_NAME);

		// Retrieve the iron fist properties from the tag compound
		this.fistLevel = properties.getInteger("fistLevel");
		this.fistXP = properties.getDouble("fistXP");

		this.fatigue = properties.getDouble("fatigue");
		this.cumulativeWork = properties.getFloat("cumulativeWork");
		this.lastBreakMillis = properties.getLong("lastBreakMillis");

		IronFist.log.info("Loaded extended player properties");
	}

	/**
	 * Used to initialize the extended properties with the entity that this is
	 * attached to, as well as the world object. Called automatically if you
	 * register with the EntityConstructing event. May be called multiple times
	 * if the extended properties is moved over to a new entity. Such as when a
	 * player switches dimension {Minecraft re-creates the player entity}
	 * 
	 * @param entity
	 *            The entity that this extended properties is attached to
	 * @param world
	 *            The world in which the entity exists
	 */
	@Override
	public void init(Entity entity, World world) {

	}

	/**
	 * Used to register the extended properties for the player during the
	 * EntityConstructing event.
	 * 
	 * @param entityPlayer
	 *            The player to register the properties to
	 */
	public static void register(EntityPlayer entityPlayer) {
		entityPlayer.registerExtendedProperties(IronFistPlayer.EXT_PROP_NAME,
				new IronFistPlayer(entityPlayer));
	}

	/**
	 * @return The fist level
	 */
	public int getFistLevel() {
		return fistLevel;
	}

	/**
	 * Sets the fist level.
	 * 
	 * @param fistLevel
	 *            The new fist level
	 */
	public void setFistLevel(int fistLevel) {
		this.fistLevel = fistLevel;
	}

	/**
	 * @return The fist XP
	 */
	public double getFistXP() {
		return fistXP;
	}

	/**
	 * Sets the fist XP.
	 * 
	 * @param fistXP
	 *            The new fist XP
	 */
	public void setFistXP(double fistXP) {
		this.fistXP = fistXP;
	}

	/**
	 * @return The fatigue of the player, used to calculate XP and possible
	 *         damage
	 */
	public double getFatigue() {
		return fatigue;
	}

	/**
	 * Sets the fatigue of the player.
	 * 
	 * @param fatigue
	 *            The new fatigue
	 */
	public void setFatigue(double fatigue) {
		this.fatigue = fatigue;
	}

	/**
	 * @return The cumulative work, used in fatigue calculations
	 */
	public float getCumulativeWork() {
		return cumulativeWork;
	}

	/**
	 * Sets the cumulative work.
	 * 
	 * @param cumulativeWork
	 *            The new cumulative work
	 */
	public void setCumulativeWork(float cumulativeWork) {
		this.cumulativeWork = cumulativeWork;
	}

	/**
	 * @return The last time in milliseconds when the player broke a block
	 */
	public long getLastBreakMillis() {
		return lastBreakMillis;
	}

	/**
	 * Updates the last time in milliseconds when the player broke a block.
	 * 
	 * @param lastBreakMillis
	 *            The time in milliseconds
	 */
	public void setLastBreakMillis(long lastBreakMillis) {
		this.lastBreakMillis = lastBreakMillis;
	}
}
