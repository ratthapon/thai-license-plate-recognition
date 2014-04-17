package system;

import java.util.ArrayList;
import java.util.List;

import ocr.text.recognition.OCR;

import org.opencv.core.Mat;

import plate.detection.Detector;

public class Core {
	public static String readPlate(Mat carImage) {
		// TODO
		System.loadLibrary("opencv_java248");
		String plateNumber = "";
		plateNumber = dummyReadPlate(carImage);
		return plateNumber;
	}
	
	static String dummyReadPlate(Mat carImage) {
		System.loadLibrary("opencv_java248");
		String plateNumber = "";
		List<Mat> charImageList = new ArrayList<>();
		Mat dummyPlateImage = new Mat();
		charImageList = Detector.getCharImageList(dummyPlateImage);
		for (Mat charImage : charImageList) {
			plateNumber = plateNumber + OCR.recognizeCharImage(charImage);
		}
		return plateNumber;
	}
	
	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		System.out.println(readPlate(new Mat())); // push car image
	}
}
