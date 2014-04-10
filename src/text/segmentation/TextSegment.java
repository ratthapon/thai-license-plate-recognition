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
	private static final int structureElementSize = 5;

	public static Mat preprocessPlate(Mat image) {
		Mat plateImg = image.clone();
		System.out.println("Segmenting Text");
		cvtColor(plateImg, plateImg, COLOR_RGB2GRAY);
		Highgui.imwrite("cvt.jpg", plateImg);
		// adaptiveThreshold(plateImg, plateImg, 255,
		// ADAPTIVE_THRESH_GAUSSIAN_C, 1, 11, 5);
		// cvtColor(plateImg, plateImg, COLOR_BGR2GRAY);
		// Highgui.imwrite("adaptived.jpg", plateImg);
		threshold(plateImg, plateImg, 192, 255, THRESH_BINARY);
		Highgui.imwrite("thresh.jpg", plateImg);

		// apply some dilation and erosion to join the gaps
		Mat structureElement = getStructuringElement(MORPH_RECT, new Size(
				structureElementSize, structureElementSize));
		dilate(plateImg, plateImg, structureElement);
		erode(plateImg, plateImg, structureElement);
		Highgui.imwrite("open and close.jpg", plateImg);

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRectPoint = new ArrayList<MatOfPoint>();
		List<Rect> boundingRect = new ArrayList<Rect>();
		Mat hierarchy = new Mat();

		findContours(plateImg, contours, hierarchy, RETR_LIST,
				CHAIN_APPROX_NONE);
		cvtColor(plateImg, plateImg, COLOR_GRAY2RGBA);
		System.out.println("size of " + contours.size());
		// drawContours(plateImg, contours, -1, new Scalar(255, 255,
		// 255));
		float calibrate = structureElementSize / 2;
		int charSizeThresh = 90; // px 2/3 of plat hieght
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
		int i = 0;
		for (Rect rect : boundingRect) {
			Mat cropImg = (new Mat(image, rect)).clone();
			System.out.println("sorted x = " + rect.x);
			Highgui.imwrite("cropchar/img_" + (i++) + ".jpg", cropImg);
		}
		drawContours(image, boundingRectPoint, -1, new Scalar(0, 255, 0), 1);
		drawContours(image, contours, -1, new Scalar(0, 0, 255), 1);
		Highgui.imwrite("drawBound.jpg", image);
		return plateImg.clone();
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
		Mat plateImg;
		img = Highgui.imread("detectplate.jpg");
		resize(img, img, new Size(600, 270));
		plateImg = preprocessPlate(img);
		Highgui.imwrite("preprocessed.jpg", plateImg);
	}
}
