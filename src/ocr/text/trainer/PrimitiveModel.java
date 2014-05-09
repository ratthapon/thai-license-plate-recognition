package ocr.text.trainer;

import java.io.Serializable;

import org.opencv.core.Size;

class PrimitiveModel implements Serializable {
	private static final long serialVersionUID = 1L;
	public double[] V;
	public int[] sizeV;
	public double[] meanVec;
	public int[] sizeMeanVec;
	public double[] eigenvectors;
	public int[] sizeEigenvectors;
	public double[] response;
	public int[] sizeResponse;
	public int trainCount;

	public PrimitiveModel(double[] v, int[] sizeV, double[] meanVec,
			int[] sizeMeanVec, double[] eigenvectors, int[] sizeEigenvectors,
			double[] response, int[] sizeResponse, int trainCount) {
		super();
		V = v;
		this.sizeV = sizeV;
		this.meanVec = meanVec;
		this.sizeMeanVec = sizeMeanVec;
		this.eigenvectors = eigenvectors;
		this.sizeEigenvectors = sizeEigenvectors;
		this.response = response;
		this.sizeResponse = sizeResponse;
		this.trainCount = trainCount;
	}

}