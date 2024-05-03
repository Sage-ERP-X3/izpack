package com.sage.izpack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.coi.tools.os.win.RegDataContainer;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.core.resource.ResourceManager;

/*
* @author Franck DEPOORTERE
*/
public class CheckProductAlreadyInstalled implements DataValidator {

	private static Logger logger = Logger.getLogger(CheckProductAlreadyInstalled.class.getName());
	private static final String SPEC_FILE_NAME = "productsSpec.txt";

	protected String errMessage = "";
	protected String warnMessage = "";
	private RegistryHandler registryHandler;

	public CheckProductAlreadyInstalled(RegistryDefaultHandler handler) {
		super();
		this.registryHandler = handler.getInstance();
	}

	@Override
	public Status validateData(InstallData installData) {
		// open an input stream
		InputStream input = null;

		try {
			
			input = new ResourceManager().getInputStream(SPEC_FILE_NAME);
			logger.log(Level.FINE, "CheckProductAlreadyInstalled input: " + input);

			if (input == null) {
				// spec file is missing
				errMessage = "specFileMissing";
				logger.log(Level.FINE, "CheckProductAlreadyInstalled  input: " + errMessage);
				return Status.ERROR;
			} else 
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				StringBuilder out = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {

					logger.log(Level.FINE,"line:" + line);
					line = line.trim();

					this.registryHandler.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
					if (registryHandler.keyExist(RegistryHandler.UNINSTALL_ROOT + line)) {
						logger.log(Level.FINE, "CheckProductAlreadyInstalled  MODIFY_INSTALLATION=true - Registry key exists:" + RegistryHandler.UNINSTALL_ROOT
								+ line);
						RegDataContainer oldInstallPath = registryHandler.getValue(RegistryHandler.UNINSTALL_ROOT + line, "DisplayIcon");
						String path = oldInstallPath.getStringData().substring(0, oldInstallPath.getStringData().indexOf("Uninstaller") - 1);
						installData.setInstallPath(path);
						installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
						logger.log(Level.FINE, "CheckProductAlreadyInstalled  Detected path: " + path);
						
						//  INSTALLER_GUI = 0, INSTALLER_AUTO = 1, INSTALLER_CONSOLE = 2;
						// if (Installer.getInstallerMode() == Installer.INSTALLER_AUTO) {
						// X3-301654: [ ERROR: compFoundAskUpdate ] We avoid any warning or Error in batch mode
						//	return Status.OK;
						//}
						// 	<str id="compFoundAskUpdate" txt="An earlier version of this component has been found on this host, do you want to update this installation ?"/>
						// this.warnMessage = "compFoundAskUpdate";
						// return Status.WARNING;
						
						// X3-302700: Print Server 2.29 installer displays message twice when updating
						// This warning is already managed by CheckedHelloNewPanel.java
						return Status.OK;
						
					} else {
						logger.log(Level.FINE, "CheckProductAlreadyInstalled  MODIFY_INSTALLATION=false - Registry key not found:"
								+ RegistryHandler.UNINSTALL_ROOT + line);
					}
					
				}
				reader.close();
			}

		} catch (Exception ex) {
			errMessage = ex.getMessage();
			return Status.ERROR;
		}

		return Status.OK;
	}

	public String getErrorMessageId() {
		return errMessage;
	}

	public String getWarningMessageId() {
		return warnMessage;
	}

	public boolean getDefaultAnswer() {
		// unfortunately we can't say yes by default
		return false;
	}

}
