package com.KoryuObihiro.bukkit.loftjump;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public enum LoftJumpEffect 
{	
	//TODO Read below.
	//Current attributes of any jump that are planned:
	//-Item Consumption
	//-Jump type:
		//-X-relative
			// X power (multiply or add?)
	        //
		//-XY-relative
			// X power (multiply or add?)
			// Y power (multiply or add?)
			// Distribute unused X power to Y?
	
	LONG_JUMP(200), 
	HIGH_JUMP(200),
	CUSTOM_JUMP(150);
	
	private final int defaultPower;
	private LoftJumpEffect(int maxPower)
	{
		this.defaultPower = maxPower;
	}
	public int getDefaultAttribute(){ return defaultPower;}
	
	public static LoftJumpEffect matchEffect(String string)
	{
		for(LoftJumpEffect effect : LoftJumpEffect.values())
			if(effect.name().equalsIgnoreCase(string))
				return effect;
		return null;
	} 
	
	public void applyEffect(Player player, int input)
	{
		double power = input/100;
		Vector vector = null;
		switch(this)
		{
				case LONG_JUMP:
					vector = new Vector(player.getVelocity().getX(), power, player.getVelocity().getZ());
					break;
				case HIGH_JUMP:
					vector = new Vector(0, power, 0);
					break;
				case CUSTOM_JUMP:
					vector = new Vector(player.getVelocity().getX() * player.getVelocity().getX()/Math.abs(player.getVelocity().getX())* 1.2 , 1D, player.getVelocity().getZ() * player.getVelocity().getZ()/Math.abs(player.getVelocity().getZ())* 1.2 );
					break;
		}
		if(vector != null)
			player.setVelocity(vector);
	}
}
