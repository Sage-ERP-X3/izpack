package com.sage.izpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.util.OsVersion;

/**
*
* @author Franck DEPOORTERE
*/
public class AdxCompHelper {
	
	private static final Logger logger = Logger.getLogger(AdxCompHelper.class.getName());

	/**
	 * adxinstalls.xml
	 */
	public static final String ADX_INSTALL_FILENAME = "adxinstalls.xml";
	/*
	 * SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN
	 */
	public static final String ADXADMIN_REG_KeyName64Bits = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
	/*
	 * SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN
	 */
	public static final String ADXADMIN_REG_KeyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";

	private com.izforge.izpack.api.data.InstallData installData;
	private RegistryHandler registryHandler;

	
	public AdxCompHelper(RegistryHandler registryHandler, com.izforge.izpack.api.data.InstallData installData) {
		this.registryHandler = registryHandler;
		this.installData = installData;
	}


	/**
	 * 
	 * @param dirAdxDir
	 * @return Ex: C:\Sage\SafeX3\ADXADMIN\inst\adxinstalls.xml
	 */
	public java.io.File getAdxInstallFile(java.io.File dirAdxDir) {

		StringBuilder adxInstallBuilder = new StringBuilder();
		adxInstallBuilder.append(dirAdxDir.getAbsolutePath());
		adxInstallBuilder.append(File.separator);
		adxInstallBuilder.append("inst");
		adxInstallBuilder.append(File.separator);
		adxInstallBuilder.append(AdxCompHelper.ADX_INSTALL_FILENAME);

		return new java.io.File(adxInstallBuilder.toString());
	}

	
	/*
	 * we need to find adxadmin path
	 */
	public String getAdxAdminPath() throws NativeLibException, Exception, FileNotFoundException, IOException {

		String strAdxAdminPath = "";

		logger.log(Level.FINE,
				"AdxCompHelper  Init registry installData Locale: " + this.installData.getLocaleISO2());
		logger.log(Level.FINE,
				"AdxCompHelper  Init registry getInstallPath: " + this.installData.getInstallPath());

		RegistryHandlerX3 rh = new RegistryHandlerX3(this.registryHandler);
		if (this.registryHandler != null && rh != null) {

			boolean adxAdminRegistered = rh.adxadminProductRegistered();
			logger.log(Level.FINE, "AdxCompHelper  Init RegistryHandlerX3. adxadminProductRegistered: "
					+ adxAdminRegistered);

			// Test adxadmin is already installed. Read registry
			// String keyName64Bits = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
			// String keyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
			if (adxAdminRegistered) {

				int oldVal = this.registryHandler.getRoot();
				this.registryHandler.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);

				String keyName = ADXADMIN_REG_KeyName64Bits;
				if (!this.registryHandler.valueExist(keyName, "ADXDIR"))
					keyName = ADXADMIN_REG_KeyName32Bits;
				if (!this.registryHandler.valueExist(keyName, "ADXDIR"))
					// <str id="adxadminNoAdxDirReg" txt="A previous installation of the adxadmin
					// administration runtime was detected in the registry but the setup is unable
					// to find the installation path, some registry keys are missing !"/>
					// throw new
					// Exception(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("adxadminNoAdxDirReg"));
					throw new Exception(
							"A previous installation of the adxadmin administration runtime was detected in the registry but the setup is unable to find the installation path, some registry keys are missing.");

				// fetch ADXDIR path
				strAdxAdminPath = this.registryHandler.getValue(keyName, "ADXDIR").getStringData();

				logger.log(Level.FINE,
						"AdxCompHelper  ADXDIR path: " + strAdxAdminPath + "  Key: " + keyName);

				// free RegistryHandler
				this.registryHandler.setRoot(oldVal);
			} else {
				// else throw new Exception(langpack.getString("adxadminNotRegistered"));
				// <str id="adxadminNotRegistered" txt="You must install an adxadmin
				// administration runtime first. Exiting now."/>
				// String warnMessage = String.format(ResourceBundle.getBundle("messages",
				// installData.getLocale()).getString("adxadminNotRegistered"), line);
				// throw new
				// Exception(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("adxadminNotRegistered"));
				throw new Exception(ResourcesHelper.getCustomPropString("adxadminNotRegistered"));
				// throw new Exception("You must install an adxadmin administration runtime first. Exiting now.");
			}

		} else {
			logger.log(Level.FINE, "AdxCompHelper - Could not get RegistryHandler !");

			// else we are on a os which has no registry or the needed dll was not bound to
			// this installation.
			// In both cases we forget the "already exist" check.

			// test adxadmin sous unix avec /adonix/adxadm ?
			if (OsVersion.IS_UNIX) {
				java.io.File adxadmFile = new java.io.File("/sage/adxadm");
				if (!adxadmFile.exists()) {
					adxadmFile = new java.io.File("/adonix/adxadm");
					if (!adxadmFile.exists()) {
						// throw new Exception(langpack.getString("adxadminNotRegistered"));
						throw new Exception(ResourcesHelper.getCustomPropString("adxadminNotRegistered"));
								// ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages")
								//.getString("adxadminNotRegistered"));
					}
				}

				FileReader readerAdxAdmFile = new FileReader(adxadmFile);
				BufferedReader buffread = new BufferedReader(readerAdxAdmFile);
				strAdxAdminPath = buffread.readLine();
			}

		}
		return strAdxAdminPath;
	}
}
