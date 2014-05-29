package input.video;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

public class Test extends Thread {

	public void run() {
		Mat webcam_image = new Mat();
		BufferedImage temp = null;
		VideoCapture capture = new VideoCapture("IMG_2002.mov");
		int width = (int) capture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH);
		int height = (int) capture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT);
		double frameRate = capture.get(5); // frameRate =
									// cap.get(Highgui.CV_CAP_PROP_FPS);
		System.out.println(width+" "+height+" "+frameRate);
		while (true) {
			if (capture.isOpened()) {
				capture.read(webcam_image);
				if (!webcam_image.empty()) {
					temp = Panel.matToBufferedImage(webcam_image);
					MyWindowsForm.label0.setIcon(new ImageIcon(temp));
					Highgui.imwrite("c.jpg", webcam_image);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			// System.out.println("running");
		}

	}

}
