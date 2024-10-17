package com.sage.izpack;

import static com.sage.izpack.CTextLineUtils.CHAR_SPACE_INSECABLE;
import static com.sage.izpack.CTextLineUtils.generateLineBeginEnd;
import static com.sage.izpack.CTextLineUtils.generateLineFull;
import static com.sage.izpack.CTextLineUtils.toInsecable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 *
 * @author ogattaz
 *
 */
public class CReport {

	/**
	 *
	 */
	public enum EKindOfBanner {
		//
		BANNER_STEP,
		//
		BANNER_TITLE;
	}

	public static final char CHAR_NEWLINE = '\n';
	public static final char CHAR_PIPE = '|';
	public static final char CHAR_STEP = '+';
	public static final char CHAR_TITRE = '#';

	public static final boolean CONSOLE_LOG_OFF = false;
	public static final boolean CONSOLE_LOG_ON = true;

	private static final String EMPTY = "";

	private static final String EOF = toInsecable(CHAR_TITRE + " eof");

	private static final String FOLLOWING = ">>" + CHAR_SPACE_INSECABLE;

	private final static String PATTERN_ISO_8601 = "yyyy-MM-dd HH:mm:ss.SSS";

	public static final String REGEX_SPLIT_LINES = "\n";

	private static final String REPORT_NAME = "report";

	private static final int REPORT_WITDH = 160;

	private final static SimpleDateFormat sIso8601Formatter = new SimpleDateFormat(PATTERN_ISO_8601);

	public static final String SUFFIX_EMPTY = "";

	public static final String SUFFIX_ON_ERROR = "_onError";

	private final StringBuilder pBuffer = new StringBuilder();

	private boolean pConsoleLogOn = false;

	private final String pCreatedAt;

	private final String pName;

	private int pStepIdx = 0;

	private int pTitleIdx = 0;

	private final int pWidth;

	/**
	 *
	 */
	public CReport(final String aName) {
		this(aName, REPORT_WITDH);
	}

	/**
	 * @param aWidth the width of the report
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
	private String appendBanner(final EKindOfBanner aKindOfBanner, final char aBannerChar, final String aFormat,
			final Object... aArgs) {

		String wTitle = String.format(aFormat, aArgs);

		if (EKindOfBanner.BANNER_STEP == aKindOfBanner) {
			wTitle = String.format("Step  [%2d] : %s", pStepIdx, wTitle);
		}
		//
		else if (EKindOfBanner.BANNER_TITLE == aKindOfBanner) {
			wTitle = String.format("Title [%2d] : %s", pTitleIdx, wTitle);
		}

		String wLineFull = generateLineFull(aBannerChar, getWidth());
		appendEmpty();
		append(wLineFull);
		// append(generateLineBeginEnd(aBannerChar, getWidth()));
		append(generateLineBeginEnd(aBannerChar, getWidth(), wTitle));
		// append(generateLineBeginEnd(aBannerChar, getWidth()));
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
	 * @param aText
	 * @return
	 */
	public String appendError(final String aText) {
		return append(CHAR_NEWLINE + "ERROR  : " + aText);
	}

	/**
	 * @param aFormat
	 * @param aArgs
	 * @return
	 */
	public String appendError(String aFormat, final Object... aArgs) {
		return appendError(String.format(aFormat, aArgs));

	}

	/**
	 * @param e
	 * @return the logged text
	 */
	public String appendError(Throwable e) {
		return appendError(CLoggerUtils.dumpStackTrace(e));
	}

	/**
	 * @param aChar
	 * @return the logged text
	 */
	public String appendLineFull(final char aChar) {
		return appendLineFull(aChar, getWidth(), null);
	}

	/**
	 * @param aChar
	 * @param aWidth
	 * @return
	 */
	public String appendLineFull(final char aChar, final int aWidth, final String aText) {
		return append(generateLineFull(aChar, aWidth, aText));
	}

	/**
	 * @param aChar
	 * @param aText
	 * @return
	 */
	public String appendLineFull(final char aChar, final String aText) {
		return appendLineFull(aChar, getWidth(), aText);
	}

	/**
	 * @param aLines
	 * @return
	 */
	public int appendNumberedLines(String[] aLines) {

		int wIdx = 0;
		for (String wScriptLine : aLines) {
			wIdx++;
			append("(%3d) %s", wIdx, wScriptLine);
		}

		return wIdx;
	}

	/**
	 * <pre>
	 *
	 * </pre>
	 *
	 * @param aOutPut
	 * @return
	 */
	public String appendOutput(String aOutPut) {

		appendLineFull('-', "Output begin");
		int wNbAppentLines = appendNumberedLines(aOutPut.split(REGEX_SPLIT_LINES));
		appendLineFull('-', "Output end");
		return String.format(" [%d] output lines appent", wNbAppentLines);
	}

	/**
	 * @param aScript
	 * @return
	 */
	public String appendScript(String aScript) {

		appendLineFull('.', "Script begin");
		int wNbAppentLines = appendNumberedLines(aScript.split(REGEX_SPLIT_LINES));
		appendLineFull('.', "Script end");

		return String.format(" [%d] script lines appent", wNbAppentLines);
	}

	/**
	 * @param aFormat
	 * @param aArgs
	 * @return the logged text
	 */
	public String appendStep(String aFormat, final Object... aArgs) {

		pStepIdx++;

		return appendBanner(EKindOfBanner.BANNER_STEP, CHAR_STEP, aFormat, aArgs);
	}

	/**
	 * @param aText
	 * @return
	 */
	public String appendSuccess(final String aText) {
		return append(CHAR_NEWLINE + "SUCCESS: " + aText);
	}

	/**
	 * @param aFormat
	 * @param aArgs
	 * @return
	 */
	public String appendSuccess(String aFormat, final Object... aArgs) {
		return appendSuccess(String.format(aFormat, aArgs));

	}

	/**
	 * @param aFormat
	 * @param aArgs
	 * @return the logged text
	 */
	public String appendTitle(String aFormat, final Object... aArgs) {

		pTitleIdx++;

		return appendBanner(EKindOfBanner.BANNER_TITLE, CHAR_TITRE, aFormat, aArgs);

	}

	private void consoleLog(final String aLogLine) {
		// CLoggerUtils.logInfo(aLogLine);
		System.out.print(aLogLine);
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
