package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.api.DummyPacketHandler;
import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.api.PacketHandler;
import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.tinyprotocol.TinyProtocol;
import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.util.ReflectionUtil;
import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.video.EntityNameCallback;
import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.video.ItemFrameCallback;
import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.video.VideoPlayer;

import io.netty.channel.Channel;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

public class MinecraftVideo extends JavaPlugin {
	private static MinecraftVideo INSTANCE;

	TinyProtocol protocol;
	PacketHandler handler;

	VideoPlayer player;
	ItemFrameCallback callback;
	EntityNameCallback entityCallback;
	Entity[] stands;

	@Override
	public void onEnable() {
		INSTANCE = this;

		handler = ReflectionUtil.getNewPacketHandlerInstance();
		if (handler == null) {
			getLogger().severe(ReflectionUtil.VERSION + " is not supported!");
			handler = new DummyPacketHandler();
		}

		protocol = new TinyProtocol(this) {
			@Override
			public Object onPacketOutAsync(Player player, Channel channel, Object packet) {
				return handler.onPacketInterceptOut(player, packet);
			}

			@Override
			public Object onPacketInAsync(Player player, Channel channel, Object packet) {
				return handler.onPacketInterceptIn(player, packet);
			}
		};

		JetpImageUtil.init();

		new MediaPlayerFactory();

		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
	}

	protected void panic() {
		// Uh oh, stop all the things just in case
		if (player != null) {
			player.release();
			player = null;
			callback = null;
			entityCallback = null;
			killEntities(stands);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			if (player != null) {
				if (args[0].equalsIgnoreCase("stop")) {
					player.stop();
					sender.sendMessage("Stopped the Video");
				} else if (args[0].equalsIgnoreCase("pause")) {
					player.pause();
					sender.sendMessage("Paused the Video");
				} else if (args[0].equalsIgnoreCase("play")) {
					sender.sendMessage("Playing the Video");
					player.resume();
				} else if (args[0].equalsIgnoreCase("release")) {
					sender.sendMessage("Killing Entities");
					player.release();
					player = null;
					callback = null;
					entityCallback = null;
					killEntities(stands);
				}
			}
		} else if (args.length == 2) {
			if (callback != null) {
				if (args[0].equalsIgnoreCase("setmap")) {
					int mapId = Integer.parseInt(args[1]);
					sender.sendMessage("Set map to " + mapId);
				} else if (args[0].equalsIgnoreCase("setdim")) {
					String[] dims = args[1].split(":");
					int width = Integer.parseInt(dims[0]);
					int height = Integer.parseInt(dims[1]);

					callback.setWidth(width);
					callback.setHeight(height);

					sender.sendMessage(String.format("Set map dimensions to %dx%d", width, height));
				}
			}
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("load")) {
				String url = args[1];
				String[] dims = args[2].split(":");
				int width = Integer.parseInt(dims[0]);
				int height = Integer.parseInt(dims[1]);

				if (player != null) {
					player.release();
					callback = null;
					entityCallback = null;
					killEntities(stands);
				}

				callback = new ItemFrameCallback(null, 0, 5, 5, width, 0);
				player = new VideoPlayer(url, width, height, callback::send);
				player.start();
			} else if (args[0].equalsIgnoreCase("entityload")) {
				if (sender instanceof Player) {
					Player pl = (Player) sender;
					String url = args[1];
					String[] dims = args[2].split(":");
					int width = Integer.parseInt(dims[0]);
					int height = Integer.parseInt(dims[1]);

					if (player != null) {
						player.release();
						callback = null;
						entityCallback = null;
						killEntities(stands);
					}

					stands = spawnClouds(pl.getLocation(), height);

					// 4:3 111:83
					// 16:9 128:72

					entityCallback = new EntityNameCallback(null, stands, width, 40);
					player = new VideoPlayer(url, width, height, entityCallback::send);
					player.start();
				}
			}
		}
		return true;
	}

	public Entity[] spawnClouds(Location location, int height) {
		Entity[] ents = new Entity[height];

		Location spawn = location.clone();
		for (int i = height - 1; i >= 0; i--) {
			AreaEffectCloud cloud = (AreaEffectCloud) spawn.getWorld().spawnEntity(spawn, EntityType.AREA_EFFECT_CLOUD);
			ents[i] = cloud;

			cloud.setInvulnerable(true);
			cloud.setDuration(999999);
			cloud.setDurationOnUse(0);
			cloud.setRadiusOnUse(0);
			cloud.setRadius(0);
			cloud.setRadiusPerTick(0);
			cloud.setReapplicationDelay(0);
			cloud.setCustomNameVisible(true);
			cloud.setCustomName(StringUtils.repeat("-", height));
			cloud.setGravity(false);

			spawn.add(0, .225, 0);
		}

		return ents;
	}

	public void killEntities(Entity[] entities) {
		if (entities != null) {
			for (Entity entity : entities) {
				entity.remove();
			}
		}
	}

	public TinyProtocol getProtocol() {
		return protocol;
	}

	public PacketHandler getHandler() {
		return handler;
	}

	public static MinecraftVideo getInstance() {
		return INSTANCE;
	}

}




