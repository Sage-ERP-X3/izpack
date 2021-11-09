package com.sage.izpack;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.finish.FinishPanel;

public class FinishNewPanel extends FinishPanel {

	public FinishNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			UninstallDataWriter uninstallDataWriter, UninstallData uninstallData, Log log) {
		super(panel, parent, installData, resources, uninstallDataWriter, uninstallData, log);
		// TODO Auto-generated constructor stub
	}

}
