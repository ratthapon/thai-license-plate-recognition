package ocr.text.trainer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import ocr.text.recognition.OCR;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Trainer {
	private static String fileName = "sourcedata/CHAR/char_name.txt";
	private static String directory = "sourcedata/CHAR/";

	public static void train(String fileListName, String labelListName,String saveModelToFileName) {
		System.loadLibrary("opencv_java248");
		Date timer = new Date();
		long startTime = timer.getTime();
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

			System.out.println("Build data char matrix.");
			// build datachar (Matrix of vector of char image)
			Mat charImageMat;
			Mat dataChar = new MatOfDouble();
			Mat meanVec = MatOfDouble.zeros(32 * 32, 1, CvType.CV_32FC1);
			List<Mat> dataCharVectorList = new Vector<Mat>();

			for (int i = 0; i < filename.length; i++) { //filename.length
				String fileName = filename[i];
				charImageMat = Highgui.imread(fileName);
				Imgproc.cvtColor(charImageMat, charImageMat,
						Imgproc.COLOR_RGB2GRAY);
				Imgproc.resize(charImageMat, charImageMat, new Size(32, 32));
				charImageMat.convertTo(charImageMat, meanVec.type());
				Imgproc.threshold(charImageMat, charImageMat, 0, 255, Imgproc.THRESH_BINARY_INV);
				System.out.println(fileName);
				Core.add(meanVec, charImageMat.reshape(1, 32 * 32), meanVec);
				dataCharVectorList
						.add(charImageMat.reshape(1, 32 * 32).clone());
				trainCount++;
			}
			Core.hconcat(dataCharVectorList, dataChar);

			System.out.println("Calculate mean.");
			// build mean matrix for normalize
			List<Mat> meanVecList = new Vector<Mat>();
			for (int i = 0; i < trainCount; i++) {
				meanVecList.add(meanVec.clone());
			}
			Mat mean = new Mat(trainCount, 32 * 32, meanVec.type());
			Core.hconcat(meanVecList, mean);
			Core.divide(mean, new Scalar(trainCount), mean);

			System.out.println("Normalize char datas.");
			// normalize datachar
			Core.subtract(dataChar, mean, dataChar);

			// manual pca
			System.out.println("Finding eigen vectors.");
			// covariance matrix
			Mat L = new MatOfDouble();
			Core.gemm(dataChar.t(), dataChar, 1.0, Mat.zeros(dataChar.cols(),
					dataChar.cols(), dataChar.type()), 0, L);
			Mat eigenvectors = new Mat();
			Core.PCACompute(L, Mat.zeros(1, dataChar.cols(), dataChar.type()), eigenvectors);

			// find eigen vaector
			Mat V = new MatOfDouble();
			Core.gemm(dataChar, eigenvectors, 1.0,
					Mat.zeros(dataChar.rows(), eigenvectors.cols(), V.type()), 0, V);
			V = V.t();
			
			Mat feature = new MatOfDouble();
			Core.gemm(V, dataChar, 1.0,
					Mat.zeros(V.rows(), dataChar.cols(), V.type()), 0, feature);

			fileReader = new FileReader(labelListName);
			bufferedReader = new BufferedReader(fileReader);
			lines = new ArrayList<String>();
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
			String[] lebelName = lines.toArray(new String[lines.size()]);

			System.out.println("Build label matrix.");
			// build lebel matrix for lebel the result
			Mat response = new MatOfDouble();
			response = new Mat(trainCount, 1, CvType.CV_32FC1);
			double[] label = new double[trainCount];
			for (int i = 0; i < trainCount; i++) {
				label[i] = Double.parseDouble(lebelName[i]); // must change to
																// class number
			}
			response.put(0, 0, label);

			System.out.println("Creating feature.");
			// create feature bin file
			Model model = new Model(V, meanVec, feature.t(), response,
					trainCount);
			model.save(saveModelToFileName);
			System.out.println("Save model at "+saveModelToFileName);

			// END OF TRAINING
			timer = new Date();
			long endTime = timer.getTime();
			System.out.println("Time "+((endTime-startTime)/1000.0));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
