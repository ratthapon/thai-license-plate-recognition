package input.video;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class TestTime extends Thread{

	public void run() {
		while(true){
			DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			MyWindowsForm.lblTime.setText("Time : "+timeFormat.format(cal.getTime()));
		}
	}
}
	