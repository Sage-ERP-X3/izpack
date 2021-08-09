package com.izforge.izpack.util.sage;

import static com.izforge.izpack.util.sage.CTextLineUtils.toInsecable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JTextPane;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.panels.UserInputPanel;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.sage.CWordList.EKindOfFinding;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * 
 * @author ogattaz
 *
 */
public class CompilePrerequisitesControl implements DataValidator {

	private static final int REPORT_WIDTH_200 = 200;

	private static final int RESULT_FONT_SIZE_9 = 9;

	/**
	 * 
	 */
	public CompilePrerequisitesControl() {
		super();
	}

	/**
	 * @param aPanel
	 * @param aContainer
	 */
	private String dumpComponents(final Container aContainer) {
		return dumpComponents(aContainer, new StringBuilder()).toString();
	}

	/**
	 * @param aContainer
	 * @param aSB
	 * @return
	 */
	private StringBuilder dumpComponents(final Container aContainer,
			StringBuilder aSB) {

		for (Component wComponent : aContainer.getComponents()) {

			boolean wIsTextPane = wComponent instanceof JTextPane;

			String wText = "";

			if (wIsTextPane) {
				wText = ((JTextPane) wComponent).getText();
			} else if (wComponent instanceof JLabel) {
				wText = ((JLabel) wComponent).getText();
			}

			aSB.append(toInsecable(String.format(
					"\n- isTextPane=[%-5s] Component=[%-12s][%s]", wIsTextPane,
					//
					wComponent.getClass().getSimpleName(),
					//
					wText)));

			if (wComponent instanceof java.awt.Container) {
				dumpComponents((java.awt.Container) wComponent, aSB);
			}
		}
		return aSB;

	}

	/**
	 * @param aData
	 */
	private String dumpVariables(AutomatedInstallData aData) {
		StringBuilder wDump = new StringBuilder();
		Properties wVariables = aData.getVariables();

		Map<Object, Object> wSortedProperties = new TreeMap<>(wVariables);

		int wIdx = 0;
		for (Entry<Object, Object> wEntry : wSortedProperties.entrySet()) {
			wIdx++;
			wDump.append(toInsecable(String.format("\n- %3d[%-60s]=[%s]", wIdx,
					String.valueOf(wEntry.getKey()),
					String.valueOf(wEntry.getValue()))));

		}
		return wDump.toString();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.izforge.izpack.installer.DataValidator#getDefaultAnswer()
	 */
	@Override
	public boolean getDefaultAnswer() {

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.izforge.izpack.installer.DataValidator#getErrorMessageId()
	 */
	@Override
	public String getErrorMessageId() {

		return "compileprerequisitestesterror";

	}

	/**
	 * <pre>
	 * -  56[SYSTEM_os_arch                                              ]=[x86_64]
	 * -  57[SYSTEM_os_name                                              ]=[Mac OS X]
	 * -  58[SYSTEM_os_version                                           ]=[10.15.7]
	 * </pre>
	 * 
	 * @return
	 */
	private String getOsDetails(AutomatedInstallData aData) {

		return String
				.format("%s - %s - %s", aData.getVariable("SYSTEM_os_name"),
						aData.getVariable("SYSTEM_os_version"),
						aData.getVariable("SYSTEM_os_arch"))
				.replace('\n', ' ');

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.izforge.izpack.installer.DataValidator#getWarningMessageId()
	 */
	@Override
	public String getWarningMessageId() {

		return "compileprerequisitestesterror";
	}

	/**
	 * <pre>
	 * -   9[INSTALLER                ]=[console]
	 * ...
	 * -  67[SYSTEM_sun_java_command  ]=[com.izforge.izpack.installer.Installer -console -language eng]
	 * </pre>
	 * 
	 * @return
	 */
	private boolean isConsoleMode(AutomatedInstallData aData) {

		// condition A
		String wInstaller = aData.getVariable("INSTALLER");
		if (wInstaller != null && wInstaller.contains("console")) {
			return true;
		}

		// condition B
		String wJavaCommand = aData.getVariable("SYSTEM_sun_java_command");
		if (wJavaCommand != null && wJavaCommand.contains("console")) {
			return true;
		}

		return false;
	}

	/**
	 * @param aData
	 * @return
	 */
	private boolean isGuiMode(AutomatedInstallData aData) {
		return !isConsoleMode(aData);
	}

	/**
	 * @param aData
	 * @return
	 * @throws Exception
	 */
	private IzPanel retrieveCurrentPanel(AutomatedInstallData aData)
			throws Exception {

		// test each panel of the installer
		for (IzPanel wPanel : aData.panels) {
			if (wPanel instanceof UserInputPanel) {

				// its the current panel if the validation service of the panel
				// is this instance of CompilePrerequisitesControl
				boolean wIsCurrentPanel = (wPanel
						.getValidationService() == this);

				if (wIsCurrentPanel) {
					return wPanel;
				}
			}
		}
		throw new Exception("Unable to retrieve the current panel");
	}

	/**
	 * @param aPanel
	 * @return the 3th JTextPane of the panel
	 * @throws Exception
	 */
	private JTextPane searchJTextPaneProgress(Container aContainer)
			throws Exception {

		return new GUIComponentSearcher<>(JTextPane.class, aContainer, 3)
				.search();
	}

	/**
	 * @param aPanel
	 * @return the 4th JTextPane of the panel
	 * @throws Exception
	 */
	private JTextPane searchJTextPaneResult(Container aContainer)
			throws Exception {

		return new GUIComponentSearcher<>(JTextPane.class, aContainer, 4)
				.search();
	}

	/**
	 * @param aData
	 * @param aInfos
	 * @throws Exception
	 */
	private void setProgress(final AutomatedInstallData aData,
			final String aInfos) throws Exception {

		// if not in console mode,
		if (!isConsoleMode(aData)) {

			JTextPane wTextPaneResultLabel = searchJTextPaneProgress(
					retrieveCurrentPanel(aData));

			String wText = wTextPaneResultLabel.getText();
			if (wText != null) {
				int wPos = wText.indexOf(':');
				if (wPos > -1) {
					wText = wText.substring(0, wPos);
				}
			} else {
				wText = "Result";
			}
			wTextPaneResultLabel
					.setText(String.format("%s: %s", wText, aInfos));
		}
		// else, if in console mode
		else {
			CLoggerUtils.logInfo("Progress: %s", aInfos);
		}
	}

	/**
	 * @param aData
	 * @param aIsOK
	 * @param aReport
	 * @throws Exception
	 */
	private void setResult(AutomatedInstallData aData, final boolean aIsOK,
			final CReport aReport) throws Exception {

		IzPanel wPanel = retrieveCurrentPanel(aData);

		CLoggerUtils.logInfo("Components of the panel:%s",
				dumpComponents(wPanel));

		JTextPane wJTextPaneResult = searchJTextPaneResult(wPanel);

		wJTextPaneResult.setForeground(aIsOK ? Color.BLUE : Color.RED);

		wJTextPaneResult.setFont(
				new Font(Font.MONOSPACED, Font.PLAIN, RESULT_FONT_SIZE_9));

		wJTextPaneResult.setText(aReport.toStringWithoutNow());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.izforge.izpack.installer.DataValidator#validateData(com.izforge.
	 * izpack.installer.AutomatedInstallData)
	 */
	@Override
	public Status validateData(AutomatedInstallData aData) {

		CLoggerUtils.logInfo("Validatator begin.");

		// hypothesis
		boolean wIsOK = false;
		Status wValidatorStatus = Status.ERROR;

		CReportWritter wReportWritter = null;

		// create the report (name of the validator & width 200)
		CReport wReport = new CReport(getClass().getSimpleName(),
				REPORT_WIDTH_200);

		// tels the report to log each line in the root jul logger
		wReport.setConsoleLogOn(CReport.CONSOLE_LOG_ON);

		wReport.appendTitle("%s:validateData", getClass().getSimpleName());

		boolean wIsRedHatOrOracleLinux = OsVersion.IS_REDHAT_LINUX
				|| OsVersion.IS_ORACLE_LINUX;

		if (!wIsRedHatOrOracleLinux) {

			wReport.appendStep(
					"WARNING NOT 'REDHAT_LINUX' OR 'ORACLE_LINUX' : [%s]",
					getOsDetails(aData));
		}

		wReport.appendStep("Current variables");

		wReport.append("Variables:%s", dumpVariables(aData));

		try {
			// the validation itself: serching of the prerequisites
			wIsOK = validPrerequisites(wReport, aData);

			// according the result of the seraching of the prerequisites
			wValidatorStatus = (wIsOK) ? Status.OK : Status.ERROR;

			wReport.appendStep("Validator status");

			wReport.append("ValidatorStatus=[%s]", wValidatorStatus.name());

			// if the status is not OK, prepare the storage of the report
			if (!wIsOK) {
				wReport.appendStep("Writing the '_onError' report");

				// new Report writter
				wReportWritter = new CReportWritter(wReport,
						CReport.SUFFIX_ON_ERROR);

				wReport.append("OutputFile=[%s]",
						wReportWritter.getOutputFile());

			}

		} catch (Exception e) {
			wReport.appendError(e);
		} finally {
			wReport.appendEof();
		}

		// if the status is not OK, store the "_onError" report
		if (wReportWritter != null) {

			try {
				wReportWritter.write();

				CLoggerUtils.logInfo("NbWritedBytes=[%s]",
						Files.size(wReportWritter.getOutputFile().toPath()));
			} catch (Exception e) {
				CLoggerUtils.logSevere(e);
			}
		}

		// if GUI mode (not console)
		if (isGuiMode(aData)) {

			try {
				// put the report in the 4th textField of the panel
				setResult(aData, wIsOK, wReport);

			} catch (Exception e) {
				CLoggerUtils.logSevere(e);
			}
		}
		// else, if in console mode
		else {

			// if the status is not OK
			if (!wIsOK) {
				// write a title banner
				CLoggerUtils.logBanner(Level.SEVERE, "INSTALLATION STOPPED");
			}
		}

		CLoggerUtils.logInfo("Validatator end.");

		return wValidatorStatus;
	}

	/**
	 * @param wReport
	 * @param aData
	 * @return
	 * @throws Exception
	 */
	private boolean validPrerequisites(CReport aReport,
			AutomatedInstallData aData) throws Exception {

		/**
		 * <pre>
		<variable name="compile.prerequisites.controle.packages.tools" value=
		"gcc,make" />
		 * </pre>
		 */
		aReport.appendStep("validPrerequisites searching tools");

		String wToolsDef = aData
				.getVariable("compile.prerequisites.controle.packages.tools");

		CWordList wToolList = new CWordList(aReport, "tool",
				wToolsDef.split(","));

		setProgress(aData,
				String.format("Searching %d tools.", wToolList.size()));

		aReport.append(wToolList.dumpAsNumberedList());

		String wDevToolsInfos = new CDevToolsInfosFinder(aReport).execute();

		boolean wToolsFound = wToolList.isAllWordsIn(wDevToolsInfos,
				EKindOfFinding.AT_THE_BEGINING_OF_A_LINE);

		setProgress(aData, String.format("All tools found=[%b]", wToolsFound));

		/**
		 * <pre>
		<variable name=
		"compile.prerequisites.controle.packages.libs" value=
		"pcre-devel.x86_64,apr-devel.x86_64,apr-util-devel.x86_64,httpd-devel,libxml2.x86_64,libxml2-devel.x86_64"/>
		 * </pre>
		 */

		aReport.appendStep("validPrerequisites searching libraries");

		String wLibsDef = aData
				.getVariable("compile.prerequisites.controle.packages.libs");

		CWordList wLibraryList = new CWordList(aReport, "library",
				wLibsDef.split(","));

		setProgress(aData,
				String.format("Searching %d libraries.", wLibraryList.size()));

		aReport.append(wLibraryList.dumpAsNumberedList());

		String wDevLibrariesInfos = new CDevLibrariesInfosfinder(aReport)
				.execute();

		boolean wLibraryFound = wLibraryList.isAllWordsIn(wDevLibrariesInfos,
				EKindOfFinding.AT_THE_BEGINING_OF_A_LINE);

		setProgress(aData,
				String.format("All libraries found=[%b]", wLibraryFound));

		setProgress(aData, "");

		return wToolsFound && wLibraryFound;
	}
}
