package com.sage.izpack;

import com.izforge.izpack.core.os.RegistryHandler;
// import com.izforge.izpack.util.OSClassHelper;
import com.izforge.izpack.api.exception.NativeLibException;

/**
 * This class represents a registry handler in a operating system independent way. OS specific
 * subclasses are used to implement the necessary mapping from this generic API to the classes that
 * reflect the system dependent AIP.
 *
 * @author Franck DEPOORTERE
 */
public class RegistryHandlerX3 extends RegistryHandler//  implements MSWinConstants
{
 
    public boolean adxadminProductRegistered() throws NativeLibException
    {
        String keyName = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
        int oldVal = getRoot();
        setRoot(HKEY_LOCAL_MACHINE);
        boolean retval = keyExist(keyName);
        keyName = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN"; // TODO: remove 32 bits
        retval = retval || keyExist(keyName);
        setRoot(oldVal);
        return (retval);        
    }


}
