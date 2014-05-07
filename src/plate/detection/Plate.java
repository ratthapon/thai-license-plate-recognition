package plate.detection;

import java.util.Comparator;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class Plate {
	private Mat plateMat = null;
	private Rect boundingRect = null;
	Rect rect = null;
	int top;
	int bottom;
	int left;
	int right;
	int width;
	int height;
	double density;

	Plate(Mat image, Rect rect, double density) {
		this.plateMat = new Mat(image.clone(), rect);
		this.top = rect.y;
		this.bottom = rect.y + rect.height;
		this.width = rect.width;
		this.height = rect.height;
		this.boundingRect = rect.clone();
		Mat grayPlate = plateMat.clone();
		Imgproc.cvtColor(grayPlate, grayPlate, Imgproc.COLOR_RGB2GRAY);
		this.density = density;
		//System.out.println("Created new plate instance.");
	}

	public Mat toMat() {
		return plateMat;
	}

	public Rect getBoundingRect() {
		return boundingRect;
	}

	// for sort character in plate
	public static Comparator<Plate> PLATE_HUERISTIC_CAMPATATOR = new Comparator<Plate>() {
		public int compare(Plate c1, Plate c2) {
			int precision = 1000000;
			double c1_alpha1 = 0.4 * c1.width;
			double c2_alpha2 = 0.4 * c2.width;

			double o1_alpha3 = 0.4 * c1.density;
			double o2_alpha3 = 0.4 * c2.density;

			double o1_alpha4 = 1 - (c1.width / c1.height - 7);
			double o2_alpha4 = 1 - (c2.width / c2.height - 7);

			int c1_value = (int) (c1_alpha1 + o1_alpha3 + o1_alpha4)
					* precision;
			int c2_value = (int) (c2_alpha2 + o2_alpha3 + o2_alpha4)
					* precision;
			// System.out.println(" plate hueristic " + " " + c1_alpha1 + " "
			// + " " + o1_alpha3 + " " + c2_alpha2 + " "
			// + " " + o2_alpha3);
			return c2_value - c1_value;
		}
	};
}
