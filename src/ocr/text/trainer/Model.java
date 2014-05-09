package ocr.text.trainer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Model {

	private Mat V;
	private Mat meanVec;
	private Mat eigenvectors;
	private Mat response;
	private int trainCount = 0;

	public Mat getV() {
		return V;
	}

	public Mat getMeanVec() {
		return meanVec;
	}

	public Mat getEigenvectors() {
		return eigenvectors;
	}

	public Mat getResponse() {
		return response;
	}

	public int getTrainCount() {
		return trainCount;
	}

	public void save(String fileName) {
		System.loadLibrary("opencv_java248");
		try {
			// create feature bin file
			FileOutputStream fos;
			ObjectOutputStream oos;

			Mat outV = new Mat();
			Mat outMeanVec = new Mat();
			Mat outEigenvectors = new Mat();
			Mat outResponse = new Mat();

			V.convertTo(outV, CvType.CV_64FC1);
			meanVec.convertTo(outMeanVec, CvType.CV_64FC1);
			eigenvectors.convertTo(outEigenvectors, CvType.CV_64FC1);
			response.convertTo(outResponse, CvType.CV_64FC1);
			double[] VData = new double[(int) (outV.size().area())];
			outV.get(0, 0, VData);
			double[] meanVecData = new double[(int) (outMeanVec.size().area())];
			outMeanVec.get(0, 0, meanVecData);
			double[] eigenvectorsData = new double[(int) (outEigenvectors
					.size().area())];
			outEigenvectors.get(0, 0, eigenvectorsData);
			double[] responseData = new double[(int) (outResponse.size().area())];
			outResponse.get(0, 0, responseData);

			PrimitiveModel modelStruct = new PrimitiveModel(VData, outV.cols(),
					outV.rows(), meanVecData, outMeanVec.cols(),
					outMeanVec.rows(), eigenvectorsData,
					outEigenvectors.cols(), outEigenvectors.rows(),
					responseData, outResponse.cols(), outResponse.rows(),
					trainCount);

			fos = new FileOutputStream(fileName);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(modelStruct);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Model(Mat v, Mat meanVec, Mat eigenvectors, Mat response,
			int trainCount) {
		super();
		System.loadLibrary("opencv_java248");
		V = v;
		this.meanVec = meanVec;
		this.eigenvectors = eigenvectors;
		this.response = response;
		this.trainCount = trainCount;
	}
	
	public Model(String fileName) {
		System.loadLibrary("opencv_java248");
		FileInputStream fis;
		ObjectInputStream ois;
		PrimitiveModel modelStruct;
		try {
			fis = new FileInputStream(fileName);
			ois = new ObjectInputStream(fis);
			modelStruct = (PrimitiveModel) ois.readObject();
			
			V = new Mat(modelStruct.vrow, modelStruct.vcol, CvType.CV_64FC1);
			V.put(0, 0, modelStruct.V);
			V.convertTo(V, CvType.CV_32FC1);
			this.meanVec = new Mat(modelStruct.mrow, modelStruct.mcol,
					CvType.CV_64FC1);
			this.meanVec.put(0, 0, modelStruct.meanVec);
			this.meanVec.convertTo(this.meanVec, CvType.CV_32FC1);
			this.eigenvectors = new Mat(modelStruct.erow, modelStruct.ecol,
					CvType.CV_64FC1);
			this.eigenvectors.put(0, 0, modelStruct.eigenvectors);
			this.eigenvectors.convertTo(this.eigenvectors, CvType.CV_32FC1);
			this.response = new Mat(modelStruct.rrow, modelStruct.rcol,
					CvType.CV_64FC1);
			this.response.put(0, 0, modelStruct.response);
			this.response.convertTo(this.response, CvType.CV_32FC1);
			this.trainCount = modelStruct.trainCount;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
