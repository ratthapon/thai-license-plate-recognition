package samplecode;

import java.util.ArrayList;
import java.util.List;

import ocr.text.recognition.OCR;
import ocr.text.segmentation.TextSegment;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import plate.detection.Band;
import plate.detection.Car;
import plate.detection.Plate;

public class SampleDetectPlate {

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		Car car = new Car(Highgui.imread("testCarImage.jpg"));

		// method 1
		List<String> resultList = car.readPlate();
		for (String string : resultList) {
			System.out.println("Result " + string); // result is up to model
		}

		// method 2
		List<Band> bands = new ArrayList<Band>();
		bands = car.clipBands(1); // up to row you want to analyze
		List<Plate> plates = new ArrayList<Plate>();
		for (Band band : bands) {
			plates.addAll(band.clipPlates2(car.toMat()));
		}
		for (Plate plate : plates) {
			List<Mat> charImageMatList = TextSegment
					.getListMatOfCharImage(plate.toMat());
			int[] resultAsciiCode = OCR.recognizeCharImage(charImageMatList);
			System.out.println("\n\rResult ");
			for (int i = 0; i < resultAsciiCode.length; i++) {
				int c = resultAsciiCode[i];
				if (resultAsciiCode[i] >= 161) {
					c = 0x0e00 + (resultAsciiCode[i] - 160); // Thai char
				}
				System.out.print(String.format("%c", c));
			}
		}

		// method 3
		plates = car.clipPlates(1);
		for (Plate plate : plates) {
			List<Mat> charImageMatList = TextSegment
					.getListMatOfCharImage(plate.toMat());
			if (charImageMatList == null || charImageMatList.size() <= 0) {
				break;
			}
			int[] resultAsciiCode = OCR.recognizeCharImage(charImageMatList);
			System.out.println("\n\rResult ");
			for (int i = 0; i < resultAsciiCode.length; i++) {
				int c = resultAsciiCode[i];
				if (resultAsciiCode[i] >= 161) {
					c = 0x0e00 + (resultAsciiCode[i] - 160); // Thai char
				}
				System.out.print(String.format("%c", c));
			}
		}
	}

}
