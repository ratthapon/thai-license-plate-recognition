package plate.text.segmentation;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class TextSegment extends Imgproc {
	private static String logtag = "";
	private static int structureElementSize = 3;
	private static double calibrate = Math.floor(structureElementSize / 2.0);
	private static int plateHeight = 90; // px
	private static int plateWidth = (int) (20.0 / 9.0 * plateHeight); // px
	private static int charSizeThresh = plateHeight * 1 / 3; // px 1/3 of plat
																// hieght

	public static ArrayList<IplImage> segmentText(IplImage iplImage) {
		ByteBuffer iplBuffer = iplImage.getByteBuffer();
		// Create a Matrix the same size of image
		byte[] data;
		data = new byte[iplBuffer.remaining()];
		Mat image = new Mat(iplImage.height(), iplImage.width(), CvType.CV_8UC3);
		// transfer bytes from this buffer into the given destination array
		iplBuffer.get(data, 0, data.length);
		// Retrieve all bytes in the buffer
		iplBuffer.clear();
		// Fill Matrix with image values
		image.put(0, 0, data);
		Highgui.imwrite("log/" + logtag + "/iplmat.jpg", image);

		ArrayList<IplImage> iplImageList = new ArrayList<>();
		ArrayList<Mat> charList;
		charList = segmentText(image);
		for (Mat mat : charList) {
			data = new byte[(int) mat.size().area()];
			BufferedImage buff = new BufferedImage(mat.width(), mat.height(),
					BufferedImage.TYPE_4BYTE_ABGR);
			WritableRaster raster = buff.getRaster();
			DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
			data = dataBuffer.getData();
			mat.get(0, 0, data);
			IplImage img = IplImage.createFrom(buff);
			iplImageList.add(img);
		}
		return iplImageList;
	}

	public static ArrayList<Mat> segmentText(Mat image) {
		resize(image, image, new Size(plateWidth, plateHeight));
		Mat plateImg = image.clone();
		// preprocessing image
		cvtColor(plateImg, plateImg, COLOR_RGB2GRAY);
		// GaussianBlur(plateImg, plateImg, new Size(3, 3), 3);
		threshold(plateImg, plateImg, 0, 255, THRESH_OTSU);
		threshold(plateImg, plateImg, 0, 255, THRESH_BINARY_INV);
		Highgui.imwrite("log/" + logtag + "/preprocess.jpg", plateImg);
		Mat preprocessPlate = plateImg.clone();

		// apply some dilation and erosion to join the gaps
		for (int i = 1; i < structureElementSize; i++) {
			Mat structureElement = getStructuringElement(MORPH_RECT, new Size(
					i, 1));
			dilate(plateImg, plateImg, structureElement);
			erode(plateImg, plateImg, structureElement);
		}
		Highgui.imwrite("log/" + logtag + "/morphological.jpg", plateImg);

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
			boolean rule2 = tempRect.width > charSizeThresh /2
					&& tempRect.width < charSizeThresh * 1.5;
			if (rule1 && rule2 ) {
				boundingRectPoint.add(tmp);
				boundingRect.add(boundingRect(tmp));
				candidateRectList.add(boundingRect(tmp));
			}
		}
		drawContours(image, boundingRectPoint, -1, new Scalar(0, 255, 0), 1);
		Highgui.imwrite("log/" + logtag + "/contours.jpg", image);
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
			threshold(cropImg, cropImg, 127, 255, THRESH_BINARY_INV);
			resize(cropImg, cropImg, new Size(32, 32));
			cvtColor(cropImg, cropImg, COLOR_GRAY2RGBA);
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
		ArrayList<Mat> charList;
		// ArrayList<IplImage> iplList;
		String[] filename = { "LP2.jpg", "LP3.jpg", "LP6.png", "LP7.jpg",
				"LP8.jpg", "LP9.jpg", "LP10.png", "LP11.jpg", "LP5.jpg",
				"LP12.jpg", "LP13.jpg" }; // , "LP5.jpg"
		for (int i = 0; i < filename.length; i++) {
			// remove old file
			File folder = new File("segment/");
			folder.mkdir();
			File[] listOfFiles = folder.listFiles();
			for (int n = 0; n < listOfFiles.length; n++) {
				if (listOfFiles[n].isDirectory()) {
					// System.out.println("segment/" + i + "/"
					// + listOfFiles[n].getName());
					File file = new File("segment/" + listOfFiles[n].getName());
					file.delete();
				}
			}
			folder = new File("log/");
			folder.mkdir();
			listOfFiles = folder.listFiles();
			for (int n = 0; n < listOfFiles.length; n++) {
				if (listOfFiles[n].isDirectory()) {
					// System.out.println("log/" + i + "/"
					// + listOfFiles[n].getName());
					(new File("log/" + listOfFiles[n].getName())).delete();
				}
			}

			// create new file
			String dirName = "sourcedata/";
			// IplImage img = cvLoadImage(dirName + filename[i]);
			File file;
			logtag = filename[i].split(".j")[0].split(".p")[0];
			file = new File("segment/" + logtag);
			file.mkdir();
			file = new File("log/" + logtag);
			file.mkdir();
			Mat img2 = Highgui.imread(dirName + filename[i]);
			System.out.println(logtag);
			resize(img2, img2, new Size(75, 33));
			charList = segmentText(img2);
			int j;
			j = 1;
			for (Mat mat : charList) {

				Highgui.imwrite("segment/" + logtag + "/" + filename[i] + "_"
						+ (j++) + ".jpg", mat);
			}
			/*
			 * j = 1; for (IplImage iplImage : iplList) { file = new
			 * File("segment/" + filename[i]); file.mkdir(); file = new
			 * File("log/" + filename[i]); file.mkdir(); cvSaveImage("segment/"
			 * + filename[i] + "/" + filename[i] + "_" + (j++) + ".jpg",
			 * iplImage); }
			 */
		}

	}
}
