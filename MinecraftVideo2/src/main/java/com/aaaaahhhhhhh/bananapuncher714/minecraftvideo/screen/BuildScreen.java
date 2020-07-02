package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.screen;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BuildScreen {

	public static void buildScreen(Player p, int width, int length) {

		Location loc = p.getEyeLocation();
		Vector vec = p.getLocation().getDirection();
		Location front = loc.add(vec);
		
		Material mat = p.getInventory().getItemInMainHand().getType();
		
		int xPos = (int) front.getX();
		int yPos = (int) front.getY();
		
		for (int x = xPos; x < length; x++) {
			
			for (int y = yPos; y < width; y++) {
				
	            Location blockLocation = front.clone();
	            blockLocation.add(x, y, 0);
	            Block block = blockLocation.getBlock();
	            block.setType(mat);

			}

		}

	}

}
