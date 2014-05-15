package system;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ocr.text.recognition.OCR;
import ocr.text.segmentation.TextSegment;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import plate.detection.Car;
import plate.detection.Plate;

public class ProcessingCore {
	public static String logtag = "";
	private static int testCount = 0;
	private static int foundCount = 0;
	private static int notFoundCOunt = 0;
	private static int isPlateCount = 0;

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
				// // detect all plate
				// List<Plate> plates = new ArrayList<Plate>();
				// plates = car.clipPlatesMaxBandLimit(7);
				// testCount++;
				//
				// if (plates.size() <= 0) {
				// notFoundCOunt++;
				// System.out.println("image " + filename[i] + " not found");
				// continue;
				// } else {
				// foundCount++;
				// System.out
				// .println("Foune candidate plate " + plates.size());
				// }
				// int p = 1;
				//
				// // show bounding rect
				// Mat showBounding = car.toMat().clone();
				// List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
				//
				// for (Plate plate : plates) {
				// Point p1 = new Point(plate.getBoundingRect().x,
				// plate.getBoundingRect().y);
				// Point p2 = new Point(plate.getBoundingRect().x
				// + plate.getBoundingRect().width,
				// plate.getBoundingRect().y);
				// Point p3 = new Point(plate.getBoundingRect().x
				// + plate.getBoundingRect().width,
				// plate.getBoundingRect().y
				// + plate.getBoundingRect().height);
				// Point p4 = new Point(plate.getBoundingRect().x,
				// plate.getBoundingRect().y
				// + plate.getBoundingRect().height);
				// contours.add(new MatOfPoint(p1, p2, p3, p4));
				// // Highgui.imwrite("platelocalize/" + logtag + "_PLATE_"
				// // + (p++) + ".jpg", plate.toMat());
				// List<Mat> charImageList = TextSegment
				// .getListMatOfCharImage(plate.toMat());
				// if (charImageList.size() >= 1) {
				// isPlateCount++;
				// }
				// int c = 0;
				// for (Mat mat : charImageList) {
				// // Highgui.imwrite("platelocalize/" + logtag + "_CHAR/"
				// // + logtag + "_CHAR_" + (c++) + ".jpg", mat);
				// }
				// }
				// Imgproc.drawContours(showBounding, contours, -1, new
				// Scalar(0,
				// 255, 0), 3);
				// // Highgui.imwrite("platelocalize/" + logtag + "_LOCATE_" +
				// // (p++)
				// // + ".jpg", showBounding);
				// // end of bounding plates
				//
				// // image for presentation session 2
				// List<Band> bands = car.clipBands(7);
				// List<MatOfPoint> bandBound = new ArrayList<MatOfPoint>();
				// Band band = bands.get(bands.size() - 1);
				// Point b1 = new Point(band.getBoundingRect().x,
				// band.getBoundingRect().y);
				// Point b2 = new Point(band.getBoundingRect().x
				// + band.getBoundingRect().width,
				// band.getBoundingRect().y);
				// Point b3 = new Point(band.getBoundingRect().x
				// + band.getBoundingRect().width,
				// band.getBoundingRect().y
				// + band.getBoundingRect().height);
				// Point b4 = new Point(band.getBoundingRect().x,
				// band.getBoundingRect().y
				// + band.getBoundingRect().height);
				// bandBound.add(new MatOfPoint(b1, b2, b3, b4));
				// showBounding = car.toMat().clone();
				// Imgproc.drawContours(showBounding, bandBound, -1, new
				// Scalar(0,
				// 255, 0), 3);
				// // Highgui.imwrite("platelocalize/" + logtag + "_BAND.jpg",
				// // showBounding);
				// // Highgui.imwrite("platelocalize/" + logtag +
				// // "_VERTICALLINE.jpg", Utils.verticalLine(car.toMat()));
				//
				// Mat histogram = new Mat();
				// histogram = Utils.histoGraph(Utils.verticalLine(car.toMat()),
				// false, true);
				// // Highgui.imwrite("platelocalize/" + logtag +
				// "_HISTLINE.jpg",
				// // histogram);
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

	private static List<String> readPlate(Mat carMat) {
		// load car image
		System.loadLibrary("opencv_java248");
		Car car = new Car(carMat);
		// detect all plate
		long detectPlateStartTime = (new Date()).getTime();
		List<Plate> plates = new ArrayList<Plate>();
		plates = car.clipPlatesMaxBandLimit(7);
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

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		testDetectPlates(args[0]);

		// Trainer.train(args[0]+".txt", args[1]+".txt",args[2]+".bin");
		// System.out.println(args[0]+args[1]);
		// OCR.testClassifier(args[0], args[1]);
	}
}
