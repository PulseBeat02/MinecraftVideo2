package io.github.bananapuncher714.minecraftvideo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import io.github.bananapuncher714.minecraftvideo.resoucepack.SoundPackWrapper;
import io.github.bananapuncher714.minecraftvideo.resoucepack.SoundPackWrapper.SoundResource;
import io.github.bananapuncher714.minecraftvideo.util.Util;
import io.github.bananapuncher714.minecraftvideo.video.McVideo;

public class VideoCache {
	protected final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	protected final File baseDir;
	protected JsonObject index;
	protected final Map< String, McVideo > videos = new ConcurrentHashMap< String, McVideo >();
	protected final Set< SoundPackWrapper > soundPacks = Collections.synchronizedSet( new HashSet< SoundPackWrapper >() );
	
	public VideoCache( File baseDir ) {
		this.baseDir = baseDir;
		getVideoCacheLocation().mkdirs();
		getSoundCacheLocation().mkdirs();
		
		File indexFile = new File( baseDir + "/index.json" );
		if ( indexFile.exists() ) {
			try {
				index = gson.fromJson( new InputStreamReader( new FileInputStream( indexFile ) ), JsonObject.class );
			} catch ( JsonSyntaxException | JsonIOException | FileNotFoundException e ) {
				e.printStackTrace();
			}
			for ( Entry< String, JsonElement > entry : index.entrySet() ) {
				String id = entry.getKey();
				String mrl = entry.getValue().getAsString();
				McVideo video = new McVideo( id, mrl );
				videos.put( id, video );
			}
		} else {
			index = new JsonObject();
			saveIndex();
		}
		
		String description = "§6Awesome server reource pack";
		File iconPack = new File( baseDir, "pack.png" );
		
		for ( File file : getSoundCacheLocation().listFiles() ) {
			try {
				SoundPackWrapper wrapper = new SoundPackWrapper( file );
				if ( iconPack.exists() ) {
					wrapper.addElement( "pack.png", iconPack, StandardCopyOption.REPLACE_EXISTING );
				}
				wrapper.setDescription( description );
				wrapper.save();
				soundPacks.add( wrapper );
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}
	
	public File getVideoCacheLocation() {
		return new File( baseDir + "/videos" );
	}
	
	public File getSoundCacheLocation() {
		return new File( baseDir + "/sounds" );
	}
	
	public Set< String > getVideoList() {
		Set< String > videos = new HashSet< String >();
		videos.addAll( this.videos.keySet() );
		return videos;
	}
	
	public void registerMcVideo( McVideo video ) {
		videos.put( video.getId(), video );
		index.addProperty( video.getId(), video.getMrl() );
		saveIndex();
		for ( SoundPackWrapper wrapper : soundPacks ) {
			if ( wrapper.containsSound( video.getId() ) ) {
				return;
			}
		}
		addToSoundpack( video );
	}
	
	public McVideo getVideo( String id ) {
		return videos.get( id );
	}
	
	public File getVideoSoundpack( String id ) {
		McVideo video = videos.get( id );
		if ( video == null ) {
			return null;
		}
		return new File( getVideoCacheLocation() + "/" + video.getId() + "/" + video.getId() + ".zip" );
	}
	
	public SoundPackWrapper getUberSoundpack( String id ) {
		for ( SoundPackWrapper wrapper : soundPacks ) {
			if ( wrapper.containsSound( id ) ) {
				return wrapper;
			}
		}
		return null;
	}
	
	protected SoundPackWrapper getByName( String uuid ) {
		for ( SoundPackWrapper wrapper : soundPacks ) {
			if ( wrapper.getFile().getName().equalsIgnoreCase( uuid ) ) {
				return wrapper;
			}
		}
		return null;
	}
	
	public void delete( String id ) {
		videos.remove( id );
		index.remove( id );
		File videoDir = new File( getVideoCacheLocation() + "/" + id );
		if ( videoDir.exists() ) {
			Util.recursiveDelete( videoDir );
		}
		for ( SoundPackWrapper wrapper : soundPacks ) {
			wrapper.removeSound( id );
		}
		saveIndex();
	}
	
	private File getSoundFile( String id ) {
		McVideo video = videos.get( id );
		if ( video == null ) {
			return null;
		}
		return new File( getVideoCacheLocation() + "/" + video.getId() + "/" + video.getId() + ".ogg" );
	}
	
	private void addToSoundpack( McVideo video ) {
		File audioFile = getSoundFile( video.getId() );
		if ( audioFile == null ) {
			return;
		}
		long size = audioFile.length();
		if ( size > 75_000_000 ) {
			throw new IllegalArgumentException( "Audio file for " + video.getId() + " is too large! >75mb" );
		}
		for ( SoundPackWrapper pack : soundPacks ) {
			if ( pack.getFile().length() + size > 75_000_000 ) {
				continue;
			}
			pack.addSound( new SoundResource( video.getId(), audioFile, true ), "minecraftvideo", false );
			return;
		}
		try {
			SoundPackWrapper wrapper = new SoundPackWrapper( new File( getSoundCacheLocation() + "/" + UUID.randomUUID() ) );
			wrapper.addSound( new SoundResource( video.getId(), audioFile, true ), "minecraftvideo", false );
			soundPacks.add( wrapper );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	private void saveIndex() {
		File indexFile = new File( baseDir + "/index.json" );
		try {
			Files.write( Paths.get( indexFile.getAbsolutePath() ), gson.toJson( index ).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
}
