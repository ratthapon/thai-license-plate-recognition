package text.segmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
		Imgproc.adaptiveThreshold(plateImg, plateImg, 255,
				Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 1, 11, 5);
		// Imgproc.cvtColor(plateImg, plateImg, Imgproc.COLOR_BGR2GRAY);
		Highgui.imwrite("adaptived.jpg", plateImg);

		// apply some dilation and erosion to join the gaps
		/*
		 * >> fasf = f; >> for k = 2:5 se = strel('disk', k); fasf =
		 * imclose(imopen(fasf, se), se); end >> figure imshow(fasf); >> figure
		 */
		for (int i = 5; i <= 14; i++) {
			Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
					new Size(i, i));
			Imgproc.dilate(plateImg, plateImg, kernel);
			Imgproc.erode(plateImg, plateImg, kernel);
		}
		Highgui.imwrite("open and close.jpg", plateImg);

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRect = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();

		Imgproc.findContours(plateImg, contours, hierarchy, Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);

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
			boundingRect.add(tmp);

		}
		Imgproc.drawContours(plateImg, boundingRect, -1, new Scalar(255, 255, 255));
		Highgui.imwrite("contour.jpg", plateImg);
		return plateImg.clone();
	}

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		Mat img;
		Mat plateImg;
		img = Highgui.imread("LP2.jpg");
		plateImg = preprocessPlate(img);
		Highgui.imwrite("preprocessed.jpg", plateImg);
	}
}
