package com.sage.izpack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
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
	
	// private com.izforge.izpack.api.data.InstallData installData;
	// private RegistryHandler registryHandler;

	
	/*
	public CheckProductAlreadyInstalled( RegistryDefaultHandler handler) {
		super();
		this.registryHandler = handler.getInstance();
	}
	*/

	@Override
	public Status validateData(InstallData installData) {
		// open an input stream
		InputStream input = null;
	
		try {
			// this.registryHandler.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);

			// logger.log(Level.FINE, "registryHandler:" + registryHandler);
			// logger.info("registryHandler:" + registryHandler);

			//if (registryHandler.keyExist(RegistryHandler.UNINSTALL_ROOT + installData.getVariable("APP_NAME"))) {
				// logger.log(Level.FINE, "APP_NAME key " + RegistryHandler.UNINSTALL_ROOT + installData.getVariable("APP_NAME") + " found in registry. Set '" + InstallData.MODIFY_INSTALLATION + "': true");
			//	logger.info("APP_NAME key " + RegistryHandler.UNINSTALL_ROOT + installData.getVariable("APP_NAME") + " found in registry. Set '" + InstallData.MODIFY_INSTALLATION + "': true");

			//	installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
			//}

			
			input = new ResourceManager().getInputStream(SPEC_FILE_NAME);
			// logger.log(Level.FINE, "input: " + input);
			logger.info("input: " + input);

			
			if (input == null) {
				// spec file is missing
				errMessage = "specFileMissing";

				// logger.log(Level.FINE, "input: " + errMessage);
				logger.info("input: " + errMessage);

				return Status.ERROR;
			} else {


				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				StringBuilder out = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {

					// logger.log(Level.FINE, "line:" + line);
					logger.info("line:" + line);

					line = line.trim(); //

/*
					if (registryHandler.keyExist(RegistryHandler.UNINSTALL_ROOT + line)) {
						warnMessage = String.format(ResourceBundle.getBundle("messages", installData.getLocale())
								.getString("compFoundAskUpdate"), line);

						RegDataContainer oldInstallPath = registryHandler.getValue(RegistryHandler.UNINSTALL_ROOT + line,
								"DisplayIcon");
						installData.setInstallPath(oldInstallPath.getStringData().substring(0,
								oldInstallPath.getStringData().indexOf("Uninstaller") - 1));
						installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");

						logger.log(Level.FINE, "old path applied: " + oldInstallPath);
						logger.log(Level.FINE, "set variable " + InstallData.MODIFY_INSTALLATION + ": true");

						
						return Status.WARNING;
					}
					*/
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
