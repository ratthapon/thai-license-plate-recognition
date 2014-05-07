package plate.detection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import utils.Utils;

public class Band {
	private Mat bandMat = null;
	private Rect boundingRect = null;
	public int top;
	public int bottom;
	public int width;
	public int height;
	public double density;
	public int maxAmplitude;

	Band(Mat image, int top, int bottom, int ybm) {
		this.bandMat = new Mat(image, new Range(top, bottom), new Range(0,
				image.cols() - 1));
		this.top = top;
		this.bottom = bottom;
		this.width = bandMat.cols();
		this.height = bandMat.rows();
		this.boundingRect = new Rect(0, top, image.cols() - 1, bottom - top);
		this.density = Core.sumElems(image.submat(boundingRect)).val[0]
				/ image.size().area();
		this.maxAmplitude = ybm;
		// System.out.println("Created new band.");
	}

	public static Comparator<Band> HUERISTIC_COMPARATOR = new Comparator<Band>() {

		@Override
		public int compare(Band o1, Band o2) {
			int precision = 1000000;
			double o1_alpha1 = 0.4 * o1.height;
			double o2_alpha1 = 0.4 * o2.height;

			double o1_alpha2 = 0.0 * o1.maxAmplitude;
			double o2_alpha2 = 0.0 * o2.maxAmplitude;

			double o1_alpha3 = 1 * o1.density;
			double o2_alpha3 = 1 * o2.density;

			// double o1_alpha4 = 0.4 * (o1.width / o1.height - 5);
			// double o2_alpha4 = 0.4 * (o2.width / o2.height - 5);

			int o1_value = (int) (o1_alpha1 + o1_alpha2 + o1_alpha3)
					* precision;
			int o2_value = (int) (o2_alpha1 + o2_alpha2 + o2_alpha3)
					* precision;
			return o1_value - o2_value;
		}
	};

	public List<Plate> clipPlates(Mat carImage, int maxPlate) {
		List<Plate> clipPlates = this.clipPlates(carImage);
		Collections.sort(clipPlates, Plate.PLATE_HUERISTIC_CAMPATATOR);
		if (clipPlates.size() <= 0) {
			return clipPlates;
		}
		return clipPlates.subList(0, maxPlate);
	}

	// unfinish
	private List<Plate> clipPlate2(Mat carImage) {
		List<Plate> plate;
		// System.out.println("Project band image in X axis");
		Vector<Byte> pxMagnitude = Utils.projectMatX(Utils
				.verticalLine(bandMat));
		byte xpm = Collections.max(pxMagnitude);
		int xpmIndex = pxMagnitude.indexOf(xpm);
		double c1 = (Collections.max(pxMagnitude) + Collections
				.min(pxMagnitude)) * 0.86;
		double c2 = (Collections.max(pxMagnitude) + Collections
				.min(pxMagnitude)) * 0.86;
		// yb0 = max(y0<=y<=ybm){y|py(y)<=c*py(ybm)}
		Vector<Byte> xp0InspectSet = new Vector<Byte>(pxMagnitude.subList(0,
				xpmIndex));
		int xp0Index = 0;
		for (int i = 0; i < xp0InspectSet.size(); i++) {
			Byte byte1 = xp0InspectSet.get(i);
			if (byte1 <= c1) {
				xp0Index = i;
			}
		}

		// yb1 = min(ybm<=y<=y1){y|py(y)<=c*py(ybm)}
		Vector<Byte> xp1InspectSet = new Vector<Byte>(pxMagnitude.subList(
				xpmIndex, pxMagnitude.size() - 1));
		int xp1Index = pxMagnitude.size() - 1;
		for (int i = 0; i < xp1InspectSet.size(); i++) {
			Byte byte1 = xp1InspectSet.get(i);
			if (byte1 <= c2) {
				xp1Index = i + xpmIndex;
				break;
			}
		}

		System.out.println("Calibrate band coordinate");

		int calibrate = (int) ((xp1Index - xp0Index) * 0.1);
		xp0Index -= calibrate;
		xp1Index += calibrate;
		if (xp0Index < 0) {
			xp0Index = 0;
		}
		if (xp1Index > bandMat.cols() - 1) {
			xp1Index = bandMat.cols() - 1;
		}
		return null;
	}

	public List<Plate> clipPlates(Mat carImage) {
		Mat grayImage = this.bandMat.clone();
		System.out.println("Plate clipping.");

		// preprocess plate img
		Imgproc.cvtColor(grayImage, grayImage, Imgproc.COLOR_RGBA2GRAY);
		Imgproc.GaussianBlur(grayImage, grayImage, new Size(3, 3), 5);

		// morphological img processing
		int strelSize = (int) (height * 0.25) + 1;
		Imgproc.morphologyEx(grayImage, grayImage, Imgproc.MORPH_TOPHAT,
				Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(
						height, height)));
		Imgproc.threshold(grayImage, grayImage, 96, 255, Imgproc.THRESH_BINARY);
		Mat structureElementKernel = Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(strelSize, strelSize));
		Imgproc.dilate(grayImage, grayImage, structureElementKernel);
		Mat morpho = grayImage.clone();

		//Highgui.imwrite("platelocalize/" + PlateDetector.logtag
		//		+ "_band_morpho.jpg", grayImage);

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRectPoint = new ArrayList<MatOfPoint>();
		List<Rect> boundingRect = new ArrayList<Rect>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(grayImage, contours, hierarchy, Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_NONE);
		Imgproc.cvtColor(grayImage, grayImage, Imgproc.COLOR_GRAY2RGBA);

		// approx rectangle contours
		for (MatOfPoint matOfPoint : contours) {
			MatOfPoint2f mop2f = new MatOfPoint2f(matOfPoint.toArray());
			MatOfPoint2f approx = new MatOfPoint2f();
			Imgproc.approxPolyDP(mop2f, approx,
					Imgproc.arcLength(mop2f, true) * 0.02, true);
			Rect tempRect = Imgproc.boundingRect(matOfPoint);
			Rect newRect = Utils.expandRect(tempRect, 0.00,
					new Size(grayImage.cols(), grayImage.rows()));
			boolean rule4 = tempRect.height >= grayImage.rows() / 2;
			boolean rule5 = approx.rows() == 4;
			boolean rule6 = tempRect.width > tempRect.height;
			boolean rule7 = Math.abs((tempRect.width / tempRect.height) - 5) <= 3;
			if (rule4 && rule5 && rule6 && rule7) { // rule3
				boundingRect.add(newRect);
			}
		}

		// sort most feasible location
		// Collections.sort(boundingRect,
		// List<Plate>.PLATE_HUERISTIC_CAMPATATOR);

		// mapping rect to car image coordinate
		for (int i = 0; i < boundingRect.size(); i++) {
			Rect tmpRect = Utils.expandRect(boundingRect.get(i), 0.01,
					new Size(grayImage.cols(), grayImage.rows()));
			Point p1 = new Point(tmpRect.x, top + tmpRect.y);
			Point p2 = new Point(tmpRect.x + tmpRect.width, top + tmpRect.y);
			Point p3 = new Point(tmpRect.x + tmpRect.width, top + tmpRect.y
					+ tmpRect.height);
			Point p4 = new Point(tmpRect.x, top + tmpRect.y + tmpRect.height);
			MatOfPoint tmp = new MatOfPoint(p1, p2, p3, p4);
			tmpRect = new Rect(p1, p3);
			boundingRect.set(i, tmpRect);
			boundingRectPoint.add(tmp);
		}
		//Imgproc.drawContours(carImage, boundingRectPoint, -1, new Scalar(0,
		//		255, 0), 3);
		List<MatOfPoint> bandRect = new ArrayList<MatOfPoint>();
		Point p1 = new Point(0, top);
		Point p2 = new Point(width - 1, top);
		Point p3 = new Point(width - 1, bottom);
		Point p4 = new Point(0, bottom);
		bandRect.add(new MatOfPoint(p1, p2, p3, p4));
		//Imgproc.drawContours(carImage, bandRect, -1, new Scalar(0, 255, 0), 3);
		//Highgui.imwrite("platelocalize/" + PlateDetector.logtag
		//		+ "_plate_local.jpg", carImage);
		List<Plate> result = new ArrayList<Plate>();
		for (Rect rect : boundingRect) {
			double density = Core.sumElems(morpho).val[0]
					/ morpho.size().area();
			result.add(new Plate(carImage, rect, density));
		}
		return result;
	}

	public Mat toMat() {
		return bandMat.clone();
	}

	public Rect getBoundingRect() {
		return boundingRect;
	}
}
