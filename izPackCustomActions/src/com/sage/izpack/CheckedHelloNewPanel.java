package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
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

	private ResourcesHelper _resourceHelper = null;

	private static final long serialVersionUID = 1737042770727953387L; // 1737042770727953387L

	private RegistryHelper _registryHelper;
	private RegistryHandler _registryHandler;

	public CheckedHelloNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			RegistryDefaultHandler handler, Log log) throws Exception {
		super(panel, parent, installData, resources, handler, log);

		_resourceHelper = new ResourcesHelper(installData, resources);
		_resourceHelper.mergeCustomMessages();
		_registryHelper = new RegistryHelper(handler, installData);
		_registryHandler = handler.getInstance();

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

			if (!InstallationInformationHelper.hasAlreadyReadInformation(this.installData)) {
				InstallationInformationHelper.readInformation(installData, resources);
			} else {
				logger.log(Level.FINE,
						"CheckedHelloNewPanel ReadInstallationInformation: "
								+ this.installData.getInfo().isReadInstallationInformation() + " AlreadyRead: "
								+ InstallationInformationHelper.hasAlreadyReadInformation(this.installData));
			}

		}
	}

	/*
	 * We override this method to avoid the alert message sent by
	 * setUniqueUninstallKey()
	 */
	@Override
	public void panelActivate() {
		if (abortInstallation) {

			// test whether multiple install is allowed
			// String disallowMultipleInstall =
			// installData.getVariable("CheckedHelloNewPanel.disallowMultipleInstance");
			String allowMultipleInstall = installData.getVariable("CheckedHelloNewPanel.allowMultipleInstance");

			// multiple install is allowed
			// <variable name="CheckedHelloNewPanel.allowMultipleInstance" value="true"/>
			if (Boolean.TRUE.toString().equalsIgnoreCase(allowMultipleInstall)) {

				logger.log(Level.FINE, "CheckedHelloNewPanel allowMultipleInstance=true");

				parent.lockNextButton();
				try {
					// if (multipleInstall()) {
					setUniqueUninstallKey();
					abortInstallation = false;
					parent.unlockNextButton();
					// }
				} catch (Exception exception) {
					logger.log(Level.WARNING, exception.getMessage(), exception);
				}
			}
			// Default behavior: update mode
			// or <variable name="CheckedHelloNewPanel.allowMultipleInstance"
			// value="false"/>
			else {
				// String allowUpdateMode =
				// installData.getVariable("CheckedHelloPanel.allowUpdateMode");
				logger.log(Level.FINE, "CheckedHelloNewPanel allowMultipleInstance=false (updatemode)");

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
		}

		Variables variables = this.installData.getVariables();
		installData.setVariable("UNINSTALL_NAME", variables.get("APP_NAME"));
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
		logger.log(Level.FINE, "CheckedHelloNewPanel Set " + InstallData.MODIFY_INSTALLATION + ": " + result);
		return result;
	}

	/*
	 * X3-240420 : Wrong message when updating the console This method should only
	 */
	@Override
	public String getString(String key) {

		ResourcesHelper helper = new ResourcesHelper(this.installData, this.getResources());
		String result = helper.getCustomString(key);

		if (result == null || !result.equals(key)) {
			result = super.getString(key);
		}
		return result;
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

	/**
	 * @throws Exception
	 */
	private void setUniqueUninstallKey() throws Exception {
		// Let us play a little bit with the registry again...
		// Now we search for an unique uninstall key.
		// First we need a handler. There is no overhead at a
		// second call of getInstance, therefore we do not buffer
		// the handler in this class.

		// RegistryHandler rh = RegistryDefaultHandler.getInstance();
		int oldVal = _registryHandler.getRoot(); // Only for security...
		// We know, that the product is already installed, else we
		// would not in this method. First we get the
		// "default" uninstall key.
		if (oldVal > 100) // Only to inhibit warnings about local variable never read.
		{
			return;
		}
		String uninstallName = _registryHelper.getUninstallName();
		int uninstallModifier = 1;
		while (true) {
			if (uninstallName == null) {
				break; // Should never be...
			}
			// Now we define a new uninstall name.
			String newUninstallName = uninstallName + "(" + Integer.toString(uninstallModifier) + ")";
			// Then we "create" the reg key with it.
			String keyName = RegistryHandler.UNINSTALL_ROOT + newUninstallName;
			_registryHandler.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
			if (!_registryHandler.keyExist(keyName)) { // That's the name for which we searched.
														// Change the uninstall name in the reg helper.
				_registryHandler.setUninstallName(newUninstallName);
				// Now let us inform the user.
				emitNotification(getString("CheckedHelloNewPanel.infoOverUninstallKey") + newUninstallName);
				// Now a little hack if the registry spec file contains
				// the pack "UninstallStuff".
				break;
			}
			uninstallModifier++;
		}
	}

}
