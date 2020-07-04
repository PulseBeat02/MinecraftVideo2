package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.audio;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import uk.co.caprica.vlcj.player.base.MediaApi;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

public class AudioManager {

	protected Process process;

	public AudioManager(String youtubeLink, File YOUTUBE_DL, File dir) {
		
		Bukkit.getLogger().info(ChatColor.GOLD
				+ "Preparing to Download MP3 File from Youtube Link");
		
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
							
							MediaApi mediaPlayer = new MediaApi(new MediaPlayer());
//							mediaPlayer.playMedia( mrl, "sout=#transcode{vcodec=none,acodec=vorbis}:standard{dst=" + output.getAbsolutePath() + ",mux=ogg,access=file}");
							
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
