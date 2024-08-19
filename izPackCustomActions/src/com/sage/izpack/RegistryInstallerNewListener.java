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

	private static final Logger logger = Logger.getLogger(RegistryInstallerNewListener.class.getName());
	private final RegistryDefaultHandler myhandler;
    public RegistryInstallerNewListener(IUnpacker unpacker, VariableSubstitutor substitutor, InstallData installData,
			UninstallData uninstallData, Resources resources, RulesEngine rules, Housekeeper housekeeper,
			RegistryDefaultHandler handler) {
		super(unpacker, substitutor, installData, uninstallData, resources, rules, housekeeper, handler);
		this.myhandler = handler;
	}

	@Override
	public void afterPacks(List<Pack> packs, ProgressListener listener) {
		// logger.log(Level.FINE, "RegistryInstallerNewListener.afterPacks start");
		super.afterPacks(packs, listener);
		updateRegistry();
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
                //noinspection ResultOfMethodCallIgnored
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

	private void updateRegistry() {

		Variables variables = getInstallData().getVariables();
		String version = variables.get("app-version");
		if (version == null)
			version = variables.get("APP_VER");
		String appName = variables.get("UNINSTALL_NAME");
		if (appName == null || appName.isBlank()) {
			appName = variables.get("APP_NAME");
		}
		// String keyName = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + appName;
		String publisher = variables.get("Publisher");

		String keyName = RegistryHandler.UNINSTALL_ROOT + appName;

		RegistryHandler myHandlerInstance = myhandler.getInstance();
		try {
			myHandlerInstance.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
			myHandlerInstance.setUninstallName("");
			myHandlerInstance.setUninstallName(appName);

			if (myHandlerInstance.keyExist(keyName)) {
				updateEntry(myHandlerInstance, keyName, "DisplayVersion", version);
				updateEntry(myHandlerInstance, keyName, "Publisher", publisher);
			}
		} catch (NativeLibException e) {
			e.printStackTrace();
		}
	}

	private void updateEntry(RegistryHandler myHandlerInstance, String keyName, String entryName, String entryValue)
		throws NativeLibException {

		if (!myHandlerInstance.valueExist(keyName, entryName)) {
			myHandlerInstance.setValue(keyName, entryName, entryValue);
		} else {
			RegDataContainer contVal = myHandlerInstance.getValue(keyName, entryName);
			if (contVal != null) {
				String stringVal = contVal.getStringData();
				if (stringVal != null && entryValue != null && !stringVal.equals(entryValue)) {
					myHandlerInstance.setValue(keyName, entryName, entryValue);
				}
			}
		}
	}
}
