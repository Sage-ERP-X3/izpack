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
			logger.info("input: " + input);

			if (input == null) {
				// spec file is missing
				errMessage = "specFileMissing";
				logger.log(Level.FINE, "input: " + errMessage);
				return Status.ERROR;
			} else 
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				StringBuilder out = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {

					logger.info("line:" + line);
					line = line.trim();

					this.registryHandler.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
					if (registryHandler.keyExist(RegistryHandler.UNINSTALL_ROOT + line)) {
						logger.log(Level.FINE, "MODIFY_INSTALLATION=true - Registry key exists:" + RegistryHandler.UNINSTALL_ROOT
								+ line);
						RegDataContainer oldInstallPath = registryHandler.getValue(RegistryHandler.UNINSTALL_ROOT + line, "DisplayIcon");
						String path = oldInstallPath.getStringData().substring(0, oldInstallPath.getStringData().indexOf("Uninstaller") - 1);
						installData.setInstallPath(path);
						this.warnMessage = "compFoundAskUpdate";
						installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
						logger.log(Level.FINE, "Detected path: " + path);
						return Status.WARNING;
					} else {
						logger.log(Level.FINE, "MODIFY_INSTALLATION=false - Registry key not found:"
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
