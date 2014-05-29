Thai License Plate Recognition Project
======
Introduction
------------
Thai License Plate Recognition คือโปรแกรมสำหรับรู้จำแผ่นป้ายทะเบียนภาษาไทยจากรูปภาพหรือภาพเคลื่อนไหว ในโครงการฝึกงาน True Academy Project

โปรแกรมนี้ใช้ library ในการทำ Image processing ด้วย [OpenCV](http://opencv.org/downloads.html) version 2.4.8

Runnable JAR
------------
 - [ThaiLPR.rar](http://www.mediafire.com/download/fxs31bh8ps7976s/ThaiLPR.rar) x64 (สำหรับ x86 ให้ copy file opencv/x86/opencv_java248.dll วางไว้ที่ root folder แทน)

Download
--------
 - [OpenCV](http://opencv.org/downloads.html) version 2.4.8[วิธีติดตั้ง](http://docs.opencv.org/doc/tutorials/introduction/java_eclipse/java_eclipse.html#java-eclipse) [OpenCV documentation](http://docs.opencv.org/java/)
 - [ฐานข้อมูลตัวอักษรสำหรับโปรแกรมนี้](http://www.kmitl.ac.th/~s4070081/400dpi_NB_TN_all.bin)

Sample Code
-----------

1.train ฐานข้อมูลตัวอักษรสำหรับรู้จำ
```java
package samplecode;

import ocr.text.trainer.Trainer;

public class SampleTrainer {

	public static void main(String[] args) {
		Trainer.train("trainFileNameList.txt", "trainLabelList.txt", "outputModel.bin");

	}

}
```

2.การรู้จำตัวอักษร
```java
package samplecode;

import java.util.ArrayList;
import java.util.List;

import ocr.text.recognition.OCR;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class SampleOCR {

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		List<Mat> charImageMatList = new ArrayList<>();
		Mat charImageMat = Highgui.imread("testChar.bmp");
		charImageMatList.add(charImageMat);
		OCR.setModelPath("testmodel.bin");
		int[] asciiCodes ;
		asciiCodes = OCR.recognizeCharImage(charImageMatList);
		System.out.print("Result ascii code ");
		for (int i = 0; i < asciiCodes.length; i++) {
			System.out.print(asciiCodes[i]+",");
		}
		
	}

}
```

3.การอ่านป้ายทะเบียน
```java
package samplecode;

import java.util.ArrayList;
import java.util.List;

import ocr.text.recognition.OCR;
import ocr.text.segmentation.TextSegment;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import plate.detection.Band;
import plate.detection.Car;
import plate.detection.Plate;

public class SampleDetectPlate {

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		Car car = new Car(Highgui.imread("testCarImage.jpg"));

		// method 1
		List<String> resultList = car.readPlate();
		for (String string : resultList) {
			System.out.println("Result " + string); // result is up to model
		}

		// method 2
		List<Band> bands = new ArrayList<Band>();
		bands = car.clipBands(1); // up to row you want to analyze
		List<Plate> plates = new ArrayList<Plate>();
		for (Band band : bands) {
			plates.addAll(band.clipPlates2(car.toMat()));
		}
		for (Plate plate : plates) {
			List<Mat> charImageMatList = TextSegment
					.getListMatOfCharImage(plate.toMat());
			int[] resultAsciiCode = OCR.recognizeCharImage(charImageMatList);
			System.out.println("\n\rResult ");
			for (int i = 0; i < resultAsciiCode.length; i++) {
				int c = resultAsciiCode[i];
				if (resultAsciiCode[i] >= 161) {
					c = 0x0e00 + (resultAsciiCode[i] - 160); // Thai char
				}
				System.out.print(String.format("%c", c));
			}
		}

		// method 3
		plates = car.clipPlates(1);
		for (Plate plate : plates) {
			List<Mat> charImageMatList = TextSegment
					.getListMatOfCharImage(plate.toMat());
			if (charImageMatList == null || charImageMatList.size() <= 0) {
				break;
			}
			int[] resultAsciiCode = OCR.recognizeCharImage(charImageMatList);
			System.out.println("\n\rResult ");
			for (int i = 0; i < resultAsciiCode.length; i++) {
				int c = resultAsciiCode[i];
				if (resultAsciiCode[i] >= 161) {
					c = 0x0e00 + (resultAsciiCode[i] - 160); // Thai char
				}
				System.out.print(String.format("%c", c));
			}
		}
	}

}

```
