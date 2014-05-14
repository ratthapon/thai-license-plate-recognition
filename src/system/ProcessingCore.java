package system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ocr.text.recognition.OCR;
import ocr.text.segmentation.TextSegment;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import plate.detection.Band;
import plate.detection.Car;
import plate.detection.Plate;
import utils.Utils;

public class ProcessingCore {
	public static String logtag = "";
	private static int testCount = 0;
	private static int foundCount = 0;
	private static int notFoundCOunt = 0;
	private static int isPlateCount = 0;

	public static void detectPlate() {
		System.loadLibrary("opencv_java248");
		FileReader fileReader;
		BufferedReader bufferedReader;
		List<String> lines;
		String line;
		try {
			fileReader = new FileReader("car_name.txt");
			bufferedReader = new BufferedReader(fileReader);
			lines = new ArrayList<String>();
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();

			// remove old file
			File folder = new File("platelocalize/");
			folder.mkdir();
			File[] listOfFiles = folder.listFiles();
			for (int n = 0; n < listOfFiles.length; n++) {
				if (listOfFiles[n].isDirectory()) {
					File file = new File("platelocalize/"
							+ listOfFiles[n].getName());
					file.delete();
				}
				if (listOfFiles[n].isFile()) {
					File file = new File("platelocalize/"
							+ listOfFiles[n].getName());
					file.delete();
				}
			}

			String[] filename = lines.toArray(new String[lines.size()]);
			for (int i = 0; i < filename.length; i++) {

				folder = new File("log/");
				folder.mkdir();
				listOfFiles = folder.listFiles();
				for (int n = 0; n < listOfFiles.length; n++) {
					if (listOfFiles[n].isDirectory()) {
						(new File("log/" + listOfFiles[n].getName())).delete();
					}
				}
				logtag = filename[i].split(".j")[0].split(".p")[0];
				File file = new File("log/" + logtag);
				file.mkdir();
				System.out.println(filename[i]);
				
				// load car image 
				Car car = new Car(filename[i]);
				// detect all plate
				List<Plate> plates = new ArrayList<Plate>();
				plates = car.clipPlatesMaxBandLimit(7);
				testCount++;

				if (plates.size() <= 0) {
					notFoundCOunt++;
					System.out.println("image " + filename[i] + " not found");
					continue;
				} else {
					foundCount++;
					System.out
							.println("Foune candidate plate " + plates.size());
				}
				int p = 1;
				
				// show bounding rect
				Mat showBounding = car.toMat().clone();
				List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

				for (Plate plate : plates) {
					Point p1 = new Point(plate.getBoundingRect().x,
							plate.getBoundingRect().y);
					Point p2 = new Point(plate.getBoundingRect().x
							+ plate.getBoundingRect().width,
							plate.getBoundingRect().y);
					Point p3 = new Point(plate.getBoundingRect().x
							+ plate.getBoundingRect().width,
							plate.getBoundingRect().y
									+ plate.getBoundingRect().height);
					Point p4 = new Point(plate.getBoundingRect().x,
							plate.getBoundingRect().y
									+ plate.getBoundingRect().height);
					contours.add(new MatOfPoint(p1, p2, p3, p4));
					//Highgui.imwrite("platelocalize/" + logtag + "_PLATE_"
					//		+ (p++) + ".jpg", plate.toMat());
					List<Mat> charImageList = TextSegment
							.getListMatOfCharImage(plate.toMat());
					if (charImageList.size() >= 1) {
						file = new File("platelocalize/" + logtag + "_CHAR/");
						file.mkdir();
						//Highgui.imwrite("platelocalize/" + logtag + "_CHAR/"
						//		+ logtag + "_PLATE_" + (p++) + ".jpg",
						//		plate.toMat());
						isPlateCount++;
					}
					int c = 0;
					for (Mat mat : charImageList) {
						//Highgui.imwrite("platelocalize/" + logtag + "_CHAR/"
						//		+ logtag + "_CHAR_" + (c++) + ".jpg", mat);
					}
				}
				Imgproc.drawContours(showBounding, contours, -1, new Scalar(0,
						255, 0),3);
				//Highgui.imwrite("platelocalize/" + logtag + "_LOCATE_" + (p++)
				//		+ ".jpg", showBounding);
				// end of bounding plates
				
				// image for presentation session 2
				List<Band> bands = car.clipBands(7);
				List<MatOfPoint> bandBound = new ArrayList<MatOfPoint>();
				Band band = bands.get(bands.size()-1);
				Point b1 = new Point(band.getBoundingRect().x,
						band.getBoundingRect().y);
				Point b2 = new Point(band.getBoundingRect().x
						+ band.getBoundingRect().width,
						band.getBoundingRect().y);
				Point b3 = new Point(band.getBoundingRect().x
						+ band.getBoundingRect().width,
						band.getBoundingRect().y
								+ band.getBoundingRect().height);
				Point b4 = new Point(band.getBoundingRect().x,
						band.getBoundingRect().y
								+ band.getBoundingRect().height);
				bandBound.add(new MatOfPoint(b1, b2, b3, b4));
				showBounding = car.toMat().clone();
				Imgproc.drawContours(showBounding, bandBound, -1, new Scalar(0,
						255, 0),3);
				//Highgui.imwrite("platelocalize/" + logtag + "_BAND.jpg", showBounding);
				//Highgui.imwrite("platelocalize/" + logtag + "_VERTICALLINE.jpg", Utils.verticalLine(car.toMat()));
				
				Mat histogram = new Mat();
				histogram = Utils.histoGraph(Utils.verticalLine(car.toMat()), false, true);
				//Highgui.imwrite("platelocalize/" + logtag + "_HISTLINE.jpg", histogram);
			}

			System.out.println("Test "+testCount+" img. Found rect " + foundCount + ". Not found "
					+ notFoundCOunt + " is Plate " + isPlateCount);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String readPlate(Mat carMat) {
		// load car image 
		System.loadLibrary("opencv_java248");
		Car car = new Car(carMat);
		// detect all plate
		List<Plate> plates = new ArrayList<Plate>();
		plates = car.clipPlatesMaxBandLimit(7);
		String result = "";
		for (Plate plate : plates) {
			List<Mat> charMatList = new ArrayList<Mat>();
			charMatList = TextSegment.getListMatOfCharImage(plate.toMat());
			int[] charCode = OCR.recognizeCharImage(charMatList);
			System.out.print("OUTPUT ");
			for (int i = 0; i < charCode.length; i++) {
				System.out.printf("%c ",charCode[i]);
				result = result + String.format("%c", charCode[i]);
			}
			result = result + "\r\n";
			System.out.println();
			
		}
		return result;
	}

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		detectPlate();
		
		
		//Trainer.train("trainFileNameList.txt", "trainLabelList.txt","400dpi_NB_TN_all.bin");
		OCR.testClassifier();
	}
}
