package com.sage.izpack;

import java.util.logging.Logger;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;

/**
 * Listener Implemented to avoid error on Linux Uninstaller.jar:
 * Error while instantiate AdxCompUninstallerListener
 * Caused by: java.lang.ClassNotFoundException: com.coi.tools.os.win.MSWinConstants 
 * The reference to RegistryDefaultHandler class in the constructor cause the issue.
 * 
 * @author Franck DEPOORTERE
 */
public class AdxCompUninstallerListenerLinux  extends AdxCompUninstallerListenerCommon {

	private static final Logger logger = Logger.getLogger(AdxCompUninstallerListenerLinux.class.getName());
	private static String LogPrefix = "AdxCompUninstallerListenerLinux - ";
	
	public AdxCompUninstallerListenerLinux(Resources resources, Messages messages, Prompt prompt) {
		super(resources, messages, prompt);
	}

	

}
