package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.util;

import org.bukkit.Bukkit;

import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.api.PacketHandler;

public class ReflectionUtil {
public static final String VERSION;
	
	static {
		VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	}
	
	public static PacketHandler getNewPacketHandlerInstance() {
		try {
			Class< ? > clazz = Class.forName( "com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.implementation." + VERSION + ".NMSHandler" );
			return ( PacketHandler ) clazz.newInstance();
		} catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
			e.printStackTrace();
			return null;
		}
	}
}
