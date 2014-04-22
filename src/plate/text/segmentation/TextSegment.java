package plate.text.segmentation;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
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
	private static TextSegment textSegmentObj = null;
	private static String logtag = ""; // TODO debug var
	private int structureElementSize = 3;
	private double calibrate = Math.floor(structureElementSize / 2.0);
	private int plateHeight = 90; // px
	private int plateWidth = (int) (20.0 / 9.0 * plateHeight); // px
	private int charSizeThresh = plateHeight * 1 / 3; // px 1/3 of plat
																// hieght
	
	private ArrayList<IplImage> segmentText(IplImage iplImage) {
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
		Highgui.imwrite("log/" + logtag + "/iplmat.jpg", image); // TODO Log

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
	
	public static ArrayList<Mat> getListMatOfCharImage(Mat image){
		if (textSegmentObj == null) {
			textSegmentObj = new TextSegment();
		}
		return textSegmentObj.segmentText(image);
	}

	private ArrayList<Mat> segmentText(Mat image) {
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
		Highgui.imwrite("log/" + logtag + "/morphological.jpg", plateImg);// TODO
																			// Log

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> boundingRectPoint = new ArrayList<MatOfPoint>();
		List<Rect> boundingRect = new ArrayList<Rect>();
		List<Rect> candidateRectList = new ArrayList<Rect>();
		Mat hierarchy = new Mat();
		findContours(plateImg, contours, hierarchy, RETR_LIST,
				CHAIN_APPROX_SIMPLE);
		cvtColor(plateImg, plateImg, COLOR_GRAY2RGBA);
		System.out.println("size of contours " + contours.size());// TODO Log

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
			boolean rule2 = tempRect.width > charSizeThresh * 1 / 3
					&& tempRect.width < charSizeThresh * 2;
			if (rule1 && rule2) {
				boundingRectPoint.add(tmp);
				boundingRect.add(boundingRect(tmp));
				candidateRectList.add(boundingRect(tmp));
			}
		}
		drawContours(image, boundingRectPoint, -1, new Scalar(0, 255, 0), 1);
		Highgui.imwrite("log/" + logtag + "/contours.jpg", image);// TODO Log
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

	private static void testSegment() {
		System.loadLibrary("opencv_java248");

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

		ArrayList<Mat> charList;
		String[] filename = { "1.jpg", "2.jpg", "3.jpg", "4.jpg", "5.jpg",
				"6.jpg", "7.jpg", "8.jpg", "9.jpg", "10.jpg", "11.png",
				"12.jpg", "13.jpg", "14.png", "15.jpg", "16.jpg", "17.jpg",
				"18.jpg" }; // , "LP5.jpg"
		String[] ans = { "กก35", "กจ99", "กด7171", "กค9535", "ก2219", "ปพ6945",
				"ฎผ6557", "ฎก7700", "ชห9515", "จร638", "กอ9999", "กท9999",
				"กท1000", "983597", "704928", "๔๒๗๔๙", "06469", "40ม2", };
		for (int i = 0; i < filename.length; i++) {
			// create new file
			String dirName = "sourcedata/LP/";
			logtag = filename[i].split(".j")[0].split(".p")[0];
			System.out.println("LOGTAG " + logtag + " read "
					+ (dirName + filename[i]));
			(new File("segment/" + logtag)).mkdir();
			(new File("log/" + logtag)).mkdir();
			if ((new File(dirName + filename[i])).exists()) {
				Mat plateImage = Highgui.imread(dirName + filename[i]);
				charList = getListMatOfCharImage(plateImage);
				int j;
				j = 1;
				try {
					(new File("segment/" + logtag + "/" + ans[i]))
							.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("LP length " + charList.size());
				for (Mat mat : charList) {
					Highgui.imwrite("segment/" + logtag + "/" + logtag + "_"
							+ (j++) + ".jpg", mat);
				}
			} else {
				System.out.println("File not found");
			}
		}
	}

	public static void main(String[] args) {
		testSegment();
	}
}
