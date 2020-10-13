package com.sage.izpack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;

import com.coi.tools.os.win.RegDataContainer;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.core.resource.ResourceManager;

/*
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.installer.ResourceNotFoundException;
import com.izforge.izpack.util.os.RegistryHandler;
import com.sun.jna.platform.win32.Advapi32Util;

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;
*/

public class CheckProductAlreadyInstalled implements DataValidator {

	private static final String SPEC_FILE_NAME = "productsSpec.txt";

	protected String errMessage = "";
	protected String warnMessage = "";

	@Override
	public Status validateData(InstallData adata)
	// public Status validateData(AutomatedInstallData adata)
	{
		// open an input stream
		InputStream input = null;

		try {
			// input = ResourceManager.getInstance().getInputStream(SPEC_FILE_NAME);
			input = new ResourceManager().getInputStream(SPEC_FILE_NAME);

			if (input == null) {
				// spec file is missing
				errMessage = "specFileMissing";
				return Status.ERROR;
			} else {

				RegistryHandlerX3 rh = new RegistryHandlerX3();
				rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);

				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				StringBuilder out = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {

					line = line.trim(); //

					// if (Advapi32Util.registryKeyExists(RegistryHandler.HKEY_LOCAL_MACHINE,
					// "SOFTWARE\\Wow6432Node\\Sage\\"+line)
					// || Advapi32Util.registryKeyExists(RegistryHandler.HKEY_LOCAL_MACHINE,
					// "SOFTWARE\\Sage\\"+line))
					if (rh.keyExist("SOFTWARE\\Wow6432Node\\Sage\\" + line) || rh.keyExist("SOFTWARE\\Sage\\" + line)) {
						// TODO: FRDEPO
						// errMessage = String.format(adata.langpack.getString("errIsProductFound"),
						// line);
				        // InputStream customlangPack = getClass().getResourceAsStream("eng.xml");
				        // installData.setMessages(new LocaleDatabase(langPack, Mockito.mock(Locales.class)));
				        // LocaleDatabase localdb = new LocaleDatabase(customlangPack, null);
				        // localdb.getString("errIsProductFound");
						errMessage = String.format(ResourceBundle.getBundle("messages", adata.getLocale()).getString("errIsProductFound"), line);
						return Status.ERROR;
					} else if (rh.keyExist(RegistryHandler.UNINSTALL_ROOT + line)) {
						// TODO: FRDEPO
						// warnMessage = String.format(adata.langpack.getString("compFoundAskUpdate"),
						// line);
						warnMessage = String.format(ResourceBundle.getBundle("messages", adata.getLocale()).getString("compFoundAskUpdate"), line);
						// String oldInstallPath =
						// Advapi32Util.registryGetStringValue(RegistryHandler.HKEY_LOCAL_MACHINE,
						// RegistryHandler.UNINSTALL_ROOT+line, "DisplayIcon");
						RegDataContainer oldInstallPath = rh.getValue(RegistryHandler.UNINSTALL_ROOT + line,
								"DisplayIcon");
						adata.setInstallPath(oldInstallPath.getStringData().substring(0,
								oldInstallPath.getStringData().indexOf("Uninstaller") - 1));
						// Debug.trace("modification installation");
						// Debug.trace("old path applied :"+oldInstallPath);
						// adata.setVariable(InstallData.MODIFY_INSTALLATION, "true");
						adata.setVariable(InstallData.MODIFY_INSTALLATION, "true");

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
