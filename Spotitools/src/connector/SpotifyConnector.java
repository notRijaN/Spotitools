package connector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import domain.Track;
import domain.Tracklist;

public class SpotifyConnector {
	
	private static String USER_ID = "rijan_";
	private static String QUEUE_ID = "1OArlu09nNXISAjQTSPAlK";
	
	private static String currentPlaylist = "33nQ8OrHQuIHi3pdPo36rW";
	private static int FETCH_SIZE = 3;
	private static Tracklist lastFetch;
	private static int playlistTop = 0;
	private static int[] order;
	
	private static int current = 0;
	private static int queueTop = 0;
	private static int priority = 0;	// if priority == current => no priority
	
	private static Track currentSong;
	private static int timeleft;

	
	private static URLConnection openConnection(String urlString, String mode, String data) {
		
		HttpsURLConnection uc = null;
		try{
			//	Open connection
			URL url = new URL(urlString);
	        uc = (HttpsURLConnection) url.openConnection();
	        
	        //	Mode (GET, POST, PUT)
	        uc.setRequestMethod(mode);
	        if (mode.equals("POST")) {
	        	uc.setFixedLengthStreamingMode(0);
	        }
	        
	        //	Properties
	        uc.setRequestProperty("X-Requested-With", "Curl");
	        uc.setRequestProperty("Accept", "application/json");
	        uc.setRequestProperty("Content-Type", "application/json");
	        uc.setRequestProperty("Authorization", "Bearer BQBOHVbYNzdOOF3WfXfLtHmcAJbSJZUsm1qznBwKU7dzwZrBN2x1YMbgeEbovw6g9aftgi1aMZ-uOv8FA-DAB3N_azGV5cAxeak6C-z_w60e8uglc-5bTR-XEZSilwF-jafh3_1gSBOTb4ki9syyeN1lM74bSnLLFMqkGZzMJwlEYPfN5fQ2C8nxahGCqvQ5EER74FsN_CGVlslDKAupf9znA1AmjUjVt7mozkCb638XZLuC2gJYesA_9di7PU_NwxCqINxiRfrX4mIozGRjq_ncPsQ");
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
	
	
	
	
	
	
	/*
	 * 
	 * 		PLAYER
	 * 
	 */
	
	
	
	
	public static Track getPlayingTrack() {
		
		try {
	        
	        URLConnection uc = openConnection("https://api.spotify.com/v1/me/player/currently-playing", "GET", null);
	        BufferedReader input = new BufferedReader(new InputStreamReader(uc.getInputStream()));
	        
	        // read this input
	        String line, artist = null, album = null, song = null, id = null;
	        int current = -1, total = 0;
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("progress_ms")) {
	        		current = Integer.parseInt(line.substring(line.indexOf(" ", line.indexOf(":")) + 1, line.indexOf(",")));
	        		break;
	        	}
	        }
	        
        	//	No song
	        if (current == -1) {
	        	System.out.println("No song playing");
	        	timeleft = -1;
	        }
	        
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("name")) {
	        		//	Artist
	        		artist = removeQuotes(getInfoFromLine(line, ","));
	        		break;
	        	}
	        }

        	//Ad
	        if (artist == null) {
	        	System.out.println("Ad");
	        	timeleft = -2;
	        }
	        
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("name")) {
	        		//	Album
	        		album = removeQuotes(getInfoFromLine(line, ","));
	        		break;
	        	}
	        }
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("duration_ms")) {
	        		total = Integer.parseInt(getInfoFromLine(line, ","));
	        		break;
	        	}
	        }
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("\"id\"")) {
	        		id = removeQuotes(getInfoFromLine(line, ","));
	        		break;
	        	}
	        }
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("name")) {
	        		//	Song
	        		song = removeQuotes(getInfoFromLine(line, ","));
	        		break;
	        	}
	        }
	        
	        
	        
	        float progress = (float) current / total * 100;
	        System.out.println("\"" + song + "\" - " + artist + " (" + album + "). Progress: " + progress + "%");
	        
	        timeleft = total - current;
	        return new Track(id, song, album, artist);
		}
		catch (IOException e) {
			e.printStackTrace();
			timeleft = -3;
			return null;
		}
		
	}
	

	public static void skipSong() {
		URLConnection uc = openConnection("https://api.spotify.com/v1/me/player/next", "POST", null);
		logOutput(uc);
	}
	
	
	public static void resume() {
		URLConnection uc = openConnection("https://api.spotify.com/v1/me/player/play", "PUT", null);
		logOutput(uc);
	}
	
	
	public static void pause() {
		URLConnection uc = openConnection("https://api.spotify.com/v1/me/player/pause", "PUT", null);
		logOutput(uc);
	}
	
	
	
	
	
	/*
	 * 
	 * 		PLAYLIST
	 * 
	 */
	
	

	
	private static int getSongAmount(String playlistID) {
		URLConnection uc = openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + playlistID + "/tracks?fields=total", "GET", null);
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
	

	private static Tracklist getSongsFromPlaylist(int amount, int offset, String playlistID) {
		try{
			URLConnection uc = openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + playlistID + "/tracks?fields=items(track(id))&limit=" + amount + "&offset=" + offset, "GET", null);
			BufferedReader input = new BufferedReader(new InputStreamReader(uc.getInputStream()));
	        
			String line;
			String[] ids = new String[amount];
	        int i = 0;
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("id")) {
	        		ids[i] = removeQuotes(getInfoFromLine(line, null));
	        		i++;
	        	}
	        }
	        System.out.println("Taken " + i + " songs.");
	        
	        Tracklist list = new Tracklist(ids);
	        return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
        
	}
	
	
	private static void reorder(int trackIndex, int trackAmount, int place) {
		String data = "{\r\n" + 
    			"  \"range_start\": " + trackIndex + ",\r\n" + 
    			"  \"range_length\": " + trackAmount + ",\r\n" + 
    			"  \"insert_before\": " + place + "\r\n" + 
    			"}";
		URLConnection uc = openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + QUEUE_ID + "/tracks", "PUT", data);
		logOutput(uc);

	}
	
	
	
	
	
	
	
	/*
	 * 
	 * 		QUEUE
	 * 
	 */
	
	
	
	
	private static void addSongsToQueue(Tracklist tracks) {
		String uri = tracks.toUri(true);
		URLConnection uc = openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + QUEUE_ID + "/tracks?position=" + queueTop + "&uris=" + uri, "POST", null);
	    logOutput(uc);
		queueTop += tracks.amount();
	    System.out.println("Added " + tracks.amount() + " songs.");
	      
	}
	
	
	public static void addSongsToPriorityQueue(Tracklist tracks) {
		addSongsToQueue(tracks);
		
		int trackAmount = tracks.amount();
		reorder(queueTop - trackAmount, trackAmount, priority + 1);
		priority += trackAmount;
	}
	
	
	private static void removeTracksFromQueue(Tracklist tracks) {
		int trackAmount = tracks.amount();
		
		String data = "{\r\n  \"tracks\": [\r\n";
		for (int i = 0; i < trackAmount; i++) {
			data += "    {\r\n" + 
					"      \"uri\": \"" + tracks.get(i).toUri(false) + "\"\r\n";
			if (i == trackAmount - 1) {
				data += "    }\r\n";
				break;
			}
			data += "    },\r\n";
		}
		data += "  ]\r\n" +
				"}\r\n";
		
		System.out.println("Removing songs...");
//		System.out.println(data);
		
		URLConnection uc = openConnection("https://api.spotify.com/v1/users/" + USER_ID + "/playlists/" + QUEUE_ID + "/tracks", "DELETE", data);
		logOutput(uc);
		
		queueTop -= trackAmount;
	}
	

	
	
	
	
	
	/*
	 * 
	 * 		HELPERS
	 * 
	 */
	
	
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
	
	
	private static String getInfoFromLine(String line, String endingCharacter) {
		int startingIndex = line.indexOf(":") + 2;
		if (endingCharacter == null) {
			return line.substring(startingIndex);			//	Space after colon.
		}
		else {
			int endingIndex = line.indexOf(endingCharacter, startingIndex);
			return line.substring(startingIndex, endingIndex);
		}
	}
	
	
	private static String removeQuotes(String string) {
		return string.substring(1, string.length() - 1);
	}
	
	
	
	
	
	
	
	private static Tracklist getSongsFromPlaylistDefault() {
		Tracklist list = new Tracklist(FETCH_SIZE);
		
		for (int i = 0; i < FETCH_SIZE; i++) {
			list.add(getSongsFromPlaylist(1, order[playlistTop + i], currentPlaylist).get(0));
		}
		list.trim();
		lastFetch = list;
		
        playlistTop += list.amount();
		if (list.amount() == 0) {
			System.err.println("No more songs!");
		}
		
		return list;
	}
	
	
	public static void playPlaylist(String playlistID, boolean shuffle) {

		//	take out all the remaining from the old playlist (those in the queue must remain)
		if (lastFetch != null) {
			removeTracksFromQueue(lastFetch);
	    	waitFor(1000);
		}
		
    	
    	//	add from the new playlist
		playlistTop = 0;
		currentPlaylist = playlistID;
		
		int songAmount = getSongAmount(playlistID);
		order = createOrder(songAmount, shuffle);
		
		Tracklist tracks = getSongsFromPlaylistDefault();
		addSongsToQueue(tracks);
		
		currentSong = tracks.get(0);	
	}
	
	
	private static int[] createOrder(int size, boolean shuffle) {
		
		order = new int[size];
		for (int i = 0; i < order.length; i++) {
			order[i] = i;
		}
		
		if (shuffle) {
			Random r = new Random();
			for (int i = 0; i < order.length; i++) {
				int randomIndex = r.nextInt(order.length);
				
				int helper = order[randomIndex];
				order[randomIndex] = order[i];
				order[i] = helper;
			}
		}
		
		return order;
	}




	
	public static void onSongEnd() {
		
		Track check = getPlayingTrack();
		//	TODO: fix when no song or ad before (probably add another boolean)
		while (currentSong == null || currentSong.same(check)) {
			waitFor(400);
			check = getPlayingTrack();
		}
		currentSong = check;

		
		if (current == priority) {
			priority++;
		}
		current++;
		
		
		//	If queue ending, add more.
		if (queueTop - current < 2) {
			Tracklist tracks = getSongsFromPlaylistDefault();
			addSongsToQueue(tracks);
		}
		
		
		logStats();
	}
	

    public static void main(String args[]) {
    	
    	//	TODO: flush queue
    	
    	playPlaylist("2nfGhtAZh52FfbBEpwyr8v", false);
		waitFor(1000);
    	Tracklist ids = getSongsFromPlaylist(1, 6, "33nQ8OrHQuIHi3pdPo36rW");
    	addSongsToPriorityQueue(ids);
    	
    	
    	while (true) {
    		getPlayingTrack();
    		if (timeleft < 0) {
        		waitFor(10000);
    		}
    		else if(timeleft < 12000){
    			if (timeleft < 2000) {
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