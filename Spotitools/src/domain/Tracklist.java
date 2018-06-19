package domain;

public class Tracklist {
	
	private Track[] tracks;
	private int top;
	
	public Tracklist(String[] ids) {
		tracks = new Track[ids.length];
		for (int i = 0; i < ids.length; i++) {
			tracks[i] = new Track(ids[i]);
		}
		top = tracks.length;
	}
	
	public Tracklist(int size) {
		tracks = new Track[size];
		top = 0;
	}
	
	public Tracklist(Track t) {
		tracks = new Track[1];
		tracks[0] = t;
		top = 1;
	}
	
	public void add(Track t) {
		tracks[top] = t;
		top++;
	}
	
	public void trim() {
		int i;
		for (i = 0; i < tracks.length; i++) {
			if (tracks[i] == null) {
				break;
			}
		}
		
		//	Empty or full
		if (i == 0 || i == tracks.length) {
			return;
		}
		
		i++;
		Track[] newArray = new Track[i];
		for (int j = 0; j < i; j++) {
			newArray[j] = tracks[j];
		}
		tracks = newArray;
	}
	
	
	public String toUri(boolean asLink) {
		String result = "";
		String separator;
		if (asLink) {
			separator = "%2C";
		}else {
			separator = ",";
		}
		
		for (int i = 0; i < tracks.length; i++) {
			result += tracks[i].toUri(asLink);
			
			if (i < tracks.length - 1) {
				result += separator;
			}
		}
		
		return result;
	}
	
	public Track get(int index) {
		return tracks[index];
	}
	
	
	public int amount() {
		return tracks.length;
	}
	
	
	@Override
	public String toString() {
		String result = "";
		for (Track t : tracks) {
			result += t.toString() + "\n";
		}
		return result;
	}

}
