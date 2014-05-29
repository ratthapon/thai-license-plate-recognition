package input.video;

import java.awt.EventQueue;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.BorderLayout;

import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;

import java.awt.Color;

import javax.swing.SwingConstants;

import java.awt.Font;

import javax.swing.border.LineBorder;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JButton;








//
import java.awt.*;
import java.awt.image.BufferedImage;  

import javax.swing.*;  

import org.opencv.core.Mat;  
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;  
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class MyWindowsForm {
	private JFrame frame;
	public static JLabel label0;
	public static JLabel label_1;
	public static JLabel label_2;
	public static JLabel lblTime;
	public static JLabel lblDate;
	public static JLabel lblInformation;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					System.loadLibrary("opencv_java248");
					
					MyWindowsForm window = new MyWindowsForm();
					window.frame.setVisible(true);
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws InterruptedException 
	 */
	public MyWindowsForm() {
			initialize();
			Thread t = new Test();
			t.start();
			Thread tim = new TestTime();
			tim.start();
			Thread dat = new TestDate();
			dat.start();
			Thread plat = new TestPlate();
			plat.start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setVisible(true);
		frame.setBounds(100, 100, 1024, 768);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		label0 = new JLabel();
		label0.setPreferredSize(new Dimension(640,480));
		label0.setSize(640, 480);
		label0.setBounds(10, 11, 640, 480);
		label0.setBackground(Color.WHITE);
		label0.setBorder(new LineBorder(new Color(0, 0, 0)));
		label0.setForeground(Color.BLACK);
		frame.getContentPane().add(label0);
		
		label_1 = new JLabel();
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setPreferredSize(new Dimension(303,216));
		label_1.setBounds(10, 502, 303, 216);
		label_1.setBackground(Color.WHITE);
		label_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		frame.getContentPane().add(label_1);
		
		label_2 = new JLabel();
		label_2.setBounds(323, 502, 327, 216);
		label_2.setBackground(Color.WHITE);
		label_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		frame.getContentPane().add(label_2);
		
		lblDate = new JLabel();
		lblDate.setBounds(692, 32, 259, 50);
		lblDate.setFont(new Font("Tahoma", Font.BOLD, 24));
		frame.getContentPane().add(lblDate);
		
		lblTime = new JLabel();
		lblTime.setBounds(692, 93, 259, 50);
		lblTime.setFont(new Font("Tahoma", Font.BOLD, 24));
		frame.getContentPane().add(lblTime);
		
		lblInformation = new JLabel("Information");
		lblInformation.setBackground(Color.DARK_GRAY);
		lblInformation.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblInformation.setHorizontalAlignment(SwingConstants.CENTER);
		lblInformation.setBounds(743, 190, 184, 27);
		frame.getContentPane().add(lblInformation);
		
		JButton btnNewButton = 	new JButton("New button");
		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.print("Ahh yesss! ");
			}
		});
		btnNewButton.setBounds(755, 589, 155, 50);
		frame.getContentPane().add(btnNewButton);
		
		JTextPane textPane = new JTextPane();
		textPane.setFont(new Font("Tahoma", Font.PLAIN, 36));
		textPane.setBounds(660, 228, 338, 263);
		frame.getContentPane().add(textPane);
		
	}
}

