package com.sage.izpack;

public abstract class StringUtil {

	public static String asciiToHex(String asciiValue) {
		char[] chars = asciiValue.toCharArray();
		StringBuffer hex = new StringBuffer();
		for (char element : chars) {
			hex.append(Integer.toHexString(element));
		}
		return hex.toString();
	}
}
