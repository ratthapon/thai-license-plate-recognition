package samplecode;

import ocr.text.trainer.Trainer;

public class SampleTrainer {

	public static void main(String[] args) {
		Trainer.train("trainFileNameList.txt", "trainLabelList.txt", "outputModel.bin");

	}

}
