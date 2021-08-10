package com.izforge.izpack.util;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

import com.izforge.izpack.util.sage.CDevToolsInfosFinder;
import com.izforge.izpack.util.sage.CLoggerUtils;
import com.izforge.izpack.util.sage.CReport;
import com.izforge.izpack.util.sage.CReportWritter;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * @author ogattaz
 *
 */
public class CTestReport {

	private static final String LOREM_1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

	private static final String LOREM_2 = "Proin cursus cursus consequat. Etiam egestas tortor at sapien pulvinar sodales. Sed tempor massa eget magna posuere euismod. Curabitur hendrerit augue eu sapien egestas, at molestie risus elementum. Nulla ullamcorper magna id posuere tempus. In sit amet tempor augue. Mauris consectetur condimentum tincidunt. Vestibulum egestas felis vel vestibulum dignissim. In hac habitasse platea dictumst. Morbi suscipit urna nec nisi pellentesque gravida. Pellentesque euismod nibh id elit ornare facilisis.";

	private static final String LOREM_3 = "Cras at ante et risus dignissim eleifend sit amet placerat mi. Fusce maximus est diam, iaculis maximus massa auctor vitae. Donec massa lectus, fringilla et dapibus eu, convallis nec nibh. Nunc dictum, libero quis ullamcorper egestas, augue justo facilisis leo, nec ultricies purus ligula at erat. Mauris leo nulla, sagittis quis arcu in, elementum congue massa. Nullam malesuada commodo tellus at pellentesque. Duis sed urna sit amet ex congue gravida. Pellentesque pretium ex vel rhoncus finibus. Duis blandit nunc sed urna congue, in rhoncus nulla pellentesque. Vivamus ut tincidunt odio, eu vestibulum lorem. Sed sit amet rhoncus orci.";

	private static final String LOREM_FULL = LOREM_1 + ' ' + LOREM_2 + ' '
			+ LOREM_3;

	private static final String[] WORDS = LOREM_FULL.replace(",", " ")
			.replace(".", " ").split(" ");

	/**
	 * @param aReportWidth
	 * @throws Exception
	 */
	public CReport doTtestReport(final String aName, final int aWidth,
			final boolean aConsoleLogOn) throws Exception {

		CReport wReport = new CReport(aName, aWidth);

		wReport.setConsoleLogOn(aConsoleLogOn);

		wReport.appendTitle(generateText());

		wReport.appendEmpty();

		wReport.append(generateText());

		wReport.appendEmpty();

		Thread.sleep(200);

		wReport.appendStep(generateText());

		wReport.append(LOREM_1.replace(". ", ".\n"));
		wReport.appendLineFull('-');
		wReport.append(LOREM_2.replace(". ", ".\n"));
		wReport.appendLineFull('*');
		wReport.append(LOREM_3.replace(". ", ".\n"));
		wReport.appendLineFull('.');

		wReport.appendError(new Exception(generateText()));

		wReport.appendStep(generateText());

		wReport.appendEof();

		return wReport;
	}

	/**
	 * @return a according the generated long is greater than 500 ;
	 */
	private Boolean generateBoolean() {
		return generateLong() > 500;
	}

	/**
	 * @return a double between 0 and 1000;
	 */
	private Double generateDouble() {
		return new Double(Math.random() * 1000);
	}

	/**
	 * @return a long between 0 and 1000;
	 */
	private Long generateLong() {
		return generateDouble().longValue();
	}

	/**
	 * @return
	 */
	private String generateString() {

		int wStringIdx = new Double(Math.random() * WORDS.length).intValue();

		String wString = WORDS[wStringIdx];
		if (wString.length() < 6) {
			wString = generateString();
		}
		return wString;
	}

	/**
	 * @return
	 */
	private String generateText() {

		return String.format(
				"long=[%4d] float=[%4.3f] bolean=[%-5s] string=[%s] ",
				//
				generateLong(),
				//
				generateDouble(),
				//
				generateBoolean(),
				//
				generateString());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testReportA() throws Exception {
		CReport wReport = doTtestReport("testReportA", 200,
				CReport.CONSOLE_LOG_ON);

		System.out.println(wReport.toString());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testReportB() throws Exception {
		CReport wReport = doTtestReport("testReportB", 75,
				CReport.CONSOLE_LOG_ON);

		System.out.println(wReport.toStringWithoutNow());

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testReportC() throws Exception {
		CReport wReport = doTtestReport("testReportC", 0,
				CReport.CONSOLE_LOG_OFF);

		CReportWritter wReportWritter = new CReportWritter(wReport);

		File wOutputFile = wReportWritter
				.write(new File("test", wReport.getFileName("_suffixTest")));

		CLoggerUtils.logInfo("WritedOutputFile=[%s]", wOutputFile);

		CLoggerUtils.logInfo("NbWritedBytes=[%s]",
				Files.size(wOutputFile.toPath()));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testReportD() throws Exception {

		CReport wReport = new CReport("testReportD");

		wReport.setConsoleLogOn(CReport.CONSOLE_LOG_OFF);

		wReport.appendTitle(generateText());

		wReport.appendEmpty();

		CDevToolsInfosFinder wDevToolsInfosFinder = new CDevToolsInfosFinder(
				wReport);

		wDevToolsInfosFinder.execute();

		System.out.println(wReport.toString());

	}

}
