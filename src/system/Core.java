package system;

import java.util.ArrayList;
import java.util.List;

import ocr.text.recognition.OCR;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import plate.detection.PlateNumberDetector;

public class Core {
	public boolean open(){
		return false;
	};
	private static String readPlate() {
		// TODO
		System.loadLibrary("opencv_java248");
		String plateNumber = "";
		plateNumber = dummyReadPlate();
		return plateNumber;
	}
	
	private static String dummyReadPlate() {
		System.loadLibrary("opencv_java248");
		Mat carImage = new Mat();
		String plateNumber = "";
		List<Mat> charImageList = new ArrayList<>();
		Mat dummyPlateImage = Highgui.imread("sourcedata/LP5.jpg");
		charImageList = PlateNumberDetector.getCharImageList(dummyPlateImage);
		for (Mat charImage : charImageList) {
			plateNumber = plateNumber + OCR.recognizeCharImage(charImage);
		}
		return plateNumber;
	}
	
	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		System.out.println(readPlate()); // push car image
	}
}
