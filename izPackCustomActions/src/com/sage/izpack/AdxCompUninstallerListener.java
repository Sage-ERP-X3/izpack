package com.sage.izpack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;

/*
 * Note this class work on Linux for the moment, due to a reference to the RegistryDefaultHandler class 
 * Version: IzPack 5.2.0 - 2023.12.01
 * Caused by: java.lang.ClassNotFoundException: com.coi.tools.os.win.MSWinConstants
 * We need to use AdxCompUninstallerListenerLinux for the moment
 * 
 * @author Franck DEPOORTERE
 */
public class AdxCompUninstallerListener extends AdxCompUninstallerListenerCommon {

	private static final Logger logger = Logger.getLogger(AdxCompUninstallerListener.class.getName());
	private static String LogPrefix = "AdxCompUninstallerListener - ";
	private final RegistryHandler registryHandler;

	public AdxCompUninstallerListener(RegistryDefaultHandler handler, Resources resources, Messages messages, Prompt prompt) {

		// Doesn't seem to be possible to get InstallData installData, UninstallData
		// uninstallData in this type of class: the Uninstall.jar program will crash during launch.
		super(resources, messages, prompt);
		this.registryHandler = handler.getInstance();
	}

	
	@Override
	protected Document getAdxInstallDocument() throws FileNotFoundException, NativeLibException, IOException, Exception {
		AdxCompHelper adxCompHelper = new AdxCompHelper(this.registryHandler, null);
		Document adxInstallXmlDoc = adxCompHelper.getAdxInstallDocument();
		return adxInstallXmlDoc;
	}	


	@Override
	protected String getAdxAdminPath() throws FileNotFoundException, NativeLibException, IOException, Exception {
		AdxCompHelper adxCompHelper = new AdxCompHelper(this.registryHandler, null);
		return adxCompHelper.getAdxAdminPath();
	}

	@Override
	protected File getAdxInstallFile(File adxAdminDir) {
		AdxCompHelper adxCompHelper = new AdxCompHelper(this.registryHandler, null);
		return adxCompHelper.getAdxInstallFile(adxAdminDir);
	}
	

}
