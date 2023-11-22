package com.sage.izpack;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.target.TargetConsolePanel;
import com.izforge.izpack.util.Console;

public class TargetNewConsolePanel extends TargetConsolePanel {

	private static Logger logger = Logger.getLogger(TargetNewConsolePanel.class.getName());
	private static final String logPrefix = "TargetNewConsolePanel - ";

	public TargetNewConsolePanel(PanelView<ConsolePanel> panel, InstallData installData, Prompt prompt) {
		super(panel, installData, prompt);
	}

	@Override
	public boolean run(InstallData installData, Properties properties) {
		logger.log(Level.FINE, logPrefix + "run  properties: " + properties);

		// return super.run(installData, properties);

		boolean result = false;
		String path = properties.getProperty(InstallData.INSTALL_PATH);
		if (path == null || "".equals(path.trim())) {
			System.err.println(logPrefix + "Missing mandatory target path!");
		} else if (InstallationInformationHelper.isIncompatibleInstallation(installData.getInstallPath(),
				installData.getInfo().isReadInstallationInformation())) {

			System.err.println(logPrefix + "getIncompatibleInstallationMsg(installData)");

		}
		// else if (TargetPanelHelper.isIncompatibleInstallation(path,
		// installData.getInfo().isReadInstallationInformation()))
		// {
		// System.err.println(getIncompatibleInstallationMsg(installData));
		// }
		else {
			path = installData.getVariables().replace(path);
			installData.setInstallPath(path);
			result = true;
		}
		return result;
	}

	@Override
	public boolean run(InstallData installData, Console console) {
		logger.log(Level.FINE, logPrefix + "run  console: " + console);
		return super.run(installData, console);
	}
}
