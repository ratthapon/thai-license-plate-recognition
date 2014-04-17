package input.video;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_ANY;
import static com.googlecode.javacv.cpp.opencv_highgui.cvCreateCameraCapture;
import static com.googlecode.javacv.cpp.opencv_highgui.cvQueryFrame;
import static com.googlecode.javacv.cpp.opencv_highgui.cvReleaseCapture;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvShowImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvWaitKey;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2HSV;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;

public class JavaCV4 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*
		 * CvFont font = new CvFont(); int fontFace = CV_FONT_HERSHEY_SIMPLEX;
		 * double fontScale = 0.5; int thickness = 1;
		 * cvInitFont(font,fontFace,fontScale,fontScale,0,thickness,CV_AA);
		 */
		IplImage img1, imghsv, imgbin, imgcap;

		imghsv = cvCreateImage(cvSize(640, 480), 8, 3);
		imgbin = cvCreateImage(cvSize(640, 480), 8, 1);

		CvCapture capture1 = cvCreateCameraCapture(CV_CAP_ANY);

		int i = 1;

		while (i == 1) {

			img1 = cvQueryFrame(capture1);

			if (img1 == null)
				break;

			cvCvtColor(img1, imghsv, CV_BGR2HSV);
			// CvScalar minc = cvScalar(95,150,75,0), maxc =
			// cvScalar(145,255,255,0);
			// cvInRangeS(imghsv,minc,maxc,imgbin);

			cvShowImage("color", img1);
			// cvShowImage("Binary",imgbin);
			// cvPutText(img1,"...Press C to Capture , Q to Exit...",cvPoint(20,20),font,CvScalar.GRAY);
			char c = (char) cvWaitKey(15);
			if (c == 'q')
				break;
			if (c == 'c') {
				final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
				try {
					grabber.start();
					IplImage cap = grabber.grab();
					if (cap != null) {
						cvSaveImage("cap.jpg", cap);
						cvShowImage("cApTuRe", cap);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		cvReleaseImage(imghsv);
		// cvReleaseImage(imgbin);
		cvReleaseCapture(capture1);
	}

}
