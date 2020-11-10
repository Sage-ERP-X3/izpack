package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.checkedhello.CheckedHelloPanel;
import com.izforge.izpack.panels.checkedhello.RegistryHelper;

/*
* @author Franck DEPOORTERE
*/

public class CheckedHelloNewPanel extends CheckedHelloPanel {

	private static Logger logger = Logger.getLogger(CheckedHelloNewPanel.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1737042770727953387L; // 1737042770727953387L

	public CheckedHelloNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			RegistryDefaultHandler handler, Log log) throws Exception {
		super(panel, parent, installData, resources, handler, log);

		RegistryHelper registryHelper = new RegistryHelper(handler, installData);
		String path = registryHelper.getInstallationPath();
		if (path != null) {
			installData.setVariable("TargetPanel.dir.windows", path);
			logger.log(Level.FINE, "Set TargetPanel.dir.windows: " + path);

			installData.setVariable(InstallData.INSTALL_PATH, path);
			logger.log(Level.FINE, "Set INSTALL_PATH", path);
		}
	}

	/**
	 * Returns whether the handled application is already registered or not. The
	 * validation will be made only on systems which contains a registry (Windows).
	 *
	 * @return <tt>true</tt> if the application is registered
	 * @throws Exception if it cannot be determined if the application is registered
	 */
	@Override
	protected boolean isRegistered() throws Exception {
		boolean result = super.isRegistered();

		// registryHelper.getInstallationPath();
		if (result) {			
			// Set variable "modify.izpack.install"
			installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
		}
		logger.log(Level.FINE, "Set " + InstallData.MODIFY_INSTALLATION + ": true");
		return result;
	}

	/*
	 * public void panelActivate() { if (abortInstallation) {
	 * parent.lockNextButton(); try { if (multipleInstall()) { //
	 * setUniqueUninstallKey(); abortInstallation = false;
	 * parent.unlockNextButton(); } else {
	 * installData.getInfo().setUninstallerPath(null);
	 * installData.getInfo().setUninstallerName(null);
	 * installData.getInfo().setUninstallerCondition("uninstaller.nowrite"); } }
	 * catch (Exception exception) { logger.log(Level.WARNING,
	 * exception.getMessage(), exception); } }
	 * 
	 * installData.setVariable("UNINSTALL_NAME", registryHelper.getUninstallName());
	 * }
	 */
}
