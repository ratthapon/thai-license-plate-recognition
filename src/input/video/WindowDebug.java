package input.video;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import system.ProcessingCore;

public class WindowDebug {

	private JFrame frame;
	public static JTextField text1;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JTextField textField_6;
	private JTextField textField_7;
	private JTextField textField_8;
	private JTextField textField_9;
	public static JLabel showImage1;
	public static JLabel showImage2;
	public static JLabel showImage3;
	public static JLabel showImage4;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WindowDebug window = new WindowDebug();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public WindowDebug() {
		initialize();
		ProcessingCore core = new ProcessingCore(showImage1, true);
		core.start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1100, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		showImage1 = new JLabel();
		showImage1.setBorder(new LineBorder(new Color(0, 0, 0)));
		showImage1.setBounds(10, 11, 400, 300);
		frame.getContentPane().add(showImage1);

		showImage2 = new JLabel();
		showImage2.setBorder(new LineBorder(new Color(0, 0, 0)));
		showImage2.setBounds(10, 322, 400, 300);
		frame.getContentPane().add(showImage2);

		showImage3 = new JLabel();
		showImage3.setBorder(new LineBorder(new Color(0, 0, 0)));
		showImage3.setBounds(420, 11, 400, 300);
		frame.getContentPane().add(showImage3);

		showImage4 = new JLabel();
		showImage4.setBorder(new LineBorder(new Color(0, 0, 0)));
		showImage4.setBounds(420, 322, 400, 299);
		frame.getContentPane().add(showImage4);

		text1 = new JTextField();
		text1.setBounds(848, 11, 200, 50);
		frame.getContentPane().add(text1);
		text1.setColumns(10);

		textField_1 = new JTextField();
		textField_1.setBounds(848, 72, 200, 50);
		frame.getContentPane().add(textField_1);
		textField_1.setColumns(10);

		textField_2 = new JTextField();
		textField_2.setBounds(848, 133, 200, 50);
		frame.getContentPane().add(textField_2);
		textField_2.setColumns(10);

		textField_3 = new JTextField();
		textField_3.setBounds(848, 194, 200, 50);
		frame.getContentPane().add(textField_3);
		textField_3.setColumns(10);

		textField_4 = new JTextField();
		textField_4.setBounds(848, 255, 201, 50);
		frame.getContentPane().add(textField_4);
		textField_4.setColumns(10);

		textField_5 = new JTextField();
		textField_5.setBounds(848, 322, 200, 50);
		frame.getContentPane().add(textField_5);
		textField_5.setColumns(10);

		textField_6 = new JTextField();
		textField_6.setBounds(848, 383, 200, 50);
		frame.getContentPane().add(textField_6);
		textField_6.setColumns(10);

		textField_7 = new JTextField();
		textField_7.setBounds(848, 444, 200, 50);
		frame.getContentPane().add(textField_7);
		textField_7.setColumns(10);

		textField_8 = new JTextField();
		textField_8.setBounds(848, 505, 200, 50);
		frame.getContentPane().add(textField_8);
		textField_8.setColumns(10);

		textField_9 = new JTextField();
		textField_9.setBounds(848, 566, 200, 50);
		frame.getContentPane().add(textField_9);
		textField_9.setColumns(10);
	}
}
