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
	private boolean firstClick;
	private boolean connected;
	
	
	

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
		lblPlaylist.setBounds(238, 172, 82, 14);
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
		comboBoxPlaylist.setBounds(238, 197, 119, 20);
		contentPane.add(comboBoxPlaylist);
		
		JButton btnPlaylist = new JButton("Play");
		btnPlaylist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				clickOnPlaylist();
			}
		});
		btnPlaylist.setBounds(238, 228, 89, 23);
		contentPane.add(btnPlaylist);
		
		checkboxShuffle = new JCheckBox("Shuffle");
		checkboxShuffle.setBounds(363, 196, 97, 23);
		contentPane.add(checkboxShuffle);
		
		textFieldQueue = new JTextField();
		textFieldQueue.setBounds(10, 172, 171, 20);
		contentPane.add(textFieldQueue);
		textFieldQueue.setColumns(10);
		
		btnQueue = new JButton("Add to queue");
		btnQueue.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				clickOnQueue();
			}
		});
		btnQueue.setBounds(42, 203, 109, 23);
		contentPane.add(btnQueue);
		
		textFieldConnect = new JTextField();
		textFieldConnect.setBounds(10, 45, 483, 20);
		contentPane.add(textFieldConnect);
		textFieldConnect.setColumns(10);
		
		
		firstClick = true;
	}
	
	
	
	
	private void clickOnConnect() {
		if (firstClick) {
			lblConnect.setText("After connecting. Write down the URI of the redirected page! DO NOT SHOW PUBLICLY");
			SpotifyConnector.getOauth1();
			firstClick = false;
		}else {
			if (SpotifyConnector.getOauth2(textFieldConnect.getText())) {
				connected = true;
				List<Playlist> list = SpotifyConnector.getPlaylists();
				for (Playlist playlist : list) {
					comboBoxPlaylist.addItem(playlist);
				}
				lblConnect.setText("Success!");
			}else {
				lblConnect.setText("Puto noob!");
				firstClick = true;
			}
		}
		
	}
	
	
	private void clickOnPlaylist() {
		String id = ((Playlist) comboBoxPlaylist.getSelectedItem()).getID();
		SpotifyConnector.playPlaylist(id, checkboxShuffle.isSelected());
	}
	
	
	private void clickOnQueue() {
		SpotifyConnector.addSongsToPriorityQueue(new Tracklist(new Track(textFieldQueue.getText())));
	}
	
	
}
