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

public class Train {
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
			Mat meanVec = MatOfDouble.zeros(32 * 32, 1, CvType.CV_64F);
			List<Mat> meanVecList = new Vector<Mat>();
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
			Core.divide(meanVec, new Scalar(trainCount), meanVec);
			for (int i = 0; i < filename.length; i++) {
				meanVecList.add(meanVec.clone());
			}
			Mat mean = new Mat(trainCount, 32 * 32, meanVec.type());
			Core.hconcat(meanVecList, mean);
			Core.divide(mean, new Scalar(trainCount), mean);
			System.out.println("Mean size " + mean.size());

			// normalize datachar
			Core.subtract(dataChar, mean, dataChar);

			Mat response = new MatOfDouble();
			Mat eigenvectors = new MatOfDouble();
			int maxComponent = 20;
			Core.PCACompute(dataChar, mean.submat(0, 1, 0, mean.width()),
					eigenvectors, maxComponent);

			eigenvectors.convertTo(eigenvectors, CvType.CV_32FC1);
			System.out.println("eigenvectors " + eigenvectors.dump());
			System.out.println("response " + response.size());

			// classify
			Mat sample = new MatOfDouble();
			Vector<Mat> sampleVector = new Vector<>();
			for (int i = 7; i <= 8; i++) {
				Mat img = Highgui.imread("sourcedata/CHAR/c"+i+".bmp");
				Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
				Imgproc.resize(img, img, new Size(32, 32));
				img = img.reshape(1, 32 * 32);
				System.out.println("Normalize test "+img.size() +" "+eigenvectors.size());
				img.convertTo(img, meanVec.type());
				Core.subtract(img, meanVec, img);
				// find eigen vaector
				img.convertTo(img, eigenvectors.type());
				System.out.println("img "+img.size());
				sampleVector.add(img.clone());
			}
			Core.hconcat(sampleVector, sample);
			sample.convertTo(sample, CvType.CV_32FC1);
			

			
			double[] label = new double[eigenvectors.rows()];
			for (int i = 0; i < label.length; i++) {
				label[i] = i+1;
			}
			response.put(0, 0, label);
			response = new Mat(eigenvectors.rows(), 1, CvType.CV_32FC1);
			CvKNearest knn = new CvKNearest(eigenvectors, response);
			
			Mat testDataEigen = new MatOfDouble();
			Core.PCACompute(sample, mean.submat(0, 1, 0, sample.width()),
					testDataEigen, maxComponent);
			
			//Core.PCACompute(sample, mean.submat(0, 1, 0, sample.width()),
			//		testDataEigen, maxComponent);
			testDataEigen.convertTo(testDataEigen, CvType.CV_32FC1);
			
			Mat result = new MatOfDouble();
			result.convertTo(result, CvType.CV_32FC1);
			System.out.println("test eigen "+eigenvectors.dump());
			System.out.println("result "+result.dump());
			System.out.println("eig "+testDataEigen.size()+" tr_vectors "+eigenvectors.size());
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
