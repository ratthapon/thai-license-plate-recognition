package system.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.border.BevelBorder;
import javax.swing.JLabel;

import java.awt.Font;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.JTextArea;
import javax.swing.JFormattedTextField;

import system.ProcessingCore;

public class NewWindowsForm {

	private JFrame frame;
	private JTextField dateText;
	private JTextField timeText;
	private JTextField inforText;
	private JTextField pltText;
	private JLabel fullImage;
	private JLabel plateImage;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					NewWindowsForm window = new NewWindowsForm();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Look and feel not set.");
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public NewWindowsForm() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("License Plate Recognition");
		frame.setBounds(100, 100, 500, 350);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		fullImage = new JLabel();
		fullImage.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		fullImage.setLocation(21, 56);
		fullImage.setBackground(Color.WHITE);
		frame.getContentPane().add(fullImage);
		fullImage.setSize(275, 170);
		
		JLabel lblLPR = new JLabel("License Plate Recognition");
		lblLPR.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblLPR.setBounds(21, 20, 233, 25);
		frame.getContentPane().add(lblLPR);
		
		JLabel lblDate = new JLabel("Date:");
		lblDate.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDate.setBounds(290, 7, 39, 17);
		frame.getContentPane().add(lblDate);
		
		JLabel lblTime = new JLabel("Time:");
		lblTime.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblTime.setBounds(290, 28, 34, 17);
		frame.getContentPane().add(lblTime);
		
		dateText = new JTextField();
		dateText.setEditable(false);
		dateText.setFont(new Font("Tahoma", Font.PLAIN, 14));
		dateText.setBounds(329, 7, 117, 17);
		frame.getContentPane().add(dateText);
		dateText.setColumns(10);
		
		timeText = new JTextField();
		timeText.setEditable(false);
		timeText.setFont(new Font("Tahoma", Font.PLAIN, 14));
		timeText.setBounds(329, 28, 117, 17);
		frame.getContentPane().add(timeText);
		timeText.setColumns(10);
		
		JLabel lblInfor = new JLabel("Information:");
		lblInfor.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblInfor.setBounds(21, 251, 122, 25);
		frame.getContentPane().add(lblInfor);
		
		inforText = new JTextField();
		inforText.setFont(new Font("Tahoma", Font.PLAIN, 16));
		inforText.setBounds(119, 253, 129, 20);
		frame.getContentPane().add(inforText);
		inforText.setColumns(10);
		
		JButton btnOpen = new JButton("OPEN");
		btnOpen.setForeground(Color.BLACK);
		btnOpen.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnOpen.setBounds(329, 245, 117, 33);
		frame.getContentPane().add(btnOpen);
		
		plateImage = new JLabel();
		plateImage.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		plateImage.setBackground(Color.WHITE);
		plateImage.setBounds(329, 56, 110, 60);
		frame.getContentPane().add(plateImage);
		
		pltText = new JTextField();
		 if (!pltText.hasFocus()) {
			 pltText.requestFocusInWindow();
		 }
		pltText.setFont(new Font("Tahoma", Font.PLAIN, 16));
		pltText.setBounds(329, 130, 110, 42);
		frame.getContentPane().add(pltText);
		pltText.setColumns(10);
	}
}
