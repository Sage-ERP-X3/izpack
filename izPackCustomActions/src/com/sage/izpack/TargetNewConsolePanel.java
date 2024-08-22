package com.sage.izpack;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.installer.util.InstallPathHelper;
import com.izforge.izpack.panels.path.PathInputBase;
import com.izforge.izpack.panels.target.TargetConsolePanel;
import com.izforge.izpack.panels.target.TargetPanelHelper;
import com.izforge.izpack.util.Console;

import static com.izforge.izpack.panels.target.TargetPanel.PANEL_NAME;

public class TargetNewConsolePanel extends TargetConsolePanel {

	private static Logger logger = Logger.getLogger(TargetNewConsolePanel.class.getName());
	private static final String logPrefix = "TargetNewConsolePanel - ";

	public TargetNewConsolePanel(PanelView<ConsolePanel> panel, InstallData installData, Resources resources,
			Prompt prompt) {
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
		} else {

			// if
			// (InstallationInformationHelper.isIncompatibleInstallation(installData.getInstallPath(),
			// installData.getInfo().isReadInstallationInformation())) {
			// System.err.println(logPrefix +
			// "getIncompatibleInstallationMsg(installData)");

			// }
			// else {
			path = installData.getVariables().replace(path);
			installData.setInstallPath(path);
			result = true;
		}
		return result;
	}

	@Override
	public boolean run(InstallData installData, Console console) {
		printHeadLine(installData, console);

		String introText = getI18nStringForClass("intro", PANEL_NAME, installData);
		if (introText != null) {
			console.println(introText);
			console.println();
		}

		String defaultPath = InstallPathHelper.getPath(installData);
		PathInputBase.setInstallData(installData);

		if (defaultPath == null) {
			defaultPath = "";
		}

		while (true) {
			String path = console.promptLocation(getMessage("info") + " [" + defaultPath + "] ", defaultPath);
			if (path != null) {
				path = installData.getVariables().replace(path);
				String normalizedPath = PathInputBase.normalizePath(path);
				File pathFile = new File(normalizedPath);

				if (InstallationInformationHelper.isIncompatibleInstallation(normalizedPath, installData.getInfo().isReadInstallationInformation())) {
					console.println(getMessage("incompatibleInstallation"));
					continue;
				} else if (!PathInputBase.isWritable(normalizedPath)) {
					console.println(getMessage("notwritable"));
					continue;
				} else if (!normalizedPath.isEmpty()) {
					if (pathFile.isFile()) {
						console.println(getMessage("isfile"));
						continue;
					} else if (pathFile.exists()) {
						if (!checkOverwrite(pathFile, console)) {
							continue;
						}
					} else if (!checkCreateDirectory(pathFile, console)) {
						continue;
					} else if (!installData.getPlatform().isValidDirectoryPath(pathFile)) {
						console.println(getMessage("syntax.error"));
						continue;
					}
					installData.setInstallPath(normalizedPath);
					return promptEndPanel(installData, console);
				}
				return run(installData, console);
			} else {
				return false;
			}
		}
	}
}
