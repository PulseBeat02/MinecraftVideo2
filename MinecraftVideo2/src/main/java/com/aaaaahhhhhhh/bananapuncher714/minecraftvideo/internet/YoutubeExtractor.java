package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.internet;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.MinecraftVideo;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.VideoDetails;
import com.github.kiulian.downloader.model.YoutubeVideo;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncodingAttributes;
import net.md_5.bungee.api.ChatColor;

public class YoutubeExtractor {

	public static File[] getFiles(Player p, String url) throws Exception {

		File video = downloadVideo(p, url);
		File sound = new File(video.getParentFile() + "/audio.ogg");

		AudioAttributes audio = new AudioAttributes();
		audio.setCodec("libvorbis");
		audio.setBitRate(new Integer(160000));
		audio.setChannels(new Integer(2));
		audio.setSamplingRate(new Integer(44100));

		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat("ogg");
		attrs.setAudioAttributes(audio);

		Encoder encoder = new Encoder();
		encoder.encode(video, sound, attrs);

		return new File[] { video, sound };

	}

	public static File downloadVideo(Player p, String url) throws IOException, YoutubeException {
		
		YoutubeDownloader downloader = new YoutubeDownloader();

		String ID = getVideoId(url);

		if (ID != null) {

			YoutubeVideo video = downloader.getVideo(ID);

			VideoDetails details = video.details();
			p.sendMessage(ChatColor.GOLD + "=====================================");
			p.sendMessage(ChatColor.RED + "Now Playing: " + ChatColor.AQUA + details.title());
			p.sendMessage(ChatColor.RED + "Author: " + ChatColor.AQUA + details.author());
			p.sendMessage(ChatColor.RED + "Rating: " + ChatColor.AQUA + details.averageRating());
			p.sendMessage(ChatColor.RED + "Description: " + ChatColor.AQUA + details.description());
			p.sendMessage(ChatColor.GOLD + "=====================================");

			File outputDir = new File(MinecraftVideo.getInstance().getDataFolder().getAbsolutePath());

			return video.download(video.videoWithAudioFormats().get(0), outputDir, "video", true);

		} else {

			p.sendMessage(ChatColor.DARK_RED
					+ "The Youtube Link provided is invalid. Please make sure the link is properly formatted.");

			return null;

		}

	}

	public static String getVideoId(String url) {

		String pattern = "(?<=watch\\\\?v=|/videos/|embed\\\\/)[^#\\\\&\\\\?]*";

		Pattern compiledPattern = Pattern.compile(pattern);
		Matcher matcher = compiledPattern.matcher(url);

		if (matcher.find()) {

			return matcher.group();

		} else {

			System.out.println("Invalid Youtube URL Found!");

		}

		return null;

	}

}
