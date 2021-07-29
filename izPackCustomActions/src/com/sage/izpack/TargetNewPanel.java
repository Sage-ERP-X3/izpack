package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.target.TargetPanel;

public class TargetNewPanel extends TargetPanel {

	private static Logger logger = Logger.getLogger(TargetNewPanel.class.getName());

	public TargetNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			Log log) {
		super(panel, parent, installData, resources, log);
	}

	/**
	 * Called when the panel becomes active.
	 */
	@Override
	public void panelActivate() {

		logger.log(Level.FINE, "TargetNewPanel.panelActivate");
		super.panelActivate();
		logger.log(Level.FINE, "TargetNewPanel.panelActivate  Path: " + pathSelectionPanel.getPath());
	}

	// TODO: FRDEPO => How to set variable ${component.node.name} in a generic way ?
	// <variable name="TargetPanel.dir.windows"
	// value="c:\Sage\SafeX3\${component.node.name}" condition="!updatemode"/>

	@Override
	public void saveData() {

		logger.log(Level.FINE, "TargetNewPanel.saveData");

		// <ComponentName>c:\Sage\SafeX3\${component.node.name}</ComponentName>
		String platformSep = "[\\\\\\/]";// "\\\\"; 
		int positionFromEnd = 0;
		String variableNameRaw = getMetadata().getConfigurationOptionValue("SetVariableName", installData.getRules());
		String variableName = null;
		logger.log(Level.FINE, "TargetNewPanel.saveData  variableNameRaw: " + variableNameRaw);

		if (variableNameRaw != null && variableNameRaw.indexOf("${") >= 0 && variableNameRaw.indexOf("}") >= 0) {

			variableName = variableNameRaw.substring(variableNameRaw.indexOf("${") + 2, variableNameRaw.indexOf("}"));

			String[] pathElements = variableNameRaw.split(platformSep);
			for (int i = 0; i < pathElements.length; i++) {
				String part = pathElements[i];
				if (part.equals(variableName)) {
					positionFromEnd = pathElements.length - 1 - i;
				}
			}
			logger.log(Level.FINE, "TargetNewPanel.saveData  variableName: '" + variableName + "'  positionFromEnd: "
					+ positionFromEnd);
		}

		super.saveData();
		String path = pathSelectionPanel.getPath();

		logger.log(Level.FINE, "TargetNewPanel.saveData  Path: " + path);
		if (variableName != null) {

			String[] pathElements = path.split(platformSep);
			String variableValue = pathElements[(pathElements.length - 1 - positionFromEnd)];
			installData.setVariable(variableName, variableValue);
			logger.log(Level.FINE, "TargetNewPanel.saveData path: " + path + "  setVariable  '" + variableName + "': "
					+ variableValue);
			
			}				
		
	}

}
