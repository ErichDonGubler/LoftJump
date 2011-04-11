package com.KoryuObihiro.bukkit.loftjump;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;


public class LoftJumpPlayerListener extends PlayerListener
{
//Members
	private LoftJump plugin;
	private static Logger log = Logger.getLogger("Minecraft");
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
				&& plugin.hasPermission(event.getPlayer(), "loftjump.use"))
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
	 
	 //grabs a chat when the PlayerChatEvent is triggered and checks for a Basic command
	 /*public void onPlayerCommand(PlayerChatEvent event) 
	 {
		 log.info("called onPlayerCommand()");
		 String command = event.getMessage().toLowerCase();
		 Player player = event.getPlayer();
		 if ( command.startsWith("loftjump") || command.startsWith("lj"))
		 {			
			 plugin.toggleUsage(player);
			 event.setCancelled(true);
		 }
	 }*/
			 
}
