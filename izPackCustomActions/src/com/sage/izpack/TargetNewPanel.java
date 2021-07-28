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

	// TODO: FRDEPO =>  How to set variable ${component.node.name} in a generic way ?
	// <variable name="TargetPanel.dir.windows" value="c:\Sage\SafeX3\${component.node.name}" condition="!updatemode"/>

	@Override
	public void saveData() {

		logger.log(Level.FINE, "TargetNewPanel.saveData");
		super.saveData();
		String path = pathSelectionPanel.getPath();

        // String show = pathSelectionPanel. getConfigurationOptionValue("", installData.getRules());        
		// installData.getPlatform().
		
		logger.log(Level.FINE, "TargetNewPanel.saveData  Path: " + path);
		if (path.lastIndexOf("\\") > 0) {
			String name = path.substring(path.lastIndexOf("\\") + 1);
			installData.setVariable("component.node.name", name);
			logger.log(Level.FINE, "TargetNewPanel.saveData  component.node.name: " + name);
		}
	}

}
