package com.izforge.izpack.util.sage;

import static com.izforge.izpack.util.sage.CReport.toInsecable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JTextPane;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.panels.UserInputPanel;

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
	 * @param aStrings
	 * @param aLabel
	 * @return
	 */
	private String dumpAsNumberedList(String[] aStrings, final String aLabel) {
		StringBuilder wDump = new StringBuilder();
		int wIdx = 0;
		for (String wString : aStrings) {
			wIdx++;
			wDump.append(toInsecable(
					String.format("\n- %s(%2d)=[%s]", aLabel, wIdx, wString)));
		}
		return wDump.toString();
	}

	/**
	 * @param aPanel
	 * @param aContainer
	 */
	private String dumpComponents(final Container aContainer) {
		StringBuilder wDump = new StringBuilder();

		for (Component wComponent : aContainer.getComponents()) {

			boolean wIsTextPane = wComponent instanceof JTextPane;

			String wText = getText(wComponent);

			wDump.append(toInsecable(String.format(
					"\n- isTextPane=[%-5s] Component=[%-12s][%-50s]",
					wIsTextPane,
					//
					wComponent.getClass().getSimpleName(),
					//
					wText)));

			if (wComponent instanceof java.awt.Container) {
				dumpComponents((java.awt.Container) wComponent);
			}

		}
		return wDump.toString();

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

	private String getText(Component aComponent) {

		if (aComponent instanceof JLabel) {
			return ((JLabel) aComponent).getText();
		}
		if (aComponent instanceof JTextPane) {
			return ((JTextPane) aComponent).getText();
		}
		return null;
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
	 * -   9[INSTALLER                                                   ]=[console]
	 * ...
	 * -  67[SYSTEM_sun_java_command                                     ]=[com.izforge.izpack.installer.Installer -console -language eng]
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
	 * @throws Exception
	 */
	private IzPanel retrieveCurrentPanel(AutomatedInstallData aData)
			throws Exception {
		List<IzPanel> wPanels = aData.panels;

		for (IzPanel wPanel : wPanels) {
			if (wPanel instanceof UserInputPanel) {

				// is the validation service ofthat panel is this instance of
				// CompilePrerequisitesControl
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
	 * @return the 4th JTextPane of the panel
	 * @throws Exception
	 */
	private JTextPane searchJTextPaneResult(Container aContainer)
			throws Exception {

		return new GUIComponentSearcher<>(JTextPane.class, aContainer, 4)
				.search();
	}

	/**
	 * @param wReport
	 * @param aData
	 * @return
	 */
	private boolean searchPrerequisites(CReport aReport,
			AutomatedInstallData aData) {

		aReport.appendStep("searchPrerequisites");

		/**
		 * <pre>
		<variable name="compile.prerequisites.controle.packages.tools" value=
		"gcc,make" />
		 * </pre>
		 */
		String wTools = aData
				.getVariable("compile.prerequisites.controle.packages.tools");

		aReport.append(dumpAsNumberedList(wTools.split(","), "tool"));

		/**
		 * <pre>
		<variable name=
		"compile.prerequisites.controle.packages.libs" value=
		"pcre-devel.x86_64,apr-devel.x86_64,apr-util-devel.x86_64,httpd-devel,libxml2.x86_64,libxml2-devel.x86_64"/>
		 * </pre>
		 */
		String wLibs = aData
				.getVariable("compile.prerequisites.controle.packages.libs");

		aReport.append(dumpAsNumberedList(wLibs.split(","), "library"));

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.izforge.izpack.installer.DataValidator#validateData(com.izforge.
	 * izpack.installer.AutomatedInstallData)
	 */
	@Override
	public Status validateData(AutomatedInstallData aData) {

		// hypothesis
		Status wValidatorStatus = Status.ERROR;

		boolean wIsOK = false;

		CReportWritter wReportWritter = null;

		CReport wReport = new CReport(getClass().getSimpleName(),
				REPORT_WIDTH_200);

		wReport.setConsoleLogOn(CReport.CONSOLE_LOG_ON);

		wReport.appendTitle("%s:validateData", getClass().getSimpleName());

		wReport.appendStep("AutomatedInstallData variables");

		wReport.append("Variables:%s", dumpVariables(aData));

		try {

			boolean wPrerequisitesFound = searchPrerequisites(wReport, aData);

			// according the result of the seraching of the prerequisites
			wValidatorStatus = (wPrerequisitesFound) ? Status.OK : Status.ERROR;

			wIsOK = Status.OK.equals(wValidatorStatus);

			wReport.appendStep("Validator status");

			wReport.append("ValidatorStatus=[%s]", wValidatorStatus.name());

			// if the status is not OK, store the report
			if (!wIsOK) {
				wReport.appendStep("Writing on error report");

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

		// if the status is not OK, store the report
		if (wReportWritter != null) {

			try {
				wReportWritter.write();

				CLoggerUtils.logInfo("NbWritedBytes=[%s]",
						Files.size(wReportWritter.getOutputFile().toPath()));
			} catch (Exception e) {
				CLoggerUtils.logSevere(e);
			}
		}

		// if not in console mode, try to set the 4th textField with the report
		if (!isConsoleMode(aData)) {

			try {
				IzPanel wPanel = retrieveCurrentPanel(aData);

				CLoggerUtils.logInfo("Components of the panel:%s",
						dumpComponents(wPanel));

				JTextPane wJTextPaneResult = searchJTextPaneResult(wPanel);

				wJTextPaneResult.setForeground(wIsOK ? Color.BLUE : Color.RED);

				wJTextPaneResult.setFont(new Font(Font.MONOSPACED, Font.PLAIN,
						RESULT_FONT_SIZE_9));

				wJTextPaneResult.setText(wReport.toStringWithoutNow());

			} catch (Exception e) {
				CLoggerUtils.logSevere(e);
			}
		}

		// if in console mode, write a title banner
		if (isConsoleMode(aData) && !wIsOK) {
			wReport.appendTitle("INSTALLATION CANCELLED");
		}

		return wValidatorStatus;
	}
}
