package ocr.text.trainer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvKNearest;

public class Trainer {
	private static String fileName = "sourcedata/CHAR/char_name.txt";
	private static String directory = "sourcedata/CHAR/";

	public static Mat dataBuilder(String fileListName) {
		System.loadLibrary("opencv_java248");
		FileReader fileReader;
		BufferedReader bufferedReader;
		List<String> lines;
		String line;
		int trainCount = 0;
		try {
			fileReader = new FileReader(fileListName);
			bufferedReader = new BufferedReader(fileReader);
			lines = new ArrayList<String>();
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
			String[] filename = lines.toArray(new String[lines.size()]);

			Mat charImageMat;
			// build datachar (Matrix of vector of char image)
			Mat dataChar = new MatOfDouble();
			Mat meanVec = MatOfDouble.zeros(32 * 32, 1, CvType.CV_32FC1);

			List<Mat> dataCharVectorList = new Vector<Mat>();

			for (int i = 0; i < filename.length; i++) {
				String fileName = directory + filename[i];
				charImageMat = Highgui.imread(fileName);
				Imgproc.cvtColor(charImageMat, charImageMat,
						Imgproc.COLOR_RGB2GRAY);

				Imgproc.resize(charImageMat, charImageMat, new Size(32, 32));
				charImageMat.convertTo(charImageMat, meanVec.type());
				System.out.println(fileName);
				Core.add(meanVec, charImageMat.reshape(1, 32 * 32), meanVec);
				dataCharVectorList
						.add(charImageMat.reshape(1, 32 * 32).clone());
				trainCount++;
			}
			Core.hconcat(dataCharVectorList, dataChar);

			System.out.println("dataChar " + dataChar.size());
			System.out.println("meanVec " + meanVec.size());

			// build mean matrix for normalize
			List<Mat> meanVecList = new Vector<Mat>();
			for (int i = 0; i < filename.length; i++) {
				meanVecList.add(meanVec.clone());
			}
			Mat mean = new Mat(trainCount, 32 * 32, meanVec.type());
			Core.hconcat(meanVecList, mean);
			Core.divide(mean, new Scalar(trainCount), mean);
			System.out.println("Mean size " + mean.size());

			// normalize datachar
			Core.subtract(dataChar, mean, dataChar);

			// manual pca
			// covariance matrix
			Mat L = new MatOfDouble();
			Core.gemm(dataChar.t(), dataChar, 1.0, Mat.zeros(dataChar.cols(),
					dataChar.cols(), dataChar.type()), 0, L);
			System.out.println("Covariance size " + L.size());
			Mat eigenvalues = new Mat();
			Mat eigenvectors = new Mat();
			Core.eigen(L, true, eigenvalues, eigenvectors);

			// find eigen vaector
			Mat V = new MatOfDouble();
			System.out.println("eigenvectors " + eigenvectors.size()
					+ " eigenvalues " + eigenvalues.size() + " dataChar "
					+ dataChar.size());
			Core.gemm(dataChar, eigenvectors, 1.0,
					Mat.zeros(V.rows(), dataChar.cols(), V.type()), 0, V);
			System.out.println("V " + V.size());
			V = V.t();

			// build feature vector
			Mat tr_vectors = new Mat();
			System.out.println("V " + V.size() + " type " + V.type() + " ch "
					+ V.channels());
			System.out.println("dataChar " + dataChar.size() + " type "
					+ dataChar.type() + " ch " + dataChar.width());
			Core.gemm(V, dataChar, 1.0,
					Mat.zeros(V.rows(), dataChar.cols(), V.type()), 0,
					tr_vectors);
			System.out.println("debug tr " + eigenvectors.dump());
			System.out.println(V.size());

			List<Mat> tr_vectorsList = new Vector<Mat>();
			tr_vectorsList.add(tr_vectors);
			tr_vectorsList.add(V);
			Mat feature = new MatOfDouble();
			Core.hconcat(tr_vectorsList, feature);
			feature.convertTo(feature, CvType.CV_64F);
			System.out.println("log concat tr_vectors " + tr_vectors.size());
			System.out.println("log concat V " + V.size());

			fileReader = new FileReader(directory + "label.txt");
			bufferedReader = new BufferedReader(fileReader);
			lines = new ArrayList<String>();
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
			String[] lebelName = lines.toArray(new String[lines.size()]);

			// build lebel matrix for lebel the result
			Mat response = new MatOfDouble();
			response = new Mat(trainCount, 1, CvType.CV_32FC1);
			double[] label = new double[trainCount];
			System.out.println("res size " + response.size());
			for (int i = 0; i < lebelName.length; i++) {
				label[i] = Double.parseDouble(lebelName[i]); // must change to
																// class number
			}
			response.put(0, 0, label);

			// create feature bin file
			Model model = new Model(V, meanVec, eigenvectors, response,
					trainCount);
			model.save("model.bin");

			// END OF TRAINING

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Mat result = new Mat();
		return result;
	}

	public static void testClassifier() {
		//
		// THIS IS CLASSIFY PHASE
		//
		// classify

		Model model = new Model("model.bin");
		System.out.println("Model " + model.getTrainCount());

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
		System.out.println("sample mat "+sample.size());
		System.out.println("model.getV() mat "+model.getV().size());

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
		Mat V = new Mat(), eigenvectors = new Mat(), response = new Mat();
		model.getV().convertTo(V, CvType.CV_32FC1);
		model.getEigenvectors().convertTo(eigenvectors, CvType.CV_32FC1);
		Core.gemm(V, sample, 1.0, Mat.zeros(V.rows(), sample.cols(), V.type()),
				0, testDataEigen);
		testDataEigen = testDataEigen.t();
		testDataEigen.convertTo(testDataEigen, CvType.CV_32FC1);

		// build mat for result from knn size equal test data
		Mat result = new Mat(8, 1, CvType.CV_32FC1);
		result.convertTo(result, CvType.CV_32FC1);

		// knn classify
		CvKNearest knn = new CvKNearest(eigenvectors, response);
		knn.find_nearest(testDataEigen, 1, result, new Mat(), new Mat());

		// Highgui.imwrite(directory + "feature.jpg", tr_vectors);
		System.out.println("result " + result.dump());

	}

	public static void main(String[] args) {
		dataBuilder(fileName);
		testClassifier();

	}

}
