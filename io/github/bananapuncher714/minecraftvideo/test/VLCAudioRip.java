package io.github.bananapuncher714.minecraftvideo.test;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

public class VLCAudioRip {
	private final CountDownLatch completionLatch;

    private final MediaPlayerFactory mediaPlayerFactory;

    private final MediaPlayer mediaPlayer;

    private final String MRL;
    private final File output;
    
    private long time;
    
    public static void main(String[] args) {
    	new NativeDiscovery().discover();

        File base = new File( System.getProperty( "user.dir" ) );
        new VLCAudioRip( "https://www.youtube.com/watch?v=TH0ZXfmjRqE", new File( base, "output.ogg" ) ).rip();
    }

    public VLCAudioRip( String source, File output ) {
    	MRL = source;
    	this.output = output;
    	
        // Create a synchronisation point
        completionLatch = new CountDownLatch(1);

        // Create the media player
        mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
        mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished( MediaPlayer mediaPlayer ) {
                System.out.println( "Rip completed successfully" );
                completionLatch.countDown();
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                System.out.println( "Rip failed" );
                completionLatch.countDown();
            }
        } );
    }

    private void rip() {
        System.out.println("Ripping '" + MRL + "' to '" + output.getAbsolutePath() + "'...");
        mediaPlayer.setPlaySubItems( true );
        time = System.currentTimeMillis();
        mediaPlayer.playMedia( MRL, "sout=#transcode{vcodec=none,acodec=vorbis}:standard{dst=" + output.getAbsolutePath() + ",mux=ogg,access=file}");

        try {
            // Wait here until finished or error
            completionLatch.await();
            System.out.println( "Took " + ( ( System.currentTimeMillis() - time ) / 1000.0  ) + " seconds to complete" );
        } catch(InterruptedException e ) {
        	e.printStackTrace();
        }

        // Clean up
        mediaPlayer.release();
        mediaPlayerFactory.release();
        System.out.println( "DONE" );
    }
}
