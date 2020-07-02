package io.github.bananapuncher714.minecraftvideo.video.process;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import com.google.common.io.Files;

import io.github.bananapuncher714.minecraftvideo.resoucepack.SoundPackWrapper;
import io.github.bananapuncher714.minecraftvideo.resoucepack.SoundPackWrapper.SoundResource;
import io.github.bananapuncher714.minecraftvideo.util.Util;
import io.github.bananapuncher714.minecraftvideo.video.McVideo;

public class VideoLoader {
	protected String mrl;
	protected final String id;
	protected File baseDir;
	
	public VideoLoader( File baseDir, String id, String mrl ) {
		this.mrl = mrl;
		this.id = id;
		this.baseDir = new File( baseDir + "/" + id );
		this.baseDir.mkdirs();
	}
	
	public String getId() {
		return id;
	}
	
	public File getBaseFolder() {
		return baseDir;
	}
	
	/**
	 * Called before downloading the file, from youtube;
	 * May not be called if the video is not from youtube or is available on disk.
	 * 
	 * @param videoFile The location of the video
	 */
	public void onDownloadStart( File videoFile ) {
	}
	
	/**
	 * Called after downloading a youtube video successfully
	 * 
	 * @param videoFile The location of the video
	 */
	public void onDownloadFinish( File videoFile ) {
	}
	
	/**
	 * Called before starting extraction of the video's audio; May not be called if audio file exists already
	 * 
	 * @param destination The location of the audio output
	 */
	public void onAudioExtractStart( File destination ) {
	}
	
	/**
	 * Called after extracting audio from video file
	 * 
	 * @param audio The location of the audio output
	 */
	public void onAudioExtractFinish( File audio ) {
	}
	
	public McVideo load() {
		File file = new File( mrl );
		// Misleading, not necessarily MP4 format
		File video = new File( baseDir + "/" + id + ".video" );
		if ( file.exists() && !video.exists() ) {
			try {
				Files.move( file, video );
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
		if ( video.exists() ) {
			mrl = video.getAbsolutePath();
		} else if ( Util.isYoutubeURL( mrl ) ) {
			CountDownLatch latch = new CountDownLatch( 1 );
			YoutubeDownloader downloader = new YoutubeDownloader( mrl ) {
				@Override
				public void onFinish( Process process ) {
					latch.countDown();
				}
			};
			File videoFile = new File( baseDir + "/" + id + ".video" );
			mrl = videoFile.getAbsolutePath();
			onDownloadStart( videoFile );
			downloader.download( videoFile );
			try {
				latch.await();
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}
			onDownloadFinish( videoFile );
		}
		SoundPackWrapper resourcePack = null;
		try {
			resourcePack = new SoundPackWrapper( new File( baseDir + "/" + id + ".zip" ) );
		} catch ( IOException e ) {
			e.printStackTrace();
			return null;
		}
		if ( !resourcePack.containsSound( id ) ) {
			File audio = new File( baseDir + "/" + id + ".ogg" );
			if ( !audio.exists() ) {
				AudioExtractor aExt = new AudioExtractor( mrl, audio );
				onAudioExtractStart( audio );
				aExt.extract();
				onAudioExtractFinish( audio );
			}
			resourcePack.addSound( new SoundResource( id, audio, true ), "minecraftvideo", false );
		}
		return new McVideo( id, mrl );
	}
}