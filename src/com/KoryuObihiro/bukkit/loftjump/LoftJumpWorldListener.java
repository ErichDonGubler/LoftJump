package com.KoryuObihiro.bukkit.loftjump;

import org.bukkit.World;

import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;


public class LoftJumpWorldListener extends WorldListener
{
//Members
	private LoftJump plugin;
	//Constructors	
	public LoftJumpWorldListener(LoftJump plugin) {this.plugin = plugin;}
	
//Functions
	
	@Override
	public void onWorldLoad(WorldLoadEvent event)
	{
		//enable LJ and LJ-free according to settings
		World world = event.getWorld();
		if(plugin.isEnabled())
			plugin.loadConfig(world);
	}			 
}
