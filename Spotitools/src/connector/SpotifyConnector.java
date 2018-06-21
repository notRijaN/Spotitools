package connector;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import domain.Playlist;
import domain.Track;
import domain.Tracklist;

public class SpotifyConnector {
	
	private static String QUEUE_ID = "1OArlu09nNXISAjQTSPAlK";
	
	private static String OAUTH = null;
	private static String REFRESH = null;
	private static String AUTH = "1da95bd1adf240aaa20ace0656f1c44a:8745082898384867932f8a133dec0acc";
	private static long EXPIRE;		//	TODO: auth renewal
	private static boolean isConnected = false;	//	TODO:	check this
	
	private static String currentPlaylist = null;
	private static int FETCH_SIZE = 3;
	private static Tracklist lastFetch;
	private static int playlistTop = 0;
	private static int[] order;
	
	private static int current = 0;
	private static int queueTop = 0;
	private static int priority = 0;	// if priority == current => no priority
	
	private static Track currentSong;
	private static int timeleft;
	
	
	//	TODO: get error codes and interpret for this app.
	
	//	TODO: check expired oauths
	
	/**
	 * 
	 * @return whether it was initialized
	 */
	public static boolean init() {
		File data = new File("res/info.data");
		if (!data.exists()) {
			return false;
		}else {

			Scanner sc = null;
			try {
				sc = new Scanner(data);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			OAUTH = sc.nextLine().split("=")[1];
			EXPIRE = Long.parseLong(sc.nextLine().split("=")[1]);
			REFRESH = sc.nextLine().split("=")[1];
			QUEUE_ID = sc.nextLine().split("=")[1];
			
			sc.close();
			return true;
		}
		
	}
	
	public static void setQueueId(String id) {
		QUEUE_ID = id;
	}
	
	public static void save() {

		try {
			File data = new File("res/info.data");
			FileWriter writer = new FileWriter(data);
			writer.write("oauth=" + OAUTH + "\n");
			writer.write("expire=" + EXPIRE + "\n");
			writer.write("refresh=" + REFRESH + "\n");
			writer.write("queue_id=" + QUEUE_ID + "\n");
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void disconnect() {
		new File("res/info.data").delete();
		System.out.println("Disconnected!");
	}
	
	

	
	public static void getOauth1() {
		String s = "https://accounts.spotify.com/en/authorize?client_id=1da95bd1adf240aaa20ace0656f1c44a&redirect_uri=https:%2F%2Fwww.google.com" +
				"&response_type=code&scope=playlist-modify-public%20user-read-email";
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.browse(URI.create(s));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static boolean getOauth2(String uri) {
		
		try {
			
			int authBeginIndex = uri.indexOf("=") + 1;
			String code = uri.substring(authBeginIndex);
			
			//	Connection
			URL url = new URL("https://accounts.spotify.com/api/token");
			HttpsURLConnection uc = (HttpsURLConnection) url.openConnection();
	        
			//	Properties
	        uc.setRequestMethod("POST");
	        uc.setRequestProperty("X-Requested-With", "Curl");
	        uc.setRequestProperty("Accept", "application/json");
	        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        uc.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(AUTH.getBytes()));
	        uc.setDoOutput(true);

	        //	Data
	        String data = "grant_type=authorization_code&code=" +code + "&redirect_uri=https:%2F%2Fwww.google.com";
	        BufferedWriter writer = new BufferedWriter(new PrintWriter(uc.getOutputStream()));
	        writer.write(data);
	        writer.flush();
	        
	        
	        BufferedReader r = new BufferedReader(new InputStreamReader(uc.getInputStream()));
	        String line = r.readLine();
        	String[] data1 = line.split(",");
        	
        	int oauthBegin = data1[0].indexOf(":") + 2;
        	int oauthEnd = data1[0].length() - 1;
        	OAUTH = data1[0].substring(oauthBegin, oauthEnd);
        	
        	int expireBegin = data1[2].indexOf(":") + 1;
        	EXPIRE = Calendar.getInstance().getTimeInMillis() + Integer.parseInt(data1[2].substring(expireBegin)) * 1000;

        	int refreshBegin = data1[3].indexOf(":") + 2;
        	int refreshEnd = data1[3].length() - 1;
        	REFRESH = data1[3].substring(refreshBegin, refreshEnd);
        	
        	save();
	        
	        
	        return true;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		
	}
	
	private static void refreshOauth() {
		
		try {
			URL url = new URL("https://accounts.spotify.com/api/token");
			HttpsURLConnection uc = (HttpsURLConnection) url.openConnection();
	        
			//	Properties
	        uc.setRequestMethod("POST");
	        uc.setRequestProperty("X-Requested-With", "Curl");
	        uc.setRequestProperty("Accept", "application/json");
	        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        uc.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(AUTH.getBytes()));
	        uc.setDoOutput(true);

	        //	Data
	        String data = "grant_type=refresh_token&refresh_token=" + REFRESH;
	        BufferedWriter writer = new BufferedWriter(new PrintWriter(uc.getOutputStream()));
	        writer.write(data);
	        writer.flush();
	        
	        
	        BufferedReader r = new BufferedReader(new InputStreamReader(uc.getInputStream()));
	        String line = r.readLine();
        	String[] data1 = line.split(",");
        	
        	int oauthBegin = data1[0].indexOf(":") + 2;
        	int oauthEnd = data1[0].length() - 1;
        	OAUTH = data1[0].substring(oauthBegin, oauthEnd);
        	
        	int expireBegin = data1[2].indexOf(":") + 1;
        	EXPIRE = Calendar.getInstance().getTimeInMillis() + Integer.parseInt(data1[2].substring(expireBegin)) * 1000;
        	
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	private static BufferedReader openConnection(String urlString, String mode, String data) {
		
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
	        uc.setRequestProperty("Authorization", "Bearer " + OAUTH);
	        uc.setDoOutput(true);
	        
	        //	Data (body)
	        if (data != null) {
		        BufferedWriter writer = new BufferedWriter(new PrintWriter(uc.getOutputStream()));
		        writer.write(data);
		        writer.flush();
	        }

	        //	Get Response Code
			int code = uc.getResponseCode();
			if (code >= 400) {
				System.out.println(uc.getResponseMessage());
				boolean retry = tryToFixError(code);
				if (retry) {
					openConnection(urlString, mode, data);
				}
			}
	        
	        return new BufferedReader(new InputStreamReader(uc.getInputStream()));
	        
		}catch (Exception e) {
			e.printStackTrace();
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
	        
			BufferedReader input = openConnection("https://api.spotify.com/v1/me/player/currently-playing", "GET", null);
	        
	        String line, artist = null, album = null, song = null, id = null;
	        
        	//	Ad
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("\"item\"")) {
	        		if (line.contains("null")) {
	        			return new Track("ad");
	        		}else {
	        			break;
	        		}
	        	}
	        }
	        
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("\"name\"")) {
	        		//	Artist
	        		artist = removeQuotes(getInfoFromLine(line, ","));
	        		break;
	        	}
	        }

	        
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("\"name\"")) {
	        		//	Album
	        		album = removeQuotes(getInfoFromLine(line, ","));
	        		break;
	        	}
	        }
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("\"id\"")) {
	        		//	ID
	        		id = removeQuotes(getInfoFromLine(line, ","));
	        		break;
	        	}
	        }
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("\"name\"")) {
	        		//	Song
	        		song = removeQuotes(getInfoFromLine(line, ","));
	        		break;
	        	}
	        }
	        
	        
	        System.out.println("\"" + song + "\" - " + artist + " (" + album + ")");
	        
	        return new Track(id, song, album, artist);
		}
		catch (IOException e) {
			e.printStackTrace();
			timeleft = -3;
			return null;
		}
		
	}
	
	/**
	 * If no real track is playing return -1;
	 * 
	 * @return
	 */
	public static int getTimeLeft() {
		
		if (!getPlayingTrack().isRealTrack()) {
			return -1;
		}
		
		try {
	        
			BufferedReader input = openConnection("https://api.spotify.com/v1/me/player/currently-playing", "GET", null);
	        
	        // read this input
	        String line;
	        int current = -1, total = 0;
	        while ((line = input.readLine()) != null) {
	        	if (line.contains("\"progress_ms\"")) {
	        		current = Integer.parseInt(line.substring(line.indexOf(" ", line.indexOf(":")) + 1, line.indexOf(",")));
	        		break;
	        	}
	        }

	        while ((line = input.readLine()) != null) {
	        	if (line.contains("\"duration_ms\"")) {
	        		total = Integer.parseInt(getInfoFromLine(line, ","));
	        		break;
	        	}
	        }
	        
	        float progress = (float) current / total * 100;
	        System.out.println("Progress: " + progress + "%");
	        
	        return total - current;
		}
		catch (IOException e) {
			e.printStackTrace();
			return -3;
		}
		
	}
	

	public static void skipSong() {
		BufferedReader reader = openConnection("https://api.spotify.com/v1/me/player/next", "POST", null);
		disposeOutput(reader);
	}
	
	
	public static void resume() {
		BufferedReader reader = openConnection("https://api.spotify.com/v1/me/player/play", "PUT", null);
		disposeOutput(reader);
	}
	
	
	public static void pause() {
		BufferedReader reader = openConnection("https://api.spotify.com/v1/me/player/pause", "PUT", null);
		disposeOutput(reader);
	}
	
	
	
	
	
	/*
	 * 
	 * 		PLAYLIST
	 * 
	 */
	
	public static List<Playlist> getPlaylists(){
		try {
			BufferedReader input = openConnection("https://api.spotify.com/v1/me/playlists?fields=items(name,id,tracks(total))", "GET", null);
	        
			List<Playlist> list = new ArrayList<Playlist>();
			String line = input.readLine(), id, name;
			int trackAmount;
			boolean end = false;
			
	        while (true) {
	        	
	        	while (!line.contains("\"id\"")) {
	        		//	End here
	        		if (line.contains("\"limit\"")) {
	        			end = true;
	        			break;
	        		}
	        		line = input.readLine();
	        	}
	        	
	        	if (end) {
	        		break;
	        	}
	        	id = removeQuotes(getInfoFromLine(line, ","));
	        	
	        	
	        	while (!line.contains("name")) {
	        		line = input.readLine();
		        }
	        	name = removeQuotes(getInfoFromLine(line, ","));
	        	if (name.equals("queue")) {
	        		line = input.readLine();
	        		continue;
	        	}
	        	
	        	
	        	while (!line.contains("total")) {
	        		line = input.readLine();
		        }
	        	trackAmount = Integer.parseInt(getInfoFromLine(line, null));
	        	list.add(new Playlist(id, name, trackAmount));
	        	System.out.println(id + "," + name + "," + trackAmount);
        		line = input.readLine();
	        	
	        }
	        
	        return list;
		} catch(Exception e) {
			e.printStackTrace();
		}
        return null;
	}

	
	private static int getSongAmount(String playlistID) {
		BufferedReader input = openConnection("https://api.spotify.com/v1/users/me/playlists/" + playlistID + "/tracks?fields=total", "GET", null);
        String line;
        int totalTracks = -1;	//Error code
        
		try {
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
			BufferedReader input = openConnection("https://api.spotify.com/v1/users/me/playlists/" + playlistID + "/tracks?fields=items(track(id))&limit=" + amount + "&offset=" + offset, "GET", null);
	        
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
    			"}\r\n";
		BufferedReader input = openConnection("https://api.spotify.com/v1/users/me/playlists/" + QUEUE_ID + "/tracks", "PUT", data);
		disposeOutput(input);

	}
	
	
	
	
	
	
	
	/*
	 * 
	 * 		QUEUE
	 * 
	 */
	
	
	
	
	private static void addSongsToQueue(Tracklist tracks) {
		String uri = tracks.toUri(true);
		BufferedReader input = openConnection("https://api.spotify.com/v1/users/me/playlists/" + QUEUE_ID + "/tracks?position=" + queueTop + "&uris=" + uri, "POST", null);
	    disposeOutput(input);
	    
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
		
		BufferedReader input = openConnection("https://api.spotify.com/v1/users/me/playlists/" + QUEUE_ID + "/tracks", "DELETE", data);
		disposeOutput(input);
		
		queueTop -= trackAmount;
	}
	

	
	
	
	
	
	/*
	 * 
	 * 		HELPERS
	 * 
	 */
	
	
	private static void disposeOutput(BufferedReader input) {
		try {
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
	
	
	private static boolean tryToFixError(int code) {
		switch (code) {
		case 401:
			//refresh oauth
			refreshOauth();
			return true;
		case 400:
			//	Wrong syntax (shouldn't happen if not for wrong URIs I suppose)
			return false;
		default:
			return false;
		}
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
		while (currentSong.same(check)) {
			waitFor(400);
			check = getPlayingTrack();
		}
		currentSong = check;

		//	If ad
		if (!currentSong.isRealTrack()) {
			return;
		}
		
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
    	
//    	getOauth();
//    	
//    	refreshOauth();
  	
//    	timeleft = 20000;
//    	while (true) {
//    		getPlayingTrack();
//    		getTimeLeft();
//    		if (timeleft < 0) {
//        		waitFor(10000);
//    		}
//    		else if(timeleft < 12000){
//    			if (timeleft < 2000) {
//        			onSongEnd();
//    			}
//    			waitFor(500);
//    		}
//    		else {
//        		waitFor(10000);
//    		}
//    	}

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