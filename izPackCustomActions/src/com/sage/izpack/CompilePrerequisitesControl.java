package com.sage.izpack;

import static com.sage.izpack.CTextLineUtils.toInsecable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.nio.file.Files;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JTextPane;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;
// import com.izforge.izpack.util.sage.CWordList.EKindOfFinding;
import com.sage.izpack.CWordList.EKindOfFinding;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 *
 *
 * @author ogattaz
 *
 */
public class CompilePrerequisitesControl implements DataValidator {

	private static final Logger logger = Logger.getLogger(AdxCompInstallerListener.class.getName());

	private static final int REPORT_WIDTH_200 = 200;
	private static final int REPORT_WIDTH_80 = 100;

	private static final int RESULT_FONT_SIZE_9 = 9;
	private Resources resources;
	private GUIInstallData guiInstallData;

	/**
	 *
	 */
	public CompilePrerequisitesControl(GUIInstallData installData, Resources resources) {
		super();
		this.resources = resources;
		this.guiInstallData = installData;
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
	private StringBuilder dumpComponents(final Container aContainer, StringBuilder aSB) {

		for (Component wComponent : aContainer.getComponents()) {

			boolean wIsTextPane = wComponent instanceof JTextPane;

			String wText = "";

			if (wIsTextPane) {
				wText = ((JTextPane) wComponent).getText();
			} else if (wComponent instanceof JLabel) {
				wText = ((JLabel) wComponent).getText();
			}

			aSB.append(toInsecable(String.format("\n- isTextPane=[%-5s] Component=[%-12s][%s]", wIsTextPane,
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
	private String dumpVariables(InstallData installData) {
		StringBuilder wDump = new StringBuilder();
		Variables wVariables = installData.getVariables();
		// Map<Object, Object> wSortedProperties = new TreeMap<>(wVariables);
		int wIdx = 0;
		for (Entry<Object, Object> wEntry : wVariables.getProperties().entrySet()) {
			wIdx++;
			wDump.append(toInsecable(String.format("\n- %3d[%-60s]=[%s]", wIdx, String.valueOf(wEntry.getKey()),
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
	 * 56[SYSTEM_os_arch]=[x86_64]
	 * 57[SYSTEM_os_name]=[MacOSX]
	 * 58[SYSTEM_os_version]=[10.15.7]
	 * </pre>
	 *
	 * @return
	 */
	private String getOsDetails(InstallData aData) {

		return String.format("%s - %s - %s", aData.getVariable("SYSTEM_os_name"),
				aData.getVariable("SYSTEM_os_version"), aData.getVariable("SYSTEM_os_arch")).replace('\n', ' ');

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
	private boolean isConsoleMode(InstallData aData) {
		return isMode(aData, "console");
	}

	private boolean isBatchMode(InstallData aData) {
		return isMode(aData, "automated");
	}

	private boolean isMode(InstallData aData, String occurrence) {

		String wInstaller = aData.getVariable("INSTALLER");
		if (wInstaller != null && wInstaller.contains(occurrence)) {
			return true;
		}
		String wJavaCommand = aData.getVariable("SYSTEM_sun_java_command");
		if (wJavaCommand != null && wJavaCommand.contains(occurrence)) {
			return true;
		}
		return false;
	}

	/**
	 * @param aData
	 * @return
	 */
	private boolean isGuiMode(InstallData aData) {
		return !isConsoleMode(aData) && !isBatchMode(aData);
	}

	/**
	 * @param aData
	 * @return
	 * @throws Exception
	 */
	private IzPanel retrieveCurrentPanel(GUIInstallData guiInstallData, InstallData installData) throws Exception {

		Panel panelFound = null;
		String panelValidator = null;
		for (Panel panel : installData.getPanelsOrder()) {
			List<String> validators = panel.getValidators();
			logger.log(Level.FINE, "Panel: " + panel.getClassName() + " validators: " + validators + " this.classname: "
					+ this.getClass().getName());
			for (String validator : validators) {
				logger.log(Level.FINE, "validators: " + validator + " classname: " + this.getClass().getName());
				if (validator == this.getClass().getName()) {
					panelFound = panel;
					panelValidator = validator;
				}
			}
		}
		logger.log(Level.FINE, " panelFound:" + panelFound + " panelFound.getClassName():" + panelFound.getClassName());

		// test each panel of the installer
		// for (Panel wPanel : installData.getPanelsOrder()) {
		for (IzPanel wPanel : guiInstallData.getPanels()) {

			logger.log(Level.FINE, "wPanel: " + wPanel + " wPanel.getName(): " + wPanel.getName() + " panelFound:"
					+ panelFound + " panelFound.getClassName():" + panelFound.getClassName());

			if (panelFound != null) {
				if (wPanel.getName() == panelFound.getClassName()) {
					return wPanel;
				}
			}
			// if (wPanel instanceof UserInputPanel) {
			// its the current panel if the validation service of the panel
			// is this instance of CompilePrerequisitesControl
			// boolean wIsCurrentPanel = (wPanel.getValidationService() == this);
			// boolean wIsCurrentPanel =
			// (wPanel.getValidators().contains(this.getClass().getName()));
			// logger.log(Level.FINE, wPanel.getValidators() + " contains " +
			// this.getClass().getName());

			/*
			 * TODO: FRDEPO if (wIsCurrentPanel) { return wPanel; }
			 */
		}

		throw new Exception("Unable to retrieve the current panel");
	}

	/**
	 * @param aPanel
	 * @return the 3th JTextPane of the panel
	 * @throws Exception
	 */
	private JTextPane searchJTextPaneProgress(Container aContainer) throws Exception {

		return new GUIComponentSearcher<>(JTextPane.class, aContainer, 3).search();
	}

	/**
	 * @param aPanel
	 * @return the 4th JTextPane of the panel
	 * @throws Exception
	 */
	private JTextPane searchJTextPaneResult(Container aContainer) throws Exception {

		return new GUIComponentSearcher<>(JTextPane.class, aContainer, 4).search();
	}

	/**
	 * @param aData
	 * @param aInfos
	 * @throws Exception
	 */
	private void setProgress(final InstallData installData, final String aInfos) throws Exception {

		// if not in console mode,
		if (isGuiMode(installData)) { // isConsoleMode(aData) && !isBatchMode(aData)) {

			JTextPane wTextPaneResultLabel = searchJTextPaneProgress(
					retrieveCurrentPanel(this.guiInstallData, installData));

			String wText = wTextPaneResultLabel.getText();
			if (wText != null) {
				int wPos = wText.indexOf(':');
				if (wPos > -1) {
					wText = wText.substring(0, wPos);
				}
			} else {
				wText = "Result";
			}
			wTextPaneResultLabel.setText(String.format("%s: %s", wText, aInfos));
		}
		// else, if in "console" or "batch" mode
		else {
			CLoggerUtils.logInfo("Progress: %s", aInfos);
			// LogInfo(aData, String.format("Progress: %s", aInfos));
		}
	}

	/**
	 * @param aData
	 * @param aIsOK
	 * @param aReport
	 * @throws Exception
	 */
	private void setResult(InstallData installData, final boolean aIsOK, final CReport aReport) throws Exception {

		IzPanel wPanel = retrieveCurrentPanel(this.guiInstallData, installData);
		CLoggerUtils.logInfo("Components of the panel:%s", dumpComponents(wPanel));
		JTextPane wJTextPaneResult = searchJTextPaneResult(wPanel);
		wJTextPaneResult.setForeground(aIsOK ? Color.BLACK : Color.BLUE);
		wJTextPaneResult.setFont(new Font(Font.MONOSPACED, Font.PLAIN, RESULT_FONT_SIZE_9));
		wJTextPaneResult.setText(aReport.toStringWithoutNow());
	}

	private void LogInfo(InstallData aData, String mesg) {

		if (isGuiMode(aData)) {
			CLoggerUtils.logInfo(mesg);
		} else { // Console mode
			System.out.println(mesg);
		}
	}

	private void LogError(InstallData aData, String mesg) {

		if (isGuiMode(aData)) {
			CLoggerUtils.logSevere(mesg);
		} else { // Console mode
			System.err.println(mesg);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.izforge.izpack.installer.DataValidator#validateData(com.izforge.
	 * izpack.installer.AutomatedInstallData)
	 */
	@Override
	public Status validateData(InstallData aData) {
		// TODO Auto-generated method stub
		// return null;
		// }

		// @Override
		// public Status validateData(AutomatedInstallData aData) {

		LogInfo(aData, "Validator begin.");

		// hypothesis
		boolean wIsOK = false;
		Status wValidatorStatus = Status.ERROR;

		CReportWritter wReportWritter = null;

		// create the report (name of the validator & width 200)

		CReport wReport = new CReport(getClass().getSimpleName(),
				isConsoleMode(aData) ? REPORT_WIDTH_80 : REPORT_WIDTH_200);

		// tels the report to log each line in the root jul logger
		if (Debug.isTRACE()) // || isConsoleMode(aData))
			wReport.setConsoleLogOn(CReport.CONSOLE_LOG_ON);
		else
			wReport.setConsoleLogOn(CReport.CONSOLE_LOG_OFF);

		boolean wIsRedHatOrOracleLinux = OsVersion.IS_REDHAT_LINUX || OsVersionHelper.IS_ORACLE_LINUX;

		if (!wIsRedHatOrOracleLinux) {
			wReport.appendStep("WARNING NOT 'REDHAT_LINUX' OR 'ORACLE_LINUX' : [%s]", getOsDetails(aData));
		}

		if (Debug.isTRACE()) {
			wReport.appendStep("Current variables");
			wReport.append("Variables: %s", dumpVariables(aData));
		}
		try {
			// the validation itself: searching of the prerequisites
			wIsOK = validPrerequisites(wReport, aData, resources);

			// according the result of the seraching of the prerequisites
			wValidatorStatus = (wIsOK) ? Status.OK : Status.ERROR;

			// wReport.appendStep("Validator status");

			wReport.append("");
			wReport.append("Validator status = [%s]", wValidatorStatus.name());

			// if the status is not OK, prepare the storage of the report
			if (!wIsOK) {
				// wReport.appendStep("Writing the '_onError' report");
				wReportWritter = new CReportWritter(wReport, CReport.SUFFIX_ON_ERROR);
				wReport.append("OutputFile = [%s]", wReportWritter.getOutputFile());
			}

		} catch (Exception e) {
			wReport.appendError(e);
		} finally {
			// wReport.appendEof();
		}

		// if the status is not OK, store the "_onError" report
		if (wReportWritter != null) {

			try {
				wReportWritter.write();
				LogInfo(aData,
						String.format("NbWritedBytes = [%s]", Files.size(wReportWritter.getOutputFile().toPath())));
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

				LogInfo(aData, wReport.toStringWithoutNow());

				// write a title banner
				LogError(aData, "INSTALLATION STOPPED");
				// CLoggerUtils.logBanner(Level.SEVERE, "INSTALLATION STOPPED");
			}
		}

		LogInfo(aData, "Validator end.");

		return wValidatorStatus;
	}

	/**
	 * @param wReport
	 * @param aData
	 * @return
	 * @throws Exception
	 */
	private boolean validPrerequisites(CReport aReport, InstallData aData, Resources resources) throws Exception {

		/**
		 * <pre>
		<variable name="compile.prerequisites.control.packages.tools" value=
		"gcc,make" />
		 * </pre>
		 */
		aReport.appendStep("validPrerequisites searching tools");

		String wToolsDef = aData.getVariable("compile.prerequisites.control.packages.tools");
		CWordList wToolList = new CWordList(aReport, "tool", wToolsDef.split(","));
		wToolList.SetFriendlySuccessMsg(aData.getVariable("compile.prerequisites.control.packages.successmessage"));
		wToolList.SetFriendlyWarningMsg(aData.getVariable("compile.prerequisites.control.packages.warningmessage"));
		setProgress(aData, String.format("Searching %d tools : %s", wToolList.size(), wToolsDef));
		aReport.append(wToolList.dumpAsNumberedList());

		String wDevToolsInfos = new CDevToolsInfosFinder(aReport, aData, resources).execute();
		boolean wToolsFound = wToolList.isAllWordsIn(wDevToolsInfos, EKindOfFinding.AT_THE_BEGINING_OF_A_LINE);
		setProgress(aData, String.format("All tools found=[%b]", wToolsFound));

		/**
		 * <pre>
		<variable name=
		"compile.prerequisites.control.packages.libs" value=
		"pcre-devel.x86_64,apr-devel.x86_64,apr-util-devel.x86_64,httpd-devel,libxml2.x86_64,libxml2-devel.x86_64"/>
		 * </pre>
		 */

		aReport.appendStep("validPrerequisites searching libraries");

		String wLibsDef = aData.getVariable("compile.prerequisites.control.packages.libs");
		CWordList wLibraryList = new CWordList(aReport, "library", wLibsDef.split(","));
		wLibraryList.SetFriendlySuccessMsg(aData.getVariable("compile.prerequisites.control.packages.successmessage"));
		wLibraryList.SetFriendlyWarningMsg(aData.getVariable("compile.prerequisites.control.packages.warningmessage"));
		setProgress(aData, String.format("Searching %d libraries : %s", wLibraryList.size(), wLibsDef));
		aReport.append(wLibraryList.dumpAsNumberedList());
		String wDevLibrariesInfos = new CDevLibrariesInfosfinder(aReport, aData, resources).execute();
		boolean wLibraryFound = wLibraryList.isAllWordsIn(wDevLibrariesInfos, EKindOfFinding.AT_THE_BEGINING_OF_A_LINE);
		setProgress(aData, String.format("All libraries found=[%b]", wLibraryFound));
		setProgress(aData, "");
		return wToolsFound && wLibraryFound;
	}

}
