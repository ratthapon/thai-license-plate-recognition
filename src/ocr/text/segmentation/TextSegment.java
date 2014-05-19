package ocr.text.segmentation;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class TextSegment {
	private static TextSegment textSegmentObj = null;
	private static String logtag = ""; // TODO debug var
	private int plateHeight = 90; // px
	private int plateWidth = (int) (20.0 / 4.0 * plateHeight); // px
	private int structureElementSize = (int) (0.01 * plateHeight);
	private double calibrate = Math.floor(structureElementSize / 2.0);

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
		List<Mat> charList;
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

	public static List<Mat> getListMatOfCharImage(Mat image) {
		if (textSegmentObj == null) {
			textSegmentObj = new TextSegment();
		}
		return textSegmentObj.segmentText(image);
	}

	private List<Mat> segmentText(Mat image) {
		Mat plateImg = image.clone();
		plateWidth = image.width();
		plateHeight = (int) (plateWidth / image.cols()* image.rows());
		structureElementSize = (int) (0.01 * plateHeight);
		calibrate = Math.floor(structureElementSize / 2.0);
		Imgproc.resize(plateImg, plateImg, new Size(plateWidth, plateHeight));

		// preprocessing image
		Imgproc.cvtColor(plateImg, plateImg, Imgproc.COLOR_RGB2GRAY);
		// GaussianBlur(plateImg, plateImg, new Size(3, 3), 3);
		Imgproc.threshold(plateImg, plateImg, 0, 255, Imgproc.THRESH_OTSU);
		Imgproc.threshold(plateImg, plateImg, 0, 255, Imgproc.THRESH_BINARY_INV);
		// Highgui.imwrite("log/" + logtag + "/preprocess.jpg", plateImg);
		Mat preprocessPlate = plateImg.clone();

		// apply some dilation and erosion to join the gaps
		for (int i = 1; i < structureElementSize; i++) {
			Mat structureElement = Imgproc.getStructuringElement(
					Imgproc.MORPH_RECT, new Size(i, 1));
			Imgproc.dilate(plateImg, plateImg, structureElement);
			Imgproc.erode(plateImg, plateImg, structureElement);
		}
		// Highgui.imwrite("log/" + logtag + "/morphological.jpg", plateImg);//
		// TODO
		// Log

		// Find the contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		boundingRectPoint = new ArrayList<MatOfPoint>();
		boundingRect = new ArrayList<Rect>();
		List<Rect> candidateRectList = new ArrayList<Rect>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(plateImg, contours, hierarchy, Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.cvtColor(plateImg, plateImg, Imgproc.COLOR_GRAY2RGBA);
		// System.out.println("size of contours " + contours.size());// TODO Log

		// create bounding rect for crop
		for (MatOfPoint matOfPoint : contours) {
			Rect tempRect = Imgproc.boundingRect(matOfPoint);
			MatOfPoint tmp = new MatOfPoint(new Point(tempRect.x - calibrate,
					tempRect.y - calibrate), new Point(tempRect.x
					+ tempRect.width - calibrate, tempRect.y - calibrate),
					new Point(tempRect.x + tempRect.width - calibrate,
							tempRect.y + tempRect.height - calibrate),
					new Point(tempRect.x - calibrate, tempRect.y
							+ tempRect.height - calibrate));

			// determine char candidate using criteria
			// 1. char h must relate to charSizeThresh
			boolean rule1 = tempRect.height > plateHeight * 0.55
					&& tempRect.height < plateHeight * 0.9;
			// 2. char size must relate to ratio of w and h of it self
			boolean rule2 = tempRect.width > plateHeight * 0.6 * 1 / 4
					&& tempRect.width < plateHeight * 0.6 * 2;
			if (rule1 && rule2) {
				boundingRectPoint.add(tmp);
				boundingRect.add(Imgproc.boundingRect(tmp));
				candidateRectList.add(Imgproc.boundingRect(tmp));
			}
		}

		// Mat boudingLog = image.clone();
		// resize(boudingLog, boudingLog, new Size(plateWidth, plateHeight));
		// drawContours(boudingLog, boundingRectPoint, -1, new Scalar(0, 255,
		// 0),
		// 1);
		// Highgui.imwrite("log/" + logtag + "/contours.jpg", boudingLog);//
		// TODO
		// Log
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
			Imgproc.threshold(cropImg, cropImg, 127, 255,
					Imgproc.THRESH_BINARY_INV);
			Imgproc.resize(cropImg, cropImg, new Size(32, 32));
			Imgproc.cvtColor(cropImg, cropImg, Imgproc.COLOR_GRAY2RGBA);
			charImageList.add(cropImg);
		}
		return charImageList;
	}

	public static List<MatOfPoint> getBoundingRectPoint() {
		return textSegmentObj.boundingRectPoint;
	}

	public static List<Rect> getBoundingRect() {
		return textSegmentObj.boundingRect;
	}

	// for sort character in plate
	static Comparator<Rect> horizontalOrderComparator = new Comparator<Rect>() {
		public int compare(Rect c1, Rect c2) {
			return c1.x - c2.x;
		}
	};
	private List<Rect> boundingRect;
	private List<MatOfPoint> boundingRectPoint;

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

		List<Mat> charList;
		folder = new File("sourcedata/LP/");
		folder.mkdir();
		FileReader fileReader;
		BufferedReader bufferedReader;
		List<String> lines;
		String line;
		try {
			fileReader = new FileReader("sourcedata/LP/plate_name.txt");
			bufferedReader = new BufferedReader(fileReader);
			lines = new ArrayList<String>();
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
			String[] fileNames = lines.toArray(new String[lines.size()]);

			fileReader = new FileReader("sourcedata/LP/lebel.txt");
			bufferedReader = new BufferedReader(fileReader);
			lines = new ArrayList<String>();
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
			String[] ans = lines.toArray(new String[lines.size()]);
			for (int i = 0; i < fileNames.length; i++) {
				// create new file
				String dirName = "sourcedata/LP/";
				logtag = fileNames[i].split(".j")[0].split(".p")[0].replace(
						"/", "_");
				System.out.println("LOGTAG " + logtag + " read "
						+ (dirName + fileNames[i]));
				(new File("segment/" + logtag)).mkdir();
				(new File("log/" + logtag)).mkdir();
				File sourceFile = new File("sourcedata/LP/" + fileNames[i]);
				if (sourceFile.exists() && sourceFile.isFile()) {
					Mat plateImage = Highgui.imread(dirName + fileNames[i]);
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
						Highgui.imwrite("segment/" + logtag + "/" + logtag
								+ "_" + (j++) + ".bmp", mat);
					}
				} else {
					System.out.println("File not found");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
