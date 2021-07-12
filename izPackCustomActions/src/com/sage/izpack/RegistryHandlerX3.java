package com.sage.izpack;

import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.util.DefaultTargetPlatformFactory;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.TargetFactory;

import java.util.logging.Logger;

// import com.izforge.izpack.util.OSClassHelper;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.factory.ObjectFactory;

/**
 * This class represents a registry handler in a operating system independent
 * way. OS specific subclasses are used to implement the necessary mapping from
 * this generic API to the classes that reflect the system dependent AIP.
 *
 * @author Franck DEPOORTERE
 */
public class RegistryHandlerX3 // extends RegistryHandler// implements MSWinConstants
{
	private static final Logger logger = Logger.getLogger(RegistryHandlerX3.class.getName());

	
	private RegistryHandler registryHandler;

	public RegistryHandlerX3(RegistryHandler registryHandler) {

		this.registryHandler = registryHandler;
	}

	public RegistryHandler getRegistryHandler() {
		return this.registryHandler;
	}

	public boolean adxadminProductRegistered() throws NativeLibException {

		String keyName64Bits = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
		String keyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN"; // TODO: remove 32 bits
		int oldVal = this.registryHandler.getRoot();
		this.registryHandler.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
		boolean retval = this.registryHandler.keyExist(keyName64Bits) || this.registryHandler.keyExist(keyName32Bits);
		this.registryHandler.setRoot(oldVal);
		return (retval);
	}

}
