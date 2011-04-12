package com.KoryuObihiro.bukkit.loftjump;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;


public class LoftJumpPlayerListener extends PlayerListener
{
//Members
	private LoftJump plugin;
	//Constructors	
	public LoftJumpPlayerListener(LoftJump plugin) 
	{
		this.plugin = plugin;
	}
	
//Functions
	@Override
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if(plugin.settings_onByDefault && !plugin.player_Enabled(event.getPlayer()) 
				&& LoftJump.hasPermission(event.getPlayer(), "loftjump.use"))
			plugin.toggleUsage(event.getPlayer(), false);
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (plugin.player_Enabled(event.getPlayer()))
			plugin.toggleUsage(event.getPlayer(), false);
	}
	
	@Override
	public void onPlayerKick(PlayerKickEvent event)
	{
		if (plugin.player_Enabled(event.getPlayer()))
			plugin.toggleUsage(event.getPlayer(), false);
	}
				 
}
