import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class TestOpenCV {
	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		String filename = "test.png";
		System.out.println(String.format("Writing %s", filename));
		Mat img = Highgui.imread("lena.png");
		Highgui.imwrite(filename, img);
		// test open
		String filename2 = "test.png";
	}
}