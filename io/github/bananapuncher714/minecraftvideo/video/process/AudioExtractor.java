package io.github.bananapuncher714.minecraftvideo.video.process;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

public class AudioExtractor {
	private final CountDownLatch completionLatch;

	private final MediaPlayerFactory mediaPlayerFactory;

	private final MediaPlayer mediaPlayer;

	private final String mrl;
	private final File output;

	public AudioExtractor( String mrl, File output ) {
		this.mrl = mrl;
		this.output = output;

		completionLatch = new CountDownLatch( 1 );

		// Create the media player
		mediaPlayerFactory = new MediaPlayerFactory();
		mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
		mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void finished( MediaPlayer mediaPlayer ) {
				completionLatch.countDown();
			}

			@Override
			public void error( MediaPlayer mediaPlayer ) {
				completionLatch.countDown();
			}
		} );
	}

	public String getMRL() {
		return mrl;
	}
	
	public File getOutput() {
		return output;
	}
	
	public void terminate() {
		completionLatch.countDown();
	}
	
	public void extract() {
		mediaPlayer.playMedia( mrl, "sout=#transcode{vcodec=none,acodec=vorbis}:standard{dst=" + output.getAbsolutePath() + ",mux=ogg,access=file}");

		try {
			completionLatch.await();
		} catch(InterruptedException e ) {
			e.printStackTrace();
		}

		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayerFactory.release();
	}
}
