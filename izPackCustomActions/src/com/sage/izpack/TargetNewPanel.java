package com.sage.izpack;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.target.TargetPanel;

/*
 *  @author Franck DEPOORTERE
 */
public class TargetNewPanel extends TargetPanel {

	private static final long serialVersionUID = 4462248660406450482L;
	private static Logger logger = Logger.getLogger(TargetNewPanel.class.getName());
	private static final String logPrefix = "TargetNewPanel - ";

	public TargetNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			Log log) {
		super(panel, parent, installData, resources, log);
	}

	/**
	 * Called when the panel becomes active.
	 */
	@Override
	public void panelActivate() {

		logger.log(Level.FINE, logPrefix + "panelActivate");
		super.panelActivate();
		logger.log(Level.FINE, logPrefix + "panelActivate  Path: " + pathSelectionPanel.getPath());
	}

	@Override
	public void saveData() {

		logger.log(Level.FINE, logPrefix + "saveData");

		// <ComponentName>c:\Sage\SafeX3\${component.node.name}</ComponentName>
		String platformSep = "[\\\\\\/]";// "\\\\";
		int positionFromEnd = 0;
		String variableNameRaw = getMetadata().getConfigurationOptionValue("SetVariableName", installData.getRules());
		String variableName = null;
		logger.log(Level.FINE, logPrefix + "saveData variableNameRaw: " + variableNameRaw);

		if (variableNameRaw != null && variableNameRaw.indexOf("${") >= 0 && variableNameRaw.indexOf("}") >= 0) {

			variableName = variableNameRaw.substring(variableNameRaw.indexOf("${") + 2, variableNameRaw.indexOf("}"));

			String[] pathElements = variableNameRaw.split(platformSep);
			for (int i = 0; i < pathElements.length; i++) {
				String part = pathElements[i];
				if (part.equals(variableName)) {
					positionFromEnd = pathElements.length - 1 - i;
				}
			}
			logger.log(Level.FINE,
					logPrefix + "saveData variableName: '" + variableName + "'  positionFromEnd: " + positionFromEnd);
		}

		super.saveData();
		String path = pathSelectionPanel.getPath();

		logger.log(Level.FINE, logPrefix + "saveData Path: " + path);
		if (variableName != null) {

			String[] pathElements = path.split(platformSep);
			String variableValue = pathElements[(pathElements.length - 1 - positionFromEnd)];
			installData.setVariable(variableName, variableValue);
			logger.log(Level.FINE,
					logPrefix + "saveData path: " + path + "  setVariable  '" + variableName + "': " + variableValue);
		}

	}

	@Override
	public boolean isValidated() {
		boolean result = false;
		File targetPathFile = new File(installData.getVariables().replace(getPath()));
		if (InstallationInformationHelper.isIncompatibleInstallation(getPath(), installData.getInfo().isReadInstallationInformation())) {
			emitError(error, getMessage("incompatibleInstallation"));
		} else if (targetPathFile.isFile()) {
			emitError(error, getMessage("isfile"));
			return false;
		} else {
			String path = getPath();
			path = installData.getVariables().replace(path);
			installData.setInstallPath(path);
			result = true;
		}
		return result;
	}

}
