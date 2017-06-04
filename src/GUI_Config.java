import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class GUI_Config extends JFrame {

	private final File configFile;

	private final JTextField bottoken;
	private final JTextField controlchannel;
	private final JTextField commandprefix;
	private final JTextField voicechannel;
	private final JTextField adminsrole;
	private final JCheckBox display_song_as_game;
	private final JCheckBox update_check_box;
	private final JSpinner update_check_interval_hours_spinner;
	private final JLabel lblIntervalInHours;
	private final JButton btnGet;

	public GUI_Config(File config) {
		configFile = config;

		setTitle(configFile.getName());
		setResizable(false);
		setSize(360, 320);
		setLocationRelativeTo(null);

		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(0, 4, 348, 228);
		getContentPane().add(panel);
		panel.setLayout(null);

		JLabel bottokenlable = new JLabel("Bot token:");
		bottokenlable.setBounds(12, 8, 120, 26);
		panel.add(bottokenlable);

		JLabel controlchannellable = new JLabel("Control channel:");
		controlchannellable.setBounds(12, 36, 120, 26);
		panel.add(controlchannellable);

		JLabel commandprefixlable = new JLabel("Command prefix:");
		commandprefixlable.setBounds(10, 64, 120, 26);
		panel.add(commandprefixlable);

		bottoken = new JTextField();
		bottoken.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(bottoken.getText().length() < 2) {
					hideGetButton();
				}
			}
		});
		bottoken.setBounds(140, 8, 206, 26);
		panel.add(bottoken);

		commandprefix = new JTextField();
		commandprefix.setBounds(140, 64, 206, 26);
		panel.add(commandprefix);

		JLabel voicechannellable = new JLabel("Voice channel:");
		voicechannellable.setBounds(10, 92, 120, 26);
		panel.add(voicechannellable);

		voicechannel = new JTextField();
		voicechannel.setBounds(140, 92, 206, 26);
		panel.add(voicechannel);

		JLabel adminsrolelable = new JLabel("Admins role:");
		adminsrolelable.setBounds(10, 120, 120, 26);
		panel.add(adminsrolelable);

		adminsrole = new JTextField();
		adminsrole.setBounds(140, 120, 206, 26);
		panel.add(adminsrole);

		display_song_as_game = new JCheckBox("Display song as game");
		display_song_as_game.setBounds(10, 148, 330, 26);
		panel.add(display_song_as_game);

		update_check_box = new JCheckBox("Update check");
		update_check_box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				update_check_interval_hours_spinner.setEnabled(update_check_box.isSelected());
			}
		});
		update_check_box.setBounds(10, 176, 130, 26);
		panel.add(update_check_box);

		update_check_interval_hours_spinner = new JSpinner();
		update_check_interval_hours_spinner.setEnabled(false);
		update_check_interval_hours_spinner.setModel(new SpinnerNumberModel(24, 1, null, 1));
		update_check_interval_hours_spinner.setBounds(274, 176, 66, 26);
		panel.add(update_check_interval_hours_spinner);

		lblIntervalInHours = new JLabel("Interval in hours:");
		lblIntervalInHours.setBounds(148, 176, 124, 26);
		panel.add(lblIntervalInHours);

		controlchannel = new JTextField();
		controlchannel.setBounds(140, 36, 206, 26);
		panel.add(controlchannel);

		btnGet = new JButton("Get");
		btnGet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hideGetButton();

				try {
					Desktop.getDesktop().browse(new URI(Values.DISCORD_GET_TOKEN));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnGet.setVisible(false);
		btnGet.setBounds(266, 8, 80, 26);
		panel.add(btnGet);

		JButton btnApply = new JButton("Apply");
		btnApply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnApply.setEnabled(false); // prevent from double clicking

				Config.set(Config.BOT_TOKEN, bottoken.getText());
				Config.set(Config.CONTROL_CHANNEL, controlchannel.getText());
				Config.set(Config.COMMAND_PREFIX, commandprefix.getText());
				Config.set(Config.VOICE_CHANNEL, voicechannel.getText());
				Config.set(Config.ADMINS_ROLE, adminsrole.getText());
				Config.set(Config.DISPLAY_SONG_AS_GAME, String.valueOf(display_song_as_game.isSelected()));
				Config.set(Config.UPDATE_CHECK_INTERVAL_HOURS, (update_check_box.isSelected() ? update_check_interval_hours_spinner.getValue().toString() : "0"));

				if(Config.save()) {
					JOptionPane.showMessageDialog(null, "Config saved.", Values.BOT_NAME, JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null, "Failed to save config.", Values.BOT_NAME, JOptionPane.ERROR_MESSAGE);
				}
				dispose();

				GUI.mvToFront();
			}
		});
		btnApply.setBounds(258, 254, 90, 26);
		getContentPane().add(btnApply);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		read();

		setVisible(true);

	}

	private void read() {
		Config.init(configFile);

		adminsrole.setText(Config.get(Config.ADMINS_ROLE));
		commandprefix.setText(Config.get(Config.COMMAND_PREFIX));
		controlchannel.setText(Config.get(Config.CONTROL_CHANNEL));
		display_song_as_game.setSelected(Boolean.parseBoolean(Config.get(Config.DISPLAY_SONG_AS_GAME)));
		voicechannel.setText(Config.get(Config.VOICE_CHANNEL));

		int h = Integer.parseInt(Config.get(Config.UPDATE_CHECK_INTERVAL_HOURS));
		if (h != 0) {
			update_check_box.setSelected(true);
			update_check_interval_hours_spinner.setEnabled(true);
			update_check_interval_hours_spinner.setValue(h);
		}

		String token = Config.get(Config.BOT_TOKEN);
		if(token.isEmpty()) {
			bottoken.setSize(124, bottoken.getHeight());
			btnGet.setVisible(true);
		} else {
			bottoken.setText(token);
		}

	}

	private void hideGetButton() {
		btnGet.setVisible(false);
		bottoken.setSize(206, bottoken.getHeight());
	}
}
