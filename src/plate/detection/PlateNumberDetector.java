package plate.detection;


import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import plate.text.segmentation.TextSegment;

public class PlateNumberDetector {
	public static List<Mat> getCharImageList(Mat plateImage){
		System.loadLibrary("opencv_java248");
		List<Mat> charImageList = new ArrayList<>();
		Imgproc.resize(plateImage, plateImage, new Size(600, 270));
		charImageList = TextSegment.segmentText(plateImage);
		Imgproc.resize(plateImage, plateImage, new Size(200, 90));
		return charImageList;
	};

}
