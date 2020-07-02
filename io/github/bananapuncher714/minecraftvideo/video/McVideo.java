package io.github.bananapuncher714.minecraftvideo.video;

public class McVideo {
	private final String mrl;
	private final String id;
	
	public McVideo( String id, String mrl ) {
		this.id = id;
		this.mrl = mrl;
	}
	
	public String getId() {
		return id;
	}
	
	public String getMrl() {
		return mrl;
	}
}
