package com.sage.izpack;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.exception.WrappedNativeLibException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.event.RegistryUninstallerListener;

/*
 * This class fixes the bug when un-installing a product, sometimes, the Registry is not cleaned (Ex: X3-237732)
 *  
 *  Ex: Delete HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + getUninstallName();
 *  
 *  @author Franck DEPOORTERE
 */
public class RegistryUninstallerNewListener extends RegistryUninstallerListener {

	private static final Logger logger = Logger.getLogger(RegistryUninstallerNewListener.class.getName());
	private static final String LogPrefix = "RegistryUninstallerNewListener - ";
	private RegistryDefaultHandler myhandler;
	private final Prompt prompt;
	private final Resources resources;
	private final Messages messages;

	public RegistryUninstallerNewListener(RegistryDefaultHandler handler, Resources resources, Messages messages,
			Prompt prompt) {
		super(handler, resources, messages);

		this.myhandler = handler;
		this.resources = resources;
		this.messages = messages;
		this.prompt = prompt;
	}

	private AbstractUIHandler GetPromptUIHandler() {

		AbstractUIHandler handler = new PromptUIHandler(this.prompt);
		return handler;
	}

	/**
	 * Invoked after a file is deleted.
	 *
	 * @param file the file which was deleted
	 * @throws IzPackException for any error
	 */
	@Override
	public void afterDelete(File file) {

		logger.log(Level.FINE, LogPrefix + "afterDelete. File : " + file);

		super.afterDelete(file);
	}

	@Override
	public void beforeDelete(List<File> files, ProgressListener listener) {

		// deleteRegistry();
		logger.log(Level.FINE, LogPrefix + "beforeDelete.  ");
		try {
			super.beforeDelete(files, listener);
		} catch (WrappedNativeLibException exception) {
			GetPromptUIHandler().emitWarning("Error", this.getString("privilegesIssue", AdxCompUninstallerListener.PrivilegesFriendlyMessage));
			throw exception;
		} catch (Exception exception) {
			throw exception;
		}
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

		logger.log(Level.FINE, LogPrefix + "afterDelete.  ");

		super.afterDelete(files, listener);

	}

	private void deleteRegistry() {

		RegistryHandler myHandlerInstance = myhandler.getInstance();
		String unInstallName = myHandlerInstance.getUninstallName();
		// String keyName =
		// "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Sage
		// X3 Management Console";
		// RegistryHandler.UNINSTALL_ROOT =
		// "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\"
		String keyName = RegistryHandler.UNINSTALL_ROOT + unInstallName;
		if (unInstallName == null) {
			logger.log(Level.FINE, LogPrefix + "Error in deleteRegistry: getUninstallName() is empty");
			// myHandlerInstance.setUninstallName("Sage X3 Management Console");
			return;
		}

		logger.log(Level.ALL, LogPrefix + "UninstallName Registry key " + keyName);

		try {
			myHandlerInstance.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);

			if (myHandlerInstance.keyExist(keyName)) {
				myHandlerInstance.deleteValue(keyName, "DisplayVersion");
				// myHandlerInstance.deleteKey(keyName + "\\DisplayVersion");
				myHandlerInstance.deleteKey(keyName);
				logger.log(Level.FINE, LogPrefix + "Registry key " + keyName + " deleted");
			}
		} catch (NativeLibException e) {
			e.printStackTrace();
			logger.log(Level.FINE, LogPrefix + "Error, registry key " + keyName + " NOT deleted");
		}
	}

	private String getString(String resourceId, String defaultTranslation) {
		ResourcesHelper helper = new ResourcesHelper(null, resources);
		helper.mergeCustomMessages(messages);
		String result = helper.getCustomString(resourceId);
		if (result == null)
			result = defaultTranslation;
		return result;
	}

}
