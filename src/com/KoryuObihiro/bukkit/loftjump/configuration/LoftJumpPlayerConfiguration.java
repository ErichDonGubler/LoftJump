package com.KoryuObihiro.bukkit.loftjump.configuration;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.KoryuObihiro.bukkit.loftjump.LoftJump;
import com.KoryuObihiro.bukkit.loftjump.LoftJumpEffect;

public class LoftJumpPlayerConfiguration 
{	
	private LoftJump plugin;
	private Player player;
	private ConfigurationNode configNode = null;

	private HashMap<Material, LoftJumpEffect> bindList = new HashMap<Material, LoftJumpEffect>();
	private HashMap<LoftJumpEffect, Integer> typeAttributes = new HashMap<LoftJumpEffect, Integer>();
	
	public LoftJumpPlayerConfiguration(LoftJump plugin, Player player)
	{
		this.plugin = plugin;
		this.player = player;

		configNode = plugin.getConfiguration().getNode("players." + player.getName());
		if(configNode == null) writeDefaults();

		for(LoftJumpEffect effect : LoftJumpEffect.values())
		{
			String materialString = configNode.getString("binds." + effect.name(), "");
			if(!materialString.isEmpty()) 
			{
				Material material = Material.matchMaterial(materialString);
				if(material != null)
					bindList.put(material, effect);
			}
			try
			{
				Integer attribute = configNode.getInt("effects." + effect.name(), 0);
				typeAttributes.put(effect, attribute);
			}
			catch(Exception e)
			{
				LoftJump.log.severe("[LoftJump] Couldn't read player \"" + player.getName() + "\"'s attribute for type \"" + effect.name() + "\"");
				typeAttributes.put(effect, effect.getDefaultAttribute());
			}
		}
	}

	private void writeDefaults() 
	{
		for(LoftJumpEffect effect : LoftJumpEffect.values())
		{
			plugin.config.setProperty("players." + player.getName() + ".binds." + effect.name(), "");
			plugin.config.setProperty("players." + player.getName() + ".effects." + effect.name(), effect.getDefaultAttribute());
		}
		configNode = plugin.config.getNode("players." + player.getName());
		plugin.config.save();
	}
	
	public void save()
	{
		for(LoftJumpEffect effect : LoftJumpEffect.values())
		{
			if(bindList.containsValue(effect))
			{
				Material material = getBoundMaterial(effect);
				if(material != null && bindList.get(material) == effect)
					configNode.setProperty("binds." + effect.name(), material.name());
			}
			else configNode.setProperty("binds." + effect.name(), null);

			if(typeAttributes.containsKey(effect))
				configNode.setProperty("effects." + effect.name(), typeAttributes.get(effect));
		}
		plugin.config.save();
	}

	public void bind(Material material, LoftJumpEffect effect)
	{
		if(material.equals(Material.AIR))
		{
			player.sendMessage(ChatColor.RED + "[LoftJump] Can't bind to your fists. :(");
			return;
		}
		if(bindList.get(material) == effect)
		{
			player.sendMessage(ChatColor.RED + "[LoftJump] This bind already exists!");
			return;
		}
		if(bindList.containsValue(effect))
		{
			Material currentlyBound = null;
			for(Material materialKey : bindList.keySet())
				if(materialKey != null && bindList.get(materialKey) == effect)
					currentlyBound = materialKey;
			player.sendMessage(ChatColor.YELLOW + "Warning: bind of type " + effect.name() +  " already exists for " + currentlyBound.name());
		}
		if(bindList.containsKey(material))
		{
			player.sendMessage(ChatColor.YELLOW + "Warning: overriding existing bind of type \"" + bindList.get(material).name() + "\"");
			bindList.remove(material);
		}
		bindList.put(material, effect);
		player.sendMessage(ChatColor.GREEN + "\"" + effect.name() + "\"-type lightning bound to " + material.name());
		save();
	}
	
	public void unbindAll()
	{
		if(!bindList.keySet().isEmpty())
		{
			configNode.setProperty("binds", null);
			bindList.clear();
			player.sendMessage(ChatColor.GREEN + "Removed all binds.");
			save();
		}
		else player.sendMessage(ChatColor.RED + "[LoftJump] Error: no binds to remove!");
	}
	
	public void unbind(Material material)
	{
		if(bindList.containsKey(material))
		{
			configNode.setProperty("binds." + bindList.get(material).name(), null);
			player.sendMessage(ChatColor.GREEN + "[LoftJump] Removed bind " + material.name() + " for type \"" + bindList.get(material).name() + "\"");
			bindList.remove(material);
			return;
		}
		player.sendMessage(ChatColor.RED + "[LoftJump] No bind found for material " + material.name());
		save();
	}
	
	public void unbind(LoftJumpEffect effect)
	{
		if(bindList.containsValue(effect))
			for(Material material : bindList.keySet())
				if(bindList.get(material).equals(effect))
				{
					bindList.remove(material);
					configNode.setProperty("binds." + effect.name(), null);
					player.sendMessage(ChatColor.GREEN + "[LoftJump] Removed all bind " + material.name() + " for type \"" + effect.name() + "\"");
					return;
				}
		player.sendMessage(ChatColor.RED + "[LoftJump] No bind found for type \"" + effect.name() + "\"");
	}

	public Material getBoundMaterial(LoftJumpEffect effect) 
	{
		for(Material material : bindList.keySet())
			if(material != null && bindList.get(material) == effect)
				return material;
		return null;
	}

	public int applyCushion(int damage) 
	{
		//TODO
		return 0;
	}

	public void fireInteractEvent() 
	{
		if(bindList.containsKey(player.getItemInHand().getType()))
		{
			LoftJumpEffect effect = bindList.get(player.getItemInHand().getType());
			effect.applyEffect(player, typeAttributes.get(effect));
		}
	}	
}
