package com.sage.izpack;

import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.util.OsVersion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;

/**
 *
 * @author Franck DEPOORTERE
 */
public class RegistryHandlerX3 {
	private static final Logger logger = Logger.getLogger(RegistryHandlerX3.class.getName());

	private RegistryHandler registryHandler;
	private InstallData installData;

	public RegistryHandlerX3(RegistryHandler registryHandler, InstallData installData) {

		this.registryHandler = registryHandler;
		this.installData = installData;
	}

	public RegistryHandler getRegistryHandler() {
		return this.registryHandler;
	}
	
	
	public boolean isAdminSetup() {
		
		String isAdxAdmin = this.installData!= null ?  this.installData.getVariable("is-adxadmin"): null;
    	if (isAdxAdmin != null && isAdxAdmin.equalsIgnoreCase("true")) {

    		return true;
    	}

    	return false;
	}

        // return getAdxAdminDirPath();

	
	
	/**
	 * Check if AdxAdmin is installed Check the Registry if Windows
	 * 
	 * @return
	 * @throws NativeLibException
	 */
	public boolean adxadminProductRegistered() throws NativeLibException {

		String adxAdminPath = getAdxAdminDirPath();
		logger.log(Level.FINE, "RegistryHandlerX3.adxadminProductRegistered. adxAdminPath: " + adxAdminPath + "  result: " + (adxAdminPath != null));
		return (adxAdminPath != null);
	}

	/**
	 * 
	 * @return
	 * @throws NativeLibException
	 */
	public String getAdxAdminDirPath() throws NativeLibException {
		if (OsVersion.IS_UNIX) {
			return getAdxAdminPathUnix();
		}
		return getAdxAdminPathWin();
	}
	

	/**
	 * String keyName64Bits = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
	 * String keyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
	 * @return  C:\Sage\SafeX3\ADXADMIN
	 * @throws NativeLibException
	 */
	private String getAdxAdminPathWin() throws NativeLibException {
		String adxAdminPath = null;
		int oldVal = this.registryHandler.getRoot();
		this.registryHandler.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
		boolean exists64bits = this.registryHandler.keyExist(AdxCompHelper.ADXADMIN_REG_KeyName64Bits);
		boolean exists32bits = this.registryHandler.keyExist(AdxCompHelper.ADXADMIN_REG_KeyName32Bits);

		if (exists64bits) {
			adxAdminPath = this.registryHandler
					.getValue(AdxCompHelper.ADXADMIN_REG_KeyName64Bits, "ADXDIR").getStringData();
		} else if (exists32bits){
			adxAdminPath = this.registryHandler
					.getValue(AdxCompHelper.ADXADMIN_REG_KeyName32Bits, "ADXDIR").getStringData();
		}
		this.registryHandler.setRoot(oldVal);

		logger.log(Level.FINE, "RegistryHandlerX3.getAdxAdminPathWin. adxAdminPath: " + adxAdminPath);
		return adxAdminPath;
	}

	
	private String getAdxAdminPathUnix() {
		String adxAdminPath = null;

		java.io.File adxadmFile = new java.io.File("/sage/adxadm");
		if (!adxadmFile.exists()) {
			adxadmFile = new java.io.File("/adonix/adxadm");
			if (!adxadmFile.exists()) {
				return null;
			}
		}

		FileReader readerAdxAdmFile;
		try {
			readerAdxAdmFile = new FileReader(adxadmFile);
			BufferedReader buffread = new BufferedReader(readerAdxAdmFile);
			adxAdminPath = buffread.readLine();
			buffread.close();

			logger.log(Level.FINE, "RegistryHandlerX3.getAdxAdminPathUnix. adxAdminPath: " + adxAdminPath);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return adxAdminPath;

	}

}
