package com.sage.izpack;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.coi.tools.os.win.RegDataContainer;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.util.Housekeeper;

/*
 * 
 * @author Franck DEPOORTERE
 */
public class RegistryInstallerNewListener extends com.izforge.izpack.event.RegistryInstallerListener {

	private static Logger logger = Logger.getLogger(RegistryInstallerNewListener.class.getName());
	private final RegistryDefaultHandler myhandler;
	private static final String LogPrefix = "RegistryInstallerNewListener - ";

	public RegistryInstallerNewListener(IUnpacker unpacker, VariableSubstitutor substitutor, InstallData installData,
			UninstallData uninstallData, Resources resources, RulesEngine rules, Housekeeper housekeeper,
			RegistryDefaultHandler handler) {
		super(unpacker, substitutor, installData, uninstallData, resources, rules, housekeeper, handler);
		myhandler = handler;
	}

	@Override
	public void afterPacks(List<Pack> packs, ProgressListener listener) {

		// logger.log(Level.FINE, "RegistryInstallerNewListener.afterPacks start");

		updateRegistry();

		super.afterPacks(packs, listener);

		// Fix the bug when un-installing a product, sometimes, the Registry
		// is not cleaned and on old file .installationinformation from a former setup
		// can disturb the process. (Ex: X3-237732)
		// readinstallationinformation
		// writeinstallationinformation
		if (!this.getInstallData().getInfo().isReadInstallationInformation()
				&& !this.getInstallData().getInfo().isWriteInstallationInformation()) {
			deleteInstallInformation();
		}

		// logger.log(Level.FINE, "RegistryInstallerNewListener.afterPacks end");
	}

	/*
	 * While we update the Console, for ex from the previous 2.49 to 2.50,
	 * Sometimes, the version in registry key is not updated.
	 * Computer\HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\
	 * Uninstall\Sage X3 Management Console\DisplayVersion
	 */
	private void updateRegistry() {

		InstallationInformationHelper.restoreNewVersion(getInstallData());
		Variables variables = getInstallData().getVariables();
		String version = variables.get("app-version");
		if (version == null)
			version = variables.get("APP_VER");
		String appName = variables.get("APP_NAME");
		// String keyName = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + appName;
		String publisher = variables.get("publisher");

		String keyName = RegistryHandler.UNINSTALL_ROOT + appName;

		logger.log(Level.FINE, LogPrefix + "updateRegistry   Updating DisplayVersion, Registry path " + keyName
				+ " key: " + "DisplayVersion: " + version  + " Publisher:" + publisher);


		RegistryHandler myHandlerInstance = myhandler.getInstance();
		try {
			myHandlerInstance.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);			
			myHandlerInstance.setUninstallName("");
			myHandlerInstance.setUninstallName(appName);
			
			if (myHandlerInstance.keyExist(keyName)) {
				RegDataContainer cont = myHandlerInstance.getValue(keyName, "DisplayVersion");
				if (cont != null) {
					String displayVersionVal = cont.getStringData();
					if (displayVersionVal != null && version != null && displayVersionVal != version) {
						myHandlerInstance.setValue(keyName, "DisplayVersion", version);

						logger.log(Level.FINE, LogPrefix + "updateRegistry   DisplayVersion updated, Registry path "
								+ keyName + " key: " + "DisplayVersion" + " value: " + version);
					}
				}

				RegDataContainer contPublisher = myHandlerInstance.getValue(keyName, "Publisher");
				if (contPublisher != null) {
					String publisherVal = contPublisher.getStringData();
					if (publisherVal != null && publisher != null && publisherVal != publisher) {
						myHandlerInstance.setValue(keyName, "Publisher", publisher);

						logger.log(Level.FINE, LogPrefix + "updateRegistry   Publisher updated, Registry path "
								+ keyName + " key: " + "Publisher" + " value: " + publisher);
					}
				}

			}
		} catch (NativeLibException e) {
			e.printStackTrace();
			logger.log(Level.WARNING, LogPrefix + "updateRegistry   Error while update registry path " + keyName
					+ " Key: DisplayVersion, value: " + version);
		}
	}

	/*
	 * This class fix the bug when un-installing a product, sometimes, the Registry
	 * is not cleaned and on old file .installationinformation from a former setup
	 * can disturb the process. (Ex: X3-237732)
	 */
	private void deleteInstallInformation() {
		InstallData installData = getInstallData();
		if (!installData.getInfo().isWriteInstallationInformation()
				&& !installData.getInfo().isReadInstallationInformation()) {

			String installDir = installData.getInstallPath();
			String installInformationFileName = installDir + File.separator + InstallData.INSTALLATION_INFORMATION;
			File installationInfo = new File(installInformationFileName);
			if (installationInfo.exists()) {
				installationInfo.delete();
				logger.log(Level.FINE, "File " + installInformationFileName + " deleted.");
			}
		}
	}

	@Override
	protected String getUninstallName() {
		Variables variables = getInstallData().getVariables();
		// We had to override this method to remove APP_VER
		// return variables.get("APP_NAME") + " " + variables.get("APP_VER");
		return variables.get("APP_NAME");
	}

}
