package com.sage.izpack;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.coi.tools.os.win.MSWinConstants;
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

/*
 * @author Franck DEPOORTERE
*/
public class CheckedHelloNewPanelAutomationHelper extends CheckedHelloPanelAutomationHelper {

	private static Logger logger = Logger.getLogger(CheckedHelloNewPanelAutomationHelper.class.getName());

	private final RegistryHelper registryHelper;
	private final InstallData installData;
	private final RegistryHandler registryHandler;
	private final RegistryHandlerX3 x3Handler;
	private static final String logPrefix = "CheckedHelloNewPanelAutomationHelper - ";

	public CheckedHelloNewPanelAutomationHelper(RegistryDefaultHandler handler, InstallData installData,
			Resources resources) throws NativeLibException {
		super(handler, installData);

		logger.log(Level.FINE, logPrefix);

		this.registryHelper = new RegistryHelper(handler, installData);
		this.registryHandler = handler != null ? handler.getInstance() : null;
		this.installData = installData;
		this.x3Handler = new RegistryHandlerX3(registryHandler, installData);

		CheckedHelloNewPanel.initPath(installData, resources, registryHelper, x3Handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void runAutomated(InstallData installData, IXMLElement panelRoot) {
		ModifyInstallationUtil.set(installData, isUpdate());

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

	/**
	 * @throws Exception
	 */
	public static void setUniqueUninstallKey(RegistryHandler registryHandler, RegistryHelper registryHelper,
			ResourcesHelper resourcesHelper) throws Exception {
		// Let us play a little bit with the registry again...
		// Now we search for an unique uninstall key.
		// First we need a handler. There is no overhead at a
		// second call of getInstance, therefore we do not buffer
		// the handler in this class.

		int oldVal = registryHandler.getRoot(); // Only for security...
		// We know, that the product is already installed, else we
		// would not in this method. First we get the
		// "default" uninstall key.
		if (oldVal > 100) // Only to inhibit warnings about local variable never read.
		{
			return;
		}
		String uninstallName = registryHelper.getUninstallName();
		int uninstallModifier = 1;
		while (true) {
			if (uninstallName == null) {
				logger.log(Level.WARNING, logPrefix + "uninstallName returns NULL");
				break; // Should never be...
			}
			// Now we define a new uninstall name.
			String newUninstallName = uninstallName + "(" + Integer.toString(uninstallModifier) + ")";
			// Then we "create" the reg key with it.
			String keyName = RegistryHandler.UNINSTALL_ROOT + newUninstallName;
			registryHandler.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
			if (!registryHandler.keyExist(keyName)) { // That's the name for which we searched.
														// Change the uninstall name in the reg helper.
				registryHandler.setUninstallName(newUninstallName);
				// Now let us inform the user.
				// emitNotification(getString("CheckedHelloNewPanel.infoOverUninstallKey") +
				// newUninstallName);
				logger.log(Level.FINE, logPrefix + "setUniqueUninstallKey() "
						+ getString("CheckedHelloNewPanel.infoOverUninstallKey", resourcesHelper) + newUninstallName);
				// Now a little hack if the registry spec file contains
				// the pack "UninstallStuff".
				break;
			}
			uninstallModifier++;
		}
	}

	public static String getString(String key, ResourcesHelper resourcesHelper) {

		return resourcesHelper.getCustomString(key);
	}

	private boolean isUpdate() {
		if (OsVersion.IS_WINDOWS) {
            try {
                return registryHelper.isRegistered();
            } catch (NativeLibException e) {
				return Boolean.FALSE;
            }
        }
		if (installData == null
				|| installData.getInfo() == null
				|| installData.getInfo().getUninstallerPath() == null
				|| installData.getInstallPath() == null
		) {
			return Boolean.FALSE;
		}
		String uninstallerPath =
				installData.getInfo().getUninstallerPath().replaceAll("\\$INSTALL_PATH", installData.getInstallPath())
				+ File.separator
				+ "uninstaller.jar";
		logger.log(Level.FINE, logPrefix + "checking for presence of uninstaller, path=" + uninstallerPath);
		File uninstaller = new File(uninstallerPath);
		boolean exists = uninstaller.exists();
		logger.log(Level.FINE, logPrefix + "uninstaller.jar exists: " + (exists ? "yes" : "no"));
		return exists;
	}
}
