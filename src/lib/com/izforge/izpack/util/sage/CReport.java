package com.izforge.izpack.util.sage;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * @author ogattaz
 *
 */
public class CReport {

	public static final char CHAR_NEWLINE = '\n';
	public static final char CHAR_PIPE = '|';
	public static final char CHAR_SPACE = ' ';
	public static final char CHAR_SPACE_INSECABLE = '\u00A0';
	public static final char CHAR_STEP = '+';
	public static final char CHAR_TITRE = '#';

	public static final boolean CONSOLE_LOG_OFF = false;
	public static final boolean CONSOLE_LOG_ON = true;

	private static final String EMPTY = "";

	private static final String EOF = toInsecable(CHAR_TITRE + " eof");

	private static final String FOLLOWING = ">>" + CHAR_SPACE_INSECABLE;

	private final static String PATTERN_ISO_8601 = "yyyy-MM-dd HH:mm:ss.SSS";

	private static final String REGEX_SPLIT_LINES = "\n";

	private static final String REPORT_NAME = "report";

	private static final int REPORT_WITDH = 160;

	private final static SimpleDateFormat sIso8601Formatter = new SimpleDateFormat(
			PATTERN_ISO_8601);

	public static final String SUFFIX_EMPTY = "";

	public static final String SUFFIX_ON_ERROR = "_onError";

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

		String wText = truncate(aText, aLen - 4);

		if (wText != null && !wText.isEmpty()) {
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

	private final StringBuilder pBuffer = new StringBuilder();

	private boolean pConsoleLogOn = false;

	private final String pCreatedAt;

	private final String pName;

	private final int pWidth;

	/**
	 * 
	 */
	public CReport(final String aName) {
		this(aName, REPORT_WITDH);
	}

	/**
	 * @param aWidth
	 *            the width of the report
	 */
	public CReport(final String aName, final int aWidth) {
		super();
		pName = nameValidation(aName);
		pWidth = widthValidation(aWidth);
		pCreatedAt = getNowForName();
	}

	/**
	 * @param aLine
	 * @return
	 */
	public String append(String aText) {

		String wNow = getNowIso8601();

		for (String wLine : aText.split(REGEX_SPLIT_LINES)) {
			String wRest = null;
			while (true) {
				if (wLine.length() > getWidth()) {
					wRest = FOLLOWING + wLine.substring(getWidth());
					wLine = wLine.substring(0, getWidth());
				}

				pBuffer.append(CHAR_NEWLINE);
				pBuffer.append(wNow);
				pBuffer.append(CHAR_PIPE);
				pBuffer.append(wLine);

				if (pConsoleLogOn) {
					consoleLog(wLine);
				}

				if (wRest != null) {
					wLine = wRest;
					wRest = null;
				} else {
					break;
				}
			}
		}

		return aText;
	}

	/**
	 * @param aFormat
	 * @param aArgs
	 * @return
	 */
	public String append(String aFormat, final Object... aArgs) {
		return append(String.format(aFormat, aArgs));
	}

	/**
	 * @param aBannerChar
	 * @param aFormat
	 * @param aArgs
	 * @return
	 */
	private String appendBanner(final char aBannerChar, final String aFormat,
			final Object... aArgs) {

		String wTitle = String.format(aFormat, aArgs);

		String wLineFull = generateLineFull(aBannerChar, getWidth());
		appendEmpty();
		append(wLineFull);
		append(generateLineBeginEnd(aBannerChar, getWidth()));
		append(generateLineBeginEnd(aBannerChar, getWidth(), wTitle));
		append(generateLineBeginEnd(aBannerChar, getWidth()));
		append(wLineFull);
		appendEmpty();
		return wTitle;
	}

	/**
	 * @return
	 */
	public String appendEmpty() {

		return append(EMPTY);
	}

	/**
	 * @return
	 */
	public String appendEof() {

		return append(CHAR_NEWLINE + EOF);
	}

	/**
	 * @param e
	 * @return the logged text
	 */
	public String appendError(Throwable e) {
		return append(CHAR_NEWLINE + CLoggerUtils.dumpStackTrace(e));
	}

	/**
	 * @param aChar
	 * @return the logged text
	 */
	public String appendLineFull(final char aChar) {
		return append(generateLineFull(aChar, getWidth()));
	}

	/**
	 * @param aFormat
	 * @param aArgs
	 * @return the logged text
	 */
	public String appendStep(String aFormat, final Object... aArgs) {

		return appendBanner(CHAR_STEP, aFormat, aArgs);
	}

	/**
	 * @param aFormat
	 * @param aArgs
	 * @return the logged text
	 */
	public String appendTitle(String aFormat, final Object... aArgs) {

		return appendBanner(CHAR_TITRE, aFormat, aArgs);

	}

	private void consoleLog(final String aLogLine) {
		CLoggerUtils.logInfo(aLogLine);
	}

	/**
	 * @return
	 */
	public String getFileName() {
		return getFileName(SUFFIX_EMPTY);
	}

	/**
	 * @param aSuffix
	 * @return
	 */
	public String getFileName(final String aSuffix) {

		return String.format("%s_%s%s.txt", pCreatedAt, getName(), aSuffix);

	}

	/**
	 * @return
	 */
	public String[] getLines() {
		return toString().split(REGEX_SPLIT_LINES);
	}

	/**
	 * @return
	 */
	public String getName() {
		return pName;
	}

	/**
	 * @return
	 */
	private String getNowForName() {
		return getNowIso8601().replace(' ', '_');
	}

	/**
	 * @return
	 */
	private String getNowIso8601() {
		return sIso8601Formatter.format(new Date(System.currentTimeMillis()));
	}

	/**
	 * @return the width of the report
	 */
	public int getWidth() {
		return pWidth;
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return pBuffer.length() == 0;
	}

	/**
	 * @param aName
	 * @return
	 */
	private String nameValidation(final String aName) {
		return (aName == null || aName.isEmpty()) ? REPORT_NAME : aName;
	}

	/**
	 * @param aConsoleLogOn
	 */
	public void setConsoleLogOn(final boolean aConsoleLogOn) {
		pConsoleLogOn = aConsoleLogOn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return pBuffer.toString();
	}

	/**
	 * @return
	 */
	public String toStringWithoutNow() {

		String wPayload = toString();

		// remove the first empty line
		if (wPayload.charAt(0) == CHAR_NEWLINE) {
			wPayload = wPayload.substring(1);
		}
		// to keep the content of each line begining after the pipe character
		int wBeginLinePos = wPayload.indexOf(CHAR_PIPE) + 1;

		StringBuilder wSB = new StringBuilder();
		for (String wLine : wPayload.split(REGEX_SPLIT_LINES)) {

			String wKeptPart = wLine.substring(wBeginLinePos);
			wSB.append(CHAR_NEWLINE).append(wKeptPart);
		}
		return wSB.toString();
	}

	/**
	 * @param aWidth
	 * @return the given width if 80<=aWidth<=512, else return the constant
	 *         REPORT_WITDH (160)
	 */
	private int widthValidation(final int aWidth) {
		return (aWidth < 80 || aWidth > 512) ? REPORT_WITDH : aWidth;
	}

}
