package ocr.text.trainer;

import java.io.Serializable;

import org.opencv.core.Size;

class PrimitiveModel implements Serializable {
	private static final long serialVersionUID = 1L;
	public double[] V;
	public int vcol;
	public int vrow;
	public double[] meanVec;
	public int mcol;
	public int mrow;
	public double[] eigenvectors;
	public int ecol, erow;
	public double[] response;
	public int rcol;
	public int rrow;
	public int trainCount;

	public PrimitiveModel(double[] v, int vcol, int vrow, double[] meanVec,
			int mcol, int mrow, double[] eigenvectors, int ecol, int erow,
			double[] response, int rcol, int rrow, int trainCount) {
		super();
		V = v;
		this.vcol = vcol;
		this.vrow = vrow;
		this.meanVec = meanVec;
		this.mcol = mcol;
		this.mrow = mrow;
		this.eigenvectors = eigenvectors;
		this.ecol = ecol;
		this.erow = erow;
		this.response = response;
		this.rcol = rcol;
		this.rrow = rrow;
		this.trainCount = trainCount;
	}

}