package domain;

public class Playlist {
	
	private String id;
	private String name;
	private int trackAmount;
	
	
	public Playlist(String id, String name, int trackAmount) {
		super();
		this.id = id;
		this.name = name;
		this.trackAmount = trackAmount;
	}


	public String getID() {
		return id;
	}


	public String getName() {
		return name;
	}


	public int getTrackAmount() {
		return trackAmount;
	}
	
	
	@Override
	public String toString() {
		return name;
	}
	
	
	
	

}
