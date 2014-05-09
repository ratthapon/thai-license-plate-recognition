package ocr.text.trainer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.stream.DoubleStream;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

public class Model {
	/**
	 * 
	 */

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

			V.convertTo(outV, CvType.CV_64F);
			meanVec.convertTo(outMeanVec, CvType.CV_64F);
			eigenvectors.convertTo(outEigenvectors, CvType.CV_64F);
			response.convertTo(outResponse, CvType.CV_64F);
			double[] VData = new double[(int) (outV.size().area())];
			outV.get(0, 0, VData);
			double[] meanVecData = new double[(int) (outMeanVec.size().area())];
			outMeanVec.get(0, 0, meanVecData);
			double[] eigenvectorsData = new double[(int) (outEigenvectors
					.size().area())];
			outEigenvectors.get(0, 0, eigenvectorsData);
			double[] responseData = new double[(int) (outResponse.size().area())];
			outResponse.get(0, 0, responseData);

			PrimitiveModel modelStruct = new PrimitiveModel(VData,new int[2], meanVecData,new int[2],
					eigenvectorsData,new int[2], responseData,new int[2], trainCount);

			fos = new FileOutputStream(fileName);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(modelStruct);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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

	public Model(double[] v, double[] meanVec, double[] eigenvectors,
			double[] response, int trainCount) {
		super();
		System.loadLibrary("opencv_java248");
		V = new Mat();
		V.put(0, 0, v);
		this.meanVec = new Mat();
		this.meanVec.put(0, 0, meanVec);
		this.eigenvectors = new Mat();
		this.eigenvectors.put(0, 0, eigenvectors);
		this.response = new Mat();
		this.response.put(0, 0, response);
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
			System.out.println("Primitive "+modelStruct.trainCount);
			V = new MatOfDouble(modelStruct.V);
			V.convertTo(V, CvType.CV_64FC1);
			V.put(0, 0, modelStruct.V);
			System.out.println("modelStruct.V "+modelStruct.V.length);
			System.out.println(V.dump());
			this.meanVec = new Mat();
			this.meanVec.put(0, 0, modelStruct.meanVec);
			this.eigenvectors = new Mat();
			this.eigenvectors.put(0, 0, modelStruct.eigenvectors);
			this.response = new Mat();
			this.response.put(0, 0, modelStruct.response);
			this.trainCount = modelStruct.trainCount;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
