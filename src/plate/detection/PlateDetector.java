package plate.detection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class PlateDetector extends Imgproc {
	private static String logtag = "";
	private static int strelSize = 80; // px
	private static int threshLevel = 256; // 8bit
	private static double detectRatio = 0.5; // 95% of rectangke
	private static int detectSizeMin = 10000; // px
	private static int detectSizeMax = 240000; // px
	private static double plateRatio = 20.0 / 9.0; // px
	static int scale = 1;
	static int delta = 0;

	public static Mat detectPlate(String filename) {
		System.loadLibrary("opencv_java248");
		Mat image = Highgui.imread(filename);
		resize(image, image, new Size(1366, 768));
		return detectPlate(image);
	}

	public static Mat localizePlate(String filename) {
		System.loadLibrary("opencv_java248");
		Mat image = Highgui.imread(filename);
		resize(image, image, new Size(1024, 768));
		return localizePlate(image);
	}

	public static Mat localizePlate(Mat image) {
		System.loadLibrary("opencv_java248");
		Mat carImage = image.clone();
		resize(carImage, carImage, new Size(1024, 768));
		// preprocess plate img
		cvtColor(carImage, carImage, COLOR_RGBA2GRAY);
		GaussianBlur(carImage, carImage, new Size(3, 3), 0);
		// threshold(carImage, carImage, 0, 255, THRESH_OTSU);
		Highgui.imwrite("log/" + logtag + "/preprocess.jpg", carImage);
		Mat soble = new Mat();
		Sobel(carImage, soble, carImage.depth(), 1, 0, 3, scale, delta,
				BORDER_DEFAULT);
		carImage = soble.clone();
		Highgui.imwrite("log/" + logtag + "/edge.jpg", soble);
		// projection to x y hist

		// cvtColor(graph, graph, COLOR_BGRA2RGB);
		// MatOfPoint verticalMagnitude;
		// MatOfPoint horizontalMagnitude;
		List<MatOfPoint> verticalGraph = new ArrayList<MatOfPoint>();
		List<MatOfPoint> horizontalGraph = new ArrayList<MatOfPoint>();
		Point[] verticalMagnitude = new Point[carImage.rows()];
		Point[] horizontalMagnitude = new Point[carImage.cols()];
		for (int i = 0; i < carImage.rows(); i++) {
			verticalMagnitude[i] = new Point(
					Core.sumElems(carImage.row(i)).val[0] / 256, i);
		}

		for (int i = 0; i < carImage.cols(); i++) {
			horizontalMagnitude[i] = new Point(i,
					Core.sumElems(carImage.col(i)).val[0] / 256);
			// /System.out.println("Mag at "+i+" "+Core.sumElems(carImage.col(i)).val[0]);
		}
		Mat graph = new Mat(image.rows(), image.cols(), image.type(),
				new Scalar(255, 255, 255));
		MatOfPoint mopVG = new MatOfPoint(verticalMagnitude);
		verticalGraph.add(mopVG);
		MatOfPoint mopHG = new MatOfPoint(horizontalMagnitude);
		horizontalGraph.add(mopHG);
		drawContours(graph, verticalGraph, -1, new Scalar(255, 0, 0), 1);
		drawContours(graph, horizontalGraph, -1, new Scalar(0, 255, 0), 1);
		return graph;
	}

	public static Mat detectPlate(Mat image) {
		System.loadLibrary("opencv_java248");
		Mat carImage = image.clone();
		resize(carImage, carImage, new Size(1366, 768));
		// preprocess plate img
		cvtColor(carImage, carImage, COLOR_RGBA2GRAY);
		GaussianBlur(carImage, carImage, new Size(3, 3), 5);
		// threshold(carImage, carImage, 0, 255, THRESH_OTSU);
		Highgui.imwrite("log/" + logtag + "/preprocess.jpg", carImage);
		Mat sobel = new Mat();
		Sobel(carImage, sobel, carImage.depth(), 1, 0, 3, scale, delta,
				BORDER_DEFAULT);
		carImage = sobel.clone();
		Highgui.imwrite("log/" + logtag + "/edge.jpg", sobel);

		// morphological img processing
		// for (int i = strelSize-1; i < strelSize; i++) {
		Mat structureElementKernel = Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(strelSize, 30));
		Imgproc.dilate(carImage, carImage, structureElementKernel);
		Imgproc.erode(carImage, carImage, structureElementKernel);

		// }
		// threshold(carImage, carImage, 0, 255, THRESH_OTSU);
		Imgproc.erode(carImage, carImage, Imgproc.getStructuringElement(
				Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
		threshold(carImage, carImage, 192, 255, THRESH_BINARY);
		Mat thresh = carImage.clone();

		Highgui.imwrite("log/" + logtag + "/morphological.jpg", thresh);

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRectPoint = new ArrayList<MatOfPoint>();
		List<Rect> boundingRect = new ArrayList<Rect>();
		Mat hierarchy = new Mat();

		findContours(carImage, contours, hierarchy, RETR_LIST,
				CHAIN_APPROX_NONE);
		cvtColor(carImage, carImage, COLOR_GRAY2RGBA);
		drawContours(carImage, boundingRectPoint, -1, new Scalar(0, 255, 0), 1);

		for (MatOfPoint matOfPoint : contours) {
			Rect tempRect = boundingRect(matOfPoint);
			MatOfPoint tmp = new MatOfPoint();
			Rect newRect = expandRect(tempRect, tmp, 0.00,
					new Size(image.cols(), image.rows()));

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
			if (rule3) {
				boundingRectPoint.add(tmp);
				boundingRect.add(newRect);
			}
		}
		cvtColor(sobel, sobel, COLOR_GRAY2RGB);
		drawContours(image, boundingRectPoint, -1, new Scalar(0, 255, 0), 1);
		Highgui.imwrite("log/" + logtag + "/drawBound.jpg", image);
		/*
		 * drawContours(image, boundingRectPoint, -1, new Scalar(0, 255, 0), 1);
		 * cvtColor(thresh, thresh, COLOR_GRAY2RGB); drawContours(thresh,
		 * boundingRectPoint, -1, new Scalar(0, 255, 0), 3);
		 * Highgui.imwrite("plate/drawBound.jpg", image);
		 * Highgui.imwrite("plate/thresh.jpg", thresh);
		 */
		Mat plate = new Mat();
		Collections.sort(boundingRect, platRatioComparator);
		for (int i = 0; i < boundingRect.size(); i++) {
			boundingRect.set(
					i,
					expandRect(boundingRect.get(i), new MatOfPoint(), 0.01,
							new Size(image.cols(), image.rows())));
		}
		if (boundingRect.size() <= 0) {
			return null;
		}
		plate = image.submat(boundingRect.get(0)).clone();
		resize(plate, plate, new Size(600, 270));
		return plate;
	}

	private static Rect expandRect(Rect rect, MatOfPoint mop,
			double expandRatio, Size imageSize) {
		int calibrateX = 0;
		int calibrateY = 0;
		double padding = expandRatio;
		Point p1, p2, p3, p4;
		double x1, y1, x2, y2, x3, y3, x4, y4;
		x1 = (rect.x - calibrateX) * (1.0 - padding);
		y1 = (rect.y - calibrateY) * (1.0 - padding);
		x2 = (rect.x + rect.width - calibrateX) * (1.0 + padding);
		y2 = (rect.y - calibrateY) * (1.0 - padding);
		x3 = (rect.x + rect.width - calibrateX) * (1.0 + padding);
		y3 = (rect.y + rect.height - calibrateY) * (1.0 + padding);
		x4 = (rect.x - calibrateX) * (1.0 - padding);
		y4 = (rect.y + rect.height - calibrateY) * (1.0 + padding);
		int width = (int) imageSize.width - 1;
		int height = (int) imageSize.height - 1;
		if (x1 < 0) {
			x1 = 0;
		}
		if (x2 > width) {
			x2 = width;
		}
		if (x3 > width) {
			x3 = width;
		}
		if (x4 < 0) {
			x4 = 0;
		}

		if (y1 < 0) {
			y1 = 0;
		}
		if (y2 < 0) {
			y2 = 0;
		}
		if (y3 > height) {
			y3 = height;
		}
		if (y4 > height) {
			y4 = height;
		}

		p1 = new Point(x1, y1);
		p2 = new Point(x2, y2);
		p3 = new Point(x3, y3);
		p4 = new Point(x4, y4);
		mop = new MatOfPoint(p1, p2, p3, p4);
		rect = boundingRect(mop);
		return rect;
	}

	// for sort character in plate
	private static Comparator<Rect> platRatioComparator = new Comparator<Rect>() {
		public int compare(Rect c1, Rect c2) {
			int precision = 100000;
			double ratioRect1 = Math
					.abs(((double) c1.width / (double) c1.height) - plateRatio);
			double ratioRect2 = Math
					.abs(((double) c2.width / (double) c2.height) - plateRatio);
			int diff = (int) (ratioRect1 * precision - ratioRect2 * precision);
			return diff;
		}
	};

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		Mat result;
		String[] filename = { "CAR1.jpg", "CAR4.jpg", "CAR6.jpg", "CAR7.jpg",
				"CAR8.jpg" };
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
			result = PlateDetector.detectPlate("sourcedata/" + filename[i]);
			if (result == null) {
				System.out.println("image " + filename[i] + " not found");
				continue;
			}
			Highgui.imwrite("platelocalize/" + filename[i], result);
		}
	}

}
