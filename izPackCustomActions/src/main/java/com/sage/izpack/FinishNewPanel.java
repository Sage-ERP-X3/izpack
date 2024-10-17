package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

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
	private static String logPrefix = "FinishNewPanel instance. ";
	private final UninstallDataWriter uninstallDataWriter;
	private final Resources resources;
	private final ResourcesHelper resourceHelper;

	public FinishNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			UninstallDataWriter uninstallDataWriter, UninstallData uninstallData, Log log) {

		super(panel, parent, installData, resources, uninstallDataWriter, uninstallData, log);

		logger.log(Level.FINE, logPrefix + "Init custom resources.");
		// this.installData = installData;
		this.uninstallDataWriter = uninstallDataWriter;
		this.resources = resources;
		this.resourceHelper = new ResourcesHelper(installData, resources);
		resourceHelper.mergeCustomMessages();

		logger.log(Level.FINE, logPrefix + "Custom resources initialized");

	}

	@Override
	public void panelActivate() {

		boolean uninstallRequired = this.uninstallDataWriter.isUninstallRequired();
		logger.log(Level.FINE, logPrefix + "uninstallRequired:" + uninstallRequired);

		FinishNewPanelAutomationHelper.initUninstallPath(this.resources, this.installData);
		logger.log(Level.FINE, logPrefix + "getUninstallerPath:" + installData.getInfo().getUninstallerPath());

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
		boolean wasIzPack4 = InstallationInformationHelper.isLegacyIzpackInfo();
		installData.setVariable("force-generate-uninstaller", String.valueOf(wasIzPack4));
		boolean uninstallRequired = this.uninstallDataWriter.isUninstallRequired();
		logger.log(Level.FINE, logPrefix + "writeUninstallData. uninstallRequired:" + uninstallRequired);

		if (!uninstallRequired && wasIzPack4) {
			result = uninstallDataWriter.write();
			logger.log(Level.FINE,
					logPrefix + "force writeUninstallData. uninstallDataWriter.write() returns " + result);

			if (!result) {
				String title = this.resourceHelper.getCustomString("installer.error");
				String message = this.resourceHelper.getCustomString("installer.uninstall.writefailed");
				JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			}
		}
		return result;
	}
}
