package text.segmentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

class TextSegment extends Imgproc {
	private static int structureElementSize = 5;
	private static float calibrate = structureElementSize / 2;
	private static int charSizeThresh = 90; // px 2/3 of plat hieght

	public static ArrayList<Mat> segmentText(Mat image) {
		Mat plateImg = image.clone();
		ArrayList<Mat> charList = new ArrayList<Mat>();
		resize(plateImg, plateImg, new Size(600, 270));
		cvtColor(plateImg, plateImg, COLOR_RGBA2GRAY);
		threshold(plateImg, plateImg, 192, 255, THRESH_BINARY);

		// apply some dilation and erosion to join the gaps
		Mat structureElement = getStructuringElement(MORPH_RECT, new Size(
				structureElementSize, structureElementSize));
		dilate(plateImg, plateImg, structureElement);
		erode(plateImg, plateImg, structureElement);

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRectPoint = new ArrayList<MatOfPoint>();
		List<Rect> boundingRect = new ArrayList<Rect>();
		Mat hierarchy = new Mat();
		findContours(plateImg, contours, hierarchy, RETR_LIST,
				CHAIN_APPROX_NONE);
		cvtColor(plateImg, plateImg, COLOR_GRAY2RGBA);
		// System.out.println("size of contours " + contours.size());

		for (MatOfPoint matOfPoint : contours) {
			Rect tempRect = boundingRect(matOfPoint);
			MatOfPoint tmp = new MatOfPoint(new Point(tempRect.x - calibrate,
					tempRect.y - calibrate), new Point(tempRect.x
					+ tempRect.width - calibrate, tempRect.y - calibrate),
					new Point(tempRect.x + tempRect.width - calibrate,
							tempRect.y + tempRect.height - calibrate),
					new Point(tempRect.x - calibrate, tempRect.y
							+ tempRect.height - calibrate));
			if (tempRect.height > charSizeThresh
					&& tempRect.height < charSizeThresh * 2) {
				boundingRectPoint.add(tmp);
				boundingRect.add(tempRect);
			}
		}
		Collections.sort(boundingRect, comparator);
		for (Rect rect : boundingRect) {
			Mat cropImg = (new Mat(image, rect)).clone();
			/*
			 //fill contour
			cvtColor(cropImg, cropImg, COLOR_RGBA2GRAY);
			List<MatOfPoint> charContours = new ArrayList<MatOfPoint>();
			Mat charContoursHierarchy = new Mat();
			findContours(cropImg, charContours, charContoursHierarchy, RETR_LIST,
					CHAIN_APPROX_NONE);
			drawContours(cropImg, charContours, -1, new Scalar(0, 0, 0), -1);
			*/
			resize(cropImg, cropImg, new Size(32, 32));
			charList.add(cropImg);
		}
		/*
		 * drawContours(image, boundingRectPoint, -1, new Scalar(0, 255, 0), 1);
		 * drawContours(image, contours, -1, new Scalar(0, 0, 255), 1);
		 * Highgui.imwrite("drawBound.jpg", image);
		 */
		return charList;
	}

	// for sort character in plate
	static Comparator<Rect> comparator = new Comparator<Rect>() {
		public int compare(Rect c1, Rect c2) {
			return c1.x - c2.x;
		}
	};

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		Mat img;
		ArrayList<Mat> charList;
		img = Highgui.imread("detectplate.jpg");
		resize(img, img, new Size(600, 270));
		charList = segmentText(img);
		int i = 1;
		for (Mat mat : charList) {
			Highgui.imwrite("segment/char_at_" + (i++)+".jpg", mat);
		}
	}
}
