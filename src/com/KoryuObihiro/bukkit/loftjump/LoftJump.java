package com.KoryuObihiro.bukkit.loftjump;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
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
 * @author Erich Gubler, Kylie Estrada
 *
 */
public class LoftJump extends JavaPlugin{
	private final LoftJumpPlayerListener playerListener = new LoftJumpPlayerListener(this);
	private final LoftJumpEntityListener blockListener = new LoftJumpEntityListener(this);
	public final HashMap<Player, ArrayList<Block>> LoftJumpers = new HashMap<Player, ArrayList<Block>>();
	public final HashMap<Player, ArrayList<Block>> freeMen = new HashMap<Player, ArrayList<Block>>();
	public HashMap<World, LoftJumpConfiguration> configs = new HashMap<World, LoftJumpConfiguration>();
	public static Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler Permissions = null;
	
	//DEFAULT SETTINGS
	
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
		
		for(World world : getServer().getWorlds())
		{
			configs.put(world, new LoftJumpConfiguration(world, this));
			configs.get(world).loadSettings(this.getConfiguration());
		}
	}
	
	@Override
	public void onDisable() 
	{
		//TODO Deregister when Bukkit supports
		log.info("["+getDescription().getName()+"] disabled.");	
		configs.clear();
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
					sendUsage(player);
					return true;
				}
				
				//handle player usage
				sendUsage(player);
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
							else if(hasPermission(player, "loftjump.toggle.others"))
								toggleUsage(playerMatch, true, player);
							else player.sendMessage(ChatColor.RED + "[LoftJump] You don't have permission to toggle others.");
							return true;
						}
						else
						{
							if(player == null) log.info("Error: Couldn't find matching player substring.");
							else player.sendMessage(ChatColor.RED + "[LoftJump] Couldn't find matching player name.");
							return true;
						}
					}
				}
				else if(args[0].equalsIgnoreCase("free") || args[0].equalsIgnoreCase("f"))
				{
					
					if(args.length == 1)
					{
						if(player == null)
						{
								log.info("Error: player not specified.");
								return true;
						}
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
							else if(hasPermission(player, "loftjump.free.others"))
								toggleFreedom(playerMatch, true, player);
							else player.sendMessage(ChatColor.RED + "[LoftJump] You don't have permission to toggle others.");
							return true;
						}
						else
						{
							if(player == null) log.info("Error: Couldn't find matching player substring.");
							else player.sendMessage(ChatColor.RED + "[LoftJump] Couldn't find matching player name.");
							return true;
						}
					}
				}
				else if(args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("c"))
				{
					if(args.length == 1)
					{
						if(player == null)
						{
								log.info("Error: world not specified.");
								return true;
						}
						else if(hasPermission(player, "loftjump.check")) 
						{
							sendWorldConfig(player, player.getWorld());
						}
						else player.sendMessage(ChatColor.RED + "[LoftJump] You don't have access to that command.");
						return true;
					}
					else if(args.length == 2)
					{
						World worldMatch = null;
						for(World temp : getServer().getWorlds())
							for(int i = 0; i < (temp.getName().length() - args[1].length() - 1); i++)
								if(args[1].equalsIgnoreCase(temp.getName().substring(i, i + args[1].length())))
								{
									worldMatch = temp;
									break;
								}
						
						if(worldMatch != null)
						{
							if(player == null) return sendWorldConfig(player, worldMatch);
							else if(hasPermission(player, "loftjump.check.others"))
								sendWorldConfig(player, worldMatch);
							else player.sendMessage(ChatColor.RED + "[LoftJump] You don't have permission to check other worlds.");
							return true;
						}
						else
						{
							if(player == null) log.info("Error: Couldn't find matching world substring.");
							else player.sendMessage(ChatColor.RED + "[LoftJump] Couldn't find matching world name.");
							return true;
						}
					}
				}
				else if(args[0].equalsIgnoreCase("reload"))
				{
					if(player == null) return reloadConfigs(player);
					else if(hasPermission(player, "loftjump.reload"))
						return reloadConfigs(player);
					player.sendMessage(ChatColor.RED + "[LoftJump] You don't have access to that command.");
				}
			}
		}
		return sendUsage(player);
	}	

	private boolean sendWorldConfig(Player player, World world) 
	{
		LoftJumpConfiguration thisConfig = configs.get(world);
		if(player != null)
		{
			player.sendMessage(ChatColor.GREEN + "LoftJump status:" + 
					(LoftJumpers.containsKey(player)?("on"):(ChatColor.DARK_RED + "off")) +
					(freeMen.containsKey(player)?("(item consumption off)"):("")));
		    player.sendMessage(ChatColor.YELLOW + "LoftJump settings for world " + ChatColor.DARK_PURPLE + world.getName());
		 
		 //holdMaterial   
		    if(!thisConfig.get_use_onByDefault())
		    	player.sendMessage(ChatColor.YELLOW + "holdMaterial_use: " 
		    		+ ChatColor.DARK_RED + "false");
		    else player.sendMessage(ChatColor.YELLOW + "holdMaterial: " 
		    		+ ChatColor.AQUA + thisConfig.get_HoldMaterial().name());
		
		//consumeMaterial    
		    player.sendMessage(ChatColor.YELLOW + "consumeMaterial: " 
					+ ChatColor.AQUA + thisConfig.get_ConsumeMaterial().name());
		
		//cost
		    player.sendMessage(ChatColor.YELLOW + "Cost/half heart: " 
					+ ChatColor.AQUA + thisConfig.get_Cost());
		    
		//use_onByDefault   
		    player.sendMessage(((thisConfig.get_use_onByDefault())
		    	?(ChatColor.YELLOW + "use_onByDefault: " + ChatColor.GREEN + thisConfig.get_use_onByDefault())
		    	:(ChatColor.YELLOW + "use_onByDefault: " + ChatColor.DARK_RED + thisConfig.get_use_onByDefault())));
		    
		//free_onByDefault
		    player.sendMessage((thisConfig.get_free_onByDefault())
		    	?(ChatColor.YELLOW + "free_OnByDefault: " + ChatColor.GREEN + thisConfig.get_free_onByDefault())
		    	:(ChatColor.YELLOW + "free_onByDefault: " + ChatColor.DARK_RED + thisConfig.get_free_onByDefault()));
		}
		else
		{
			log.info("[LoftJump] Settings for world " + world.getName() + ":" +
					((thisConfig.get_HoldMaterial_use())
							?("\nholdMaterial: " + thisConfig.get_HoldMaterial())
							:("\nholdMaterial_use: " + thisConfig.get_HoldMaterial_use())) +
					"\nconsumeMaterial: " + thisConfig.get_ConsumeMaterial() +
					"\ncost: " + thisConfig.get_Cost() +
					"\nuse_onByDefault: " + thisConfig.get_use_onByDefault() +
					"\nfree_onByDefault: " + thisConfig.get_free_onByDefault());
		}
	    
	    return true;
	}

	private boolean sendUsage(Player player) 
	{
		if(player != null)
		{
			player.sendMessage(ChatColor.LIGHT_PURPLE + "LoftJump commands: ");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "/loftjump (alias /lj) - brings up this help message");
			if(hasPermission(player, "loftjump.toggle")) player.sendMessage(ChatColor.LIGHT_PURPLE + "/loftjump toggle [player] (alias /lj t) - toggle LoftJump");
			if(hasPermission(player, "loftjump.free")) player.sendMessage(ChatColor.LIGHT_PURPLE + "/loftjump free [player] (alias /lj f) - toggle item consume");
			if(hasPermission(player, "loftjump.check")) player.sendMessage(ChatColor.LIGHT_PURPLE + "/loftjump check [world] (alias /lf c) - check world config");
			if(hasPermission(player, "loftjump.reload")) player.sendMessage(ChatColor.LIGHT_PURPLE + "/loftjump reload - reload file configuration");
		}
		else
		{
			log.info("[LoftJump] Commands:\n" +
					"loftjump (alias lj) - brings up this help message\n" +
					"loftjump toggle (player) (alias lj t) - toggle LoftJump on a player\n" +
					"loftjump free (player) (alias lj f) - toggle item consume on a player\n" +
					"loftjump check (world) (alias lf c) - check config for a world\n" +
					"loftjump reload - reload file configuration");
		}
		
		
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
		  else player.sendMessage(ChatColor.RED + "[LoftJump] You don't have permission to do that.");
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
			  else player.sendMessage(ChatColor.RED + "[LoftJump] You don't have permission to do that.");
		  }
		  else
		  {
			  sender.sendMessage(ChatColor.RED + ((sender != null)
					?"Error: That person does not have Loftjump enabled."
					:"Error: You do not have LoftJump enabled."));
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
				log.info("[LoftJump] Item consumption for " + player.getName() + " enabled.");
			} 
			else 
			{
				this.freeMen.put(player, null);
				player.sendMessage(ChatColor.GREEN + "LoftJump item consumption disabled.");
				log.info("[LoftJump] Item consumption for " + player.getName() + " disabled.");
			}
		  return true;
	  }
	  
//////////////////// LOFTJUMP EXECUTION /////////////////////////
	  
	public void tryLoftJump(Player player, EntityDamageEvent event)
	{
		LoftJumpConfiguration thisConfig = getConfig(event.getEntity().getWorld());
		int cost = thisConfig.get_Cost();
		Material toConsume = thisConfig.get_ConsumeMaterial();
		Material holdMe = thisConfig.get_HoldMaterial();
		
		if(!player_Enabled(player) || !hasPermission(player, "loftjump.use")) return;
		//get information about the player's held item, check against the settings for that world
		ItemStack presumablyFeathers = player.getItemInHand();
		int damage = event.getDamage();
		if(!thisConfig.get_HoldMaterial_use() || presumablyFeathers.getType().equals(holdMe))
		{
			//count the number of consumable items in the players inventory
			int totalConsumables = 0;
			for (ItemStack item : player.getInventory().getContents())
			{
				if (item == null) continue;//have to do this for recent builds of CB
				else if(toConsume.equals(item.getType()))
						totalConsumables += item.getAmount();
			}
			
			//do some damage calculation
			int netDamage = damage * cost - totalConsumables;			
			if(netDamage >= 0 && !player_isFree(player))//unless they have the right perm
				event.setDamage(netDamage); //For technical reasons, we still want the fall event to continue after the damage is handled.
			else event.setDamage(0); //obviously
			
			//subtract the necessary feathers...unless they don't need to. :P
			if(!player_isFree(player)) 
				player.getInventory().removeItem(new ItemStack(toConsume, damage * cost));
			
			/*
			player.sendMessage("Damage incurred: " + damage);
			player.sendMessage("Damage cost: " + settings_costPerDamage);
			player.sendMessage("Feathers you have: " + totalConsumables);
			player.sendMessage("Net damage: " + netDamage);
			*/ //Some debugging stuff. :D
		}
		return;
	}

private LoftJumpConfiguration getConfig(World world) 
{
		if(configs.containsKey(world))
		{
			return configs.get(world);
		}
		else if(loadConfig(world))
				return configs.get(world);
		else
			return new LoftJumpConfiguration();
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

	  
	public boolean loadConfig(World world)
	{
		if(!configs.containsKey(world) && world != null)
		{
			configs.put(world, new LoftJumpConfiguration(world, this));
			configs.get(world).loadSettings(this.getConfiguration());
			return true;
		}
		else return false;
	}
	
	public boolean reloadConfigs(Player player) {return reloadConfigs(player, true);}
	public boolean reloadConfigs(Player player, boolean printToConsole) 
	{
		configs.clear();
		for(World world : getServer().getWorlds())
		{
			configs.put(world, new LoftJumpConfiguration(world, this));
			configs.get(world).loadSettings(this.getConfiguration());
		}
		if(player == null)
		{
			if(printToConsole) log.info("[LoftJump] World configuration reloaded.");
		}
		else player.sendMessage(ChatColor.GREEN + "[LoftJump] World configuration reloaded.");
		
		return true;
	}
	  
}
