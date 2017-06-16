import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Base64;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.io.output.FileWriterWithEncoding;

@SuppressWarnings("serial")
public class GUI extends JFrame {

	private static final File DEFAULT_CONFIG_FILE = new File(Values.DEFAULT_CONFIG);

	private static GUI gui_Main;
	private static GUI_Config gui_Config;

	private final JPanel contentPane;
	private final JLabel lblConfig;
	private final JCheckBox chckbxUseCustomConfig;
	private final JButton btnEdit;
	private final JTextField txtCustomconfig;
	private final JButton btnBrowse;
	private final JButton btnStart;
	private final JLabel lblStatus;
	private final JLabel lblCurrstatus;

	public GUI() {
		gui_Main = this;

		setTitle(Values.BOT_NAME + " v" + Values.BOT_VERSION);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 208);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if(gui_Config != null && gui_Config.isVisible()) {
					gui_Config.toFront();
				}
			}
		});

		lblConfig = new JLabel("Config:");
		lblConfig.setBounds(12, 12, 70, 28);
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

				gui_Config = new GUI_Config(cfgFile);
				gui_Config.setVisible(true);
			}
		});
		btnEdit.setFocusable(false);
		btnEdit.setBounds(370, 12, 116, 28);
		contentPane.add(btnEdit);

		txtCustomconfig = new JTextField();
		txtCustomconfig.setEnabled(false);
		txtCustomconfig.setText(DEFAULT_CONFIG_FILE.getAbsolutePath());
		txtCustomconfig.setBounds(12, 52, 346, 28);
		contentPane.add(txtCustomconfig);

		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(true);
				int r = fileChooser.showSaveDialog(null);
				if(r == JFileChooser.APPROVE_OPTION) {
					txtCustomconfig.setText(fileChooser.getSelectedFile().getAbsolutePath());
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
							a.launch(new String[]{"--config", cfgFile.getAbsolutePath()});
						} else {
							a.launch(new String[]{});
						}

						lblCurrstatus.setText("Started");
					}
				}).start();
			}
		});
		btnStart.setFocusable(false);
		btnStart.setBounds(12, 102, 474, 28);
		contentPane.add(btnStart);

		lblStatus = new JLabel("Status:");
		lblStatus.setBounds(12, 138, 70, 28);
		contentPane.add(lblStatus);

		lblCurrstatus = new JLabel("Stopped");
		lblCurrstatus.setBounds(95, 138, 388, 28);
		contentPane.add(lblCurrstatus);

		// Load the GUI Config
		File guiConfigFileCustomConfig = new File(System.getProperty("java.io.tmpdir"), "blizcordguicustomconfig");
		if(guiConfigFileCustomConfig.exists()) {
			try {
				// read
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(guiConfigFileCustomConfig), "UTF-8"));
				String readLine = bufferedReader.readLine(); // path to custom config in Base64
				bufferedReader.close();

				// decode and set
				String decodedFileName = new String(Base64.getDecoder().decode(readLine), "UTF-8");
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
						BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(guiConfigFileCustomConfig, Charset.forName("UTF-8"), false));
						writer.write(Base64.getEncoder().encodeToString(txtCustomconfig.getText().getBytes("UTF-8")));
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
		JOptionPane.showMessageDialog(null, msg, Values.BOT_NAME, JOptionPane.INFORMATION_MESSAGE);
	}

	static void showErrMsgBox(String msg) {
		JOptionPane.showMessageDialog(null, msg, Values.BOT_NAME, JOptionPane.ERROR_MESSAGE);
	}

	static void mvToFront() {
		gui_Main.toFront();
	}

	private File getConfig() {
		if (chckbxUseCustomConfig.isSelected()) {
			return new File(txtCustomconfig.getText());
		} else {
			return DEFAULT_CONFIG_FILE;
		}
	}
}
