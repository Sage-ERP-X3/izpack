package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.checkedhello.CheckedHelloPanel;
import com.izforge.izpack.panels.checkedhello.RegistryHelper;
import com.izforge.izpack.api.resource.Locales;

/*
* @author Franck DEPOORTERE
*/
public class CheckedHelloNewPanel extends CheckedHelloPanel {

	private static Logger logger = Logger.getLogger(CheckedHelloNewPanel.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1737042770727953387L; // 1737042770727953387L

	private RegistryHelper _registryHelper;

	public CheckedHelloNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			RegistryDefaultHandler handler, Log log) throws Exception {
		super(panel, parent, installData, resources, handler, log);

		_registryHelper = new RegistryHelper(handler, installData);
		String path = _registryHelper.getInstallationPath();
		// Update case :
		if (path != null) {
			installData.setVariable("TargetPanel.dir.windows", path);
			logger.log(Level.FINE, "CheckedHelloNewPanel Set TargetPanel.dir.windows: " + path);

			installData.setVariable(InstallData.INSTALL_PATH, path);
			logger.log(Level.FINE, "CheckedHelloNewPanel Set INSTALL_PATH: " + path);
		}

		// Update case : read .installationinformation
		if (path != null && installData.getInfo().isReadInstallationInformation()) {

			InstallationInformationHelper.readInformation(installData);

		}
	}

	/*
	 * We override this method to avoid the alert message sent by
	 * setUniqueUninstallKey()
	 */
	@Override
	public void panelActivate() {
		if (abortInstallation) {
			parent.lockNextButton();
			try {
				if (multipleInstall()) {
					// setUniqueUninstallKey();
					abortInstallation = false;
					parent.unlockNextButton();
				}
				// if we let the "else", izpack create a unique Key after each installation, and
				// the registry is not uninstalled
				installData.getInfo().setUninstallerPath(null);
				installData.getInfo().setUninstallerName(null);
				installData.getInfo().setUninstallerCondition("uninstaller.nowrite");
			} catch (Exception exception) {
				logger.log(Level.WARNING, exception.getMessage(), exception);
			}
		}

		Variables variables = this.installData.getVariables();
		installData.setVariable("UNINSTALL_NAME", variables.get("APP_NAME"));
		// installData.setVariable("UNINSTALL_NAME",
		// _registryHelper.getUninstallName());
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
		logger.log(Level.FINE, "CheckedHelloNewPanel Set " + InstallData.MODIFY_INSTALLATION + ": true");
		return result;
	}

	/*
	 * X3-240420 : Wrong message when updating the console This method should only
	 */
	@Override
	public String getString(String key) {

		ResourcesHelper helper = new ResourcesHelper(this.installData, this.getResources());
		String result = helper.getCustomString(key);

		if (result == null) {
			result = super.getString(key);
		}
		return result;

		/*
		 * customResourcesPath = "/com/sage/izpack/langpacks/" +
		 * installData.getLocaleISO3() + ".xml"; String result = null; try { result =
		 * customResources.get(key); } catch (Exception ex) { logger.log(Level.FINE,
		 * "CheckedHelloNewPanel Cannot get resource " + key + " " +
		 * customResourcesPath);
		 * 
		 * } if (result == null) { result = super.getString(key); } return result;
		 */
	}

	/**
	 * X3-240420 : Wrong message when updating the console This method should only
	 * be called if this product was already installed. It resolves the install path
	 * of the first already installed product and asks the user whether to install
	 * twice or not.
	 *
	 * @return whether a multiple Install should be performed or not.
	 * @throws NativeLibException for any native library error
	 */
	@Override
	protected boolean multipleInstall() throws NativeLibException {
		String path = _registryHelper.getInstallationPath();
		if (path == null) {
			path = "<not found>";
		}

		String noLuck = getString("CheckedHelloPanel.productAlreadyExist0") + path + ". "
				+ getString("CheckedHelloPanel.productAlreadyExist1");

		return (askQuestion(getString("installer.warning"), noLuck,
				AbstractUIHandler.CHOICES_YES_NO) == AbstractUIHandler.ANSWER_YES);
	}

}
