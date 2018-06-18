package access;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class SpotifyConnector {
	
	private static final String USER_ID = "rijan_";
	private static final String QUEUE_ID = "1OArlu09nNXISAjQTSPAlK";
	
	private static String currentPlaylist = "33nQ8OrHQuIHi3pdPo36rW";
	private static int playlistTop = 0;
	private static int[] order;
	
	private static int current = 0;
	private static int queueTop = 0;
	private static int priority = 0;	// priority == current => no priority
	
	private static String currentSong;

	
	private static URLConnection openConnection(String urlString, String mode, String data) {
		
		HttpsURLConnection uc = null;
		try{
			//	Open connection
			URL url = new URL(urlString);
	        uc = (HttpsURLConnection) url.openConnection();
	        
	        //	Mode (GET, POST, PUT)
	        uc.setRequestMethod(mode);
	        if (mode.equals("POST") || mode.equals("PUT")) {
	        	uc.setFixedLengthStreamingMode(0);
	        }
	        
	        //	Properties
	        uc.setRequestProperty("X-Requested-With", "Curl");
	        uc.setRequestProperty("Accept", "application/json");
	        uc.setRequestProperty("Content-Type", "application/json");
	        uc.setRequestProperty("Authorization", "Bearer BQAOBJzT8YM5vC6WFexBsLjbdvE_YP2SZqDQlrNluhS_E9V-FmxtlHRkjy4Sw5TSd9M2Y5r5mrzr5JU7d5kSl4YpzwP2Na-AfnF9AwfE2Of-OR-EoiadMXgGN8Q7cXlDGlShBNUFk7Yxp2_eoKYXdWZ6r99uERKVJgzMaFk7W94c6G8rc1bKMr7B8kcYvlDmHkxjnCK7pGmeGoldRX9LHrjtlfe5H0uQLT4uIdQKXucl_4UAJlrjeZ6Z1PLxC6g1B-c57mn2qHjaZAKuVcSJPCP5PDM");
	        uc.setDoOutput(true);
	        
	        //	Data (body)
	        if (data != null) {
		        BufferedWriter writer = new BufferedWriter(new PrintWriter(uc.getOutputStream()));
		        writer.write(data);
		        writer.flush();
	        }
	        
	        return uc;
		}catch (IOException e) {
			e.printStackTrace();
			BufferedReader input;
	        String line;
			try {
				input = new BufferedReader(new InputStreamReader(uc.getErrorStream()));
				while ((line = input.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}
	}
	
	
	private static int getSongAmount(String playlistID) {
		URLConnection uc = openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + playlistID + "/tracks?fields=total&limit=1&offset=0", "GET", null);
        BufferedReader input;
        String line;
        int totalTracks = -1;	//Error code
        
		try {
			input = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			while ((line = input.readLine()) != null) {
				if (line.contains("total")) {
					totalTracks = Integer.parseInt(line.substring(line.indexOf(" ", line.indexOf(":")) + 1));
					break;
				}
			}
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
		}
        
        return totalTracks;
	}
	
	
	private static void reorder(int trackIndex, int trackAmount, int place) {
		String data = "{\r\n" + 
    			"  \"range_start\": " + trackIndex + ",\r\n" + 
    			"  \"range_length\": " + trackAmount + ",\r\n" + 
    			"  \"insert_before\": " + place + "\r\n" + 
    			"}";
//		openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + QUEUE_ID + "/tracks", "PUT", data);
		URLConnection uc = openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + QUEUE_ID + "/tracks", "PUT", data);
		logOutput(uc);

	}
	
	
	private static String[] getSongsFromPlaylist(int amount, int offset, String playlistID) {
		try{
			URLConnection uc = openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + playlistID + "/tracks?fields=total%2Citems(track(id))&limit=" + amount + "&offset=" + offset, "GET", null);
			BufferedReader input = new BufferedReader(new InputStreamReader(uc.getInputStream()));
	        String line;
	        
	        String ids = "";
	        int i = 0;
	        while ((line = input.readLine()) != null) {
//	        	System.out.println(line);
	        	if (line.contains("id")) {
	        		int startingIndex = line.indexOf("\"", line.indexOf(":")) + 1;
	        		ids += "spotify%3Atrack%3A" + line.substring(startingIndex, line.indexOf("\"", startingIndex));
	        		i++;
	        		if (i == amount) {
	        			break;
	        		}
	        		ids += ",";
	        	}
	        }
	        System.out.println("Taken " + i + " songs.");
	        return ids.split(",");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
        
	}
	
	private static void addSongsToQueue(String[] ids) {
		String ids_string = toString(ids, "%2C");
//		openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + QUEUE_ID + "/tracks?position=" + playlistTop + "&uris=" + ids, "POST", null);
		URLConnection uc = openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + QUEUE_ID + "/tracks?position=" + queueTop + "&uris=" + ids_string, "POST", null);
	    logOutput(uc);
		queueTop += howManySongs(ids);
	    System.out.println("Added " + howManySongs(ids) + " songs.");
	      
	}
	
	
	private static String toString(String[] ids, String separator) {
		String ids_string = "";
		for (int i = 0; i < ids.length; i++) {
			ids_string += ids[i];
			if (i == ids.length - 1) {
				break;
			}
			ids_string += separator;
		}
		return ids_string;
	}
	
	
	private static String[] toStringArray(String ids) {
		if (ids.contains(",")) {
			return ids.split(",");
		}
		else {
			String[] array = new String[1];
			array[0] = ids;
			return array;
		}
	}
	
	private static void logOutput(URLConnection uc) {
        BufferedReader input;
		try {
			input = new BufferedReader(new InputStreamReader(uc.getInputStream()));
	        String line;
	        while ((line = input.readLine()) != null) {
//	        	System.out.println(line);
	        }
		} catch (IOException e) {
			e.printStackTrace();
			
		}
	}
	
	
	@SuppressWarnings("unused")
	private static int check() {
		
		try {
	        
	        URLConnection uc = openConnection("https://api.spotify.com/v1/me/player", "GET", null);
	        BufferedReader input = new BufferedReader(new InputStreamReader(uc.getInputStream()));
	        
	        // read this input
	        String line, artist = null, album = null, song = null;
	        int current = -1, total = 0;
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("progress_ms")) {
	        		current = Integer.parseInt(line.substring(line.indexOf(" ", line.indexOf(":")) + 1, line.indexOf(",")));
	        		break;
	        	}
	        }
	        if (current == -1) {
	        	//	Add or something strange
	        	System.out.println("No song playing");
	        	return -1;
	        }
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("name")) {
	        		//	Artist
	        		artist = line.substring(line.indexOf("\"", line.indexOf(":")) + 1, line.indexOf(",") - 1);
	        		break;
	        	}
	        }
	        if (artist == null) {
	        	//Ad
	        	System.out.println("Ad");
	        	return -2;
	        }
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("name")) {
	        		//	Album
	        		album = line.substring(line.indexOf("\"", line.indexOf(":")) + 1, line.indexOf(",") - 1);
	        		break;
	        	}
	        }
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("duration_ms")) {
	        		total = Integer.parseInt(line.substring(line.indexOf(" ", line.indexOf(":")) + 1, line.indexOf(",")));
	        		break;
	        	}
	        }
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("name")) {
	        		//	Song
	        		song = line.substring(line.indexOf("\"", line.indexOf(":")) + 1, line.indexOf(",") - 1);
	        		break;
	        	}
	        }
	        
	        float progress = (float) current / total * 100;
	        
	        System.out.println("\"" + song + "\" - " + artist + " (" + album + "). Progress: " + progress + "%");
	        
	        return total - current;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return -2;
		
	}
	
	private static int howManySongs(String[] ids) {
		return ids.length;
	}
	
	private static String[] getSongsFromPlaylistDefault(int[] order) {
		String[] ids = new String[3];
		
		for (int i = 0; i < 3; i++) {
			ids[i] = toString(getSongsFromPlaylist(1, order[playlistTop + i], currentPlaylist), ",");
		}
        playlistTop += howManySongs(ids);
		if (howManySongs(ids) == 0) {
			System.err.println("No more songs!");
		}
		return ids;
	}
	
	public static void addSongsToPriorityQueue(String[] songIDs) {
		addSongsToQueue(songIDs);
		prioritize(queueTop);
	}
	
	private static void prioritize(int trackAmount) {
		priority = current + 1;
		reorder(queueTop - trackAmount, trackAmount, priority);
	}
	
	private static void skipSong() {
//		openConnection("https://api.spotify.com/v1/me/player/next", "POST", null);
		URLConnection uc = openConnection("https://api.spotify.com/v1/me/player/next", "POST", null);
		logOutput(uc);
	}
	
	private static void removeTracks(String[] ids) {
		String data = "{\r\n  \"tracks\": [\r\n";
		for (int i = 0; i < ids.length; i++) {
			data += "    {\r\n" + 
					"      \"uri\": \"" + ids[i].replaceAll("%3A", ":") + "\"\r\n";
			if (i == ids.length - 1) {
				data += "    }\r\n";
				break;
			}
			data += "    },\r\n";
		}
		data += "  ]\r\n" +
				"}\r\n";
		System.out.println("Removing songs...");
//		System.out.println(data);
		openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + QUEUE_ID + "/tracks", "DELETE", data);
		URLConnection uc = openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + QUEUE_ID + "/tracks", "DELETE", data);
		logOutput(uc);
		queueTop -= howManySongs(ids);
	}
	


	public static void playPlaylist(String playlistID, boolean shuffle) {

		//	take out all the remaining from the old playlist (those in the queue must remain)
		if (priority < queueTop) {
			String[] toRemove = getSongsFromPlaylist(queueTop - priority, playlistTop - 5, currentPlaylist);	//Doesnt work, offset is +1 in second case
			removeTracks(toRemove);
		}
		
		
    	waitFor(3000);
		
    	
    	//	add from the new playlist
		playlistTop = 0;
		currentPlaylist = playlistID;
		int songAmount = getSongAmount(playlistID);
		order = createOrder(songAmount, shuffle);
		String[] ids = getSongsFromPlaylistDefault(order);
		currentSong = ids[0].replaceAll("%3A", ":");
		addSongsToQueue(ids);
		
		
		
	}
	
	/*
	 * TODO: check if it does alright with shuffle too
	 */
	private static int[] createOrder(int size, boolean shuffle) {
		
		order = new int[size];
		
		for (int i = 0; i < order.length; i++) {
			order[i] = i;
		}
		
		if (shuffle) {
			Random r = new Random();
			for (int i = 0; i < order.length; i++) {
				order[i] = order[r.nextInt(order.length)];
			}
		}
		
		return order;
		
	}


	public static void resume() {
		URLConnection uc = openConnection("https://api.spotify.com/v1/me/player/play", "PUT", null);
		logOutput(uc);
	}
	
	public static void pause() {
		URLConnection uc = openConnection("https://api.spotify.com/v1/me/player/pause", "PUT", null);
		logOutput(uc);
	}
	
	private static String getCurrentSongId() {
		URLConnection uc = openConnection("https://api.spotify.com/v1/me/player/currently-playing?fields=uri", "GET", null);
		BufferedReader input = null;
		try {
			input = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        String line;
        String id = null;
        try {
			while ((line = input.readLine()) != null) {
				if (line.contains("uri") && line.contains("track")) {
//					System.out.println(line);
					int startingIndex = line.indexOf("\"", line.indexOf(":")) + 1;
					id = line.substring(startingIndex, line.indexOf("\"", startingIndex));
//					System.out.println(id);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        //	If add (or no song playing, I suppose)
        if (id == null) {
        	id = "ad";
        }
        return id;
	}
	
	public static void onSongEnd() {
		
//		System.out.println("Previous song: " + currentSong);
		while (currentSong.equals(getCurrentSongId())) {
//			System.out.println("Waiting...");
			waitFor(200);
		}
		currentSong = getCurrentSongId();

		

		if (current == priority) {
			priority++;
		}
		current++;
		
		
		//	If queue ending, add more.
		if (queueTop - current < 2) {
			String[] ids = getSongsFromPlaylistDefault(order);
			addSongsToQueue(ids);
		}
		
		
		logStats();
		
		
		
		
	}
	

    public static void main(String args[]) {
    	
    	playPlaylist("33nQ8OrHQuIHi3pdPo36rW", true);
//    	String[] ids = getSongsFromPlaylist(1, 6, "33nQ8OrHQuIHi3pdPo36rW");
//    	addSongsToPriorityQueue(ids);
//    	playPlaylist("2nfGhtAZh52FfbBEpwyr8v");
//    	System.out.println(toString(getSongsFromPlaylist(5, 0, "33nQ8OrHQuIHi3pdPo36rW"), ","));
        
//    	pause();
//    	waitFor(2000);
//    	resume();
    	
    	
    	while (true) {
    		int sleep = check();
//    		System.out.println(sleep);
    		if (sleep < 0) {
        		waitFor(10000);
    		}
    		else if(sleep < 12000){
    			if (sleep < 2000) {
        			onSongEnd();
    			}
    			waitFor(500);
    		}
    		else {
        		waitFor(10000);
    		}
    	}
        
    	
    	
    	
    	

    }
    
    
    
	private static void logStats() {
		System.out.println("Playlist Top: " + playlistTop);
		System.out.println("Queue Top: " + queueTop);
		System.out.println("Current: " + current);
		System.out.println("Current Song: " + currentSong);
		System.out.println("Priority: " + priority);
		System.out.println("--------------------");
	}


	private static void waitFor(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}