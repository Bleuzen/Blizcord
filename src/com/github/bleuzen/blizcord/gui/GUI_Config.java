package com.github.bleuzen.blizcord.gui;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultEditorKit;

import com.github.bleuzen.blizcord.Config;
import com.github.bleuzen.blizcord.Values;
import com.github.bleuzen.blizcord.a;

@SuppressWarnings("serial")
public class GUI_Config extends JFrame {

	static GUI_Config instance;

	private final File configFile;

	private final JPopupMenu popupMenu;

	private final JTextField bottoken;
	private final JTextField controlchannel;
	private final JTextField commandprefix;
	private final JTextField voicechannel;
	private final JTextField adminsrole;
	private final JCheckBox display_song_as_game;
	private final JCheckBox periodic_update_check_box;
	private final JSpinner update_check_interval_hours_spinner;
	private final JLabel lblIntervalInHours;
	private final JButton btnGet;
	private final JCheckBox chckbxAdminsRole;
	private final JCheckBox chckbxAllowCustomVolume;
	private final JSpinner spinnerStartingVolume;
	private final JCheckBox chckbxEnableMediaControl;
	private final JTabbedPane tabbedPane;
	private final JPanel panel_Basic;
	private final JPanel panel_Advanced;
	private final JCheckBox chckbxAutoReconnect;
	private final JCheckBox chckbxNativeAudioSystem;

	public GUI_Config(File config) {
		instance = this;

		configFile = config;

		// Init popup menu
		popupMenu = new JPopupMenu();
		/*Action copy = new DefaultEditorKit.CopyAction();
		copy.putValue(Action.NAME, "Copy");
		copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
		popupMenu.add(copy);*/
		Action paste = new DefaultEditorKit.PasteAction();
		paste.putValue(Action.NAME, "Paste");
		paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
		popupMenu.add(paste);


		setType(Type.UTILITY);
		setTitle(configFile.getName());
		setResizable(false);
		setSize(390, 360);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		if(GUI_Main.isIconSet()) {
			setIconImage(GUI_Main.getIcon());
		}

		getContentPane().setLayout(null);

		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.setBounds(4, 4, 376, 270);
		getContentPane().add(tabbedPane);

		panel_Basic = new JPanel();
		tabbedPane.addTab("Basic", null, panel_Basic, null);
		panel_Basic.setLayout(null);

		JLabel bottokenlable = new JLabel("Bot token:");
		bottokenlable.setBounds(8, 8, 120, 28);
		panel_Basic.add(bottokenlable);

		bottoken = new JTextField();
		bottoken.setBounds(140, 8, 206, 28);
		bottoken.setComponentPopupMenu(popupMenu);
		panel_Basic.add(bottoken);

		JLabel controlchannellable = new JLabel("Control channel:");
		controlchannellable.setBounds(8, 38, 120, 28);
		panel_Basic.add(controlchannellable);
		controlchannellable.setToolTipText("The channel where you can send commands to the bot");

		JLabel commandprefixlable = new JLabel("Command prefix:");
		commandprefixlable.setBounds(8, 68, 120, 28);
		panel_Basic.add(commandprefixlable);

		commandprefix = new JTextField();
		commandprefix.setBounds(140, 68, 206, 28);
		panel_Basic.add(commandprefix);

		JLabel voicechannellable = new JLabel("Voice channel:");
		voicechannellable.setBounds(8, 98, 120, 28);
		panel_Basic.add(voicechannellable);
		voicechannellable.setToolTipText("<html>The bot tries to join the channel of the user who sent the command.<br>If this fails, it will join the following channel.</html>");

		voicechannel = new JTextField();
		voicechannel.setBounds(140, 98, 206, 28);
		panel_Basic.add(voicechannel);

		adminsrole = new JTextField();
		adminsrole.setBounds(140, 128, 206, 28);
		panel_Basic.add(adminsrole);

		display_song_as_game = new JCheckBox("Display song as game");
		display_song_as_game.setBounds(8, 158, 330, 28);
		panel_Basic.add(display_song_as_game);
		display_song_as_game.setToolTipText("Displays the name of the current song as game on Discord");

		periodic_update_check_box = new JCheckBox("Periodic update check");
		periodic_update_check_box.setBounds(8, 188, 192, 28);
		panel_Basic.add(periodic_update_check_box);

		update_check_interval_hours_spinner = new JSpinner();
		update_check_interval_hours_spinner.setBounds(276, 188, 64, 28);
		panel_Basic.add(update_check_interval_hours_spinner);
		update_check_interval_hours_spinner.setEnabled(false);
		update_check_interval_hours_spinner.setModel(new SpinnerNumberModel(24, 1, null, 1));

		lblIntervalInHours = new JLabel("Hours:");
		lblIntervalInHours.setBounds(204, 188, 68, 28);
		panel_Basic.add(lblIntervalInHours);

		controlchannel = new JTextField();
		controlchannel.setBounds(140, 38, 206, 28);
		panel_Basic.add(controlchannel);

		btnGet = new JButton("Get");
		btnGet.setBounds(266, 8, 80, 28);
		panel_Basic.add(btnGet);
		btnGet.setToolTipText(Values.DISCORD_GET_TOKEN);

		chckbxAdminsRole = new JCheckBox("Admins role");
		chckbxAdminsRole.setBounds(8, 128, 120, 28);
		panel_Basic.add(chckbxAdminsRole);
		chckbxAdminsRole.setToolTipText("Users in this role can control the bot");
		chckbxAdminsRole.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(chckbxAdminsRole.isSelected()) {
					adminsrole.setEnabled(true);
				} else {
					adminsrole.setEnabled(false);
					adminsrole.setText("");
				}
			}
		});
		btnGet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hideGetButton();

				try {
					Desktop.getDesktop().browse(new URI(Values.DISCORD_GET_TOKEN));
				} catch (Exception e1) {
					GUI_Main.showErrMsgBox(e1.getMessage());
				}
			}
		});
		btnGet.setVisible(false);
		periodic_update_check_box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				update_check_interval_hours_spinner.setEnabled(periodic_update_check_box.isSelected());
			}
		});
		bottoken.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(bottoken.getText().length() < 2) {
					hideGetButton();
				}
			}
		});

		panel_Advanced = new JPanel();
		tabbedPane.addTab("Advanced", null, panel_Advanced, null);
		panel_Advanced.setLayout(null);

		chckbxAllowCustomVolume = new JCheckBox("Allow to change the Volume");
		chckbxAllowCustomVolume.setBounds(8, 8, 240, 28);
		panel_Advanced.add(chckbxAllowCustomVolume);
		chckbxAllowCustomVolume.setToolTipText("<html>This enables the <i>!volume</i> command.<br>Other than 100% requires more CPU.</html>");

		spinnerStartingVolume = new JSpinner();
		spinnerStartingVolume.setToolTipText("Starting volume");
		spinnerStartingVolume.setBounds(276, 8, 64, 28);
		panel_Advanced.add(spinnerStartingVolume);
		spinnerStartingVolume.setEnabled(false);
		spinnerStartingVolume.setModel(new SpinnerNumberModel(100, 0, Values.MAX_VOUME, 5));

		chckbxEnableMediaControl = new JCheckBox("Enable media control keys");
		chckbxEnableMediaControl.setBounds(8, 38, 262, 28);
		panel_Advanced.add(chckbxEnableMediaControl);
		//TODO: Update ToolTipText, when JNativeHook got a fix
		chckbxEnableMediaControl.setToolTipText("<html>If enabled you can use the media control keys on your keyboard to control the bot.<br>This will increase your CPU and RAM usage. Currently this does not work on all systems.</html>");

		chckbxAutoReconnect = new JCheckBox("Auto Reconnect");
		chckbxAutoReconnect.setToolTipText("Sets whether or not JDA should try to reconnect if a connection-error is encountered.");
		chckbxAutoReconnect.setBounds(8, 68, 262, 28);
		panel_Advanced.add(chckbxAutoReconnect);

		chckbxNativeAudioSystem = new JCheckBox("Use Native Audio System");
		chckbxNativeAudioSystem.setToolTipText("Gets rid of stuttering caused by GC pauses. This may not work on all systems.");
		chckbxNativeAudioSystem.setBounds(8, 98, 262, 28);
		panel_Advanced.add(chckbxNativeAudioSystem);
		chckbxAllowCustomVolume.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerStartingVolume.setEnabled(chckbxAllowCustomVolume.isSelected());
			}
		});

		JButton btnApply = new JButton("Apply");
		btnApply.setBounds(280, 286, 90, 28);
		getContentPane().add(btnApply);
		btnApply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnApply.setEnabled(false);

				Config.set(Config.BOT_TOKEN, bottoken.getText());
				Config.set(Config.CONTROL_CHANNEL, controlchannel.getText());
				Config.set(Config.COMMAND_PREFIX, commandprefix.getText());
				Config.set(Config.VOICE_CHANNEL, voicechannel.getText());
				Config.set(Config.ADMINS_ROLE, adminsrole.getText());
				Config.set(Config.DISPLAY_SONG_AS_GAME, String.valueOf(display_song_as_game.isSelected()));
				Config.set(Config.UPDATE_CHECK_INTERVAL_HOURS, (periodic_update_check_box.isSelected() ? update_check_interval_hours_spinner.getValue().toString() : "0"));
				Config.set(Config.ALLOW_CUSTOM_VOLUME, String.valueOf(chckbxAllowCustomVolume.isSelected()));
				Config.set(Config.STARTING_VOLUME, spinnerStartingVolume.getValue().toString());
				Config.set(Config.ENABLE_MEDIA_CONTROL_KEYS, String.valueOf(chckbxEnableMediaControl.isSelected()));
				Config.set(Config.AUTO_RECONNECT, String.valueOf(chckbxAutoReconnect.isSelected()));
				Config.set(Config.USE_NATIVE_AUDIO_SYSTEM, String.valueOf(chckbxNativeAudioSystem.isSelected()));

				if(Config.save()) {
					// Do nothing, close the window
				} else {
					JOptionPane.showMessageDialog(instance, "Failed to save config.", Values.BOT_NAME, JOptionPane.ERROR_MESSAGE);
				}

				closeWindow();

				GUI_Main.mvToFront();
			}
		});

		if(read()) {
			setVisible(true);
		} else {
			closeWindow();
			// Re-enable Config chooser (allow to select a new one)
			GUI_Main.setConfigChooserEnabled(true);
		}
	}

	private boolean read() {
		boolean successfullyInitialized = Config.init(configFile, true);

		if(!successfullyInitialized) {
			return false;
		}

		commandprefix.setText(Config.get(Config.COMMAND_PREFIX));
		controlchannel.setText(Config.get(Config.CONTROL_CHANNEL));
		display_song_as_game.setSelected(Config.getBoolean(Config.DISPLAY_SONG_AS_GAME));
		chckbxEnableMediaControl.setSelected(Config.getBoolean(Config.ENABLE_MEDIA_CONTROL_KEYS));
		voicechannel.setText(Config.get(Config.VOICE_CHANNEL));

		if(a.isDisableUpdateChecker()) {
			periodic_update_check_box.setEnabled(false);
		} else {
			int updateH = Integer.parseInt(Config.get(Config.UPDATE_CHECK_INTERVAL_HOURS));
			if (updateH != 0) {
				periodic_update_check_box.setSelected(true);
				update_check_interval_hours_spinner.setEnabled(true);
				update_check_interval_hours_spinner.setValue(updateH);
			}
		}

		spinnerStartingVolume.setValue(Integer.parseInt(Config.get(Config.STARTING_VOLUME)));

		chckbxAllowCustomVolume.setSelected(Config.getBoolean(Config.ALLOW_CUSTOM_VOLUME));
		spinnerStartingVolume.setEnabled(chckbxAllowCustomVolume.isSelected());

		chckbxAutoReconnect.setSelected(Config.getBoolean(Config.AUTO_RECONNECT));
		chckbxNativeAudioSystem.setSelected(Config.getBoolean(Config.USE_NATIVE_AUDIO_SYSTEM));

		String token = Config.get(Config.BOT_TOKEN);
		if(token.isEmpty()) {
			bottoken.setSize(124, bottoken.getHeight());
			btnGet.setVisible(true);
		} else {
			bottoken.setText(token);
		}

		String adminsRoleStr = Config.get(Config.ADMINS_ROLE);
		if(adminsRoleStr.isEmpty()) {
			adminsrole.setEnabled(false);
		} else {
			adminsrole.setText(adminsRoleStr);
			chckbxAdminsRole.setSelected(true);
		}

		return successfullyInitialized;
	}

	private void hideGetButton() {
		btnGet.setVisible(false);
		bottoken.setSize(206, bottoken.getHeight());
	}

	private void closeWindow() {
		dispose();
		instance = null;
	}
}
