package com.KoryuObihiro.bukkit.loftjump;


import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.logging.Logger;

/**
 * "LoftJump" for Bukkit
 * 
 * @author Erich Gubler
 *
 */
public class LoftJump extends JavaPlugin{
	private final LoftJumpPlayerListener playerListener = new LoftJumpPlayerListener(this);
	private final LoftJumpEntityListener blockListener = new LoftJumpEntityListener(this);
	public final HashMap<Player, ArrayList<Block>> LoftJumpers = new HashMap<Player, ArrayList<Block>>();
	public final HashMap<Player, ArrayList<Block>> freeMen = new HashMap<Player, ArrayList<Block>>();
	public static Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler Permissions = null;
	
	//DEFAULT SETTINGS
	public boolean settings_use_onByDefault = true;
	public boolean settings_free_onByDefault = false;
	public Material settings_ConsumeMaterial = Material.FEATHER;
	public Material settings_HoldMaterial = Material.FEATHER;
	public int settings_costPerDamage = 1;
	
//SETTINGS TODO add changing of settings ingame, perhaps persistent with file read/write
	
////////////////////////// INITIALIZATION ///////////////////////////////
	@Override
	public void onEnable() 
	{
		//attempt to find permissions
		Plugin test = getServer().getPluginManager().getPlugin("Permissions");
		if (test != null)
		{
			LoftJump.Permissions = ((Permissions)test).getHandler();
			log.info("["+getDescription().getName()+"] " + this.getDescription().getVersion() 
					+ " enabled [Permissions v" + test.getDescription().getVersion() + " active]");
		}
		else
			log.info("["+getDescription().getName()+"] " + this.getDescription().getVersion() 
					+ " enabled [Permissions not found]");
		
		//register plugin-related stuff with the server's plugin manager
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, blockListener, Event.Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
		
		//placeholder for loadSettings, currently does nothing TODO
		loadSettings();
	}
	
	@Override
	public void onDisable() 
	{
		//TODO Deregister when Bukkit supports
		log.info("["+getDescription().getName()+"] disabled.");	
	}
	
	public void loadSettings()
	{
		//TODO
		return;
	}
	
///////////////////// COMMAND HANDLING //////////////////////////////
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		Player player = null;
		//debugging
		String tempo = "";
		for(String string : args)
			tempo += " " + string;
		
		if (label.equalsIgnoreCase("LoftJump") || label.equalsIgnoreCase("lj"))
		{
			// §
			if (sender instanceof Player)
				player = (Player)sender;
			if (args.length == 0)
			{
				//handle external commands like console, IRC, etc.
				if(player == null)
				{
						log.info("[LoftJump]Commands:\n" +
								"loftjump (alias /lj) - brings up this help message\n" +
								"loftjump toggle (player) (alias /lj t) - toggle LoftJump on a player\n" +
								"loftjump free (player) (alias /lj f) - toggle item consume on a player");
						return true;
				}
				
				//handle player usage
				sendUsage(player);
				if(hasPermission(player, "loftjump.check")) 
				{
					player.sendMessage(ChatColor.DARK_RED + "Material cost per 1/2-heart damage: " + settings_costPerDamage);
				}
					return true;
			}
			else if(args.length >= 0)
			{
				if(args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("t"))
				{
					if(args.length == 1)
					{
						//handle external commands like console, IRC, etc.
						if(player == null)
						{
								log.info("Error: player not specified.");
								return true;
						}
						return toggleUsage(player);
					}
					else if(args.length == 2)
					{
						//search for a player by substring
						Player playerMatch = null;
						for(Player temp : this.getServer().getOnlinePlayers())
							for(int i = 0; i < (temp.getName().length() - args[1].length() - 1); i++)
								if(args[1].equalsIgnoreCase(temp.getName().substring(i, i + args[1].length())))
								{
									playerMatch = temp;
									break;
								}
						if(playerMatch != null)
						{
							if(player == null)
								return toggleFromConsole(playerMatch);
							else if(hasPermission(player, "loftjump.toggleothers"))
								toggleUsage(playerMatch, true, player);
							else player.sendMessage(ChatColor.RED + "[LoftJump]You don't have permission to toggle others.");
							return true;
						}
						else
						{
							if(player == null) log.info("Error: Couldn't find matching player substring.");
							else player.sendMessage(ChatColor.RED + "[LoftJump]Couldn't find matching player name.");
							return true;
						}
					}
				}
				else if(args[0].equalsIgnoreCase("free") || args[0].equalsIgnoreCase("f"))
				{
					if(args.length == 1)
					{
						return toggleFreedom(player);
					}
					else if(args.length == 2)
					{
						//search for a player by substring
						Player playerMatch = null;
						for(Player temp : this.getServer().getOnlinePlayers())
							for(int i = 0; i < (temp.getName().length() - args[1].length() - 1); i++)
								if(args[1].equalsIgnoreCase(temp.getName().substring(i, i + args[1].length())))
								{
									playerMatch = temp;
									break;
								}
						if(playerMatch != null)
						{
							if(player == null)
								return toggleFreeFromConsole(playerMatch);
							else if(hasPermission(player, "loftjump.freeothers"))
								toggleFreedom(playerMatch, true, player);
							else player.sendMessage(ChatColor.RED + "[LoftJump]You don't have permission to toggle others.");
							return true;
						}
						else
						{
							if(player == null) log.info("Error: Couldn't find matching player substring.");
							else player.sendMessage(ChatColor.RED + "[LoftJump]Couldn't find matching player name.");
							return true;
						}
					}
				}
			}
		}
		return sendUsage(player);
	}
	
	private boolean sendUsage(Player player) 
	{
		player.sendMessage(ChatColor.LIGHT_PURPLE + "LoftJump commands: ");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "/loftjump (alias /lj) - brings up this help message");
		if(hasPermission(player, "loftjump.toggle")) player.sendMessage(ChatColor.LIGHT_PURPLE + "/loftjump toggle [player] (alias /lj t) - toggle LoftJump");
		if(hasPermission(player, "loftjump.free")) player.sendMessage(ChatColor.LIGHT_PURPLE + "/loftjump free [player] (alias /lj f) - toggle item consume");
		return true;
	}
	
////////////////////// PLAYER TOGGLES ////////////////////////
    
	  //toggle this plugin for a player
	  public boolean toggleUsage(Player player){return toggleUsage(player, true, null);}
	  public boolean toggleUsage(Player player, boolean player_notify){return toggleUsage(player, player_notify, null);}
	  public boolean toggleUsage(Player player, boolean player_notify, Player sender) 
	  {
		  if(hasPermission(player, "loftjump.toggle") || sender != null)
		  {
			if (player_Enabled(player)) 
			{
				this.LoftJumpers.remove(player);
				if(player_notify) player.sendMessage(ChatColor.GREEN + "LoftJump disabled.");
				if(sender != null) sender.sendMessage(ChatColor.GREEN 
						+ "LoftJump for " + player.getName() + " disabled.");
			} 
			else 
			{
				this.LoftJumpers.put(player, null);
				if(player_notify) player.sendMessage(ChatColor.GREEN + "LoftJump enabled.");
				if(sender != null) sender.sendMessage(ChatColor.GREEN 
						+ "LoftJump for " + player.getName() + " enabled.");
			}
		  }
		  else player.sendMessage(ChatColor.RED + "[LoftJump]You don't have permission to do that.");
		  return true;
	  }
	  
	//toggle cost associated with LoftJumping
	  public boolean toggleFreedom(Player player){return toggleFreedom(player, true, null);}
	  public boolean toggleFreedom(Player player, boolean player_notify){return toggleFreedom(player, player_notify, null);}
	  public boolean toggleFreedom(Player player, boolean player_notify, Player sender) 
	  {
		  if(player_Enabled(player))
		  {
			  if(hasPermission(player, "loftjump.free") || sender != null)
			  {
				if (player_isFree(player)) 
				{
					this.freeMen.remove(player);
					if(player_notify) player.sendMessage(ChatColor.GREEN + "LoftJump item consumption enabled.");
					if(sender != null) sender.sendMessage(ChatColor.GREEN 
							+ "LoftJump item consumption for " + player.getName() + " enabled.");
				} 
				else 
				{
					this.freeMen.put(player, null);
					if(player_notify) player.sendMessage(ChatColor.GREEN + "LoftJump item consumption disabled.");
					if(sender != null) sender.sendMessage(ChatColor.GREEN 
							+ "LoftJump item consumption for " + player.getName() + " disabled.");
				}
			  }
			  else player.sendMessage(ChatColor.RED + "[LoftJump]You don't have permission to do that.");
		  }
		  else
		  {
			  if(sender != null)
				  sender.sendMessage(ChatColor.RED + "Error: That person does not have Loftjump enabled.");
			  else
				  player.sendMessage(ChatColor.RED + "Error: You do not have LoftJump enabled.");
		  }
		  
		  return true;
	  }
	  
	  private boolean toggleFromConsole(Player player) 
	  {
		  if (player_Enabled(player)) 
			{
				this.LoftJumpers.remove(player);
				player.sendMessage(ChatColor.GREEN + "LoftJump disabled.");
				log.info("[LoftJump] for " + player.getName() + " disabled.");
			} 
			else 
			{
				this.LoftJumpers.put(player, null);
				player.sendMessage(ChatColor.GREEN + "LoftJump enabled.");
				log.info("[LoftJump] for " + player.getName() + " enabled.");
			}
		  return true;
	  }
	
	  private boolean toggleFreeFromConsole(Player player) 
	  {
		  if (player_isFree(player)) 
			{
				this.freeMen.remove(player);
				player.sendMessage(ChatColor.GREEN + "LoftJump item consumption enabled.");
				log.info("[LoftJump]Item consumption for " + player.getName() + " enabled.");
			} 
			else 
			{
				this.freeMen.put(player, null);
				player.sendMessage(ChatColor.GREEN + "LoftJump item consumption disabled.");
				log.info("[LoftJump]Item consumption for " + player.getName() + " disabled.");
			}
		  return true;
	  }
	  
//////////////////// LOFTJUMP EXECUTION /////////////////////////
	  
	public void tryLoftJump(Player player, EntityDamageEvent event)
	{
		if(!player_Enabled(player) || !hasPermission(player, "loftjump.use")) return;
		//get information about the player's held item, check against the settings for that world
		ItemStack presumablyFeathers = player.getItemInHand();
		int damage = event.getDamage();
		if(presumablyFeathers.getType().equals(settings_HoldMaterial))
		{
			//count the number of consumable items in the players inventory
			int totalConsumables = 0;
			for (ItemStack item : player.getInventory().getContents())
			{
				if (item == null) continue;//have to do this for recent builds of CB
				else if (settings_ConsumeMaterial.equals(item.getType()))
						totalConsumables += item.getAmount();
			}
			
			//do some damage calculation
			int netDamage = damage * settings_costPerDamage - totalConsumables;
			if(netDamage >= 0 && !player_isFree(player))//unless they have the right perm
			{
				//For technical reasons, we still want the fall event to continue after the damage is handled.
				event.setDamage(netDamage);
				
				//Since we incurred damage, that can only mean
				// we're out of feathers. :(
				player.getInventory().removeItem(new ItemStack(settings_ConsumeMaterial, damage));
			}
			else 
			{
				event.setDamage(0); //obviously
				//subtract the amount of feathers necessary
				if(!player_isFree(player)) 
					player.getInventory().removeItem(new ItemStack(settings_ConsumeMaterial, damage));
			}
		}
		return;
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

//player_Enabled function
//returns a bool based on whether the inputted player is on the LoftJumpers hashmap or not
public boolean player_Enabled(Player player) {return this.LoftJumpers.containsKey(player);}
public boolean player_isFree(Player player) {return this.freeMen.containsKey(player);}

	  
	  
}
