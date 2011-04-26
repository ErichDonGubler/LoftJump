package com.KoryuObihiro.bukkit.loftjump;

import org.bukkit.World;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;


public class LoftJumpPlayerListener extends PlayerListener
{
//Members
	private LoftJump plugin;
	//Constructors	
	public LoftJumpPlayerListener(LoftJump plugin) {this.plugin = plugin;}
	
//Functions
	@Override
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		//enable LJ and LJ-free according to settings
		World world = event.getPlayer().getWorld();
		if(plugin.configs.get(world) == null)
		{
			plugin.loadConfig(world);
		}
		if(plugin.configs.get(world) == null)
		{
			LoftJump.log.info("Could not initialize configuration settings for world " + world.getName());
		}
		else 
		{
			if(plugin.configs.get(world).get_use_onByDefault() 
				&& !plugin.player_Enabled(event.getPlayer()) 
				&& LoftJump.hasPermission(event.getPlayer(), "loftjump.use"))
			plugin.toggleUsage(event.getPlayer(), false);
		
			if(plugin.configs.get(world).get_free_onByDefault() && !plugin.player_isFree(event.getPlayer()) 
					&& LoftJump.hasPermission(event.getPlayer(), "loftjump.free"))
				plugin.toggleFreedom(event.getPlayer(), false);
		}
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if(plugin.player_Enabled(event.getPlayer())) plugin.toggleUsage(event.getPlayer(), false);
		if(plugin.player_isFree(event.getPlayer())) plugin.toggleFreedom(event.getPlayer(), false);
	}
	
	@Override
	public void onPlayerKick(PlayerKickEvent event)
	{
		if(plugin.player_Enabled(event.getPlayer())) plugin.toggleUsage(event.getPlayer(), false);
		if(plugin.player_isFree(event.getPlayer())) plugin.toggleFreedom(event.getPlayer(), false);
	}
				 
}
