package com.sage.izpack;

import java.util.logging.Logger;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;

/**
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
