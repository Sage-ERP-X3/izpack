package com.sage.izpack;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.event.RegistryUninstallerListener;

/*
 * This class fix the bug when uninstalling a product, sometimes, the Registry is not cleaned (Ex: X3-237732)
 *  
 *  Ex: Delete HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + getUninstallName();
 *  
 *  @author Franck DEPOORTERE
 */
public class RegistryUninstallerNewListener extends RegistryUninstallerListener {

	private static final Logger logger = Logger.getLogger(RegistryUninstallerNewListener.class.getName());

	private RegistryDefaultHandler myhandler;
	public RegistryUninstallerNewListener(RegistryDefaultHandler handler, Resources resources, Messages messages) {
		super(handler, resources, messages);
		
		myhandler = handler;
	}

	/**
	 * Invoked after a file is deleted.
	 *
	 * @param file the file which was deleted
	 * @throws IzPackException for any error
	 */
	@Override
	public void afterDelete(File file) {

	}

	/**
	 * Invoked after files are deleted.
	 *
	 * @param files    the files which where deleted
	 * @param listener the progress listener
	 * @throws IzPackException for any error
	 */
	@Override
	public void afterDelete(List<File> files, ProgressListener listener) {

		String unInstallName = this.myhandler.getInstance().getUninstallName();
		// String keyName = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Sage X3 Management Console";
		String keyName = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + unInstallName;

		logger.log(Level.ALL, "unInstallName Registry key " + unInstallName);
		
		RegistryHandlerX3 rh = new RegistryHandlerX3();
		if (rh != null) {
			try {
				if (!rh.keyExist(keyName)) {
					rh.deleteKey(keyName);
					logger.log(Level.FINE, "Registry key " + keyName + "deleted");
				}
			} catch (NativeLibException e) {
				e.printStackTrace();
				logger.log(Level.FINE, "Error, registry key " + keyName + " NOT deleted");
			}
		}
	}

}
