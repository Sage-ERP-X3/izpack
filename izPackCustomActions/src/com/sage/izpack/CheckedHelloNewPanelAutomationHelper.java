package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.panels.checkedhello.CheckedHelloPanelAutomationHelper;
import com.izforge.izpack.panels.checkedhello.RegistryHelper;
import com.izforge.izpack.util.OsVersion;

public class CheckedHelloNewPanelAutomationHelper extends CheckedHelloPanelAutomationHelper {

	private static Logger logger = Logger.getLogger(CheckedHelloNewPanelAutomationHelper.class.getName());

	private final RegistryHelper registryHelper;
	private InstallData installData;
	private RegistryHandler registryHandler;
	private static String logPrefix = "CheckedHelloNewPanelAutomationHelper ";

	public CheckedHelloNewPanelAutomationHelper(RegistryDefaultHandler handler, InstallData installData,
			Resources resources) throws NativeLibException {
		super(handler, installData);

		logger.log(Level.FINE, logPrefix);

		this.registryHelper = new RegistryHelper(handler, installData);
		this.registryHandler = handler != null ? handler.getInstance() : null;
		this.installData = installData;

		String path = installData.getInstallPath();
		if (path == null && OsVersion.IS_WINDOWS)
			path = this.registryHelper.getInstallationPath();

		if (path == null) {
			RegistryHandlerX3 x3Handler = new RegistryHandlerX3(this.registryHandler, installData);
			if (x3Handler.isAdminSetup()) {
				path = x3Handler.getAdxAdminDirPath();
			}
			logger.log(Level.WARNING, logPrefix + "Could not get InstallationPath() return NULL. path: " + path);
		}

		if (path != null) {
			String targetPanelDir = "TargetPanel.dir.windows";
			if (OsVersion.IS_LINUX) {
				targetPanelDir = "TargetPanel.dir.unix";
				installData.setVariable(targetPanelDir, path);
			} else {
				installData.setVariable(targetPanelDir, path);
			}
			logger.log(Level.FINE, logPrefix + "Set " + targetPanelDir + ": " + path);

			installData.setVariable(InstallData.INSTALL_PATH, path);
			logger.log(Level.FINE, logPrefix + "Set INSTALL_PATH", path);

			installData.setVariable("UNINSTALL_NAME", installData.getVariable("APP_NAME"));
			logger.log(Level.FINE, logPrefix + "Set UNINSTALL_NAME: " + path);

			installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
			logger.log(Level.FINE, logPrefix + "Set " + InstallData.MODIFY_INSTALLATION + ": true");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void runAutomated(InstallData installData, IXMLElement panelRoot) {
		RegistryHandlerX3 x3Handler = new RegistryHandlerX3(this.registryHandler, installData);

		if (x3Handler.needAdxAdmin()) {
			try {
				// Check presence of adxadmin
				String adxAdminPath = x3Handler.getAdxAdminDirPath();
				boolean adxAdminInstalled = (adxAdminPath != null);

				// No Adxadmin
				if (!adxAdminInstalled) {
					logger.log(Level.FINE, ResourcesHelper.getCustomPropString("adxadminNotRegistered"));
					System.out.println(ResourcesHelper.getCustomPropString("adxadminNotRegistered"));
					System.out.println();
					return;
				}

			} catch (Exception e) { // Will only be happen if registry handler is good, but an
									// exception at performing was thrown. This is an error...
				logger.log(Level.WARNING,
						ResourcesHelper.getCustomPropString("installer.error") + ":" + e.getMessage());
				return;
			}
		}

		// We had to override this method to remove APP_VER
		// return variables.get("APP_NAME") + " " + variables.get("APP_VER");
		Variables variables = this.installData.getVariables();
		installData.setVariable("UNINSTALL_NAME", variables.get("APP_NAME"));
	}

}
