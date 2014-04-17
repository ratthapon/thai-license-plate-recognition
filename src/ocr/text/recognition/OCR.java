package ocr.text.recognition;

import org.opencv.core.Mat;

public class OCR {
	public static char recognizeCharImage(Mat charImage) {
		// TODO implement this
		System.loadLibrary("opencv_java248");
		char ascii = 0;
		ascii = dummyRecognizeCharImage(charImage);
		return ascii;
	}

	static char dummyRecognizeCharImage(Mat charImage) {
		System.loadLibrary("opencv_java248");
		char ascii = 'ก';
		return ascii;
	}
}
