package plate.detection;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class PlateDetect extends Imgproc {
	public static Mat detectPlate(Mat image) {
		Mat plateImg = image.clone();
		System.out.println("Detecting Plate");
		cvtColor(plateImg, plateImg, COLOR_RGB2GRAY);
		Highgui.imwrite("plate/cvt.jpg", plateImg);
		threshold(plateImg, plateImg, 192, 255, THRESH_BINARY);
		Highgui.imwrite("plate/thresh1.jpg", plateImg);
		for (int i = 40; i <= 40; i++) {
			Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
					new Size(i, i));
			Imgproc.dilate(plateImg, plateImg, kernel);
			Imgproc.erode(plateImg, plateImg, kernel);
		}
		Highgui.imwrite("plate/openandclose.jpg", plateImg);
		// Imgproc.adaptiveThreshold(plateImg, plateImg, 255,
		// Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 1, 11, 5);
		// /GaussianBlur(plateImg, plateImg, new Size(5, 5), 0);
		threshold(plateImg, plateImg, 230, 255, THRESH_BINARY);
		Highgui.imwrite("plate/threshold.jpg", plateImg);
		Mat hist = new Mat();
		Mat thresh = plateImg.clone();
		equalizeHist(plateImg, hist);
		Highgui.imwrite("plate/hist.jpg", plateImg);

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRectPoint = new ArrayList<MatOfPoint>();
		List<Rect> boundingRect = new ArrayList<Rect>();
		Mat hierarchy = new Mat();

		findContours(plateImg, contours, hierarchy, RETR_LIST,
				CHAIN_APPROX_SIMPLE);
		cvtColor(plateImg, plateImg, COLOR_GRAY2RGBA);
		System.out.println("size of " + contours.size());

		for (MatOfPoint matOfPoint : contours) {
			Rect tempRect = boundingRect(matOfPoint);
			MatOfPoint tmp = new MatOfPoint(new Point(tempRect.x, tempRect.y),
					new Point(tempRect.x + tempRect.width, tempRect.y),
					new Point(tempRect.x + tempRect.width, tempRect.y
							+ tempRect.height), new Point(tempRect.x,
							tempRect.y + tempRect.height));
			if ((Core.sumElems(thresh.submat(tempRect)).val[0] / (tempRect.width
					* tempRect.height * 256)) >= 0.95
					&& ((tempRect.width * tempRect.height) > 10000)) {
				System.out.println("This is plate");
				boundingRectPoint.add(tmp);
				boundingRect.add(tempRect);
			}

		}
		/*
		 * MatOfPoint2f approx = new MatOfPoint2f(); for (int i = 0; i <
		 * contours.size(); i++) { approxPolyDP( new
		 * MatOfPoint2f(contours.get(i).toArray()), (MatOfPoint2f) approx,
		 * arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) * 0.02,
		 * true);
		 * 
		 * System.out.println("approx len " + approx.rows()); if (approx.rows()
		 * ==4 && isContourConvex(new MatOfPoint(approx.toArray()))) { double
		 * maxCosine = 0; Rect tempRect = boundingRect(new
		 * MatOfPoint(approx.toArray())); MatOfPoint tmp = new MatOfPoint(new
		 * Point(tempRect.x, tempRect.y), new Point(tempRect.x + tempRect.width,
		 * tempRect.y), new Point(tempRect.x + tempRect.width, tempRect.y +
		 * tempRect.height), new Point(tempRect.x, tempRect.y +
		 * tempRect.height)); boundingRectPoint.add(tmp);
		 * 
		 * System.out.println("found rect"); for (int j = 2; j < 5; j++) { } } }
		 */
		//drawContours(image, contours, -1, new Scalar(0, 0, 255), 1);
		drawContours(image, boundingRectPoint, -1, new Scalar(0, 255, 0), 1);
		cvtColor(thresh, thresh, COLOR_GRAY2RGB);
		drawContours(thresh, boundingRectPoint, -1, new Scalar(0, 255, 0), 3);
		Highgui.imwrite("plate/drawBound.jpg", image);
		Highgui.imwrite("plate/thresh.jpg", thresh);
		Mat plate = new Mat();
		plate = image.submat(boundingRect.get(0)).clone();
		return plate;
	}

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		Mat img;
		Mat plateImg;
		img = Highgui.imread("CAR1.jpg");
		resize(img, img, new Size(1024, 768));
		plateImg = detectPlate(img);
		resize(plateImg, plateImg, new Size(600, 270));
		Highgui.imwrite("detectplate.jpg", plateImg);

	}

}
