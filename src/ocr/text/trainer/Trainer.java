package ocr.text.trainer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
			int trainCount = 0;
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

			// create feature bin file

			double[] data = new double[(int) (feature.size().area())];
			feature.get(0, 0, data);
			FileOutputStream fos = new FileOutputStream("feature.bin");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(data);
			oos.close();

			// FileInputStream fis = new FileInputStream("feature.bin");
			// ObjectInputStream ois = new ObjectInputStream(fis); double[]
			// readData = (double[]) ois.readObject(); ois.close(); Mat feat =
			// new MatOfDouble(); feat.put(0, 0, readData);
			// System.out.println("FEAT AVG " + Core.mean(feature));

			// classify
			Mat response = new MatOfDouble();
			Mat sample = new MatOfDouble();
			Vector<Mat> sampleVector = new Vector<>();
			for (int i = 1; i <= 8; i++) {
				Mat img = Highgui.imread("sourcedata/CHAR/c" + i + ".bmp");
				Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
				Imgproc.resize(img, img, new Size(32, 32));
				img = img.reshape(1, 32 * 32);
				System.out.println("Normalize test " + img.size() + " "
						+ eigenvectors.size());
				img.convertTo(img, meanVec.type());
				sampleVector.add(img.clone());
			}
			Core.hconcat(sampleVector, sample);
			
			// build mean matrix for normalize
			meanVecList = new Vector<Mat>();
			for (int i = 0; i < sample.cols(); i++) {
				meanVecList.add(meanVec.clone());
			}
			mean = new Mat(trainCount, 32 * 32, meanVec.type());
			Core.hconcat(meanVecList, mean);
			Core.divide(mean, new Scalar(trainCount), mean);
			
			// normalize 
			Core.subtract(sample, mean, sample);

			Mat testDataEigen = new Mat();
			response = new Mat(eigenvectors.rows(), 1, CvType.CV_32FC1);
			double[] label = new double[eigenvectors.rows()];
			System.out.println("res size " + response.size());
			for (int i = 0; i < label.length; i++) {
				label[i] = i + 1;
			}
			response.put(0, 0, label);
			V.convertTo(V, CvType.CV_32FC1);
			eigenvectors.convertTo(eigenvectors, CvType.CV_32FC1);
			System.out.println("V " + V.rows());
			System.out.println("label " + label.length);
			System.out.println("response " + response.dump());
			Core.gemm(V, sample, 1.0,
					Mat.zeros(V.rows(), sample.cols(), V.type()), 0,
					testDataEigen);
			testDataEigen = testDataEigen.t();
			testDataEigen.convertTo(testDataEigen, CvType.CV_32FC1);
			System.out.println("testDataEigen " + testDataEigen.size());
			CvKNearest knn = new CvKNearest(eigenvectors, response);

			Mat result = new Mat(8, 1, CvType.CV_32FC1);
			result.convertTo(result, CvType.CV_32FC1);
			System.out.println("testDataEigen " + testDataEigen.dump());
			System.out.println("eigenvectors " + eigenvectors.dump());
			System.out.println("eig " + testDataEigen.size() + " eigenvectors "
					+ eigenvectors.size());
			knn.find_nearest(testDataEigen, 1, result, new Mat(), new Mat());

			// Highgui.imwrite(directory + "feature.jpg", tr_vectors);
			System.out.println("result " + result.dump());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Mat result = new Mat();
		return result;
	}

	public static void main(String[] args) {
		dataBuilder(fileName);

	}

}
