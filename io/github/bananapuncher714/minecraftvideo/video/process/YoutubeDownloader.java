package io.github.bananapuncher714.minecraftvideo.video.process;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import io.github.bananapuncher714.minecraftvideo.MinecraftVideo;
import io.github.bananapuncher714.minecraftvideo.util.Util;

public class YoutubeDownloader {
	private static File YOUTUBE_DL;
	private static final String[] FORMATS = new String[] { "22", "best" };

	public static boolean setDir( File baseDir ) {
		if ( System.getProperty( "os.name" ).toLowerCase().contains( "windows" ) ) {
			MinecraftVideo.log( "Windows OS detected! Using youtube-dl.exe" );
			YOUTUBE_DL = new File( baseDir, "youtube-dl.exe" );
		} else {
			MinecraftVideo.log( "Windows OS not detected! Using youtube-dl" );
			YOUTUBE_DL = new File( baseDir, "youtube-dl" );
		}
		baseDir.mkdirs();
		if ( !YOUTUBE_DL.exists() ) {
			String url = "https://yt-dl.org/downloads/latest/youtube-dl";
			if ( System.getProperty( "os.name" ).toLowerCase().contains( "windows" ) ) {
				url = "https://yt-dl.org/latest/youtube-dl.exe";
			}
			Util.saveFile( url, YOUTUBE_DL );
			return false;
		}
		return true;
	}

	public File getExecutable() {
		return YOUTUBE_DL;
	}

	protected final String url;
	protected Process process;
	
	public YoutubeDownloader( String url ) {
		this.url = url;
	}

	public void onFinish( Process process ) {
	}

	public void onError( Exception exception ) {
	}

	public void terminate() {
		process.destroy();
	}
	
	public void download( File file ) {
		new Thread() {
			@Override
			public void run() {
				for ( String format : FORMATS ) {
					ProcessBuilder builder = new ProcessBuilder( YOUTUBE_DL.getAbsolutePath(), "-f", format, "-o", file.getAbsolutePath(), url );
					builder.redirectOutput( Redirect.INHERIT );
					builder.redirectError( Redirect.INHERIT );
					try {
						process = builder.start();
						process.waitFor();
						if ( file.exists() ) {
							onFinish( process );
							process.destroy();
							return;
						}
						MinecraftVideo.log( "Requested format(" + format + ") for " + url + " not found!" );
						process.destroy();
					} catch ( IOException | InterruptedException e ) {
						onError( e );
					}
				}
			}
		}.start();
	}
}