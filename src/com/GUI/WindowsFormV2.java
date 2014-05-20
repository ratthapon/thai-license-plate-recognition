package com.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import system.ProcessingCore;

public class WindowsFormV2 extends JFrame {

	private JPanel panel1;
	private JPanel panel2;
	private JLabel lblLicensePlateRecognition;
	private JLabel label;
	private JTextField textField;
	private JButton btnOpenFile;
	private static JTextField pltLabel;
	private static JLabel outLabel;
	private static JLabel inLabel;
	private JLabel inforLabel;
	private static JLabel videoLabel;
	private static JLabel outLabel2;
	private static JTextField pltLabel2;
	private static ProcessingCore core;
	private static JTabbedPane tabPane;
	private JLabel lblFilePath;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
					WindowsFormV2 frame = new WindowsFormV2();
					frame.setVisible(true);
					core = new ProcessingCore(inLabel, outLabel, pltLabel, true);
					core.setDebugMode(false);
					//core.start();
				} catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}

			}
		});
	}

	/**
	 * Create the application.
	 */
	public WindowsFormV2() {
		// initialize();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		setTitle("License Plate Recognition");

		/*
		 * frame = new JFrame("License Plate Recognition"); frame.setBounds(100,
		 * 100, 800, 600); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 * frame.getContentPane().setLayout(null); //frame.setExtendedState(5);
		 */

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		getContentPane().add(mainPanel);

		itemTabPanel1();
		itemTabPanel2();

		tabPane = new JTabbedPane();
		tabPane.addTab("�Ҿ�ҡ���ͧ", panel1);
		{
			lblLicensePlateRecognition = new JLabel("License Plate Recognition");
			lblLicensePlateRecognition.setFont(new Font("Tahoma", Font.PLAIN,
					30));
			lblLicensePlateRecognition.setBounds(20, 11, 345, 43);
			panel1.add(lblLicensePlateRecognition);
		}
		tabPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				final int WEBCAM_TAB = 0;
				final int VIDEO_TAB = 1;
				switch (tabPane.getSelectedIndex()) {
				case WEBCAM_TAB:
					core.pause();
					core = new ProcessingCore(inLabel, outLabel, pltLabel, true);
					core.setDebugMode(false);
					// core.start();
					break;
				case VIDEO_TAB:
					core.pause();
					break;

				default:
					break;
				}

			}
		});

		textField = new JTextField();
		textField.setFont(new Font("Tahoma", Font.PLAIN, 40));
		textField.setBounds(120, 430, 300, 80);
		panel1.add(textField);
		textField.setColumns(10);

		JButton btnOpen = new JButton("OPEN");
		btnOpen.setFont(new Font("Tahoma", Font.PLAIN, 30));
		btnOpen.setBounds(528, 455, 136, 55);
		panel1.add(btnOpen);

		JLabel lblInformation = new JLabel("Information:");
		lblInformation.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblInformation.setBounds(20, 384, 139, 35);
		panel1.add(lblInformation);

		pltLabel = new JTextField("");
		pltLabel.setForeground(Color.WHITE);
		pltLabel.setBackground(UIManager.getColor("Button.focus"));
		pltLabel.setFont(new Font("Tahoma", Font.PLAIN, 40));
		pltLabel.setBounds(450, 285, 300, 80);
		// set border
		Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
		pltLabel.setBorder(border);
		panel1.add(pltLabel);

		outLabel = new JLabel("ouput");
		outLabel.setBounds(450, 65, 300, 200);
		outLabel.setBorder(border);
		panel1.add(outLabel);

		inLabel = new JLabel("input");
		inLabel.setBounds(20, 65, 400, 300);
		inLabel.setBorder(border);
		panel1.add(inLabel);

		inforLabel = new JLabel("");
		inforLabel.setFont(new Font("Tahoma", Font.PLAIN, 25));
		inforLabel.setBounds(165, 384, 255, 30);
		inforLabel.setBorder(border);
		panel1.add(inforLabel);

		tabPane.addTab("�Ҿ�ҡ����մ���", panel2);
		{
			label = new JLabel("License Plate Recognition");
			label.setFont(new Font("Tahoma", Font.PLAIN, 30));
			label.setBounds(20, 11, 345, 43);
			panel2.add(label);
		}

		btnOpenFile = new JButton("Open file...");
		btnOpenFile.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnOpenFile.setBounds(20, 376, 141, 33);
		btnOpenFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				File currentDir = new File("F:\\SkyDrive\\Workspace\\OpenCV");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(currentDir);
				int returnVal = fileChooser.showDialog(null, "Open");
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					
					String filePath = fileChooser.getSelectedFile().getAbsolutePath();
					ProcessingCore.setFilePath(filePath);
					lblFilePath.setText(filePath);
					core = new ProcessingCore(videoLabel, outLabel2, pltLabel2,
							false);
					core.setDebugMode(false);
				}
			}
		});

		panel2.add(btnOpenFile);

		videoLabel = new JLabel("video input");
		videoLabel.setBounds(20, 65, 400, 300);
		videoLabel.setBorder(border);
		panel2.add(videoLabel);

		outLabel2 = new JLabel("ouput");
		outLabel2.setBounds(450, 65, 300, 200);
		outLabel2.setBorder(border);
		panel2.add(outLabel2);

		pltLabel2 = new JTextField("");
		pltLabel2.setFont(new Font("Tahoma", Font.PLAIN, 40));
		pltLabel2.setBackground(Color.BLACK);
		pltLabel2.setBounds(450, 285, 300, 80);
		pltLabel2.setBorder(border);
		panel2.add(pltLabel2);

		JButton btnRunVideo = new JButton("Run");
		btnRunVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (core != null) {
					core.start();
				}
				
			}
		});
		btnRunVideo.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnRunVideo.setBounds(171, 376, 141, 33);
		panel2.add(btnRunVideo);

		lblFilePath = new JLabel("");
		lblFilePath.setBounds(20, 420, 400, 14);
		panel2.add(lblFilePath);
		mainPanel.add(tabPane);

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void itemTabPanel1() {
		panel1 = new JPanel();
		panel1.setLayout(null);

		// frame.setExtendedState(5);

	}

	private void itemTabPanel2() {
		panel2 = new JPanel();
		panel2.setLayout(null);

	}
}
