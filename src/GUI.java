import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class GUI extends JFrame {

	private static final File DEFAULT_CONFIG_FILE = new File(Values.DEFAULT_CONFIG);

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
		setTitle(Values.BOT_NAME + " " + Values.BOT_VERSION);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 210);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);

		lblConfig = new JLabel("Config:");
		lblConfig.setBounds(12, 12, 70, 26);
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
		chckbxUseCustomConfig.setBounds(96, 12, 166, 26);
		contentPane.add(chckbxUseCustomConfig);

		btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if(chckbxUseCustomConfig.isSelected()) {
						Desktop.getDesktop().open(new File(txtCustomconfig.getText()));
					} else {
						Desktop.getDesktop().open(DEFAULT_CONFIG_FILE);
					}
				} catch (Exception e1) {
					showErrMsgBox("Failed to open config with default editor.");
				}
			}
		});
		btnEdit.setFocusable(false);
		btnEdit.setBounds(370, 12, 117, 28);
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
		btnBrowse.setBounds(370, 52, 117, 28);
		contentPane.add(btnBrowse);

		btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnStart.setEnabled(false);
				lblCurrstatus.setText("Starting");
				new Thread(new Runnable() {
					@Override
					public void run() {
						if(chckbxUseCustomConfig.isSelected()) {
							a.launch(new String[]{"--config", new File(txtCustomconfig.getText()).getAbsolutePath()});
						} else {
							a.launch(new String[]{});
						}
						lblCurrstatus.setText("Started");
					}
				}).start();
			}
		});
		btnStart.setFocusable(false);
		btnStart.setBounds(12, 103, 476, 28);
		contentPane.add(btnStart);

		lblStatus = new JLabel("Status:");
		lblStatus.setBounds(12, 143, 70, 28);
		contentPane.add(lblStatus);

		lblCurrstatus = new JLabel("Stopped");
		lblCurrstatus.setBounds(95, 143, 392, 28);
		contentPane.add(lblCurrstatus);
	}

	static void onErrExit(String msg) {
		showErrMsgBox("Bot crashed." + (msg == null ? "" : (" Reason:" + System.lineSeparator() + msg)));
	}

	private static void showErrMsgBox(String msg) {
		JOptionPane.showMessageDialog(null, msg, Values.BOT_NAME, JOptionPane.ERROR_MESSAGE);
	}

}
