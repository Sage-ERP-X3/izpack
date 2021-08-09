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
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.event.RegistryUninstallerListener;

/*
 * This class fix the bug when un-installing a product, sometimes, the Registry is not cleaned (Ex: X3-237732)
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

		logger.log(Level.FINE, "RegistryUninstallerNewListener.afterDelete. File : " + file);

		super.afterDelete(file);
	}

	@Override
	public void beforeDelete(List<File> files, ProgressListener listener) {

		// deleteRegistry();
		logger.log(Level.FINE, "RegistryUninstallerNewListener.beforeDelete.  ");

		super.beforeDelete(files, listener);
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

		logger.log(Level.FINE, "RegistryUninstallerNewListener.afterDelete.  ");

		super.afterDelete(files, listener);

	}

	private void deleteRegistry() {

		RegistryHandler myHandlerInstance = myhandler.getInstance();
		String unInstallName = myHandlerInstance.getUninstallName();
		// String keyName =
		// "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Sage X3 Management Console";
		// RegistryHandler.UNINSTALL_ROOT =
		// "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\"
		String keyName = RegistryHandler.UNINSTALL_ROOT + unInstallName;
		if (unInstallName == null) {
			logger.log(Level.FINE, "Error in deleteRegistry: getUninstallName() is empty");
			// myHandlerInstance.setUninstallName("Sage X3 Management Console");
			return;
		}

		logger.log(Level.ALL, "UninstallName Registry key " + keyName);

		try {
			myHandlerInstance.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);

			if (myHandlerInstance.keyExist(keyName)) {
				myHandlerInstance.deleteValue(keyName, "DisplayVersion");
				// myHandlerInstance.deleteKey(keyName + "\\DisplayVersion");
				myHandlerInstance.deleteKey(keyName);
				logger.log(Level.FINE, "Registry key " + keyName + " deleted");
			}
		} catch (NativeLibException e) {
			e.printStackTrace();
			logger.log(Level.FINE, "Error, registry key " + keyName + " NOT deleted");
		}
	}

}
