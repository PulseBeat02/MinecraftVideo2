package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.audio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;

public class YoutubeDLLInstallation {

	private static File YOUTUBE_DL;

	public static boolean setDir(File baseDir) throws IOException {

		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			YOUTUBE_DL = new File(baseDir, "youtube-dl.exe");
		} else {
			YOUTUBE_DL = new File(baseDir, "youtube-dl");
		}
		baseDir.mkdirs();

		if (!YOUTUBE_DL.exists()) {

			String url = "https://yt-dl.org/downloads/latest/youtube-dl";
			
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				url = "https://yt-dl.org/latest/youtube-dl.exe";
			}
			YOUTUBE_DL.getParentFile().mkdirs();

			URL website = new URL(url);
			HttpURLConnection httpConnection = (HttpURLConnection) (website.openConnection());
			long completeFileSize = httpConnection.getContentLength();

			BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
			FileOutputStream fos = new FileOutputStream(YOUTUBE_DL);
			BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
			
			byte[] data = new byte[1024];
			long downloadedFileSize = 0;
			int x = 0;
			while ((x = in.read(data, 0, 1024)) >= 0) {
				downloadedFileSize += x;

				// calculate progress
				final int percent = (int) ((((double) downloadedFileSize) / ((double) completeFileSize))
						* 100000d);
				
				Bukkit.getLogger().info(ChatColor.GOLD + "Downloading Youtube-DL: " + percent + "% done");

				bout.write(data, 0, x);
				
			}
			bout.close();
			in.close();

			return false;

		}
		
		return true;
	}

}
