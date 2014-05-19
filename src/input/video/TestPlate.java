package input.video;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class TestPlate extends Thread {

	public void run() {
		Mat a = new Mat();
		BufferedImage temp;
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			a = Highgui.imread("platelocalize/c_BAND_CLIPPED.jpg");
			if(!a.empty()){
				temp = Panel.matToBufferedImage(a);
				MyWindowsForm.label_1.setIcon(new ImageIcon(temp));
			}
			
		}
	}
}
