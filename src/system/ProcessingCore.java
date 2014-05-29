package system;

import input.video.Panel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;

import ocr.text.recognition.OCR;
import ocr.text.segmentation.TextSegment;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import plate.detection.Band;
import plate.detection.Car;
import plate.detection.Plate;
import utils.StringFrequency;
import utils.Utils;

public class ProcessingCore {
	private int maxBandLimit = 1;

	private int processFrameRate = 30; // fps
	private double evaluationRate = 0.8; // time per sec
	private long startTime = 1;
	private long endTime = 1;

	private TimerTask task = null;
	private Timer timer = new Timer();

	private int accTime = 1;
	private int detectTime = 1;
	private static VideoCapture capture;

	public ProcessingCore() {
		System.loadLibrary("opencv_java248");
		// capture = new VideoCapture(0);
	}

	public void webcamProcess(JLabel showFullInputImageLabel,
			JLabel showPlateOutputImageLabel, JTextField outputTextField) {
		final Mat srcImage = new Mat();
		capture = new VideoCapture(0);
		final JLabel fullImagePanel = showFullInputImageLabel;
		final JLabel plateImagePanel = showPlateOutputImageLabel;
		final JTextField showOutputText = outputTextField;
		task = new TimerTask() {

			private StringFrequency resultFrequency = new StringFrequency();
			private int recognizedTime = 0;

			@Override
			public void run() {
				System.out.println("Running.Mode show image.");
				if (capture.isOpened()) {
					capture.read(srcImage);
					if (!srcImage.empty()) {
						Car car = new Car(srcImage);
						List<Band> bands = new ArrayList<>();
						bands = car.clipBands(maxBandLimit);
						List<Plate> plates = new ArrayList<Plate>();
						recognizedTime++;
						if (recognizedTime % processFrameRate == 0) {
							recognizedTime = 0;
							String outString = resultFrequency
									.getMax((int) (processFrameRate * evaluationRate));
							resultFrequency = new StringFrequency();
							if (outString.equalsIgnoreCase("") != true) {
								showOutputText.setText(outString);
								// reset output text
								(new Timer()).schedule(new TimerTask() {

									@Override
									public void run() {
										showOutputText.setText("");
									}
								}, 2000);
								;
								System.out.println(outString);
							}
						}
						List<Mat> charMatList;
						List<MatOfPoint> plateContours = new ArrayList<MatOfPoint>();
						for (Band band : bands) {
							plates.add(band.clipPlate2(car.toMat()));
						}
						String result = "";
						for (Plate plate : plates) {
							charMatList = new ArrayList<Mat>();
							charMatList = TextSegment
									.getListMatOfCharImage(plate.toMat());
							if (charMatList.size() <= 0) {
								continue;
							}
							int[] charCode = OCR
									.recognizeCharImage(charMatList);
							for (int i = 0; i < charCode.length; i++) {
								int c = charCode[i];
								if (charCode[i] >= 161) {
									c = 0x0e00 + (charCode[i] - 160);
								}
								result = result + String.format("%c", c);
							}
							resultFrequency.push(result);
							result = new String("");
							plateContours.add(Utils.rectToMatOfPoint(plate
									.getBoundingRect()));
						}
						Mat boundingRect = car.toMat();
						Imgproc.drawContours(boundingRect, plateContours, -1,
								new Scalar(255, 0, 0), 2);
						Imgproc.resize(
								boundingRect,
								boundingRect,
								new Size(
										fullImagePanel.getWidth(),
										(int) (fullImagePanel.getWidth()
												/ fullImagePanel.getWidth() * fullImagePanel
												.getHeight())));
						BufferedImage temp = Panel
								.matToBufferedImage(boundingRect);
						fullImagePanel.setIcon(new ImageIcon(temp));

						if (plates.size() >= 1) {
							Mat plate = plates.get(0).toMat();
							// Imgproc.drawContours(plate,
							// TextSegment.getBoundingRectPoint(), -1,
							// new Scalar(0, 0, 255), 2);
							Imgproc.resize(
									plate,
									plate,
									new Size(plateImagePanel.getWidth(),
											(int) (plateImagePanel.getWidth()
													* plate.height() / plate
													.width())));
							BufferedImage band = Panel
									.matToBufferedImage(plate);
							plateImagePanel.setIcon(new ImageIcon(band));
						}
					}
				}
			}
		};
	}

	public void videoProcess(JLabel showFullInputImageLabel,
			JLabel showPlateOutputImageLabel, JTextField outputTextField,
			String videoPath) {
		final Mat frame = new Mat();
		System.out.println("Set" + capture);
		capture = new VideoCapture("IMG_2002.mov");
		System.out.println("Set" + capture);
		final JLabel fullImagePanel = showFullInputImageLabel;
		final JLabel plateImagePanel = showPlateOutputImageLabel;
		final JTextField showOutputText = outputTextField;
		final String videoFIlePath = "IMG_2002_xvid.avi";
		task = new TimerTask() {

			private StringFrequency resultFrequency = new StringFrequency();
			private int recognizedTime = 0;

			@Override
			public void run() {
				System.out.println("Running.Video.");
				if (capture.isOpened()) {

					System.out.println("Capture video frame." + frame.size());
					if (!frame.empty()) {
						Car car = new Car(frame);
						List<Band> bands = new ArrayList<>();
						bands = car.clipBands(maxBandLimit);
						List<Plate> plates = new ArrayList<Plate>();
						recognizedTime++;
						if (recognizedTime % processFrameRate == 0) {
							recognizedTime = 0;
							String outString = resultFrequency
									.getMax((int) (processFrameRate * evaluationRate));
							resultFrequency = new StringFrequency();
							if (outString.equalsIgnoreCase("") != true) {
								showOutputText.setText(outString);
								// reset output text
								(new Timer()).schedule(new TimerTask() {

									@Override
									public void run() {
										showOutputText.setText("");
									}
								}, 2000);
								;
								System.out.println(outString);
							}
						}
						List<Mat> charMatList;
						List<MatOfPoint> plateContours = new ArrayList<MatOfPoint>();
						for (Band band : bands) {
							plates.add(band.clipPlate2(car.toMat()));
						}
						String result = "";
						for (Plate plate : plates) {
							charMatList = new ArrayList<Mat>();
							charMatList = TextSegment
									.getListMatOfCharImage(plate.toMat());
							if (charMatList.size() <= 0) {
								continue;
							}
							int[] charCode = OCR
									.recognizeCharImage(charMatList);
							for (int i = 0; i < charCode.length; i++) {
								int c = charCode[i];
								if (charCode[i] >= 161) {
									c = 0x0e00 + (charCode[i] - 160);
								}
								result = result + String.format("%c", c);
							}
							resultFrequency.push(result);
							result = new String("");
							plateContours.add(Utils.rectToMatOfPoint(plate
									.getBoundingRect()));
						}
						Mat boundingRect = car.toMat();
						Imgproc.drawContours(boundingRect, plateContours, -1,
								new Scalar(255, 0, 0), 2);
						Imgproc.resize(
								boundingRect,
								boundingRect,
								new Size(
										fullImagePanel.getWidth(),
										(int) (fullImagePanel.getWidth()
												/ fullImagePanel.getWidth() * fullImagePanel
												.getHeight())));
						BufferedImage temp = Panel
								.matToBufferedImage(boundingRect);
						fullImagePanel.setIcon(new ImageIcon(temp));

						if (plates.size() >= 1) {
							Mat plate = plates.get(0).toMat();
							// Imgproc.drawContours(plate,
							// TextSegment.getBoundingRectPoint(), -1,
							// new Scalar(0, 0, 255), 2);
							Imgproc.resize(
									plate,
									plate,
									new Size(plateImagePanel.getWidth(),
											(int) (plateImagePanel.getWidth()
													* plate.height() / plate
													.width())));
							BufferedImage band = Panel
									.matToBufferedImage(plate);
							plateImagePanel.setIcon(new ImageIcon(band));
						}
					}
				}
			}
		};
	}

	public void webcamDebugMode(JLabel panelTopLeft, JLabel panelTopRight,
			JLabel panelBottomLeft, JLabel panelBottomRight,
			JTextField[] textFields) {
		System.loadLibrary("opencv_java248");
		final Mat srcImage = new Mat();
		capture = new VideoCapture(0);

		final JLabel topLeft = panelTopLeft;
		final JLabel topRight = panelTopRight;
		final JLabel bottomLeft = panelBottomLeft;
		final JLabel bottomRight = panelBottomRight;
		final JTextField txtOutputText = textFields[0];
		final JTextField txtFrameRate = textFields[1];
		final JTextField txtExpectOutput = textFields[2];
		final JTextField txtAcc = textFields[3];
		final JTextField txtPlateDetectSpeed = textFields[4];
		final JTextField txtCharRecogSpeed = textFields[5];

		task = new TimerTask() {

			private StringFrequency resultFrequency = new StringFrequency();
			private int recognizedTime = 0;

			@Override
			public void run() {
				System.out.println("Running.Mode show image.");
				if (capture.isOpened()) {
					capture.read(srcImage);
					if (!srcImage.empty()) {
						long plateDetectionStartTime = System
								.currentTimeMillis();
						Car car = new Car(srcImage);
						List<Band> bands = new ArrayList<>();
						bands = car.clipBands(maxBandLimit);
						List<Plate> plates = new ArrayList<Plate>();
						recognizedTime++;
						if (recognizedTime % processFrameRate == 0) {
							recognizedTime = 0;
							String outString = resultFrequency
									.getMax((int) (processFrameRate * evaluationRate));
							resultFrequency = new StringFrequency();
							if (outString.equalsIgnoreCase("") != true) {
								txtOutputText.setText(outString);
								// show acc
								if (!(txtExpectOutput.getText()
										.equalsIgnoreCase(""))) {
									detectTime++;
									if (outString
											.equalsIgnoreCase(txtExpectOutput
													.getText())) {
										accTime++;

									}
									txtAcc.setText("ACC : "
											+ String.format(
													"%3.2f",
													(float) ((double) accTime * 100 / (double) detectTime))
											+ " % (EQUAL " + accTime
											+ "/DETECT " + detectTime + ")");
								}
								// reset output text
								(new Timer()).schedule(new TimerTask() {

									@Override
									public void run() {
										txtOutputText.setText("");
									}
								}, 2000);
								;
								System.out.println(outString);
							}
						}
						List<Mat> charMatList;
						List<MatOfPoint> bandContours = new ArrayList<MatOfPoint>();
						List<MatOfPoint> plateContours = new ArrayList<MatOfPoint>();
						for (Band band : bands) {
							plates.add(band.clipPlate2(car.toMat()));
							bandContours.add(Utils.rectToMatOfPoint(band
									.getBoundingRect()));
						}
						long plateDetectionEndTime = System.currentTimeMillis();

						txtPlateDetectSpeed
								.setText(""
										+ String.format(
												"%3.2f",
												(float) ((plateDetectionEndTime - plateDetectionStartTime) / 1000))
										+ " sec. "
										+ String.format(
												"%3.2f",
												(float) (1000 / (plateDetectionEndTime - plateDetectionStartTime)))
										+ " Plate / Second");

						String result = "";
						for (Plate plate : plates) {
							charMatList = new ArrayList<Mat>();
							charMatList = TextSegment
									.getListMatOfCharImage(plate.toMat());
							if (charMatList.size() <= 0) {
								continue;
							}
							int[] charCode = OCR
									.recognizeCharImage(charMatList);
							for (int i = 0; i < charCode.length; i++) {
								int c = charCode[i];
								if (charCode[i] >= 161) {
									c = 0x0e00 + (charCode[i] - 160);
								}
								result = result + String.format("%c", c);
							}
							resultFrequency.push(result);
							result = new String("");
							plateContours.add(Utils.rectToMatOfPoint(plate
									.getBoundingRect()));
						}
						Mat boundingRect = car.toMat();
						// Imgproc.drawContours(boundingRect, bandContours, -1,
						// new Scalar(0, 255, 0), 3);
						Imgproc.drawContours(boundingRect, plateContours, -1,
								new Scalar(255, 0, 0), 2);
						Imgproc.resize(
								boundingRect,
								boundingRect,
								new Size(topLeft.getWidth(),
										(int) (topLeft.getWidth()
												/ topLeft.getWidth() * topLeft
												.getHeight())));
						BufferedImage temp = Panel
								.matToBufferedImage(boundingRect);
						topLeft.setIcon(new ImageIcon(temp));

						if (plates.size() >= 1) {
							Mat plate = plates.get(0).toMat();
							// Imgproc.drawContours(plate,
							// TextSegment.getBoundingRectPoint(), -1,
							// new Scalar(0, 0, 255), 2);
							Imgproc.resize(
									plate,
									plate,
									new Size(bottomRight.getWidth(),
											(int) (bottomRight.getWidth()
													* plate.height() / plate
													.width())));
							BufferedImage band = Panel
									.matToBufferedImage(plate);
							bottomRight.setIcon(new ImageIcon(band));
						}

						// show bug session

						Mat edgeImage = Utils.histoGraph(
								Utils.verticalLine(car.toMat()), true, true);
						Imgproc.resize(
								edgeImage,
								edgeImage,
								new Size(
										400,
										(int) (400.0 / edgeImage.cols() * edgeImage
												.rows())));
						BufferedImage band = Panel
								.matToBufferedImage(edgeImage);
						topRight.setIcon(new ImageIcon(band));

						Mat debugHist = Utils.verticalLine(car.toMat());
						Imgproc.resize(
								debugHist,
								debugHist,
								new Size(
										400,
										(int) (400.0 / debugHist.cols() * debugHist
												.rows())));
						BufferedImage debugHistBuffer = Panel
								.matToBufferedImage(debugHist);
						bottomLeft.setIcon(new ImageIcon(debugHistBuffer));

						endTime = System.currentTimeMillis();
						txtFrameRate.setText("REAL FPS : "
								+ (1000 / (endTime - startTime)));
						startTime = endTime;
					}

				}
			}
		};
	}

	public void start() {
		if (task == null) {
			System.out.println("Can not start.Task is null.");
		} else {
			timer = new Timer();
			timer.scheduleAtFixedRate(task, 0, 1000 / processFrameRate);
		}

	}

	public void pause() {
		timer.cancel();
	}

}
