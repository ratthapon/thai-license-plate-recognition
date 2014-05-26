package samplecode;

import java.util.ArrayList;
import java.util.List;

import ocr.text.recognition.OCR;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class SampleOCR {

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		List<Mat> charImageMatList = new ArrayList<>();
		Mat charImageMat = Highgui.imread("testChar.bmp");
		charImageMatList.add(charImageMat);
		OCR.setModelPath("testmodel.bin");
		int[] asciiCodes ;
		asciiCodes = OCR.recognizeCharImage(charImageMatList);
		System.out.print("Result ascii code ");
		for (int i = 0; i < asciiCodes.length; i++) {
			System.out.print(asciiCodes[i]+",");
		}
		
	}

}
