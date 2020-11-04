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

	@Override
	public Status validateData(InstallData installData) {
		// open an input stream
		InputStream input = null;

		try {
			RegistryHandlerX3 registryHandler = new RegistryHandlerX3();
			registryHandler.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);

			logger.log(Level.FINEST, "registryHandler:" + registryHandler);

			if (registryHandler.keyExist(RegistryHandler.UNINSTALL_ROOT + installData.getVariable("APP_NAME"))) {
				logger.log(Level.FINEST, "APP_NAME key " + RegistryHandler.UNINSTALL_ROOT + installData.getVariable("APP_NAME") + " found in registry. Set '"
						+ InstallData.MODIFY_INSTALLATION + "': true");

				installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
			}

			
			input = new ResourceManager().getInputStream(SPEC_FILE_NAME);
			logger.log(Level.FINEST, "input: " + input);

			
			if (input == null) {
				// spec file is missing
				errMessage = "specFileMissing";

				logger.log(Level.FINEST, "input: " + errMessage);

				return Status.ERROR;
			} else {


				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				StringBuilder out = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {

					logger.log(Level.FINEST, "line:" + line);

					line = line.trim(); //

					// if (Advapi32Util.registryKeyExists(RegistryHandler.HKEY_LOCAL_MACHINE,
					// "SOFTWARE\\Wow6432Node\\Sage\\"+line)
					// || Advapi32Util.registryKeyExists(RegistryHandler.HKEY_LOCAL_MACHINE,
					// "SOFTWARE\\Sage\\"+line))
					// if (rh.keyExist("SOFTWARE\\Wow6432Node\\Sage\\" + line) ||
					// rh.keyExist("SOFTWARE\\Sage\\" + line)) {
					// errMessage = String.format(adata.langpack.getString("errIsProductFound"),
					// line);
					// InputStream customlangPack = getClass().getResourceAsStream("eng.xml");
					// installData.setMessages(new LocaleDatabase(langPack,
					// Mockito.mock(Locales.class)));
					// LocaleDatabase localdb = new LocaleDatabase(customlangPack, null);
					// localdb.getString("errIsProductFound");
					// errMessage = String.format(ResourceBundle.getBundle("messages",
					// adata.getLocale()).getString("errIsProductFound"), line);
					// return Status.ERROR;
					// } else
					if (registryHandler.keyExist(RegistryHandler.UNINSTALL_ROOT + line)) {
						warnMessage = String.format(ResourceBundle.getBundle("messages", installData.getLocale())
								.getString("compFoundAskUpdate"), line);

						RegDataContainer oldInstallPath = registryHandler.getValue(RegistryHandler.UNINSTALL_ROOT + line,
								"DisplayIcon");
						installData.setInstallPath(oldInstallPath.getStringData().substring(0,
								oldInstallPath.getStringData().indexOf("Uninstaller") - 1));
						installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");

						logger.log(Level.FINEST, "old path applied: " + oldInstallPath);
						logger.log(Level.FINEST, "set variable " + InstallData.MODIFY_INSTALLATION + ": true");

						
						return Status.WARNING;
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
