package ocr.text.recognition;

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

	public static void testClassifier() {
		//
		// THIS IS CLASSIFY PHASE
		//
		// classify

		Model model = new Model("model.bin");

		Mat sample = new MatOfDouble();
		Vector<Mat> sampleVector = new Vector<>();
		for (int i = 1; i <= 8; i++) {
			Mat img = Highgui.imread("sourcedata/CHAR/c" + i + ".bmp");
			Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
			Imgproc.resize(img, img, new Size(32, 32));
			img = img.reshape(1, 32 * 32);
			img.convertTo(img, model.getMeanVec().type());
			sampleVector.add(img.clone());
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
		Mat V = model.getV(), eigenvectors = model.getEigenvectors(), response = model
				.getResponse();
		Core.gemm(V, sample, 1.0, Mat.zeros(V.rows(), sample.cols(), V.type()),
				0, testDataEigen);
		testDataEigen = testDataEigen.t();

		// build mat for result from knn size equal test data
		Mat result = new Mat(8, 1, CvType.CV_32FC1);

		// knn classify
		CvKNearest knn = new CvKNearest(eigenvectors, response);
		knn.find_nearest(testDataEigen, 1, result, new Mat(), new Mat());

		// Highgui.imwrite(directory + "feature.jpg", tr_vectors);
		System.out.println("result " + result.dump());

	}

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
