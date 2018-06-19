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
	private JTextField textField2;



	/**
	 * Create the frame.
	 */
	public OauthGUI(MainGUI main) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				main.setEnabled(true);
			}
		});
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 410, 277);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblSteps = new JLabel("Steps:");
		lblSteps.setBounds(163, 11, 46, 14);
		contentPane.add(lblSteps);
		
		JLabel lbl1 = new JLabel("1.- Click on this button. It will take you to log in");
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
		
		JLabel lbl2 = new JLabel("2.- Paste the redirection link here:");
		lbl2.setBounds(10, 144, 199, 14);
		contentPane.add(lbl2);
		
		textField2 = new JTextField();
		textField2.setBounds(200, 141, 178, 20);
		contentPane.add(textField2);
		textField2.setColumns(10);
		
		JLabel lbl3 = new JLabel("3.- Click on the connect button:");
		lbl3.setBounds(10, 204, 163, 14);
		contentPane.add(lbl3);
		
		JButton btn3 = new JButton("Connect!");
		btn3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				SpotifyConnector.getOauth2(textField2.getText());
				close();
			}
		});
		btn3.setBounds(238, 200, 89, 23);
		contentPane.add(btn3);
		
		JLabel lblWithYourSpotify = new JLabel("with your Spotify Account. Log in and the webpage");
		lblWithYourSpotify.setBounds(10, 68, 263, 14);
		contentPane.add(lblWithYourSpotify);
		
		JLabel lblWebpageWillRedirect = new JLabel("will redirect itself to Google. Copy the link.");
		lblWebpageWillRedirect.setBounds(10, 87, 263, 14);
		contentPane.add(lblWebpageWillRedirect);
	}
	
	private void close() {
		dispose();
	}
}
