package com.sage.izpack;

import com.izforge.izpack.core.os.RegistryHandler;
import java.util.logging.Logger;
import com.izforge.izpack.api.exception.NativeLibException;

/**
 *
 * @author Franck DEPOORTERE
 */
public class RegistryHandlerX3 {
	private static final Logger logger = Logger.getLogger(RegistryHandlerX3.class.getName());

	private RegistryHandler registryHandler;

	public RegistryHandlerX3(RegistryHandler registryHandler) {

		this.registryHandler = registryHandler;
	}

	public RegistryHandler getRegistryHandler() {
		return this.registryHandler;
	}

	public boolean adxadminProductRegistered() throws NativeLibException {

		// String keyName64Bits = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
		// String keyName32Bits = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
		// // TODO: remove 32 bits
		int oldVal = this.registryHandler.getRoot();
		this.registryHandler.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
		boolean retval = this.registryHandler.keyExist(AdxCompInstallerListener.ADXADMIN_REG_KeyName64Bits)
				|| this.registryHandler.keyExist(AdxCompInstallerListener.ADXADMIN_REG_KeyName32Bits);
		this.registryHandler.setRoot(oldVal);
		return (retval);
	}

}
