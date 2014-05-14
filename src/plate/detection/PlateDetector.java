package plate.detection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import utils.Utils;

public class PlateDetector extends Imgproc {
	public static String logtag = "";
	private static int strelSize = 3; // px
	private static int threshLevel = 256; // 8bit
	private static double detectRatio = 0.5; // 95% of rectangke
	private static int detectSizeMin = 10000; // px
	private static int detectSizeMax = 240000; // px

	public static Mat detectPlateUsingProjection(Mat image) {
		System.loadLibrary("opencv_java248");
		Car car = new Car(image.clone());
		List<Band> candidateBand = car.clipBands(3);
		for (int i = 0; i < candidateBand.size(); i++) {
			Mat bandMat = Utils.verticalLine(car.toMat()).submat(
					candidateBand.get(i).getBoundingRect());
			Highgui.imwrite("log/" + logtag + "/band_" + i + "_" + logtag
					+ ".jpg",
					image.submat(candidateBand.get(i).getBoundingRect()));
			Highgui.imwrite("log/" + logtag + "/plate_band_" + i + "_" + logtag
					+ ".jpg", candidateBand.get(i).clipPlates(image).get(0)
					.toMat());
			Highgui.imwrite("log/" + logtag + "/edge_band_" + i + "_" + logtag
					+ ".jpg", Utils.histoGraph(bandMat, true, false));
		}
		return image;
	}

	public static Mat detectPlateUsingProjection(String filename) {
		System.loadLibrary("opencv_java248");
		Mat image = Highgui.imread(filename);
		resize(image, image, new Size(1366, 768));
		return detectPlateUsingProjection(image);
	}

	public static Mat detectPlateByMorphological(Mat image) {
		System.loadLibrary("opencv_java248");
		Mat carImage = image.clone();
		int w = image.cols();
		int h = 400 / w * image.rows();
		Imgproc.resize(carImage, carImage, new Size(400, h));
		// preprocess plate img
		cvtColor(carImage, carImage, COLOR_RGBA2GRAY);
		GaussianBlur(carImage, carImage, new Size(3, 3), 5);
		// threshold(carImage, carImage, 0, 255, THRESH_OTSU);

		Imgproc.morphologyEx(carImage, carImage, Imgproc.MORPH_TOPHAT, Imgproc
				.getStructuringElement(Imgproc.MORPH_RECT, new Size(40, 40)));
		// carImage = Utils.verticalLine(carImage);
		Highgui.imwrite("platelocalize/TOPHAT_" + logtag + ".jpg", carImage);
		threshold(carImage, carImage, 128, 255, THRESH_OTSU);
		// adaptiveThreshold(carImage, carImage, 255,
		// Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 3, 3);
		Highgui.imwrite("platelocalize/THRESH_BINARY_" + logtag + ".jpg",
				carImage);
		// threshold(carImage, carImage, 128, 255, THRESH_OTSU);
		// morphological img processing
		// for (int i = strelSize-1; i < strelSize; i++) {
		Mat structureElementKernel = Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(3, 3));
		Imgproc.morphologyEx(carImage, carImage, Imgproc.MORPH_CLOSE,
				structureElementKernel);
		// Imgproc.morphologyEx(carImage, carImage, Imgproc.MORPH_OPEN,
		// structureElementKernel);
		// Imgproc.dilate(carImage, carImage, structureElementKernel);
		// Imgproc.erode(carImage, carImage, structureElementKernel);
		Highgui.imwrite("platelocalize/MORPH_" + logtag + ".jpg", carImage);
		Mat thresh = carImage.clone();

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRectPoint = new ArrayList<MatOfPoint>();
		List<Rect> boundingRect = new ArrayList<Rect>();
		Mat hierarchy = new Mat();
		findContours(carImage, contours, hierarchy, RETR_LIST,
				CHAIN_APPROX_TC89_KCOS);
		cvtColor(carImage, carImage, COLOR_GRAY2RGBA);

		// approx rectangle contours

		for (MatOfPoint matOfPoint : contours) {
			MatOfPoint2f mop2f = new MatOfPoint2f(matOfPoint.toArray());
			MatOfPoint2f approx = new MatOfPoint2f();
			Imgproc.approxPolyDP(mop2f, approx,
					Imgproc.arcLength(mop2f, true) * 0.01, true);

			Rect tempRect = boundingRect(matOfPoint);
			Rect newRect = Utils.expandRect(tempRect, 0.00,
					new Size(image.cols(), image.rows()));
			boolean rule5 = approx.rows() == 4;
			boolean rule6 = tempRect.width > tempRect.height;
			// check condition
			// 1. white element in rect per rect area should have more than
			// ratio
			boolean rule1 = (Core.sumElems(thresh.submat(newRect)).val[0] / (newRect.width
					* newRect.height * threshLevel)) >= detectRatio;
			// 2. rect size should around thresh
			boolean rule2 = (newRect.width * newRect.height) > detectSizeMin
					&& (newRect.width * newRect.height) < detectSizeMax;
			// 3. w h ratio must nearly 20/9
			boolean rule3 = ((double) newRect.width / (double) newRect.height) >= 1.7
					&& ((double) newRect.width / (double) newRect.height) <= 2.6;
			if (rule5 && rule6) { // rule3
				boundingRect.add(newRect);
			}
		}
		// Collections.sort(boundingRect, Plate.PLATE_HUERISTIC_CAMPATATOR);
		for (int i = 0; i < boundingRect.size(); i++) {
			Rect tmpRect = Utils.expandRect(boundingRect.get(i), 0.01,
					new Size(image.cols(), image.rows()));

			Point p1 = new Point(tmpRect.x, tmpRect.y);
			Point p2 = new Point(tmpRect.x + tmpRect.width, tmpRect.y);
			Point p3 = new Point(tmpRect.x + tmpRect.width, tmpRect.y
					+ tmpRect.height);
			Point p4 = new Point(tmpRect.x, tmpRect.y + tmpRect.height);
			MatOfPoint tmp = new MatOfPoint(p1, p2, p3, p4);
			boundingRect.set(i, tmpRect);
			boundingRectPoint.add(tmp);
		}
		Mat bound = image.clone();
		//drawContours(bound, boundingRectPoint, -1, new Scalar(0, 255, 0), 1);

		Highgui.imwrite("platelocalize/bound_" + logtag + ".jpg", bound);
		if (boundingRect.size() <= 0) {
			return null;
		}
		Mat plate = image.submat(boundingRect.get(0)).clone();
		resize(plate, plate, new Size(600, 270));
		return plate;
	}

	public static Mat detectPlateByMorphological(String filename) {
		System.loadLibrary("opencv_java248");
		Mat image = Highgui.imread(filename);
		resize(image, image, new Size(1366, 768));
		return detectPlateByMorphological(image);
	}

	public static void testDetectPlate() {
		System.loadLibrary("opencv_java248");
		Mat result;
		FileReader fileReader;
		BufferedReader bufferedReader;
		List<String> lines;
		String line;
		try {
			fileReader = new FileReader("sourcedata/CAR/car_name.txt");
			bufferedReader = new BufferedReader(fileReader);
			lines = new ArrayList<String>();
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
			String[] filename = lines.toArray(new String[lines.size()]);
			for (int i = 0; i < filename.length; i++) {
				// remove old file
				File folder = new File("platelocalize/");
				folder.mkdir();
				File[] listOfFiles = folder.listFiles();
				for (int n = 0; n < listOfFiles.length; n++) {
					if (listOfFiles[n].isDirectory()) {
						// System.out.println("segment/" + i + "/"
						// + listOfFiles[n].getName());
						File file = new File("platelocalize/"
								+ listOfFiles[n].getName());
						file.delete();
					}
				}
				folder = new File("log/");
				folder.mkdir();
				listOfFiles = folder.listFiles();
				for (int n = 0; n < listOfFiles.length; n++) {
					if (listOfFiles[n].isDirectory()) {
						(new File("log/" + listOfFiles[n].getName())).delete();
					}
				}
				logtag = filename[i].split(".j")[0].split(".p")[0];
				File file = new File("log/" + logtag);
				file.mkdir();
				result = PlateDetector
						.detectPlateByMorphological("sourcedata/CAR/"
								+ filename[i]);

				if (result == null) {
					System.out.println("image " + filename[i] + " not found");
					continue;
				}
				Highgui.imwrite("platelocalize/" + filename[i], result);

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void testProjection() {
		System.loadLibrary("opencv_java248");
		Mat result;
		FileReader fileReader;
		BufferedReader bufferedReader;
		List<String> lines;
		String line;
		try {
			fileReader = new FileReader("sourcedata/CAR/car_name.txt");
			bufferedReader = new BufferedReader(fileReader);
			lines = new ArrayList<String>();
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
			String[] filename = lines.toArray(new String[lines.size()]);
			for (int i = 0; i < filename.length; i++) {
				// remove old file
				File folder = new File("platelocalize/");
				folder.mkdir();
				File[] listOfFiles = folder.listFiles();
				for (int n = 0; n < listOfFiles.length; n++) {
					if (listOfFiles[n].isDirectory()) {
						// System.out.println("segment/" + i + "/"
						// + listOfFiles[n].getName());
						File file = new File("platelocalize/"
								+ listOfFiles[n].getName());
						file.delete();
					}
				}
				folder = new File("log/");
				folder.mkdir();
				listOfFiles = folder.listFiles();
				for (int n = 0; n < listOfFiles.length; n++) {
					if (listOfFiles[n].isDirectory()) {
						(new File("log/" + listOfFiles[n].getName())).delete();
					}
				}
				logtag = filename[i].split(".j")[0].split(".p")[0];
				File file = new File("log/" + logtag);
				file.mkdir();
				System.out.println(filename[i]);
				result = PlateDetector
						.detectPlateUsingProjection("sourcedata/CAR/"
								+ filename[i]);

				if (result == null) {
					System.out.println("image " + filename[i] + " not found");
					continue;
				}
				Highgui.imwrite("platelocalize/" + filename[i], result);

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void testMorphClipping() {
		System.loadLibrary("opencv_java248");
		Mat image = Highgui.imread("sourcedata/LP/band_0_CAR2.jpg");
		Mat grayImage = image.clone();
		// preprocess plate img
		cvtColor(grayImage, grayImage, COLOR_RGBA2GRAY);
		GaussianBlur(grayImage, grayImage, new Size(3, 3), 5);
		threshold(grayImage, grayImage, 0, 255, THRESH_OTSU);
		Highgui.imwrite("platelocalize/" + "preprocess_test_platelocalize.jpg",
				grayImage);

		// morphological img processing
		// for (int i = strelSize-1; i < strelSize; i++) {
		int strelSize = grayImage.rows() / 4;
		Mat structureElementKernel = Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(strelSize, strelSize));
		Imgproc.dilate(grayImage, grayImage, structureElementKernel);
		Imgproc.erode(grayImage, grayImage, structureElementKernel);
		Highgui.imwrite("platelocalize/" + "morpho_test_platelocalize.jpg",
				grayImage);

		// }
		// threshold(carImage, carImage, 0, 255, THRESH_OTSU);
		Imgproc.erode(grayImage, grayImage, Imgproc.getStructuringElement(
				Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
		threshold(grayImage, grayImage, 192, 255, THRESH_BINARY);

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRectPoint = new ArrayList<MatOfPoint>();
		List<Rect> boundingRect = new ArrayList<Rect>();
		Mat hierarchy = new Mat();
		findContours(grayImage, contours, hierarchy, RETR_LIST,
				CHAIN_APPROX_NONE);
		cvtColor(grayImage, grayImage, COLOR_GRAY2RGBA);
		System.out.println(contours.size());

		// approx rectangle contours
		for (MatOfPoint matOfPoint : contours) {
			MatOfPoint2f mop2f = new MatOfPoint2f(matOfPoint.toArray());
			MatOfPoint2f approx = new MatOfPoint2f();
			approxPolyDP(mop2f, approx, arcLength(mop2f, true) * 0.02, true);
			System.out.println("point con size " + approx.rows());
			Rect tempRect = boundingRect(matOfPoint);
			Rect newRect = Utils.expandRect(tempRect, 0.00,
					new Size(image.cols(), image.rows()));
			boolean rule4 = tempRect.height >= image.rows() / 2;
			boolean rule5 = approx.rows() == 4;
			boolean rule6 = tempRect.width > tempRect.height;
			if (rule4 && rule5 && rule6) { // rule3
				boundingRect.add(newRect);
			}
		}

		// Collections.sort(boundingRect, Plate.PLATE_HUERISTIC_CAMPATATOR);
		for (int i = 0; i < boundingRect.size(); i++) {
			Rect tmpRect = Utils.expandRect(boundingRect.get(i), 0.01,
					new Size(image.cols(), image.rows()));
			Point p1 = new Point(tmpRect.x, tmpRect.y);
			Point p2 = new Point(tmpRect.x + tmpRect.width, tmpRect.y);
			Point p3 = new Point(tmpRect.x + tmpRect.width, tmpRect.y
					+ tmpRect.height);
			Point p4 = new Point(tmpRect.x, tmpRect.y + tmpRect.height);
			MatOfPoint tmp = new MatOfPoint(p1, p2, p3, p4);
			boundingRect.set(i, tmpRect);
			boundingRectPoint.add(tmp);
		}
		for (Rect rect : boundingRect) {
			System.out.println(contours.size() + " x y w h " + rect.x + " "
					+ rect.y + " " + rect.width + " " + rect.height + " ");
		}
		drawContours(image, boundingRectPoint, -1, new Scalar(127, 127, 0), 3);
		Highgui.imwrite("platelocalize/" + "test_platelocalize.jpg", image);
	}

	public static void detectPlate() {
		System.loadLibrary("opencv_java248");
		Mat result;
		FileReader fileReader;
		BufferedReader bufferedReader;
		List<String> lines;
		String line;
		try {
			fileReader = new FileReader("sourcedata/CAR/car_name.txt");
			bufferedReader = new BufferedReader(fileReader);
			lines = new ArrayList<String>();
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
			String[] filename = lines.toArray(new String[lines.size()]);
			for (int i = 0; i < filename.length; i++) {
				// remove old file
				File folder = new File("platelocalize/");
				folder.mkdir();
				File[] listOfFiles = folder.listFiles();
				for (int n = 0; n < listOfFiles.length; n++) {
					if (listOfFiles[n].isDirectory()) {
						// System.out.println("segment/" + i + "/"
						// + listOfFiles[n].getName());
						File file = new File("platelocalize/"
								+ listOfFiles[n].getName());
						file.delete();
					}
				}
				folder = new File("log/");
				folder.mkdir();
				listOfFiles = folder.listFiles();
				for (int n = 0; n < listOfFiles.length; n++) {
					if (listOfFiles[n].isDirectory()) {
						(new File("log/" + listOfFiles[n].getName())).delete();
					}
				}
				logtag = filename[i].split(".j")[0].split(".p")[0];
				File file = new File("log/" + logtag);
				file.mkdir();
				System.out.println(filename[i]);
				Mat grayImage = Highgui.imread("sourcedata/CAR/" + filename[i]);
				int w = grayImage.cols();
				int h = (int) (400.0 / w * grayImage.rows());
				Imgproc.resize(grayImage, grayImage, new Size(400, h));
				Imgproc.cvtColor(grayImage, grayImage, Imgproc.COLOR_RGB2GRAY);

				Car car = new Car(Highgui.imread("sourcedata/CAR/"
						+ filename[i]));
				List<Band> bands = car.clipBands(7);
				List<Plate> plates = new ArrayList<Plate>();
				for (Band band : bands) {
					plates.addAll(band.clipPlates(car.toMat()));
				}
				System.out.println("Foune candidate plate "+plates.size());
				if (plates.size() <= 0) {
					System.out.println("image " + filename[i] + " not found");
					continue;
				}
				int p = 1;
				for (Plate plate : plates) {
					Highgui.imwrite("platelocalize/" + logtag + "_PLATE_" + (p++)
							+ ".jpg", plate.toMat());
				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
