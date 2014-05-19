package input.video;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

public class Test extends Thread {

	public void run() {
		Mat webcam_image = new Mat();
		BufferedImage temp = null;
		VideoCapture capture = new VideoCapture(0);
		while (true) {
			if (capture.isOpened()) {
				capture.read(webcam_image);
				if (!webcam_image.empty()) {
					temp = Panel.matToBufferedImage(webcam_image);
					MyWindowsForm.showImage.setIcon(new ImageIcon(temp));
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

}
