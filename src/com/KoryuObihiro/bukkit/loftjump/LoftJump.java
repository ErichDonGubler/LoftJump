package com.KoryuObihiro.bukkit.loftjump;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * "LoftJump" for Bukkit
 * 
 * @author Erich Gubler
 *
 */
public class LoftJump extends JavaPlugin
{	//FIXME splayer.getJumpKey() For Spout. :D
	public static Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler Permissions = null;
	public Configuration config;
	private final LoftJumpPlayerListener playerListener = new LoftJumpPlayerListener(this);
	private final LoftJumpEntityListener blockListener = new LoftJumpEntityListener(this);
	
	public HashMap<String, LoftJumpPlayerConfiguration> playerConfigs = new HashMap<String, LoftJumpPlayerConfiguration>();
	
////////////////////////// INITIALIZATION ///////////////////////////////
	@Override
	public void onEnable() 
	{
		//attempt to find permissions
		Plugin test = getServer().getPluginManager().getPlugin("Permissions");
		if (test != null)
		{
			LoftJump.Permissions = ((Permissions)test).getHandler();
			log.info("["+getDescription().getName()+"] " + this.getDescription().getVersion() + " enabled [Permissions v" + test.getDescription().getVersion() + " active]");
		}
		else log.info("[" + getDescription().getName() + "] " + this.getDescription().getVersion()  + " enabled [Permissions not found]");
		
		//register plugin-related stuff with the server's plugin manager
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, blockListener, Event.Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
		
		config = this.getConfiguration();
		if(!(new File("plugins\\LoftJump", "config.yml")).exists())
			config.save();
		config.load();
		
		for(Player player : getServer().getOnlinePlayers())
			loadPlayer(player);
	}
	
	@Override
	public void onDisable() 
	{
		//TODO Deregister when Bukkit supports
		log.info("["+getDescription().getName()+"] disabled.");
		playerConfigs.clear();
	}
	
///////////////////// COMMAND HANDLING //////////////////////////////
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		Player player = null;
		if (label.equalsIgnoreCase("LoftJump") || label.equalsIgnoreCase("lj"))
		{
			if(sender instanceof Player)
				player = (Player)sender;
			if (args.length == 0)
			{
				//handle external commands like console, IRC, etc.
				if(player == null)
				{
					sendUsage(player);
					return true;
				}
				//handle player usage
				sendUsage(player);
				return true;
			}
			else if(args.length >= 0)
			{
				if(args.length == 2)
				{
					if(args[0].equalsIgnoreCase("bind"))
					{//TODO
						LoftJumpEffect effect = LoftJumpEffect.matchEffect(args[1]);
						if(effect != null)
						{
							loadPlayer(player).bind(player.getItemInHand().getType(), LoftJumpEffect.CUSTOM_JUMP);
							player.sendMessage("Bound effect " + effect.name() + " to item " + player.getItemInHand().getType().name());
						}
						else
						{
							player.sendMessage("STUPID STUPID STUPID");
						}
					}
				}
				if(args[0].equalsIgnoreCase("reload"))
				{
					//TODO call reload() for player, send return code message to player when done
				}
			}
		}
		return sendUsage(player);
	}	

	private boolean sendUsage(Player player) 
	{

		return true;
	}

	/////////////////// HELPER FUNCTIONS ////////////////////////////
	//check for Permissions
	public static boolean hasPermission(Player player, String permission)
	{
		if (LoftJump.Permissions != null)
		{
			if (LoftJump.Permissions.has(player, permission)) 
				return true;
			return false;
		}
		return player.isOp();
	}
	
	public LoftJumpPlayerConfiguration loadPlayer(Player player) 
	{
		if(!playerConfigs.containsKey(player.getName()))
			playerConfigs.put(player.getName(), new LoftJumpPlayerConfiguration(this, player));
		return playerConfigs.get(player.getName());
	}

	public void unloadPlayer(Player player) 
	{
		if(playerConfigs.containsKey(player.getName()))
			playerConfigs.remove(player.getName());
	}

	public boolean loadedPlayer(Player player){ return playerConfigs.containsKey(player);}
}
