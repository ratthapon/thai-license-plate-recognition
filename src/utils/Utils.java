package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Utils {
	public static int scale = 1;
	public static int delta = 0;

	public static Mat verticalLine(Mat image) {
		System.loadLibrary("opencv_java248");
		Mat carImage = image.clone();
		// preprocess plate img
		Imgproc.cvtColor(carImage, carImage, Imgproc.COLOR_RGBA2GRAY);
		Imgproc.GaussianBlur(carImage, carImage, new Size(3, 3), 5);
		// threshold(carImage, carImage, 0, 255, THRESH_OTSU);
		Core.subtract(carImage, Core.mean(carImage), carImage);
		Core.subtract(carImage, Core.mean(carImage), carImage);

		Mat varticalLine = new Mat();
		Imgproc.Sobel(carImage, varticalLine, carImage.depth(), 1, 0, 3, scale,
				delta, Imgproc.BORDER_DEFAULT);
		Core.subtract(varticalLine, Core.mean(varticalLine), varticalLine);
		return varticalLine;
	}

	public static Vector<Byte> projectMatY(Mat image) {
		Vector<Byte> result = new Vector<Byte>(image.rows());
		for (int i = 0; i < image.rows(); i++) {
			result.add(i,
					(byte) (Core.sumElems(image.row(i)).val[0] / image.cols()));
		}
		return result;
	}

	public static Vector<Byte> projectMatX(Mat image) {
		Vector<Byte> result = new Vector<Byte>(image.cols());
		for (int i = 0; i < image.cols(); i++) {
			result.add(i,
					(byte) (Core.sumElems(image.col(i)).val[0] / image.rows()));
		}
		return result;
	}

	public static Vector<Byte> derivativeProjectMatX(Mat verticalLine, int H) {
		Mat XsubH = new Mat();
		// System.out.println("vertical line "+verticalLine.rows()+" "+verticalLine.cols());
		Core.subtract(
				verticalLine,
				new Mat(verticalLine.rows(), verticalLine.cols(), verticalLine
						.type(), new Scalar(H)), XsubH);
		Vector<Byte> pDifXperH = new Vector<Byte>(verticalLine.cols());
		Vector<Byte> px = Utils.projectMatX(verticalLine);
		Vector<Byte> pXsubH = projectMatX(XsubH);
		for (int i = 0; i < px.size(); i++) {
			pDifXperH.add(i, (byte) ((px.get(i) - pXsubH.get(i)) / H));
		}
		return pDifXperH;
	}

	public static Rect expandRect(Rect rect, double expandRatio, Size sizeBound) {
		int calibrateX = 0;
		int calibrateY = 0;
		double padding = expandRatio;
		double x1, y1, x2, y2, x3, y3, x4, y4;
		x1 = (rect.x - calibrateX) * (1.0 - padding);
		y1 = (rect.y - calibrateY) * (1.0 - padding);
		x2 = (rect.x + rect.width - calibrateX) * (1.0 + padding);
		y2 = (rect.y - calibrateY) * (1.0 - padding);
		x3 = (rect.x + rect.width - calibrateX) * (1.0 + padding);
		y3 = (rect.y + rect.height - calibrateY) * (1.0 + padding);
		x4 = (rect.x - calibrateX) * (1.0 - padding);
		y4 = (rect.y + rect.height - calibrateY) * (1.0 + padding);
		int width = (int) sizeBound.width - 1;
		int height = (int) sizeBound.height - 1;
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
		Point p1, p2, p3, p4;
		p1 = new Point(x1, y1);
		p2 = new Point(x2, y2);
		p3 = new Point(x3, y3);
		p4 = new Point(x4, y4);
		MatOfPoint mop = new MatOfPoint();
		mop.fromArray(p1, p2, p3, p4);
		rect = Imgproc.boundingRect(mop);
		return rect;
	}

	public static Mat histoGraph(String filename, boolean drawVertical,
			boolean drawHorizontal) {
		System.loadLibrary("opencv_java248");
		Mat image = Highgui.imread(filename);
		Imgproc.resize(image, image, new Size(1024, 768));
		return histoGraph(image, drawVertical, drawHorizontal);
	}

	public static Mat histoGraph(Mat image, boolean drawVertical,
			boolean drawHorizontal) {
		System.loadLibrary("opencv_java248");
		Vector<Byte> pyMagnitude = Utils.projectMatY(image);
		Vector<Byte> pxMagnitude = Utils.projectMatX(image);

		Point[] pyPoint = new Point[pyMagnitude.size()];
		Point[] pxPoint = new Point[pxMagnitude.size()];
		for (int i = 0; i < pyMagnitude.size(); i++) {
			pyPoint[i] = new Point(pyMagnitude.get(i), i);
		}

		for (int i = 0; i < pxMagnitude.size(); i++) {
			pxPoint[i] = new Point(i, pxMagnitude.get(i));
		}
		Mat graph = new Mat(image.rows(), image.cols(), image.type(),
				new Scalar(255, 255, 255));
		List<MatOfPoint> verticalGraph = new ArrayList<MatOfPoint>();
		List<MatOfPoint> horizontalGraph = new ArrayList<MatOfPoint>();
		MatOfPoint mopVG = new MatOfPoint(pyPoint);
		verticalGraph.add(mopVG);
		MatOfPoint mopHG = new MatOfPoint(pxPoint);
		horizontalGraph.add(mopHG);
		if (drawHorizontal) {
			Imgproc.drawContours(graph, verticalGraph, -1, new Scalar(0, 0, 0),
					1);
		}
		if (drawVertical) {
			Imgproc.drawContours(graph, horizontalGraph, -1, new Scalar(127,
					127, 127), 1);
		}
		return graph;
	}
}
