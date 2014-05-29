package input.video;
// Import the basic graphics classes.  
 // The problem here is that we read the image with OpenCV into a Mat object.  
 // But OpenCV for java doesn't have the method "imshow", so, we got to use  
 // java for that (drawImage) that uses Image or BufferedImage.  
 // So, how to go from one the other... Here is the way...  
import java.awt.*;
import java.awt.image.BufferedImage;  

import javax.swing.*;  

import org.opencv.core.Mat;  
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;  
 
public class Panel extends JPanel{  
	private static final long serialVersionUID = 1L;  
	private BufferedImage image;  
	// Create a constructor method  
	public Panel(){  
		super();
	}  
	private BufferedImage getimage(){  
		return image;  
	}  
	void setimage(BufferedImage newimage){  
		image=newimage;  
		return;  
	}  
	/**  
    * Converts/writes a Mat into a BufferedImage.  
    *  
    * @param matrix Mat of type CV_8UC3 or CV_8UC1  
    * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY  
    */  
	public static BufferedImage matToBufferedImage(Mat matrix) {  
		if(!matrix.empty()){
		int cols = matrix.cols();  
		int rows = matrix.rows();  
		int elemSize = (int)matrix.elemSize();  
		byte[] data = new byte[cols * rows * elemSize];  
		int type;  
		matrix.get(0, 0, data);  
		switch (matrix.channels()) {  
		case 1:  
			type = BufferedImage.TYPE_BYTE_GRAY;  
			break;  
		case 3:  
			type = BufferedImage.TYPE_3BYTE_BGR;  
			// bgr to rgb  
			byte b;  
			for(int i=0; i<data.length; i=i+3) {  
				b = data[i];  
				data[i] = data[i+2];  
				data[i+2] = b;  
			}  
			break;  
		default:  
			return null;  
		}  
		BufferedImage image2 = new BufferedImage(cols, rows, type);  
		image2.getRaster().setDataElements(0, 0, cols, rows, data);  
		
		return image2;  
		}
		return null;
	}
	public void paintComponent(Graphics g){  
		BufferedImage temp=getimage();
		g.drawImage(temp,10,10, this);
	}  
	/*public static void main(String arg[]) throws AWTException, InterruptedException{  
	// Load the native library.  
	System.loadLibrary("opencv_java248");    
	JFrame frame = new JFrame("Recording");  
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	frame.setSize(400,400);  
	Panel panel = new Panel();  
	frame.setContentPane(panel);       
	frame.setVisible(true);       
	Mat webcam_image=new Mat();  
	BufferedImage temp;  
	VideoCapture capture =new VideoCapture(0);
	Thread.sleep(1000);
	if( capture.isOpened()){  
		while( true ){  
			capture.read(webcam_image);  
			if( !webcam_image.empty() ){  
				frame.setSize(webcam_image.width()+40,webcam_image.height()+60);
				temp=matToBufferedImage(webcam_image);
				panel.setimage(temp);
				panel.repaint();
				//
				System.out.print("1");
				//capture.grab();
				Highgui.imwrite("cap.jpg",webcam_image);
				Thread.sleep(100);
			}
			else{
				System.out.println(" --(!) No captured frame -- Break!");
				break;
			}
		}
	}
	return;
	}*/
}  