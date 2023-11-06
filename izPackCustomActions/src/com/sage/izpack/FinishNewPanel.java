package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.finish.FinishPanel;

public class FinishNewPanel extends FinishPanel {

	/*
	 * @author Franck DEPOORTERE
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(FinishNewPanel.class.getName());
	private UninstallDataWriter uninstallDataWriter;
	
	public FinishNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			UninstallDataWriter uninstallDataWriter, UninstallData uninstallData, Log log) {

		super(panel, parent, installData, resources, uninstallDataWriter, uninstallData, log);

		logger.log(Level.FINE, "FinishNewPanel instance. Init custom resources.");

		this.uninstallDataWriter = uninstallDataWriter;
		ResourcesHelper resourceHelper = new ResourcesHelper(installData, resources);
		resourceHelper.mergeCustomMessages();

		logger.log(Level.FINE, "FinishNewPanel instance. Custom resources initialized");
	}

}
