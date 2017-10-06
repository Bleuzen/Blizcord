package me.bleuzen.blizcord;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.output.FileWriterWithEncoding;

import me.bleuzen.blizcord.bot.AudioPlayerThread;
import me.bleuzen.blizcord.bot.Bot;

@SuppressWarnings("serial")
public class GUI extends JFrame {

	private static final File DEFAULT_CONFIG_FILE = Config.getDefaultConfig();

	private static Image icon;
	static Image getIcon() {
		return icon;
	}
	private static boolean iconSet;
	static boolean isIconSet() {
		return iconSet;
	}

	private static GUI instance;

	private JPanel contentPane;
	private JLabel lblConfig;
	private JCheckBox chckbxUseCustomConfig;
	private JButton btnEdit;
	private JTextField txtCustomconfig;
	private JButton btnBrowse;
	private JButton btnStart;
	private JLabel lblStatus;
	private JLabel lblCurrstatus;
	private JLabel lblVersion;
	private JPanel panelControls;
	private JButton btnAdd;
	private JToggleButton tglbtnPause;
	private JButton btnStop;
	private JButton btnNext;

	private File addFileChooserDir;
	private JPanel panelUpdate;
	private JLabel lblANewVersion;
	private JButton btnUpdate;

	public GUI() {
		instance = this;

		setTitle(Values.BOT_NAME);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 200);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);

		try {
			InputStream imgStream = GUI.class.getResourceAsStream("icon.png");
			icon = ImageIO.read(imgStream);
			iconSet = true;
			setIconImage(icon);
			imgStream.close();
		} catch(Exception e) {
			Log.debug("Failed to set icon.");
		}

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if(GUI_Config.instance != null && GUI_Config.instance.isVisible()) {
					GUI_Config.instance.toFront();
				}
			}
		});

		lblConfig = new JLabel("Config:");
		lblConfig.setBounds(10, 12, 70, 28);
		contentPane.add(lblConfig);

		chckbxUseCustomConfig = new JCheckBox("Use custom config");
		chckbxUseCustomConfig.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean selected = chckbxUseCustomConfig.isSelected();
				if(!selected) {
					txtCustomconfig.setText(DEFAULT_CONFIG_FILE.getAbsolutePath());
				}
				txtCustomconfig.setEnabled(selected);
				btnBrowse.setEnabled(selected);
			}
		});
		chckbxUseCustomConfig.setFocusable(false);
		chckbxUseCustomConfig.setBounds(96, 12, 264, 28);
		contentPane.add(chckbxUseCustomConfig);

		btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// disable some controls (because config gets already initialized, cannot get changed anymore)
				btnBrowse.setEnabled(false);
				txtCustomconfig.setEnabled(false);
				chckbxUseCustomConfig.setEnabled(false);
				btnEdit.setEnabled(false); // disable to prevent double clicking

				File cfgFile = getConfig();

				GUI_Config.instance = new GUI_Config(cfgFile);
				GUI_Config.instance.setVisible(true);
			}
		});
		btnEdit.setFocusable(false);
		btnEdit.setBounds(370, 12, 116, 28);
		contentPane.add(btnEdit);

		txtCustomconfig = new JTextField();
		txtCustomconfig.setEnabled(false);
		txtCustomconfig.setText(DEFAULT_CONFIG_FILE.getAbsolutePath());
		txtCustomconfig.setBounds(10, 52, 348, 28);
		contentPane.add(txtCustomconfig);

		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.setFileFilter(new FileNameExtensionFilter(Values.CONFIG_FILE_EXTENSION.toUpperCase() + " file", Values.CONFIG_FILE_EXTENSION));
				int r = fileChooser.showSaveDialog(null);
				if(r == JFileChooser.APPROVE_OPTION) {
					String fileName = fileChooser.getSelectedFile().getAbsolutePath();
					if(!fileName.endsWith(Values.CONFIG_FILE_EXTENSION)) {
						fileName += ("." + Values.CONFIG_FILE_EXTENSION);
					}

					txtCustomconfig.setText(fileName);
				}
			}
		});
		btnBrowse.setEnabled(false);
		btnBrowse.setFocusable(false);
		btnBrowse.setBounds(370, 52, 116, 28);
		contentPane.add(btnBrowse);

		btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File cfgFile = getConfig();

				if(!cfgFile.exists()) {
					showMsgBox("The config file doesn't exist already. Please use the 'Edit' button first.");
					return;
				}

				// disable controls
				btnStart.setEnabled(false);
				btnBrowse.setEnabled(false);
				txtCustomconfig.setEnabled(false);
				chckbxUseCustomConfig.setEnabled(false);
				btnEdit.setEnabled(false);

				lblCurrstatus.setText("Starting");

				new Thread(new Runnable() {
					@Override
					public void run() {

						// start bot
						if(chckbxUseCustomConfig.isSelected()) {
							setTitle(getTitle() + " - " + cfgFile.getName());
							Bot.launch(new String[]{"--config", cfgFile.getAbsolutePath()});
						} else {
							Bot.launch(new String[]{});
						}

						btnStart.setVisible(false);
						panelControls.setVisible(true);

						lblCurrstatus.setText("Started");
					}
				}).start();
			}
		});
		btnStart.setFocusable(false);
		btnStart.setBounds(10, 98, 474, 28);
		contentPane.add(btnStart);

		panelControls = new JPanel();
		panelControls.setBounds(10, 98, 474, 28);
		panelControls.setVisible(false);
		contentPane.add(panelControls);

		btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();

				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setMultiSelectionEnabled(true);

				if(addFileChooserDir != null) {
					// restore latest dir
					fileChooser.setCurrentDirectory(addFileChooserDir);
				}

				int r = fileChooser.showOpenDialog(instance);
				if(r == JFileChooser.APPROVE_OPTION) {
					// add all selected files
					File[] selected = fileChooser.getSelectedFiles();
					for(File f : selected) {
						AudioPlayerThread.addToPlaylist(f.getAbsolutePath());
					}

					// remember the latest dir
					File singleSelectedFile = fileChooser.getSelectedFile();
					if(singleSelectedFile.isFile()) {
						singleSelectedFile = singleSelectedFile.getParentFile();
					}
					addFileChooserDir = singleSelectedFile;
				}
			}
		});
		btnAdd.setFocusable(false);

		tglbtnPause = new JToggleButton("Pause");
		tglbtnPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AudioPlayerThread.togglePause();
			}
		});
		tglbtnPause.setFocusable(false);

		btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Bot.stopPlayer();
			}
		});
		btnStop.setFocusable(false);
		panelControls.setLayout(new GridLayout(0, 4, 0, 0));
		panelControls.add(btnAdd);

		btnNext = new JButton("Next");
		btnNext.setFocusable(false);
		btnNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AudioPlayerThread.getMusicManager().scheduler.nextTrack(1);
			}
		});
		panelControls.add(btnNext);
		panelControls.add(tglbtnPause);
		panelControls.add(btnStop);

		lblStatus = new JLabel("Status:");
		lblStatus.setBounds(14, 134, 66, 26);
		contentPane.add(lblStatus);

		lblCurrstatus = new JLabel("Stopped");
		lblCurrstatus.setBounds(88, 134, 134, 26);
		contentPane.add(lblCurrstatus);

		lblVersion = new JLabel("Version: " + Values.BOT_VERSION);
		lblVersion.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVersion.setBounds(344, 134, 134, 26);
		contentPane.add(lblVersion);

		panelUpdate = new JPanel();
		panelUpdate.setVisible(false);
		panelUpdate.setBorder(new LineBorder(null, 2, true));
		panelUpdate.setBounds(10, 174, 470, 30);
		contentPane.add(panelUpdate);
		panelUpdate.setLayout(new BorderLayout(0, 0));

		lblANewVersion = new JLabel("A new version is available!");
		lblANewVersion.setHorizontalAlignment(SwingConstants.CENTER);
		panelUpdate.add(lblANewVersion, BorderLayout.CENTER);

		btnUpdate = new JButton("Update");
		btnUpdate.setFocusable(false);
		btnUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Utils.openInBrowser("https://github.com/" + Values.BOT_GITHUB_REPO + "/releases");
				System.exit(0);
			}
		});
		panelUpdate.add(btnUpdate, BorderLayout.EAST);

		/* ----- */

		// Check for updates the first time
		if(!a.isDisableUpdateChecker()) {
			new Thread(Bot.getUpdateChecker()).start();
		}

		// Load the GUI Config
		File guiConfigFileCustomConfig = new File(System.getProperty("java.io.tmpdir"), "blizcordguicustomconfig");
		if(guiConfigFileCustomConfig.exists()) {
			try {
				// read
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(guiConfigFileCustomConfig), StandardCharsets.UTF_8));
				String readLine = bufferedReader.readLine(); // path to custom config in Base64
				bufferedReader.close();

				// decode and set
				String decodedFileName = new String(Base64.getDecoder().decode(readLine), StandardCharsets.UTF_8);
				File customConfig = new File(decodedFileName);
				if(customConfig.isFile()) { // ignore if it got deleted or is invalid
					txtCustomconfig.setText(customConfig.getAbsolutePath());
					txtCustomconfig.setEnabled(true);
					btnBrowse.setEnabled(true);
					chckbxUseCustomConfig.setSelected(true);
				}
			} catch(Exception e) {
				// Failed to read file, ignore
			}
		}

		// Save last custom config on exit if set
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				if(chckbxUseCustomConfig.isSelected()) {
					try {
						BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(guiConfigFileCustomConfig, StandardCharsets.UTF_8, false));
						writer.write(Base64.getEncoder().encodeToString(txtCustomconfig.getText().getBytes(StandardCharsets.UTF_8)));
						writer.close();
					} catch(Exception e) {
						// Ignore
						// maybe we don't have the write permission here, at least we tryed it ;)
					}
				} else {
					guiConfigFileCustomConfig.delete(); // delete if it exists
				}
			}
		}));
	}

	static void onErrExit(String msg) {
		showErrMsgBox("Crash!" + (msg == null ? "" : (" Reason:" + System.lineSeparator() + msg)));
	}

	static void showMsgBox(String msg) {
		JOptionPane.showMessageDialog(instance, msg, Values.BOT_NAME, JOptionPane.INFORMATION_MESSAGE);
	}

	static void showErrMsgBox(String msg) {
		JOptionPane.showMessageDialog(instance, msg, Values.BOT_NAME, JOptionPane.ERROR_MESSAGE);
	}

	static void mvToFront() {
		instance.toFront();
	}

	private File getConfig() {
		if (chckbxUseCustomConfig.isSelected()) {
			return new File(txtCustomconfig.getText());
		} else {
			return DEFAULT_CONFIG_FILE;
		}
	}

	public static void addToSever(String link) {
		int r = JOptionPane.showConfirmDialog(instance, "Do you want to add the bot to your server now?", Values.BOT_NAME, JOptionPane.YES_NO_OPTION);
		if(r == JOptionPane.YES_OPTION) {
			Utils.openInBrowser(link);
		} else if(r == JOptionPane.NO_OPTION) {
			System.exit(0);
		}
	}

	public static void settglbtnPauseSelected(boolean selected) {
		instance.tglbtnPause.setSelected(selected);
	}

	static void showUpdatePanel() {
		if(!instance.panelUpdate.isVisible()) {
			instance.panelUpdate.setVisible(true);
			instance.setSize(instance.getWidth(), instance.getHeight() + 50);
		}
	}
}
