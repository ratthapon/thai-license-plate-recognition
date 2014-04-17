package plate.text.segmentation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class TextSegment extends Imgproc {
	private static int structureElementSize = 3;
	private static double calibrate = Math.floor(structureElementSize / 2.0);
	private static int plateHeight = 90; // px
	private static int plateWidth = (int) (20.0 / 9.0 * plateHeight); // px
	private static int charSizeThresh = plateHeight * 1 / 3; // px 1/3 of plat
																// hieght

	public static ArrayList<Mat> segmentText(Mat image) {
		resize(image, image, new Size(plateWidth, plateHeight));
		Mat plateImg = image.clone();

		// preprocessing image
		cvtColor(plateImg, plateImg, COLOR_RGBA2GRAY);
		// GaussianBlur(plateImg, plateImg, new Size(3, 3), 3);
		threshold(plateImg, plateImg, 0, 255, THRESH_OTSU);
		threshold(plateImg, plateImg, 0, 255, THRESH_BINARY_INV);
		Highgui.imwrite("log/preprocess.jpg", plateImg);
		Mat preprocessPlate = plateImg.clone();

		// apply some dilation and erosion to join the gaps
		for (int i = 1; i < structureElementSize; i++) {
			Mat structureElement = getStructuringElement(MORPH_RECT, new Size(
					i, 1));
			dilate(plateImg, plateImg, structureElement);
			erode(plateImg, plateImg, structureElement);
		}
		Highgui.imwrite("log/morphological.jpg", plateImg);

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRectPoint = new ArrayList<MatOfPoint>();
		List<Rect> boundingRect = new ArrayList<Rect>();
		List<Rect> candidateRectList = new ArrayList<Rect>();
		Mat hierarchy = new Mat();
		findContours(plateImg, contours, hierarchy, RETR_LIST,
				CHAIN_APPROX_NONE);
		cvtColor(plateImg, plateImg, COLOR_GRAY2RGBA);
		System.out.println("size of contours " + contours.size());
		Highgui.imwrite("log/contours.jpg", plateImg);

		// create bounding rect for crop
		for (MatOfPoint matOfPoint : contours) {
			Rect tempRect = boundingRect(matOfPoint);
			MatOfPoint tmp = new MatOfPoint(new Point(tempRect.x - calibrate,
					tempRect.y - calibrate), new Point(tempRect.x
					+ tempRect.width - calibrate, tempRect.y - calibrate),
					new Point(tempRect.x + tempRect.width - calibrate,
							tempRect.y + tempRect.height - calibrate),
					new Point(tempRect.x - calibrate, tempRect.y
							+ tempRect.height - calibrate));

			// determine char candidate using criteria
			// 1. char h must relate to charSizeThresh
			boolean rule1 = tempRect.height > charSizeThresh
					&& tempRect.height < charSizeThresh * 2;
			// 2. char size must relate to ratio of w and h of it self
			boolean rule2 = tempRect.width > charSizeThresh / 3
					&& tempRect.width < charSizeThresh;
			if (rule1 && rule2) {
				boundingRectPoint.add(tmp);
				boundingRect.add(boundingRect(tmp));
				candidateRectList.add(boundingRect(tmp));
			}
		}
		// 3. remove all contour that contained by other contour
		int i = 0;
		for (Rect inner : candidateRectList) {
			for (Rect outer : candidateRectList) {
				if (inner.x > outer.x && inner.y > outer.y
						&& inner.x + inner.width < outer.x + outer.width
						&& inner.y + inner.height < outer.y + outer.height) {
					boundingRect.remove(i);
					boundingRectPoint.remove(i);
				}
			}
			i++;
		}
		// sort by position left to right
		Collections.sort(boundingRect, horizontalOrderComparator);

		// build return value
		ArrayList<Mat> charImageList = new ArrayList<Mat>();
		for (Rect rect : boundingRect) {
			Mat cropImg = (new Mat(preprocessPlate, rect)).clone();
			threshold(cropImg, cropImg, 127, 255, THRESH_BINARY);
			resize(cropImg, cropImg, new Size(32, 32));
			charImageList.add(cropImg);
		}
		return charImageList;
	}

	// for sort character in plate
	static Comparator<Rect> horizontalOrderComparator = new Comparator<Rect>() {
		public int compare(Rect c1, Rect c2) {
			return c1.x - c2.x;
		}
	};

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		Mat img;
		ArrayList<Mat> charList;
		String[] filename = { "LP2.jpg", "LP3.jpg", "LP5.jpg", "LP6.png",
				"LP8.jpg" };
		for (int i = 0; i < filename.length; i++) {
			// remove old file
			File folder = new File("segment/" + i);
			File[] listOfFiles = folder.listFiles();
			for (int n = 0; n < listOfFiles.length; n++) {
				if (listOfFiles[n].isFile()) {
					System.out.println("segment/" + i + "/"
							+ listOfFiles[n].getName());
					(new File("segment/" + i + "/" + listOfFiles[n].getName()))
							.delete();
				}
			}

			// create new file
			String dirName = "sourcedata/";
			img = Highgui.imread(dirName + filename[i]);
			charList = segmentText(img);
			int j = 1;
			for (Mat mat : charList) {
				Highgui.imwrite("segment/" + i + "/" + filename[i] + "_"
						+ (j++) + ".jpg", mat);
			}
		}

	}
}
