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
	private JTextField textFieldConnect;	
	
	private JTextField textFieldCurrent;
	private JLabel lblCurrent;
	
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
					frame.startCheck();
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
		
		textFieldConnect = new JTextField();
		textFieldConnect.setBounds(10, 45, 483, 20);
		contentPane.add(textFieldConnect);
		textFieldConnect.setColumns(10);
		
		lblCurrent = new JLabel("Currently playing:");
		lblCurrent.setBounds(10, 216, 109, 14);
		contentPane.add(lblCurrent);
		
		textFieldCurrent = new JTextField();
		textFieldCurrent.setBounds(129, 213, 305, 20);
		contentPane.add(textFieldCurrent);
		textFieldCurrent.setColumns(10);
		
		boolean init = SpotifyConnector.init();
		if (!init) {
			JFrame connect = new OauthGUI(this);
			connect.setVisible(true);
			this.setEnabled(false);
			
		}
		
	}
	
	
	
	
	private void clickOnConnect() {
				connected = true;
				List<Playlist> list = SpotifyConnector.getPlaylists();
				for (Playlist playlist : list) {
					comboBoxPlaylist.addItem(playlist);
				}
				lblConnect.setText("Success!");
		
		
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
