package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.screen;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;

import net.md_5.bungee.api.ChatColor;

public class BuildScreen implements CommandExecutor {
	
	public static ItemStack wand = new ItemStack(Material.STICK);
	public static ItemMeta meta = wand.getItemMeta();

	public static Location firstLocation;
	public static Location secondLocation;

	public static Material mat;

	public static boolean checkMaterial = false;

	@EventHandler
	public void Place(BlockPlaceEvent event) {

		if (checkMaterial) {

			Block b = event.getBlockPlaced();
			mat = b.getType();

			event.getPlayer().sendMessage(ChatColor.GOLD + "SELECTED BLOCK: " + b.getType().name());

			b.setType(Material.AIR);
			checkMaterial = false;

		}

	}

	@SuppressWarnings("deprecation")
	public MapMeta[] getMaps(int length, int width) {

		int AREA = length * width;

		MapMeta[] maps = new MapMeta[AREA];

		for (int i = 0; i < maps.length; i++) {

			ItemStack stack = new ItemStack(Material.MAP);
			MapMeta meta = (MapMeta) stack.getItemMeta();
			meta.setMapId(i);

			maps[i] = meta;

		}

		return maps;

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (sender instanceof Player) {

			Player p = (Player) sender;

			if (args.length == 1) {

				if (args[0].equalsIgnoreCase("buildscreen")) {

					String[] sizes = args[1].split(":");

					if (sizes.length == 2) {

						if (firstLocation != null && secondLocation != null && mat != null) {

							int length = Integer.parseInt(sizes[0]);
							int width = Integer.parseInt(sizes[1]);

							MapMeta[] maps = getMaps(length, width);

							int topBlockX = (firstLocation.getBlockX() < secondLocation.getBlockX()
									? secondLocation.getBlockX()
									: firstLocation.getBlockX());
							int bottomBlockX = (firstLocation.getBlockX() > secondLocation.getBlockX()
									? secondLocation.getBlockX()
									: firstLocation.getBlockX());

							int topBlockY = (firstLocation.getBlockY() < secondLocation.getBlockY()
									? secondLocation.getBlockY()
									: firstLocation.getBlockY());
							int bottomBlockY = (firstLocation.getBlockY() > secondLocation.getBlockY()
									? secondLocation.getBlockY()
									: firstLocation.getBlockY());

							int topBlockZ = (firstLocation.getBlockZ() < secondLocation.getBlockZ()
									? secondLocation.getBlockZ()
									: firstLocation.getBlockZ());
							int bottomBlockZ = (firstLocation.getBlockZ() > secondLocation.getBlockZ()
									? secondLocation.getBlockZ()
									: firstLocation.getBlockZ());

							if (!isFlat(topBlockX - bottomBlockX, topBlockY - bottomBlockY, topBlockZ - bottomBlockZ)) {

								sender.sendMessage(ChatColor.DARK_RED + "The screen isn't flat!");
								return true;

							}

							int index = 0;

							for (int x = bottomBlockX; x <= topBlockX; x++) {

								for (int z = bottomBlockZ; z <= topBlockZ; z++) {

									for (int y = bottomBlockY; y <= topBlockY; y++) {

										Block b = p.getWorld().getBlockAt(x, y, z);
										b.setType(mat);
										setMap(p, b, maps[index]);

										index++;

									}

								}

							}

						} else if (firstLocation == null || secondLocation == null) {

							sender.sendMessage(ChatColor.DARK_RED + "One of the locations aren't set properly yet!");

						} else if (mat == null) {

							sender.sendMessage(ChatColor.DARK_RED + "The screen material hasn't been specified yet!");

						}

					}

				} else if (args[0].equalsIgnoreCase("wand")) {

					ArrayList<String> lore = new ArrayList<>();
					lore.add("Use this stick to create a screen!");
					lore.add("LEFT Click - Set First Position");
					lore.add("RIGHT Click - Set Second Position");
					
					
					meta.addEnchant(Enchantment.DAMAGE_UNDEAD, 1, true);
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					meta.setLore(lore);
					
					wand.setItemMeta(meta);

					p.getInventory().addItem(wand);

					checkMaterial = true;

					p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Banana Gods Have Sent You an Offering!");
					p.sendMessage(
							ChatColor.GOLD + "The next block you place will be the material used for the screen.");

				} else if (args[0].contains("map:")) {

					int mapID = Integer.parseInt(args[0].split(":")[1]);

					if (mapID < 0) {

						p.sendMessage(ChatColor.GOLD
								+ "The Map ID isn't valid. Please specify an ID between 0 - 4,294,967,296.");
						return true;

					}

					ItemStack stack = new ItemStack(Material.MAP);
					
					MapMeta meta = (MapMeta) stack.getItemMeta();
					meta.setMapId(mapID);

					stack.setItemMeta(meta);

					p.getInventory().addItem(stack);

					p.sendMessage(ChatColor.GOLD + "Given you the map for ID: " + mapID);

				}

			}

		} else {

			sender.sendMessage(ChatColor.DARK_RED + "You must be a player to use this command!");

		}

		return true;
	}

	public static void setMap(Player p, Block b, MapMeta map) {

		ItemMeta meta = (ItemMeta) map;
		ItemStack stack = new ItemStack(Material.MAP, 1);
		stack.setItemMeta(meta);

		Location loc = b.getLocation();
		loc.setY(loc.getY() - 1);

		ItemFrame itemFrame = (ItemFrame) p.getWorld().spawnEntity(loc, EntityType.ITEM_FRAME);
		itemFrame.setItem(stack);

	}

	public static boolean isFlat(int l, int w, int h) {

		return l == 1 || w == 1 || h == 1;

	}

	@EventHandler
	public static void onClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();

		if (item == null) {
			return;
		}

		if (item.equals(wand)) {
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {

				firstLocation = event.getClickedBlock().getLocation();
				player.sendMessage("First Position Set At: " + (int) firstLocation.getX() + ", "
						+ (int) firstLocation.getY() + ", " + (int) firstLocation.getZ());

			} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

				secondLocation = event.getClickedBlock().getLocation();
				player.sendMessage("Second Position Set At: " + (int) secondLocation.getX() + ", "
						+ (int) secondLocation.getY() + ", " + (int) secondLocation.getZ());

			}
		}
	}

}
