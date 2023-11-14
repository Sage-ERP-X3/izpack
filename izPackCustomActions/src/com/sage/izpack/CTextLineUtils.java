package com.sage.izpack;


/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * @author ogattaz
 *
 */
public class CTextLineUtils {

	public static final char CHAR_SPACE = ' ';
	public static final char CHAR_SPACE_INSECABLE = '\u00A0';

	/**
	 * @param aChar
	 * @param aLen
	 * @return
	 */
	public static String generateLineBeginEnd(final char aChar,
			final int aLen) {

		return aChar + String.valueOf(new char[aLen - 2]).replace((char) 0x00,
				CHAR_SPACE_INSECABLE) + aChar;
	}

	/**
	 * @param aChar
	 * @param aLen
	 * @param aText
	 * @return
	 */
	public static String generateLineBeginEnd(final char aChar, final int aLen,
			final String aText) {

		String wLine = generateLineBeginEnd(aChar, aLen);

		if (aText != null && !aText.isEmpty()) {

			String wText = truncate(aText, aLen - 4);

			int wLen = aLen - (aLen - (2 + wText.length()));

			wLine = wLine.substring(0, 2) + wText + wLine.substring(wLen);
		}
		return toInsecable(wLine);
	}

	/**
	 * @param aChar
	 * @param aLen
	 * @return
	 */
	public static String generateLineFull(final char aChar, final int aLen) {

		return String.valueOf(new char[aLen]).replace((char) 0x00, aChar);
	}

	/**
	 * @param aText
	 * @param aChar
	 * @param aLen
	 * @return
	 */
	public static String generateLineFull(final char aChar, final int aLen,
			final String aText) {

		String wLine = generateLineFull(aChar, aLen);

		if (aText != null && !aText.isEmpty()) {

			String wLabel = truncate(CHAR_SPACE_INSECABLE + toInsecable(aText)
					+ CHAR_SPACE_INSECABLE, aLen - 8);

			int wLen = aLen - (aLen - (4 + wLabel.length()));

			wLine = wLine.substring(0, 4) + wLabel + wLine.substring(wLen);
		}

		return wLine;
	}

	/**
	 * @param aLine
	 * @return
	 */
	public static String toInsecable(final String aLine) {
		return aLine.replace(CHAR_SPACE, CHAR_SPACE_INSECABLE);
	}

	/**
	 * @param aText
	 * @param aLen
	 * @return
	 */
	public static String truncate(final String aText, final int aLen) {
		return (aText != null && aText.length() > aLen)
				? aText.substring(0, aLen)
				: aText;
	}

	/**
	 * never instanciate a Helper
	 */
	private CTextLineUtils() {
		super();
	}

}
