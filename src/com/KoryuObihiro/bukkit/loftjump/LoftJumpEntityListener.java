package com.KoryuObihiro.bukkit.loftjump;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;
public class LoftJumpEntityListener extends EntityListener
{ 

	public static LoftJump plugin;
	public LoftJumpEntityListener(LoftJump plugin){this.plugin = plugin;}
	public static Logger log = Logger.getLogger("Minecraft");
	//This method is called when ever a block is placed.
	@Override
	 public void onEntityDamage(EntityDamageEvent event) 
	 {
		Player player = null;
		try
		{
			player = (Player)event.getEntity();
		}
		catch(Exception e){return;}
		Material toConsume = Material.FEATHER;
		
		if(event.getCause().equals(DamageCause.FALL))
			plugin.tryLoftJump(player, event, toConsume);
		return;
	 }
}
