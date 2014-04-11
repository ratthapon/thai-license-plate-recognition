package plate.detection;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class PlateDetect extends Imgproc {
	private static int structureElementSize = 40; // px
	private static int threshLevel = 256; // 8bit
	private static double detectRatio = 0.95; // 95% of rectangke
	private static int detectSize = 10000; // px

	public static Mat detectPlate(String filename) {
		System.loadLibrary("opencv_java248");
		Mat image = Highgui.imread(filename);
		resize(image, image, new Size(1024, 768));
		return detectPlate(image);
	}

	public static Mat detectPlate(Mat image) {
		System.loadLibrary("opencv_java248");
		Mat carImage = image.clone();
		resize(carImage, carImage, new Size(1024, 768));
		// preprocess plate img
		cvtColor(carImage, carImage, COLOR_RGBA2GRAY);
		threshold(carImage, carImage, 192, 255, THRESH_BINARY);
		// morphological img processing

		Mat structureElementKernel = Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(structureElementSize,
						structureElementSize));
		Imgproc.dilate(carImage, carImage, structureElementKernel);
		Imgproc.erode(carImage, carImage, structureElementKernel);
		threshold(carImage, carImage, 230, 255, THRESH_BINARY);
		Mat thresh = carImage.clone();

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRectPoint = new ArrayList<MatOfPoint>();
		List<Rect> boundingRect = new ArrayList<Rect>();
		Mat hierarchy = new Mat();

		findContours(carImage, contours, hierarchy, RETR_LIST,
				CHAIN_APPROX_SIMPLE);
		cvtColor(carImage, carImage, COLOR_GRAY2RGBA);
		// System.out.println("size of contour " + contours.size());

		for (MatOfPoint matOfPoint : contours) {
			Rect tempRect = boundingRect(matOfPoint);
			MatOfPoint tmp = new MatOfPoint(new Point(tempRect.x, tempRect.y),
					new Point(tempRect.x + tempRect.width, tempRect.y),
					new Point(tempRect.x + tempRect.width, tempRect.y
							+ tempRect.height), new Point(tempRect.x,
							tempRect.y + tempRect.height));
			// found plate
			if ((Core.sumElems(thresh.submat(tempRect)).val[0] / (tempRect.width
					* tempRect.height * threshLevel)) >= detectRatio
					&& ((tempRect.width * tempRect.height) > detectSize)) {
				boundingRectPoint.add(tmp);
				boundingRect.add(tempRect);
			}

		}
		/*
		 * drawContours(image, boundingRectPoint, -1, new Scalar(0, 255, 0), 1);
		 * cvtColor(thresh, thresh, COLOR_GRAY2RGB); drawContours(thresh,
		 * boundingRectPoint, -1, new Scalar(0, 255, 0), 3);
		 * Highgui.imwrite("plate/drawBound.jpg", image);
		 * Highgui.imwrite("plate/thresh.jpg", thresh);
		 */
		Mat plate = new Mat();
		plate = image.submat(boundingRect.get(0)).clone();
		resize(plate, plate, new Size(600, 270));
		return plate;
	}

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		Mat expected;
		Mat actual;

		expected = Highgui.imread("expected.jpg");
		actual = PlateDetect.detectPlate("sourcedata/CAR1.jpg");
		Mat dif = new Mat();
		//Highgui.imwrite("expected.jpg",actual);
		//Highgui.imwrite("actual.jpg",actual);
		Core.absdiff(expected, actual, dif);
		System.out.println(dif.dump());
		// fail("Not yet implemented");
	}

}
