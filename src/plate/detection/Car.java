package plate.detection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import utils.Utils;

public class Car {
	private Mat carImage;

	public Car(String filePath) {
		this(Highgui.imread(filePath));
	}

	public Car(Mat image) {
		System.loadLibrary("opencv_java248");
		carImage = new Mat();
		image.copyTo(carImage);
		int w = image.cols();
		int h = (int) (600.0 / w * image.rows());
		Imgproc.resize(carImage, carImage, new Size(600, h));
		System.out.println("Created new car instance.");
	}

	public List<Band> clipBands(int maxCandidate) {
		System.out.println("Bands clipping");
		List<Band> clipBands = new ArrayList<Band>();
		Mat image = Utils.verticalLine(carImage);
		for (int i = 0; i < maxCandidate; i++) {
			Band band = clipBand(image);
			if (band.height <= 1) {
				break;
			}
			Mat zeros = Mat.zeros(band.height, band.width, image.type());
			zeros.copyTo(image.submat(band.getBoundingRect()));
			clipBands.add(band);
		}

		// sort band by heuristic
		Collections.sort(clipBands, Band.HUERISTIC_COMPARATOR);
		// System.out.println("Clipped " + clipBands.size() + " bands");
		return clipBands;
	}

	public Band clipBand() {
		Mat grayImage = Utils.verticalLine(carImage);
		return clipBand(grayImage);
	}

	private Band clipBand(Mat grayImage) {
		Band band;
		// System.out.println("Project car image in Y axis");
		Vector<Byte> pyMagnitude = Utils.projectMatY(grayImage);
		byte ybm = Collections.max(pyMagnitude);
		int ybmIndex = pyMagnitude.indexOf(ybm);
		double c1 = (Collections.max(pyMagnitude) + Collections
				.min(pyMagnitude)) * 0.55;
		double c2 = (Collections.max(pyMagnitude) + Collections
				.min(pyMagnitude)) * 0.42;
		// yb0 = max(y0<=y<=ybm){y|py(y)<=c*py(ybm)}
		Vector<Byte> yb0InspectSet = new Vector<Byte>(pyMagnitude.subList(0,
				ybmIndex));
		int yb0Index = 0;
		for (int i = 0; i < yb0InspectSet.size(); i++) {
			Byte byte1 = yb0InspectSet.get(i);
			if (byte1 <= c1) {
				yb0Index = i;
			}
		}

		// yb1 = min(ybm<=y<=y1){y|py(y)<=c*py(ybm)}
		Vector<Byte> yb1InspectSet = new Vector<Byte>(pyMagnitude.subList(
				ybmIndex, pyMagnitude.size() - 1));
		int yb1Index = pyMagnitude.size() - 1;
		for (int i = 0; i < yb1InspectSet.size(); i++) {
			Byte byte1 = yb1InspectSet.get(i);
			if (byte1 <= c2) {
				yb1Index = i + ybmIndex;
				break;
			}
		}

		// System.out.println("Calibrate band coordinate");

		int calibrate = (int) ((yb1Index - yb0Index) * 0.1);
		yb0Index -= calibrate;
		yb1Index += calibrate;
		if (yb0Index < 0) {
			yb0Index = 0;
		}
		if (yb1Index > grayImage.rows() - 1) {
			yb1Index = grayImage.rows() - 1;
		}
		band = new Band(this.carImage.clone(), yb0Index, yb1Index, ybm);
		return band;
	}

	public List<Plate> clipPlates(int maxPlate) {
		Band bands = this.clipBand();
		List<Plate> plates = new ArrayList<Plate>();
		plates.addAll(bands.clipPlates(this.toMat()));
		return plates;
	}

	public List<Plate> clipPlatesMaxBandLimit(int maxBand) {
		List<Band> bands = this.clipBands(maxBand);
		List<Plate> plates = new ArrayList<Plate>();
		for (Band band : bands) {
			plates.addAll(band.clipPlates(this.toMat()));
		}
		return plates;
	}

	public List<Plate> clipPlates(int maxBand, int maxPlate) {
		List<Band> bands = this.clipBands(maxBand);
		List<Plate> plates = new ArrayList<Plate>();
		for (Band band : bands) {
			plates.addAll(band.clipPlates(this.toMat(), maxPlate));
		}
		return plates;
	}

	public Mat toMat() {
		return this.carImage.clone();

	}
}
