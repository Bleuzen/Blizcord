package com.github.bleuzen.blizcord.gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.output.FileWriterWithEncoding;

import com.github.bleuzen.blizcord.Config;
import com.github.bleuzen.blizcord.Log;
import com.github.bleuzen.blizcord.Utils;
import com.github.bleuzen.blizcord.Values;
import com.github.bleuzen.blizcord.a;
import com.github.bleuzen.blizcord.bot.AudioPlayerThread;
import com.github.bleuzen.blizcord.bot.Bot;

@SuppressWarnings("serial")
public class GUI_Main extends JFrame {

	private final static int WIDTH = 500;
	private final static int HEIGHT = 154;

	private static final File DEFAULT_CONFIG_FILE = Config.getDefaultConfig();

	private static Image icon;
	static Image getIcon() {
		return icon;
	}
	private static boolean iconSet;
	static boolean isIconSet() {
		return iconSet;
	}

	private static GUI_Main instance;

	private JPanel contentPane;
	private JLabel lblConfig;
	private JButton btnEdit;
	private JTextField txtConfig;
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
	private JLabel lblNewVersion;
	private JButton btnDownloadUpdate;

	public GUI_Main() {
		instance = this;

		setTitle(Values.BOT_NAME);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);

		try {
			InputStream imgStream = a.class.getResourceAsStream("icon.png");
			icon = ImageIO.read(imgStream);
			iconSet = true;
			setIconImage(icon);
			imgStream.close();
		} catch(Exception e) {
			Log.debug("Failed to set icon.");
		}

		lblConfig = new JLabel("Config:");
		lblConfig.setBounds(10, 12, 62, 28);
		contentPane.add(lblConfig);

		btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File cfgFile = getConfig();

				if(!cfgFile.exists()) {
					showMsgBox("The config file doesn't exist already. Please use the 'Edit' button first.");
					return;
				}

				// check if Config is already saved (by GUI_Config)
				// GUI_Config.instance gets reset to null after Config got saved
				// If not null, display an error
				if(GUI_Config.instance != null) {
					showMsgBox("You forgot to Apply (save) the config.");
					return;
				}

				// disable controls
				btnStart.setEnabled(false);
				txtConfig.setEnabled(false);
				btnEdit.setEnabled(false);

				lblCurrstatus.setText("Starting");

				new Thread(new Runnable() {
					@Override
					public void run() {

						boolean started;

						// start bot
						setTitle(getTitle() + " - " + cfgFile.getName());
						started = Bot.launch(new String[]{"--config", cfgFile.getAbsolutePath()});


						if(started) {

							btnStart.setVisible(false);
							panelControls.setVisible(true);

							lblCurrstatus.setText("Running");

						} else {
							// Failed to start

							// Reset Status
							lblCurrstatus.setText("Stopped");

							// Re-enable Start and Edit buttons
							btnStart.setEnabled(true);
							btnEdit.setEnabled(true);

						}

					}
				}).start();
			}
		});

		btnStart.setBounds(10, 52, 476, 28);
		contentPane.add(btnStart);

		btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setConfigChooserEnabled(false);

				File cfgFile = getConfig();

				if(GUI_Config.instance != null && GUI_Config.instance.isVisible()) {
					GUI_Config.instance.toFront();
				} else {
					GUI_Config.instance = new GUI_Config(cfgFile);
				}
			}
		});
		btnEdit.setFocusable(false);
		btnEdit.setBounds(370, 12, 116, 28);
		contentPane.add(btnEdit);

		txtConfig = new JTextField();
		txtConfig.setEditable(false);
		txtConfig.setBounds(72, 12, 292, 28);
		contentPane.add(txtConfig);
		txtConfig.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// Show nothing if config textfield is disabled
				if(!txtConfig.isEnabled()) {
					return;
				}

				if(SwingUtilities.isRightMouseButton(e)) {

					JPopupMenu contextMenu = new JPopupMenu(null);

					JMenuItem Browse = new JMenuItem("Browse");
					JMenuItem Reset = new JMenuItem("Reset");

					Browse.addActionListener(new ActionListener() {
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

								txtConfig.setText(fileName);
							}
						}
					});

					Reset.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							txtConfig.setText(DEFAULT_CONFIG_FILE.getAbsolutePath());
						}
					});

					contextMenu.add(Browse);
					contextMenu.add(Reset);
					contextMenu.show((Component) e.getSource(), e.getX(), e.getY());

				}
			}
		});
		txtConfig.setText(DEFAULT_CONFIG_FILE.getAbsolutePath());

		panelControls = new JPanel();
		panelControls.setBounds(10, 52, 476, 28);
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
						AudioPlayerThread.addToPlaylist(f.getAbsolutePath(), false);
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
		lblStatus.setBounds(14, 88, 66, 26);
		contentPane.add(lblStatus);

		lblCurrstatus = new JLabel("Stopped");
		lblCurrstatus.setBounds(88, 88, 134, 26);
		contentPane.add(lblCurrstatus);

		lblVersion = new JLabel("Version: " + Values.BOT_VERSION);
		lblVersion.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVersion.setBounds(346, 88, 134, 26);
		contentPane.add(lblVersion);

		panelUpdate = new JPanel();
		panelUpdate.setVisible(false);
		panelUpdate.setBorder(new LineBorder(null, 2, true));
		panelUpdate.setBounds(10, (HEIGHT - 26), 470, 30);
		contentPane.add(panelUpdate);
		panelUpdate.setLayout(new BorderLayout(0, 0));

		lblNewVersion = new JLabel();
		lblNewVersion.setHorizontalAlignment(SwingConstants.CENTER);
		panelUpdate.add(lblNewVersion, BorderLayout.CENTER);

		btnDownloadUpdate = new JButton("Download");
		btnDownloadUpdate.setFocusable(false);
		btnDownloadUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onClickDownloadUpdateButton();
			}
		});
		panelUpdate.add(btnDownloadUpdate, BorderLayout.EAST);

		/* ----- */

		// Check for updates the first time
		if(!a.isDisableUpdateChecker()) {
			new Thread(Bot.getUpdateChecker()).start();
		}

		// Load the GUI Config
		File guiSetBotConfigFile = new File(System.getProperty("java.io.tmpdir"), "blizcordguicustomconfig");
		if(guiSetBotConfigFile.exists()) {
			try {
				// read
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(guiSetBotConfigFile), StandardCharsets.UTF_8));
				String readLine = bufferedReader.readLine(); // path to custom config in Base64
				bufferedReader.close();

				// decode and set
				String decodedFileName = new String(Base64.getDecoder().decode(readLine), StandardCharsets.UTF_8);
				File loadConfig = new File(decodedFileName);
				if(loadConfig.isFile()) { // ignore if it got deleted or is invalid
					txtConfig.setText(loadConfig.getAbsolutePath());
				}
			} catch(Exception e) {
				// Failed to read file, ignore
			}
		}

		// Save last custom config on exit if set
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(guiSetBotConfigFile, StandardCharsets.UTF_8, false));
					writer.write(Base64.getEncoder().encodeToString(txtConfig.getText().getBytes(StandardCharsets.UTF_8)));
					writer.close();
				} catch(Exception e) {
					// Ignore
					// maybe we don't have the write permission here, at least we tryed it ;)
				}
			}
		}));
	}

	public static void onErrExit(String msg) {
		showErrMsgBox("Crash!" + (msg == null ? "" : (" Reason:" + System.lineSeparator() + msg)));
	}

	public static void showMsgBox(String msg) {
		JOptionPane.showMessageDialog(instance, msg, Values.BOT_NAME, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void showErrMsgBox(String msg) {
		JOptionPane.showMessageDialog(instance, msg, Values.BOT_NAME, JOptionPane.ERROR_MESSAGE);
	}

	static void mvToFront() {
		instance.toFront();
	}

	private File getConfig() {
		return new File(txtConfig.getText());
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

	public static void showUpdatePanel() {
		if(!Utils.getOS().equals(Values.OS_WINDOWS)) {
			// Hide the update button for non Windows users (because currently only .exe file is on the download page)
			instance.btnDownloadUpdate.setVisible(false);
		}

		if(!instance.panelUpdate.isVisible()) {
			instance.panelUpdate.setVisible(true);
			instance.setSize(WIDTH, HEIGHT + 50);
		}
		instance.lblNewVersion.setText("A new version is available: " + Bot.getUpdateChecker().getLatestTag());
	}

	private void onClickDownloadUpdateButton() {
		Utils.openInBrowser(Bot.getUpdateChecker().getLatestTagUrl());
		System.exit(0);
	}

	static void setConfigChooserEnabled(boolean e) {
		instance.txtConfig.setEnabled(e);

		//TODO: No browse, reset
	}
}
