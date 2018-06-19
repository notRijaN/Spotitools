package domain;

public class Track {
	
	private String id;
	private boolean onlyID;
	
	private String song;
	private String album;
	private String artist;
	
	
	public Track(String id) {
		this.id = id;
		onlyID = true;
	}
	

	public Track(String id, String song, String album, String artist) {
		this.id = id;
		this.song = song;
		this.album = album;
		this.artist = artist;
		onlyID = false;
	}
	
	
	public String getID() {
		return id;
	}
	
	
	@Override
	public String toString() {
		if (onlyID) {
			return id;
		}else {
			return "\"" + song + "\" - " + artist + " (" + album + ")";
		}
	}
	

	public String toUri(boolean asLink) {
		if (asLink) {
			return "spotify%3Atrack%3A" + id;
		}else {
			return "spotify:track:" + id;
		}
	}
	
	
	public boolean same(Track other) {
		return this.id.equals(other.id);
	}
	
	
	public static String toUri(String id, boolean asLink) {
		if (asLink) {
			return "spotify track " + id;
		}else {
			return "spotify:track:" + id;
		}
	}
	
	public boolean isRealTrack() {
		if (id.equals("ad") || id.equals("nosong")) {
			return false;
		}else{
			return true;
		}
	}

}
