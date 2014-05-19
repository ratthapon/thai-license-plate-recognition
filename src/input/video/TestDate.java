package input.video;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestDate extends Thread{
	public void run(){
		while(true){
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			Date date = new Date();
			MyWindowsForm.lblDate.setText("Date : "+dateFormat.format(date));
		}
	}
}
