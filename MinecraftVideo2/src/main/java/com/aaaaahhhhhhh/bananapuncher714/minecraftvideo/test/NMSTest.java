package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.test;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.craftbukkit.v1_16_R1.util.CraftChatMessage;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R1.ChatComponentText;
import net.minecraft.server.v1_16_R1.ChatHexColor;
import net.minecraft.server.v1_16_R1.IChatBaseComponent;

public class NMSTest {
	static int[] DATA;
	
	public static void main(String[] args) {
		DATA = new int[ 128 ];
		for ( int i = 0; i < DATA.length; i++ ) {
			DATA[ i ] = ThreadLocalRandom.current().nextInt( 0x1000000 );
		}
		
		IChatBaseComponent bComp = bukkitString();
		IChatBaseComponent bananaComp = bananaString();
		System.out.println( "Bukkit" );
		System.out.println( bComp.getClass() );
		System.out.println( bComp.toString().substring( 0, 1024 ) );
		System.out.println( "Banana" );
		System.out.println( bananaComp.getClass() );
		System.out.println( bananaComp.toString().substring( 0, 1024 ) );
		System.out.println( bananaComp.equals( bComp ) );
		System.out.println( bComp.equals( bananaComp ) );
	}
	
	public static IChatBaseComponent bukkitString() {
		StringBuilder builder = new StringBuilder();
		int index = 0;
		for ( int x = 0; x < DATA.length; x++ ) {
			int c = DATA[ index++ ];
			builder.append( ChatColor.of( new Color( c ) ) );
			builder.append( "█" );
		}
		return CraftChatMessage.fromStringOrNull( builder.toString() );
	}

	public static IChatBaseComponent bananaString() {
		ChatComponentText component = new ChatComponentText( "" );
		int index = 0;
		for ( int x = 0; x < DATA.length; x++ ) {
			int c = DATA[ index++ ];
			ChatComponentText p = new ChatComponentText( "█" );
			p.setChatModifier( p.getChatModifier().setColor( ChatHexColor.a( c ) ) );
			component.addSibling( p );
		}
		return component;
	}
}
