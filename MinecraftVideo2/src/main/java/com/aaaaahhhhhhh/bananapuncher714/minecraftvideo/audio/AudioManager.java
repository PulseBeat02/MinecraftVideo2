package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.audio;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.MinecraftVideo;

public class AudioManager {

	protected Process process;

	public AudioManager(String youtubeLink, File YOUTUBE_DL, File dir) throws IOException {

		YoutubeDLLInstallation.installation(MinecraftVideo.getInstance().getDataFolder());

		Bukkit.getLogger().info(ChatColor.GOLD + "Preparing to Download MP3 File from Youtube Link");

		new Thread() {

			@Override
			public void run() {

				for (String format : new String[] { "22", "best" }) {
					ProcessBuilder builder = new ProcessBuilder(YOUTUBE_DL.getAbsolutePath(), "-f", format, "-o",
							dir.getAbsolutePath(), youtubeLink);
					builder.redirectOutput(Redirect.INHERIT);
					builder.redirectError(Redirect.INHERIT);

					try {

						process = builder.start();
						process.waitFor();
						if (dir.exists()) {

							Bukkit.getLogger().info(ChatColor.GOLD
									+ "Downloaded MP3 File from Youtube Link. Proceeding to Conversion.");
							process.destroy();

							// File source = new File("file path");

							// Audio Attributes
							//AudioAttributes audio = new AudioAttributes();
//							audio.setCodec("libmp3lame");
//							audio.setBitRate(128000);
//							audio.setChannels(2);
//							audio.setSamplingRate(44100);

							// Encoding attributes
							//EncodingAttributes attrs = new EncodingAttributes();
//							attrs.setFormat("mp3");
//							attrs.setAudioAttributes(audio);

							// Encode
							//Encoder encoder = new Encoder();
							//encoder.encode(new MultimediaObject(source), target, attrs);

							return;
						}
						Bukkit.getLogger()
								.info(ChatColor.RED + "The Youtube link format is incorrect. Make sure it follows the ["
										+ format + "] format");
						process.destroy();

					} catch (IOException | InterruptedException e) {
						Bukkit.getLogger().info(ChatColor.RED + "Failed to download MP3 File.");
					}
				}
			}
		}.start();

	}

}
