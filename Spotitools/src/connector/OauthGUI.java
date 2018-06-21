package connector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class OauthGUI extends JFrame {

	private JPanel contentPane;
	
	private JTextField textFieldLink;
	private JTextField textFieldQueueID;
	
	private JLabel lbl1;
	private JLabel lbl2;
	private JLabel lbl3;
	private JLabel lbl4;
	private JLabel lbl5;
	private JLabel lbl6;
	private JLabel lbl7;


	private MainGUI main;

	/**
	 * Create the frame.
	 */
	public OauthGUI(MainGUI main) {
		this.main = main;
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				main.backFromLogin(false);
				close();
			}
		});
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 410, 329);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblSteps = new JLabel("Steps:");
		lblSteps.setBounds(163, 11, 46, 14);
		contentPane.add(lblSteps);
		
		lbl1 = new JLabel("1.- Click on this button. It will take you to log in");
		lbl1.setBounds(10, 48, 263, 14);
		contentPane.add(lbl1);
		
		JButton btn1 = new JButton("Log In");
		btn1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				SpotifyConnector.getOauth1();
			}
		});
		btn1.setBounds(272, 64, 89, 23);
		contentPane.add(btn1);
		
		lbl4 = new JLabel("2.- Paste the redirection link here:");
		lbl4.setBounds(10, 144, 199, 14);
		contentPane.add(lbl4);
		
		textFieldLink = new JTextField();
		textFieldLink.setBounds(200, 141, 178, 20);
		contentPane.add(textFieldLink);
		textFieldLink.setColumns(10);
		
		lbl7 = new JLabel("4.- Click on the connect button:");
		lbl7.setBounds(10, 265, 163, 14);
		contentPane.add(lbl7);
		
		JButton btn3 = new JButton("Connect!");
		btn3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				SpotifyConnector.getOauth2(textFieldLink.getText());
				SpotifyConnector.setQueueId(textFieldQueueID.getText());
				SpotifyConnector.save();
				main.backFromLogin(true);
				close();
			}
		});
		btn3.setBounds(238, 261, 89, 23);
		contentPane.add(btn3);
		
		lbl2 = new JLabel("with your Spotify Account. Log in and the webpage");
		lbl2.setBounds(10, 68, 263, 14);
		contentPane.add(lbl2);
		
		lbl3 = new JLabel("will redirect itself to Google. Copy the link.");
		lbl3.setBounds(10, 87, 263, 14);
		contentPane.add(lbl3);
		
		lbl5 = new JLabel("3.- Please, write the ID of the playlist");
		lbl5.setBounds(10, 193, 316, 14);
		contentPane.add(lbl5);
		
		textFieldQueueID = new JTextField();
		textFieldQueueID.setColumns(10);
		textFieldQueueID.setBounds(238, 209, 140, 20);
		contentPane.add(textFieldQueueID);
		
		lbl6 = new JLabel("that will act as the Queue");
		lbl6.setBounds(10, 212, 178, 14);
		contentPane.add(lbl6);
		
		setVisible(true);
	}
	
	private void close() {
		main.setEnabled(true);
		main.setVisible(true);
		setVisible(false);
	}
}
