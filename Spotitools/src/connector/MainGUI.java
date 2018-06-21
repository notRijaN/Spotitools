package connector;

import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import domain.Playlist;
import domain.Track;
import domain.Tracklist;

@SuppressWarnings("serial")
public class MainGUI extends JFrame {

	private JPanel contentPane;
	
	
	private JTextField textFieldQueue;
	private JButton btnQueue;
	
	private JLabel lblPlaylist;
	private JComboBox<Playlist> comboBoxPlaylist;
	private JCheckBox checkboxShuffle;
	private JButton btnPlaylist;
	
	private JButton btnConnect;
	private JLabel lblConnect;
	
	private JTextField textFieldCurrent;
	private JLabel lblCurrent;
	

	private JTextField textFieldQueueId;
	private JButton btnQueueId;
	
	private boolean connected;
	
	private int state;
	/*	0 => not connected
	 * 	1 => idle
	 * 	2 => playing
	 */
	
	
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGUI frame = new MainGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
	}
	

	private void startCheck() {
		while (true) {
    		Track current = SpotifyConnector.getPlayingTrack();
    		int timeleft = SpotifyConnector.getTimeLeft();
    		textFieldCurrent.setText(current.toString());
    		
    		if (timeleft < 0) {
        		waitFor(10000);
    		}
    		else if(timeleft < 12000){
    			if (timeleft < 2000) {
//        			onSongEnd();
    				waitFor(2000);
    			}
    			waitFor(500);
    		}
    		else {
        		waitFor(10000);
    		}
		}
    	
		
	}
	

	/**
	 * Create the frame.
	 */
	public MainGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 519, 343);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		lblPlaylist = new JLabel("Playlists");
		lblPlaylist.setBounds(238, 111, 82, 14);
		contentPane.add(lblPlaylist);
		
		btnConnect = new JButton("Connect!");
		btnConnect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				clickOnConnect();
			}
		});
		btnConnect.setBounds(10, 11, 89, 23);
		contentPane.add(btnConnect);
		
		lblConnect = new JLabel("Not connected");
		lblConnect.setBounds(109, 15, 76, 14);
		contentPane.add(lblConnect);
		
		comboBoxPlaylist = new JComboBox<Playlist>();
		comboBoxPlaylist.setBounds(238, 136, 119, 20);
		contentPane.add(comboBoxPlaylist);
		
		btnPlaylist = new JButton("Play");
		btnPlaylist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				clickOnPlaylist();
			}
		});
		btnPlaylist.setBounds(238, 167, 89, 23);
		contentPane.add(btnPlaylist);
		
		checkboxShuffle = new JCheckBox("Shuffle");
		checkboxShuffle.setBounds(363, 135, 97, 23);
		contentPane.add(checkboxShuffle);
		
		textFieldQueue = new JTextField();
		textFieldQueue.setBounds(10, 111, 171, 20);
		contentPane.add(textFieldQueue);
		textFieldQueue.setColumns(10);
		
		btnQueue = new JButton("Add to queue");
		btnQueue.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				clickOnQueue();
			}
		});
		btnQueue.setBounds(42, 142, 109, 23);
		contentPane.add(btnQueue);
		
		lblCurrent = new JLabel("Currently playing:");
		lblCurrent.setBounds(10, 216, 109, 14);
		contentPane.add(lblCurrent);
		
		//	TODO: this xD (get playback)
		textFieldCurrent = new JTextField();
		textFieldCurrent.setBounds(129, 213, 305, 20);
		contentPane.add(textFieldCurrent);
		textFieldCurrent.setColumns(10);
		
		textFieldQueueId = new JTextField();
		textFieldQueueId.setBounds(234, 12, 86, 20);
		contentPane.add(textFieldQueueId);
		textFieldQueueId.setColumns(10);
		textFieldQueueId.setVisible(false);
		
		btnQueueId = new JButton("Set Queue id");
		btnQueueId.setBounds(330, 11, 104, 23);
		btnQueueId.setVisible(false);
		contentPane.add(btnQueueId);
		
		connected = SpotifyConnector.init();
		if (!connected) {
			btnConnect.setText("Connect");
			lblConnect.setText("Not connected");
		}else {
			populatePlaylistComboBox();
			btnConnect.setText("Disconnect!");
			lblConnect.setText("Connected");
		}
		setVisible(true);
		
		
	}
	
	private void populatePlaylistComboBox() {
		List<Playlist> list = SpotifyConnector.getPlaylists();
		for (Playlist playlist : list) {
			comboBoxPlaylist.addItem(playlist);
		}
	}
	
	
	public void backFromLogin(boolean hasConnected) {
		if (hasConnected) {
			connected = true;
			btnConnect.setText("Disconnect!");
			lblConnect.setText("Connected");
			populatePlaylistComboBox();
		}
	}
	
	
	private void clickOnConnect() {
		if (!connected) {
			setEnabled(false);
			new OauthGUI(this);
		}else {
			SpotifyConnector.disconnect();
			connected = false;
			btnConnect.setText("Connect");
			lblConnect.setText("Not connected");
		}
	}
	
	
	private void clickOnPlaylist() {
		String id = ((Playlist) comboBoxPlaylist.getSelectedItem()).getID();
		SpotifyConnector.playPlaylist(id, checkboxShuffle.isSelected());
	}
	
	
	private void clickOnQueue() {
		SpotifyConnector.addSongsToPriorityQueue(new Tracklist(new Track(textFieldQueue.getText())));
	}
	
	
	
	
	
	

	private static void waitFor(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
