package com.KoryuObihiro.bukkit.loftjump;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if((event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) && event.getPlayer().getLocation().add(0,  -1, 0).getBlock().isEmpty())
			plugin.loadPlayer(event.getPlayer()).fireInteractEvent();
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		plugin.loadPlayer(event.getPlayer());
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		plugin.unloadPlayer(event.getPlayer());
	}
	
	@Override
	public void onPlayerKick(PlayerKickEvent event)
	{
		plugin.unloadPlayer(event.getPlayer());
	}
				 
}
