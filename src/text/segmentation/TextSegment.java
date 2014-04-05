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

class TextSegment {
	public static Mat preprocessPlate(Mat image) {
		Mat plateImg = image.clone();
		System.out.println("Segmenting Text");
		Imgproc.cvtColor(plateImg, plateImg, Imgproc.COLOR_RGBA2GRAY);
		Highgui.imwrite("cvt.jpg", plateImg);
		Imgproc.adaptiveThreshold(plateImg, plateImg, 255,
				Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 1, 11, 5);
		// Imgproc.cvtColor(plateImg, plateImg, Imgproc.COLOR_BGR2GRAY);
		Highgui.imwrite("adaptived.jpg", plateImg);

		// apply some dilation and erosion to join the gaps
		for (int i = 5; i <= 14; i++) {
			Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
					new Size(i, i));
			Imgproc.erode(plateImg, plateImg, kernel);
			Imgproc.dilate(plateImg, plateImg, kernel);
			
		}
		Highgui.imwrite("open and close.jpg", plateImg);

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRectPoint = new ArrayList<MatOfPoint>();
		List<Rect> boundingRect = new ArrayList<Rect>();
		Mat hierarchy = new Mat();

		Imgproc.findContours(plateImg, contours, hierarchy, Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.cvtColor(plateImg, plateImg, Imgproc.COLOR_GRAY2RGBA);
		System.out.println("size of " + contours.size());
		// Imgproc.drawContours(plateImg, contours, -1, new Scalar(255, 255,
		// 255));

		for (MatOfPoint matOfPoint : contours) {
			Rect tempRect = Imgproc.boundingRect(matOfPoint);
			MatOfPoint tmp = new MatOfPoint(new Point(tempRect.x, tempRect.y),
					new Point(tempRect.x + tempRect.width, tempRect.y),
					new Point(tempRect.x + tempRect.width, tempRect.y
							+ tempRect.height), new Point(tempRect.x,
							tempRect.y + tempRect.height));
			boundingRectPoint.add(tmp);
			boundingRect.add(tempRect);

		}
		Collections.sort(boundingRect, comparator);
		int i = 0;
		for (Rect rect : boundingRect) {
			Mat cropImg = (new Mat(image, rect)).clone();
			System.out.println("sorted x = "+rect.x);
			Highgui.imwrite("cropchar/img_" + (i++) + ".jpg", cropImg);
		}
		Imgproc.drawContours(image, boundingRectPoint, -1, new Scalar(0,
				255, 0),1);
		Imgproc.drawContours(image, contours, -1, new Scalar(0,
				0, 255),1);
		Highgui.imwrite("drawBound.jpg", image);
		return plateImg.clone();
	}
	
	// for sort character in plate
	static Comparator<Rect> comparator = new Comparator<Rect>() {
		public int compare(Rect c1, Rect c2) {
			return c1.x - c2.x ;
		}
	};

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		Mat img;
		Mat plateImg;
		img = Highgui.imread("LP2.jpg");
		plateImg = preprocessPlate(img);
		Highgui.imwrite("preprocessed.jpg", plateImg);
	}
}
