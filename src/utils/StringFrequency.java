package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringFrequency {

	private List<String> stringList = new ArrayList<String>();
	private List<Integer> countList = new ArrayList<Integer>();

	public StringFrequency push(String string) {
		if (stringList.contains(string)) {
			int idx = stringList.indexOf(string);
			int value = countList.get(idx) + 1;
			countList.set(idx, value);
		} else {
			stringList.add(string);
			countList.add(1);
		}
		return this;
	}

	public String getMax() {
		if (stringList.size() <= 0) {
			return "";
		}
		int maxValue = Collections.max(countList);
		int maxIdx = countList.indexOf(maxValue);
		return stringList.get(maxIdx);
	}

	public String getMax(int thresh) {
		if (stringList.size() <= 0) {
			return "";
		}
		int maxValue = Collections.max(countList);
		if (maxValue < thresh) {
			return "";
		}
		int maxIdx = countList.indexOf(maxValue);
		return stringList.get(maxIdx);
	}

}