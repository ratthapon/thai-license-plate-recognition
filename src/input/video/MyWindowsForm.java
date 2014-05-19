package input.video;

import java.awt.Color;
//
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.opencv.highgui.Highgui;

import system.ProcessingCore;

public class MyWindowsForm {
	private JFrame frame;
	public static JLabel showImage;
	public static JLabel label_1;
	public static JLabel label_2;
	public static JLabel lblTime;
	public static JLabel lblDate;
	public static JLabel lblInformation;
	public static JButton captureBtn;
	
	private int banNum = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
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
			ProcessingCore core = new ProcessingCore(showImage,true);
			core.start();
			Thread tim = new TestTime();
			tim.start();
			Thread dat = new TestDate();
			dat.start();
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
		
		showImage = new JLabel();
		showImage.setPreferredSize(new Dimension(640,480));
		showImage.setSize(640, 480);
		showImage.setBounds(10, 11, 640, 480);
		showImage.setBackground(Color.WHITE);
		showImage.setBorder(new LineBorder(new Color(0, 0, 0)));
		showImage.setForeground(Color.BLACK);
		frame.getContentPane().add(showImage);
		
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
		
		captureBtn = new JButton("New button");
		captureBtn.setFont(new Font("Tahoma", Font.PLAIN, 14));
		captureBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Highgui.imwrite("C:/Project/LPR/RES/band/"+(banNum++)+".jpg", ProcessingCore.captureBand.toMat());
			}
		});
		captureBtn.setBounds(755, 589, 155, 50);
		frame.getContentPane().add(captureBtn);
		
		JTextPane textPane = new JTextPane();
		textPane.setFont(new Font("Tahoma", Font.PLAIN, 36));
		textPane.setBounds(660, 228, 338, 263);
		frame.getContentPane().add(textPane);
		
	}
}

