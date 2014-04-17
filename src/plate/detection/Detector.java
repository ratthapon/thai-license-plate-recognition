package plate.detection;


import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import plate.text.segmentation.TextSegment;

public class Detector {
	public static List<Mat> getCharImageList(Mat plateImage){
		// TODO 
		System.loadLibrary("opencv_java248");
		List<Mat> charImageList = new ArrayList<>();
		charImageList = dummyGetCharImageList(plateImage);
		return charImageList;
	};
	
	static List<Mat> dummyGetCharImageList(Mat plateImage){
		System.loadLibrary("opencv_java248");
		List<Mat> charImageList = new ArrayList<>();
		plateImage = Highgui.imread("sourcedata/LP2.jpg");
		Imgproc.resize(plateImage, plateImage, new Size(600, 270));
		charImageList = TextSegment.segmentText(plateImage);
		return charImageList;
	};

}
