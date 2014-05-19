package system;

import input.video.MyWindowsForm;
import input.video.Panel;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import ocr.text.recognition.OCR;
import ocr.text.segmentation.TextSegment;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import plate.detection.Band;
import plate.detection.Car;
import plate.detection.Plate;
import utils.Utils;

public class ProcessingCore {
	private static ProcessingCore core = null;

	public static String logtag = "";
	private static int testCount = 0;
	private static int foundCount = 0;
	private static int notFoundCOunt = 0;
	private static int isPlateCount = 0;

	private static int maxBandLimit = 1;
	private static boolean useWebCam = true;
	private static JLabel showLabel = null;
	final int processFrameRate = 10; // fps
	TimerTask task = null;
	Timer timer = new Timer();

	public ProcessingCore() {
		final Mat srcImage = new Mat();
		final VideoCapture capture = new VideoCapture(0);
		task = new TimerTask() {

			@Override
			public void run() {
				System.out.println("Running.Mode console.");
				if (capture.isOpened()) {
					capture.read(srcImage);
					if (!srcImage.empty()) {
						Car car = new Car(srcImage);
						System.out.println(car.readPlate());
					}
				}
			}
		};
	}

	public ProcessingCore(JLabel showLabel, boolean webCamMode) {
		System.loadLibrary("opencv_java248");
		final Mat srcImage = new Mat();
		final VideoCapture capture = new VideoCapture(0);
		ProcessingCore.showLabel = showLabel;
		task = new TimerTask() {

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
						List<String> resultArray = new ArrayList<>();
						List<Mat> charMatList;
						List<MatOfPoint> bandContours = new ArrayList<MatOfPoint>();
						List<MatOfPoint> plateContours = new ArrayList<MatOfPoint>();
						for (Band band : bands) {
							plates.add(band.clipPlate2(car.toMat()));
							bandContours.add(Utils.rectToMatOfPoint(band
									.getBoundingRect()));
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
							resultArray.add(result);
							result = new String("");
							plateContours.add(Utils.rectToMatOfPoint(plate
									.getBoundingRect()));
						}
						if (resultArray.size() >= 1) {
							MyWindowsForm.lblInformation.setText(resultArray
									.toString());
							System.out.println(resultArray);
						}
						Mat boundingRect = car.toMat();
						Imgproc.drawContours(boundingRect, bandContours, -1,
								new Scalar(0, 255, 0), 3);
						Imgproc.drawContours(boundingRect, plateContours, -1,
								new Scalar(255, 0, 0), 2);
						BufferedImage temp = Panel.matToBufferedImage(boundingRect);
						//BufferedImage temp = Panel.matToBufferedImage(Utils.histoGraph(Utils.verticalLine(bands.get(0).toMat()), true, true));
						ProcessingCore.showLabel.setIcon(new ImageIcon(temp));
					}
				}
			}
		};
	}

	private static List<String> readPlate(Mat carMat) {
		// load car image
		System.loadLibrary("opencv_java248");
		Car car = new Car(carMat);
		// detect all plate
		long detectPlateStartTime = (new Date()).getTime();
		List<Plate> plates = new ArrayList<Plate>();
		plates = car.clipPlatesMaxBandLimit(3);
		long detectPlateStopTime = (new Date()).getTime();
		System.out.println("Detect plates "
				+ ((detectPlateStopTime - detectPlateStartTime) / 1000.0)
				+ " sec.");
		String result = "";
		List<String> resultArray = new ArrayList<>();
		List<Mat> charMatList;
		for (Plate plate : plates) {
			long recogCharStartTime = (new Date()).getTime();
			charMatList = new ArrayList<Mat>();
			charMatList = TextSegment.getListMatOfCharImage(plate.toMat());
			long recogCharStopTime = (new Date()).getTime();
			System.out.println("Characters detect and recognize "
					+ ((recogCharStopTime - recogCharStartTime) / 1000.0)
					+ " sec.");
			if (charMatList.size() <= 0) {
				continue;
			}
			int[] charCode = OCR.recognizeCharImage(charMatList);
			for (int i = 0; i < charCode.length; i++) {
				int c = charCode[i];
				if (charCode[i] >= 161) {
					c = 0x0e00 + (charCode[i] - 160);
				}
				result = result + String.format("%c", c);
			}
			resultArray.add(result);
			result = new String("");
		}
		return resultArray;
	}

	public static void testDetectPlates(String fileNameList) {
		System.loadLibrary("opencv_java248");
		FileReader fileReader;
		BufferedReader bufferedReader;
		List<String> lines;
		String line;
		try {
			fileReader = new FileReader(fileNameList);
			bufferedReader = new BufferedReader(fileReader);
			lines = new ArrayList<String>();
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
			String[] filename = lines.toArray(new String[lines.size()]);

			for (int i = 0; i < filename.length; i++) {
				logtag = filename[i].split(".j")[0].split(".p")[0];
				System.out.println(filename[i]);

				long singleFrameStartTime = (new Date()).getTime();
				System.out.println(readPlate(Highgui.imread(filename[i])));
				long singleFrameStopTime = (new Date()).getTime();
				System.out
						.println("Fram process time "
								+ ((singleFrameStopTime - singleFrameStartTime) / 1000.0)
								+ " sec.\n");
				testCount++;
			}

			System.out.println("Test " + testCount + " img. Found rect "
					+ foundCount + ". Not found " + notFoundCOunt
					+ " is Plate " + isPlateCount);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void testSystem(String[] args) {
		System.loadLibrary("opencv_java248");
		testDetectPlates(args[0]);

		// Trainer.train(args[0]+".txt", args[1]+".txt",args[2]+".bin");
		// System.out.println(args[0]+args[1]);
		// OCR.testClassifier(args[0], args[1]);
	}

	public void start() {
		if (task == null) {
			System.out.println("Can not start.Task is null.");
		} else {
			timer.scheduleAtFixedRate(task, 0, 1000 / processFrameRate);
		}

	}

	public void pause() {
		timer.cancel();
	}
}
