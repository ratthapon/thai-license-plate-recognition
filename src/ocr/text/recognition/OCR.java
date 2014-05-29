package ocr.text.recognition;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import ocr.text.trainer.Model;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvKNearest;

public class OCR {
	private static OCR ocrInstance = null;

	private static Model model;
	private static String modelPath = "400dpi_NB_TN_1500.bin";

	public static String getModelPath() {
		return modelPath;
	}

	public static void setModelPath(String modelPath) {
		System.loadLibrary("opencv_java248");
		OCR.modelPath = modelPath;
		model = new Model(modelPath);
	}

	private OCR() {
		System.loadLibrary("opencv_java248");
	}

	public static int[] recognizeCharImage(List<Mat> charImageList) {
		if (ocrInstance == null) {
			ocrInstance = new OCR();
		}
		if (model == null) {
			model = new Model(modelPath);
		}
		double[] doubleResult = new double[charImageList.size()];
		Date timer = new Date();
		long startTime = timer.getTime();
		int testCount = 0;
		Mat sample = new MatOfDouble();
		Vector<Mat> sampleVector = new Vector<>();
		for (int i = 0; i < charImageList.size(); i++) {
			Mat img = charImageList.get(i);
			Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
			Imgproc.resize(img, img, new Size(32, 32));
			Imgproc.threshold(img, img, 0, 255, Imgproc.THRESH_BINARY_INV);
			img = img.reshape(1, 32 * 32);
			img.convertTo(img, model.getMeanVec().type());
			sampleVector.add(img.clone());
			testCount++;
		}
		Core.hconcat(sampleVector, sample);

		// build mean matrix for normalize
		List<Mat> meanVecList = new Vector<Mat>();
		for (int i = 0; i < sample.cols(); i++) {
			meanVecList.add(model.getMeanVec().clone());
		}
		Mat mean = new Mat(model.getTrainCount(), 32 * 32, model.getMeanVec()
				.type());
		Core.hconcat(meanVecList, mean);
		Core.divide(mean, new Scalar(model.getTrainCount()), mean);

		// normalize
		Core.subtract(sample, mean, sample);

		// build covariance matrix of test data
		Mat testDataEigen = new Mat();
		Mat V = model.getV();
		Mat eigenvectors = model.getEigenvectors();
		Mat response = model.getResponse();
		Core.gemm(V, sample, 1.0,
				Mat.zeros(V.rows(), sample.cols(), eigenvectors.type()), 0,
				testDataEigen);
		testDataEigen = testDataEigen.t();

		// build mat for result from knn size equal test data
		Mat result = new Mat(testCount, 1, CvType.CV_32FC1);

		// knn classify
		CvKNearest knn = new CvKNearest(eigenvectors, response);
		knn.find_nearest(testDataEigen, 1, result, new Mat(), new Mat());
		timer = new Date();
		long endTime = timer.getTime();
		System.out.println("Recognize " + testCount + " character "
				+ ((endTime - startTime) / 1000.0) + " sec.Speed "
				+ (testCount / ((endTime - startTime) / 1000.0)) + " c/s");
		result.convertTo(result, CvType.CV_64F);
		System.out.println("result " + result.dump());
		result.get(0, 0, doubleResult);
		int[] intResult = new int[doubleResult.length];
		for (int i = 0; i < intResult.length; i++) {
			intResult[i] = (int) doubleResult[i];
		}
		return intResult;
	}

	static double accuracyRate(int[] result, int[] expected) {
		double acc = 0.0;
		for (int i = 0; i < expected.length; i++) {
			if (result[i] == expected[i]) {
				acc += 1;
			}
		}
		return acc / result.length * 100;
	}
}
