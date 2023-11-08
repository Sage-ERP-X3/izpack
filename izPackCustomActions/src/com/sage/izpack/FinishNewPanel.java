package com.sage.izpack;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.finish.FinishPanel;
import com.izforge.izpack.util.OsVersion;

public class FinishNewPanel extends FinishPanel {

	/*
	 * @author Franck DEPOORTERE
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(FinishNewPanel.class.getName());
	private UninstallDataWriter uninstallDataWriter;
	private Resources resources;
	private ResourcesHelper resourceHelper;

	public FinishNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			UninstallDataWriter uninstallDataWriter, UninstallData uninstallData, Log log) {

		super(panel, parent, installData, resources, uninstallDataWriter, uninstallData, log);

		logger.log(Level.FINE, "FinishNewPanel instance. Init custom resources.");
		// this.installData = installData;
		this.uninstallDataWriter = uninstallDataWriter;
		this.resources = resources;
		this.resourceHelper = new ResourcesHelper(installData, resources);
		resourceHelper.mergeCustomMessages();

		logger.log(Level.FINE, "FinishNewPanel instance. Custom resources initialized");

	}

	@Override
	public void panelActivate() {

		boolean uninstallRequired = this.uninstallDataWriter.isUninstallRequired();
		logger.log(Level.FINE, "FinishNewPanel instance. uninstallRequired:" + uninstallRequired);

		FinishNewPanelAutomationHelper.initUninstallPath(this.installData);
		logger.log(Level.FINE,
				"FinishNewPanel instance. getUninstallerPath:" + installData.getInfo().getUninstallerPath());

		writeUninstallData();

		super.panelActivate();

	}

	/*
	 * @Override protected void saveData() { super.saveData(); // Save Data
	 * writeUninstallData(); }
	 */
	private boolean writeUninstallData() {

		boolean result = true;

		// X3-256055: Uninstaller (izpack 5.2)
		installData.setVariable("force-generate-uninstaller", "true");
		// installData.getInfo().setUninstallerCondition("uninstaller.write");

		boolean uninstallRequired = this.uninstallDataWriter.isUninstallRequired();
		logger.log(Level.FINE, "FinishNewPanel writeUninstallData. uninstallRequired:" + uninstallRequired);

		if (!uninstallDataWriter.isUninstallRequired()) {
			result = uninstallDataWriter.write();
			logger.log(Level.FINE,
					"FinishNewPanel force writeUninstallData. uninstallDataWriter.write() returns " + result);

			if (!result) {
				// Messages messages = locales.getMessages();
				String title = this.resourceHelper.getCustomString("installer.error");
				String message = this.resourceHelper.getCustomString("installer.uninstall.writefailed");
				JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			}
		}
		return result;
	}
}
