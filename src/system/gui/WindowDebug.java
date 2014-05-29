package system.gui;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import system.ProcessingCore;

public class WindowDebug {

	private JFrame frame;
	private JTextField txtOutputText;
	private JTextField txtRealFPS;
	private JTextField txtExpectOutput;
	private JTextField acc;
	private JTextField txtPlateDetectionSpeed;
	private JTextField charRecognitionSpeed;
	private JTextField textField_6;
	private JTextField textField_7;
	private JTextField textField_8;
	private JTextField textField_9;
	private JLabel showImage1;
	private JLabel showImage2;
	private JLabel showImage3;
	private JLabel showImage4;

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
		ProcessingCore core = new ProcessingCore();
		JTextField[] statusTextFields = new JTextField[6];
		statusTextFields[0] = txtOutputText;
		statusTextFields[1] = txtRealFPS;
		statusTextFields[2] = txtExpectOutput;
		statusTextFields[3] = acc;
		statusTextFields[4] = txtPlateDetectionSpeed;
		statusTextFields[5] = charRecognitionSpeed;
		core.webcamDebugMode(showImage1, showImage2, showImage3, showImage4,
				statusTextFields);
		core.start();
		charRecognitionSpeed.grabFocus();

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

		txtOutputText = new JTextField();
		txtOutputText.setEditable(false);
		txtOutputText.setBounds(848, 41, 200, 20);
		frame.getContentPane().add(txtOutputText);
		txtOutputText.setColumns(10);

		txtRealFPS = new JTextField();
		txtRealFPS.setEditable(false);
		txtRealFPS.setBounds(848, 102, 200, 20);
		frame.getContentPane().add(txtRealFPS);
		txtRealFPS.setColumns(10);

		txtExpectOutput = new JTextField();
		txtExpectOutput.setBounds(848, 163, 200, 20);
		frame.getContentPane().add(txtExpectOutput);
		txtExpectOutput.setColumns(10);

		acc = new JTextField();
		acc.setEditable(false);
		acc.setBounds(848, 224, 200, 20);
		frame.getContentPane().add(acc);
		acc.setColumns(10);

		txtPlateDetectionSpeed = new JTextField();
		txtPlateDetectionSpeed.setEditable(false);
		txtPlateDetectionSpeed.setBounds(848, 285, 201, 20);
		frame.getContentPane().add(txtPlateDetectionSpeed);
		txtPlateDetectionSpeed.setColumns(10);

		charRecognitionSpeed = new JTextField();
		charRecognitionSpeed.setEditable(false);
		charRecognitionSpeed.setBounds(848, 352, 200, 20);
		frame.getContentPane().add(charRecognitionSpeed);
		charRecognitionSpeed.setColumns(10);

		textField_6 = new JTextField();
		textField_6.setBounds(848, 413, 200, 20);
		frame.getContentPane().add(textField_6);
		textField_6.setColumns(10);

		textField_7 = new JTextField();
		textField_7.setBounds(848, 474, 200, 20);
		frame.getContentPane().add(textField_7);
		textField_7.setColumns(10);

		textField_8 = new JTextField();
		textField_8.setBounds(848, 535, 200, 20);
		frame.getContentPane().add(textField_8);
		textField_8.setColumns(10);

		textField_9 = new JTextField();
		textField_9.setBounds(848, 596, 200, 20);
		frame.getContentPane().add(textField_9);
		textField_9.setColumns(10);

		JLabel lblOutputText = new JLabel("Output Text");
		lblOutputText.setBounds(848, 16, 200, 14);
		frame.getContentPane().add(lblOutputText);

		JLabel lblRecognitionFramePer = new JLabel(
				"Recognition frame per second (speed)");
		lblRecognitionFramePer.setBounds(848, 77, 200, 14);
		frame.getContentPane().add(lblRecognitionFramePer);

		JLabel lblExpectOutput = new JLabel("Expect output");
		lblExpectOutput.setBounds(848, 138, 200, 14);
		frame.getContentPane().add(lblExpectOutput);

		JLabel lblRecognitionAccuracy = new JLabel("Recognition accuracy ");
		lblRecognitionAccuracy.setBounds(848, 199, 200, 14);
		frame.getContentPane().add(lblRecognitionAccuracy);

		JLabel lblPlateDetectionSpeed = new JLabel("Plate detection speed");
		lblPlateDetectionSpeed.setBounds(848, 260, 200, 14);
		frame.getContentPane().add(lblPlateDetectionSpeed);

		JLabel lblCharRecognitionSpeed = new JLabel("Char recognition speed");
		lblCharRecognitionSpeed.setBounds(848, 327, 200, 14);
		frame.getContentPane().add(lblCharRecognitionSpeed);

	}
}
