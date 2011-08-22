package com.KoryuObihiro.bukkit.loftjump;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;

public class LoftJumpEntityListener extends EntityListener
{ 

	public static LoftJump plugin;
	public LoftJumpEntityListener(LoftJump plugin){LoftJumpEntityListener.plugin = plugin;}
	public static Logger log = Logger.getLogger("Minecraft");
	//This method is called when ever a block is placed.
	@Override
	 public void onEntityDamage(EntityDamageEvent event) 
	 {
		if(event.getEntity() instanceof Player && event.getCause().equals(DamageCause.FALL))
		{
			LoftJumpPlayerConfiguration playerConfig = plugin.loadPlayer((Player)event.getEntity());
			if(playerConfig != null)
				event.setDamage(playerConfig.applyCushion(event.getDamage()));
		}
	 }
}
