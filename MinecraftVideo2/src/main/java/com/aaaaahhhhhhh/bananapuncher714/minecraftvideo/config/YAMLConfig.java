package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.config;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class YAMLConfig {

	private File configuration;
	private File pluginDataFolder;

	private FileConfiguration config;

	private String name;

	public YAMLConfig() {

		StringBuilder fileName = new StringBuilder();
		fileName.append("MinecraftVideo.yml");
		this.name = fileName.toString();

		configuration = new File(pluginDataFolder, this.name);
		config = YamlConfiguration.loadConfiguration(configuration);

	}

	public void createConfig() throws IOException {

		if (!configuration.exists()) {

			if (!this.pluginDataFolder.exists()) {

				this.pluginDataFolder.mkdir();
			}

			configuration.createNewFile();

		}

	}

	public void addDefaults() throws IOException {
		
		this.config.set("local-host", false);
		this.config.set("server-ip", "");
		this.config.set("server-port", 6911);
		this.config.set("frame-count", false);
		this.config.set("time-space", false);
		this.config.set("background-image", "");
		this.config.set("resourepack-image", "");
		this.config.set("map-ids", IntStream.range(0, 24).toArray());
		this.config.set("save-resources", false);
		this.config.set("max-frames", 30);
		this.config.save(configuration);

	}
}
